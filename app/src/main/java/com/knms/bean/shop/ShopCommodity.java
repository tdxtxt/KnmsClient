package com.knms.bean.shop;

import com.chad.library.adapter.base.entity.MultiItemEntity;

/**
 * Created by Administrator on 2016/8/31.
 * 店铺 商品列表
 */
public class ShopCommodity implements MultiItemEntity {
    /**
     * goid : afe79d981121497b8f86a56ec5813ba4
     * coInspirationPic : 637815d7996e491581645dc5dd868d7e144cf334d3e34d6ca7555e97fb0821a1.jpg
     * browseNumber : 2
     * collectNumber : 0
     * gooriginal : 0
     * goprice : null
     * cotitle : 巴山夜雨-SH001餐边柜 规格：1455*400*1080
     * goisrecommend : 0
     * gotype : null
     * iscollectNumber : 1
     * showPrice : 0.00
     * realityPrice : 0.00
     */

    public String goid;
    public String coInspirationPic;
    public int browseNumber;
    public int collectNumber;
    public int gooriginal;
    public Object goprice;
    public String cotitle;
    public int goisrecommend;
    public int gotype;
    public int iscollectNumber;
    public String showPrice;
    public String realityPrice;

//    @SerializedName("goid")
//    public String goid;//商品id
//    @SerializedName("coInspirationPic")
//    public String coInspirationPic;//商品主图
//    @SerializedName("browseNumber")
//    public String browseNumber;//浏览量
//    @SerializedName("gooriginal")
//    public String gooriginal;//商品原价
//    @SerializedName("goprice")
//    public String goprice;//商品现价
//    @SerializedName("cotitle")
//    public String cotitle;//标题
//    @SerializedName("goisrecommend")
//    public int goisrecommend;//是否特价（1是，0否）
//    @SerializedName("iscollectNumber")
//    public int iscollectNumber;//是否收藏（0表示收藏，1表示没有收藏 ）
//    public String realityPrice;
//    public String showPrice;



    @Override
    public int getItemType() {
        return goisrecommend;
    }
}
