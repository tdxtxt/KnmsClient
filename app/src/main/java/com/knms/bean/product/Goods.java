package com.knms.bean.product;

import com.chad.library.adapter.base.entity.MultiItemEntity;

/**
 * Created by Administrator on 2017/3/22.
 */

public class Goods implements MultiItemEntity {
    public String goid;
    public String coInspirationPic;
    public int browseNumber;
    public String cotitle;
    public int goisrecommend;

    public int isRecommend;//是否特价（1是,0不是）
    public double price;

    @Override
    public int getItemType() {
        return isRecommend;
    }
}
