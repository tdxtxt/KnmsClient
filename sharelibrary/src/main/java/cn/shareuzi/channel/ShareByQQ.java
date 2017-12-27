package cn.shareuzi.channel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.shop.knms.com.sharelibrary.R;
import android.text.TextUtils;

import com.tencent.connect.share.QQShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import java.util.List;

import cn.helper.Tst;
import cn.shareuzi.bean.ShareEntity;
import cn.shareuzi.interfaces.OnShareListener;
import cn.shareuzi.interfaces.ShareConstant;
import cn.shareuzi.util.ManifestUtil;
import cn.shareuzi.util.ShareUtil;

/**
 * Created by zhanglifeng
 */
public class ShareByQQ extends ShareBase {

    protected Tencent mTencent;

    public ShareByQQ(Context context) {
        super(context);
        mTencent = Tencent.createInstance(ManifestUtil.getTencentQQAppId(context.getApplicationContext()), context.getApplicationContext());
    }

    @Override
    public void share(ShareEntity data, final OnShareListener listener) {
        if (null == data) {
            return;
        }
        if (context == null) {
            return;
        }
        if (!TextUtils.isEmpty(data.getUrl()) && !TextUtils.isEmpty(data.getTitle())) {
            if (!(context instanceof Activity)) {
                return;
            }
            final Bundle params = new Bundle();
            params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
            params.putString(QQShare.SHARE_TO_QQ_TITLE, data.getTitle());//必填
            params.putString(QQShare.SHARE_TO_QQ_SUMMARY, data.getRemark());//选填
            params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, data.getUrl());//必填
            if (!TextUtils.isEmpty(data.getImg())) {
                params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, data.getImg());
            }
            mTencent.shareToQQ((Activity) context, params, new IUiListener() {
                @Override
                public void onComplete(Object o) {
                    if (null != listener) {
                        listener.onShare(ShareConstant.SHARE_CHANNEL_QQ, ShareConstant.SHARE_STATUS_COMPLETE);
                    }
                    Tst.showToast(context,R.string.share_success);
                }
                @Override
                public void onError(UiError uiError) {
                    if (null != listener) {
                        listener.onShare(ShareConstant.SHARE_CHANNEL_QQ, ShareConstant.SHARE_STATUS_FAILED);
                    }
                    if (null != uiError) {
                        Tst.showToast(context,uiError.errorMessage);
                    }
                }
                @Override
                public void onCancel() {
                    if (null != listener) {
                        listener.onShare(ShareConstant.SHARE_CHANNEL_QQ, ShareConstant.SHARE_STATUS_CANCEL);
                    }
                    Tst.showToast(context,R.string.share_cancel);
                }
            });
        } else {
            String activityName = "";
            Intent intentSend = new Intent(Intent.ACTION_SEND);
            intentSend.setType("text/plain");
            List<ResolveInfo> listActivity = context.getPackageManager().queryIntentActivities(intentSend, 0);
            for (ResolveInfo resolveInfo : listActivity) {
                if (TextUtils.equals(ShareConstant.QQ_PACKAGE_NAME, resolveInfo.activityInfo.packageName)) {
                    activityName = resolveInfo.activityInfo.name;
                    break;
                }
            }
            Intent qqIntent = new Intent(Intent.ACTION_SEND);
            qqIntent.setType("text/plain");
            qqIntent.putExtra(Intent.EXTRA_SUBJECT, data.getTitle());
            qqIntent.setClassName(ShareConstant.QQ_PACKAGE_NAME, activityName);
            qqIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            qqIntent.putExtra(Intent.EXTRA_TEXT, data.getRemark());

            if (ShareUtil.startActivity(context, qqIntent)) {
                if (null != listener) {
                    listener.onShare(ShareConstant.SHARE_CHANNEL_QQ, ShareConstant.SHARE_STATUS_COMPLETE);
                }
            } else {
                if (null != listener) {
                    listener.onShare(ShareConstant.SHARE_CHANNEL_QQ, ShareConstant.SHARE_STATUS_FAILED);
                }
            }
        }
    }
}
