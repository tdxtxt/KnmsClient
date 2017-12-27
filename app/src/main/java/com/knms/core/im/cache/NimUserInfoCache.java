package com.knms.core.im.cache;

import android.text.TextUtils;

import com.knms.bean.other.City;
import com.knms.core.im.IMHelper;
import com.knms.core.im.uinfo.UserInfoHelper;
import com.knms.util.L;
import com.knms.util.Tst;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.friend.model.Friend;
import com.netease.nimlib.sdk.uinfo.UserInfoProvider;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.UserServiceObserve;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.sina.weibo.sdk.utils.LogUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.baidu.location.d.j.O;
import static com.knms.android.R.id.user;
import static com.knms.util.SPUtils.KeyConstant.account;

/**
 * 用户资料数据缓存，适用于用户体系使用网易云信用户资料托管
 * 注册缓存变更通知，请使用UserInfoHelper的registerObserver方法
 * Created by huangjun on 2015/8/20.
 */

public class NimUserInfoCache {
    public static NimUserInfoCache getInstance() {
        return InstanceHolder.instance;
    }
    static class InstanceHolder {
        final static NimUserInfoCache instance = new NimUserInfoCache();
    }
    private Map<String, NimUserInfo> account2UserMap = new ConcurrentHashMap<>();
    /**
     * 构建缓存与清理
     */
    public void buildCache() {
        List<NimUserInfo> users = NIMClient.getService(UserService.class).getAllUserInfo();
        addOrUpdateUsers(users, false);
        L.i_im("build NimUserInfoCache completed, users totalCount = " + account2UserMap.size());
    }

    public void clear() {
        clearUserCache();
    }
    public NimUserInfo getUserInfoFromLocal(String account){
        if (TextUtils.isEmpty(account)) {
            return null;
        }
        NimUserInfo user1 = account2UserMap.get(account);
        if(user1 != null && !TextUtils.isEmpty(user1.getName())) return user1;
        NimUserInfo user2 = NIMClient.getService(UserService.class).getUserInfo(account);
        return user2;
    }
    public void getUserInfoFromRemote(String account, final RequestCallback<NimUserInfo> callback){
        NIMClient.getService(UserService.class).fetchUserInfo(Arrays.asList(account)).setCallback(new RequestCallback<List<NimUserInfo>>() {
            @Override
            public void onSuccess(List<NimUserInfo> param) {
                if(callback != null) callback.onSuccess((param != null && param.size() > 0) ? param.get(0) : null);
            }
            @Override
            public void onFailed(int code) { }
            @Override
            public void onException(Throwable exception) { if(callback != null) callback.onException(exception);}
        });
    }
    /**
     * ******************************* 业务接口（获取缓存的用户信息） *********************************
     */
    public Observable<NimUserInfo> getUserInfoObserable(String account){
        NimUserInfo userInfo = getUserInfoFromLocal(account);
        if(userInfo != null){
            return Observable.just(userInfo).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
        }
        return Observable.create(new NimUserInfoOnSubscribe(account)).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }
    private void clearUserCache() {
        account2UserMap.clear();
    }

    /**
     * ************************************ 用户资料变更监听(监听SDK) *****************************************
     */

    /**
     * 在Application的onCreate中向SDK注册用户资料变更观察者
     */
    public void registerObservers(boolean register) {
        NIMClient.getService(UserServiceObserve.class).observeUserInfoUpdate(userInfoUpdateObserver, register);
    }

    /**
     * 修改完资料后必须调用fetchUserInfo方法获取自己的信息，这样在发消息给别人后别人才会触发资料变更回调方法
     */
    private Observer<List<NimUserInfo>> userInfoUpdateObserver = new Observer<List<NimUserInfo>>() {
        @Override
        public void onEvent(List<NimUserInfo> users) {
            if (users == null || users.isEmpty()) {
                return;
            }
            addOrUpdateUsers(users, true);
        }
    };

    /**
     * *************************************** User缓存管理与变更通知 ********************************************
     */

    private void addOrUpdateUsers(final List<NimUserInfo> users, boolean notify) {
        if (users == null || users.isEmpty()) {
            return;
        }
        // update cache
        for (NimUserInfo u : users) {
            account2UserMap.put(u.getAccount(), u);
        }
        List<String> accounts = getAccounts(users);
        // 通知变更
        if (notify && accounts != null && !accounts.isEmpty()) {
            UserInfoHelper.notifyChanged(accounts);//通知到UI组件
        }
    }
    private List<String> getAccounts(List<NimUserInfo> users) {
        if (users == null || users.isEmpty()) {
            return null;
        }

        List<String> accounts = new ArrayList<>(users.size());
        for (NimUserInfo user : users) {
            accounts.add(user.getAccount());
        }

        return accounts;
    }
}
