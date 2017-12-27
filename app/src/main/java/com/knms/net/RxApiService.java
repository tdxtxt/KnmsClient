package com.knms.net;


import com.knms.bean.AffectedNumber;
import com.knms.bean.DiyInsp;
import com.knms.bean.Exists;
import com.knms.bean.IdleClassify;
import com.knms.bean.IndexAd;
import com.knms.bean.IndexIdle;
import com.knms.bean.Recommend;
import com.knms.bean.ResponseBody;
import com.knms.bean.SaveParity;
import com.knms.bean.address.CreateAddress;
import com.knms.bean.address.ShippingAddres;
import com.knms.bean.basebody.StringBody;
import com.knms.bean.basebody.StringBody;
import com.knms.bean.classification.ClassifyDetail;
import com.knms.bean.customfurniture.CustomDetail;
import com.knms.bean.goodsdetails.GoodsDetails;
import com.knms.bean.idle.ReIdleClassify;
import com.knms.bean.im.KnmsMsg;
import com.knms.bean.myidle.MyIdle;
import com.knms.bean.myparity.MyParity;
import com.knms.bean.order.CommentReward;
import com.knms.bean.order.Complaints;
import com.knms.bean.order.ComplaintsType;
import com.knms.bean.order.Order;
import com.knms.bean.order.OrderDetail;
import com.knms.bean.order.OrderRetainageBody;
import com.knms.bean.order.OrderRetainageBody;
import com.knms.bean.order.OrderState;
import com.knms.bean.order.neworder.AppOrder;
import com.knms.bean.order.neworder.CreationOrderSuccessful;
import com.knms.bean.order.neworder.InitRefunds;
import com.knms.bean.order.neworder.OrderDetails;
import com.knms.bean.order.neworder.PreviewOrder;
import com.knms.bean.order.neworder.RefundReasons;
import com.knms.bean.order.neworder.RefundsDetails;
import com.knms.bean.order.neworder.RequestCreateOrder;
import com.knms.bean.other.City;
import com.knms.bean.other.Classify;
import com.knms.bean.other.CustomType;
import com.knms.bean.other.FastLabel;
import com.knms.bean.other.Label;
import com.knms.bean.other.Tab;
import com.knms.bean.pay.OrderPayBody;
import com.knms.bean.pay.PayTypeBody;
import com.knms.bean.product.BosCommodityBody;
import com.knms.bean.product.ClassifyGood;
import com.knms.bean.product.CollectionProduct;
import com.knms.bean.product.Desytyle;
import com.knms.bean.product.Furniture;
import com.knms.bean.product.Goods;
import com.knms.bean.product.Idle;
import com.knms.bean.remark.Comment;
import com.knms.bean.repair.MyRepair;
import com.knms.bean.repair.Repair;
import com.knms.bean.repair.RepairType;
import com.knms.bean.shop.Shop;
import com.knms.bean.shop.ShopCommodity;
import com.knms.bean.shoppingcart.ShoppingCartBody;
import com.knms.bean.sku.SkuBody;
import com.knms.bean.style.HomeFurnishing;
import com.knms.bean.style.StyleDetails;
import com.knms.bean.style.StyleId;
import com.knms.bean.user.User;
import com.knms.bean.user.UserType;
import com.knms.bean.welfareservice.CouponCenter;
import com.knms.bean.welfareservice.CouponList;
import com.knms.bean.welfareservice.PreferIndex;
import com.knms.bean.welfareservice.StoreCoupon;
import com.knms.core.upgrade.pojo.UpdateInfo;

import java.util.List;
import java.util.Map;

import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import rx.Observable;

/**
 * 类说明：
 *
 * @author 作者:tdx
 * @version 版本:1.0
 * @date 时间:2016年8月26日 上午10:59:07
 */
public interface RxApiService {

    @POST("api/homepage/select")
    Observable<ResponseBody<IndexIdle>> getIndexData();

    @POST("api/labelGoods/index")
    Observable<ResponseBody<List<Classify>>> getClassifys();

    //分类商品详情
    @FormUrlEncoded
    @POST("api/labelGoods/detail")
    Observable<ResponseBody<ClassifyDetail>> getGoodsDetail(@Field("goid") String goid);

