package com.knms.bean.other;

import com.google.gson.annotations.SerializedName;

/**
 * Created by tdx on 2016/8/31.
 */
public class Style {
    @SerializedName("laid")
    public String id;
    @SerializedName("laname")
    public String name;
    public Style(){
    }
    public Style(String name){
        this.name = name;
        this.id = "";
    }
}
