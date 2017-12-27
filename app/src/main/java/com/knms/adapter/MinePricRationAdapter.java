package com.knms.adapter;

import com.knms.adapter.baserecycler.BaseQuickAdapter;
import com.knms.adapter.baserecycler.BaseViewHolder;
import com.knms.android.R;

import java.util.List;

/**
 * Created by Administrator on 2016/9/12.
 */
public class MinePricRationAdapter extends BaseQuickAdapter{

    public MinePricRationAdapter(List data) {
        super(R.layout.item_mine_price_ration, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Object item) {

    }
}
