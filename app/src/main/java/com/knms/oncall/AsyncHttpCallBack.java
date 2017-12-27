package com.knms.oncall;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.knms.bean.ResponseBody;

import java.lang.reflect.Type;

/**
 * Created by Administrator on 2016/8/23.
 */
public abstract class AsyncHttpCallBack<T> {
    public void start(){};
    public abstract void onSuccess(ResponseBody<T> body);
    public abstract void onFailure(String msg);
    public abstract Type setType();


}
