package com.knms.bean.shop;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2016/9/9.
 * 店铺  家装风格列表
 */
public class DeStyle {
    @SerializedName("inid")
    public String inid;//商品id
    @SerializedName("cotitle")
    public String cotitle;//标题
    @SerializedName("collectNumber")
    public int collectNumber;//收藏数量
    @SerializedName("browseNumber")
    public int browseNumber;//浏览量
    @SerializedName("coInspirationPic")
    public String coInspirationPic;//图片
    @SerializedName("iscollectNumber")
    public String iscollectNumber;//是否收藏 0收藏
}
