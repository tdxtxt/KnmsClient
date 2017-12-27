package com.knms.bean.address;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2017/7/27.
 */

public class ShippingAddres implements Serializable {

    /**
     * addressid : 0358d34f20bd48be9dda6911bf5f3f4007e70f8b
     * userid : XJXJXJXhkxhkjlxhjkdlahlfdhbalkfhdlkahfld
     * mailingname : 路人乙
     * mailingphone : 18512396507
     * mailingarea : 江北区
     * mailingaddress : 江北区花卉园
     * addresstype : 1
     */

    public List<orderMailingAddressBos> orderMailingAddressBos;

    public List<orderMailingAddressBos> getOrderMailingAddressBos() {
        return orderMailingAddressBos;
    }

    public void setOrderMailingAddressBos(List<orderMailingAddressBos> orderMailingAddressBos) {
        this.orderMailingAddressBos = orderMailingAddressBos;
    }

    public static class orderMailingAddressBos implements Serializable {
        public String addressid;
        public String mailingname;
        public String mailingphone;
        public String mailingarea;
        public String mailingaddress;
        public String addresstype;
        public String userid;
    }
}
