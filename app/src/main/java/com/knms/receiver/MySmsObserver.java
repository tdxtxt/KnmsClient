package com.knms.receiver;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2016/10/20.
 */
public class MySmsObserver extends ContentObserver {
    private Context mContext;
    private Handler mHandler;
    public static final int MSG_RECEIVER_CODE = 1;


    public MySmsObserver(Context context, Handler handler) {
        super(handler);
        this.mContext = context;
        this.mHandler = handler;
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
        if (uri.toString().equals("content://sms/raw")) {
            return;
        }
        Uri queryUri = Uri.parse("content://sms/inbox");
        String code = "";
        Cursor cursor = mContext.getContentResolver().query(queryUri, null, null, null, "date desc");
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String address = cursor.getString(cursor.getColumnIndex("address"));
                String message = cursor.getString(cursor.getColumnIndex("body"));
                //判断
                Pattern pattern = Pattern.compile("(\\d{6})");
                Matcher matcher = pattern.matcher(message);
                if (matcher.find()) {
                    code = matcher.group(0);
                    mHandler.obtainMessage(MSG_RECEIVER_CODE, code).sendToTarget();
                }
            }

            cursor.close();
        }


    }
}

