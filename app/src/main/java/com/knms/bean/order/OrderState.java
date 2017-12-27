package com.knms.bean.order;

import java.util.List;

/**
 * Created by Administrator on 2017/4/25.
 */

public class OrderState {
    public String orid;
    public int state;
    public ShopinfoBean shopinfo;
    public List<Complaints> complaintList;
    public List<DeliveryListBean> deliveryList;

    public static class ShopinfoBean {
        public String ssid;
        public String ssname;
        public String sslogo;
        public String ssphone;
        public String ssmerchantid;
        public String shopphone;
    }

    public static class DeliveryListBean {
        public int orderstate;
        public String created;
        public String delaytime;
        public String delayRemark;
        
    }
}
