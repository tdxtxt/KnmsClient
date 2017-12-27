package com.knms.bean.other;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2016/8/30.
 */
public class Tab {
    @SerializedName(value = "tabId",alternate = {"id"})
    public String id;
    @SerializedName(value = "tabTitle",alternate = {"classname"})
    public String title;
    @SerializedName(value = "tabSeq",alternate = {"seq"})
    public String seq;
}
