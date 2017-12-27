package com.knms.bean.product;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.google.gson.annotations.SerializedName;

/**
 * Created by tdx on 2016/8/31.
 */
public class ClassifyGood implements MultiItemEntity {
    @SerializedName("goid")
    public String id;
    @SerializedName("coInspirationPic")
    public String pic;
    public int browseNumber;
    @SerializedName("goprice")
    public double price;
    @SerializedName("gooriginal")
    public double orPrice;
    @SerializedName("cotitle")
    public String title;
    @SerializedName("goisrecommend")
    public int isRecommend;//是否特价（1是,0不是）
    @SerializedName("iscollectNumber")
    public int iscollectNumber;//是否收藏（0表示收藏，1表示没有收藏）
    public int gotype;//	宝贝类型：0展示商品, 6表示商城商品
    public String showPrice;
    public String realityPrice;

    @Override
    public int getItemType() {
        return isRecommend;
    }
}
