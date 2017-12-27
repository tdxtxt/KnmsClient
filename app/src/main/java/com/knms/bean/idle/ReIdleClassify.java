package com.knms.bean.idle;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by tdx on 2016/9/9.
 */
public class ReIdleClassify implements Serializable{
    @SerializedName("clid")
    public String id;
    @SerializedName("clname")
    public String name;
    @SerializedName("clseq")
    public int seq;
    @SerializedName("clparentid")
    public String parentId;
    @SerializedName("clisnext")
    public String isnext;
    @SerializedName("cltype")
    public String type;
    @SerializedName("classify")
    public List<ReIdleClassify> subClassifys;
}
