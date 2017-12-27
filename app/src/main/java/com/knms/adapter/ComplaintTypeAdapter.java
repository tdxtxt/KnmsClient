package com.knms.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.knms.android.R;
import com.knms.bean.order.ComplaintsType;
import com.knms.util.LocalDisplay;
import com.knms.view.flowlayout.FlowLayout;
import com.knms.view.flowlayout.TagAdapter;

import java.util.List;

/**
 * Created by tdx on 2016/9/19.
 */
public class ComplaintTypeAdapter extends TagAdapter<ComplaintsType> {
    public ComplaintTypeAdapter(List<ComplaintsType> datas) {
        super(datas);
    }

    @Override
    public View getView(FlowLayout parent, int position, ComplaintsType item) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_complaint_type, parent, false);
        TextView tv_ComplaintsType = ((TextView) view.findViewById(R.id.quality_problem));
        tv_ComplaintsType.setText(item.typeName);
        view.setTag(item);
        return view;
    }

    @Override
    public boolean setSelected(int position, ComplaintsType complaintsType) {
        return super.setSelected(position, complaintsType);
    }

}
