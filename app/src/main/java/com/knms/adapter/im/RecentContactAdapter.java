package com.knms.adapter.im;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.knms.activity.im.MessageCenterActivity;
import com.knms.adapter.baserecycler.BaseQuickAdapter;
import com.knms.adapter.baserecycler.BaseViewHolder;
import com.knms.android.R;
import com.knms.core.im.IMHelper;
import com.knms.core.im.cache.NimUserInfoCache;
import com.knms.util.ImageLoadHelper;
import com.knms.util.StrHelper;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import java.util.List;

import rx.functions.Action1;

/**
 * Created by tdx on 2016/9/29.
 */

public class RecentContactAdapter extends BaseQuickAdapter<RecentContact> {
    private Context mContext;
    public RecentContactAdapter(Context context,List<RecentContact> data) {
        super(R.layout.item_msg, data);
        mContext = context;
    }
    @Override
    protected void convert(final BaseViewHolder helper, RecentContact item) {
        boolean isTag = IMHelper.getInstance().isTagSet(item, MessageCenterActivity.RECENT_TAG_STICKY);//是否是置顶消息
        if(isTag){
           helper.getConvertView().setBackgroundResource(R.color.blue_EDFAFF);
        }else{
            helper.getConvertView().setBackgroundResource(R.color.color_white);
        }
        final ImageView iv_avatar = helper.getView(R.id.iv_avatar);
        final TextView tv_name = helper.getView(R.id.tv_name);
        NimUserInfo userInfo = NimUserInfoCache.getInstance().getUserInfoFromLocal(item.getContactId());
        String name = "未知账号", avatar = "";
        if(userInfo != null){
            name = userInfo.getName();
            avatar = userInfo.getAvatar();
        }else {
            NimUserInfoCache.getInstance().getUserInfoObserable(item.getContactId())
                    .subscribe(new Action1<NimUserInfo>() {
                        @Override
                        public void call(NimUserInfo nimUserInfo) {
                            if(nimUserInfo != null){
                                tv_name.setText(nimUserInfo.getName());
                                if(TextUtils.isEmpty(nimUserInfo.getAvatar())){
                                    ImageLoadHelper.getInstance().displayImage(mContext,R.drawable.icon_avatar,iv_avatar);
                                }else{
                                    ImageLoadHelper.getInstance().displayImageHead(mContext,nimUserInfo.getAvatar(), iv_avatar);
                                }
                            }
                        }
                    });
        }
        tv_name.setText(name);
        if(TextUtils.isEmpty(avatar)){
            ImageLoadHelper.getInstance().displayImage(mContext,R.drawable.icon_avatar,iv_avatar);
        }else{
            ImageLoadHelper.getInstance().displayImageHead(mContext,avatar, iv_avatar);
        }
        helper.setText(R.id.tv_new_count,item.getUnreadCount() > 99 ? "99+" : item.getUnreadCount() + "")
        .setText(R.id.tv_data, StrHelper.showImTime(item.getTime()))
        .setText(R.id.tv_content,item.getContent());
    }
}
