package com.knms.adapter;

import android.content.Context;
import android.widget.ImageView;

import com.knms.adapter.base.CommonAdapter;
import com.knms.adapter.base.ViewHolder;
import com.knms.android.R;
import com.knms.bean.myidle.MyIdle;
import com.knms.util.ImageLoadHelper;

import java.util.List;

/**
 * Created by Administrator on 2016/9/2.
 */
public class OnOfferFragmentAdater extends CommonAdapter<MyIdle> {

    public OnOfferFragmentAdater(Context context, List<MyIdle> mDatas) {
        super(context, R.layout.item_on_offer, mDatas);
    }

    @Override
    public void convert(ViewHolder helper, MyIdle data) {
        helper.setText(R.id.describe_content, data.coremark);
        helper.setText(R.id.time, data.goreleasetime);
        helper.setText(R.id.price, "¥"+data.goprice+"");
        helper.setText(R.id.collect_amount, data.collectNumber+"");
        helper.setText(R.id.browse_amount, data.browseNumber+"");
        ImageView img = helper.getView(R.id.img);
        ImageLoadHelper.getInstance().displayImage(mContext,data.coInspirationPic, img);
    }
}
