package com.knms.bean.order;

import java.util.List;

/**
 * Created by Administrator on 2016/9/8.
 * 订单详情
 */
public class OldOrderDetail {
    public String orid;
    public String orshopname;
    public String orreceivercontacts;
    public String orreceiverphone;
    public String orcreated;
    public String ordeliverytime;
    public String ordeliverylocation;
    public String orcontractno;
    public String orcontractamount;
    public String oramountpaid;
    public int contractstatus;
    public Integer complaintstate;
    public String orrefundamount;
    public String preferremark;
    public class Complaints {
        public String complaintsId;//投诉Id
        public String complaintsData;//
        public String complaintsTime;//
        public String complaintsType;//
        public String serviceTime;//客服受理时间
        public String serviceData;//
        public String dealWithTime;//
        public int status;//
    }

}
