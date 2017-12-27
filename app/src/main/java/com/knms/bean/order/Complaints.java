package com.knms.bean.order;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/9/30.
 */
public class Complaints implements Serializable, MultiItemEntity {


    /**
     * ocid : eceb483f00804fed8ab9ee80054d769b
     * occontent : 更好更好和更不可辜负肌肤哥哥的官方他
     * occomplaintstype : 服务安装
     * occreated : 2016-08-15 18:01:39
     * ocservicetime : 2016-08-15 18:15:59
     * ocdealwithtime : null
     * ocstate : 3
     * ocstates : 1
     */

    public String ocid;
    public String occontent;
    public String occomplaintstype;
    public String occreated;
    public String ocservicetime;
    public String ocdealwithtime;
    public int ocstate;
    public boolean isFold;


    @Override
    public int getItemType() {
        return ocstate;
    }
}
