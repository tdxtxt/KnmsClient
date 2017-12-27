package com.knms.bean.order.neworder;

import java.util.List;

/**
 * Created by Administrator on 2017/8/9.
 */

public class PreviewOrder {

    public String systemEventId;
    public double totalMoney;
    public String totalMoneyTitle;

    public List<OrderTradingBosBean> orderTradingBos;

    
    public static class OrderTradingBosBean {
        public String tradingId;
        public String shopIsHide;
        public String shopId;
        public String shopName;
        public String tradingTransportMoneyTitle;
        public String actualMoneyTitle;
        public double actualMoney;
        public String checkMessage;
        public String tradingRemark;
       

        public List<OrderTradingCommoditysBean> orderTradingCommoditys;


        public static class OrderTradingCommoditysBean {
            public String showId;
            public String showName;
            public String specificationId;
            public String specificationImg;
            public String parameterBriefing;
            public String buyNumber;
            public String checkMessage;
            public String showMoney;
            public String realityMoney;


        }
    }
}
