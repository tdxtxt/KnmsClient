package com.knms.activity.details.canbuy;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.knms.activity.base.BaseActivity;
import com.knms.activity.im.ChatActivity;
import com.knms.activity.im.KnmsKefuChatActivity;
import com.knms.activity.login.FasterLoginActivity;
import com.knms.android.R;
import com.knms.bean.ResponseBody;
import com.knms.bean.goodsdetails.GoodsDetails;
import com.knms.bean.user.User;
import com.knms.core.im.IMHelper;
import com.knms.core.im.msg.Product;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.net.RxRequestApi;
import com.knms.util.SPUtils;
import com.knms.util.Tst;

import java.util.HashMap;
import java.util.Map;

import rx.Subscription;
import rx.functions.Action1;

/**
 * 类描述：在线购买的商品详情
 * 创建人：tdx
 * 创建时间：2017/8/29 11:32
 * 传参：
 * 返回:
 */
public class ProductDetailsActivity extends BaseActivity {
    //创建 control，
    public BaseControl control;

    protected TextView tvError;
    protected ImageView ivContactMerchant;

    public String goodsId = "";
    @Override
    protected void getParmas(Intent intent) {
        super.getParmas(intent);
        goodsId = intent.getStringExtra("id");
    }

    @Override
    protected int layoutResID() {
        return R.layout.base_act_details;
    }

    @Override
    protected void initView() {
        ivContactMerchant=findView(R.id.iv_contact_merchant);
        tvError = findView(R.id.sold_out_layout);

        ivContactMerchant.setVisibility(View.GONE);
    }
    public Subscription subscriptionMsgCount;
    @Override
    protected void onResume() {
        super.onResume();
        showImMsg();
        if (control != null) {
            control.getShoppingCartCount();
        }
    }
    public void showImMsg(){
        if(subscriptionMsgCount != null) subscriptionMsgCount.unsubscribe();
        subscriptionMsgCount = IMHelper.getInstance().isUnreadMsg().subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean aBoolean) {
                if(control != null) control.updateHeadUi(aBoolean);
            }
        });
    }
    @Override
    protected void initData() {
        reqApi();
    }

    @Override
    protected void reqApi() {
        Map<String, Object> stringMap = new HashMap<>();
        stringMap.put("showId", goodsId);
        RxRequestApi.getInstance().getApiService().getCommodityDetails(stringMap)
        .compose(this.<ResponseBody<GoodsDetails>>applySchedulers())
        .subscribe(new Action1<ResponseBody<GoodsDetails>>() {
            @Override
            public void call(ResponseBody<GoodsDetails> body) {
                if(body.isSuccess1()){
                    if (null != body.data) {
                        if(control!=null){
                            control.updateData(body.data.commodityShowBo);
                            control.updateMenuUi();
                            return;
                        }
                        ivContactMerchant.setVisibility(View.VISIBLE);
                        if (body.data.commodityShowBo.showStatus.equals("3")) {
                            tvError.setVisibility(View.VISIBLE);
                        } else {
                            tvError.setVisibility(View.GONE);
                        }
                        ivContactMerchant.setTag(body.data.commodityShowBo);
                        updateView(body.data.commodityShowBo);
                    }
                }else{
                    updateView(null);
                    Tst.showToast(body.desc);
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Tst.showToast(throwable.getMessage());
                updateView(null);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        control.onActivityResult(requestCode,resultCode,data);
    }
    private void updateView(final GoodsDetails.CommodityShowBoBean data) {
        ivContactMerchant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!SPUtils.isLogin()) {
                    startActivityAnimGeneral(FasterLoginActivity.class, null);
                    return;
                }
                if(data == null) return;
                if("2".equals(data.showType)){
                    startActivityAnimGeneral(KnmsKefuChatActivity.class, null);
                    return;
                }
                GoodsDetails.CommodityShowBoBean detail = (GoodsDetails.CommodityShowBoBean) v.getTag();
                if (detail == null) return;
                Map<String, Object> parmas = new HashMap<>();
                parmas.put("sid", detail.ssmerchantid);
                parmas.put("shopId", detail.shopId);
                Product product = new Product();//构建product对象
                product.content = detail.showDescription;
                product.icon = (detail.showImages != null && detail.showImages.size() > 0) ? detail.showImages.get(0).url : "";
                product.price = detail.realityPrice + "";
                product.productType = "8";//分类商品详情
                product.productId = detail.showId;
                parmas.put("prodcut", product);
                startActivityAnimGeneral(ChatActivity.class, parmas);
            }
        });
        if(data == null){
            if(control == null) control = new CouponControl(this,goodsId,data);
            return;
        }
        if("1".equals(data.showType)){//实体物品
            if(control == null) control = new MallControl(this,goodsId,data);
        }else if("2".equals(data.showType)) {//虚拟物品
            if(control == null) control = new CouponControl(this,goodsId,data);
            ivContactMerchant.setImageResource(R.drawable.float_icon_businesskefu);
        }

        showImMsg();
        control.getShoppingCartCount();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(control != null) control.detachView();
        if(subscriptionMsgCount != null) subscriptionMsgCount.unsubscribe();
    }
    @Override
    public String setStatisticsTitle() {
        return null;
    }

    @Subscribe(tags = {@Tag(BusAction.ACTION_LOGIN)})
    public void loginNotify(User user) {
        reqApi();
    }
    @Subscribe(tags = {@Tag(BusAction.ACTION_LOGOUT)})
    public void logoutNotify(String action) {
        if(control != null) control.getShoppingCartCount();//清空购物车小红点
        if(control != null) control.updateHeadUi(false);//清空消息小红点
    }
}
