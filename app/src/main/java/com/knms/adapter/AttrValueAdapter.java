package com.knms.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.knms.android.R;
import com.knms.bean.sku.base.Vaule;
import com.knms.view.flowlayout.FlowLayout;
import com.knms.view.flowlayout.TagAdapter;

import java.util.List;

/**
 * Created by Administrator on 2017/7/26.
 */

public class AttrValueAdapter extends TagAdapter<Vaule> {
    public Vaule currentVaule;
    public AttrValueAdapter(List<Vaule> datas) {
        super(datas);
        if(datas != null && datas.size() == 1){
            currentVaule = datas.get(0);
            currentVaule.status = 1;
        }
    }

    @Override
    public View getView(FlowLayout parent, int position, Vaule vaule) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_label,parent,false);
        TextView tv_label = ((TextView)view.findViewById(R.id.label));
        tv_label.setText(vaule.name);
        if(vaule.status == 0){//可选
            tv_label.setBackgroundResource(R.drawable.bg_rectangle_gray);
            tv_label.setTextColor(tv_label.getResources().getColor(R.color.color_black_333333));
        }else if(vaule.status == 1){//已选
            tv_label.setBackgroundResource(R.drawable.bg_rectangle_btn);
            tv_label.setTextColor(tv_label.getResources().getColor(R.color.color_black_333333));
        }else if(vaule.status == 2){//不可选
            tv_label.setBackgroundResource(R.drawable.bg_rectangle_gray);
            tv_label.setTextColor(tv_label.getResources().getColor(R.color.color_gray_999999));
        }

        return view;
    }
}
