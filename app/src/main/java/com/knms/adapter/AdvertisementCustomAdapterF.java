package com.knms.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.knms.adapter.base.RecyclingPagerAdapter;
import com.knms.android.R;
import com.knms.bean.product.Ad;
import com.knms.oncall.BannerOnclick;
import com.knms.util.ImageLoadHelper;

import java.util.List;

/**
 * Created by Administrator on 2016/12/6.
 */

public class AdvertisementCustomAdapterF extends RecyclingPagerAdapter<Ad> {
    private int width,height;
    private Context mContext;
    public AdvertisementCustomAdapterF(Context context,List<Ad> ads, int width, int height){
        this.datas = ads;
        this.width = width;
        this.height = height;
        mContext = context;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup container) {
        if(convertView == null) convertView = View.inflate(container.getContext(), R.layout.item_image_view,null);
        if(convertView.getTag() != null){
            if(!convertView.getTag().toString().equals(this.datas.get(position).name)){
                convertView.setTag(this.datas.get(position).name);
                ImageLoadHelper.getInstance().displayImage(mContext,this.datas.get(position).name, (ImageView) convertView, width,height);
            }
        }else{
            convertView.setTag(this.datas.get(position).name);
            ImageLoadHelper.getInstance().displayImage(mContext,this.datas.get(position).name, (ImageView) convertView,width,height);
        }
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BannerOnclick.advertisementClick(v.getContext(), datas.get(position));
            }
        });
        return convertView;
    }

    @Override
    public int getCount() {
        return this.datas == null ? 0 : this.datas.size();
    }
}
