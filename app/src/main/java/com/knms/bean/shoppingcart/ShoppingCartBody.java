package com.knms.bean.shoppingcart;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Administrator on 2017/7/28.
 */

public class ShoppingCartBody {
    @SerializedName("shoppingCartBos")
    public List<ShoppingCart> shopCarts;
    @SerializedName("shoppingCartSpecificationBo")
    public SpecProduct product;
    public class ShoppingCart{
        @SerializedName("shoppingCartId")
        public String id;//购物车id
        @SerializedName("businessmenName")
        public String shopName;
        @SerializedName("businessmenId")
        public String shopId;
        public int state;//店铺状态 1表示正常,2表示已经下线
        @SerializedName("commoditySpecificationList")
        public List<SpecProduct> specProducts;

        //下面字段自己添加的，非服务器返回
        public boolean isCheck;//是否被选中
        public boolean isEditor; //自己对该组的编辑状态

        @Override
        public boolean equals(Object o) {
            if(o == null) return false;
            if(!(o instanceof ShoppingCart)) return false;
            ShoppingCart other = (ShoppingCart) o;
            return this.id.equals(other.id);
        }
    }
}
