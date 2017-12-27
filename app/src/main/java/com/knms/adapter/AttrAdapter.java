package com.knms.adapter;

import android.view.View;

import com.knms.adapter.baserecycler.BaseQuickAdapter;
import com.knms.adapter.baserecycler.BaseViewHolder;
import com.knms.android.R;
import com.knms.bean.classification.ClassifyDetail;

import java.util.List;

/**
 * Created by Administrator on 2017/2/9.
 */

public class AttrAdapter extends BaseQuickAdapter<ClassifyDetail.Attribute>{
    public AttrAdapter(List<ClassifyDetail.Attribute> data) {
        super(R.layout.item_goods_attribute, data);
    }
    @Override
    protected void convert(BaseViewHolder helper, ClassifyDetail.Attribute item) {
        helper.setText(R.id.tv_name,item.name).setText(R.id.tv_value,item.value);
        if(helper.getLayoutPosition() == 0){
            helper.getView(R.id.view_top).setVisibility(View.VISIBLE);
        }else {
            helper.getView(R.id.view_top).setVisibility(View.GONE);
        }

        if(helper.getLayoutPosition() == getData().size() - 1){
            helper.getView(R.id.view_bottom).setVisibility(View.VISIBLE);
        }else {
            helper.getView(R.id.view_bottom).setVisibility(View.GONE);
        }

    }
}
