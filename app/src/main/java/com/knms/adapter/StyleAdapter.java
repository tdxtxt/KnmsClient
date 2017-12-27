package com.knms.adapter;

import android.graphics.Color;
import android.text.TextUtils;
import android.widget.TextView;

import com.knms.adapter.baserecycler.BaseQuickAdapter;
import com.knms.adapter.baserecycler.BaseViewHolder;
import com.knms.android.R;
import com.knms.bean.other.Style;
import java.util.List;

/**
 * Created by tdx on 2016/8/31.
 */
public class StyleAdapter extends BaseQuickAdapter<Style> {
    public String mSelectedStyleId = "";//表示选择的这一项

    public StyleAdapter(List<Style> data) {
        super(R.layout.item_menu, data);
//        getData().add(0,new Style("全部"));
    }

    @Override
    protected void convert(BaseViewHolder helper, Style item) {
        TextView tv_name = helper.getView(R.id.tv_name);
        tv_name.setText(item.name);
        if(TextUtils.isEmpty(mSelectedStyleId)){
            tv_name.setBackgroundResource(R.drawable.bg_border_black_rectangle);
            tv_name.setTextColor(Color.parseColor("#666666"));
        }else{
            if(mSelectedStyleId.equals(item.id)){
                tv_name.setBackgroundResource(R.drawable.bg_border_red_rectangle);
                tv_name.setTextColor(Color.parseColor("#FB6161"));
            }else{
                tv_name.setBackgroundResource(R.drawable.bg_border_black_rectangle);
                tv_name.setTextColor(Color.parseColor("#666666"));
            }
        }
    }
}
