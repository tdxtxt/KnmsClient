package com.knms.activity.main;

import android.annotation.TargetApi;
import android.app.TabActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;

import com.gyf.barlibrary.ImmersionBar;
import com.knms.activity.im.MessageCenterActivity;
import com.knms.activity.login.FasterLoginActivity;
import com.knms.activity.pic.CameraActivityF;
import com.knms.android.R;
import com.knms.app.ForegroundCallbacks;
import com.knms.app.KnmsApp;
import com.knms.bean.ResponseBody;
import com.knms.bean.other.BBPrice;
import com.knms.bean.other.TipNum;
import com.knms.bean.user.User;
import com.knms.core.badgenumber.BadgeNumberManager;
import com.knms.core.im.IMHelper;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.core.storage.Svn;
import com.knms.core.upgrade.UpdateHelper;
import com.knms.net.NetworkHelper;
import com.knms.net.RxRequestApi;
import com.knms.util.CommonUtils;
import com.knms.util.ConstantObj;
import com.knms.util.L;
import com.knms.util.LocalDisplay;
import com.knms.util.SPUtils;
import com.knms.util.Tst;
import com.knms.view.GuideView;
import com.knms.view.menu.ExpandableButtonMenu;
import com.knms.view.menu.ExpandableMenuOverlay;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.NimIntent;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.auth.OnlineClient;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.RecentContact;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.TimeUnit;

