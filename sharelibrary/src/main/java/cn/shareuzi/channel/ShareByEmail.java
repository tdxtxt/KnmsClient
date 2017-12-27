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
public class ShareByEmail extends ShareBase {

    public ShareByEmail(Context context) {
        super(context);
    }

    @Override
    public void share(ShareEntity data, OnShareListener listener) {
        if (data == null || TextUtils.isEmpty(data.getRemark())) {

            Tst.showToast(context,R.string.share_empty_tip);
            return;
        }
        Intent email = new Intent(Intent.ACTION_SENDTO);
        email.setData(Uri.parse("mailto:"));
        //邮件主题
        if (!TextUtils.isEmpty(data.getTitle())) {
            email.putExtra(Intent.EXTRA_SUBJECT, data.getTitle());
        }
        //邮件内容
        String contentt = data.getRemark() + data.getUrl();
        email.putExtra(Intent.EXTRA_TEXT, contentt);
        if (ShareUtil.startActivity(context, email)) {
            if (null != listener) {
                listener.onShare(ShareConstant.SHARE_CHANNEL_EMAIL, ShareConstant.SHARE_STATUS_COMPLETE);
            }
        } else {
            if (null != listener) {
                listener.onShare(ShareConstant.SHARE_CHANNEL_EMAIL, ShareConstant.SHARE_STATUS_FAILED);
            }
        }
    }
}