    //活动商品详情
    @FormUrlEncoded
    @POST("api/activity/detail")
    Observable<ResponseBody<ClassifyDetail>> getActGoodsDetail(@Field("goid") String goid);

    @FormUrlEncoded
    @POST("api/advertisement/list")
    Observable<ResponseBody<IndexAd>> getAdvertisement(@Field("p") String type);

    @FormUrlEncoded
    @POST("api/customized/list")
    Observable<ResponseBody<List<Furniture>>> getFurnitures(@Field("typeId") String typeId, @Field("pageIndex") int pageNum);

    @FormUrlEncoded
    @POST("api/descorate/descorateList")
    Observable<ResponseBody<DiyInsp>> getDecorates(@Field("decorateType") int decorateType, @Field("typeId") String typeId, @Field("pageIndex") int pageNum);

    @FormUrlEncoded
    @POST("api/idle/classifyGoods")
    Observable<ResponseBody<IdleClassify>> getIdleClassify(@Field("pageIndex") int pageNum, @Field("goclassifyid") String goclassifyid);

    @FormUrlEncoded
    @POST("api/idle/recommendGoods")
    Observable<ResponseBody<List<Idle>>> getIdleRecommend(@Field("pageIndex") int pageNum);

    @FormUrlEncoded
    @POST("api/u/myCollectMall")
    Observable<ResponseBody<List<CollectionProduct>>> getCollectionBaby(@Field("pageIndex") int pageNum, @Field("state") int state, @Field("type") int type);

    @FormUrlEncoded
    @POST("api/u/myCollectMall")
    Observable<ResponseBody<List<Shop>>> getCollectionShop(@Field("pageIndex") int pageNum, @Field("state") int state, @Field("type") int type);

    @FormUrlEncoded
    @POST("api/u/myCollectMall")
    Observable<ResponseBody<List<Furniture>>> getCollectionInsp(@Field("pageIndex") int pageNum, @Field("state") int state, @Field("type") int type);

    @FormUrlEncoded
    @POST("api/idle/release")
    Observable<ResponseBody<String>> releaseIdle(@Field("goclassifyid") String classifyId, @Field("coremark") String coremark,
                                                 @Field("gooriginal") double orPrice, @Field("goprice") double price,
                                                 @Field("gofreeshopprice") double freeshopprice, @Field("gofreeshop") int isfreeshop,
                                                 @Field("goarea") String area, @Field("imgIds") String imgIds,
                                                 @Field("imgSeqs") String imgSeqs,
                                                 @Field("state") int state);

    @FormUrlEncoded
    @POST("api/u/myIdel/edit")
    Observable<ResponseBody<String>> editIdle(@Field("goid") String id,
                                              @Field("goclassifyid") String classifyId, @Field("coremark") String coremark,
                                              @Field("gooriginal") double orPrice, @Field("goprice") double price,
                                              @Field("gofreeshopprice") double freeshopprice, @Field("gofreeshop") int isfreeshop,
                                              @Field("goarea") String area, @Field("imgIds") String imgIds,
                                              @Field("imgSeqs") String imgSeqs);

    @POST("api/idle/acerCode")
    Observable<ResponseBody<List<City>>> getCitys();

    @POST("api/idle/idleType")
    Observable<ResponseBody<List<ReIdleClassify>>> getReIdleClassify();

    @POST("api/parity/hotLabel")
    Observable<ResponseBody<List<Label>>> getHotLabels();

    @POST("api/parity/allLabel")
    Observable<ResponseBody<List<Label>>> getAllLabels();

    @FormUrlEncoded
    @POST("api/parity/save")
    Observable<ResponseBody<String>> releaseBBPrice(@Field("coremark") String coremark,
                                                    @Field("lableId") String lableIds,
                                                    @Field("imgId") String imgIds,
                                                    @Field("state") int state);

    @FormUrlEncoded
    @POST("api/search/adaptLabels")
    Observable<ResponseBody<List<Label>>> searchProductMatch(@Field("str") String key);

    @FormUrlEncoded
    @POST("api/search/adaptTypes")
    Observable<ResponseBody<List<ReIdleClassify>>> searchIdelMatch(@Field("str") String key);

