package cn.shareuzi.channel;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
public class ShareBySms extends ShareBase {

    public ShareBySms(Context context) {
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

        Uri smsToUri = Uri.parse("smsto:");
        Intent sendIntent = new Intent(Intent.ACTION_VIEW, smsToUri);
        //sendIntent.putExtra("address", "");
        //短信内容
        sendIntent.putExtra("sms_body", content);
        sendIntent.setType("vnd.android-dir/mms-sms");
        if (ShareUtil.startActivity(context, sendIntent)) {
            if (null != listener) {
                listener.onShare(ShareConstant.SHARE_CHANNEL_SMS, ShareConstant.SHARE_STATUS_COMPLETE);
            }
        } else {
            if (null != listener) {
                listener.onShare(ShareConstant.SHARE_CHANNEL_SMS, ShareConstant.SHARE_STATUS_FAILED);
            }
        }
    }
}
