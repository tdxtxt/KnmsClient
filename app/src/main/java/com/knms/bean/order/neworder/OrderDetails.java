package com.knms.bean.order.neworder;

import java.util.List;

/**
 * Created by Administrator on 2017/8/15.
 */

public class OrderDetails {


    public OrderTradingBoBean orderTradingBo;


    public static class OrderTradingBoBean {
        public String tradingId;
        public String tradingStatusTitle;
        public String tradingStatus;
        public String mailingName;
        public String mailingPhone;
        public String deliveryStatus;
        public String mailingAddress;
        public String shopIsHide;
        public String shopId;
        public String shopName;
        public String tradingTransportMoneyTitle;
        public String actualMoney;
        public int effectiveNumber;
        public String buyerRemarks;
        public String userAppraise;
        public String businessmenAppraise;
        public int isComplaint;
        public String createTime;
        public String tradingSerialid;
        public String payTypeTitle;
        public String payOrderId;
        public String ssmerchantid;
        public String shopPhone;
        public int orderCountdown;
        public String tradingLocking;
        public String orderType;

        public List<OrderTradingCommoditysBean> orderTradingCommoditys;

    }
}
