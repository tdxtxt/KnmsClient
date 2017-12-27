package com.knms.adapter.base;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.knms.android.R;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/8/25.
 */
public abstract class CommonAdapter<T> extends BaseAdapter {
    protected Context mContext;
    protected List<T> mDatas;
    protected int resId;

    public CommonAdapter(Context context,int resId, List<T> mDatas) {
        this.resId = resId;
        this.mContext = context;
        this.mDatas = mDatas;
        if(this.mDatas == null) this.mDatas = new ArrayList<T>();
    }
    public CommonAdapter(Context context,int resId){
        this.resId = resId;
        this.mContext = context;
        this.mDatas = new ArrayList<T>();
    }
    public List<T> getData(){
        return mDatas;
    }
    public void setNewData(List<T> data){
        if(this.mDatas == null) this.mDatas = new ArrayList<T>();
        mDatas.clear();
        mDatas.addAll(data);
        notifyDataSetChanged();
    }
    public void addData(List<T> data){
        if(this.mDatas == null) this.mDatas = new ArrayList<T>();
        mDatas.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //实例化一个viewHolder
        ViewHolder viewHolder = ViewHolder.get(mContext, convertView, parent, resId, position);
        viewHolder.getConvertView().setTag(this.mDatas.get(position));
        viewHolder.getConvertView().setTag(R.string.key_tag_position,position);
        convert(viewHolder,this.mDatas.get(position));
        viewHolder.getConvertView().setTag(this.mDatas.get(position));
        return viewHolder.getConvertView();
    }
    public abstract void convert(ViewHolder helper, T data);
    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
