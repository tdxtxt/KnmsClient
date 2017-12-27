package cn.shareuzi.channel;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.shop.knms.com.sharelibrary.R;
import android.text.TextUtils;

import com.tencent.connect.share.QzoneShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.UiError;

import java.util.ArrayList;

import cn.helper.Tst;
import cn.shareuzi.bean.ShareEntity;
import cn.shareuzi.interfaces.OnShareListener;
import cn.shareuzi.interfaces.ShareConstant;

/**
 * Created by zhanglifeng
 */
public class ShareByQZone extends ShareByQQ {

    public ShareByQZone(Context context) {
        super(context);
    }

    @Override
    public void share(ShareEntity data, final OnShareListener listener) {
        if (null == data) {
            return;
        }
        if (context == null || !(context instanceof Activity)) {
            return;
        }
        final Bundle params = new Bundle();
        params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
        params.putString(QzoneShare.SHARE_TO_QQ_TITLE, data.getTitle());//必填
        params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, data.getRemark());//选填
        params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, data.getUrl());//必填
        ArrayList<String> arrayList = new ArrayList<>();
        if (!TextUtils.isEmpty(data.getImg())) {
            arrayList.add(data.getImg());
        }
        params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, arrayList);
        mTencent.shareToQzone((Activity) context, params, new IUiListener() {
            @Override
            public void onComplete(Object o) {
                if (null != listener) {
                    listener.onShare(ShareConstant.SHARE_CHANNEL_QZONE, ShareConstant.SHARE_STATUS_COMPLETE);
                }
                Tst.showToast(context,R.string.share_success);
                Tst.showToast(context,R.string.share_success);
            }
            @Override
            public void onError(UiError uiError) {
                if (null != listener) {
                    listener.onShare(ShareConstant.SHARE_CHANNEL_QZONE, ShareConstant.SHARE_STATUS_FAILED);
                }
                if (null != uiError) {
                    Tst.showToast(context,uiError.errorMessage);
                }
            }
            @Override
            public void onCancel() {
                if (null != listener) {
                    listener.onShare(ShareConstant.SHARE_CHANNEL_QZONE, ShareConstant.SHARE_STATUS_CANCEL);
                }
                Tst.showToast(context,R.string.share_cancel);
            }
        });
    }
}
