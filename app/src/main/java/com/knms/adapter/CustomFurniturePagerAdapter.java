package com.knms.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.knms.bean.other.CustomType;
import com.knms.bean.other.Tab;
import com.knms.fragment.CustomFurnitureFragment;
import com.knms.fragment.MallFragment;
import com.knms.fragment.ShopCustomFurnitureFragment;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by tdx on 2016/8/29.
 */
public class CustomFurniturePagerAdapter extends FragmentPagerAdapter {
    private List<CustomType> data;
    private List<CustomFurnitureFragment> list = new ArrayList<CustomFurnitureFragment>();
    public CustomType getItemObject(int position){
        if(data != null && data.size() > position){
            return data.get(position);
        }
        return null;
    }
    public CustomFurnitureFragment getItemFragment(int position){
        return list.get(position);
    }
    public CustomFurniturePagerAdapter(FragmentManager fm , List<CustomType> data) {
        super(fm);
        this.data = data;
        for (CustomType CustomType: data) {
            list.add(CustomFurnitureFragment.newInstance(CustomType.id));
        }
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public CustomFurnitureFragment getItem(int position) {
        return list.get(position);
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
        return data.get(position).name;
    }
}
