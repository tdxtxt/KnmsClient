package com.knms.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.knms.activity.base.HeadBaseActivity;
import com.knms.android.R;
import com.knms.app.KnmsApp;
import com.knms.bean.user.User;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.oncall.LoadListener;
import com.knms.util.CommonUtils;
import com.knms.util.SPUtils;
import com.knms.view.webview.SafeWebView;
import com.umeng.analytics.MobclickAgent;


import cn.sharesdk.framework.Platform;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.onekeyshare.ShareContentCustomizeCallback;
import cn.shareuzi.bean.ShareEntity;



/**
 * Created by Administrator on 2016/11/1.
 */
public class CommWebViewActivity extends HeadBaseActivity implements HeadBaseActivity.RightCallBack {
    private SafeWebView mWebView;
    private LinearLayout ll_content;
    private RelativeLayout rl_status;
    private String mUrl;
    private boolean isFailedLoad = false;
    private LinearLayout llBtn_close;
    TextView tv_title;
    ShareEntity shareData = new ShareEntity();
    private ProgressBar progressbar;

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_title = tv_center;
    }

    @Override
    protected void getParmas(Intent intent) {
        super.getParmas(intent);
        mUrl = intent.getStringExtra("url");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findView(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mWebView.canGoBack()) {
                    mWebView.goBack(); // 后退
                    KnmsApp.getInstance().hideLoadView(rl_status);
                    mWebView.setVisibility(View.VISIBLE);
                } else {
                    finshActivity();
                }
            }
        });
        findView(R.id.llBtn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finshActivity();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mUrl = intent.getStringExtra("url");
        initData();
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_partners_layout;
    }

    @Override
    protected void initView() {
        setRightMenuCallBack(this);
        progressbar = findView(R.id.webview_progressbar);
        rl_status = findView(R.id.rl_status);
        ll_content = findView(R.id.ll_content);
        llBtn_close = findView(R.id.llBtn_close);
        mWebView = new SafeWebView(this);
        mWebView.clearCache(true);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mWebView.setLayoutParams(params);
        ll_content.addView(mWebView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW); }
        //不使用缓存：
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.getSettings().setAppCacheEnabled(false);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new CallBack(), "knms");
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setWebViewClient(new WebViewClient() {
            @Override //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("com.kebuyer.user:")) {
                    CommonUtils.startActivity(CommWebViewActivity.this, Uri.parse(url));
                } else if (url.startsWith("tel:")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                } else {
                    CommonUtils.syncCookie(url);
                    return false;
                }
                return true;
            }
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (url.startsWith("com.kebuyer.user:")) {
                    CommonUtils.startActivity(CommWebViewActivity.this, Uri.parse(url));
                    if (url.equals(mUrl)) CommWebViewActivity.this.finshActivity();
                }
                super.onPageStarted(view, url, favicon);
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                if (isFailedLoad || TextUtils.equals(view.getTitle(), "找不到网页") || TextUtils.isEmpty(view.getTitle()))
                    tv_title.setText("加载失败");
                else {
                    tv_title.setText(view.getTitle().trim().length() > 12 ? view.getTitle().trim().substring(0, 12) + "..." : view.getTitle().trim());
                    loadJs(view);
                }
                shareData.setTitle(TextUtils.isEmpty(view.getTitle()) || url.contains(view.getTitle()) ? "铠恩买手" : view.getTitle().trim());
                shareData.setRemark(TextUtils.isEmpty(view.getTitle()) || url.contains(view.getTitle()) ? "铠恩买手" : view.getTitle().trim());
                shareData.setUrl(url);
                if (!isFailedLoad) {
                    mWebView.setVisibility(View.VISIBLE);
                    KnmsApp.getInstance().hideLoadView(rl_status);
                }
                if(view.canGoBack()){
                    llBtn_close.setVisibility(View.VISIBLE);
                }else{
                    llBtn_close.setVisibility(View.GONE);
                }
                super.onPageFinished(view, url);
            }
            @Override//拦截替换网络请求数据,  从API 21开始引入
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return super.shouldInterceptRequest(view, request);
            }
            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                tv_title.setText("加载失败");
                isFailedLoad = true;
                mWebView.setVisibility(View.GONE);
                KnmsApp.getInstance().showDataEmpty(rl_status, "网络走丢了,请检查后重试", R.drawable.no_data_undercarriage, "刷新", new LoadListener() {
                    @Override
                    public void onclick() {
                        isFailedLoad = false;
                        mWebView.reload();
                    }
                });
            }
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//                //handler.cancel(); 默认的处理方式，WebView变成空白页
//                handler.proceed();//接受证书
//                view.reload();
                super.onReceivedSslError(view,handler,error);
            }
        });
        mWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void initData() {
        CommonUtils.syncCookie(mUrl);
        mWebView.loadUrl(mUrl);

    }

    @Override
    public String setStatisticsTitle() {
        return mUrl;
    }

    @Subscribe(tags = {@Tag(BusAction.ACTION_LOGIN)})
    public void refresh(User user) {
        mWebView.reload();
    }

    public void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mWebView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mWebView.onResume();
        }
    }

    @Override
    public void setRightContent(TextView tv, ImageView icon) {
        icon.setImageResource(R.drawable.home_09);
        tv.setVisibility(View.GONE);
    }

    @Override
    public void onclick() {
        if(!(shareData != null && !TextUtils.isEmpty(shareData.getUrl()))) return;
        OnekeyShare oks = new OnekeyShare();
        oks.setShareContentCustomizeCallback(new ShareContentCustomizeCallback() {
            @Override
            public void onShare(Platform platform, Platform.ShareParams paramsToShare) {
                switch (platform.getName()) {
                    case "WechatFavorite":
                        MobclickAgent.onEvent(CommWebViewActivity.this, "h5WeChatShareClick");
                        break;
                    case "WechatMoments":
                        MobclickAgent.onEvent(CommWebViewActivity.this, "h5WeChatMomentsClick");
                        break;
                    case "QZone":
                        MobclickAgent.onEvent(CommWebViewActivity.this, "h5QZoneShareClick");
                        break;
                    case "QQ":
                        MobclickAgent.onEvent(CommWebViewActivity.this, "h5QQShareClick");
                        break;
                }
            }
        });
        oks.show(this, shareData);
    }

    final public class CallBack {
        @JavascriptInterface
        public void systemCopy(String content) {
            CommonUtils.systemCopy(content);
        }

        @JavascriptInterface
        public String getCookie() {
            return SPUtils.getFromApp(SPUtils.KeyConstant.knmsid, "").toString();
        }

        @JavascriptInterface
        public void getShareImageSource(String shareContent, String shareImg) {
            //组装分享对象
            shareData.setRemark(shareContent);
            shareData.setImg(shareImg);
        }
    }

    private void loadJs(WebView webView) {
        webView.loadUrl("javascript:window.knms.getShareImageSource(document.getElementById('knmsAppShare').getAttribute('shareContent'),document.getElementById('knmsAppShare').getAttribute('shareImg'));");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWebView != null) {
            mWebView.clearHistory();
            ((ViewGroup) mWebView.getParent()).removeView(mWebView);
            mWebView.loadUrl("about:blank");
            mWebView.stopLoading();
            mWebView.setWebChromeClient(null);
            mWebView.setWebViewClient(null);
            mWebView.destroy();
            mWebView = null;
        }
    }


    public class WebChromeClient extends android.webkit.WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == 100) {
                progressbar.setVisibility(View.GONE);
            } else {
                if (progressbar.getVisibility() == View.GONE)
                    progressbar.setVisibility(View.VISIBLE);
                progressbar.setProgress(newProgress);
            }
            super.onProgressChanged(view, newProgress);
        }
    }
}