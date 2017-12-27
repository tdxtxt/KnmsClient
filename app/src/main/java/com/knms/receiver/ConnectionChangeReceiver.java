package com.knms.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.knms.core.im.IMHelper;
import com.knms.util.SystemInfo;

import static com.knms.util.SystemInfo.NETWORN_NONE;

/**
 * Created by tdx on 2016/12/8.
 * 开机&网络状态改变触发广播
 */

public class ConnectionChangeReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        if(SystemInfo.getNetworkState() != NETWORN_NONE){
            IMHelper.getInstance().loginIM();
        }
    }
}
