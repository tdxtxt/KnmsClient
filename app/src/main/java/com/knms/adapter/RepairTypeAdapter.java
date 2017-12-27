package com.knms.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.knms.android.R;
import com.knms.bean.repair.RepairType;
import com.knms.view.flowlayout.FlowLayout;
import com.knms.view.flowlayout.TagAdapter;

import java.util.List;

/**
 * Created by tdx on 2016/9/19.
 */
public class RepairTypeAdapter extends TagAdapter<RepairType> {
    public RepairTypeAdapter(List<RepairType> datas) {
        super(datas);
    }

    @Override
    public View getView(FlowLayout parent, int position, RepairType item) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_label, parent, false);
        TextView tv_ComplaintsType = ((TextView) view.findViewById(R.id.label));
        tv_ComplaintsType.setText(item.rname);
        view.setTag(item);
        return view;
    }

    @Override
    public boolean setSelected(int position, RepairType complaintsType) {
        return super.setSelected(position, complaintsType);
    }

}
