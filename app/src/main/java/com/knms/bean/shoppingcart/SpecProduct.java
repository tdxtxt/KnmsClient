package com.knms.bean.shoppingcart;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017/7/28.
 */

public class SpecProduct {
    @SerializedName("shoppingCartSpecificationId")
    public String shopcartSpecId;
    @SerializedName("showGoodsSpecificationId")
    public String skuId;
    @SerializedName("showId")
    public String productId;
    @SerializedName("showName")
    public String productName;
    @SerializedName("specificationImg")
    public String specImg;
    @SerializedName("commodityUserLimitBuyNumber")
    public int userLimitBuyNumber;//展示商品基于账号 的限购数量， 等于0 表示不限购
    @SerializedName("limitTitle")
    public String toastLimitMsg;
    @SerializedName("specificationShowMoney")
    public double orprice;//展示价格
    @SerializedName("specificationRealityMoney")
    public double price;//交易价
    @SerializedName("planNumber")
    public int buyCount;//购物车数量
    @SerializedName("failure")
    public int state;//数据是否失效 : 0 表示失效 1 表示正常 2表示暂时为开抢
    @SerializedName("failureTitle")
    public String toastFailureMsg;
    @SerializedName(value = "commoditySpecification",alternate = {"specificationTitle"})
    public String specDesc;//规格描述
    @SerializedName("specificationTotalQuantity")
    public int count;//数量

    //下面字段自己添加的，非服务器返回
    public boolean isCheck;//是否被选中
    public SpecProduct(){}
    public SpecProduct(String skuId){
        this.shopcartSpecId = skuId;
    }
    @Override
    public boolean equals(Object o) {
        if(o == null) return false;
        if(!(o instanceof SpecProduct)) return false;
        SpecProduct other = (SpecProduct) o;
        if(TextUtils.isEmpty(this.shopcartSpecId)) this.shopcartSpecId = "";
        return this.shopcartSpecId.equals(other.shopcartSpecId);
    }
}
