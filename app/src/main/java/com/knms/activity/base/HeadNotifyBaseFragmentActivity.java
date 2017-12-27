package com.knms.activity.base;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.gyf.barlibrary.ImmersionBar;
import com.knms.activity.im.MessageCenterActivity;
import com.knms.activity.login.FasterLoginActivity;
import com.knms.android.R;
import com.knms.core.im.IMHelper;
import com.knms.util.SPUtils;

import rx.Subscription;
import rx.functions.Action1;


/**
 * Created by tdx on 2016/10/21.
 * 继承该类必须要引入top_title_layout布局文件
 */

public abstract class HeadNotifyBaseFragmentActivity extends BaseFragmentActivity{
    protected TextView tv_title_center;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            initHeadView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void initHeadView() throws Exception{
        findView(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finshActivity();
            }
        });
        tv_title_center = findView(R.id.tv_title_center);
        TextView tv_title_right = findView(R.id.tv_title_right);
        ImageView iv_icon_right = findView(R.id.iv_icon_right);
        FrameLayout fl_right = findView(R.id.fl_right);
        setCenterTitleView(tv_title_center);
        fl_right.setVisibility(View.VISIBLE);
        tv_title_right.setVisibility(View.GONE);
        iv_icon_right.setVisibility(View.VISIBLE);
        fl_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SPUtils.isLogin()){
                    startActivityAnimGeneral(MessageCenterActivity.class,null);
                }else{
                    startActivityAnimGeneral(FasterLoginActivity.class,null);
                }
            }
        });
    }
    public abstract void setCenterTitleView(TextView tv_center);

    Subscription subscriptionMsgCount;
    @Override
    protected void onResume() {
        super.onResume();
        if(subscriptionMsgCount != null) subscriptionMsgCount.unsubscribe();
        subscriptionMsgCount = IMHelper.getInstance().isUnreadMsg().subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean aBoolean) {
                if(aBoolean) ((ImageView)findView(R.id.iv_icon_right)).setImageResource(R.drawable.home_03);
                else ((ImageView)findView(R.id.iv_icon_right)).setImageResource(R.drawable.home_12);
            }
        });
    }

    @Override
    protected void onDestroy() {
        if(subscriptionMsgCount != null) subscriptionMsgCount.unsubscribe();
        super.onDestroy();
    }
}
