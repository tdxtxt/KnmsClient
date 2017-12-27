package com.knms.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.knms.activity.base.BaseActivity;
import com.knms.activity.pic.ImgBrowerPagerActivity;
import com.knms.adapter.base.RecyclingPagerAdapter;
import com.knms.android.R;
import com.knms.bean.other.Pic;
import com.knms.util.ImageLoadHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tdx on 2016/9/14.
 */
public class BrowserPicAdapterF extends RecyclingPagerAdapter<Pic> {
    private Context context;
    public BrowserPicAdapterF(Context context){
        this.context = context;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup container) {
        Pic item = datas.get(position);
        if(convertView == null) convertView = LayoutInflater.from(context).inflate(R.layout.item_pic,null);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.iv_img);
        ImageLoadHelper.getInstance().displayImageLocal(context,item.url,imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String,Object> param = new HashMap<String, Object>();
                param.put("pics",datas);
                param.put("position",position);
                param.put("showDeleteBtn",true);
                ((BaseActivity)context).startActivityAnimGeneral(ImgBrowerPagerActivity.class,param);
            }
        });
        return convertView;
    }

    @Override
    public int getCount() {
        return (datas == null) ? 0 : datas.size() ;
    }
}
