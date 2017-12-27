package com.knms.view.webview;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;
import com.knms.android.R;
import com.knms.app.KnmsApp;
import com.knms.util.DialogHelper;
import com.knms.util.LocalDisplay;

/**
 * Created by tdx on 2016/8/22.
 * Android4.2.2以下版本WebView有远程执行代码漏洞
 * 1.解决WebView各种兼容性问题
 * 2.提供基础访问原生能力
 * 3.解决JS漏洞
 */
public class SafeWebView extends WebView {
    private ProgressBar progressbar;
    public SafeWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public SafeWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SafeWebView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        WebSettings webSettings = getSettings();
        webSettings.setSupportZoom(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setDefaultTextEncodingName("utf-8");
        webSettings.setLoadsImagesAutomatically(true);
        //调用JS方法.安卓版本大于17,加上注解 @JavascriptInterface
        webSettings.setJavaScriptEnabled(true);

        saveData(webSettings);

        newWin(webSettings);
        // 设置
        setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK && canGoBack()) { // 表示按返回键
                        goBack(); // 后退
                        return true; // 已处理
                    }
                }
                return false;
            }
        });

        progressbar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        progressbar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LocalDisplay.dp2px(3), 0, 0));
        Drawable draw = getResources().getDrawable(R.drawable.progressbar_color);
        draw.setBounds(progressbar.getProgressDrawable().getBounds());
        progressbar.setProgressDrawable(draw);
        addView(progressbar);
        setWebChromeClient(new WebChromeClient());
    }
    /**
     * HTML5数据存储
     */
    private void saveData(WebSettings mWebSettings) {
        //有时候网页需要自己保存一些关键数据,Android WebView 需要自己设置
        mWebSettings.setDomStorageEnabled(true);
        mWebSettings.setDatabaseEnabled(true);
        mWebSettings.setAppCacheEnabled(true);
        String appCachePath = KnmsApp.getInstance().getCacheDir().getAbsolutePath();
        mWebSettings.setAppCachePath(appCachePath);
    }
    /**
     * 多窗口的问题
     */
    private void newWin(WebSettings mWebSettings) {
        //html中的_bank标签就是新建窗口打开，有时会打不开，需要加以下
        //然后 复写 WebChromeClient的onCreateWindow方法
        mWebSettings.setSupportMultipleWindows(false);
        mWebSettings.setJavaScriptCanOpenWindowsAutomatically(true);
    }
    public class WebChromeClient extends android.webkit.WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == 100) {
                progressbar.setVisibility(GONE);
            } else {
                if (progressbar.getVisibility() == GONE)
                    progressbar.setVisibility(VISIBLE);
                progressbar.setProgress(newProgress);
            }
            super.onProgressChanged(view, newProgress);
        }
    }
}
