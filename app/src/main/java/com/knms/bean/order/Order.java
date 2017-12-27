package com.knms.bean.order;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/8/29.
 * 我的订单
 */
public class Order implements Serializable {


    /**
     * orid : 00117d4e27cf475c8b3af89c2ad8f6d9
     * orshopid : 
     * orshopname : 美格利生
     * orcontractno : 17-0070197
     * orcreated : 2017-02-27
     * orcontractamount : 200
     * oramountpaid : 200
     * deliverystate : 1
     * isComplaint : 2
     * orreceivercontacts : 
     * orreceiverphone : 15923947509
     * ordeliverytime : 2017-03-26
     * ordeliverylocation : 江北
     * orrefundamount : 0
     * contractstatus : 2
     * complaintstate : 2
     * preferremark : 含使用优惠券0
     */

    public String orid;
    public String orshopid;
    public String orshopname;
    public String orcontractno;
    public String orcreated;
    public String orcontractamount;
    public String oramountpaid;
    public int deliverystate;
    public int isComplaint;
    public String orreceivercontacts;
    public String orreceiverphone;
    public String ordeliverytime;
    public String ordeliverylocation;
    public String orrefundamount;
    public int contractstatus;
    public int complaintstate;
    public String preferremark;

 
}
