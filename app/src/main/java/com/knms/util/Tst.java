package com.knms.util;

import android.text.TextUtils;
import android.view.Gravity;
import android.widget.Toast;

import com.knms.app.KnmsApp;
import com.knms.core.toast.SmartToast;

/**
 * Created by Administrator on 2016/8/23.
 */
public class Tst {
    public static boolean isShow = true;

    /**
     * 短时间显示Toast
     *
     * @param message
     */
    public static void showToast(CharSequence message) {

        if(!isShow || message == null) return;
        if (message.toString().contains("TimeoutException")) {
            message = "连接超时";
        } else if (message.toString().contains("ConnectException")||message.toString().contains("Connection")
                ||message.toString().contains("UnknownHostException")||message.toString().contains("Unable to resolve host")
                ||message.toString().contains("CompositeException")||message.toString().contains("Failed to connect")) {//Connection
            message = "网络不给力，请检查网络设置";
        } else if(message.toString().contains("Exception") || message.toString().contains("exception")){
            message = "";
        }
        if(!TextUtils.isEmpty(message)) {
//            Toast toast = Toast.makeText(KnmsApp.getInstance(), message, Toast.LENGTH_SHORT);
//            toast.setGravity(Gravity.CENTER, 0, 0);
//            toast.show();
            SmartToast.showInCenter(message);
        }
    }

    /**
     * 短时间显示Toast
     *
     * @param resid
     */
    public static void showToast(int resid) {
        if (isShow){
            String message = KnmsApp.getInstance().getResources().getString(resid);
            Toast toast = Toast.makeText(KnmsApp.getInstance(), message == null ? resid + "" : message, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }
}
