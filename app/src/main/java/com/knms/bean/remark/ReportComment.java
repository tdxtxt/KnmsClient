package com.knms.bean.remark;

import android.text.TextUtils;

import java.io.Serializable;

import rx.Observable;

/**
 * Created by Administrator on 2017/7/19.
 */

public class ReportComment implements Serializable{
    private static final long serialVersionUID = 7247714666080613254L;

    public String content;
    public Observable<String> observablePics;
    public String imgs = "";//上传服务器图片地址

    public String gid;
    public String productDesc = "萨哈个屁描述哟哟哟";
    public String productImg= "";
    public String specDesc;

    public ReportComment(String gid,String specDesc,String productDesc,String productImg){
        this.gid = gid;
        this.productDesc = productDesc;
        this.productImg = productImg;
        this.specDesc = specDesc;
    }
    public ReportComment(){
    }

    /**
     * true-提交数据都为空，false-有值
     * @return
     */
    public boolean isEmpty(){
        return TextUtils.isEmpty(content);
    }
}
