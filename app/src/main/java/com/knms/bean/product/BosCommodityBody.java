package com.knms.bean.product;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 类描述：爆款商品列表对象
 * 创建人：tdx
 * 创建时间：2017/8/10 15:58
 */
public class BosCommodityBody {
    @SerializedName("commodityShowBos")
    public List<SimpCommodity> commoditys;
    public class SimpCommodity{
        @SerializedName("shopImage")
        public String pic;
        @SerializedName("showId")
        public String id;
        @SerializedName("showName")
        public String name;
        @SerializedName("realityPrice")
        public String showPrice;
        public int collectNumber;
        public int browseNumber;
        public int iscollectNumber;//当前用户是否收藏(0是,1否)
    }

}
