package com.knms.bean.pay;

import com.google.gson.annotations.SerializedName;

/**
 * 类描述：支付订单生成
 * 创建人：Administrator
 * 创建时间：2017/8/25 11:49
 * 传参：
 * 返回:
 */
public class OrderPayBody {
    @SerializedName("orderPayBo")
    public OrderPay data;

    public class OrderPay{
        public String payId;
        public String paymentOrderNumber;
        public String payType;
        public String paySysAppdata;
    }
}
