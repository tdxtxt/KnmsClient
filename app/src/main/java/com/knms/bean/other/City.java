package com.knms.bean.other;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by tdx on 2016/9/9.
 */
public class City implements Serializable{
    public String id;
    public String name;
    @SerializedName("parentid")
    public String parentId;
    @SerializedName("systemacercodedto")
    public List<City> subCitys;
}
