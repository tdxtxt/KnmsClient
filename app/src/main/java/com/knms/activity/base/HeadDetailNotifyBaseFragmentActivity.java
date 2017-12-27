package com.knms.activity.base;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.gyf.barlibrary.ImmersionBar;
import com.knms.activity.im.MessageCenterActivity;
import com.knms.activity.login.FasterLoginActivity;
import com.knms.activity.main.MainActivity;
import com.knms.android.R;
import com.knms.core.im.IMHelper;
import com.knms.util.SPUtils;

import java.util.HashMap;
import java.util.Map;

import rx.Subscription;
import rx.functions.Action1;


/**
 * Created by tdx on 2016/10/21.
 * 继承该类必须要引入top_tab_details布局文件
 */

public abstract class HeadDetailNotifyBaseFragmentActivity extends BaseFragmentActivity{
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
        findView(R.id.home_page).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String,Object> parmas = new HashMap<String, Object>();
                parmas.put("source","mHomePage");
                startActivityAnimGeneral(MainActivity.class, parmas);
            }
        });
        findView(R.id.information).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SPUtils.isLogin()){
                    startActivityAnimGeneral(MessageCenterActivity.class,null);
                }else{
                    startActivityAnimGeneral(FasterLoginActivity.class,null);
                }
            }
        });
        findView(R.id.rl_top).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
    }
    Subscription subscriptionMsgCount;
    public boolean isMsg = false;
    @Override
    protected void onResume() {
        super.onResume();
        if(subscriptionMsgCount != null) subscriptionMsgCount.unsubscribe();
        subscriptionMsgCount = IMHelper.getInstance().isUnreadMsg().subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean aBoolean) {
                isMsg = aBoolean;
                if(aBoolean) ((ImageView)findView(R.id.information)).setImageResource(R.drawable.home_05);
                else ((ImageView)findView(R.id.information)).setImageResource(R.drawable.home_14);
            }
        });
    }

    @Override
    protected void onDestroy() {
        if(subscriptionMsgCount != null) subscriptionMsgCount.unsubscribe();
        super.onDestroy();
    }
}
