package com.knms.activity.main;

import android.content.Intent;
import android.net.Uri;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.gyf.barlibrary.ImmersionBar;
import com.knms.activity.base.BaseActivity;
import com.knms.android.BuildConfig;
import com.knms.android.R;
import com.knms.bean.IndexAd;
import com.knms.bean.ResponseBody;
import com.knms.bean.product.Ad;
import com.knms.net.RxRequestApi;
import com.knms.util.ImageLoadHelper;
import com.knms.util.SPUtils;
import com.knms.util.ScreenUtil;
import com.umeng.analytics.MobclickAgent;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


/**
 * Created by Administrator on 2016/10/26.
 */
public class WelcomeActivity extends BaseActivity {

    private ImageView ivAd;
    private TextView tvCountDown;
    private CountDownTimer mTimer;
    private boolean isLoadSuccess=false;
    private int width=0,height=0;

    @Override
    protected int layoutResID() {
        return R.layout.activity_welcome_layout;
    }

    @Override
    protected void initView() {
        width= ScreenUtil.getScreenWidth();
        height=ScreenUtil.getScreenWidth()/5*7;
        ivAd = (ImageView) findViewById(R.id.iv_first_ad);
        tvCountDown = (TextView) findViewById(R.id.tv_count_down);
        ivAd.setLayoutParams(new RelativeLayout.LayoutParams(width,height));
        findView(R.id.iv_bottom).setVisibility(View.VISIBLE);
    }

    @Override
    protected void initData() {
        if (!BuildConfig.SER_VERSION_CODE.equals(SPUtils.getFromApp(SPUtils.KeyConstant.current_versions, ""))) {
            skipTo(GuidePageActivity.class);
        } else {
            reqApi();
            skipTo(MainActivity.class);
        }
        mImmersionBar = ImmersionBar.with(this);
        mImmersionBar.statusBarView(findView(R.id.view))
//                .keyboardEnable(true)  //解决软键盘与底部输入框冲突问题
                .statusBarDarkFont(true, 0.1f)//状态栏字体是深色，不写默认为亮色
                .flymeOSStatusBarFontColor(R.color.status_bar_textcolor);  //修改flyme OS状态栏字体颜色;;
        mImmersionBar.init();
    }

    Subscription subscription;

    @Override
    protected void reqApi() {
        subscription= RxRequestApi.getInstance().getApiService().getAdvertisement("AA")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<IndexAd>>() {
                    @Override
                    public void call(ResponseBody<IndexAd> indexAdResponseBody) {
                        Ad ad = indexAdResponseBody.data.aa.get(0);
                        if (ad == null) ad = SPUtils.getFirstAd();
                        else SPUtils.saveFirstAd(ad);
                        init(ad);

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Ad ad = SPUtils.getFirstAd();
                        init(ad);
                    }
                });
    }
    private void showSkip(Ad ad){
        if (null == tvCountDown || null == mTimer) return;
        isLoadSuccess=true;
        tvCountDown.setText("跳过\n" + ad.time / 1000 + "s");
        tvCountDown.setVisibility(View.VISIBLE);
        mTimer.start();
        AlphaAnimation appearAnimation = new AlphaAnimation(0, 1);
        appearAnimation.setDuration(2000);
        ivAd.setAnimation(appearAnimation);
    }
    @Override
    public String setStatisticsTitle() {
        return "欢迎页";
    }

    private void init(final Ad ad) {
        if (ad == null){
            skipTo(MainActivity.class);
            return;
        }
        mTimer = new CountDownTimer(ad.time, 1000) {
            @Override
            public void onTick(long l) {
                tvCountDown.setText("跳过\n" + l / 1000 + "s");
            }

            @Override
            public void onFinish() {
                startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                finshActivity();
            }
        };
        ImageLoadHelper.getInstance().displayImage(this,ad.name, ivAd,new RequestListener<String, GlideDrawable>() {
            @Override
            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                showSkip(ad);
                return false;
            }
            @Override
            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                showSkip(ad);
                return false;
            }
        });
        if (TextUtils.isEmpty(ad.url) || ad.url.equals("/#")) {
            ivAd.setEnabled(false);
        }

        tvCountDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MobclickAgent.onEvent(WelcomeActivity.this, "countdownJumpClick");
                startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                finshActivity();
            }
        });
        ivAd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MobclickAgent.onEvent(WelcomeActivity.this, "startAdvClick");
                Intent intent=new Intent(WelcomeActivity.this,MainActivity.class);
                intent.setAction("com.welcome.ad");
                intent.putExtra("uri", Uri.parse(ad.url));
                startActivity(intent);
                finshActivity();
            }
        });

        MobclickAgent.onEvent(WelcomeActivity.this, "startAdvShow");

    }

    private void skipTo(final Class cl) {
        Observable.just("").delay(2000, TimeUnit.MILLISECONDS).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String aLong) {
                        if (isLoadSuccess && cl == MainActivity.class) return;
                        startActivity(new Intent(WelcomeActivity.this, cl));
                        if(subscription!=null&&!subscription.isUnsubscribed()) subscription.unsubscribe();
                        finshActivity();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                        finshActivity();
                    }
                });
    }
    @Override
    public void finshActivity() {
        super.finshActivity();
        if (mTimer != null) mTimer.cancel();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK)
            return false;
        return super.onKeyDown(keyCode, event);
    }
}
