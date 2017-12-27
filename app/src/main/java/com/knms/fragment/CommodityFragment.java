package com.knms.fragment;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.knms.adapter.CommodityFragmentAdapter;
import com.knms.android.R;
import com.knms.fragment.base.BaseFragment;

/**
 * Created by Administrator on 2016/8/24.
 */
public class CommodityFragment extends BaseFragment {

    private View view;
    private RecyclerView recyclerView;
    private CommodityFragmentAdapter mCommodityFragmentAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.fragment_commodity,null);
       
        return view;
    }

}
