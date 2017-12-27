package com.knms.bean.address;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/7/28.
 */

public class CreateAddress implements Serializable{

    /**
     * addressid : 96ef966ef8fb4b25a791e7a9e1fa1a67c5816b6a
     * userid : XJXJXJXhkxhkjlxhjkdlahlfdhbalkfhdlkahfld
     * mailingname : 测试的
     * mailingphone : 13564875436
     * mailingarea : 重庆市渝中区
     * mailingaddress : 重庆渝中区解放碑威斯汀酒店31楼
     * addresstype : 2
     */

    public OrderMailingAddressBoBean orderMailingAddressBo;

    public static class OrderMailingAddressBoBean implements Serializable{
        public String addressid;
        public String userid;
        public String mailingname;
        public String mailingphone;
        public String mailingarea;
        public String mailingaddress;
        public String addresstype;
    }
}
