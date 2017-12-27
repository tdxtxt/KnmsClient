package com.knms.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.CharacterStyle;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.knms.activity.CommWebViewActivity;
import com.knms.activity.CustomFurnitureDetailsActivity;
import com.knms.activity.IdleDetailsActivity;
import com.knms.activity.KnowledgeDetailsActivity;
import com.knms.activity.MinePriceRationDetailsActivity;
import com.knms.activity.ShopActivityF;
import com.knms.activity.UndercarriageDetailsActivity;
import com.knms.activity.base.BaseActivity;
import com.knms.activity.base.BaseFragmentActivity;
import com.knms.activity.comment.CommentInfoActivity;
import com.knms.activity.coupons.MyCouponsListActivity;
import com.knms.activity.details.DecorationStyleDetailsActivity;
import com.knms.activity.details.OrderDetailsActivity;
import com.knms.activity.details.ProductDetailsBaokActivity;
import com.knms.activity.details.ProductDetailsOrdinaryActivity;
import com.knms.activity.details.canbuy.ProductDetailsActivity;
import com.knms.activity.goods.CustomFurnitureActivityF;
import com.knms.activity.goods.DiyInspirationActivity;
import com.knms.activity.goods.MallActivity;
import com.knms.activity.goods.ProductMainActivity;
import com.knms.activity.im.KnmsChatActivity;
import com.knms.activity.login.FasterLoginActivity;
import com.knms.activity.main.MainActivity;
import com.knms.activity.mall.order.AppPayOrderDetailsActivity;
import com.knms.activity.mine.MineRepairDetailActivity;
import com.knms.activity.mine.MyOrderActivity;
import com.knms.android.R;
import com.knms.app.KnmsApp;
import com.knms.core.im.IMHelper;
import com.knms.core.im.msg.Product;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.net.HttpConstant;
import com.knms.view.emoji.MoonUtil;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by tdx on 2016/8/26.
 */
