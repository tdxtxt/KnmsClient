package com.knms.adapter.order;


import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.knms.android.R;
import com.knms.bean.order.neworder.PreviewOrder;
import com.knms.util.CommonUtils;
import com.knms.util.ImageLoadHelper;
import com.knms.util.LocalDisplay;
import com.knms.util.Tst;
import com.knms.view.DividerItemDecoration;
import com.knms.view.EllipsisEditText;

import java.util.List;

/**
 * Created by Administrator on 2017/7/19.
 */

public class PreviewOrderAdapter extends BaseQuickAdapter<PreviewOrder.OrderTradingBosBean, BaseViewHolder> {

    private String message;
    private TextView textView;
    private boolean isToast = false;

    public PreviewOrderAdapter(List<PreviewOrder.OrderTradingBosBean> data) {
        super(R.layout.item_confirm_order_layout, data);
    }

    public String getCheckMessage() {
        return message;
    }

    public void isToast(boolean isToast) {
        this.isToast = isToast;
    }

    @Override
    protected void convert(final BaseViewHolder helper, final PreviewOrder.OrderTradingBosBean item) {
        helper.setText(R.id.order_shopname, item.shopName)
                .setText(R.id.tv_shop_checkmessage, item.checkMessage)
                .setText(R.id.tv_freight, item.tradingTransportMoneyTitle);
        int productNum = 0;
        for (PreviewOrder.OrderTradingBosBean.OrderTradingCommoditysBean bean :
                item.orderTradingCommoditys) {
            productNum += Integer.parseInt(bean.buyNumber);
        }
        ((TextView) helper.getView(R.id.tv_order_total)).setText(CommonUtils.highlight("共 " + productNum + " 件商品,小计：" + CommonUtils.addMoneySymbol(item.actualMoneyTitle), "[￥|¥]\\d+(\\.\\d+)?"));
        RecyclerView recyclerView = helper.getView(R.id.recycler_order_item);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.HORIZONTAL_LIST, LocalDisplay.dip2px(1), Color.parseColor("#e6e6e6")));
        final EllipsisEditText editText = helper.getView(R.id.et_order_remark);
        editText.setText(item.tradingRemark);
        editText.setHint("建议与卖家沟通后填写（限200字）");

        editText.setTextChangedListener(new EllipsisEditText.TextChangedListener() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                getData().get(helper.getAdapterPosition() - 1).tradingRemark = editText.getText();
            }
        });

        editText.setOnFocusChangeListener(new EllipsisEditText.FocusChangeListener() {
            @Override
            public void onFocusChanged(boolean hasFocus) {
                if (hasFocus)
                    textView = helper.getView(R.id.tv_freight);
            }
        });

        recyclerView.setAdapter(new BaseQuickAdapter<PreviewOrder.OrderTradingBosBean.OrderTradingCommoditysBean, BaseViewHolder>(R.layout.item_order_common_layout, item.orderTradingCommoditys) {
            @Override
            protected void convert(BaseViewHolder helper, PreviewOrder.OrderTradingBosBean.OrderTradingCommoditysBean item) {
                if (TextUtils.isEmpty(message)) {
                    message = item.checkMessage;
                    if (isToast) Tst.showToast(message);
                }
                ((TextView) helper.getView(R.id.tv_product_totalshowmoney)).getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                helper.setText(R.id.tv_product_name, item.showName)
                        .setText(R.id.tv_product_specification, TextUtils.isEmpty(item.checkMessage) ? item.parameterBriefing : item.checkMessage)
                        .setText(R.id.tv_product_totalrealitymoney, CommonUtils.addMoneySymbol(item.realityMoney))
                        .setText(R.id.tv_product_totalshowmoney, CommonUtils.addMoneySymbol(item.showMoney))
                        .setText(R.id.tv_buy_count, "×" + item.buyNumber)
                        .setTextColor(R.id.tv_product_specification, TextUtils.isEmpty(item.checkMessage) ? Color.parseColor("#666666") : Color.parseColor("#FB6161"));
                ImageLoadHelper.getInstance().displayImage(mContext, item.specificationImg, (ImageView) helper.getView(R.id.iv_order_img), LocalDisplay.dp2px(70), LocalDisplay.dp2px(70));
            }
        });
    }

    public void clearEdittextFocus() {
        if (textView != null) {
            textView.setFocusableInTouchMode(true);
            textView.requestFocus();
        }
    }

}
