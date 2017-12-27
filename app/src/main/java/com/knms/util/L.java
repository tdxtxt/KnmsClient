package com.knms.util;

import android.util.Log;

import com.knms.android.BuildConfig;
import com.knms.net.HttpConstant;

/**
 * Created by Administrator on 2016/8/23.
 */
public class L {
    public static boolean isDebug = BuildConfig.isLog;//!HttpConstant.isFormalServers;
    private static final String Prefix = "Knms_";
    /**
     * 错误 Write By LILIN 2014-5-8
     *
     * @param msg
     */
    public static void e(String tag, String msg) {
        if (isDebug) {
            Log.e(Prefix + tag, msg + "");
        }
    }
    /**
     * 信息 Write By LILIN 2014-5-8
     *
     * @param msg
     */
    public static void i(String tag, String msg) {
        if (isDebug) {
            Log.i(Prefix + tag, msg + "");
        }
    }
    public static void i_http(String msg) {
        if (isDebug) {
            Log.i("http", msg + "");
        }
    }
    public static void i_im(String msg) {
        if (isDebug) {
            Log.i("chatIM", msg + "");
        }
    }
}
