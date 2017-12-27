package com.knms.adapter.order;

import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.knms.android.R;
import com.knms.bean.order.neworder.OrderTradingCommoditysBean;
import com.knms.bean.remark.ReportComment;
import com.knms.util.CommonUtils;
import com.knms.util.ImageLoadHelper;
import com.knms.util.LocalDisplay;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/8/16.
 */

public class TradingProductAdapter extends BaseQuickAdapter<OrderTradingCommoditysBean, BaseViewHolder> {
    private String state,orderType;//1：实体 ，2 虚拟

    public TradingProductAdapter(List<OrderTradingCommoditysBean> data, String state,String orderType) {
        super(R.layout.item_order_common_layout, data);
        this.state = state;
        this.orderType=orderType;
    }

    @Override
    protected void convert(BaseViewHolder helper, OrderTradingCommoditysBean item) {
        helper.setText(R.id.tv_product_name, item.showName)
                .setText(R.id.tv_product_totalrealitymoney, CommonUtils.addMoneySymbol(item.realityMoney))
                .setText(R.id.tv_product_totalshowmoney, CommonUtils.addMoneySymbol(item.showMoney))
                .setText(R.id.tv_buy_count, "×" + item.buyNumber)
                .setVisible(R.id.tv_refund, TextUtils.equals(orderType,"1")&&(item.tradingCommodityType.equals("2") || item.tradingCommodityType.equals("3")
                        || (item.tradingCommodityType.equals("1") && (state.equals("300") || state.equals("400")))))
//                .setText(R.id.tv_refund, TextUtils.isEmpty(item.specificationStatusTitle) ? "退款" : item.specificationStatusTitle)
                .addOnClickListener(R.id.tv_refund)
                .setText(R.id.tv_product_specification, item.parameterBriefing);
        if(TextUtils.equals("1",orderType)){
            TextView tvRefund=helper.getView(R.id.tv_refund);
            if(TextUtils.isEmpty(item.specificationStatusTitle)){
                switch ((item.tradingCommodityType)){
                    case "1":
                        tvRefund.setText("退款");
                        break;
                    case "2":
                        tvRefund.setText("退款中");
                        break;
                    case "3":
                        tvRefund.setText("退款完成");
                        break;
                }
            }else{
                tvRefund.setText(item.specificationStatusTitle);
            }
        }
        ImageLoadHelper.getInstance().displayImage(mContext, item.specificationImg, (ImageView) helper.getView(R.id.iv_order_img), LocalDisplay.dp2px(70), LocalDisplay.dp2px(70));
        ((TextView) helper.getView(R.id.tv_product_totalshowmoney)).getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
    }

    public List<ReportComment> createSimpData(){
        List<OrderTradingCommoditysBean> beans = getData();
        List<ReportComment> reportComments = new ArrayList<>();
        if(beans != null && beans.size() > 0){
            for (OrderTradingCommoditysBean bean : beans) {
                if("1".equals(bean.tradingCommodityType)){//只有没有退款的商品才可以被评价
                    ReportComment item = new ReportComment(bean.showId,bean.parameterBriefing,bean.showName,bean.specificationImg);
                    reportComments.add(item);
                }
            }
        }
        return reportComments;
    }
}
