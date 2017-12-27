package com.knms.bean.user;

import com.google.gson.annotations.SerializedName;
import com.knms.bean.repair.Repair;

/**
 * Created by Administrator on 2017/1/12.
 */

public class UserType {
    @SerializedName("userType")
    public String type;//账号类型 0：普通用户，1：商户，2：维修师傅
    public String shopId;//店铺id
    public Repair masterInfo;//维修师傅信息
}
