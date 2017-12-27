package com.knms.adapter;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.knms.bean.welfareservice.CouponCenter;
import com.knms.fragment.MyCouponsFragment;
import com.knms.fragment.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/8/24.
 */
public class MyCouponsViewPagerAdapter extends FragmentPagerAdapter {
    List<BaseFragment> fragmentList = new ArrayList<BaseFragment>();
    List<String> title = new ArrayList<>();

    public MyCouponsViewPagerAdapter(FragmentManager fm, List<BaseFragment> fragmentList, List<String> title) {
        super(fm);
        this.fragmentList = fragmentList;
        this.title = title;
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return title.get(position);
    }

}
