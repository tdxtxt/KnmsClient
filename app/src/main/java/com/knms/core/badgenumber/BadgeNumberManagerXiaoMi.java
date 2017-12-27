package com.knms.core.badgenumber;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;

import com.knms.android.R;
import com.yuyh.library.imgsel.common.Constant;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 小米机型的桌面角标设置管理类
 * Created by zlq on 2017 17/8/23 16:35.
 */

public class BadgeNumberManagerXiaoMi {

    public static void setBadgeNumber(Context context, int number) {
        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(context)
                .setContentTitle("title").setContentText("text").setSmallIcon(R.drawable.icon_knms);
        Notification notification = builder.build();

        try {
            Field field = notification.getClass().getDeclaredField("extraNotification");
            Object extraNotification = field.get(notification);
            Method method = extraNotification.getClass().getDeclaredMethod("setMessageCount", int.class);
            method.invoke(extraNotification, number);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        mNotificationManager.notify(0, notification);
    }
}
