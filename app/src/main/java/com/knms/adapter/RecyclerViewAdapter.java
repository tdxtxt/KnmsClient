package com.knms.adapter;

import com.knms.adapter.baserecycler.BaseQuickAdapter;
import com.knms.adapter.baserecycler.BaseViewHolder;
import com.knms.android.R;
import com.knms.bean.other.Label;
import java.util.List;

/**
 * Created by Administrator on 2016/9/14.
 */
public class RecyclerViewAdapter extends BaseQuickAdapter<Label> {
    public RecyclerViewAdapter(List<Label> data) {
        super(R.layout.item_label, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Label item) {
        helper.setText(R.id.label,item.name);
    }
}

