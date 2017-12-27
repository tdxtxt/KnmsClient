package com.knms.bean.goodsdetails;

import com.google.gson.annotations.SerializedName;
import com.knms.bean.classification.ClassifyDetail;
import com.knms.bean.comm.MixingContentBean;
import com.knms.bean.other.Pic;
import com.knms.util.L;

import java.util.List;

import cn.shareuzi.bean.ShareEntity;

/**
 * Created by Administrator on 2017/7/31.
 */

public class GoodsDetails {

    public CommodityShowBoBean commodityShowBo;

    public static class CommodityShowBoBean {
        public String shopId;
        public String shopNickname;
        public String shopImage;
        public String showId;
        public String showName;
        public String showDescription;
        public String showType;//展示商品类型 1.实体物品 2.虚拟物品
        public String showTypeTitle;//展示商品类型 字符标题 显示
        public String showStartTimeActivity;//是否显示商品消费开始时间倒计时效果: 0:没这个效果 1：有这个效果
        public int showStartTime;//商品消费开始时间 倒计时：秒
        public String showEndTimeActivity;//是否显示商品消费结束时间倒计时效果: 0:没这个效果 1：有这个效果
        public int showEndTime;//商品消费结束时间 倒计时：秒
        public String showPrice;
        public String realityPrice;
        public String showTourTitle;
        public String showSalesTitle;
        public String showFreightTitle;
        public String showStatus;//商品状态 : 2.上架(正常状态) 3.下架
        public int collectNumber;
        public int browseNumber;
        public String ssmerchantid;
        public int iscollectNumber;//当前用户是否已收藏（0：是，1：否）

        public ShareEntity shareData;

        public ClassifyDetail.CommentGs goodsComment;

        public List<Pic> showImages;
        public List<ClassifyDetail.Attribute> showParameters;
        @SerializedName("remarkList")
        public List<MixingContentBean> showMix;//图文混合内容
        public List<ClassifyDetail.ServiceInfo> serviceList;
        
    }
}
