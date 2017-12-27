package com.knms.bean.myidle;

import com.google.gson.annotations.SerializedName;
import com.knms.bean.other.Pic;

import java.io.Serializable;
import java.util.List;

import cn.shareuzi.bean.ShareEntity;

/**
 * Created by Administrator on 2016/9/2.
 * 我的闲置列表
 */
public class MyIdle implements Serializable{
    @SerializedName("goid")
    public String goid;//商品id
    @SerializedName("coremark")
    public String coremark;//商品描述
    @SerializedName("collectNumber")
    public int collectNumber;//收藏量
    @SerializedName("browseNumber")
    public int browseNumber;//浏览量
    @SerializedName("goprice")
    public double goprice;//价格
    @SerializedName("updatetime")
    public String goreleasetime;//发布时间
    @SerializedName("coInspirationPic")
    public String coInspirationPic;//商品主图
    @SerializedName("goclassifyid")
    public String goclassifyid;//分类id
    @SerializedName("goclassifyname")
    public String goclassifyname;//分类名称
    @SerializedName("goarea")
    public String goarea;//区域编码
    @SerializedName("gofreeshop")
    public int isfreeshop;//	是否包邮（0：是，1：否）
    @SerializedName("gofreeshopprice")
    public double freeshopprice;//运输费
    @SerializedName("gooriginal")
    public double orprice;//原价
    @SerializedName("goareaname")
    public String goareaname;//区域名称
    @SerializedName("coState")
    public int coState;//0正常（上架）,1系统下架（只能删除）,3.下架
    public List<Pic> imglist;//图片list
    public ShareEntity shareData;
}
