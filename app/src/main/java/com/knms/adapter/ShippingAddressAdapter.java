package com.knms.adapter;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.knms.android.R;
import com.knms.bean.address.ShippingAddres;

import java.util.List;

/**
 * Created by Administrator on 2017/7/27.
 */

public class ShippingAddressAdapter extends BaseQuickAdapter<ShippingAddres.orderMailingAddressBos, BaseViewHolder> {

    private boolean isEditable = false;
    private int selectPos;

    public ShippingAddressAdapter(List<ShippingAddres.orderMailingAddressBos> data) {
        super(R.layout.item_order_address_layout, data);
    }

    public ShippingAddressAdapter(List<ShippingAddres.orderMailingAddressBos> data, boolean isEditable) {
        super(isEditable ? R.layout.item_order_address_manager_layout : R.layout.item_order_address_layout, data);
        this.isEditable = isEditable;
    }

    public int getSelectPos(){
        return  selectPos;
    }

    @Override
    protected void convert(final BaseViewHolder helper, final ShippingAddres.orderMailingAddressBos item) {
        if(item.addresstype.equals("1")){selectPos=helper.getAdapterPosition();}
        helper.setText(R.id.tv_mailingAddress, item.mailingaddress)
                .setText(R.id.tv_mailingName, item.mailingname)
                .setText(R.id.tv_mailingPhone, item.mailingphone)
                .setVisible(R.id.iv_arrow_right,false);
        if(!isEditable&&item.addresstype.equals("1"))setText((TextView) helper.getView(R.id.tv_mailingAddress));
        else if (isEditable) {
            helper.setChecked(R.id.cb_default_address,TextUtils.equals("1",item.addresstype));
            helper.addOnClickListener(R.id.tv_edit_address)
                    .addOnClickListener(R.id.cb_default_address)
                    .addOnClickListener(R.id.tv_delete_addres);
        }
    }

    private void setText(TextView mTextView){
        ForegroundColorSpan redSpan = new ForegroundColorSpan(mContext.getResources().getColor(R.color.common_red));
        String str="[默认地址]"+mTextView.getText().toString();
        SpannableStringBuilder builder = new SpannableStringBuilder(str);
        builder.setSpan(redSpan, 0, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTextView.setText(builder);
    }
}
