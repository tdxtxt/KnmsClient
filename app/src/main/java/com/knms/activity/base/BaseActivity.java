package com.knms.activity.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.input.InputManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.gyf.barlibrary.ImmersionBar;
import com.knms.activity.main.ClassificationActivityF;
import com.knms.activity.main.HomePageActivityF;
import com.knms.activity.main.MineActivity;
import com.knms.android.R;
import com.knms.app.KnmsApp;
import com.knms.core.rxbus.RxBus;
import com.knms.util.ImageLoadHelper;
import com.knms.util.Tst;
import com.knms.view.progress.CircleProgressDialog;
import com.umeng.analytics.MobclickAgent;

import java.io.Serializable;
import java.util.Map;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;



/**
 * Created by tdx on 2016/8/22.
 */
public abstract class BaseActivity extends Activity {
    CircleProgressDialog circleProgressDialog;
    protected ImmersionBar mImmersionBar;
    public <T extends View> T findView(int id) {
        return (T) super.findViewById(id);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        KnmsApp.getInstance().addActivity(this);
        getParmas(getIntent());
        setContentView(layoutResID());
        initView();
        initData();
        initBar();
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
    public void refresh(){
        reqApi();
    }
    protected abstract int layoutResID();
    protected abstract void initView();
    protected abstract void initData();
    public String setStatisticsTitle(){return "";}
    public void showProgress(){
        if(isFinishing() || isDestroyed()) return;
        if(circleProgressDialog == null) circleProgressDialog = new CircleProgressDialog(this);
        if(circleProgressDialog.isShowing()) circleProgressDialog.dismiss();

        circleProgressDialog.showDialog();
    }
    public void showProgress(String title){
        if(isFinishing() || isDestroyed()) return;
        if(circleProgressDialog == null){ circleProgressDialog = new CircleProgressDialog(this);
        circleProgressDialog.setText(title);}
        if(circleProgressDialog.isShowing()) circleProgressDialog.dismiss();

        circleProgressDialog.showDialog();
    }
    public void hideProgress(){
        if(isFinishing() || isDestroyed()) return;
        if(circleProgressDialog != null) circleProgressDialog.dismiss();
    }
    /**
     * 隐藏软键盘
     */
    public void hideKeyboard() {
        if (this != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm.isActive() && getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        }
    }

    /**
     * 显示软键盘
     */
    public void showKeyboard() {
        if (this != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInputFromInputMethod(getCurrentFocus()
                    .getWindowToken(), 0);
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }
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
                    } else if(obj instanceof Serializable){
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
                    overridePendingTransition(R.anim.in_from_right,R.anim.out_to_left);
        }
    }
    public void startActivityGeneral(Class activityClazz, Map<String, Object> param) {
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
                    } else if(obj instanceof Serializable){
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
    long firstTime=0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(this instanceof HomePageActivityF||this instanceof ClassificationActivityF||  this instanceof MineActivity){
                long secondTime = System.currentTimeMillis();
                if (secondTime - firstTime >= 1000) {
                    Tst.showToast( "再按一次退出程序");
                    firstTime = secondTime;
                    return true;
                } else {
                    MobclickAgent.onKillProcess(this);//如果开发者调用Process.kill或者System.exit之类的方法杀死进程，请务必在此之前调用它,用来保存统计数据。
                    KnmsApp.getInstance().AppExit(this);
                }
            }
            finshActivity();
        }

        return super.onKeyDown(keyCode, event);
    }
    float mPosX,mPosY,mCurPosX,mCurPosY;
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPosX = event.getX();
                mPosY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                mCurPosX = event.getX();
                mCurPosY = event.getY();
                if(mPosY - mCurPosY > 50||mCurPosY - mPosY > 50) {
                    hideKeyboard();
                }
                break;
        }
        return super.dispatchTouchEvent(event);
    }
    public void finshActivity(){
        hideKeyboard();
        KnmsApp.getInstance().finishActivity(this);
        this.overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
    }
    public <T> Observable.Transformer<T, T> applySchedulers() {
        return new Observable.Transformer<T, T>(){
            @Override
            public Observable<T> call(Observable<T> observable) {
                return observable.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    /*
     *  截取屏幕
     */
    @Nullable
    public Bitmap getIerceptionScreen(Activity activity) {
        // View是你需要截图的View
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap b = view.getDrawingCache();

        // 获取状态栏高度
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            statusBarHeight = 0;
        }

        // 获取屏幕长和高
        int width = activity.getWindowManager().getDefaultDisplay().getWidth();
        int height = activity.getWindowManager().getDefaultDisplay().getHeight();
        // 去掉标题栏
        // Bitmap b = Bitmap.createBitmap(b1, 0, 25, 320, 455);
        Bitmap bitmap = Bitmap.createBitmap(b, 0, statusBarHeight, width, height
                - statusBarHeight);
        view.destroyDrawingCache();
        if (bitmap != null) {
            return bitmap;
        } else {
            return null;
        }
    }
    @Override
    protected void onDestroy() {
        if (mImmersionBar != null)
            mImmersionBar.destroy();  //必须调用该方法，防止内存泄漏，不调用该方法，如果界面bar发生改变，在不关闭app的情况下，退出此界面再进入将记忆最后一次bar改变的状态
        RxBus.get().unregister(this);
        if(circleProgressDialog != null && circleProgressDialog.isShowing()){
            circleProgressDialog.dismiss();
            circleProgressDialog = null;
        }
        KnmsApp.getInstance().onDestroy();//防止内存溢出
        ImageLoadHelper.getInstance().clearMemory(this);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!TextUtils.isEmpty(setStatisticsTitle()))
            MobclickAgent.onPageStart(setStatisticsTitle());
        MobclickAgent.onResume(this) ;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(!TextUtils.isEmpty(setStatisticsTitle()))
            MobclickAgent.onPageEnd(setStatisticsTitle());
        MobclickAgent.onPause(this);
    }
}
