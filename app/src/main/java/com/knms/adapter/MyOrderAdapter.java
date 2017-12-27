package com.knms.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.knms.android.R;
import com.knms.bean.order.Order;
import com.knms.util.CommonUtils;

import java.util.List;


public class MyOrderAdapter extends BaseQuickAdapter<Order, BaseViewHolder> {
    private String[] orderState = {"客户退货", "待送货", "送货完成", "已收货", "已评价", "已回复", "已收货"};

    public MyOrderAdapter(List<Order> data) {
        super(R.layout.listview_item_my_order, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Order item) {
        helper.setText(R.id.contract_number, item.orcontractno)
                .setText(R.id.order_time, item.orcreated)
                .setText(R.id.contract_amount, CommonUtils.addMoneySymbol(item.orcontractamount))
                .setText(R.id.tv_oramount_paid, CommonUtils.addMoneySymbol(item.oramountpaid))
                .setVisible(R.id.iv_complaint, item.isComplaint == 2&&item.deliverystate!=-1)
                .setVisible(R.id.tv_order_state,item.deliverystate!=-1)
                .setText(R.id.tv_order_state, item.deliverystate==-1?"":orderState[item.deliverystate])
                .setText(R.id.shop_name, item.orshopname)
                .setVisible(R.id.tv_order_evaluate, true)
                .addOnClickListener(R.id.goto_shop)
                .addOnClickListener(R.id.tv_orderdetails)
                .addOnClickListener(R.id.tv_order_state)
                .addOnClickListener(R.id.iv_complaint)
                .addOnClickListener(R.id.tv_order_evaluate);

        if (item.deliverystate == 2) helper.setText(R.id.tv_order_evaluate, "确认收货");
        else if (item.deliverystate == 6 || item.deliverystate == 3)
            helper.setText(R.id.tv_order_evaluate, "去评价");
        else if (item.deliverystate == 4 || item.deliverystate == 5)
            helper.setText(R.id.tv_order_evaluate, "查看评价");
        else helper.setVisible(R.id.tv_order_evaluate, false);
    }
}
