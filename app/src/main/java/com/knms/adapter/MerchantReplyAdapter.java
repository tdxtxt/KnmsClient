package com.knms.adapter;

import android.content.Context;

import com.knms.adapter.base.CommonAdapter;
import com.knms.adapter.base.ViewHolder;
import com.knms.android.R;
import com.knms.bean.myparity.MyParity;
import com.knms.util.ImageLoadHelper;
import com.knms.view.CircleImageView;
import java.util.List;

/**
 * Created by Administrator on 2016/9/14.
 */
public class MerchantReplyAdapter extends CommonAdapter<MyParity.Reply> {

    public MerchantReplyAdapter(Context context, List<MyParity.Reply> mDatas) {
        super(context, R.layout.item_merchant_reply, mDatas);
    }

    @Override
    public void convert(ViewHolder helper, MyParity.Reply data) {
        helper.setText(R.id.name,data.shopName);
        helper.setText(R.id.reply_time,data.contactTime);
        CircleImageView head_picture = helper.getView(R.id.head_picture);
        ImageLoadHelper.getInstance().displayImageHead(mContext,data.shopLogo,head_picture);
    }
}
