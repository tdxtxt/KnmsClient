package cn.helper;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Administrator on 2016/11/7.
 */

public class Tst {
    public static void showToast(Context context, String msg){
        Toast.makeText(context,msg, Toast.LENGTH_SHORT).show();
    }
    public static void showToast(Context context, int resId){
        Toast.makeText(context,context.getResources().getString(resId), Toast.LENGTH_SHORT).show();
    }
}
