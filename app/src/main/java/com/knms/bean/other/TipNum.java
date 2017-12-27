package com.knms.bean.other;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/5/26.
 */

public class TipNum{
    public int coupon;//优惠券模块
    public int repair;//维修模块
    public int idel;//闲置模块
    public int parity;//比比货模块
    public int order;//订单模块
    public int getTotal(){
        return this.coupon + this.repair + this.idel + this.parity + this.order;
    }
}