    @FormUrlEncoded
    @POST("api/search/goodsSearch")
//商品－搜索
    Observable<ResponseBody<List<ClassifyGood>>> searchProduct(@Field("str") String key, @Field("labelId") String labelId,
                                                               @Field("styleId") String styleId, @Field("brandId") String brandId,
                                                               @Field("isHot") int isHot,
                                                               @Field("pageIndex") int pageNum);

    @FormUrlEncoded
    @POST("api/search/goodsBrand")
//商品－品牌列表
    Observable<ResponseBody<List<Label>>> searchProductBrand(@Field("str") String key, @Field("labelId") String labelId);

    @FormUrlEncoded
    @POST("api/search/shopSearch")
//店铺-搜索
    Observable<ResponseBody<List<Shop>>> searchShop(@Field("str") String key, @Field("pageIndex") int pageNum);

    @FormUrlEncoded
    @POST("api/search/idleSearch")
//闲置-搜索
    Observable<ResponseBody<List<Idle>>> searchIdle(@Field("str") String key, @Field("typeId") String typeId, @Field("pageIndex") int pageNum);

    //收藏&取消收藏(收藏的状态costate:1商品、2店铺、3灵感（家装风格、百科）,收藏类型type:0、收藏 1、取消收藏)
    @FormUrlEncoded
    @POST("api/collect")
    Observable<ResponseBody> collect(@Field("cotypeid") String cotypeid, @Field("costate") int costate, @Field("type") int type);

    //家居百科详情
    @FormUrlEncoded
    @POST("api/descorate/decorationEncyclopediaOne")
    Observable<ResponseBody<HomeFurnishing>> getHomeFurnishing(@Field("decorateId") String decorateId);

    //清空浏览记录
    @FormUrlEncoded
    @POST("api/u/emptyBrowse")
    Observable<ResponseBody> cleanBrowsestate(@Field("state") int state);

    //我的优惠券
    @FormUrlEncoded
    @POST("api/coupon/mylist")
    Observable<ResponseBody<List<CouponCenter>>> getMyCouponsList(@Field("type") int type, @Field("state") int state);

    //领券中心
    @POST("api/prefer/centerList")
    Observable<ResponseBody<CouponList>> CouponsCenter();

    //领取优惠券
    @FormUrlEncoded
    @POST("api/prefer/receive")
    Observable<ResponseBody> getCoupons(@Field("spid") String spid);

    //码上用券
    @FormUrlEncoded
    @POST("api/prefer/shopList")
    Observable<ResponseBody<StoreCoupon>> getStoreCoupon(@Field("countNumber") String shopId);

    //消息中心
    @POST("api/infoCenter")
    Observable<ResponseBody<List<KnmsMsg>>> getMsgCenter();

    //铠恩买手官网信息
    @FormUrlEncoded()
    @POST("api/infoCenter/knms")
    Observable<ResponseBody<List<KnmsMsg>>> getknmsMsgs(@Field("pageIndex") int pageNum);

    //铠恩买手客服信息
    @FormUrlEncoded()
    @POST("api/infoCenter/mskf")
    Observable<ResponseBody<List<KnmsMsg>>> getknmsKefuMsgs(@Field("pageIndex") int pageNum);

    //刷新铠恩买手客服消息
    @POST("api/infoCenter/refreshMskf")
    Observable<ResponseBody<List<KnmsMsg>>> refreshKefuMsgs();

    @FormUrlEncoded()
    @POST("api/infoCenter/mskfCommit")
    Observable<ResponseBody<KnmsMsg>> sendKnmsMsg(@Field("content") String msg);

    //订单状态
    @FormUrlEncoded
    @POST("api/u/myorder/state")
    Observable<ResponseBody<OrderState>> getOrderState(@Field("orderId") String orderId);

    //关闭投诉
    @FormUrlEncoded
    @POST("api/u/complaint/close")
    Observable<ResponseBody> closeComplaints(@Field("orderId") String ocid);

    //闲置（上架，下架，删除）
    @FormUrlEncoded
    @POST("api/u/myIdel/updateState")
    Observable<ResponseBody> updateIdelState(@Field("goid") String goid, @Field("state") int state);

    //投诉类型
    @FormUrlEncoded
    @POST("api/u/complaint/ordertypes")
    Observable<ResponseBody<List<ComplaintsType>>> getComplaintTypes(@Field("type") int type);

    //闲置家具分类
    @POST("api/idle/classify")
    Observable<ResponseBody<List<IdleClassify>>> getIdleClassifys();

