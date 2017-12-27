package com.knms.bean.other;

import com.google.gson.annotations.SerializedName;

/**
 * Created by tdx on 2016/8/29.
 */
public class CustomType {
    @SerializedName("cuid")
    public String id;
    @SerializedName("cuname")
    public String name;
    @SerializedName("cuseq")
    public int seq;
}
