package com.knms.adapter;

import android.content.Context;

import com.knms.adapter.base.CommonAdapter;
import com.knms.adapter.base.ViewHolder;
import com.knms.android.R;
import com.knms.bean.repair.MyRepair;
import com.knms.util.ImageLoadHelper;
import com.knms.view.CircleImageView;

import java.util.List;

/**
 * Created by Administrator on 2016/10/18.
 */
public class MyRepairMerchantReplyAdapter extends CommonAdapter<MyRepair.CommentList> {

    public MyRepairMerchantReplyAdapter(Context context, List<MyRepair.CommentList> mDatas) {
        super(context, R.layout.item_merchant_reply, mDatas);
    }

    @Override
    public void convert(ViewHolder helper, MyRepair.CommentList data) {
        helper.setText(R.id.name,data.usnickname);
        helper.setText(R.id.reply_time,data.concattime);
        CircleImageView head_picture = helper.getView(R.id.head_picture);
        ImageLoadHelper.getInstance().displayImageHead(mContext,data.usphoto,head_picture);
    }
}
