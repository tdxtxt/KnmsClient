package com.knms.adapter;


import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.knms.android.R;
import com.knms.bean.order.Order;

import java.util.List;

/**
 * Created by Administrator on 2017/8/31.
 */

public class NoPaymentOrderAdapter extends BaseQuickAdapter<Order, BaseViewHolder> {
    private String orderId;

    public NoPaymentOrderAdapter(List<Order> data, String orderId) {
        super(R.layout.item_payretainage_choose_order, data);
        this.orderId = orderId;
    }

    @Override
    protected void convert(BaseViewHolder helper, Order item) {
        helper.setText(R.id.tv_order_name, item.orshopname + "  合同号" + item.orcontractno)
                .setChecked(R.id.cb_checked_order, item.orid.equals(orderId))
                .setText(R.id.tv_order_time_money, "订货时间：" + item.orcreated + "合同金额：¥" + item.orcontractamount)
                .addOnClickListener(R.id.cb_checked_order);
    }
}