    //闲置家具分类列表
    @FormUrlEncoded
    @POST("api/idle/classifyGoods")
    Observable<ResponseBody<List<Idle>>> getIdleClassifyList(@Field("goclassifyid") String type, @Field("pageIndex") int pageIndex);

    //家装灵感分类
    @FormUrlEncoded
    @POST("api/descorate/types")
    Observable<ResponseBody<List<Tab>>> getInspirationTabs(@Field("type") int type);

    //先知家具分类列表
    @FormUrlEncoded
    @POST("api/descorate/list")
    Observable<ResponseBody<List<Desytyle>>> getInspiration(@Field("type") int type, @Field("typeId") String typeId, @Field("pageIndex") int pageIndex);


    //投诉
    @FormUrlEncoded
    @POST("api/u/complaint/commit")
    Observable<ResponseBody<String>> commonComplaint(@Field("orderId") String orderID, @Field("content") String content,
                                                     @Field("complaintType") String complaintType, @Field("relationName") String relationName,
                                                     @Field("relationMobile") String relationMobile, @Field("pictureIds") String pictureIds,
                                                     @Field("octype")int octype);

    //家具维修列表
    @FormUrlEncoded
    @POST("api/repair/masters")
    Observable<ResponseBody<List<Repair>>> getRepairList(@Field("pageIndex") int pageIndex);


    //维修类型
    @POST("api/repair/types")
    Observable<ResponseBody<List<RepairType>>> getRepairType();

    //发布维修
    @FormUrlEncoded
    @POST("api/repair/release")
    Observable<ResponseBody<String>> releaseRepair(@Field("remark") String remark, @Field("area") String area, @Field("type") String type,
                                                   @Field("imgIds") String imgIds,@Field("state") int state);

    //维修详情
    @FormUrlEncoded
    @POST("api/repair/detail")
    Observable<ResponseBody<MyRepair>> getRepairDetail(@Field("reid") String reid);

    //我的维修列表
    @FormUrlEncoded
    @POST("api/u/myRepair")
    Observable<ResponseBody<List<MyRepair>>> getMyRepairList(@Field("pageIndex") int pageIndex);

    @FormUrlEncoded
    @POST("api/u/myRepair/updateState")
    Observable<ResponseBody> updateMyRepairState(@Field("state") int state, @Field("reid") String reid);//	1：解决，2：删除

    @FormUrlEncoded
    @POST("api/descorate/nextPageId")
    Observable<ResponseBody<List<StyleId>>> getStyleIds(@Field("type") String type, @Field("typeId") String typeId, @Field("shopId") String shopId);//

    @GET("clientversion/{clientType}/{cliVerId}")
    Observable<ResponseBody<UpdateInfo>> clientupdate(@Path("clientType") String clientType, @Path("cliVerId") String cliVerId);//检测更新

    @FormUrlEncoded
    @POST("api/activity/list")
    Observable<ResponseBody<List<ClassifyGood>>> getMallListByClassfiy(@Field("pageIndex") int pageIndex,@Field("id") String id);

    @POST("api/customized/types")
    Observable<ResponseBody<List<CustomType>>> getCustomizedType();

    //定制家具详情
    @FormUrlEncoded
    @POST("api/customized/detail")
    Observable<ResponseBody<CustomDetail>> getCustomizedDetail(@Field("inid")String id);

    @POST("api/prefer/index")
    Observable<ResponseBody<List<PreferIndex>>> getPrefer();

    //使用优惠券
    @FormUrlEncoded
    @POST("api/prefer/use")
    Observable<ResponseBody> usePrefer(@Field("spid") String spid, @Field("countNumber") String countNumber);
    //登出
    @POST("api/u/logout")
    Observable<ResponseBody> logout();

    //闲置详情
    @FormUrlEncoded
    @POST("api/idle/idleDetail")
    Observable<ResponseBody<MyIdle>> idleDetails(@Field("goid") String id);

    //账号类型查询
    @FormUrlEncoded
    @POST("api/account/userType")
    Observable<ResponseBody<UserType>> userType(@Field("userId") String userId);

    //获取商家自动回复消息内容
    @FormUrlEncoded
    @POST("api/sellerShop/aotoMsg")
    Observable<ResponseBody<String>> getAutoMsg(@Field("ssmerchantid") String sid);

