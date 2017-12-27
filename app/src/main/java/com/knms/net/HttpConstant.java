package com.knms.net;

import com.knms.android.BuildConfig;

/**
 * 类说明：
 *
 * @author 作者:tdx
 * @version 版本:1.0
 *
 *
 * @date 时间:2016年8月23日 上午8:59:07
 *
 */
public class HttpConstant {
    public static String HOST_UPDATE = BuildConfig.HOST_UPDATE;
    public static int SOCKET_TIMEOUT = BuildConfig.SOCKET_TIMEOUT;//毫秒
    public static String HOST = BuildConfig.HOST;
    public static String HOSTPAY = BuildConfig.HOST_RED;
    public static String SRC = BuildConfig.SRC;


    /********
     * 通用api
     **********/
    public static String clientConfig = HOST + "clientconfig";//启动加载相关配置
    public static String clientVersion = HOST + "clientversion";//升级与更新
    public static String imageUpload = HOST + "api/basis/imageupload";//图片上传
    public static String imageDelete = HOST + "api/basis/imagedelete";//图片删除
    public static String collect = HOST + "api/collect";//添加收藏、取消收藏
    /*********
     * 用户api
     *********/
    public static String sendMsg = HOST + "api/msg/sendMsg";//发送验证码
    public static String verifyMsg = HOST + "api/account/msgLogin";//验证短信接口
    public static String msgLogin = HOST + "api/account/msgLogin";//短信登录
    public static String passLogin = HOST + "api/account/passLogin";//账户登录
    public static String register = HOST + "api/account/register";//注册
    public static String updatePassword = HOST + "api/account/updatePassword";//找回密码
    public static String updateMobile = HOST + "api/u/updateMobile";//绑定新手机
    /*********
     * 首页api
     *********/
    public static String indexSelect = HOST + "api/homepage/select";//商品
    public static String indexAd = HOST + "api/advertisement/list";//广告
    /*********
     * 分类商品api
     *********/
    public static String classificationIndex = HOST + "api/labelGoods/index";//分类商品首页
    public static String styles = HOST + "api/labelGoods/styles";//风格列表
    public static String brands = HOST + "api/labelGoods/brands";//品牌列表
    public static String productList = HOST + "api/labelGoods/list";//分类模块的商品列表
    public static String labelGoodsDetail = HOST + "api/labelGoods/detail";//商品详情
    /**********
     * 个人******我的闲置api
     ********/
    public static String myIdel = HOST + "api/u/myIdel";//列表
    public static String updateState = HOST + "api/u/myIdel/updateState";//上架、下架、删除
    public static String edit = HOST + "api/myIdel/edit";//编辑
    /*********
     * 个人*******个人中心api
     ********/
    public static String uiconupdate = HOST + "api/u/uiconupdate";//头像修改
    public static String nicknameupdate = HOST + "api/u/nicknameupdate";//昵称修改
    public static String logout = HOST + "api/u/logout";//退出登录
    public static String feedback = HOST + "api/feedback";//意见反馈
    public static String myorder = HOST + "api/u/myorder";//我的订单
    public static String orderDetails = HOST + "api/u/myorder/complaints";//订单详情
    public static String complaints = HOST + "api/u/complaint/commit";//投诉
    public static String closeComplaints = HOST + "api/u/closeComplaints";//关闭投诉
    public static String complaintsData = HOST + "api/u/myorder/complaintDetail";//投诉详情
    /*********
     * 个人*******我的比比价api
     ********/
    public static String myParity = HOST + "api/u/myParity";//列表
    public static String myParityUpdateState = HOST + "api/u/myParity/updateState";//下架、删除
    public static String myParityDetail = HOST + "api/u/myParity/detail";//详情
    /**********
     * 定制家具
     **********/
    public static String customizedType = HOST + "api/customized/types";//定制类型
    public static String customizedList = HOST + "api/customized/list";//定制列表
    public static String customizedDetail = HOST + "/api/customized/detail";//定制详情
    /**********
     * 家装灵感
     **********/
    public static String descorateList = HOST + "api/descorate/descorateList";//家装灵感 (家装风格/家具百科)分类列表
    public static String decorationStyleDetail = HOST + "api/descorate/decorationStyleDetail";//家装风格详情
    /**********
     * 店铺api
     **********/
    public static String shopDetail = HOST + "api/sellerShop/shopDetail";//店铺信息
    public static String goodsList = HOST + "api/sellerShop/goodsList";//店铺商品列表
    public static String decorationStyleList = HOST + "api/sellerShop/decorationStyleList";//家装风格列表
    public static String customList = HOST + "api/sellerShop/customizedList";//定制家具列表
    /**********
     * 闲置家具api
     **********/
    public static String idleDetail = HOST + "api/idle/idleDetail";//闲置家具详情
    /**********
     * 商城活动api
     **********/
    public static String activityDetail = HOST + "api/activity/detail";//商品详情
    public static String mallActiviy = HOST + "api/activity/list";//商城活动列表

}
