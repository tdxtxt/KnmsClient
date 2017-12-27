package com.knms.adapter;

import android.graphics.Color;
import android.text.TextUtils;
import android.widget.TextView;

import com.knms.adapter.baserecycler.BaseQuickAdapter;
import com.knms.adapter.baserecycler.BaseViewHolder;
import com.knms.bean.other.Brand;
import com.knms.android.R;

import java.util.List;

/**
 * Created by tdx on 2016/8/31.
 */
public class BrandAdapter extends BaseQuickAdapter<Brand> {
    public String mSelectItembrandId = "";//表示选择的这一项

    public BrandAdapter(List<Brand> data) {
        super(R.layout.item_menu, data);
//        getData().add(0, new Brand("全部"));
    }

    @Override
    protected void convert(BaseViewHolder helper, Brand item) {
        TextView tv_name = helper.getView(R.id.tv_name);
        tv_name.setText(item.name);
        if(TextUtils.isEmpty(mSelectItembrandId)){
            tv_name.setBackgroundResource(R.drawable.bg_border_black_rectangle);
            tv_name.setTextColor(Color.parseColor("#666666"));
        }else{
            if(mSelectItembrandId.equals(item.id)){
                tv_name.setBackgroundResource(R.drawable.bg_border_red_rectangle);
                tv_name.setTextColor(helper.convertView.getContext().getResources().getColor(R.color.common_red));
            }else{
                tv_name.setBackgroundResource(R.drawable.bg_border_black_rectangle);
                tv_name.setTextColor(Color.parseColor("#666666"));
            }
        }
    }
}
