package com.knms.bean.complaint;

import com.knms.bean.other.Pic;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2016/9/9.
 * 投诉详情
 */
public class ComplaintDetail implements Serializable {


    /**
     * occreated : 2016-09-30 17:03:05
     * ocrelationmobile : 15923415628
     * ocrelationname : %E6%B5%8B%E8%AF%95
     * occomplaintstype : 服务安装
     * occontent : 没问题
     * imgList : []
     */

    public String occreated;
    public String ocrelationmobile;
    public String ocrelationname;
    public String occomplaintstype;
    public String occontent;
    public List<Pic> imgList;
    public String sysCurrentTime;
    public int ispayment;//1是，0否
    public String ocservicetime;
    public int ocstate;

}
