package com.knms.core.im;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.knms.activity.main.MainActivity;
import com.knms.android.R;
import com.knms.app.KnmsApp;
import com.knms.bean.ResponseBody;
import com.knms.bean.im.KnmsMsg;
import com.knms.core.im.cache.FriendDataCache;
import com.knms.core.im.cache.NimUserInfoCache;
import com.knms.core.im.config.UserPreferences;
import com.knms.net.RxRequestApi;
import com.knms.util.ImageLoadHelper;
import com.knms.util.L;
import com.knms.util.SDCardUtils;
import com.knms.util.SPUtils;
import com.knms.util.Tst;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.SDKOptions;
import com.netease.nimlib.sdk.StatusBarNotificationConfig;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.auth.constant.LoginSyncStatus;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.attachment.FileAttachment;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.nimlib.sdk.uinfo.UserInfoProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.jpush.android.api.JPushInterface;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

import static com.knms.util.SPUtils.KeyConstant.imAccount;
import static com.netease.nimlib.sdk.NIMClient.getService;
/**
 * Created by tdx on 2016/9/27.
 */
public class IMHelper {
    /**
     * 单例
     */
    public static IMHelper getInstance() {
        return InstanceHolder.instance;
    }
    static class InstanceHolder {
        final static IMHelper instance = new IMHelper();
    }
    public LoginInfo getLoginInfo() {
        LoginInfo loginInfo = SPUtils.getSerializable(imAccount,LoginInfo.class);
        if (loginInfo != null) {
            return loginInfo;
        }
        return null;
    }
    public String getAccount(){
        LoginInfo info = getLoginInfo();
        if(info != null) return info.getAccount();
        else  return "";
    }
    public String getToken(){
        LoginInfo info = getLoginInfo();
        if(info != null) return info.getToken();
        else  return "";
    }
    public SDKOptions getOptions() {
        SDKOptions options = new SDKOptions();
        options.appKey = "1b461a0bdb48a66d14f4b9ede1677d7d";
        // 如果将新消息通知提醒托管给 SDK 完成，需要添加以下配置。否则无需设置。
        StatusBarNotificationConfig config = UserPreferences.getStatusConfig();
        if (config == null) {
            config = new StatusBarNotificationConfig();
        }
        // 点击通知需要跳转到的界面
        config.notificationEntrance = MainActivity.class;
        config.notificationSmallIconId = R.drawable.icon_knms;

        // 通知铃声的uri字符串,暂时不要铃声
        config.notificationSound = "android.resource://com.knms.android/raw/msg";
        // 呼吸灯配置
        config.ledARGB = Color.GREEN;
        config.ledOnMs = 1000;
        config.ledOffMs = 1500;
        config.ring = false;
        options.statusBarNotificationConfig = config;
//        DemoCache.setNotificationConfig(config);
        UserPreferences.setStatusConfig(config);

        // 配置保存图片，文件，log等数据的目录
        String sdkPath = SDCardUtils.getCacheDir(KnmsApp.getInstance()) + "/nim";
        options.sdkStorageRootPath = sdkPath;

        // 配置数据库加密秘钥
        options.databaseEncryptKey = "NETEASE";

        // 配置是否需要预下载附件缩略图
        options.preloadAttach = true;

        // 配置附件缩略图的尺寸大小，
        // 该值一般应根据屏幕尺寸来确定， 默认值为 Screen.width / 2
//        options.thumbnailSize = MsgViewHolderThumbBase.getImageMaxEdge();

        // 用户信息提供者
        // 用户资料提供者, 目前主要用于提供用户资料，用于新消息通知栏中显示消息来源的头像和昵称
        options.userInfoProvider = infoProvider;

        // 定制通知栏提醒文案（可选，如果不定制将采用SDK默认文案）
//        options.messageNotifierCustomization = messageNotifierCustomization;
        return options;
    }
    public void loginIM(){
        StatusCode statusCode = NIMClient.getStatus();
        if(StatusCode.LOGINED != statusCode){
            LoginInfo loginInfo = SPUtils.getSerializable(SPUtils.KeyConstant.imAccount,LoginInfo.class);
            AbortableFuture<LoginInfo> loginRequest = NIMClient.getService(AuthService.class).login(loginInfo);
            loginRequest.setCallback(new RequestCallbackWrapper() {
                @Override
                public void onResult(int code, Object result, Throwable exception) {
                    L.i_im("im login, code=" + code);
                    if (code == ResponseCode.RES_SUCCESS) {
                        setLoginTime();
                    }else{
                    }
                }
            });
        }
    }
    public void setLoginTime(){
        SPUtils.saveToAccount("imLoginTime",System.currentTimeMillis());
    }
    public long getLoginTime(){
        return (long) SPUtils.getFromAccount("imLoginTime",120L);
    }
    public void logout() {
        SPUtils.clearSerializable(imAccount, LoginInfo.class);
        getService(AuthService.class).logout();
        FriendDataCache.getInstance().clear();
        NimUserInfoCache.getInstance().clear();
    }

