package com.knms.util;

import android.text.TextUtils;

/**
 * Created by tdx on 2016/9/5.
 */
public class ConstantObj {
    public final static int pageSize = 10;
    public final static String KEY_IMAGE_COUNT = "iamge_count";
    public final static String KEY_IMAGE_UPLOAD = "isUpload";
    public final static String OK = "0";
    /**
     * 网络请求错误状态码
     **/
    public final static String LOGIN_OUT = "2";//登出
    public final static String LOGIN_IN = "1";//登录
    public final static int STORAGE_REQUEST_CODE = 155;//检查权限

    public final static String FurnitureRepair = "00fce67afabf47b6b9a36a332bd86b3c";//家具维修
    public final static String FurnitureCustom = "3094b3cc46dc44f08140d62f16fb25c4";//定制家具
    public final static String ShoppingMall = "34de0da290e14860b74243de579cdf94";//商场活动
    public final static String FurnitureInsp = "6cbbccbc9d4c465aabfc8ef61270d17e";//家具灵感
    public final static String FurnitureIdle = "968914205f6a442a92a5fabdf544339e";//闲置家具

    public final static String SEND_TYPE_REGISTER = "registerMsg";//注册
    public final static String SEND_TYPE_LOGIN = "loginMsg";//登录
    public final static String SEND_TYPE_UPDATE_PSW = "updatePasMsg";//找回密码
    public final static String SEND_TYPE_UPDATE_MOBILE = "updateMobileMsg";//修改手机号码
    public final static String SEND_TYPE_NEW_MOBILE = "newMobileMsg";//新手机号码

    public static boolean isUpdating = false;//true目前正在请求更新接口中
    public static String TEMP_USERID = "";
//    public static String TEMP_USERID = (SPUtils.getUser() != null && !TextUtils.isEmpty(SPUtils.getUser().sid)) ? SPUtils.getUser().sid : "1b36bd68cfc44e50ba5535c9371e9bce";

}
