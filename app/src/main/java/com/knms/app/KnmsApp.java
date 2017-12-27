package com.knms.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.multidex.MultiDexApplication;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.knms.activity.details.DecorationStyleDetailsActivity;
import com.knms.activity.im.MessageCenterActivity;
import com.knms.activity.main.ClassificationActivityF;
import com.knms.activity.main.HomePageActivityF;
import com.knms.activity.main.MainActivity;
import com.knms.activity.main.MineActivity;
import com.knms.android.R;
import com.knms.core.badgenumber.BadgeNumberManager;
import com.knms.core.badgenumber.BadgeNumberManagerXiaoMi;
import com.knms.core.badgenumber.MobileBrand;
import com.knms.core.cockroach.Cockroach;
import com.knms.core.im.IMHelper;
import com.knms.core.im.cache.NimUserInfoCache;
import com.knms.core.im.config.UserPreferences;
import com.knms.core.im.msg.ProductAttachParser;
import com.knms.core.im.storage.StorageUtil;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.toast.SmartToast;
import com.knms.oncall.LoadListener;
import com.knms.oncall.UnreadObservable;
import com.knms.util.BadgeUtil;
import com.knms.util.CommonUtils;
import com.knms.util.SDCardUtils;
import com.knms.util.SPUtils;
import com.knms.util.Tst;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.SystemMessageObserver;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.orhanobut.hawk.Hawk;
import com.squareup.leakcanary.RefWatcher;
import com.tencent.bugly.crashreport.CrashReport;
import com.umeng.analytics.MobclickAgent;
import java.util.List;
import java.util.Stack;
import cn.jpush.android.api.JPushInterface;
import cn.sharesdk.framework.ShareSDK;

/**
 * Created by tdx on 2016/8/23.
 */
public class KnmsApp extends MultiDexApplication{
    private static KnmsApp mApplication;
    private Stack<Activity> activityStack = new Stack<Activity>();
    private View mLoadView;
    private UnreadObservable unreadObservable;
    public RefWatcher refWatcher;
    public static KnmsApp getInstance() {
        return mApplication;
    }
    public UnreadObservable getUnreadObservable(){
        if(unreadObservable == null) unreadObservable = new UnreadObservable();
        return unreadObservable;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
        if (CommonUtils.inMainProcess()) {
            ForegroundCallbacks.init(this);
            Hawk.init(this).build();
        }
        SmartToast.plainToast(KnmsApp.getInstance());
//        refWatcher = LeakCanary.install(this);//内存溢出检查工具初始化
        ShareSDK.initSDK(this);//配置分享sdk
        initCrash();
        initImageLoader();
        initIMChat();
        initJPush();
        initMobclick();
    }

    private void initCrash() {
//      installCrashException();//永不crash的Android
        // 获取当前包名
        String packageName = getPackageName();
        // 获取当前进程名
        String processName = CommonUtils.getProcessName(android.os.Process.myPid());
        // 设置是否为上报进程
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(this);
        strategy.setUploadProcess(processName == null || processName.equals(packageName));
        CrashReport.initCrashReport(this, "6af33135dc", false);//配置崩溃日志统计分享,建议在测试阶段建议设置成true，发布时设置为false。
    }

    private void initMobclick() {
        MobclickAgent.openActivityDurationTrack(false);
        MobclickAgent.enableEncrypt(true);//日志加密
    }

    private void initJPush() {
        JPushInterface.setDebugMode(true);// 设置开启日志,发布时请关闭日志
        JPushInterface.init(this);//配置极光推送
        jpush(SPUtils.getNotificationStatus());
    }

    private void jpush(boolean enable) {
        if(enable){//开启推送
            JPushInterface.resumePush(KnmsApp.getInstance());
        }else { //关闭推送
            JPushInterface.stopPush(KnmsApp.getInstance());
        }
    }

