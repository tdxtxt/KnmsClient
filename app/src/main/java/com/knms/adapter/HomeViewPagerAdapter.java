package com.knms.adapter;

import java.util.ArrayList;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class HomeViewPagerAdapter extends PagerAdapter {

    private ArrayList<ImageView> pagerList;

    public HomeViewPagerAdapter(ArrayList<ImageView> pagerList) {
        this.pagerList = pagerList;
    }

    @Override
    public int getCount() {
        return pagerList != null ? pagerList.size() : 0;
    }

    //用于判断某个view是否与key对象关联
    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == (View) arg1;
    }

    //回收
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    //添加视图
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = pagerList.get(position);
        //判断view父容器是否为空,不为空要先移出后添加
        if (view.getParent() != null) {
            ((ViewGroup) view.getParent()).removeAllViews();
        }
        container.addView(view, 0);
        return view;
    }

}
