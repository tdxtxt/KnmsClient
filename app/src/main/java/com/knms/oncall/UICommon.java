package com.knms.oncall;

import android.app.Activity;

import com.knms.view.progress.CircleProgressDialog;

/**
 * Created by Administrator on 2016/8/29.
 */
public abstract class UICommon {
    CircleProgressDialog circleProgressDialog;
    public void showProgress(Activity activity){
        if(!(activity != null && !activity.isFinishing())) return;
        if(circleProgressDialog == null) circleProgressDialog = new CircleProgressDialog(activity);
        if(circleProgressDialog.isShowing()) circleProgressDialog.dismiss();

        circleProgressDialog.showDialog();
    }
    public void hideProgress(Activity activity){
        if(!(activity != null && !activity.isFinishing())) return;
        if(circleProgressDialog != null) circleProgressDialog.dismiss();
    }
}
