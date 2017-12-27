package com.knms.bean.order.neworder;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/8/9.
 */

public class RequestBuyParameters implements Serializable{
    private static final long serialVersionUID = -8313544534857053309L;
    public int buyQuantity;//	商品数量 大于0
    public String specificationId;//	string（40）	是	商品规格系统ID
}
