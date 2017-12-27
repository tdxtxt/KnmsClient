package com.knms.bean.welfareservice;

import java.util.List;

/**
 * Created by Administrator on 2016/9/26.
 */
public class StoreCoupon {

    public List<ValidBean> receiveValid;

    public List<ValidBean> valid;
    public List<ValidBean> overdue;
    public List<ValidBean> used;
    public static class ValidBean {
        public String spid;
        public String spname;
        public int spmoney;
        public int spconditions;
        public String spvalid;
        public String spinvalid;
        public int sparea;
        public int sptype;
        public Object spurl;

    }

}
