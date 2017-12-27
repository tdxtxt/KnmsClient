package com.knms.bean.myparity;

import com.knms.bean.other.Label;
import com.knms.bean.other.Pic;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2016/9/13.
 * 我的比比价
 */
public class MyParity implements Serializable{
//    "coid": "002b54caacee40d391f406c77055ed96",
//            "coremark": "1914有没有更加便宜的1914",
//            "cocreated": "2016-09-05 14:40:29",
//            "coInspirationPic": "比比价.png",
//            "coState": 1,
//            "coType": 2,
//            "goid": "",
    public String coid;
    public String coremark;
    public String cocreated;//发布时间
    public String coInspirationPic;//主图
    public String coState;//状态(0：下架。1：正常)
    public int coType;//(2：个性比比价。5：内部商品比比货)
    public String goid;
    public String updatetime;
    public List<Label> labelList;
    public List<Reply> commentList;//商户联系记录列表(list)
    public List<Pic> imglist;
    public class Reply implements Serializable{
        public String contactTime;
        public String shopId;
        public String shopLogo;
        public String shopName;
        public String merchantId;
    }

}
