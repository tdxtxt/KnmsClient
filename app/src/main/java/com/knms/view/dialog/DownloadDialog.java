package com.knms.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;

import com.knms.android.R;

import static com.netease.nimlib.sdk.msg.constant.SystemMessageStatus.init;

/**
 * Created by Administrator on 2016/10/14.
 */

public class DownloadDialog extends Dialog {
    public DownloadDialog(Context context) {
        super(context);
    }

    public DownloadDialog(Context context, int theme) {
        super(context, theme);
    }

    protected DownloadDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }
    private void initView(Context context){
//        LayoutInflater layoutInflater = LayoutInflater.from(context);
//        View view = layoutInflater.inflate(R.layout.dialog_download_progress,null);
        setContentView(R.layout.dialog_download_progress);
    }
    public void setForce(){
        setOnKeyListener(keylistener);
        setCancelable(false);
    }
    public OnKeyListener keylistener = new OnKeyListener() {
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
                return true;
            } else {
                return false;
            }
        }
    };
}