    public UserInfoProvider infoProvider =  new UserInfoProvider() {
        @Override
        public UserInfo getUserInfo(String account) {
            UserInfo user = NimUserInfoCache.getInstance().getUserInfoFromLocal(account);
            if (user == null) {
                NimUserInfoCache.getInstance().getUserInfoFromRemote(account,null);
            }
            return user;
        }
        /**
         * 如果根据用户账号找不到UserInfo的avatar时，显示的默认头像资源ID
         * @return 默认头像的资源ID
         */
        @Override
        public int getDefaultIconResId() {
            return R.drawable.icon_avatar;
        }

        @Override
        public Bitmap getTeamIcon(String teamId) {
            /**
             * 注意：这里最好从缓存里拿，如果读取本地头像可能导致UI进程阻塞，导致通知栏提醒延时弹出。
             */
            // 从内存缓存中查找群头像
            // 默认图
            Drawable drawable = KnmsApp.getInstance().getResources().getDrawable(R.drawable.icon_avatar);
            if (drawable instanceof BitmapDrawable) {
                return ((BitmapDrawable) drawable).getBitmap();
            }
            return null;
        }
        /**
         * 为通知栏提供用户头像（一般从本地缓存中取，若未下载或本地不存在，返回null，通知栏将显示默认头像）
         * @return 头像位图
         */
        @Override
        public Bitmap getAvatarForMessageNotifier(String account) {
            /**
             * 注意：这里最好从缓存里拿，如果读取本地头像可能导致UI进程阻塞，导致通知栏提醒延时弹出。
             */
            UserInfo user = getUserInfo(account);
            return (user != null) ? ImageLoadHelper.getInstance().getBitmapFromCache(user.getAvatar()) : null;
        }
        @Override
        public String getDisplayNameForMessageNotifier(String account, String sessionId, SessionTypeEnum
        sessionType) {
            String nick = " ";
            if (sessionType == SessionTypeEnum.P2P) {
                if(NimUserInfoCache.getInstance().getUserInfoFromLocal(account) != null)
                nick = NimUserInfoCache.getInstance().getUserInfoFromLocal(account).getName();
            } else if (sessionType == SessionTypeEnum.Team) {
                nick = "Team";
            }
            // 返回null，交给sdk处理。如果对方有设置nick，sdk会显示nick
            if (TextUtils.isEmpty(nick)) {
                return " ";
            }
            return nick;
        }
    };

    Observer<LoginSyncStatus> loginSyncStatusObserver = new Observer<LoginSyncStatus>() {
        @Override
        public void onEvent(LoginSyncStatus status) {
            //同步开始时，SDK 数据库中的数据可能还是旧数据（如果是首次登录，那么 SDK 数据库中还没有数据，重新登录时 SDK 数据库中还是上一次退出时保存的数据）
            if (status == LoginSyncStatus.BEGIN_SYNC) {
                L.i_im("login sync data begin");
            } else if (status == LoginSyncStatus.SYNC_COMPLETED) {//同步完成时， SDK 数据库已完成更新。
                L.i_im("login sync data completed");
            }
        }
    };
    /**
     * 登录成功后，SDK 会立即同步数据（用户资料、用户关系、群资料、离线消息、漫游消息等），同步开始和同步完成都会发出通知。
     * 在App启动时向SDK注册登录后同步数据过程状态的通知
     * 调用时机：主进程Application onCreate中
     */
    public void registerLoginSyncDataStatus(boolean register) {
        L.i_im("observe login sync data completed event on Application create");
        getService(AuthServiceObserver.class).observeLoginSyncDataStatus(loginSyncStatusObserver, register);
    }

