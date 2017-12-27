package com.knms.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.knms.android.R;
import com.knms.bean.other.Classify;
import com.knms.view.flowlayout.FlowLayout;
import com.knms.view.flowlayout.TagAdapter;

import java.util.List;

/**
 * Created by tdx on 2016/9/19.
 */
public class ClassifiesSelectedAdapter extends TagAdapter<Classify> {
    boolean eableSelect = true;//是否点击可选
    public ClassifiesSelectedAdapter(List<Classify> datas) {
        super(datas);
    }
    public ClassifiesSelectedAdapter(List<Classify> datas, boolean eableSelect) {
        super(datas);
        this.eableSelect = eableSelect;
    }

    @Override
    public View getView(FlowLayout parent, int position, Classify item) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_label,parent,false);
        TextView tv_label = ((TextView)view.findViewById(R.id.label));
        tv_label.setText(item.name);
        if(!eableSelect){
            tv_label.setBackgroundResource(R.drawable.bg_rectangle_btn);
        }
        view.setTag(item);
        return view;
    }
}
