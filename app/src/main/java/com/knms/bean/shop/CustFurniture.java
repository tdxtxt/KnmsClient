package com.knms.bean.shop;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2016/8/31.
 * 店铺  定制家具列表
 */
public class CustFurniture {
    @SerializedName("inid")
    public String inid;//商品id
    @SerializedName("cotitle")
    public String cotitle;//标题
    @SerializedName("collectNumber")
    public int collectNumber;//收藏量
    @SerializedName("browseNumber")
    public int browseNumber;//浏览量
    @SerializedName("coInspirationPic")
    public String coInspirationPic;//图片
    @SerializedName("iscollectNumber")
    public String iscollectNumber;//是否收藏
}
