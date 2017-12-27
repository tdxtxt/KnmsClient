package com.knms.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.knms.android.R;
import com.knms.bean.other.Label;
import com.knms.view.flowlayout.FlowLayout;
import com.knms.view.flowlayout.TagAdapter;
import java.util.List;

/**
 * Created by tdx on 2016/9/19.
 */
public class LabelSelectAdapter extends TagAdapter<Label> {
    public LabelSelectAdapter(List<Label> datas) {
        super(datas);
    }

    @Override
    public View getView(FlowLayout parent, int position, Label item) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_label,parent,false);
        ((TextView)view.findViewById(R.id.label)).setText(item.name + " ✕");
        view.setBackgroundResource(R.drawable.bg_rectangle_btn);
        return view;
    }
}
