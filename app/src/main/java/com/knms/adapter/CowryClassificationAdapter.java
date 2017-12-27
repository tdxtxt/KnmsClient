package com.knms.adapter;

import com.knms.adapter.base.CommonAdapter;
import com.knms.adapter.base.ViewHolder;
import com.knms.android.R;
import com.knms.bean.idle.ReIdleClassify;

import android.content.Context;
import android.widget.TextView;

import java.util.List;

public class CowryClassificationAdapter extends CommonAdapter<ReIdleClassify> {
    public int mChooseId = -1;
    private boolean bgColor = false;

    public CowryClassificationAdapter(Context context, List<ReIdleClassify> mDatas) {
        super(context, R.layout.listview_item_cowry_classification1, mDatas);
    }

    @Override
    public void convert(ViewHolder helper, ReIdleClassify data) {
        TextView tv_title = helper.getView(R.id.textview);
        tv_title.setText(data.name);
        int position = (Integer) helper.getConvertView().getTag(R.string.key_tag_position);
        if (mChooseId == position) {
            tv_title.setTextColor(helper.getConvertView().getResources().getColor(R.color.common_red));
            if (bgColor) {
                tv_title.setBackgroundResource(R.color.idle_class2);
            }
        } else {
            tv_title.setTextColor(helper.getConvertView().getResources().getColor(R.color.button_text_on));
            if (bgColor) {
                tv_title.setBackgroundResource(R.color.idle_class1);
            }
        }
    }

    public void setBgColor(boolean bgColor) {
        this.bgColor = bgColor;
    }
}
