package com.knms.adapter;

import android.content.Context;

import com.knms.bean.other.City;
import com.knms.view.wheel.adapter.AbstractWheelTextAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tdx on 2016/9/9.
 */
public class CityWheelAdapter extends AbstractWheelTextAdapter {
    List<City> data;
    public List<City> getData(){
        return data;
    }
    public CityWheelAdapter(Context context, List<City> data) {
        super(context);
        this.data = data;
        if(this.data == null) this.data = new ArrayList<City>();
    }
    @Override
    protected CharSequence getItemText(int index) {
        return data.get(index).name;
    }
    @Override
    public int getItemsCount() {
        return data.size();
    }
}
