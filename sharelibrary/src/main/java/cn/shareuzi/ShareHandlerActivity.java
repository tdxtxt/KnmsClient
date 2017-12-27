package cn.shareuzi;

import android.content.Intent;
import android.os.Bundle;
import android.shop.knms.com.sharelibrary.R;
import android.view.Window;

import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.constant.WBConstants;
import com.sina.weibo.sdk.exception.WeiboException;

import cn.helper.Tst;
import cn.shareuzi.bean.ShareEntity;
import cn.shareuzi.channel.ShareByEmail;
import cn.shareuzi.channel.ShareByQQ;
import cn.shareuzi.channel.ShareByQZone;
import cn.shareuzi.channel.ShareBySms;
import cn.shareuzi.channel.ShareBySystem;
import cn.shareuzi.channel.ShareByWeibo;
import cn.shareuzi.channel.ShareByWeixin;
import cn.shareuzi.interfaces.OnShareListener;
import cn.shareuzi.interfaces.ShareConstant;

/**
 * Created by zhanglifeng on 16/06/20
 *
 * 分发Activity，主要对分享功能进行分发
 */
public class ShareHandlerActivity extends ShareBaseActivity implements IWeiboHandler.Response, WeiboAuthListener, OnShareListener {

    protected ShareEntity data;

    protected ShareByWeibo shareByWeibo;
    protected ShareByWeixin shareByWeixin;
    protected boolean isInit = true;

    protected boolean isWeiboAuthComplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        Object object = null;
        try {
            //Fuzz问题处理
            object = getIntent().getParcelableExtra(ShareConstant.EXTRA_SHARE_DATA);
        } catch (Exception e) {}
        if (null == object || !(object instanceof ShareEntity)) {
            finish();
            return;
        }
        data = (ShareEntity) object;

        if (savedInstanceState == null) {

            if (null != shareByWeixin) {
                shareByWeixin.unregisterWeixinReceiver();
                shareByWeixin = null;
            }
            switch (channel) {
                case ShareConstant.SHARE_CHANNEL_WEIXIN_CIRCLE:
                    shareByWeixin = new ShareByWeixin(this, ShareConstant.SHARE_CHANNEL_WEIXIN_CIRCLE);
                    shareByWeixin.registerWeixinReceiver();
                    shareByWeixin.share(data, this);
                    break;

                case ShareConstant.SHARE_CHANNEL_WEIXIN_FRIEND:
                    shareByWeixin = new ShareByWeixin(this, ShareConstant.SHARE_CHANNEL_WEIXIN_FRIEND);
                    shareByWeixin.registerWeixinReceiver();
                    shareByWeixin.share(data, this);
                    break;

                case ShareConstant.SHARE_CHANNEL_SINA_WEIBO:
                    shareByWeibo = new ShareByWeibo(this, this);
                    shareByWeibo.share(data, this);
                    break;

                case ShareConstant.SHARE_CHANNEL_QQ:
                    new ShareByQQ(this).share(data, this);
                    break;

                case ShareConstant.SHARE_CHANNEL_QZONE:
                    new ShareByQZone(this).share(data, this);
                    break;

                case ShareConstant.SHARE_CHANNEL_SMS:
                    new ShareBySms(this).share(data, this);
                    break;

                case ShareConstant.SHARE_CHANNEL_EMAIL:
                    new ShareByEmail(this).share(data, this);
                    break;

                case ShareConstant.SHARE_CHANNEL_SYSTEM:
                    new ShareBySystem(this).share(data, this);
                    break;

                default:
                    finishWithResult(channel, ShareConstant.SHARE_STATUS_ERROR);
                    break;
            }
        } else {
            if (null != shareByWeibo) {
                shareByWeibo.onNewIntent(getIntent(), this);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isInit && !isWeiboAuthComplete) {
            finishWithResult(channel, ShareConstant.SHARE_STATUS_ERROR);
        } else {
            isInit = false;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (null != shareByWeibo) {
            shareByWeibo.onNewIntent(intent, this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (null != shareByWeibo) {
            shareByWeibo.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onShare(int channel, int status) {
        finishWithResult(channel, status);
    }

    /**
     * weibo call back
     */
    @Override
    public void onResponse(BaseResponse baseResp) {
        if (baseResp != null) {
            switch (baseResp.errCode) {
                case WBConstants.ErrorCode.ERR_OK:
                    onShare(ShareConstant.SHARE_CHANNEL_SINA_WEIBO, ShareConstant.SHARE_STATUS_COMPLETE);
                    Tst.showToast(this,R.string.share_success);
                    break;
                case WBConstants.ErrorCode.ERR_CANCEL:
                    onShare(ShareConstant.SHARE_CHANNEL_SINA_WEIBO, ShareConstant.SHARE_STATUS_CANCEL);
                    Tst.showToast(this,R.string.share_cancel);
                    break;
                case WBConstants.ErrorCode.ERR_FAIL:
                    onShare(ShareConstant.SHARE_CHANNEL_SINA_WEIBO, ShareConstant.SHARE_STATUS_FAILED);
                    Tst.showToast(this,getString(R.string.share_failed) + "。Error Message: " + baseResp.errMsg);
                    break;
            }
        }
    }

    @Override
    public void onComplete(Bundle bundle) {
        isWeiboAuthComplete = true;
        shareByWeibo = new ShareByWeibo(this, this);
        shareByWeibo.share(data, this);
    }

    @Override
    public void onWeiboException(WeiboException e) {
        finishWithResult(channel, ShareConstant.SHARE_WEIBO_AUTH_CANCEL);
    }

    @Override
    public void onCancel() {
        finishWithResult(channel, ShareConstant.SHARE_WEIBO_AUTH_ERROR);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (null != shareByWeixin) {
            shareByWeixin.unregisterWeixinReceiver();
        }
    }
}
