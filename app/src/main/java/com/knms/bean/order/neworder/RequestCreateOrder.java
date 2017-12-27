package com.knms.bean.order.neworder;

import java.util.List;

/**
 * Created by Administrator on 2017/8/10.
 */

public class RequestCreateOrder {
    public String systemEventId;//	String（40）	是	相同ID 代表相同事件,APP 每一次进行新的
    public String userId;//string（40）	是	资金账号ID ，根据账号ID 每ID 每 秒频次1
    public String addressId;//	string（40）	是	送货地址 ID
    public List<orderParameterBosBean> orderParameterBos;

    public static class orderParameterBosBean {
        public String tradingId;//string（40）	是	订单ID
        public String remarks;//string（40）	否	订单备注
    }

}
