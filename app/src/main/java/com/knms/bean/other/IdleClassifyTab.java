package com.knms.bean.other;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Administrator on 2016/9/2.
 */
public class IdleClassifyTab {
    @SerializedName("clid")
    public String id;
    @SerializedName("clname")
    public String name;
    @SerializedName("clseq")
    public int seq;
    @SerializedName("clcreated")
    public String created;
    @SerializedName("clparentid")
    public String parentid;
    @SerializedName("clisnext")
    public String isnext;
    @SerializedName("cltype")
    public String type;
    @SerializedName("picArr")
    public List<String> imglist;

}
