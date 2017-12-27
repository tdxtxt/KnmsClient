package com.knms.core.im.msg;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/10/20.
 */

public class Product implements Serializable{
    public String icon;//图片
    public String content;//描述内容
    public String price;//价格
    public String productId;//详情id
    public String userId;//发布人id
    public String productType;//产品类型【1、家装风格详情(参数：风格类型type、店铺id、商品id) DecorationStyleDetailsActivity
                                      // 2、闲置详情(参数:商品id) IdleDetailsActivity
                                      // 3、定制家具详情(参数:商品id) CustomFurnitureDetailsActivity
                                      // 4、爆款活动商品详情(参数:商品id) ProductDetailsBaokActivity
                                      // 5、分类商品详情(参数:商品id) ProductDetailsOrdinaryActivity
                                      // 6、我的维修详情(参数:id)】MineRepairDetailActivity
                                      // 7、我的比比货详情(参数:id，？)】MinePriceRationDetailsActivity
                                      // 8、可购买的商品详情(参数:id) ProductDetailsActivity
    public String attachJson;//以json格式传递参数


    @Override
    public String toString() {
        return "icon:" + this.icon + ";content:" + this.content + ";price:" + this.price;
    }
}
