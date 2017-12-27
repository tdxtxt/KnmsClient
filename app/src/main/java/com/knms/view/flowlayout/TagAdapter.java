package com.knms.view.flowlayout;

import android.view.View;

import com.knms.bean.other.Label;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class TagAdapter<T>
{
    private List<T> mTagDatas;
    private OnDataChangedListener mOnDataChangedListener;
    private HashSet<Integer> mCheckedPosList = new HashSet<Integer>();

    public TagAdapter(List<T> datas)
    {
        mTagDatas = datas;
    }

    public TagAdapter(T[] datas)
    {
        mTagDatas = new ArrayList<T>(Arrays.asList(datas));
    }

    interface OnDataChangedListener
    {
        void onChanged();
    }

    void setOnDataChangedListener(OnDataChangedListener listener)
    {
        mOnDataChangedListener = listener;
    }
    public void setSelectdList(List<T> data){
        if(!(data != null && data.size() > 0)) setSelectedList(-1);
        Set<Integer> set = new HashSet<Integer>();
        for (T item: data) {
            for (int i= 0;i<this.mTagDatas.size();i++){
                if(item.toString().equals(mTagDatas.get(i).toString())){
                    set.add(i);
                }
            }
        }
        setSelectedList(set);
    }
    public void setSelectedList(int... poses)
    {
        Set<Integer> set = new HashSet<Integer>();
        for (int pos : poses)
        {
            set.add(pos);
        }
        setSelectedList(set);
    }

    public void setSelectedList(Set<Integer> set)
    {
        mCheckedPosList.clear();
        if (set != null)
            mCheckedPosList.addAll(set);
        notifyDataChanged();
    }

    public List<T> pos2Tag(Set<Integer> set){
        List<T> temp = new ArrayList<T>();
        if(mTagDatas.size() == 0) return temp;
        for (Integer pos : set) {
            if(pos < mTagDatas.size()){
                temp.add(mTagDatas.get(pos));
            }
        }
        return temp;
    }
    public void remove(int position){
        if(mTagDatas != null && mTagDatas.size() > position)
             mTagDatas.remove(position);
        notifyDataChanged();
    }
    HashSet<Integer> getPreCheckedList()
    {
        return mCheckedPosList;
    }


    public int getCount()
    {
        return mTagDatas == null ? 0 : mTagDatas.size();
    }

    public void notifyDataChanged()
    {
        mOnDataChangedListener.onChanged();
    }

    public T getItem(int position)
    {
        return mTagDatas.get(position);
    }
    public List<T> getData(){
        return mTagDatas == null ? new ArrayList<T>():mTagDatas;
    }
    public abstract View getView(FlowLayout parent, int position, T t);

    public boolean setSelected(int position, T t)
    {
        return false;
    }



}