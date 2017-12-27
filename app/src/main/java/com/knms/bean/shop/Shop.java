package com.knms.bean.shop;

import com.google.gson.annotations.SerializedName;
import com.knms.bean.other.Coupons;
import java.util.List;

/**
 * Created by Administrator on 2016/8/31.
 * 店铺基本信息
 */
public class Shop {
    @SerializedName("ssid")
    public String id;//店铺id
    @SerializedName("ssname")
    public String name;//店铺名称
    @SerializedName("ssmark")
    public String mark;//备注
    @SerializedName("ssphone")
    public String phone;//电话1
    @SerializedName("shopphone")
    public String shopPhone;//电话2
    @SerializedName("sslogo")
    public String logo;//店铺logo
    public String collectNumber;//被收藏数量
    public String photo;//背景图
    public int isprefer ;//是否有优惠券 0是存在,1是不存在
    public String iscollect;//当前用户是否已收藏
    public List<Coupons> preferList;//优惠券列表
    public String ssmerchantid;//聊天id
    public String ssaddress;//店铺地址
    public int commentCount;//店铺评价数量


}
