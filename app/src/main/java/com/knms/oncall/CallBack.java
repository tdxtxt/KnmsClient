package com.knms.oncall;

/**
 * 类描述：
 * 创建人：Administrator
 * 创建时间：2017/8/25 15:42
 * 传参：
 * 返回:
 */
public interface CallBack<T> {
    public void onCallBack(T t);
}
