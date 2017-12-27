package com.knms.adapter;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.knms.adapter.base.RecyclingPagerAdapter;
import com.knms.android.R;
import com.knms.bean.GuidePage;
import com.knms.util.LocalDisplay;

import java.util.List;

/**
 * Created by Administrator on 2016/12/6.
 */

public class GuidePageAdapter extends RecyclingPagerAdapter<GuidePage> {
    private Context context;
    public GuidePageAdapter(List<GuidePage> ads, Context context){
        this.datas=ads;
        this.context=context;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup container) {
        if(convertView == null) convertView =View.inflate(container.getContext(), R.layout.item_guidepage_view,null);
        ImageView ivTitle= (ImageView) convertView.findViewById(R.id.iv_title);
        ImageView ivContent= (ImageView) convertView.findViewById(R.id.iv_content);
        if(convertView.getTag() != null){
            if(!convertView.getTag().toString().equals(this.datas.get(position))){
                convertView.setTag(this.datas.get(position));
                convertView.setBackgroundResource(datas.get(position).bgDrawable);
                ivTitle.setImageResource(datas.get(position).titleDrawable);
                ivContent.setImageResource(datas.get(position).contentDrawable);
            }
        }else{
            convertView.setTag(this.datas.get(position));
            convertView.setBackgroundResource(datas.get(position).bgDrawable);
            ivTitle.setImageResource(datas.get(position).titleDrawable);
            ivContent.setImageResource(datas.get(position).contentDrawable);
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return this.datas == null ? 0 : this.datas.size();
    }
}
