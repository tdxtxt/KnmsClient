package cn.shareuzi.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;

import java.io.Serializable;

import cn.shareuzi.ShareDialogActivity;
import cn.shareuzi.ShareHandlerActivity;
import cn.shareuzi.bean.ShareEntity;
import cn.shareuzi.interfaces.ShareConstant;

/**
 * Created by zhanglifeng on 15/6/4.
 * https://github.com/xyzlf/ShareSDK
 */
public class ShareUtil {

    /**
     * 分享数据
     * @param activity Activity
     * @param channel {@link ShareConstant}
     * @param data {@link ShareEntity}
     * @param requestCode {@link Activity#onActivityResult(int, int, Intent)}
     */
    public static void startShare(Activity activity, int channel, ShareEntity data, int requestCode) {
        if (null == activity || activity.isFinishing()) {
            return;
        }
        Intent intent = new Intent(activity, ShareHandlerActivity.class);
        intent.putExtra(ShareConstant.EXTRA_SHARE_CHANNEL, channel);
        intent.putExtra(ShareConstant.EXTRA_SHARE_DATA, data);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 调起ShareDialogActivity
     * @param activity Activity
     * @param data {@link ShareEntity}
     * @param requestCode requestCode
     */
    public static void showShareDialog(Activity activity, ShareEntity data, int requestCode) {
        if(data == null) return;
        showShareDialog(activity, ShareConstant.SHARE_CHANNEL_ALL, data, requestCode);
    }

    /**
     * 调起ShareDialogActivity
     * @param activity Activity
     * @param data {@link ShareEntity}
     * @param channel {@link ShareConstant#SHARE_CHANNEL_ALL}
     * @param requestCode requestCode
     */
    public static void showShareDialog(Activity activity, int channel, ShareEntity data, int requestCode) {
        if (null == activity || activity.isFinishing()) {
            return;
        }
        Intent intent = new Intent(activity, ShareDialogActivity.class);
        intent.putExtra(ShareConstant.EXTRA_SHARE_DATA, data);
        intent.putExtra(ShareConstant.EXTRA_SHARE_CHANNEL, channel);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * data is sparsearray
     * @param activity {@link Activity}
     * @param data {@link ShareEntity}
     * @param requestCode requestCode
     */
    public static void showShareDialog(Activity activity, SparseArray<ShareEntity> data, int requestCode) {
        showShareDialog(activity, ShareConstant.SHARE_CHANNEL_ALL, data, requestCode);
    }

    /**
     * data is sparsearray
     * @param activity Activity
     * @param channel 渠道
     * @param data data
     * @param requestCode requestCode
     */
    public static void showShareDialog(Activity activity, int channel, SparseArray<ShareEntity> data, int requestCode) {
        if (null == activity || activity.isFinishing()) {
            return;
        }
        Intent intent = new Intent(activity, ShareDialogActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(ShareConstant.EXTRA_SHARE_DATA, (Serializable) data);
        intent.putExtra(ShareConstant.EXTRA_SHARE_DATA, bundle);
        intent.putExtra(ShareConstant.EXTRA_SHARE_CHANNEL, channel);
        activity.startActivityForResult(intent, requestCode);
    }


    public static boolean startActivity(Context context, Class<?> c) {
        boolean result = true;
        try {
            Intent intent = new Intent(context, c);
            context.startActivity(intent);
        } catch (ActivityNotFoundException exception) {
            result = false;
            exception.printStackTrace();
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }
        return result;
    }

    public static boolean startActivity(Context context, Intent intent) {
        boolean bResult = true;
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            bResult = false;
            e.printStackTrace();
        } catch (Exception e) {
            bResult = false;
            e.printStackTrace();
        }
        return bResult;
    }

}
