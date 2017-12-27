package com.knms.bean.product;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/8/24.
 */
public class Ad implements Serializable{
    private static final long serialVersionUID = -8617605637081422169L;
    @SerializedName("adurl")
    public String url;
    @SerializedName("adpositionid")
    public String positionid;
    @SerializedName("adtype")
    public int type;
    @SerializedName("adtime")
    public int time;
    @SerializedName("imname")
    public String name;
    public int imseq;
    public String cotitle;
}
