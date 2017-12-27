package com.knms.bean.other;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2017/3/22.
 */

public class FastLabel implements Serializable{
    @SerializedName("styleList")
    public List<Label> styles;
    @SerializedName("classifyList")
    public List<Classify> classifies;

}
