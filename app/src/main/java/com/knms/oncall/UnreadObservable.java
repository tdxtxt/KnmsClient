package com.knms.oncall;

import android.os.Handler;
import android.os.Looper;
import com.knms.bean.other.TipNum;
import com.knms.core.storage.Svn;
import com.knms.util.SPUtils;
import java.util.Observable;
/**
 * Created by tdx on 2017/4/26.
 */

public class UnreadObservable extends Observable {
    public void sendData(TipNum tipNum){
        if(!SPUtils.isLogin()){//没有登录
            tipNum = new TipNum();
        }else{
            if(tipNum == null) tipNum = new TipNum();
            Svn.put2Account("tipNum",tipNum);
        }
        final TipNum finalTipNum = tipNum;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                UnreadObservable.this.setChanged();//标记此 Observable对象为已改变的对象
                UnreadObservable.this.notifyObservers(finalTipNum);//通知所有的观察者
            }
        });
        this.setChanged();//标记此 Observable对象为已改变的对象
        this.notifyObservers(tipNum);//通知所有的观察者
    }
    public void sendData(){
        TipNum tipNum = Svn.getFromAccount("tipNum");
        if(tipNum == null) tipNum = new TipNum();
        final TipNum finalTipNum = tipNum;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                UnreadObservable.this.setChanged();//标记此 Observable对象为已改变的对象
                UnreadObservable.this.notifyObservers(finalTipNum);//通知所有的观察者
            }
        });
    }
}
