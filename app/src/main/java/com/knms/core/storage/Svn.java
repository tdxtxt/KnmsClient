package com.knms.core.storage;

import android.content.Context;
import android.text.TextUtils;
import com.knms.util.SPUtils;
import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.List;

import static android.R.attr.value;


/**
 * Created by Administrator on 2017/6/6.
 */

public class Svn {
    private static String fromApp = "fromApp_";
    private static String fromAccount = "fromAccount_";
    public static void init(Context context){
        Hawk.init(context).build();
    }
    public static <T> boolean put2App(String key,T value){
        if(TextUtils.isEmpty(key)) return false;
        String newKey = convertKey(key,fromApp);
        if(!Hawk.contains(newKey)){
            List<String> keys = Hawk.get("all_appkey");
            if(keys == null) keys = new ArrayList<>();
            keys.add(newKey);
        }
        return Hawk.put(newKey, value);
    }
    public static <T> boolean put2Account(String key,T value){
        if(TextUtils.isEmpty(key) || !SPUtils.isLogin()) return false;
        return Hawk.put(convertKey(key,fromAccount), value);
    }
    public static <T> T getFromApp(String key){
        if(TextUtils.isEmpty(key)) return null;
        return Hawk.get(convertKey(key,fromApp));
    }
    public static <T> T getFromAccount(String key){
        if(TextUtils.isEmpty(key) || !SPUtils.isLogin()) return null;
        return Hawk.get(convertKey(key,fromAccount));
    }
    public static void deleteAllFromApp(){
        List<String> keys = Hawk.get("all_appkey");
        if(keys == null) return;
        for (String key : keys) {
            Hawk.delete(key);
        }
    }
    public static boolean deleteAll(){
        return Hawk.deleteAll();
    }
    private static String convertKey(String key,String type){
        if(fromAccount.equals(type)){
            String current = SPUtils.getCurrentMobile();
            return fromAccount + current + key;
        }else if(fromApp.equals(type)){
            return fromApp + key;
        }
        return key;
    }
}
