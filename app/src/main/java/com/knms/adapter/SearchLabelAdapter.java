package com.knms.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.knms.android.R;
import com.knms.bean.other.Label;
import com.knms.view.flowlayout.FlowLayout;
import com.knms.view.flowlayout.TagAdapter;

import java.util.List;

/**
 * Created by tdx on 2016/9/19.
 */
public class SearchLabelAdapter extends TagAdapter<Label> {
    boolean eableSelect = true;//是否点击可选

    public SearchLabelAdapter(List<Label> datas) {
        super(datas);
    }

    public SearchLabelAdapter(List<Label> datas, boolean eableSelect) {
        super(datas);
        this.eableSelect = eableSelect;
    }

    @Override
    public View getView(FlowLayout parent, int position, Label item) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_label, parent, false);
        TextView tv_label = ((TextView) view.findViewById(R.id.label));
        tv_label.setText(item.name);
        tv_label.setBackgroundResource(R.drawable.bg_border_gray_solid_rectangle);
        view.setTag(item);
        return view;
    }
}
