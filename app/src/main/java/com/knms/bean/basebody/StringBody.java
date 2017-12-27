package com.knms.bean.basebody;

import com.google.gson.annotations.SerializedName;

/**
 * 类描述：
 * 创建人：Administrator
 * 创建时间：2017/9/14 9:08
 * 传参：
 * 返回:
 */
public class StringBody {
    @SerializedName(value = "data",alternate = {"systemEventId"})
    public String systemEventId;
}
