package com.knms.bean.other;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by tdx on 2016/9/18.
 */
public class Label implements Serializable{
    @SerializedName(value = "labelId",alternate = {"laid","shopid","cuid"})
    public String id;
    @SerializedName(value = "labelName",alternate = {"laname","shopname","cuname"})
    public String name;
    @SerializedName("lableList")
    public List<Label> subLables;

    public String parentId;
    @Override
    public String toString() {
        return "parentId:" + this.parentId + ";id:" + this.id + ";name:" + this.name;
    }
    @Override
    public boolean equals(Object arg0) {
        if(arg0 instanceof Label){
            if(this.name.equals(((Label) arg0).name)){
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }
}
