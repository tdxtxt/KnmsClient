package com.knms.adapter;

import android.view.View;

import com.knms.activity.MinePriceRationDetailsActivity;
import com.knms.activity.ShopActivityF;
import com.knms.activity.base.BaseActivity;
import com.knms.activity.im.ChatActivity;
import com.knms.adapter.baserecycler.BaseQuickAdapter;
import com.knms.adapter.baserecycler.BaseViewHolder;
import com.knms.android.R;
import com.knms.bean.myparity.MyParity;
import com.knms.util.ImageLoadHelper;
import com.knms.util.StrHelper;
import com.knms.view.CircleImageView;
import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/9/12.
 */
public class MineReplyPricRationAdapter extends BaseQuickAdapter<MyParity.Reply>{
    private int type=0;

    public MineReplyPricRationAdapter(List data,int type) {
        super(R.layout.item_merchant_reply, data);
        this.type=type;
    }

    @Override
    protected void convert(BaseViewHolder helper, MyParity.Reply item) {
        helper.setText(R.id.name, item.shopName);
        helper.setText(R.id.reply_time, type == 5 ? "进店逛逛" : StrHelper.displayTime(item.contactTime, true, true));
        CircleImageView head_picture = helper.getView(R.id.head_picture);
        ImageLoadHelper.getInstance().displayImageHead(mContext,item.shopLogo, head_picture);
    }
}