    /**
     * 初始化聊天库
     */
    private void initIMChat() {
        NIMClient.init(this, IMHelper.getInstance().getLoginInfo(), IMHelper.getInstance().getOptions());
        if (CommonUtils.inMainProcess()) {
            NIMClient.getService(MsgService.class).registerCustomAttachmentParser(new ProductAttachParser());//注册自定义消息解析器

            IMHelper.getInstance().registerLoginSyncDataStatus(true);
            NimUserInfoCache.getInstance().registerObservers(true);//监听用户资料变更
            if(!TextUtils.isEmpty(IMHelper.getInstance().getAccount())){
                NimUserInfoCache.getInstance().clear();
                NimUserInfoCache.getInstance().buildCache();
            }
            //兼容手机应用icon右上角标数目的显示
            NIMClient.getService(MsgServiceObserve.class).observeReceiveMessage(new Observer<List<IMMessage>>() {
                @Override
                public void onEvent(List<IMMessage> imMessages) {
                    StatusCode statusCode = NIMClient.getStatus();
                    Integer count;
                    if(StatusCode.LOGINED != statusCode){
                        count = 0;
                    }else{
                        count = NIMClient.getService(MsgService.class).getTotalUnreadCount();
                    }
                    if(Build.MANUFACTURER.equalsIgnoreCase(MobileBrand.SAMSUNG)){
                        BadgeNumberManager.from(KnmsApp.getInstance()).setBadgeNumber(count ++);
                        BadgeNumberManager.from(KnmsApp.getInstance()).setBadgeNumber(count);
                    }else{
                        BadgeNumberManager.from(KnmsApp.getInstance()).setBadgeNumber(count);
                    }

                }
            },true);
            // 初始化消息提醒
            NIMClient.toggleNotification(true);
            // 更新消息提醒配置 StatusBarNotificationConfig
            NIMClient.updateStatusBarNotificationConfig(UserPreferences.getStatusConfig());
            //存储
            StorageUtil.init(this, SDCardUtils.getCacheDir(KnmsApp.getInstance()) + "/nim");
        }
    }

