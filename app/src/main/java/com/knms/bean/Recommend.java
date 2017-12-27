package com.knms.bean;

import java.util.List;

/**
 * Created by Administrator on 2017/9/18.
 */

public class Recommend {

    public List<CommodityShowBosBean> commodityShowBos;

    public static class CommodityShowBosBean {
        public String shopId;
        public String shopImage;
        public String showId;
        public String showName;
        public String realityPrice;
        public int collectNumber;
        public int browseNumber;
        public int iscollectNumber;
    }
}
