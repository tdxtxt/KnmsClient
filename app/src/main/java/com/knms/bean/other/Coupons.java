package com.knms.bean.other;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/9/18.
 * 优惠券列表
 */
public class Coupons implements Serializable{
    public String spid;//id
    public String spmoney;//优惠金额
    public String spconditions;//优惠条件值
    public String spvalid;//优惠券开始时间
    public String spinvalid;//优惠券截止时间
    public int isReceive;
}
