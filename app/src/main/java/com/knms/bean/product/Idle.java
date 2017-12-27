package com.knms.bean.product;

import com.google.gson.annotations.SerializedName;
import com.knms.bean.other.Pic;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2016/8/24.
 */
public class Idle implements Serializable{
    private static final long serialVersionUID = 9058766079344094415L;
    @SerializedName("goid")
    public String id;
    @SerializedName("gooriginal")
    public double original;
    @SerializedName("goprice")
    public double price;
    @SerializedName("gotype")
    public int type;
    @SerializedName("goreleasetime")
    public String leasetime;
    @SerializedName("goclassifyid")
    public String classifyid;
    @SerializedName("goarea")
    public String area;
    @SerializedName("gorebate")
    public double rebate;
    @SerializedName("gofreeshop")
    public double freeshop;
    @SerializedName("goisrecommend")
    public int isrecommend;
    @SerializedName("gofreeshopprice")
    public double freeshopprice;
    public List<Pic> imglist;
    public int collectNumber;
    public int browseNumber;
    public String coremark;
    public String cotitle;
    public String usid;
    @SerializedName("usnickname")
    public String nickname;
    public String userPhoto;
    public String imgstr;
    public String goareaname;
    public int iscollectNumber;//0收藏；



}
