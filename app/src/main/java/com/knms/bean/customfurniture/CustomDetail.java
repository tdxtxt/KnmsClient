package com.knms.bean.customfurniture;

import com.knms.bean.other.Pic;

import java.util.List;

import cn.shareuzi.bean.ShareEntity;

/**
 * Created by jyy on 2016/9/5.
 */
public class CustomDetail {
    public String inid;//商品id
    public String cotitle;//标题
    public String coremark;//描述
    public int collectNumber;//收藏数量
    public int browseNumber;//浏览数量
    public int iscollectNumber;//当前用户是否收藏(0是,1否)
    public String shopid;//店铺id
    public String ssmerchantid;//聊天id
    public String shopPhoto;//店铺logo
    public String shopName;//店铺名称
    public int coState;//是否下架：0正常,非0（3或4）下架
    public List<Pic> imglist;//图片列表
    public List<Service> serviceList;//服务列表
    public ShareEntity shareData;
    public class Service {
        public String sename;//服务名称
        public String seremark;//服务备注
        public String sephoto;//标识图片
    }
}
