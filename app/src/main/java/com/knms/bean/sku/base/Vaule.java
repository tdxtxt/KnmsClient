package com.knms.bean.sku.base;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/7/25.
 */

public class Vaule implements Serializable{
    private static final long serialVersionUID = 5158622073448803098L;
    public int id;
    @SerializedName("key")
    public String specName;//规格名称
    @SerializedName("value")
    public String name;//属性值
    @SerializedName("sorting")
    public int sort;//排序
    public int status = 0;//0可选,1已选,2不可选

    @Override
    public String toString() {
        return id + "";
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
    public String getContent(){
        return specName + ":" + name;
    }
    @Override
    public boolean equals(Object obj) {
        if(obj == null) return false;
        if (!(obj instanceof Vaule))
            return false;
        Vaule o = (Vaule) obj;
        return this.name.equals(o.name);
    }
}