    // 发送文本消息
    public IMMessage sendTextMsg(String account,String txtMsg) {
        IMMessage msg = MessageBuilder.createTextMessage(account, SessionTypeEnum.P2P, txtMsg);
        Map<String,Object> map = new HashMap<>();
        map.put("fromId",getAccount());
        map.put("sound","knmsPushVoice.caf");
        msg.setPushPayload(map);
        NIMClient.getService(MsgService.class).sendMessage(msg,true);
//        NIMClient.getService(MsgService.class).saveMessageToLocal(msg, true);
        return msg;
    }
    // 发送图片消息
    public IMMessage sendImageMsg(String account, String path) {
        File file = new File(path);
        if (!file.exists()) {
            Tst.showToast("图片不存在!");
            return null;
        }
        // 创建图片消息
        IMMessage msg = MessageBuilder.createImageMessage(account, SessionTypeEnum.P2P, file, null);
        Map<String,Object> map = new HashMap<>();
        map.put("fromId",getAccount());
        map.put("sound","knmsPushVoice.caf");
        msg.setPushPayload(map);
        NIMClient.getService(MsgService.class).sendMessage(msg,true);
        return msg;
//        NIMClient.getService(MsgService.class).saveMessageToLocal(msg, true);
    }
    // 发送消息
    public IMMessage sendMsg(IMMessage msg) {
        Map<String,Object> map = new HashMap<>();
        map.put("fromId",getAccount());
        map.put("sound","knmsPushVoice.caf");
        msg.setPushPayload(map);
        NIMClient.getService(MsgService.class).sendMessage(msg,true);
        return msg;
    }
    /**
     * 设置最近联系人的消息为已读
     * 聊天对象帐号，或者以下两个值：
     *  {@link # MSG_CHATTING_ACCOUNT_ALL} 目前没有与任何人对话，但能看到消息提醒（比如在消息列表界面），不需要在状态栏做消息通知
     *  {@link # MSG_CHATTING_ACCOUNT_NONE} 目前没有与任何人对话，需要状态栏消息通知
     *  enable true显示到通知栏,fasle不显示到通知栏
     */
    public void enableMsgNotification(boolean enable) {
        if (enable) {
            NIMClient.getService(MsgService.class).setChattingAccount(MsgService.MSG_CHATTING_ACCOUNT_NONE, SessionTypeEnum.None);
        } else {
            NIMClient.getService(MsgService.class).setChattingAccount(MsgService.MSG_CHATTING_ACCOUNT_ALL, SessionTypeEnum.None);
        }
    }
    public void enableMsgNotification(String account,boolean enable) {
        if (enable) {
            NIMClient.getService(MsgService.class).setChattingAccount(MsgService.MSG_CHATTING_ACCOUNT_NONE, SessionTypeEnum.None);
        } else {
            NIMClient.getService(MsgService.class).setChattingAccount(account, SessionTypeEnum.P2P);
        }
    }
    public void enableNotification(boolean enable){
        if(enable){//开启推送
           JPushInterface.resumePush(KnmsApp.getInstance());
        }else { //关闭推送
           JPushInterface.stopPush(KnmsApp.getInstance());
        }
        NIMClient.toggleNotification(enable);
    }
    /**
     * 获取IM聊天未读消息条数
     * @return
     */
    public Observable<Integer> getIMTotalUnreadCount (){
        StatusCode statusCode = NIMClient.getStatus();
        if(StatusCode.LOGINED != statusCode){
            return Observable.just(0);
        }
        int count = NIMClient.getService(MsgService.class).getTotalUnreadCount();
        return Observable.just(count);
    }

