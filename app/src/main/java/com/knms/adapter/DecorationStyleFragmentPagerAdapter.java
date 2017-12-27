package com.knms.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.knms.bean.style.StyleId;
import com.knms.fragment.DecorationStyleDetailsFragment;

import java.util.List;

/**
 * Created by tdx on 2016/10/25.
 */

public class DecorationStyleFragmentPagerAdapter extends FragmentStatePagerAdapter {
    private SparseArray<Fragment> fragments;
    List<StyleId> styleIds;
    public DecorationStyleFragmentPagerAdapter(FragmentManager fm, List<StyleId> styleIds) {
        super(fm);
        this.styleIds = styleIds;
        fragments = new SparseArray<>(getCount());
    }
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        fragments.put(position, fragment);
        return fragment;
    }
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if(null != fragments.get(position)) fragments.get(position).onDestroy();
        fragments.remove(position);
        super.destroyItem(container,position,object);//切记务必调用此方法，否则内存泄露
    }
    public Fragment getFragment(int position) {
        return fragments.get(position);
    }
    @Override
    public Fragment getItem(int position) {
        Fragment fragment = fragments.get(position);
        if(fragment == null) fragment = DecorationStyleDetailsFragment.newInstance(position + "",styleIds.get(position).inid);
        return fragment;
    }
    @Override
    public int getCount() {
        return (styleIds == null) ? 0 : styleIds.size();
    }

}