    //家装风格详情
    @FormUrlEncoded
    @POST("api/descorate/decorationStyleDetail")
    Observable<ResponseBody<StyleDetails>> getFurnitureStyleDeails(@Field("inid") String id);

    //我的比比货列表
    @FormUrlEncoded
    @POST("api/u/myParity")
    Observable<ResponseBody<List<MyParity>>> getParitys(@Field("pageIndex") int pageNum);

    //比比货详情
    @FormUrlEncoded
    @POST("api/parity/detail")
    Observable<ResponseBody<MyParity>> getParityDetail(@Field("id") String id);

    //个人信息
    @FormUrlEncoded
    @POST("api/u/personalinformation")
    Observable<ResponseBody<User>> getUserInfo(@Field("version") String version);

    //获取推荐商品
    @FormUrlEncoded
    @POST("api/goodsdetail/recommend")
    Observable<ResponseBody<List<ClassifyGood>>> getRecommend(@Field("goid") String id);

    //快速找货-商品列表
    @FormUrlEncoded
    @POST("api/fastfind/goodslist")
    Observable<ResponseBody<List<Goods>>> fastFindGoodsList(@Field("styles") String styles, @Field("classifys") String classifys, @Field("pageIndex") int pageIndex);

    //快速找货标签列表
    @POST("api/fastfind/labels")
    Observable<ResponseBody<FastLabel>> fastfindLabels();

    @FormUrlEncoded
    @POST("api/parity/exists")
    Observable<ResponseBody<Exists>> parityExists(@Field("goid") String goid);

    @FormUrlEncoded
    @POST("api/parity/saveinner")
    Observable<ResponseBody<List<SaveParity>>> saveParity(@Field("goid") String goid);

    @FormUrlEncoded
    @POST("api/u/myParity/refreshone")
    Observable<ResponseBody<String>> refreshMyParity(@Field("goid") String goid);

    //订单详情
    @FormUrlEncoded
    @POST("api/u/myorder/detail")
    Observable<ResponseBody<OrderDetail>> getOrderDetails(@Field("orderId") String orderId);

    //确认收货
    @FormUrlEncoded
    @POST("api/u/myorder/confirmGoods")
    Observable<ResponseBody<String>> confirmGoods(@Field("orderId")String orderId);

    //提交用户评价,score:评分（1到5颗星分别为 1.0、2.0....5.0;imgs图片id拼串，用逗号隔开
    @FormUrlEncoded
    @POST("api/u/myorder/commitComment")
    Observable<ResponseBody> commitComment(@Field("orderId")String orderId,@Field("content")String content,
                                           @Field("score")double score,@Field("imgs")String imgs);

    //提交用户评价,score:评分（1到5颗星分别为 1.0、2.0....5.0;imgs图片id拼串，用逗号隔开
    @FormUrlEncoded
    @POST("api/u/myorder/commitComment")
    Observable<ResponseBody> commitCommentByGoods(@Field("orderId")String orderId,@Field("goodsid") String gid,
                                                  @Field("parameterBriefing")String specDesc,@Field("content") String content,
                                                  @Field("score")double score,@Field("imgs")String imgs);

    //对商品-总评价条数
    @FormUrlEncoded
    @POST("api/labelGoods/commentGoodsIdCount")
    Observable<ResponseBody<Integer>> getProductCommentSizeByGoid(@Field("goid") String goid);

    //对商品-用户评价列表
    @FormUrlEncoded
    @POST("api/labelGoods/commentGoodsIdList")
    Observable<ResponseBody<List<Comment>>> getProductCommentsByGoid(@Field("goid") String goid, @Field("pageIndex") int pageNum);

    //对店铺-用户评价列表
    @FormUrlEncoded
    @POST("api/labelGoods/commentList")
    Observable<ResponseBody<List<Comment>>> getShopCommentsByGoid(@Field("goid") String goid, @Field("pageIndex") int pageNum);

    //对店铺-用户评价列表
    @FormUrlEncoded
    @POST("api/sellerShop/commentList")
    Observable<ResponseBody<List<Comment>>> getCommentsByShopId(@Field("shopId") String shopId, @Field("pageIndex") int pageNum);