import cn.jpush.android.api.JPushInterface;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends TabActivity {

    private TabHost mTabHost;

    private FrameLayout mHomePage, mClassification;
    private RelativeLayout mPriceRatio,mMsgCenter,mMine;
    private ImageView imgHomePage, imgClassification, imgWelfareService,
            imgMine, imgPriceRatio;
    private TextView tv_new_count;
    private ExpandableMenuOverlay menuOverlay;
    private java.util.Observer unreadObserver;

    private static final String HOME = "HOME";
    private static final String CLASSIFICATION = "CLASSIFICATION";
    private static final String MSGCENTER = "MSGCENTER";
    private static final String MINE = "MINE";
    private static final String PRIVE_RATIO = "PRIVE_RATIO";
    private String source;
//    private boolean isTipMsg = false;//false没有红点；
    private boolean isClickMsgMenu = false;//是否点击消息中心按钮
//    private int unreadCount = 0;//未读消息条数
    ImmersionBar mImmersionBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getParams(getIntent());
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        source = intent.getStringExtra("source");
        initBar();
        initView();
        notifyMsgTip("");
        RxBus.get().register(this);
        registerObservers(true);
        updateCheck();
        startIntervalTask(1);
        KnmsApp.getInstance().addActivity(this);
    }
    protected void initBar(){
        View barView = findViewById(R.id.view);
        if(barView == null) return;
        mImmersionBar = ImmersionBar.with(this);
        mImmersionBar.statusBarView(barView)
//                .keyboardEnable(true)  //解决软键盘与底部输入框冲突问题
                .statusBarDarkFont(true, 0.1f)//状态栏字体是深色，不写默认为亮色
                .flymeOSStatusBarFontColor(R.color.status_bar_textcolor);  //修改flyme OS状态栏字体颜色;
        mImmersionBar.init();
    }
    private void updateCheck(){
        UpdateHelper updateHelper = new UpdateHelper.Builder(this)
                .isAutoInstall(false)
                .isThinkTime(false)
                .build();
        updateHelper.check();
    }

    private void getParams(Intent intent) {
        if (intent != null) {
            if (intent.hasExtra(NimIntent.EXTRA_NOTIFY_CONTENT)) {//IM聊天跳转
                ArrayList<IMMessage> messages = (ArrayList<IMMessage>) intent.getSerializableExtra(NimIntent.EXTRA_NOTIFY_CONTENT);
                Intent toIntent = new Intent(this, MessageCenterActivity.class);
                if (messages == null || messages.size() > 1) {
                    startActivity(toIntent);
                } else {
                    toIntent.putExtra(NimIntent.EXTRA_NOTIFY_CONTENT, messages.get(0));
                    startActivity(toIntent);
                }
                return;
            }else if(intent.hasExtra(JPushInterface.EXTRA_EXTRA)){//极光推送跳转
                String json = intent.getStringExtra(JPushInterface.EXTRA_EXTRA);
                CommonUtils.startActivity(this,json);
            }
            if (Intent.ACTION_VIEW == intent.getAction()) {//外部h5链接跳转
                Uri uri = intent.getData();
                if (uri != null) {
                    CommonUtils.startActivity(this, uri);
                }
            }else if(TextUtils.equals(intent.getAction(),"com.welcome.ad")){//闪屏页广告点击跳转
                Uri uri = intent.getParcelableExtra("uri");
                if (uri != null) {
                    CommonUtils.startActivity(this, uri);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isClickMsgMenu = false;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void initView() {
        mTabHost = getTabHost();
        //首页
        Intent homeIntent = new Intent(this, HomePageActivityF.class);
        mTabHost.addTab(mTabHost.newTabSpec(HOME).setIndicator(HOME)
                .setContent(homeIntent));
        //分类
        Intent classificationIntent = new Intent(this,
                ClassificationActivityF.class);
        mTabHost.addTab(mTabHost.newTabSpec(CLASSIFICATION)
                .setIndicator(CLASSIFICATION).setContent(classificationIntent));
        //消息列表
        Intent msgCenterIntent = new Intent(this,
                MessageCenterActivity.class);
        msgCenterIntent.putExtra("from", "mainTab");
        mTabHost.addTab(mTabHost.newTabSpec(MSGCENTER)
                .setIndicator(MSGCENTER).setContent(msgCenterIntent));
        //个人中心
        Intent mineIntent = new Intent(this, MineActivity.class);
        mTabHost.addTab(mTabHost.newTabSpec(MINE).setIndicator(MINE)
                .setContent(mineIntent));

        mHomePage = (FrameLayout) findViewById(R.id.home_page);
        mClassification = (FrameLayout) findViewById(R.id.classification);
        mMsgCenter = (RelativeLayout) findViewById(R.id.welfare_service);
        mMine = (RelativeLayout) findViewById(R.id.mine);
        mPriceRatio = (RelativeLayout) findViewById(R.id.price_ratio);

        mHomePage.setOnClickListener(onClickListener);
        mClassification.setOnClickListener(onClickListener);
        mMsgCenter.setOnClickListener(onClickListener);
        mMine.setOnClickListener(onClickListener);
        mPriceRatio.setOnClickListener(onClickListener);

        imgHomePage = (ImageView) findViewById(R.id.img_home_page);
        imgClassification = (ImageView) findViewById(R.id.img_classification);
        imgWelfareService = (ImageView) findViewById(R.id.img_welfare_service);
        imgMine = (ImageView) findViewById(R.id.img_mine);
        imgPriceRatio = (ImageView) findViewById(R.id.img_price_ratio);

        tv_new_count = (TextView) findViewById(R.id.tv_new_count);

        /**高斯模糊处理**/
        menuOverlay = (ExpandableMenuOverlay) findViewById(R.id.menu_overlay);
        menuOverlay.setOnMenuButtonClickListener(new ExpandableButtonMenu.OnMenuButtonClick() {
            @Override
            public void onClick(ExpandableButtonMenu.MenuButton action) {
                switch (action) {
                    case LEFT:
                        startActivity(new Intent(MainActivity.this, CameraActivityF.class));
                        menuOverlay.hide();
                        break;
                    case RIGHT://从草稿中发布
                        Intent intent = new Intent(MainActivity.this, PriceRationActivity.class);
                        intent.putExtra("isDraft", true);
                        startActivity(intent);
                        menuOverlay.hide();
                        break;
                }
            }
        });
        menuOverlay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                BBPrice bbPrice = SPUtils.getSerializable("draft", BBPrice.class);
                if (bbPrice == null) {
                    startActivity(new Intent(MainActivity.this, CameraActivityF.class));
                } else {
                    menuOverlay.show();
                }
            }
        });
        showGriade(mPriceRatio);
        unreadObserver = new java.util.Observer() {
            @Override
            public void update(Observable observable, Object data) {
                if(data instanceof TipNum) {
                    TipNum tipNum = (TipNum) data;
                    if(tipNum == null) return;
                    if(tipNum.getTotal() > 0){
                        findViewById(R.id.iv_tip_mine).setVisibility(View.VISIBLE);
                    }else{
                        findViewById(R.id.iv_tip_mine).setVisibility(View.GONE);
                    }
                }
            }
        };
        KnmsApp.getInstance().getUnreadObservable().addObserver(unreadObserver);
        KnmsApp.getInstance().getUnreadObservable().sendData();
    }
    private void showGriade(View view){
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.bg_griade_bbj);
        new GuideView.Builder()
                .setTargetView(view)    // 必须调用，设置需要Guide的View
                .setCustomGuideView(imageView)  // 必须调用，设置GuideView，可以使任意View的实例，比如ImageView 或者TextView
                .setDirction(GuideView.Direction.TOP)   // 设置GuideView 相对于TargetView的位置，有八种，不设置则默认在屏幕左上角,其余的可以显示在右上，右下等等
                .setShape(GuideView.MyShape.CIRCULAR)   // 设置显示形状，支持圆形，椭圆，矩形三种样式，矩形可以是圆角矩形，
                .setOnclickExit(true)   // 设置点击消失，可以传入一个Callback，执行被点击后的操作
                .setRadius(LocalDisplay.dp2px(32))
//                .setCenter(300, 300)    // 设置圆心，默认是targetView的中心
                .setOffset(0, -10)
                // 设置偏移，一般用于微调GuideView的位置
                .showOnce()             // 设置首次显示，设置后，显示一次后，不再显示
                .build(this)                // 必须调用，Buider模式，返回GuideView实例
                .show();                // 必须调用，显示GuideView
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        source = intent.getStringExtra("source");
        if (!TextUtils.isEmpty(source)) {
            if ("mHomePage".equals(source)) {
                setHomePage();
            }else if(ConstantObj.LOGIN_OUT.equals(source)){
                Intent loginIntent = new Intent(this, FasterLoginActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);//如果在栈中发现存在Activity实例，则清空这个实例之上的Activity
                startActivity(loginIntent);
            }
        }
        getParams(intent);
    }

    OnClickListener onClickListener = new OnClickListener() {
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onClick(View v) {
            if (v == mHomePage) {
                setHomePage();
            } else if (v == mClassification) {
              setClassification();
            } else if (v == mMsgCenter) {
                if (SPUtils.isLogin()) {
                    setMsgCenterPage();
                } else {
                    isClickMsgMenu = true;
                    Intent intent = new Intent(MainActivity.this, FasterLoginActivity.class);
                    startActivity(intent);
                }
            } else if (v == mMine) {
                setMine();
            } else if (v == mPriceRatio) {
                mTabHost.setCurrentTabByTag(PRIVE_RATIO);
                imgHomePage.setImageResource(R.drawable.wode_sy_off);
                imgClassification.setImageResource(R.drawable.wode_fl_off);
                imgWelfareService.setImageResource(R.drawable.wode_fls_off);
                imgMine.setImageResource(R.drawable.wode_wd_off);
                imgPriceRatio.setImageResource(R.drawable.wode_bbj);
            }
        }
    };

    private void setClassification(){
        mTabHost.setCurrentTabByTag(CLASSIFICATION);
        imgHomePage.setImageResource(R.drawable.wode_sy_off);
        imgClassification.setImageResource(R.drawable.wode_fl_on);
        imgWelfareService.setImageResource(R.drawable.wode_fls_off);
        imgMine.setImageResource(R.drawable.wode_wd_off);
        imgPriceRatio.setImageResource(R.drawable.wode_bbj);
        loadUnreadCount();
    }
    private void setMine() {
        mTabHost.setCurrentTabByTag(MINE);
        imgHomePage.setImageResource(R.drawable.wode_sy_off);
        imgClassification.setImageResource(R.drawable.wode_fl_off);
        loadUnreadCount();
        imgMine.setImageResource(R.drawable.wode_wd_on);
        imgPriceRatio.setImageResource(R.drawable.wode_bbj);
    }

    private void setHomePage() {
        mTabHost.setCurrentTabByTag(HOME);
        imgHomePage.setImageResource(R.drawable.wode_sy_on);
        imgClassification.setImageResource(R.drawable.wode_fl_off);
        imgMine.setImageResource(R.drawable.wode_wd_off);
        imgPriceRatio.setImageResource(R.drawable.wode_bbj);
        loadUnreadCount();
    }

    private void setMsgCenterPage() {
        mTabHost.setCurrentTabByTag(MSGCENTER);
        loadUnreadCount();
        imgHomePage.setImageResource(R.drawable.wode_sy_off);
        imgClassification.setImageResource(R.drawable.wode_fl_off);
        imgMine.setImageResource(R.drawable.wode_wd_off);
        imgPriceRatio.setImageResource(R.drawable.wode_bbj);
        if(!NetworkHelper.isNetwork()) Tst.showToast("网络不给力，请检查网络设置");
    }
    //接收登录成功后的通知
    @Subscribe(tags = {@Tag(BusAction.ACTION_LOGIN)})
    public void login(User user){
        notifyMsgTip("notify");
        if(isClickMsgMenu) setMsgCenterPage();
    }
    //接收登出成功后的通知
    @Subscribe(tags = {@Tag(BusAction.ACTION_LOGOUT)})
    public void logout(String notify){
        notifyMsgTip("notify");
    }
    //接收消息该改变的通知(聊天会话变更&查阅凯恩买手和凯恩客服后的通知)
    @Subscribe(tags = {@Tag(BusAction.REFRESH_MSG_TIP)})
    public void notifyMsgTip(String notify){
        if(!SPUtils.isLogin()){ //没有登录
            if(mTabHost.getCurrentTabTag().equals(MSGCENTER)){
                imgWelfareService.setImageResource(R.drawable.wode_fls_on);
            }else {
                imgWelfareService.setImageResource(R.drawable.wode_fls_off);
            }
        }
        IMHelper.getInstance().unreadMsgCount().subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
//                ShortcutBadger.applyCount(KnmsApp.getInstance(), integer);
                BadgeNumberManager.from(KnmsApp.getInstance()).setBadgeNumber(integer);
                if (integer >= 0) tv_new_count.setText(integer > 99 ? "99+" : integer + "");
                if (mTabHost.getCurrentTabTag().equals(MSGCENTER)) {
                    imgWelfareService.setImageResource(R.drawable.wode_fls_on);
                } else {
                    imgWelfareService.setImageResource(R.drawable.wode_fls_off);
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {}
        });
    }
    private void loadUnreadCount(){
        IMHelper.getInstance().unreadMsgCount().subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                if(integer >= 0) tv_new_count.setText(integer > 99 ? "99+" : integer + "");
                if(mTabHost.getCurrentTabTag().equals(MSGCENTER)){
                    imgWelfareService.setImageResource(R.drawable.wode_fls_on);
                }else {
                    imgWelfareService.setImageResource(R.drawable.wode_fls_off);
                }
                RxBus.get().post(BusAction.REFRESH_MSG_COUNT,"refreshMsg");
            }
        });
    }
    private void registerObservers(boolean register) {
        NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(onlineStatus, register);//在线状态变更
        NIMClient.getService(MsgServiceObserve.class).observeRecentContact(messageObserver, register);//监听最近会话变更
        NIMClient.getService(AuthServiceObserver.class).observeOtherClients(onlineclients, true);//多端登录状态观察者
    }
    Observer<StatusCode> onlineStatus = new Observer<StatusCode>(){
        @Override
        public void onEvent(StatusCode statusCode) {
            if (statusCode.wontAutoLogin()) {
                // 被踢出、账号被禁用、密码错误等情况，自动登录失败，需要返回到登录界面进行重新登录操作
                if (statusCode == StatusCode.KICKOUT) {
                    Tst.showToast("你的账号在其他设备有登录");
                }
                CommonUtils.logout();
//                KnmsApp.getInstance().keepIndex();//关闭出首页界面以为所有界面
                startActivity(new Intent(MainActivity.this,FasterLoginActivity.class));
            }
        }
    };
    //最近会话变更
    Observer<List<RecentContact>> messageObserver = new Observer<List<RecentContact>>() {
        @Override
        public void onEvent(List<RecentContact> messages) {
            notifyMsgTip("notify");
        }
    };
    Observer<List<OnlineClient>> onlineclients = new Observer<List<OnlineClient>>() {
        @Override
        public void onEvent(List<OnlineClient> onlineClients) {
            if(onlineClients != null && onlineClients.size() > 0){
                for (OnlineClient onlineClient : onlineClients) {
//                    if(onlineClient.getLoginTime() < IMHelper.getInstance().getLoginTime())
//                        NIMClient.getService(AuthService.class).kickOtherClient(onlineClient);//主动踢掉当前同时在线的其他端
                }
            }
        }
    };

    Subscription subscription;
    private void startIntervalTask(int initialDelay){
        subscription = rx.Observable.interval(initialDelay, 15, TimeUnit.SECONDS)//延时1秒 ，每间隔5秒
                .map(new Func1<Long, User>() {
                    @Override
                    public User call(Long aLong) {
                        return SPUtils.getUser();
                    }
                })
                .filter(new Func1<User, Boolean>() {
                    @Override
                    public Boolean call(User user) {
                        if(user == null) return false;
                        if(TextUtils.isEmpty(user.sid)) return false;
                        return SPUtils.isLogin() && ForegroundCallbacks.get().isForeground();
                    }
                }).flatMap(new Func1<User, rx.Observable<ResponseBody<TipNum>>>() {
                    @Override
                    public rx.Observable<ResponseBody<TipNum>> call(User user) {
                        return RxRequestApi.getInstance().getOtherApiService().getRedspot(user.sid);
                    }
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext(new Func1<Throwable, rx.Observable<? extends ResponseBody<TipNum>>>() {
                    @Override
                    public rx.Observable<? extends ResponseBody<TipNum>> call(Throwable throwable) {
                        return rx.Observable.just(new ResponseBody<TipNum>());
                    }
                })
                .subscribe(new Action1<ResponseBody<TipNum>>() {
                    @Override
                    public void call(ResponseBody<TipNum> body) {
                        if (body.isSuccess()){
                            TipNum oldTipNum = Svn.getFromAccount("tipNum");
                            if(body.data != null){
                                if(body.data.idel != 0){
                                    if(oldTipNum == null) oldTipNum = new TipNum();
                                    oldTipNum.idel = body.data.idel;
                                }
                                if(body.data.coupon != 0){
                                    if(oldTipNum == null) oldTipNum = new TipNum();
                                    oldTipNum.coupon = body.data.coupon;
                                }
                                if(body.data.repair != 0){
                                    if(oldTipNum == null) oldTipNum = new TipNum();
                                    oldTipNum.repair = body.data.repair;
                                }
                                if(body.data.order != 0){
                                    if(oldTipNum == null) oldTipNum = new TipNum();
                                    oldTipNum.order = body.data.order;
                                }
                                if(body.data.parity != 0){
                                    if(oldTipNum == null) oldTipNum = new TipNum();
                                    oldTipNum.parity = body.data.parity;
                                }
                            }
                            KnmsApp.getInstance().getUnreadObservable().sendData(oldTipNum);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {stopIntervalTask();startIntervalTask(15);
                        L.i("error",throwable.toString());}
                });
    }
    private void stopIntervalTask(){
        if(subscription != null) subscription.unsubscribe();
    }

    @Override
    protected void onDestroy() {
        RxBus.get().unregister(this);
        registerObservers(false);
        KnmsApp.getInstance().getUnreadObservable().deleteObserver(unreadObserver);
        stopIntervalTask();
        KnmsApp.getInstance().finishActivity(this);
        super.onDestroy();
    }
}
