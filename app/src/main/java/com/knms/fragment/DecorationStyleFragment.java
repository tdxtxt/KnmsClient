package com.knms.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.knms.android.R;
import com.knms.fragment.base.BaseFragment;

/**
 * Created by Administrator on 2016/8/24.
 */
public class DecorationStyleFragment extends BaseFragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_decoration_style,null);
        return view;
    }

}
