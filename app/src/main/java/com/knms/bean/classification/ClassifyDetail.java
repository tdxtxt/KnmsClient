package com.knms.bean.classification;

import com.google.gson.annotations.SerializedName;
import com.knms.bean.other.Coupons;
import com.knms.bean.other.Pic;

import java.util.List;

import cn.shareuzi.bean.ShareEntity;

/**
 * Created by Administrator on 2016/9/6.
 */
public class ClassifyDetail {
    public String goid;//商品id
    public String cotitle;//
    public String coremark;//
    public double gooriginal;//
    public double goprice;//
    public int collectNumber;//
    public int browseNumber;//
    public int iscollectNumber;//当前用户是否已收藏（0：是，1：否）
    public String usid;//店铺id
    public String usnickname;//
    public String userPhoto;//
    public int coState;//是否下架：0正常,非0（3或4）下架
    @SerializedName("ssmerchantid")
    public String sid;
    public List<Pic> imglist;
    public List<Coupons> preferList;//优惠券list
    public ShareEntity shareData;
    @SerializedName("attributeList")
    public List<Attribute> attributes;
    @SerializedName("serviceList")
    public List<ServiceInfo> serviceInfos;
    public CommentGs goodsComment;
    public class Attribute{
        @SerializedName(value = "attributeKey",alternate = {"paramName","key"})
        public String name;
        @SerializedName(value = "attributeValue",alternate = {"paramValue","value"})
        public String value;
        @SerializedName(value = "attributeSort",alternate = {"weight","sorting"})
        public int weight;
    }
    public class ServiceInfo{
        @SerializedName("sename")
        public String name;
        @SerializedName("seremark")
        public String remark;
        @SerializedName("sephoto")
        public String photo;
        @SerializedName("url")
        public String detailUrl;
        public int seq;
    }
    public class CommentGs{
        public int count;
        public String userPhoto;
        public String nikeName;
        public double score;
        public String created;
        public int state;//是否为精彩评价（1：否，2：是）
        public String content;
        @SerializedName("parameterbriefing")
        public String spesc;//评论的商品规格描述
        public List<Pic> imgList;
    }
}