public class CommonUtils {
    public static int REQUEST_CODE_GETIMAGE_BYSDCARD = 0x122222;
    public static void logout(){
        IMHelper.getInstance().logout();
        SPUtils.clear();
        KnmsApp.getInstance().getUnreadObservable().sendData();
        RxBus.get().post(BusAction.ACTION_LOGOUT,"logout");
        JPushInterface.setAliasAndTags(KnmsApp.getInstance(), "", null,null);//退出账号之后不能接受别名推送了
    }
    public static boolean verifyNetwork() {
        ConnectivityManager connectivityManager = (ConnectivityManager) KnmsApp.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null) {
            if (activeNetInfo.isConnected()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean hasExtentsion(String filename) {
        int dot = filename.lastIndexOf('.');
        if ((dot > -1) && (dot < (filename.length() - 1))) {
            return true;
        } else {
            return false;
        }
    }
    /**
     * 将cookie同步到WebView
     * @param url WebView要加载的url
     * @return true 同步cookie成功，false同步cookie失败
     * @Author JPH
     */
    public static boolean syncCookie(String url) {
//        Uri uri = new Uri.Builder().build().parse(url);
        if(TextUtils.isEmpty(url)) return false;
        if(!(url.contains("kebuyer.com") || url.contains(HttpConstant.HOST))) return false;

        String knmsid = (String) SPUtils.getFromApp(SPUtils.KeyConstant.knmsid, "");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.createInstance(KnmsApp.getInstance());
        }
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        String cookie = cookieManager.getCookie(url);
        if(!(!TextUtils.isEmpty(cookie) && cookie.equals(knmsid))){
            cookieManager.removeSessionCookie();
            cookieManager.setCookie(url, knmsid);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().sync();
        } else {
            CookieManager.getInstance().flush();
        }
        String newCookie = cookieManager.getCookie(url);
        return TextUtils.isEmpty(newCookie) ? false : true;
    }

    /**
     * push推送跳转规则
     * json样式
     * {"module":"home","parameter":{"param1":"value1"}}
     */
    public static void startActivity(Context activity, String json) {
        if (TextUtils.isEmpty(json)) return;
        Map<String, Object> map = jsonToMap(json);
        if (map.get("module") == null) return;
        String module = map.get("module").toString();
        Map<String, Object> parameter = null;
        Intent intent = new Intent();
        if(!(activity instanceof Activity))
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if(map.get("parameter") != null && map.get("parameter") instanceof Map){
            parameter = (Map<String, Object>) map.get("parameter");
        } else {
            parameter = new HashMap<>();
        }
        if ("home".equals(module)) {//首页
            intent.setClass(activity,MainActivity.class);
            intent.putExtra("source","mHomePage");
            activity.startActivity(intent);
        } else if ("styleone".equals(module)) {//家装风格-详情
            String decorateId = null != parameter.get("decorateid") ? parameter.get("decorateid").toString() : "";
            String type = null != parameter.get("type") ? parameter.get("type").toString() : "";//目前没有这个参数的
            intent.setClass(activity, DecorationStyleDetailsActivity.class);
            intent.putExtra("ids", (Serializable) Arrays.asList(decorateId));
            intent.putExtra("position", 0);
            intent.putExtra("type", type);
            activity.startActivity(intent);
        }else if ("stylelist".equals(module)) {//家装风格-列表
            String typeId = null != parameter.get("typeid") ? parameter.get("typeid").toString() : "";
            intent.setClass(activity, DiyInspirationActivity.class);
            intent.putExtra("decorateType", DiyInspirationActivity.DecorateStyle);
            intent.putExtra("typeId", typeId);
            activity.startActivity(intent);
        }else if ("homeenone".equals(module)) {//家居百科-详情
            String decorateId = null != parameter.get("decorateid") ? parameter.get("decorateid").toString() : "";
            intent.setClass(activity, KnowledgeDetailsActivity.class);
            intent.putExtra("id", decorateId);
//            intent.put("pic", item.imageUrl); 图片
            activity.startActivity(intent);
        }else if ("homeenclist".equals(module)) {//家居百科-列表
            String typeId = null != parameter.get("typeid") ? parameter.get("typeid").toString() : "";
            intent.setClass(activity, DiyInspirationActivity.class);
            intent.putExtra("decorateType", DiyInspirationActivity.DecorateKnowledge);
            intent.putExtra("typeId", typeId);
            activity.startActivity(intent);
        } else if ("labelgoodsone".equals(module)) {//商品-详情（分类里面的）
            String goId = null != parameter.get("goid") ? parameter.get("goid").toString() : "";
            intent.setClass(activity, ProductDetailsOrdinaryActivity.class);
            intent.putExtra("id", goId);
            activity.startActivity(intent);
        } else if ("labelgoodslist".equals(module)) {//商品-列表（分类里面的）
            String classificationId = null != parameter.get("classificationid") ? parameter.get("classificationid").toString() : "";
            String styleId = null != parameter.get("styleid") ? parameter.get("styleid").toString() : "";
            String brandId = null != parameter.get("brandid") ? parameter.get("brandid").toString() : "";
            String title = null != parameter.get("title") ? parameter.get("title").toString() : "";
            intent.setClass(activity, ProductMainActivity.class);
            intent.putExtra("id", classificationId);
            intent.putExtra("styleId",styleId);
            intent.putExtra("brandId",brandId);
            intent.putExtra("title",title);
            activity.startActivity(intent);
        } else if ("sellershopone".equals(module)) {//商家-详情
            String shopId = null != parameter.get("shopid") ? parameter.get("shopid").toString() : "";
            intent.setClass(activity, ShopActivityF.class);
            intent.putExtra("shopId", shopId);
            activity.startActivity(intent);
        } else if ("idleone".equals(module)) {//闲置-详情
            String goId = null != parameter.get("goid") ? parameter.get("goid").toString() : "";
            intent.setClass(activity, IdleDetailsActivity.class);
            intent.putExtra("id", goId);
            activity.startActivity(intent);
        } else if ("customizedlist".equals(module)) {//定制家居-列表
            String typeId = null != parameter.get("typeid") ? parameter.get("typeid").toString() : "";
            intent.setClass(activity, CustomFurnitureActivityF.class);
            intent.putExtra("typeId", typeId);
            activity.startActivity(intent);
        } else if ("customizedone".equals(module)) {//定制家居-详情
            String inId = null != parameter.get("inid") ? parameter.get("inid").toString() : "";
            intent.setClass(activity, CustomFurnitureDetailsActivity.class);
            intent.putExtra("id", inId);
            activity.startActivity(intent);
        } else if("newsknmsone".equals(module)){//凯恩买手详情
            intent.setClass(activity, KnmsChatActivity.class);
            activity.startActivity(intent);
        } else if("orderscomplaintsone".equals(module)){//订单-投诉-详情
            String ocid = null != parameter.get("ocid") ? parameter.get("ocid").toString() : "";
            intent.putExtra("complaintsId", ocid);
            intent.setClass(activity, MyOrderActivity.class);
            activity.startActivity(intent);
        } else if("html".equals(module)){//跳转webview
            String url = parameter.get("url").toString();
            intent.putExtra("url",url);
            intent.setClass(activity, CommWebViewActivity.class);
            activity.startActivity(intent);
        } else if("activityone".equals(module)){//买手活动-详情
            String goid = parameter.get("goid").toString();
            intent.putExtra("id",goid);
            intent.setClass(activity, ProductDetailsBaokActivity.class);
            activity.startActivity(intent);
        } else if("activitylist".equals(module)){//买手活动-列表
            intent.setClass(activity, MallActivity.class);
            activity.startActivity(intent);
        } else if("myorderstate".equals(module)){//我的订单-订单状态-(选项卡左边的订单状态)
            if(KnmsApp.getInstance().isCurrentActLive(OrderDetailsActivity.class)) {
                KnmsApp.getInstance().finishActivity(OrderDetailsActivity.class);
            }
            String orderId = parameter.get("orderId").toString();
            intent.putExtra("orderid",orderId);
            intent.setClass(activity, OrderDetailsActivity.class);
            activity.startActivity(intent);
        } else if("myordercommentdetail".equals(module)){//订单评论详情
            if(KnmsApp.getInstance().isCurrentActLive(CommentInfoActivity.class)) {
                KnmsApp.getInstance().finishActivity(CommentInfoActivity.class);
            }
            String orderId = parameter.get("orderId").toString();
            intent.putExtra("orderId",orderId);
            intent.setClass(activity, CommentInfoActivity.class);
            activity.startActivity(intent);
        } else if("labelgoodscommentdetail".equals(module)){//商品评论详情
            if(KnmsApp.getInstance().isCurrentActLive(CommentInfoActivity.class)) {
                KnmsApp.getInstance().finishActivity(CommentInfoActivity.class);
            }
            String commentId = parameter.get("commentId").toString();
            intent.putExtra("commentId",commentId);
            intent.setClass(activity, CommentInfoActivity.class);
            activity.startActivity(intent);
        } else if("prefer".equals(module)){//我的优惠券-列表
            if(KnmsApp.getInstance().isCurrentActLive(MyCouponsListActivity.class)) {
                KnmsApp.getInstance().finishActivity(MyCouponsListActivity.class);
            }
            String type = parameter.get("type").toString();
            intent.setClass(activity, MyCouponsListActivity.class);
            if("1".equals(type)){//凯恩现金券
                intent.putExtra("type",0);
            }else if("2".equals(type)){//合作优惠券
                intent.putExtra("type",1);
            }
            activity.startActivity(intent);
        } else if("myorder".equals(module)) {//我的订单-列表
            if(KnmsApp.getInstance().isCurrentActLive(MyOrderActivity.class)) {
                KnmsApp.getInstance().finishActivity(MyOrderActivity.class);
            }
            intent.setClass(activity,MyOrderActivity.class);
            activity.startActivity(intent);
        }else if("commodityshowone".equals(module)){//(可购买)商城商品-详情
            if(KnmsApp.getInstance().isCurrentActLive(ProductDetailsActivity.class)) {
                KnmsApp.getInstance().finishActivity(ProductDetailsActivity.class);
            }
            String goid = parameter.get("showid").toString();
            intent.setClass(activity, ProductDetailsActivity.class);
            intent.putExtra("id",goid);
            activity.startActivity(intent);
        }else if("commodityshowlist".equals(module)){//(可购买)商城商品列表
            String typeId = parameter.get("id").toString();//分类id
            intent.putExtra("typeId",typeId);
            intent.setClass(activity, MallActivity.class);
            activity.startActivity(intent);
        }else if("mallordersone".equals(module)){//商城订单-详情
            if(KnmsApp.getInstance().isCurrentActLive(AppPayOrderDetailsActivity.class)) {
                KnmsApp.getInstance().finishActivity(AppPayOrderDetailsActivity.class);
            }
            String orderId = parameter.get("tradingid").toString();
            intent.putExtra("orderId",orderId);
            intent.setClass(activity, AppPayOrderDetailsActivity.class);
            activity.startActivity(intent);
        } else if("mallorderslist".equals(module)){//商城订单-列表
            if(KnmsApp.getInstance().isCurrentActLive(com.knms.activity.mall.order.MyOrderActivity.class)) {
                KnmsApp.getInstance().finishActivity(com.knms.activity.mall.order.MyOrderActivity.class);
            }
            intent.putExtra("position",0);
            intent.setClass(activity, com.knms.activity.mall.order.MyOrderActivity.class);
            activity.startActivity(intent);
        }else{
            intent.setClass(activity,MainActivity.class);
            intent.putExtra("source","mHomePage");
            activity.startActivity(intent);
        }
    }
    /**
     * h5跳转规则
     * url样式com.kebuyer.user://h5.kebuyer.com/a/?module={module}&parameter={parameter}
     */
    public static void startActivity(Context activity, Uri uri) {
        String module = uri.getQueryParameter("module");
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if ("home".equals(module)) {//首页
            intent.setClass(activity,MainActivity.class);
            intent.putExtra("source","mHomePage");
            activity.startActivity(intent);
        } else if("login".equals(module)){
            activity.startActivity(new Intent(activity, FasterLoginActivity.class));
        } else if ("styleone".equals(module)) {//家装风格-详情
            String decorateId = uri.getQueryParameter("decorateid");
            String type = uri.getQueryParameter("type");//目前没有这个参数的
            intent.setClass(activity, DecorationStyleDetailsActivity.class);
            intent.putExtra("ids", (Serializable) Arrays.asList(decorateId));
            intent.putExtra("position", 0);
            intent.putExtra("type", type);
            Log.i("testil","decorateId:" +decorateId + ";type:" + type);
            activity.startActivity(intent);
        } else if ("stylelist".equals(module)) {//家装风格-列表
            String typeId = uri.getQueryParameter("typeid");
            intent.setClass(activity, DiyInspirationActivity.class);
            intent.putExtra("decorateType", DiyInspirationActivity.DecorateStyle);
            intent.putExtra("typeId", typeId);
            activity.startActivity(intent);
        } else if ("homeenone".equals(module)) {//家居百科-详情
            String decorateId = uri.getQueryParameter("decorateid");
            intent.setClass(activity, KnowledgeDetailsActivity.class);
            intent.putExtra("id", decorateId);
//            intent.put("pic", item.imageUrl); 图片
            activity.startActivity(intent);
        } else if ("homeenclist".equals(module)) {//家居百科-列表
            String typeId = uri.getQueryParameter("typeid");
            intent.setClass(activity, DiyInspirationActivity.class);
            intent.putExtra("decorateType", DiyInspirationActivity.DecorateKnowledge);
            intent.putExtra("typeId", typeId);
            activity.startActivity(intent);
        } else if ("labelgoodsone".equals(module)) {//商品-详情（分类里面的）
            String goId = uri.getQueryParameter("goid");
            intent.setClass(activity, ProductDetailsOrdinaryActivity.class);
            intent.putExtra("id", goId);
            activity.startActivity(intent);
        } else if ("labelgoodslist".equals(module)) {//商品-列表（分类里面的）
            String classificationId = uri.getQueryParameter("classificationid");
            String styleId = uri.getQueryParameter("styleid");
            String brandId = uri.getQueryParameter("brandid");
            String title = uri.getQueryParameter("title");
            intent.setClass(activity, ProductMainActivity.class);
            intent.putExtra("id", classificationId);
            intent.putExtra("styleId",styleId);
            intent.putExtra("brandId",brandId);
            intent.putExtra("title",title);
            activity.startActivity(intent);
        } else if ("sellershopone".equals(module)) {//商家-详情
            String shopId = uri.getQueryParameter("shopid");
            intent.setClass(activity, ShopActivityF.class);
            intent.putExtra("shopId", shopId);
            activity.startActivity(intent);
        } else if ("idleone".equals(module)) {//闲置-详情
            String goId = uri.getQueryParameter("goid");
            intent.setClass(activity, IdleDetailsActivity.class);
            intent.putExtra("id", goId);
            activity.startActivity(intent);
        } else if ("customizedlist".equals(module)) {//定制家居-列表
            String typeId = uri.getQueryParameter("typeid");
            intent.setClass(activity, CustomFurnitureActivityF.class);
            intent.putExtra("typeId", typeId);
            activity.startActivity(intent);
        } else if ("customizedone".equals(module)) {//定制家居-详情
            String inId = uri.getQueryParameter("inid");
            intent.setClass(activity, CustomFurnitureDetailsActivity.class);
            intent.putExtra("id", inId);
            activity.startActivity(intent);
        }  else if("activityone".equals(module)){//买手活动-详情
            String goid = uri.getQueryParameter("goid");
            intent.putExtra("id",goid);
            intent.setClass(activity, ProductDetailsBaokActivity.class);
            activity.startActivity(intent);
        } else if("activitylist".equals(module)){//买手活动-列表
            intent.setClass(activity, MallActivity.class);
            activity.startActivity(intent);
        }else if("newsknmsone".equals(module)){//凯恩买手详情
            intent.setClass(activity, KnmsChatActivity.class);
            activity.startActivity(intent);
        }else if("html".equals(module)){//跳转webview
            String url = uri.getQueryParameter("url");
            intent.putExtra("url",url);
            intent.setClass(activity, CommWebViewActivity.class);
            activity.startActivity(intent);
        } else if("myorderstate".equals(module)){//我的订单-订单状态-(选项卡左边的订单状态)
            String orderId = uri.getQueryParameter("orderId");
            intent.putExtra("orderid",orderId);
            intent.setClass(activity, OrderDetailsActivity.class);
            activity.startActivity(intent);
        }  else if("prefer".equals(module)){//我的优惠券-列表
            if(!SPUtils.isLogin()){
                intent.setClass(activity, FasterLoginActivity.class);
                activity.startActivity(intent);
                return;
            }
            String type = uri.getQueryParameter("type");
            intent.setClass(activity, MyCouponsListActivity.class);
            if("1".equals(type)){//凯恩现金券
                intent.putExtra("type",0);
            }else if("2".equals(type)){//合作优惠券
                intent.putExtra("type",1);
            }
            activity.startActivity(intent);
        } else if("myorder".equals(module)) {//我的订单-列表
            MobclickAgent.onEvent(activity,"order_clickPush");
            if(!SPUtils.isLogin()){
                intent.setClass(activity, FasterLoginActivity.class);
                activity.startActivity(intent);
                return;
            }
            intent.putExtra("position",1);
            intent.setClass(activity, com.knms.activity.mall.order.MyOrderActivity.class);
            activity.startActivity(intent);
        }else if("commodityshowone".equals(module)){//(可购买)商城商品-详情
            String goid = uri.getQueryParameter("showid");
            intent.setClass(activity, ProductDetailsActivity.class);
            intent.putExtra("id",goid);
            activity.startActivity(intent);
        }else if("commodityshowlist".equals(module)){//(可购买)商城商品列表
            String typeId = uri.getQueryParameter("id");//分类id
            intent.putExtra("typeId",typeId);
            intent.setClass(activity, MallActivity.class);
            activity.startActivity(intent);
        }else{
            intent.setClass(activity,MainActivity.class);
            intent.putExtra("source","mHomePage");
            activity.startActivity(intent);
        }
    }

    /**
     * 聊天详情链接跳转定义
     */
    public static void startActivity(Context context,Product product){
        if(product == null) return;
        String productType = product.productType;
        Intent intent = new Intent();
        if("1".equals(productType)){//家装风格详情
            String decorateId = product.productId;
//            Map<String,Object> map = jsonToMap(product.attachJson);
//            String shopId = (String) map.get("shopId");
//            String type = (String) map.get("id");
            intent.setClass(context, DecorationStyleDetailsActivity.class);
            intent.putExtra("ids", (Serializable) Arrays.asList(decorateId));
            intent.putExtra("position", 0);
//            intent.putExtra("type", type);
            context.startActivity(intent);
        }else if("2".equals(productType)){//闲置详情
            String goId = product.productId;
            intent.putExtra("id", goId);
            if(IMHelper.getInstance().getAccount().equals(product.userId)){//自己的闲置
                intent.setClass(context, UndercarriageDetailsActivity.class);
            }else {//别人的闲置
                intent.setClass(context, IdleDetailsActivity.class);
            }
            context.startActivity(intent);
        }else if("3".equals(productType)){//定制家具详情
            String inId = product.productId;
            intent.setClass(context, CustomFurnitureDetailsActivity.class);
            intent.putExtra("id", inId);
            context.startActivity(intent);
        }else if("4".equals(productType)){//爆款活动详情
            String goid = product.productId;
            intent.putExtra("id",goid);
            intent.setClass(context, ProductDetailsBaokActivity.class);
            context.startActivity(intent);
        }else if("5".equals(productType)){//分类商品详情
            String goId = product.productId;
            intent.setClass(context, ProductDetailsOrdinaryActivity.class);
            intent.putExtra("id", goId);
            context.startActivity(intent);
        }else if("6".equals(productType)){//我的维修详情
            String goId = product.productId;
            intent.setClass(context, MineRepairDetailActivity.class);
            intent.putExtra("id", goId);
            context.startActivity(intent);
        }else if("7".equals(productType)){//我的比比货详情
            String goId = product.productId;
            intent.setClass(context, MinePriceRationDetailsActivity.class);
            intent.putExtra("id", goId);
            context.startActivity(intent);
        }else if("8".equals(productType)){//可购买商品详情
            String goId = product.productId;
            intent.setClass(context, ProductDetailsActivity.class);
            intent.putExtra("id", goId);
            context.startActivity(intent);
        }
    }
    public static boolean checkPermission(Activity activity,String permission){
        // Android 6.0 checkSelfPermission
        if (ContextCompat.checkSelfPermission(activity, permission)
                != PackageManager.PERMISSION_GRANTED) {//还没授予该权限
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                Tst.showToast("您已禁止该权限，需要重新开启!");
            }else{//申请授权
                ActivityCompat.requestPermissions(activity, new String[]{permission},
                        ConstantObj.STORAGE_REQUEST_CODE);
            }
            return false;
        }else{//你已经受理
            return true;
        }
    }
    /**
     * 跳转到选择相册界面
     */
    private void goToAlbum(Activity context) {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            context.startActivityForResult(Intent.createChooser(intent, "选择图片"),
                    REQUEST_CODE_GETIMAGE_BYSDCARD);
        } else {
            intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            context.startActivityForResult(Intent.createChooser(intent, "选择图片"),
                    REQUEST_CODE_GETIMAGE_BYSDCARD);
        }

    }

    public static String getRandomPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "'IMG'_yyyyMMdd_HHmmss");
        return dateFormat.format(date) + ".jpg";
    }

    public static String convertFileSize(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;
        if (size >= gb) {
            return String.format("%.1f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
        } else
            return String.format("%d B", size);
    }

    /**
     * @param tv      TextView
     * @param content 要高亮的内容
     * @return 已经解析之后的TextView
     */
    public static void handleText(TextView tv, String content) {
        SpannableString spannableString = new SpannableString(content);
        MoonUtil.setPhoneHighlight(spannableString);
        MoonUtil.setHtmlHighlight(spannableString);
        MoonUtil.viewSetText(tv,spannableString);
    }

    public static String keepTwoDecimal(double num) {
        if (num > 9999) {
            DecimalFormat decimalFormat = new DecimalFormat("#.00");
            return decimalFormat.format(num / 10000) + "W";
        } else {
            return String.valueOf(num).substring(0,String.valueOf(num).indexOf("."));
        }
    }

    public static String phoneNumberFormat(String number) {
        if (!isMobileNO(number)) return number;
        return number.substring(0, 3) + "****" + number.substring(7, 11);
    }

    public static boolean isMobileNO(String mobiles) {
        String telRegex = "[1][34578]\\d{9}";// "[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
        if (TextUtils.isEmpty(mobiles))
            return false;
        else
            return mobiles.matches(telRegex);
    }

    public static boolean VerificationPwd(String pwd) {
        String pwdRegex = "^([0-9]+[a-zA-Z]+|[a-zA-Z]+[0-9]+)[0-9a-zA-Z]*$";
        return pwd.matches(pwdRegex);
    }

    public static boolean inMainProcess() {
        String packageName = KnmsApp.getInstance().getPackageName();
        String processName = getProcessName(KnmsApp.getInstance());
        return packageName.equals(processName);
    }

    /**
     * 获取当前进程名
     *
     * @param context
     * @return 进程名
     */
    public static final String getProcessName(Context context) {
        String processName = null;
        // ActivityManager
        ActivityManager am = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE));
        while (true) {
            for (ActivityManager.RunningAppProcessInfo info : am.getRunningAppProcesses()) {
                if (info.pid == android.os.Process.myPid()) {
                    processName = info.processName;

                    break;
                }
            }
            // go home
            if (!TextUtils.isEmpty(processName)) {
                return processName;
            }
            // take a rest and again
            try {
                Thread.sleep(100L);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
    /**
     * 获取进程号对应的进程名
     * @param pid 进程号
     * @return 进程名
     */
    public static String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }
    /**
     * 获取32位uuid
     *
     * @return
     */
    public static String get32UUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static int len(String s) {
        s = s.replaceAll("[^\\x00-\\xff]", "**");
        int length = s.length();
        return length;
    }
    public static boolean jsonContainKey(String json,String key){
        JSONObject jsonObj = null;
        try {
            jsonObj = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return !jsonObj.isNull(key);
    }
    /**
     * JsonObject转Map
     */
    public static Map<String, Object> jsonToMap(String jsonString){
        if(TextUtils.isEmpty(jsonString)) return new HashMap<>();;
        Map<String, Object> jsonMap = new TreeMap<String, Object>();
        JSONObject jsonObj = null;
        try {
            jsonObj = new JSONObject(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
            return jsonMap;
        }
        Iterator<?> jsonKeys = jsonObj.keys();
        while (jsonKeys.hasNext()) {
            String jsonKey = (String) jsonKeys.next();
            Object jsonValObj = null;
            try {
                jsonValObj = jsonObj.get(jsonKey);
            } catch (JSONException e) {
                e.printStackTrace();
                return jsonMap;
            }
            if (jsonValObj instanceof JSONArray) {
                try {
                    jsonMap.put(jsonKey, jsonToList((JSONArray) jsonValObj));
                } catch (JSONException e) {
                    e.printStackTrace();
                    return jsonMap;
                }
            } else if (jsonValObj instanceof JSONObject) {
                jsonMap.put(jsonKey, jsonToMap(jsonValObj.toString()));
            } else {
                jsonMap.put(jsonKey, jsonValObj);
            }
        }
        return jsonMap;
    }
    /**
     * JsonArray转List
     */
    private static List<Object> jsonToList(JSONArray jsonArr)
            throws JSONException {
        List<Object> jsonToMapList = new ArrayList<Object>();
        for (int i = 0; i < jsonArr.length(); i++) {
            Object object = jsonArr.get(i);
            if (object instanceof JSONArray) {
                jsonToMapList.add(jsonToList((JSONArray) object));
            } else if (object instanceof JSONObject) {
                jsonToMapList.add(jsonToMap(object.toString()));
            } else {
                jsonToMapList.add(object);
            }
        }
        return jsonToMapList;
    }

    public static String numberConvert(int numStr) {
        if (numStr < 9999) {
            return String.valueOf(numStr);
        } else {
            DecimalFormat decimalFormat = new DecimalFormat("#.00");
            return decimalFormat.format(numStr / 10000) + "W";
        }
    }

    public static void  systemCopy(String content){
        //获取剪贴板管理服务
        ClipboardManager cm =(ClipboardManager) KnmsApp.getInstance().getSystemService(Context.CLIPBOARD_SERVICE);
        //将文本数据复制到剪贴板
        cm.setText(content);
    }
    public static void copyText(final TextView textView) {
        Context context = textView.getContext();
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popView = layoutInflater.inflate(R.layout.popup_view, null);
        final PopupWindow popupWindow = new PopupWindow(popView);
        popView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popupWidth = popView.getMeasuredWidth();
        int popupHeight = popView.getMeasuredHeight();
        int viewWidth = textView.getWidth();
        int[] location = new int[2];
        textView.getLocationOnScreen(location);
        // 这个是为了点击"返回Back"也能使其消失，并且并不会影响你的背景
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        // 使其聚集
        popupWindow.setFocusable(true);
        // // 设置允许在外点击消失
        popupWindow.setOutsideTouchable(true);
        popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.showAtLocation(textView, Gravity.NO_GRAVITY, location[0] - (popupWidth - viewWidth)/2,
                location[1] - popupHeight);
        popView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                systemCopy(textView.getText().toString());
            }
        });
    }

    public static boolean isDouble(String str){
        if(TextUtils.isEmpty(str)) return false;
        Pattern pattern = Pattern.compile("^[-]?\\d+(\\.\\d+)?$");
        Matcher isNum = pattern.matcher(str);
        if( !isNum.matches() ){
            return false;
        }
        return true;
    }
    public static boolean isInteger(String str){
        if(TextUtils.isEmpty(str)) return false;
        Pattern pattern = Pattern.compile("^[-]?\\d+$");
        Matcher isNum = pattern.matcher(str);
        if( !isNum.matches() ){
            return false;
        }
        return true;
    }
    public static double str2Double(String str){
        if(!isDouble(str)) return 0;
        return Double.parseDouble(str);
    }
    public static int str2Integer(String str){
        if(!isInteger(str)) return 0;
        return Integer.parseInt(str);
    }

    public static String getEventId(){
       return get32UUID()+(System.currentTimeMillis()+"").substring(0,8);
    }
    /**
     * 在价格前面添加￥符号
     * @param content
     * @return
     */
    public static String addMoneySymbol(String content) {
        if(TextUtils.isEmpty(content)) return "";
        Pattern pattern = Pattern.compile("[￥|¥]?\\d+(\\.\\d+)?");
        Matcher matcher = pattern.matcher(content);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String oldReplace = matcher.group(0);
            String newReplace = "￥" + oldReplace.replace("￥", "").replace("¥", "");
			/*
			 * 我们在这里使用Matcher对象的appendReplacement()方法来进行替换操作，而
			 * 不是使用String对象的replaceAll()或replaceFirst()方法来进行替换操作，因为
			 * 它们都能只能进行一次性简单的替换操作，而且只能替换成一样的内容，而这里则是要求每
			 * 一个匹配式的替换值都不同，所以就只能在循环里使用appendReplacement方式来进行逐个替换了。
			 */
            matcher.appendReplacement(sb, newReplace);
        }
        //最后还得要把尾串接到已替换的内容后面去
        matcher.appendTail(sb);
        return sb.toString();
    }
    public static SpannableStringBuilder highlight(String text, String target) {
        SpannableStringBuilder spannable = new SpannableStringBuilder(text);
        CharacterStyle span = null;

        Pattern p = Pattern.compile(target);
        Matcher m = p.matcher(text);
        while (m.find()) {
            span = new ForegroundColorSpan(Color.RED);// 需要重复！
            spannable.setSpan(span, m.start(), m.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spannable;
    }
    public static void gotoIndexGuangGuang(BaseQuickAdapter adapter,String text){
        if(adapter.getItemCount() > 0) adapter.setNewData(null);
        adapter.setEmptyView(R.layout.layout_view_no_data);
        ((ImageView) adapter.getEmptyView().findViewById(R.id.img_no_data)).setImageResource(R.drawable.no_data_1);
        ((TextView) adapter.getEmptyView().findViewById(R.id.tv_no_data)).setText(text);
        Button btn = (Button) adapter.getEmptyView().findViewById(R.id.btn_bottom);
        btn.setVisibility(View.VISIBLE);
        btn.setText("去逛逛");  //去首页逛逛
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                if(context instanceof BaseActivity){
                    Map<String,Object> parmas = new HashMap<>();
                    parmas.put("source","mHomePage");
                    ((BaseActivity) context).startActivityAnimGeneral(MainActivity.class,parmas);
                }else if(context instanceof BaseFragmentActivity){
                    Map<String,Object> parmas = new HashMap<>();
                    parmas.put("source","mHomePage");
                    ((BaseFragmentActivity) context).startActivityAnimGeneral(MainActivity.class,parmas);
                }else{
                    Intent intent = new Intent(context,MainActivity.class);
                    intent.putExtra("source","mHomePage");
                    context.startActivity(intent);
                }
            }
        });
    }
    public static void showNotData(BaseQuickAdapter adapter,int imgResId,String text){
        if(adapter.getItemCount() > 0) adapter.setNewData(null);
        adapter.setEmptyView(R.layout.layout_view_no_data);//
        ((ImageView) adapter.getEmptyView().findViewById(R.id.img_no_data)).setImageResource(imgResId > 0 ? imgResId : R.drawable.no_data_on_offer);
        ((TextView) adapter.getEmptyView().findViewById(R.id.tv_no_data)).setText(text);
        Button btn = (Button) adapter.getEmptyView().findViewById(R.id.btn_bottom);
        btn.setVisibility(View.GONE);
    }
    public static void gotoGuangGuang(BaseQuickAdapter adapter,String text){
        if(adapter.getItemCount() > 0) adapter.setNewData(null);
        adapter.setEmptyView(R.layout.layout_view_no_data);
        ((ImageView) adapter.getEmptyView().findViewById(R.id.img_no_data)).setImageResource(R.drawable.no_data_1);
        ((TextView) adapter.getEmptyView().findViewById(R.id.tv_no_data)).setText(text);
        Button btn = (Button) adapter.getEmptyView().findViewById(R.id.btn_bottom);
        btn.setVisibility(View.VISIBLE);
        btn.setText("去逛逛");
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                if(context instanceof BaseActivity){
                    ((BaseActivity) context).startActivityAnimGeneral(MallActivity.class,null);
                }else if(context instanceof BaseFragmentActivity){
                    ((BaseFragmentActivity) context).startActivityAnimGeneral(MallActivity.class,null);
                }else{
                    context.startActivity(new Intent(context,MallActivity.class));
                }
            }
        });
    }
}
