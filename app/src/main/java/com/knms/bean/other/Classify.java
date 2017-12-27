package com.knms.bean.other;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by tdx on 2016/8/26.
 */
public class Classify implements Serializable{
    @SerializedName("clid")
    public String id;
    @SerializedName("clname")
    public String name;
    @SerializedName("clseq")
    public int seq;
    @SerializedName("clPhoto")
    public String photo;
    @SerializedName("classifyDto")
    public List<Classify> classifyChild;
}
