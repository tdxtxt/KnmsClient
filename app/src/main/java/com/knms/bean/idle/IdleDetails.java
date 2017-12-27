package com.knms.bean.idle;

import com.knms.bean.other.Pic;

import java.util.List;

import cn.shareuzi.bean.ShareEntity;

/**
 * Created by Administrator on 2016/9/6.
 * 闲置家具详情
 */
public class IdleDetails {
    public String goid;//商品Id
    public String gooriginal;//商品原价
    public String goprice;//商品价格
    public String goreleasetime;//上架时间
    public String goarea;//	商品所在区域
    public String goareaname;//商城区域名字
    public int collectNumber;//
    public int browseNumber;//
    public String coremark;//描述
    public String usid;//创建闲置商品的用户id
    public String usnickname;//
    public String userPhoto;//
    public int iscollectNumber;//是否收藏（0：是，1：否）
    public int gofreeshop;//是否包邮（0：是，1：否）
    public double gofreeshopprice;//运费（0：是，1：否）
    public int coState;//是否下架：0正常,非0（3或4）下架
    public List<Pic> imglist;
    public ShareEntity shareData;
}
