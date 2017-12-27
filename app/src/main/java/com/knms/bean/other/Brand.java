package com.knms.bean.other;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2016/8/31.
 */
public class Brand {
    @SerializedName("brandId")
    public String id;
    @SerializedName("brandName")
    public String name;
    public Brand(){}
    public Brand(String name){
        this.name = name;
        this.id = "";
    }
}
