package com.knms.adapter;

import android.content.Context;

import com.knms.adapter.base.CommonAdapter;
import com.knms.adapter.base.ViewHolder;
import com.knms.android.R;

import java.util.List;

/**
 * Created by Administrator on 2016/8/30.
 */
public class CommodityFragmentAdapter extends CommonAdapter {

    public CommodityFragmentAdapter(Context context, List mDatas) {
        super(context, R.layout.item_commodity, mDatas);
    }

    @Override
    public void convert(ViewHolder helper, Object data) {

    }
}
