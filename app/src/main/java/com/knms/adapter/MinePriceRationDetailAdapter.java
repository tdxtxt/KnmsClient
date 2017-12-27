package com.knms.adapter;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.knms.adapter.baserecycler.BaseQuickAdapter;
import com.knms.adapter.baserecycler.BaseViewHolder;
import com.knms.android.R;
import com.knms.bean.other.Pic;
import com.knms.util.ImageLoadHelper;
import com.knms.util.ScreenUtil;

import java.util.List;

/**
 * Created by Administrator on 2016/9/18.
 */

public class MinePriceRationDetailAdapter extends BaseQuickAdapter<Pic> {
    private Context mContext;
    public MinePriceRationDetailAdapter(Context context,int layoutResId, List<Pic> data) {
        super(layoutResId, data);
        mContext = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, Pic item) {
        ImageView picture = helper.getView(R.id.picture);
        if (getData().size() == 1)
            picture.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtil.getScreenWidth()));
        ImageLoadHelper.getInstance().displayImage(mContext,item.url, picture);
    }
}

