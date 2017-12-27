package cn.shareuzi.channel;

import android.content.Context;
import android.content.Intent;
import android.shop.knms.com.sharelibrary.R;
import android.text.TextUtils;

import cn.helper.Tst;
import cn.shareuzi.bean.ShareEntity;
import cn.shareuzi.interfaces.OnShareListener;
import cn.shareuzi.interfaces.ShareConstant;
import cn.shareuzi.util.ShareUtil;

/**
 * Created by zhanglifeng
 */
public class ShareBySystem extends ShareBase {

    public ShareBySystem(Context context) {
        super(context);
    }

    @Override
    public void share(ShareEntity data, OnShareListener listener) {
        if (data == null || TextUtils.isEmpty(data.getRemark())) {
            Tst.showToast(context,R.string.share_empty_tip);
            return;
        }
        String content;
        if (TextUtils.isEmpty(data.getRemark())) {
            content = data.getTitle() + data.getUrl();
        } else {
            content = data.getRemark() + data.getUrl();
        }

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, content);
        shareIntent.setType("text/plain");
        if(ShareUtil.startActivity(context, Intent.createChooser(
                shareIntent, context.getString(R.string.share_to)))) {
            if (null != listener) {
                listener.onShare(ShareConstant.SHARE_CHANNEL_SYSTEM, ShareConstant.SHARE_STATUS_COMPLETE);
            }
        } else {
            if (null != listener) {
                listener.onShare(ShareConstant.SHARE_CHANNEL_SYSTEM, ShareConstant.SHARE_STATUS_FAILED);
            }
        }
    }
}
