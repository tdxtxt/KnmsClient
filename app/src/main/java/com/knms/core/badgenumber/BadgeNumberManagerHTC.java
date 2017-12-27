package com.knms.core.badgenumber;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

/**
 * 类描述：
 * 创建人：Administrator
 * 创建时间：2017/12/12 13:51
 * 传参：
 * 返回:
 */
public class BadgeNumberManagerHTC{
    public static void setBadgeNumber(Context context, int count){
        Intent intentNotification = new Intent("com.htc.launcher.action.SET_NOTIFICATION");
        ComponentName localComponentName = new ComponentName(context.getPackageName(),
                BadgeNumberManager.getLauncherClassName(context));
        intentNotification.putExtra("com.htc.launcher.extra.COMPONENT", localComponentName.flattenToShortString());
        intentNotification.putExtra("com.htc.launcher.extra.COUNT", count);
        context.sendBroadcast(intentNotification);

        Intent intentShortcut = new Intent("com.htc.launcher.action.UPDATE_SHORTCUT");
        intentShortcut.putExtra("packagename", context.getPackageName());
        intentShortcut.putExtra("count", count);
        context.sendBroadcast(intentShortcut);
    }
}
