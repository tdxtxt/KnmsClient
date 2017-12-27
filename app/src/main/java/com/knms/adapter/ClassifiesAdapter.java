package com.knms.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.knms.android.R;
import com.knms.bean.other.Classify;
import com.knms.bean.other.Label;
import com.knms.view.flowlayout.FlowLayout;
import com.knms.view.flowlayout.TagAdapter;

import java.util.List;

/**
 * Created by tdx on 2016/9/19.
 */
public class ClassifiesAdapter extends TagAdapter<Classify> {
    boolean eableSelect = true;//是否点击可选
    boolean isSelect=false;
    public ClassifiesAdapter(List<Classify> datas) {
        super(datas);
    }
    public ClassifiesAdapter(List<Classify> datas, boolean eableSelect) {
        super(datas);
        this.eableSelect = eableSelect;
    }
    public ClassifiesAdapter(List<Classify> datas, boolean eableSelect,boolean isSelect) {
        super(datas);
        this.eableSelect = eableSelect;
        this.isSelect=isSelect;
    }
    @Override
    public View getView(FlowLayout parent, int position, Classify item) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_label,parent,false);
        TextView tv_label = ((TextView)view.findViewById(R.id.label));
        if(isSelect){
            tv_label.setBackgroundResource(R.drawable.bg_rectangle_btn);
        }
        tv_label.setText(item.name);
        if(!eableSelect){
            tv_label.setBackgroundResource(R.drawable.bg_border_gray_rectangle);
        }
        view.setTag(item);
        return view;
    }
}
