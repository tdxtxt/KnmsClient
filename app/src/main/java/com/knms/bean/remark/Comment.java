package com.knms.bean.remark;

import com.google.gson.annotations.SerializedName;
import com.knms.bean.other.Pic;

import java.util.List;

/**
 * Created by Administrator on 2017/5/2.
 */

public class Comment {
    public String id;
    @SerializedName("userId")
    public String sid;
    public String userPhoto;
    public String nikeName;
    public int state;//是否为精彩评价 （1：否 2:是）
    public int score;//星级
    public String created;//评论时间
    public String content;//评论内容
    public int browsenumber;//浏览量
    public int agreenumber;//点赞数量
    public int isAgree;//是否点赞（0：否，1：是）
    //2017-8-25新增商品规格字段；仅限于可买的商品
    @SerializedName("parameterbriefing")
    public String specDesc;
    public List<Pic> imgList;//图片列表
    public ShopReply shopReply;//商家回复
    public class ShopReply{
        public String content;
        public String created;
    }
}
