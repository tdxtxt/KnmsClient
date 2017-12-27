package com.knms.core.badgenumber;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.knms.util.L;

import java.util.List;

/**
 * 类描述：
 * 创建人：Administrator
 * 创建时间：2017/12/18 10:38
 * 传参：
 * 返回:
 */
public class BadgeNumberManagerSamsung {
    private static final String INTENT_ACTION = "android.intent.action.BADGE_COUNT_UPDATE";
    private static final String INTENT_EXTRA_BADGE_COUNT = "badge_count";
    private static final String INTENT_EXTRA_PACKAGENAME = "badge_count_package_name";
    private static final String INTENT_EXTRA_ACTIVITY_NAME = "badge_count_class_name";
    public static void setBadgeNumber(Context context , int badgeCount){
        ComponentName componentName = new ComponentName(context.getPackageName(),BadgeNumberManager.getLauncherClassName(context));
        Intent intent = new Intent(INTENT_ACTION);
        intent.putExtra(INTENT_EXTRA_BADGE_COUNT, badgeCount);
        intent.putExtra(INTENT_EXTRA_PACKAGENAME, componentName.getPackageName());
        intent.putExtra(INTENT_EXTRA_ACTIVITY_NAME, componentName.getClassName());
        if (BadgeNumberManagerSamsung.canResolveBroadcast(context, intent)) {
            context.sendBroadcast(intent);
        } else {
            L.i("badge","unable to resolve intent: samsumg");
        }
    }
    public static boolean canResolveBroadcast(Context context, Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> receivers = packageManager.queryBroadcastReceivers(intent, 0);
        return receivers != null && receivers.size() > 0;
    }
}
