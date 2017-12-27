package com.knms.oncall;

/**
 * Created by Administrator on 2016/9/6.
 */
public abstract class ResponseHandlerInterface {
    public void progress(int progress, long cumulative){};
    public void start(String params){};
    public abstract void success(String result);
    public abstract void onFailure(String msg);
}