    /**
     * 返回是否有未读消息（包含IM聊天和凯恩服务器聊天）
     * true有未读消息 false没有未读消息
     * @return
     */
    public Observable<Boolean> isUnreadMsg(){
        if(!SPUtils.isLogin()) return Observable.just(false)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
        StatusCode statusCode = NIMClient.getStatus();
        if(StatusCode.LOGINED != statusCode){
            return Observable.just(false);
        }
        int count = 0;
        try {
            count = NIMClient.getService(MsgService.class).getTotalUnreadCount();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(count != 0){ //有未读消息
            return Observable.just(true).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        }
        return RxRequestApi.getInstance().getApiService().getMsgCenter().flatMap(new Func1<ResponseBody<List<KnmsMsg>>, Observable<Boolean>>() {
            @Override
            public Observable<Boolean> call(ResponseBody<List<KnmsMsg>> body) {
                int readNumber = 0;
                if(body != null && body.data != null)
                    for (KnmsMsg item: body.data) {
                        readNumber += item.notReadNumber;
                    }
                return Observable.just(readNumber != 0);
            }
        }).onErrorResumeNext(new Func1<Throwable, Observable<? extends Boolean>>() {
            @Override
            public Observable<? extends Boolean> call(Throwable throwable) {
                return Observable.just(false);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 返回im聊天和系统未读消息总条数
     * @return
     */
    public Observable<Integer> unreadMsgCount(){
        if(!SPUtils.isLogin()) return Observable.just(0)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
        return Observable.zip(RxRequestApi.getInstance().getApiService().getMsgCenter(), getIMTotalUnreadCount(),
                new Func2<ResponseBody<List<KnmsMsg>>, Integer, Integer>() {
                    @Override
                    public Integer call(ResponseBody<List<KnmsMsg>> body, Integer integer) {
                        if (body.isSuccess()) {
                            if (body.data != null) {
                                for (KnmsMsg item : body.data) {
                                    integer += item.notReadNumber;
                                }
                            }
                        }
                        return integer;
                    }
                }).onErrorResumeNext(new Func1<Throwable, Observable<? extends Integer>>() {
            @Override
            public Observable<? extends Integer> call(Throwable throwable) {
                return Observable.just(0);
            }
        }).doOnError(new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Tst.showToast(throwable.toString());
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }
    public Observable<Map<String,Object>> queryMessageListByImage(final IMMessage message){
        final IMMessage anchor = MessageBuilder.createEmptyMessage(message.getSessionId(),message.getSessionType(),0);
        return Observable.unsafeCreate(new Observable.OnSubscribe<List<IMMessage>>() {
            @Override
            public void call(final Subscriber<? super List<IMMessage>> subscriber) {
                NIMClient.getService(MsgService.class).queryMessageListByType(MsgTypeEnum.image, anchor, Integer.MAX_VALUE).setCallback(new RequestCallback<List<IMMessage>>() {
                    @Override
                    public void onSuccess(List<IMMessage> param) {
                        subscriber.onNext(param);
                        subscriber.onCompleted();
                    }
                    @Override
                    public void onFailed(int code) {
                        subscriber.onError(new Throwable("code:" + code));
                        subscriber.onCompleted();
                    }
                    @Override
                    public void onException(Throwable exception) {
                        subscriber.onError(exception);
                        subscriber.onCompleted();
                    }
                });
            }
        }).map(new Func1<List<IMMessage>, Map<String,Object>>() {
            @Override
            public Map<String,Object> call(List<IMMessage> imMessages) {
                Map<String,Object> value = new HashMap<String, Object>();
                List<String> imgs = new ArrayList<String>();
                int temp = 0;
                if(imMessages != null && imMessages.size() > 0){
                    Collections.reverse(imMessages);
                    int positon = 0;
                    for (IMMessage msg: imMessages) {
                        if(compareObjects(msg,message)){
                            temp = positon;
                        }
                        FileAttachment msgAttachment = (FileAttachment) msg.getAttachment();
                        String path = msgAttachment.getPath();
                        if(TextUtils.isEmpty(path)){
                            path = msgAttachment.getUrl();
                        }
                        imgs.add(path);
                        positon ++;
                    }
                }
                value.put("data",imgs);
                value.put("position",temp);
                return value;
            }
        });
    }
    protected boolean compareObjects(IMMessage t1, IMMessage t2) {
        return (t1.getUuid().equals(t2.getUuid()));
    }
    /**********消息置顶标记的增删**********/
    /**
     * &与运算符，两个操作数中位都为1，结果才为1，否则结果为0
     * |或运算符，两个位只要有一个为1，那么结果就是1，否则就为0。
     * ~非运算符，如果位为0，结果是1，如果位为1，结果是0。
     * ^异或运算符，两个操作数的位中，相同则结果为0，不同则结果为1。
     */
    public void addTag(RecentContact recent, long tag) {
        tag = recent.getTag() | tag;
        recent.setTag(tag);
    }
    public void removeTag(RecentContact recent, long tag) {
        tag = recent.getTag() & (~tag);
        recent.setTag(tag);
    }
    public boolean isTagSet(RecentContact recent, long tag) {
        return (recent.getTag() & tag) == tag;
    }
}
