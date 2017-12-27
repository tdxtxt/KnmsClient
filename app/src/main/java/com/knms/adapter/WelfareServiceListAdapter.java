package com.knms.adapter;

import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.knms.activity.dialog.QRcodeActivityDialog;
import com.knms.adapter.baserecycler.BaseQuickAdapter;
import com.knms.adapter.baserecycler.BaseViewHolder;
import com.knms.android.R;
import com.knms.bean.welfareservice.CouponCenter;

import java.util.List;

/**
 * Created by Administrator on 2016/9/26.
 */
public class WelfareServiceListAdapter extends BaseQuickAdapter<CouponCenter> {

    private int type;//1:现金券2：合作券
    private boolean isInvalid;//是否为无效券


    public WelfareServiceListAdapter(List<CouponCenter> data, int type, boolean isInvalid) {
        super(R.layout.item_common_welfareservice, data);
        this.type = type;
        this.isInvalid = isInvalid;
    }

    @Override
    protected void convert(final BaseViewHolder helper, final CouponCenter item) {
        final TextView tvRule = helper.getView(R.id.coupons_rule);
        helper.setText(R.id.coupons_name, item.title);
        helper.setText(R.id.coupons_rule, item.explain);
        helper.setText(R.id.tv_coupon_time, (item.startTime + "—" + item.endTime).replace("-", "."));
        helper.setImageResource(R.id.coupons_state, 0);
        if (type == 1) {
            helper.setBackgroundRes(R.id.welfare_service_centent_layout, R.drawable.yhq_03);
            tvRule.setTextColor(0xffA97700);
            helper.setText(R.id.tv_show_time,item.dateGainType);
            helper.setVisible(R.id.tv_show_time, true);
            helper.setTextColor(R.id.tv_show_time, isInvalid ? Color.parseColor("#999999") : Color.parseColor("#FB6161"));
            helper.setBackgroundRes(R.id.tv_show_time,isInvalid?R.drawable.bg_coupon_invalid :R.drawable.bg_yhq_show_time);
            helper.setText(R.id.coupons_money, item.money);
            helper.setText(R.id.coupons_count, item.quantity > 1 ? "(" + item.quantity + "张)" : "");
            helper.setVisible(R.id.tv_rmb, true);
        } else if (type == 2) {
            helper.setVisible(R.id.get_coupons, false);
            helper.setVisible(R.id.coupons_number, true);
            helper.setVisible(R.id.tv_rmb, false);
            helper.setText(R.id.coupons_number, item.qrcode);
            helper.setVisible(R.id.tv_show_time, false);
            helper.setBackgroundRes(R.id.welfare_service_centent_layout, R.drawable.yhq_04);
            tvRule.setTextColor(0xff008D7A);
            helper.setTextColor(R.id.tv_coupon_time, 0xff008D7A);
            helper.setText(R.id.coupons_money, item.discount);
            helper.setText(R.id.coupons_count, "折");
            helper.setText(R.id.coupons_condition, "折扣券");
        }
        helper.setOnClickListener(R.id.get_coupons, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, QRcodeActivityDialog.class);
                intent.putExtra("coupons", item);
                mContext.startActivity(intent);
            }
        });
        if (isInvalid) {
            helper.setVisible(R.id.get_coupons, false);
            helper.setBackgroundRes(R.id.welfare_service_centent_layout, R.drawable.yhq_10);
            tvRule.setTextColor(0xff999999);
            helper.setTextColor(R.id.tv_coupon_time, 0xff999999);
            //1:已使用2：已过期
            helper.setImageResource(R.id.coupons_state, item.invalidType == 1 ? R.drawable.icon_coupon_used : R.drawable.icon_coupon_expired);
        }
    }
}
