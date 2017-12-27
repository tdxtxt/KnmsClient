package com.knms.bean.repair;

import com.knms.bean.other.Pic;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2016/10/18.
 */
public class MyRepair implements Serializable {
    /**
     * reid : c3c87cd29df5456b808dc8c5d9440f00
     * image : d2e8af75012e4da4aab9625bf4fa75c890e5b76e83ef4de08167d37ada680238.jpg
     * reremark : 咯女URI
     * retype : 茶几桌子维修
     * rearea : 黑龙江省 | 大兴安岭地区
     * restate : 0
     * recreated : 2016-10-18 11:35:31.0
     * imglist : ["d2e8af75012e4da4aab9625bf4fa75c890e5b76e83ef4de08167d37ada680238.jpg"]
     * commentList : []
     */

    public String reid;
    public String image;
    public String reremark;
    public String retype;
    public String rearea;
    public int restate;
    public String recreated;
    public List<String> imglist;
    public List<CommentList> commentList;
    public String areaId;
    public String repairTypeId;
    public List<Pic> pic;

    public class CommentList implements Serializable{
        public String userid;
        public String usnickname;
        public String usphoto;
        public String concattime;
    }
//    public String remark;
//    public String area;
//    public String areaId;
//    public String repairTypeId;
//    public String repairTypeName;
//    public List<Pic> pics;


}
