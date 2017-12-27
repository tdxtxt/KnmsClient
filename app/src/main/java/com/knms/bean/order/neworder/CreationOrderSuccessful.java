package com.knms.bean.order.neworder;

import java.util.List;

/**
 * Created by Administrator on 2017/8/10.
 */

public class CreationOrderSuccessful {


    public  String tradingStatus;//0，未成功 1 生成 成功
    public List<String> tradingIds;

    public String systemEventId;
    public double totalMoney;
    public String totalMoneyTitle;

    public List<PreviewOrder.OrderTradingBosBean> orderTradingBos;

//    public static class OrderTradingBosBean {
//        public String tradingId;
//        public String shopIsHide;
//        public String shopId;
//        public String shopName;
//        public String tradingTransportMoneyTitle;
//        public String actualMoneyTitle;
//        public double actualMoney;
//
//        public List<OrderTradingCommoditysBean> orderTradingCommoditys;
//
//        public static class OrderTradingCommoditysBean {
//            public String showId;
//            public String showName;
//            public String specificationId;
//            public String specificationImg;
//            public String parameterBriefing;
//            public String showMoney;
//            public String realityMoney;
//            public String buyNumber;
//            public String totalShowMoney;
//            public String checkMessage;
//        }
//    }

}