    //对订单-用户评价列表
    @FormUrlEncoded
    @POST("api/sellerShop/orderCommentList")
    Observable<ResponseBody<List<Comment>>> getCommentsByOrder(@Field("orderId") String orderId);

    //订单-用户评价详情
    @FormUrlEncoded
    @POST("api/u/myorder/commentDetail")
    Observable<ResponseBody<Comment>> getCommentByOrder(@Field("orderId") String orderId);

    //商品-用户评价详情
    @FormUrlEncoded
    @POST("api/labelGoods/commentDetail")
    Observable<ResponseBody<Comment>> getCommentByProduct(@Field("commentId") String commentId);

    //评论点赞
    @FormUrlEncoded
    @POST("api/labelGoods/agreeComment")
    Observable<ResponseBody> praiseToComment(@Field("commentId") String commentId);

    //是否显示评价有礼
    @POST("api/u/myorder/commentReward")
    Observable<ResponseBody<CommentReward>> commentReward();

    //订单投诉列表
    @FormUrlEncoded
    @POST("api/u/myorder/complaints")
    Observable<ResponseBody<List<Complaints>>> getComplaintsList(@Field("orderId") String orderId);

    //发送短信验证码(校验)
    @FormUrlEncoded
    @POST("api/msg/sendMsgCheck")
    Observable<ResponseBody> sendMsgCheck(@Field("username") String username,@Field("sendType") String sendType,@Field("checkUsername") String checkUsername);

    //爆款活动分类
    @POST("api/labelGoods/ActivityGoodsClass")
    Observable<ResponseBody<List<Tab>>> getMallClassify();
    //闲置热门标签
    @POST("api/idle/hotIdeltype")
    Observable<ResponseBody<List<Label>>> getIdleHotLabels();

    //店铺热门标签
    @POST("api/sellerShop/hotShopList")
    Observable<ResponseBody<List<Label>>> getShopHotLabels();

    //获取店铺下的商品列表
    @FormUrlEncoded
    @POST("api/sellerShop/mallGoodsList")
    Observable<ResponseBody<List<ShopCommodity>>> getShopGoodsList(@Field("shopId") String shopId ,@Field("pageIndex") int pageIndex );

    //商城支付订单
    @FormUrlEncoded
    @POST("api/u/myorder")
    Observable<ResponseBody<List<Order>>> getMarketOrderList(@Field("pageIndex") int pageIndex);

    /******************************************************************************************************************************
     **********************************************************购物相关api**********************************************************
     **********************************************************购物相关api**********************************************************
     ******************************************************************************************************************************/
    //爆款商品-分类列表
    @POST("mall/goods/commodityShow_list")
    Observable<ResponseBody<BosCommodityBody>> getCommoditysByClassifyId(@Body Map<String,Object> params);
    //库存商品属性
    @POST("mall/goods/getAttribute")
    Observable<ResponseBody<SkuBody>> getSkuProduct(@Body Map<String,Object> params);

    //购物车列表商品
    @POST("mall/shopcart/shoppingCart_list")
    Observable<ResponseBody<ShoppingCartBody>> getShoppingCartProducts(@Body Map<String, Object> params);

    //商品数量设置
    @POST("mall/shopcart/shoppingCart_set_planNumber")
    Observable<ResponseBody<ShoppingCartBody>> setShopCartSkuNumber(@Body Map<String,Object> params);

    //商品规格设置
    @POST("mall/shopcart/shoppingCart_set_type")
    Observable<ResponseBody<ShoppingCartBody>> setShopCartSkuSpec(@Body Map<String,Object> params);

    //删除购物车商品
    @POST("mall/shopcart/shoppingCart_del")
    Observable<ResponseBody> removeShopCartSku(@Body Map<String,Object> params);

    //添加商品到购物车
    @POST("mall/shopcart/specificationAddShoppingCart")
    Observable<ResponseBody<AffectedNumber>> addShoppingCart(@Body Map<String,Object> params);

    //获取购物车商品数量
    @POST("mall/shopcart/shoppingCart_goods_count")
    Observable<ResponseBody<AffectedNumber>> getShoppingCartCount(@Body Map<String,Object> params);


    //收货地址列表
    @POST("mall/address/shippingAddress_list")
    Observable<ResponseBody<ShippingAddres>> getShoppingAddressList(@Body Map<String,Object> userIdMap);

