package com.knms.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.knms.bean.IdleClassify;
import com.knms.bean.other.IdleClassifyTab;
import com.knms.bean.other.Tab;
import com.knms.fragment.IdleClassifyChildFragment;
import com.knms.fragment.MallFragment;
import com.knms.fragment.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by tdx on 2016/8/29.
 */
public class IdleClassifyPagerAdapter extends FragmentPagerAdapter {
    private List<IdleClassify> data;
    List<BaseFragment> fragments;
    private int decorateType;

    public IdleClassify getItemObject(int position){
        if(data != null && data.size() > position){
            return data.get(position);
        }
        return null;
    }

    public IdleClassifyPagerAdapter(FragmentManager fm , List<IdleClassify> data) {
        super(fm);
        this.data = data;
        if(fragments == null) fragments = new ArrayList<>();
        for (IdleClassify idleClassify: data) {
            fragments.add(IdleClassifyChildFragment.newInstance(idleClassify.clid));
        }
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
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
        return data.get(position).clname;
    }
}
