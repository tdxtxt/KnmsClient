package com.knms.adapter;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.knms.android.R;
import com.knms.bean.order.Complaints;
import com.umeng.analytics.MobclickAgent;

import java.util.List;

/**
 * Created by Administrator on 2016/9/30.
 */

public class ComplaintsAdapter extends BaseMultiItemQuickAdapter<Complaints, BaseViewHolder> {

    private boolean isNewOrder = false;//区分新旧订单，TRUE：新，不显示展开收起

    public ComplaintsAdapter(List<Complaints> data) {
        super(data);
        addItemType(1, R.layout.item_order_complain1);
        addItemType(2, R.layout.item_order_complain2);
        addItemType(3, R.layout.item_order_complain3);
    }

    public ComplaintsAdapter(List<Complaints> data, boolean isNewOrder) {
        super(data);
        addItemType(1, R.layout.item_order_complain1);
        addItemType(2, R.layout.item_order_complain2);
        addItemType(3, R.layout.item_order_complain3);
        this.isNewOrder = isNewOrder;
    }


    @Override
    protected void convert(final BaseViewHolder helper, final Complaints item) {
        Drawable drawable = !isNewOrder ? mContext.getResources().getDrawable(item.isFold ? R.drawable.icon_flod : R.drawable.icon_un_flod) : null;
        if (drawable != null)
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        switch (item.getItemType()) {
            case 1:
                ((TextView) helper.getView(R.id.tv_fold)).setCompoundDrawables(null, null, drawable, null);
                helper.setText(R.id.complaint_time, item.occreated)
                        .setText(R.id.complaint_content, item.occontent)
                        .setText(R.id.complaint_type, "投诉类型：" + item.occomplaintstype)
                        .addOnClickListener(R.id.tv_complaints_details1);
                ((TextView) helper.getView(R.id.tv_complaints_details1)).getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);

                helper.setBackgroundColor(R.id.view_line, isNewOrder ? Color.parseColor("#FFDC50") : Color.parseColor("#E6E6E6"));

                if (!isNewOrder) {
                    helper.setVisible(R.id.complaint_type, item.isFold)
                            .setText(R.id.tv_fold, item.isFold ? " 收起 " : " 展开 ")
                            .setVisible(R.id.complaint_content, item.isFold);
                    helper.setOnClickListener(R.id.tv_fold, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            item.isFold = !item.isFold;
                            MobclickAgent.onEvent(mContext, item.isFold ? "orderComplaintUnfoldClick" : "orderComplaintfoldClick");
                            notifyDataSetChanged();
                        }
                    });
                }
                break;
            case 2:
                ((TextView) helper.getView(R.id.tv_fold)).setCompoundDrawables(null, null, drawable, null);
                helper.setText(R.id.complaint_time, item.occreated)
                        .setText(R.id.complaint_content, item.occontent)
                        .setText(R.id.complaint_type, "投诉类型：" + item.occomplaintstype)
                        .setText(R.id.accept_time, item.ocservicetime)
                        .addOnClickListener(R.id.tv_complaints_details2);
                ((TextView) helper.getView(R.id.tv_complaints_details2)).getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);

                helper.setBackgroundColor(R.id.view_line1, isNewOrder ? Color.parseColor("#FFDC50") : Color.parseColor("#E6E6E6"))
                        .setBackgroundColor(R.id.view_line2, isNewOrder ? Color.parseColor("#FFDC50") : Color.parseColor("#E6E6E6"));

                helper.setImageResource(R.id.iv_left, isNewOrder ? R.drawable.icon_2 : R.drawable.bg_oval_gray);

                helper.setTextColor(R.id.tv_succed_title, isNewOrder ? Color.parseColor("#000000") : Color.parseColor("#999999"));


                if (!isNewOrder) {
                    helper.setText(R.id.tv_fold, item.isFold ? " 收起 " : " 展开 ").setVisible(R.id.tv_accept_remark, item.isFold)
                            .setVisible(R.id.complaints_one_layout, item.isFold)
                            .setVisible(R.id.view_bottom_accept, item.isFold);
                    helper.setOnClickListener(R.id.tv_fold, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            item.isFold = !item.isFold;
                            MobclickAgent.onEvent(mContext, item.isFold ? "orderComplaintUnfoldClick" : "orderComplaintfoldClick");
                            notifyDataSetChanged();
                        }
                    });
                }
                break;
            case 3:
                ((TextView) helper.getView(R.id.tv_fold)).setCompoundDrawables(null, null, drawable, null);
                helper.setText(R.id.complaint_time, item.occreated)
                        .setText(R.id.complaint_content, item.occontent)
                        .setText(R.id.complaint_type, "投诉类型：" + item.occomplaintstype)
                        .setText(R.id.accept_time, item.ocservicetime)
                        .setText(R.id.dispose_time, item.ocdealwithtime)
                        .setText(R.id.dispose_content, "您的问题客服已处理完成")
                        .addOnClickListener(R.id.tv_complaints_details3);
                ((TextView) helper.getView(R.id.tv_complaints_details3)).getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);

                helper.setBackgroundColor(R.id.view_line1, isNewOrder ? Color.parseColor("#FFDC50") : Color.parseColor("#E6E6E6"))
                        .setBackgroundColor(R.id.view_line2, isNewOrder ? Color.parseColor("#FFDC50") : Color.parseColor("#E6E6E6"))
                        .setBackgroundColor(R.id.view_line3, isNewOrder ? Color.parseColor("#FFDC50") : Color.parseColor("#E6E6E6"));

                helper.setImageResource(R.id.iv_left1, isNewOrder ? R.drawable.icon_2 : R.drawable.bg_oval_gray)
                        .setImageResource(R.id.iv_left2, isNewOrder ? R.drawable.icon_2 : R.drawable.bg_oval_gray);

                helper.setTextColor(R.id.tv_succed_title, isNewOrder ? Color.parseColor("#000000") : Color.parseColor("#999999"))
                        .setTextColor(R.id.tv_accept_title, isNewOrder ? Color.parseColor("#000000") : Color.parseColor("#999999"));

                if (!isNewOrder) {
                    helper.setText(R.id.tv_fold, item.isFold ? " 收起 " : " 展开 ")
                            .setVisible(R.id.dispose_content, item.isFold)
                            .setVisible(R.id.complaints_one_layout, item.isFold)
                            .setVisible(R.id.complaints_two_layout, item.isFold)
                            .setVisible(R.id.view_bottom_dispose, item.isFold);
                    helper.setOnClickListener(R.id.tv_fold, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            item.isFold = !item.isFold;
                            MobclickAgent.onEvent(mContext, item.isFold ? "orderComplaintUnfoldClick" : "orderComplaintfoldClick");
                            notifyDataSetChanged();
                        }
                    });
                }
                break;
        }
    }
}
