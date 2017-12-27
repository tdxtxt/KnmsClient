package com.knms.bean.welfareservice;

import java.util.List;

/**
 * Created by Administrator on 2016/9/27.
 */
public class StoreCouponClassify {
    private String couponTypeName;
    private List<StoreCoupon.ValidBean> couponList;

    public String getCouponTypeName() {
        return couponTypeName;
    }

    public void setCouponTypeName(String couponTypeName) {
        this.couponTypeName = couponTypeName;
    }

    public List<StoreCoupon.ValidBean> getCouponList() {
        return couponList;
    }

    public void setCouponList(List<StoreCoupon.ValidBean> couponList) {
        this.couponList = couponList;
    }
}
