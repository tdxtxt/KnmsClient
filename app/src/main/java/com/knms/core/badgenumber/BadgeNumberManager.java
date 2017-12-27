package com.knms.core.badgenumber;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;

/**
 * 应用桌面角标设置的管理类
 * Created by and on 2017 17/8/23 14:50.
 */

public class BadgeNumberManager {

    private Context mContext;

    private BadgeNumberManager(Context context) {
        mContext = context;
    }

    public static BadgeNumberManager from(Context context) {
        return new BadgeNumberManager(context);
    }

    private static final Impl IMPL;


    /**
     * 设置应用在桌面上显示的角标数字
     *
     * @param number 显示的数字
     */
    public void setBadgeNumber(int number) {
        IMPL.setBadgeNumber(mContext, number);
    }

    interface Impl {

        void setBadgeNumber(Context context, int number);

    }

    static class ImplHuaWei implements Impl {

        @Override
        public void setBadgeNumber(Context context, int number) {
            BadgeNumberManagerHuaWei.setBadgeNumber(context, number);
        }
    }

    static class ImplXiaoMi implements Impl {

        @Override
        public void setBadgeNumber(Context context, int number) {
            //小米机型的桌面应用角标API跟通知绑定在一起了，所以单独做处理
//            BadgeNumberManagerXiaoMi.setBadgeNumber(context, number);
        }
    }

    static class ImplVIVO implements Impl {

        @Override
        public void setBadgeNumber(Context context, int number) {
            BadgeNumberManagerVIVO.setBadgeNumber(context, number);
        }
    }

    static class ImplOPPO implements Impl {

        @Override
        public void setBadgeNumber(Context context, int number) {
            BadgeNumberManagerOPPO.setBadgeNumber(context, number);
        }
    }

    static class ImplHTC implements Impl {

        @Override
        public void setBadgeNumber(Context context, int number) {
            BadgeNumberManagerHTC.setBadgeNumber(context, number);
        }
    }
    static class ImplSamSung implements Impl {

        @Override
        public void setBadgeNumber(Context context, int number) {
            BadgeNumberManagerSamsung.setBadgeNumber(context, number);
        }
    }

    static class ImplBase implements Impl {

        @Override
        public void setBadgeNumber(Context context, int number) {
            //do nothing
        }
    }

    static {
        String manufacturer = Build.MANUFACTURER;
        if (manufacturer.equalsIgnoreCase(MobileBrand.HUAWEI)) {
            IMPL = new ImplHuaWei();
        } else if (manufacturer.equalsIgnoreCase(MobileBrand.XIAOMI)) {
            IMPL = new ImplXiaoMi();
        } else if (manufacturer.equalsIgnoreCase(MobileBrand.VIVO)) {
            IMPL = new ImplVIVO();
        } else if (manufacturer.equalsIgnoreCase(MobileBrand.OPPO)) {
            IMPL = new ImplOPPO();
        } else if(manufacturer.equalsIgnoreCase(MobileBrand.HTC)) {
            IMPL = new ImplHTC();
        } else if(manufacturer.equalsIgnoreCase(MobileBrand.SAMSUNG)){
            IMPL = new ImplSamSung();
        }
        else {
            IMPL = new ImplBase();
        }
    }
    public static String getLauncherClassName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        // To limit the components this Intent will resolve to, by setting an
        // explicit package name.
        intent.setPackage(context.getPackageName());
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        // All Application must have 1 Activity at least.
        // Launcher activity must be found!
        ResolveInfo info = packageManager
                .resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        // get a ResolveInfo containing ACTION_MAIN, CATEGORY_LAUNCHER
        // if there is no Activity which has filtered by CATEGORY_DEFAULT
        if (info == null) {
            info = packageManager.resolveActivity(intent, 0);
        }
        //////////////////////另一种实现方式//////////////////////
        // ComponentName componentName = context.getPackageManager().getLaunchIntentForPackage(mContext.getPackageName()).getComponent();
        // return componentName.getClassName();
        //////////////////////另一种实现方式//////////////////////
        return info.activityInfo.name;
    }
}
