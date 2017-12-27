package com.knms.core.im.cache;

import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.Poi;
import com.knms.core.location.LocationOnSubscribe;
import com.netease.nimlib.sdk.InvocationFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by tdx on 2016/10/20.
 * 从云信服务器获取批量用户信息[异步]
 */

public class NimUserInfoOnSubscribe implements Observable.OnSubscribe<NimUserInfo>{
    List<String> accouts;
    public NimUserInfoOnSubscribe(String accout){
        this.accouts = Arrays.asList(accout);
    }
        @Override
        public void call(final Subscriber<? super NimUserInfo> subscriber) {
            MyRequestCallback callback = new MyRequestCallback(subscriber);
            NIMClient.getService(UserService.class).fetchUserInfo(accouts).setCallback(callback);
        }
    class MyRequestCallback implements RequestCallback<List<NimUserInfo>> {
        Subscriber<? super NimUserInfo> subscriber;
        public MyRequestCallback(Subscriber<? super NimUserInfo> subscriber){
            this.subscriber = subscriber;
        }

        @Override
        public void onSuccess(List<NimUserInfo> param) {
            if(param != null && param.size() > 0){
                subscriber.onNext(param.get(0));
            }
            subscriber.onCompleted();
        }

        @Override
        public void onFailed(int code) {
            subscriber.onCompleted();
        }
        @Override
        public void onException(Throwable exception) {
//            subscriber.onError(exception);
            subscriber.onCompleted();
        }
    }
}
