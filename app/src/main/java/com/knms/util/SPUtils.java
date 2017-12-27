package com.knms.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.knms.app.KnmsApp;
import com.knms.bean.other.TipNum;
import com.knms.bean.product.Ad;
import com.knms.bean.user.User;
import com.knms.core.storage.Svn;
import com.umeng.analytics.MobclickAgent;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by Administrator on 2016/8/23.
 */
public class SPUtils {
    /**
     * 保存在手机里面的文件名
     */
    private static final String APP_COMMON_FILE_NAME = "Knms";
    private static final String ACCOUNT_FILE_NAME = "AccountCommon";

    public static boolean saveToApp(String key, Object object) {
        SharedPreferences sp = KnmsApp.getInstance().getSharedPreferences(APP_COMMON_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        if (object instanceof String) {
            editor.putString(key, (String) object);
        } else if (object instanceof Integer) {
            editor.putInt(key, (Integer) object);
        } else if (object instanceof Boolean) {
            editor.putBoolean(key, (Boolean) object);
        } else if (object instanceof Float) {
            editor.putFloat(key, (Float) object);
        } else if (object instanceof Long) {
            editor.putLong(key, (Long) object);
        } else {
            editor.putString(key, object.toString());
        }
        return editor.commit();
    }

    /**
     * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
     *
     * @param key
     * @param defaultObject
     * @return
     */
    public static Object getFromApp(String key, Object defaultObject) {
        SharedPreferences sp = KnmsApp.getInstance().getSharedPreferences(APP_COMMON_FILE_NAME, Context.MODE_PRIVATE);
        if (defaultObject instanceof String) {
            return sp.getString(key, (String) defaultObject);
        } else if (defaultObject instanceof Integer) {
            return sp.getInt(key, (Integer) defaultObject);
        } else if (defaultObject instanceof Boolean) {
            return sp.getBoolean(key, (Boolean) defaultObject);
        } else if (defaultObject instanceof Float) {
            return sp.getFloat(key, (Float) defaultObject);
        } else if (defaultObject instanceof Long) {
            return sp.getLong(key, (Long) defaultObject);
        }
        return null;
    }

    /**
     * 保存数据的方法，我们需要拿到保存数据的具体类型，然后根据类型调用不同的保存方法
     *
     * @param key
     * @param object
     */
    public static boolean saveToAccount(String key, Object object) {
        String currentAccount = getCurrentMobile();
        if (!TextUtils.isEmpty(key) && !key.endsWith(currentAccount))
            key = key + "_" + currentAccount;
        SharedPreferences sp = KnmsApp.getInstance().getSharedPreferences(ACCOUNT_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        if (object instanceof String) {
            editor.putString(key, (String) object);
        } else if (object instanceof Integer) {
            editor.putInt(key, (Integer) object);
        } else if (object instanceof Boolean) {
            editor.putBoolean(key, (Boolean) object);
        } else if (object instanceof Float) {
            editor.putFloat(key, (Float) object);
        } else if (object instanceof Long) {
            editor.putLong(key, (Long) object);
        } else {
            editor.putString(key, object.toString());
        }

        // SharedPreferencesCompat.apply(editor);
        return editor.commit();
    }

    public static String getCurrentMobile() {
        return (String) SPUtils.getFromApp(KeyConstant.currentMobile, "");
    }

    public static void saveCurrentMoblie(String moblie) {
        SPUtils.saveToApp(KeyConstant.currentMobile, moblie);
    }

    /**
     * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
     *
     * @param key
     * @param defaultObject
     * @return
     */
    public static Object getFromAccount(String key, Object defaultObject) {
        String currentAccount = (String) SPUtils.getFromApp(KeyConstant.currentMobile, "youke");
        if (!TextUtils.isEmpty(key) && !key.endsWith(currentAccount))
            key = key + "_" + currentAccount;
        SharedPreferences sp = KnmsApp.getInstance().getSharedPreferences(ACCOUNT_FILE_NAME, Context.MODE_PRIVATE);
        if (defaultObject instanceof String) {
            return sp.getString(key, (String) defaultObject);
        } else if (defaultObject instanceof Integer) {
            return sp.getInt(key, (Integer) defaultObject);
        } else if (defaultObject instanceof Boolean) {
            return sp.getBoolean(key, (Boolean) defaultObject);
        } else if (defaultObject instanceof Float) {
            return sp.getFloat(key, (Float) defaultObject);
        } else if (defaultObject instanceof Long) {
            return sp.getLong(key, (Long) defaultObject);
        }
        return null;
    }

    /**
     * 序列化对象
     *
     * @return
     * @throws IOException
     */
    private static String serialize(Object obj) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(obj);
        String serStr = byteArrayOutputStream.toString("ISO-8859-1");
        serStr = java.net.URLEncoder.encode(serStr, "UTF-8");
        objectOutputStream.close();
        byteArrayOutputStream.close();
        return serStr;
    }

    /**
     * 反序列化对象
     *
     * @param str
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private static Object deSerialization(String str) throws IOException, ClassNotFoundException {
        String redStr = java.net.URLDecoder.decode(str, "UTF-8");
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(redStr.getBytes("ISO-8859-1"));
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        Object obj = objectInputStream.readObject();
        objectInputStream.close();
        byteArrayInputStream.close();
        return obj;
    }

    public static boolean saveSerializable(String key, Object obj) {
        String str = "";
        try {
            str = serialize(obj);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return SPUtils.saveToApp("key_" + key + obj.getClass().getSimpleName(), str);
    }

    public static boolean clearSerializable(String key, Class clazz) {
        return clearKey("key_" + key + clazz.getSimpleName());
    }

    public static <T> T getSerializable(String key, Class<T> clazz) {
        T obj = null;
        String str = (String) SPUtils.getFromApp("key_" + key + clazz.getSimpleName(), "");
        try {
            obj = (T) deSerialization(str);
        } catch (Exception e) {
//            e.printStackTrace();
            return obj;
        }
        return obj;
    }

    public static boolean saveToAccountSerializable(String key, Object obj) {
        String str = "";
        try {
            str = serialize(obj);
        } catch (IOException e) {
//            e.printStackTrace();
        }
        return SPUtils.saveToAccount(key, str);
    }
    public static <T> T getFromAccountSerializable(String key, Class<T> clazz) {
        T obj = null;
        String str = (String) SPUtils.getFromAccount(key, "");
        try {
            obj = (T) deSerialization(str);
        } catch (Exception e) {
            e.printStackTrace();
            return obj;
        }
        return obj;
    }

    public static boolean saveNotificationStatus(boolean status) {
        return SPUtils.saveToApp("notificationStatus", status);
    }

    public static boolean getNotificationStatus() {
        return (boolean) SPUtils.getFromApp("notificationStatus", true);
    }

    public static boolean saveUser(User user) {
        String str = "";
        try {
            str = serialize(user);
        } catch (IOException e) {
            e.printStackTrace();
        }
        MobclickAgent.onProfileSignIn(user.account);
        return SPUtils.saveToApp(KeyConstant.currentUser, str);
    }

    public static boolean saveFirstAd(Ad ad) {
        String str = "";
        try {
            str = serialize(ad);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return SPUtils.saveToApp(KeyConstant.firstAd, str);
    }

    public static Ad getFirstAd() {
        Ad ad = null;
        String str = (String) SPUtils.getFromApp(KeyConstant.firstAd, "");
        try {
            ad = (Ad) deSerialization(str);
        } catch (Exception e) {
            e.printStackTrace();
            return ad;
        }
        return ad;
    }

    public static User getUser() {
        User userInfo;
        String str = (String) SPUtils.getFromApp(KeyConstant.currentUser, "");
        if(TextUtils.isEmpty(str)) return new User();
        try {
            userInfo = (User) deSerialization(str);
        } catch (Exception e) {
            e.printStackTrace();
            return new User();
        }
        return userInfo;
    }

    public static boolean isLogin() {
        return (Boolean) SPUtils.getFromApp(KeyConstant.loginState, false);
    }

    public static boolean saveLoginStatus(boolean status) {
        return SPUtils.saveToApp(KeyConstant.loginState, status);
    }

    /**
     * 清除所有数据
     */
    public static void clear() {
        MobclickAgent.onProfileSignOff();
        String mobllie = getCurrentMobile();
        SharedPreferences sp = KnmsApp.getInstance().getSharedPreferences(APP_COMMON_FILE_NAME, Context.MODE_PRIVATE);
        String imgUrl = sp.getString(KeyConstant.homePageAd, "");
        String version = sp.getString(KeyConstant.current_versions,"");

        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.commit();
        saveCurrentMoblie(mobllie);
        SPUtils.saveToApp(KeyConstant.homePageAd, imgUrl);
        SPUtils.saveToApp(KeyConstant.current_versions,version);
        Svn.deleteAllFromApp();
    }

    /**
     * 清除key数据
     */
    private static boolean clearKey(String key) {
        SharedPreferences sp = KnmsApp.getInstance().getSharedPreferences(APP_COMMON_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(key);
        return editor.commit();
    }

    public static class KeyConstant {
        public static String account = "account";
        public static String knmsid = "knmsid";
        public static String loginState = "loginState";
        public static String currentMobile = "currentMobile";
        public static String currentUser = "user";
        public static String searchProductHistory = "searchProductHistory";
        public static String searchIdleHistory = "searchIdleHistory";
        public static String searchShopHistory = "searchShopHistory";
        public static String imAccount = "imAccount";
        public static String time = "time";
        public static String homePageAd = "homePageAd";
        public static String k_ex = "k_ex";
        public static String current_versions = "current_versions";
        public static String cashCouponNumber = "cashCouponNumber";
        public static String cooperationCouponNumber = "cooperationCouponNumber";
        public static String isNewCashInvalidCoupon="isNewCashInvalidCoupon";
        public static String isNewCooperationInvalidCoupon="isNewCooperationInvalidCoupon";
        public static String firstAd="firstAd";
//        public static String tipNum = "tipNum";
    }


}
