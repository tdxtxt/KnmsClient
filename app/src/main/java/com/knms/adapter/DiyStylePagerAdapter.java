package com.knms.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.knms.bean.other.Tab;
import com.knms.fragment.DiyChildFragment;

import java.util.List;


/**
 * Created by tdx on 2016/8/29.
 */
public class DiyStylePagerAdapter extends FragmentPagerAdapter {
    private List<Tab> data;
    private int decorateType;

    public Tab getItemObject(int position){
        if(data != null && data.size() > position){
            return data.get(position);
        }
        return null;
    }

    public DiyStylePagerAdapter(FragmentManager fm , List<Tab> data,int decorateType) {
        super(fm);
        this.data = data;
        this.decorateType = decorateType;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public Fragment getItem(int position) {
        return DiyChildFragment.newInstance(this.data.get(position).id,decorateType);
    }

    @Override
    public long getItemId(int position) {
        return data.get(position).hashCode();
    }

    @Override
    public int getCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return data.get(position).title;
    }
}