    /**
     * 初始化图片加载库
     */
    private void initImageLoader() {
//        com.nostra13.universalimageloader.utils.L.writeLogs(false);//关闭打印框架日志
//        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
//                .tasksProcessingOrder(QueueProcessingType.FIFO)
//                .memoryCacheExtraOptions(480, 800)// 即保存的每个缓存文件的最大长宽
//                .threadPoolSize(3)
//                .threadPriority(Thread.NORM_PRIORITY - 2)//降低线程的优先级保证主UI线程不受太大影响
//                .denyCacheImageMultipleSizesInMemory()
//                .memoryCache(new LruMemoryCache(10 * 1024 * 1024))//建议内存设在5-10M,可以有比较好的表现
//                .memoryCacheSize(10 * 1024 * 1024)
//                .diskCacheSize(150 * 1024 * 1024)
//                .diskCacheFileCount(150)
//                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
//                .tasksProcessingOrder(QueueProcessingType.LIFO)
//                .diskCacheFileCount(150)
//                .diskCache(new UnlimitedDiskCache(SDCardUtils.getCacheImgDirFile(this)))// 自定义缓存路径
//                .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
//                .imageDownloader(new BaseImageDownloader(this, 5 * 1000, 30 * 1000))
//                //.writeDebugLogs()
//                .build();
//        ImageLoader.getInstance().init(config);

//        long maxSize = Runtime.getRuntime().maxMemory() / 8;//设置图片缓存大小为运行时缓存的八分之一
//        OkHttpClient client = new OkHttpClient.Builder()
//                .cache(new Cache(SDCardUtils.getCacheImgDirFile(KnmsApp.getInstance()), maxSize))
//                .build();
//        Picasso picasso = new Picasso.Builder(KnmsApp.getInstance())
//                .memoryCache(new LruCache(10 << 20))//设置内存缓存大小10M
//                .downloader(new OkHttp3Downloader(client))
//                .indicatorsEnabled(false) //设置左上角标记，主要用于测试
//                .build();
//        Picasso.setSingletonInstance(picasso);
//        picasso.setLoggingEnabled(true);  //打开日志，即log中会打印出目前下载的进度、情况
    }
    private void installCrashException(){
        Cockroach.install(new Cockroach.ExceptionHandler() {
            // handlerException内部建议手动try{  你的异常处理逻辑  }catch(Throwable e){ } ，以防handlerException内部再次抛出异常，导致循环调用handlerException
            @Override
            public void handlerException(final Thread thread, final Throwable throwable) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.d("Cockroach", thread + "\n" + throwable.toString());
                            throwable.printStackTrace();
                            Toast.makeText(KnmsApp.this, "Exception Happend\n" + thread + "\n" + throwable.toString(), Toast.LENGTH_SHORT).show();
                        } catch (Throwable e) {
                        }
                    }
                });
            }
        });
    }
    /**
     * 添加Activity到堆栈
     */
    public void addActivity(Activity activity) {
        if (activityStack == null) {
            activityStack = new Stack<Activity>();
        }
        if(!(activity instanceof DecorationStyleDetailsActivity)){
            activityStack.add(activity);
        }
    }

    /**
     * 获取当前Activity（堆栈中最后一个压入的）
     */
    public Activity currentActivity() {
        Activity activity = activityStack.lastElement();
        return activity;
    }

    /**
     * 结束当前Activity（堆栈中最后一个压入的）
     */
    public void finishActivity() {
        Activity activity = activityStack.lastElement();
        finishActivity(activity);
    }

    /**
     * 结束指定的Activity
     */
    public void finishActivity(Activity activity) {
        if (activity != null) {
            activityStack.remove(activity);
            activity.finish();
            activity = null;
        }
    }
    public Activity findActivity(Class<?> cls){
        for (Activity activity : activityStack) {
            if (activity.getClass().equals(cls)) {
                return activity;
            }
        }
        return null;
    }
    public boolean isActLive(Class<?> cls){
        Activity act = findActivity(cls);
        if(act == null) return false;
        else return !act.isFinishing();
    }
    public boolean isCurrentActLive(Class<?> cls){
        Activity act = activityStack.lastElement();
        if(act == null) return false;
        if (act.getClass().equals(cls)) {
            return true;
        }else{
            return false;
        }
    }
    /**
     * 结束指定类名的Activity
     */
    public void finishActivity(Class<?> cls) {
        Activity temp = null;
        for (Activity activity : activityStack) {
            if (activity.getClass().equals(cls)) {
                temp = activity;
                break;
            }
        }
        if(temp != null){
            activityStack.remove(temp);
            temp.finish();
        }
    }

    /**
     * 结束所有Activity
     */
    public void finishAllActivity() {
        for (int i = 0; i < activityStack.size(); i++) {
            if (null != activityStack.get(i)) {
                activityStack.get(i).finish();
            }
        }
        activityStack.clear();
    }

    /**
     * 退出应用程序
     */
    public void AppExit(Context context) {
        try {
            finishAllActivity();
            ActivityManager activityMgr = (ActivityManager) context
                    .getSystemService(Context.ACTIVITY_SERVICE);
            activityMgr.killBackgroundProcesses(context.getPackageName());
            System.exit(0);
        } catch (Exception e) {
        }
    }
    public void keepIndex(){
        try{
            for (Activity activity: activityStack){
                if(null != activity && !MainActivity.class.getSimpleName().equals(activity.getClass().getSimpleName())
                        && !HomePageActivityF.class.getSimpleName().equals(activity.getClass().getSimpleName())
                        && !ClassificationActivityF.class.getSimpleName().equals(activity.getClass().getSimpleName())
                        && !MessageCenterActivity.class.getSimpleName().equals(activity.getClass().getSimpleName())
                        && !MineActivity.class.getSimpleName().equals(activity.getClass().getSimpleName())){
                    activity.finish();
                }
            }
        }catch (Exception e){

        }
    }
    public void showLoadViewIng(RelativeLayout layoutStatus) {
        if (layoutStatus == null) {
            return;
        }
        layoutStatus.removeAllViews();
        mLoadView = (ViewGroup) LayoutInflater.from(this).inflate(
                R.layout.layout_view_loading, null);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        lp.addRule(RelativeLayout.CENTER_VERTICAL);
        layoutStatus.addView(mLoadView, lp);
        layoutStatus.setVisibility(View.VISIBLE);
    }

    public void showLoadViewFaild(RelativeLayout layoutStatus, final LoadListener listener) {
        if (layoutStatus == null) {
            return;
        }
        layoutStatus.removeAllViews();
        mLoadView = (ViewGroup) LayoutInflater.from(this).inflate(
                R.layout.layout_view_load_fail, null);

        mLoadView.findViewById(R.id.textBtn_overLoaded_new).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null)
                            listener.onclick();
                    }
                });
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        lp.addRule(RelativeLayout.CENTER_VERTICAL);
        layoutStatus.addView(mLoadView, lp);
        layoutStatus.setVisibility(View.VISIBLE);
    }

    public void showLoadSmailViewFaild(RelativeLayout layoutStatus, final LoadListener listener) {
        if (layoutStatus == null) {
            return;
        }
        layoutStatus.removeAllViews();
        mLoadView = (ViewGroup) LayoutInflater.from(this).inflate(
                R.layout.layout_view_load_fail, null);
        mLoadView.findViewById(R.id.iv_icon).setVisibility(View.GONE);
        mLoadView.findViewById(R.id.textBtn_overLoaded_new).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null)
                            listener.onclick();
                    }
                });
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        lp.addRule(RelativeLayout.CENTER_VERTICAL);
        layoutStatus.addView(mLoadView, lp);
        layoutStatus.setVisibility(View.VISIBLE);
    }

    public void showLoadEmpty(RelativeLayout layoutStatus, final LoadListener listener) {
        layoutStatus.removeAllViews();
        mLoadView = (ViewGroup) LayoutInflater.from(this).inflate(
                R.layout.layout_view_load_fail, null);

        mLoadView.setVisibility(View.VISIBLE);
        ((TextView) mLoadView.findViewById(R.id.text_fail_content_new))
                .setText("暂无数据");
        ((TextView) mLoadView.findViewById(R.id.text_fail_content_desc))
                .setText("请点击刷新吧!");
        mLoadView.findViewById(R.id.textBtn_overLoaded_new)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null)
                            listener.onclick();
                    }
                });
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        lp.addRule(RelativeLayout.CENTER_VERTICAL);
        layoutStatus.addView(mLoadView, lp);
        layoutStatus.setVisibility(View.VISIBLE);
    }

    public void showDataEmpty(RelativeLayout layoutStatus, String text, int imgId) {
        layoutStatus.removeAllViews();
        mLoadView = LayoutInflater.from(this).inflate(R.layout.layout_view_no_data, null);
        mLoadView.setVisibility(View.VISIBLE);
        ((TextView) mLoadView.findViewById(R.id.tv_no_data)).setText(text);
        ((ImageView) mLoadView.findViewById(R.id.img_no_data)).setImageResource(imgId);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        layoutStatus.addView(mLoadView, lp);
        layoutStatus.setVisibility(View.VISIBLE);
    }


    public void showDataEmpty(RelativeLayout layoutStatus, String text, int imgId, String textBtn, final LoadListener listener) {
        layoutStatus.removeAllViews();
        mLoadView = LayoutInflater.from(this).inflate(R.layout.layout_view_no_data, null);
        mLoadView.setVisibility(View.VISIBLE);
        Button btn_bottom = (Button) mLoadView.findViewById(R.id.btn_bottom);
        btn_bottom.setVisibility(View.VISIBLE);
        btn_bottom.setText(textBtn);
        btn_bottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) listener.onclick();
            }
        });
        ((TextView) mLoadView.findViewById(R.id.tv_no_data)).setText(text);
        ((ImageView) mLoadView.findViewById(R.id.img_no_data)).setImageResource(imgId);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        layoutStatus.addView(mLoadView, lp);
        layoutStatus.setVisibility(View.VISIBLE);
        layoutStatus.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
    }

    public void hideLoadView(RelativeLayout layoutStatus) {
        if (layoutStatus == null) return;
        if (layoutStatus.getVisibility() == View.GONE) return;
        layoutStatus.removeAllViews();
        layoutStatus.setVisibility(View.GONE);
    }
    public void onDestroy(){
        if(mLoadView != null) mLoadView = null;
    }
}
