package com.knms.adapter;

import android.graphics.Paint;
import android.view.View;
import android.widget.TextView;

import com.knms.activity.ComplaintDetailsActivity;
import com.knms.activity.base.BaseActivity;
import com.knms.adapter.baserecycler.BaseQuickAdapter;
import com.knms.adapter.baserecycler.BaseViewHolder;
import com.knms.android.R;
import com.knms.bean.order.Complaints;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/9/30.
 */

public class OldComplaintsAdapter extends BaseQuickAdapter<Complaints> {


    public OldComplaintsAdapter(List<Complaints> data) {
        super(R.layout.item_order_old_complain, data);

    }

    @Override
    protected void convert(BaseViewHolder helper, final Complaints item) {
        helper.setText(R.id.complaint_time, item.occreated);
        helper.setText(R.id.complaint_content, item.occontent);
        helper.setText(R.id.complaint_type, "投诉类型：" + item.occomplaintstype);
        switch (item.ocstate) {
            case 1:
                helper.getView(R.id.iv_complaint_left).setVisibility(View.INVISIBLE);
                helper.setVisible(R.id.complaints_one_layout, true)
                .setVisible(R.id.complaints_two_layout, false)
                .setVisible(R.id.complaints_three_layout, false)
                .setVisible(R.id.tv_complaints_details1, true)
                .setVisible(R.id.tv_complaints_details3, false)
                .setVisible(R.id.tv_complaints_details2, false);
                break;
            case 2:
                helper.getView(R.id.iv_accept_left).setVisibility(View.INVISIBLE);
                helper.setVisible(R.id.complaints_one_layout, true)
                .setVisible(R.id.complaints_two_layout, true)
                .setVisible(R.id.complaints_three_layout, false)
                .setText(R.id.accept_time, item.ocservicetime)
                .setVisible(R.id.tv_complaints_details2, true)
                .setVisible(R.id.tv_complaints_details1, false)
                .setVisible(R.id.tv_complaints_details3, false)
                .setVisible(R.id.iv_complaint_left,true);

                break;
            case 3:
                helper.setVisible(R.id.complaints_one_layout, true)
                .setVisible(R.id.complaints_two_layout, true)
                .setVisible(R.id.complaints_three_layout, true)
                .setText(R.id.accept_time, item.ocservicetime)
                .setText(R.id.dispose_time, item.ocdealwithtime)
                .setVisible(R.id.tv_complaints_details3, true)
                .setVisible(R.id.tv_complaints_details1, false)
                .setVisible(R.id.tv_complaints_details2, false)
                .setVisible(R.id.iv_accept_left,true)
                .setVisible(R.id.iv_complaint_left,true);
                helper.setText(R.id.dispose_content, "您的问题客服已处理完成");
                break;
        }
        ((TextView) helper.getView(R.id.tv_complaints_details1)).getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        ((TextView) helper.getView(R.id.tv_complaints_details2)).getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        ((TextView) helper.getView(R.id.tv_complaints_details3)).getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        helper.setOnClickListener(R.id.tv_complaints_details1, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, Object> param = new HashMap<String, Object>();
                param.put("complaintsId", item.ocid);
                ((BaseActivity) mContext).startActivityAnimGeneral(ComplaintDetailsActivity.class, param);
            }
        });
        helper.setOnClickListener(R.id.tv_complaints_details2, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, Object> param = new HashMap<String, Object>();
                param.put("complaintsId", item.ocid);
                ((BaseActivity) mContext).startActivityAnimGeneral(ComplaintDetailsActivity.class, param);
            }
        });
        helper.setOnClickListener(R.id.tv_complaints_details3, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, Object> param = new HashMap<String, Object>();
                param.put("complaintsId", item.ocid);
                ((BaseActivity) mContext).startActivityAnimGeneral(ComplaintDetailsActivity.class, param);
            }
        });
    }
}
