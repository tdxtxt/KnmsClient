package com.knms.adapter.order;


import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.knms.activity.base.BaseActivity;
import com.knms.activity.login.FasterLoginActivity;
import com.knms.activity.mall.order.AppPayOrderDetailsActivity;
import com.knms.android.R;
import com.knms.bean.order.neworder.AppOrder;
import com.knms.bean.order.neworder.OrderTradingCommoditysBean;
import com.knms.bean.remark.ReportComment;
import com.knms.util.CommonUtils;
import com.knms.util.ImageLoadHelper;
import com.knms.util.LocalDisplay;
import com.knms.util.SPUtils;
import com.knms.view.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/7/19.
 */

public class AppOrderAdapter extends BaseQuickAdapter<AppOrder.OrderTradingBosBean, BaseViewHolder> {

    public AppOrderAdapter(List<AppOrder.OrderTradingBosBean> data) {
        super(R.layout.item_order_layout, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, AppOrder.OrderTradingBosBean item) {

        helper.setText(R.id.order_shopname, item.shopName)
                .setText(R.id.tv_trading_status, item.tradingStatusTitle)
                .addOnClickListener(R.id.order_shopname)
                .addOnClickListener(R.id.btn_order_state_right)
                .setVisible(R.id.ll_order_bottom_layout, true)
                .setVisible(R.id.iv_complaint, item.isComplaint == 2)
                .setText(R.id.tv_order_freight, "(" + CommonUtils.addMoneySymbol(item.tradingTransportMoneyTitle) + ")")
                .addOnClickListener(R.id.btn_order_state_left);

        TextView tvShowShopName = helper.getView(R.id.order_shopname);
        tvShowShopName.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(mContext, R.drawable.icon_shop), null, TextUtils.equals("2", item.orderType) ? null : ContextCompat.getDrawable(mContext, R.drawable.qiepian_33), null);

        switch (item.tradingStatus) {
            case "100"://待付款
                helper.setText(R.id.btn_order_state_left, "取消订单")
                        .setText(R.id.btn_order_state_right, "去付款")
                        .setText(R.id.tv_trading_status, TextUtils.isEmpty(item.tradingStatusTitle) ? "待付款" : item.tradingStatusTitle)
                        .setVisible(R.id.btn_order_state_left, true)
                        .setVisible(R.id.btn_order_state_right, true);
                break;
            case "200":
                helper.setVisible(R.id.ll_order_bottom_layout, false)
                        .setText(R.id.tv_trading_status, TextUtils.isEmpty(item.tradingStatusTitle) ? "付款确认中" : item.tradingStatusTitle);
                break;
            case "300"://待送货
                helper.setVisible(R.id.ll_order_bottom_layout, false)
                        .setText(R.id.tv_trading_status, TextUtils.isEmpty(item.tradingStatusTitle) ? "待送货" : item.tradingStatusTitle);
                break;
            case "400"://送货完成
                helper.setText(R.id.tv_trading_status, TextUtils.isEmpty(item.tradingStatusTitle) ? "送货完成" : item.tradingStatusTitle);
                if (TextUtils.equals("2", item.orderType)) {
                    helper.setVisible(R.id.ll_order_bottom_layout, false);
                } else {
                    helper.setVisible(R.id.btn_order_state_left, false)
                            .setVisible(R.id.btn_order_state_right, true)
                            .setText(R.id.btn_order_state_right, "确认收货");
                }
                break;
            case "500"://已完成(待评价，待回复，已回复)
                if (TextUtils.equals("2", item.orderType)) {
                    helper.setVisible(R.id.btn_order_state_right, false)
                            .setVisible(R.id.btn_order_state_left, true)
                            .setText(R.id.tv_trading_status, TextUtils.isEmpty(item.tradingStatusTitle) ? "已完成" : item.tradingStatusTitle)
                            .setText(R.id.btn_order_state_left, "删除订单");
                } else {
                    helper.setVisible(R.id.btn_order_state_left, TextUtils.equals(item.businessmenAppraise, "1"))
                            .setText(R.id.btn_order_state_left, TextUtils.equals(item.businessmenAppraise, "1") ? "删除订单" : "取消订单")
                            .setVisible(R.id.btn_order_state_right, true);
                    if (TextUtils.equals(item.userAppraise, "0")) {
                        helper.setText(R.id.btn_order_state_right, "去评价")
                                .setText(R.id.tv_trading_status, TextUtils.isEmpty(item.tradingStatusTitle) ? "待评价" : item.tradingStatusTitle);
                    } else {
                        helper.setText(R.id.btn_order_state_right, "查看评价")
                                .setText(R.id.tv_trading_status, TextUtils.isEmpty(item.tradingStatusTitle) ? "待回复" : item.tradingStatusTitle);
                    }
                }
                break;
            case "600"://已关闭
                helper.setVisible(R.id.btn_order_state_right, false)
                        .setVisible(R.id.btn_order_state_left, true)
                        .setText(R.id.btn_order_state_left, "删除订单")
                        .setText(R.id.tv_trading_status, TextUtils.isEmpty(item.tradingStatusTitle) ? "已关闭" : item.tradingStatusTitle);
                break;
            case "900"://订单被锁定（无操作）
            default:
                helper.setVisible(R.id.ll_order_bottom_layout, false)
                        .setText(R.id.tv_trading_status, TextUtils.isEmpty(item.tradingStatusTitle) ? "订单被锁定" : item.tradingStatusTitle);
                break;
        }
//        if (TextUtils.equals("请升级APP版本后查看", item.tradingStatusTitle) || item.tradingLocking.equals("1"))
//            helper.setVisible(R.id.ll_order_bottom_layout, false);


        String tradingSummary = "共" + item.effectiveNumber + "件商品，合计：" + CommonUtils.addMoneySymbol(item.actualMoney);
        ((TextView) helper.getView(R.id.tv_order_total)).setText(CommonUtils.highlight(tradingSummary, "[￥|¥]\\d+(\\.\\d+)?"));
        final String orderType = item.orderType;

        final RecyclerView recyclerView = helper.getView(R.id.recycler_order_item);
        recyclerView.setTag(item.tradingId);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.HORIZONTAL_LIST, LocalDisplay.dip2px(1), Color.parseColor("#e6e6e6")));
        recyclerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BaseActivity) mContext).startActivityAnimGeneral(AppPayOrderDetailsActivity.class, null);
            }
        });
        recyclerView.setAdapter(new BaseQuickAdapter<OrderTradingCommoditysBean, BaseViewHolder>(R.layout.item_order_common_layout, item.orderTradingCommoditys) {
            @Override
            protected void convert(final BaseViewHolder helper, final OrderTradingCommoditysBean item) {
                helper.setText(R.id.tv_product_name, item.showName)
                        .setText(R.id.tv_product_totalrealitymoney, CommonUtils.addMoneySymbol(item.realityMoney))
                        .setText(R.id.tv_product_totalshowmoney, CommonUtils.addMoneySymbol(item.showMoney))
                        .setText(R.id.tv_buy_count, "×" + item.buyNumber)
                        .setVisible(R.id.tv_refund, !TextUtils.isEmpty(item.specificationStatusTitle))
                        .setText(R.id.tv_refund, item.specificationStatusTitle)
                        .setBackgroundColor(R.id.tv_refund, Color.parseColor("#ffffff"))
                        .setText(R.id.tv_product_specification, item.parameterBriefing);
                ImageLoadHelper.getInstance().displayImage(mContext, item.specificationImg, (ImageView) helper.getView(R.id.iv_order_img), LocalDisplay.dp2px(70), LocalDisplay.dp2px(70));
                ((TextView) helper.getView(R.id.tv_product_totalshowmoney)).getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                helper.getConvertView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = null;
                        if (!SPUtils.isLogin()) {
                            intent = new Intent(mContext, FasterLoginActivity.class);
                            mContext.startActivity(intent);
                            return;
                        }
                        intent = new Intent(mContext, AppPayOrderDetailsActivity.class);
                        intent.putExtra("orderId", recyclerView.getTag().toString());
                        mContext.startActivity(intent);
                    }
                });
            }
        });
    }

    public List<ReportComment> createSimpData(int position) {
        List<OrderTradingCommoditysBean> beans = getItem(position).orderTradingCommoditys;
        List<ReportComment> reportComments = new ArrayList<>();
        if (beans != null && beans.size() > 0) {
            for (OrderTradingCommoditysBean bean : beans) {
                if ("1".equals(bean.tradingCommodityType)) {//只有没有退款的商品才可以被评价
                    ReportComment item = new ReportComment(bean.showId, bean.parameterBriefing, bean.showName, bean.specificationImg);
                    reportComments.add(item);
                }
            }
        }
        return reportComments;
    }

}
