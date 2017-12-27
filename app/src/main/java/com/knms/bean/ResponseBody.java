package com.knms.bean;

import android.app.Activity;
import android.content.Intent;
import com.google.gson.annotations.SerializedName;
import com.knms.activity.main.MainActivity;
import com.knms.app.KnmsApp;
import com.knms.util.CommonUtils;
import com.knms.util.ConstantObj;
import java.io.Serializable;

/**
 * Created by tdx on 2016/8/23.
 */
public class ResponseBody<T> implements Serializable{
    public String code;
    @SerializedName(value = "msg",alternate = {"message","desc"})
    public String desc;
    @SerializedName(value = "responseData",alternate = {"data","globalData"})
    public T data;
    public boolean isSuccess() {
        if(ConstantObj.LOGIN_OUT.equals(this.code)){
            CommonUtils.logout();
            Activity activity = KnmsApp.getInstance().currentActivity();
            if(activity != null){
                Intent intent = new Intent(activity, MainActivity.class);
                intent.putExtra("source",ConstantObj.LOGIN_OUT);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);//如果在栈中发现存在Activity实例，则清空这个实例之上的Activity
                activity.startActivity(intent);
            }
        }
        return ConstantObj.OK.equals(code);
    }
    public boolean isSuccess1(){
        if(ConstantObj.LOGIN_OUT.equals(this.code)){
            CommonUtils.logout();
            Activity activity = KnmsApp.getInstance().currentActivity();
            if(activity != null){
                Intent intent = new Intent(activity, MainActivity.class);
                intent.putExtra("source",ConstantObj.LOGIN_OUT);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);//如果在栈中发现存在Activity实例，则清空这个实例之上的Activity
                activity.startActivity(intent);
            }
        }
        return "1".equals(code);
    }
}
