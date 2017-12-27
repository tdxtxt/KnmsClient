package com.knms.bean.order.neworder;

import java.util.List;

/**
 * Created by Administrator on 2017/8/16.
 */

public class RefundsDetails {

    public OrderTradingCommodityBoBean orderTradingCommodityBo;

    public List<OrderRecedeBosBean> orderRecedeBos;


    public static class OrderTradingCommodityBoBean {
        public String tradingCommodityId;
        public String showId;
        public String showName;
        public String specificationImg;
        public String parameterBriefing;
        public String totalRealityMoney;
        public String totalTransportMoney;

    }

    public static class OrderRecedeBosBean {
        public String recedeId;
        public String tradingCommodityId;
        public String tradingId;
        public String recedeMoney;
        public String recedeFreightMoney;
        public String recedeReason;
        public String recedeRemarks;
        public String createTime;
        public String recedeTo;
        public List<String> recedeImg;
        public String recedeType;
    }
}
