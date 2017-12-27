package com.knms.bean.order.neworder;

/**
 * Created by Administrator on 2017/8/17.
 */

public class InitRefunds {

    /**
     * tradingCommodityId : 79aa35eb61864c3ab7e6b54943ab1c6719faee08
     * showId : 2a9049932b394b67b5b087638806b054d743b62e
     * showName : 特价秒杀  0.01元秒杀实木衣柜，数量有限，先到先得，犹豫就没有了哦
     * specificationImg : 0f805ee60a944fcf856ee68bf40780b058cdcc785265415faa6614aa660b76a5.jpg
     * parameterBriefing : 颜色:原木色|材质:实木
     * showMoney : 4176.00
     * realityMoney : 0.01
     * totalTransportMoney : 0.00
     */

    public OrderTradingCommodityBoBean orderTradingCommodityBo;

    public static class OrderTradingCommodityBoBean {
        public String tradingCommodityId;
        public String showId;
        public String showName;
        public String specificationImg;
        public String parameterBriefing;
        public String showMoney;
        public String totalRealityMoney;
        public String totalTransportMoney;
    }
}
