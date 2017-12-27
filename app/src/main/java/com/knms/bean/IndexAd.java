package com.knms.bean;

import com.google.gson.annotations.SerializedName;
import com.knms.bean.product.Ad;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2016/8/24.
 */
public class IndexAd implements Serializable{
    private static final long serialVersionUID = -2163570419872921564L;
    @SerializedName("A1")
    public List<Ad> a1;
    @SerializedName("A2")
    public List<Ad> a2;
    @SerializedName("A3")
    public List<Ad> a3;
    @SerializedName("A4")
    public List<Ad> a4;
    @SerializedName("A5")
    public List<Ad> a5;
    @SerializedName("A6")
    public List<Ad> a6;
    @SerializedName("A7")
    public List<Ad> a7;
    @SerializedName("A8")
    public List<Ad> a8;
    @SerializedName("A9")
    public List<Ad> a9;
    @SerializedName("A10")
    public List<Ad> a10;
    @SerializedName("B1")
    public List<Ad> b1;
    @SerializedName("C1")
    public List<Ad> c1;
    @SerializedName("D1")
    public List<Ad> d1;
    @SerializedName("E1")
    public List<Ad> e1;
    @SerializedName("E2")
    public List<Ad> e2;
    @SerializedName("E3")
    public List<Ad> e3;
    @SerializedName("E4")
    public List<Ad> e4;
    @SerializedName("E5")
    public List<Ad> e5;
    @SerializedName("E6")
    public List<Ad> e6;
    @SerializedName("E7")
    public List<Ad> e7;
    @SerializedName("E8")
    public List<Ad> e8;
    @SerializedName("E9")
    public List<Ad> e9;
    @SerializedName("A0")
    public List<Ad> a0;
    @SerializedName("A11")
    public List<Ad> a11;
    @SerializedName("AA")
    public List<Ad> aa;
//    @SerializedName("A12")
//    public List<Ad> a12;//极速比货,1.4.5版本之前数据格式
//    @SerializedName("AN1")
//    public List<Ad> an1;//爆款活动,1.4.5版本之前数据格式
    @SerializedName("AB1")
    public List<Ad> ab1;//爆款活动1
    @SerializedName("AB2")
    public List<Ad> ab2;//爆款活动2
    @SerializedName("AB3")
    public List<Ad> ab3;//爆款活动3
    @SerializedName("AB4")
    public List<Ad> ab4;//爆款活动4
    @SerializedName("AB5")
    public List<Ad> ab5;//爆款活动5
    @SerializedName("AB6")
    public List<Ad> ab6;//标题

    @SerializedName("AM1")
    public List<Ad> am1;//买手特价商品1
    @SerializedName("AM2")
    public List<Ad> am2;//买手特价商品2
    @SerializedName("AM3")
    public List<Ad> am3;//买手特价商品3
    @SerializedName("AM4")
    public List<Ad> am4;//买手特价商品4
    @SerializedName("AM5")
    public List<Ad> am5;//买手特价商品5
    @SerializedName("AM6")
    public List<Ad> am6;//标题

    @SerializedName("AH1")
    public List<Ad> ah1;//活动广告位
    @SerializedName("AJ1")
    public List<Ad> aj1;//极速比货广告位

    @SerializedName("AN2")
    public List<Ad> an2;//定制家具
    @SerializedName("AN3")
    public List<Ad> an3;//买手活动
    @SerializedName("AN4")
    public List<Ad> an4;//家居风格
}
