package com.knms.activity.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;

import com.gyf.barlibrary.ImmersionBar;
import com.knms.app.KnmsApp;
import com.knms.bean.user.User;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.view.progress.CircleProgressDialog;
import com.knms.android.R;
import com.umeng.analytics.MobclickAgent;

import java.io.Serializable;
import java.util.Map;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.knms.android.R.id.get;

/**
 * Created by tdx on 2016/8/29.
 */
public abstract class BaseFragmentActivity extends FragmentActivity {
    CircleProgressDialog circleProgressDialog;
    protected ImmersionBar mImmersionBar;
    protected <T extends View> T findView(int id) {
        return (T) super.findViewById(id);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        KnmsApp.getInstance().addActivity(this);
        getParmas(getIntent());
        setContentView(layoutResID());
        initBar();
        initView();
        initData();
        RxBus.get().register(this);
    }
    protected void initBar(){
        View barView = findView(R.id.view);
        if(barView == null) return;
        mImmersionBar = ImmersionBar.with(this);
        mImmersionBar.statusBarView(barView)
//                .keyboardEnable(true)  //解决软键盘与底部输入框冲突问题
                .statusBarDarkFont(true, 0.1f)//状态栏字体是深色，不写默认为亮色
                .flymeOSStatusBarFontColor(R.color.status_bar_textcolor);  //修改flyme OS状态栏字体颜色;
        mImmersionBar.init();
    }
    protected void getParmas(Intent intent){};
    protected void reqApi(){};
    protected abstract int layoutResID();
    protected abstract void initView();
    protected abstract void initData();
    public abstract String setStatisticsTitle();
    public <T> Observable.Transformer<T, T> applySchedulers() {
        return new Observable.Transformer<T, T>(){
            @Override
            public Observable<T> call(Observable<T> observable) {
                return observable.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }
    public void showProgress(){
        if(circleProgressDialog == null) circleProgressDialog = new CircleProgressDialog(this);
        if(circleProgressDialog.isShowing()) circleProgressDialog.dismiss();

        circleProgressDialog.showDialog();
    }
    public void hideProgress(){
        if(circleProgressDialog != null) circleProgressDialog.dismiss();
    }
    public void startActivityAnimGeneral(Class activityClazz, Map<String, Object> param) {
        Intent intent = new Intent(this, activityClazz);
        // intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//清除堆栈顶部的
        if (param != null && !param.isEmpty()) {
            for (Map.Entry<String, Object> entry : param.entrySet()) {
                Object obj = entry.getValue();
                if (obj != null) {
                    if (obj instanceof String) {
                        intent.putExtra(entry.getKey(),
                                (String) entry.getValue());
                    } else if (obj instanceof Integer) {
                        intent.putExtra(entry.getKey(),
                                (Integer) entry.getValue());
                    } else {
                        try {
                            intent.putExtra(entry.getKey(),
                                    (Serializable) entry.getValue());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        startActivity(intent);
        if (getParent() != null) {
            getParent().overridePendingTransition(
                    R.anim.in_from_right, R.anim.out_to_left);
        } else {
            overridePendingTransition(R.anim.in_from_right,
                    R.anim.out_to_left);
        }
    }
    public void startActivityForResultAnimGeneral(Class activityClazz, Map<String, Object> param,int code) {
        Intent intent = new Intent(this, activityClazz);
        // intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//清除堆栈顶部的
        if (param != null && !param.isEmpty()) {
            for (Map.Entry<String, Object> entry : param.entrySet()) {
                Object obj = entry.getValue();
                if (obj != null) {
                    if (obj instanceof String) {
                        intent.putExtra(entry.getKey(),
                                (String) entry.getValue());
                    } else if (obj instanceof Integer) {
                        intent.putExtra(entry.getKey(),
                                (Integer) entry.getValue());
                    } else {
                        try {
                            intent.putExtra(entry.getKey(),
                                    (Serializable) entry.getValue());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        startActivityForResult(intent,code);
        if (getParent() != null) {
            getParent().overridePendingTransition(
                    R.anim.in_from_right, R.anim.out_to_left);
        } else {
            overridePendingTransition(R.anim.in_from_right,R.anim.out_to_left);
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finshActivity();
        }
        return super.onKeyDown(keyCode, event);
    }
    public void finshActivity(){
        KnmsApp.getInstance().finishActivity(this);
        this.overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
    }

    @Override
    protected void onDestroy() {
        if (mImmersionBar != null)
            mImmersionBar.destroy();
        RxBus.get().unregister(this);
        if(circleProgressDialog != null && circleProgressDialog.isShowing()){
            circleProgressDialog.dismiss();
            circleProgressDialog = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(setStatisticsTitle());
        MobclickAgent.onResume(this) ;
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(setStatisticsTitle());
        MobclickAgent.onPause(this);
    }
}
