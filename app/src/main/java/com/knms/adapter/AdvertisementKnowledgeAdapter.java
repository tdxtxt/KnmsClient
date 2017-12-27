package com.knms.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.knms.android.R;
import com.knms.bean.product.Ad;
import com.knms.oncall.BannerOnclick;
import com.knms.util.ImageLoadHelper;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/8/25.
 */
public class AdvertisementKnowledgeAdapter extends PagerAdapter {
    private List<Ad> mPicAdvertisements;
    private List<View> mRecycledViews = new ArrayList<View>();
    private Context mContext;

    public AdvertisementKnowledgeAdapter(Context context,List<Ad> mPicAdvertisements){
        this.mPicAdvertisements = mPicAdvertisements;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mPicAdvertisements == null ? 0 : mPicAdvertisements.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
        mRecycledViews.add((View) object);
    }
    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        ImageView rootView;
        if (mRecycledViews.isEmpty()) {
            rootView = (ImageView) View.inflate(container.getContext(), R.layout.item_image_view,null);
        } else {
            rootView = (ImageView) mRecycledViews.get(0);
            mRecycledViews.remove(0);
        }
        if(mPicAdvertisements.size() <= position){
            return rootView;
        }
        if(rootView.getTag() != null){
            if(!rootView.getTag().toString().equals(mPicAdvertisements.get(position).name)){
                rootView.setTag(mPicAdvertisements.get(position).name);
                ImageLoadHelper.getInstance().displayImage(mContext,mPicAdvertisements.get(position).name, rootView);
            }
        }else{
            rootView.setTag(mPicAdvertisements.get(position).name);
            ImageLoadHelper.getInstance().displayImage(mContext,mPicAdvertisements.get(position).name,rootView);
        }
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BannerOnclick.advertisementClick(v.getContext(), mPicAdvertisements.get(position));
            }
        });
        container.addView(rootView);
        return rootView;
    }
}