    //新增收货信息
    @POST("mall/address/shippingAddress_create")
    Observable<ResponseBody<CreateAddress>> createShippingAddress(@Body Map<String,Object> addresInfoMap);

    //刪除收貨信息
    @POST("mall/address/shippingAddress_delete")
    Observable<ResponseBody<AffectedNumber>> deleteShippingAddress(@Body Map<String,Object> deleteMap);

    //编辑收货信息
    @POST("mall/address/shippingAddress_update")
    Observable<ResponseBody<CreateAddress>> updateShippingAddress(@Body Map<String,Object> addresInfoMap);

    //设置默认地址
    @POST("mall/address/shippingAddress_updateDefault")
    Observable<ResponseBody<ShippingAddres.orderMailingAddressBos>> updateDefaultShippingAddress(@Body Map<String,Object> paramMap);

    //商品详情（可购买商品）
    @POST("mall/goods/commodityShow_one")
    Observable<ResponseBody<GoodsDetails>> getCommodityDetails(@Body Map<String,Object> showId);

    //获取默认的收货地址
    @POST("mall/address/shippingAddress_default")
    Observable<ResponseBody<CreateAddress>> getDefaultAddress(@Body Map<String,Object> userId);

    //预览订单
    @POST("mall/order/orderTrading_check")
    Observable<ResponseBody<PreviewOrder>> previewOrder(@Body Map<String,Object> parameters);

    //创建订单
    @POST("mall/order/userOrderTrading_create")
    Observable<ResponseBody<CreationOrderSuccessful>> createOrder(@Body RequestCreateOrder orderMap);

    //订单列表
    @POST("mall/order/orderTrading_user_list")
    Observable<ResponseBody<AppOrder>> getOrderList(@Body Map<String,Object> map);

    //订单详情
    @POST("mall/order/orderTrading_one")
    Observable<ResponseBody<OrderDetails>> getTradingDetails(@Body Map<String,Object> map);

    //取消订单
    @POST("mall/order/orderTrading_user_cancel")
    Observable<ResponseBody<OrderDetails>> cancelTrading(@Body Map<String,Object> map);

    //确认收货
    @POST("mall/order/orderTrading_user_confirmreceipt")
    Observable<ResponseBody<OrderDetails>> confirmReceipt(@Body Map<String,Object> map);

    //删除订单
    @POST("mall/order/orderTrading_user_delete")
    Observable<ResponseBody<AffectedNumber>> deleteTrading(@Body Map<String,Object> map);

    //退款初始化
    @POST("mall/order/orderTrading_user_refunds_init")
    Observable<ResponseBody<InitRefunds>> initRefunds(@Body Map<String,Object> map);

    //申请退款
    @POST("mall/order/orderTrading_user_refunds")
    Observable<ResponseBody<AffectedNumber>> refunds(@Body Map<String,Object> map);

    //退款详情
    @POST("mall/order/orderTrading_user_refunds_list")
    Observable<ResponseBody<RefundsDetails>> refundsDetails(@Body Map<String,Object> map);

    //退货原因
    @POST("mall/order/refund_reasons")
    Observable<ResponseBody<RefundReasons>> getRefundsReasons();

    //获取支付方式
    @POST("mall/pay/paymethod_list")
    Observable<ResponseBody<PayTypeBody>> getPayMethod(@Body Map<String,Object> params);

    //生成支付参数
    @POST("mall/pay/orderpay_create")
    Observable<ResponseBody<OrderPayBody>> createOrderPay(@Body Map<String,Object> params);

    //检查支付成功否
    @POST("mall/pay/orderpay_payment_client_success")
    Observable<ResponseBody> checkPaySccess(@Body Map<String,Object> params);


    //尾款 生成订单事件id
    @POST("mall/order/systemEventId_create")
    Observable<ResponseBody<StringBody>> getSystemEventId();

    //尾款确认创建订单2合1
    @POST("mall/order/orderRetainagePay_Merge")
    Observable<ResponseBody<OrderRetainageBody>> retainageOrderConfirmAndCreate(@Body Map<String,Object> params);

    //获取为你推荐列表（新）
    @POST("mall/goods/commodityShow_list_recommend")
    Observable<ResponseBody<Recommend>> getRecommendList(@Body Map<String,Object> params);
}
