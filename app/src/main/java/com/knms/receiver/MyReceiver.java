package com.knms.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.knms.activity.main.MainActivity;
import com.knms.app.KnmsApp;
import com.knms.util.CommonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import cn.jpush.android.api.JPushInterface;

/**
 * 自定义接收器
 * 
 * 如果不定义这个 Receiver，则：
 * 1) 默认用户会打开主界面
 * 2) 接收不到自定义消息
 */
public class MyReceiver extends BroadcastReceiver {
	private static final String TAG = "MyJPush";

	@Override
	public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
		Log.d(TAG, "[MyReceiver] onReceive - " + intent.getAction() + ", extras: " + printBundle(bundle));

        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
            String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
            Log.d(TAG, "[MyReceiver] 接收Registration Id : " + regId);
            //send the Registration Id to your server...
                        
        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
        	Log.d(TAG, "[MyReceiver] 接收到推送下来的自定义消息: " + bundle.getString(JPushInterface.EXTRA_MESSAGE));
//        	processCustomMessage(context, bundle);
        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
            Log.d(TAG, "[MyReceiver] 接收到推送下来的通知");
            int notifactionId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
            Log.d(TAG, "[MyReceiver] 接收到推送下来的通知的ID: " + notifactionId);

        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
            Log.d(TAG, "[MyReceiver] 用户点击打开了通知");
//			String json = intent.getStringExtra(JPushInterface.EXTRA_EXTRA);
			if(KnmsApp.getInstance().isActLive(MainActivity.class)){
				String json = intent.getStringExtra(JPushInterface.EXTRA_EXTRA);
				CommonUtils.startActivity(context,json);
			}else {
				intent.setClass(context, MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
			}
//			CommonUtils.startActivity(context,json);
        	//打开自定义的Activity
//        	Intent i = new Intent(context, TestActivity.class);
//        	i.putExtras(bundle);
//        	//i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        	i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );
//        	context.startActivity(i);
        } else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent.getAction())) {
            Log.d(TAG, "[MyReceiver] 用户收到到RICH PUSH CALLBACK: " + bundle.getString(JPushInterface.EXTRA_EXTRA));
            //在这里根据 JPushInterface.EXTRA_EXTRA 的内容处理代码，比如打开新的Activity， 打开一个网页等..
        	
        } else if(JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent.getAction())) {
        	boolean connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false);
        	Log.w(TAG, "[MyReceiver]" + intent.getAction() +" connected state change to "+connected);
        } else {
        	Log.d(TAG, "[MyReceiver] Unhandled intent - " + intent.getAction());
        }
	}

	// 打印所有的 intent extra 数据
	private static String printBundle(Bundle bundle) {
		if(bundle == null) return "";
		StringBuilder sb = new StringBuilder();
		for (String key : bundle.keySet()) {
			if (key.equals(JPushInterface.EXTRA_NOTIFICATION_ID)) {
				sb.append("\nkey:" + key + ", value:" + bundle.getInt(key));
			}else if(key.equals(JPushInterface.EXTRA_CONNECTION_CHANGE)){
				sb.append("\nkey:" + key + ", value:" + bundle.getBoolean(key));
			} else if (key.equals(JPushInterface.EXTRA_EXTRA)) {
				if (bundle.getString(JPushInterface.EXTRA_EXTRA).isEmpty()) {
					Log.i(TAG, "This message has no Extra data");
					continue;
				}

				try {
					JSONObject json = new JSONObject(bundle.getString(JPushInterface.EXTRA_EXTRA));
					Iterator<String> it =  json.keys();

					while (it.hasNext()) {
						String myKey = it.next().toString();
						sb.append("\nkey:" + key + ", value: [" +
								myKey + " - " +json.optString(myKey) + "]");
					}
				} catch (JSONException e) {
					Log.e(TAG, "Get message extra JSON error!");
				}
			} else {
				sb.append("\nkey:" + key + ", value:" + bundle.getString(key));
			}
		}
		return sb.toString();
	}
	
	//send msg to MainActivity
	private void processCustomMessage(Context context, Bundle bundle) {
//		String message = bundle.getString(JPushInterface.EXTRA_MESSAGE);
//		String title = bundle.getString(JPushInterface.EXTRA_TITLE);
//		String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
//		Log.i("receiverMB","message:" + message + ";title:" + title + ";extras:" + extras);
//		TipNum tipNum = SPUtils.getFromAccountSerializable("tipNum",TipNum.class);
//		if(tipNum == null) tipNum = new TipNum();
//		if(CommonUtils.jsonContainKey(extras,"coupon")){
//			tipNum.coupon = 1;
//		}else if(CommonUtils.jsonContainKey(extras,"repair")){
//			tipNum.repair = 1;
//		}else if(CommonUtils.jsonContainKey(extras,"idel")){
//			tipNum.idel = 1;
//		}else if(CommonUtils.jsonContainKey(extras,"parity")){
//			tipNum.parity = 1;
//		}else if(CommonUtils.jsonContainKey(extras,"order")){
//			tipNum.order = 1;
//		}
//		KnmsApp.getInstance().getUnreadObservable().sendData(tipNum);

//		NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
//		NotificationCompat.Builder notification = new NotificationCompat.Builder(context);
//		notification.setAutoCancel(true)
//				.setContentText(message)
//				.setContentTitle(title)
//				.setSmallIcon(R.drawable.icon_knms);
//		notification.setWhen(System.currentTimeMillis()); // 设置来通知时的时间
//		notification.setSound(Uri.parse("android.resource://" + context.getPackageName() + "/" +R.raw.msg));
//		Intent mIntent = new Intent(context,MainActivity.class);
//		mIntent.putExtras(bundle);
//		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, mIntent, 0);
//		notification.setContentIntent(pendingIntent);
//		notificationManager.notify(2, notification.build());
	}
}
