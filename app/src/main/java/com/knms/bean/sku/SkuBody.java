package com.knms.bean.sku;

import com.google.gson.annotations.SerializedName;
import com.knms.bean.sku.base.SkuModel;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2017/7/27.
 */

public class SkuBody {
    @SerializedName("commodityShowBo")
    public ComdiBo comdiBo;
    public class ComdiBo implements Serializable{
        private static final long serialVersionUID = -8884779873670175540L;
        @SerializedName("showId")
        public String productId;//商品id
        @SerializedName("showName")
        public String productName;//商品名称
        @SerializedName("realityPrice")
        public String defaultProce;//默认价格
        @SerializedName("showImagedefault")
        public String defaultProductImg;//默认图片
        /**展示订单最大购买数量，0 ：无限制， 大于0：单次选择数量不能大于，最大购买数**/
        @SerializedName("orderLimitBuyNumber")
        public int maxBuyNumByOrder;
        /**展示账号最大购买数量，0 ：无限制， 大于0： 不能大于，最大购买数**/
        @SerializedName("userLimitBuyNumber")
        public int maxBuyNumByAccount;
        /**限购数量提示语**/
        @SerializedName("limitBuyTitle")
        public String toastMsg;
        /**库存商品**/
        @SerializedName("commoditySpecifications")
        public List<SkuModel> skuProducts;

        public long getTotalStock(){
            if(skuProducts == null) return 0;
            long totalStock = 0;
            for (SkuModel sku : skuProducts) {
                totalStock += sku.stock;
            }
            return totalStock;
        }
    }
}
