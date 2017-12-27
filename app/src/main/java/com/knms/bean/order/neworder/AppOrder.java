package com.knms.bean.order.neworder;

import java.util.List;

/**
 * Created by Administrator on 2017/7/19.
 */

public class AppOrder  {


    public List<OrderTradingBosBean> orderTradingBos;


    public static class OrderTradingBosBean {
        public String tradingId;
        public String tradingStatusTitle;
        public String tradingStatus;
        public String shopIsHide;
        public String shopId;
        public String shopName;
        public String tradingTransportMoneyTitle;
        public String actualMoney;
        public int effectiveNumber;
        public String userAppraise;//0 用户未评价 1用户已评价
        public String businessmenAppraise;//0 商户未回复 1 商户已未回复
        public String tradingLocking;
        public String orderType;
        public int isComplaint;//1表示没有投诉，2表示投诉中

        public List<OrderTradingCommoditysBean> orderTradingCommoditys;

    }
}
