package com.knms.adapter.fragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.knms.bean.other.Tab;
import com.knms.fragment.BaokMallFragment;
import com.knms.fragment.MallFragment;
import com.knms.fragment.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2017/6/1.
 */

public class MallFragmentAdapter extends FragmentStatePagerAdapter{
    List<Tab> tabs;
    List<BaseFragment> fragments;
    public MallFragmentAdapter(FragmentManager fm,List<Tab> tabs) {
        super(fm);
        this.tabs = tabs;
        if(fragments == null) fragments = new ArrayList<>();
        for (Tab tab: tabs) {
            fragments.add(MallFragment.newInstance(tab.id));
        }
    }

    public MallFragmentAdapter(FragmentManager fm,List<Tab> tabs,boolean isBaok) {
        super(fm);
        this.tabs = tabs;
        if(fragments == null) fragments = new ArrayList<>();
        for (Tab tab: tabs) {
            fragments.add(BaokMallFragment.newInstance(tab.id));
        }
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return tabs.size();
    }
    @Override
    public Object instantiateItem(ViewGroup arg0, int arg1) {
        return super.instantiateItem(arg0, arg1);
    }
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
    }
    @Override
    public CharSequence getPageTitle(int position) {
        return tabs.get(position).title;
    }
}
