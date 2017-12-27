package com.knms.bean.product;

import com.google.gson.annotations.SerializedName;

/**
 * Created by tdx on 2016/8/29.
 */
public class Furniture {
    @SerializedName("inid")
    public String id;
    @SerializedName("cotitle")
    public String title;
    public int collectNumber;
    public int browseNumber;
    @SerializedName("coInspirationPic")
    public String pic;
    @SerializedName("intype")
    public int type;
    public int iscollectNumber;
}
