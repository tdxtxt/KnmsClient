package com.knms.activity.details.canbuy;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.TextView;

import com.knms.activity.base.BaseActivity;
import com.knms.activity.dialog.ChoiceSizeActivityDialog;
import com.knms.activity.im.ChatActivity;
import com.knms.activity.login.FasterLoginActivity;
import com.knms.activity.mall.order.ConfirmOrderActivity;
import com.knms.android.R;
import com.knms.bean.ResponseBody;
import com.knms.bean.goodsdetails.GoodsDetails;
import com.knms.bean.order.neworder.RequestBuyParameters;
import com.knms.bean.sku.SkuBody;
import com.knms.bean.sku.base.SkuModel;
import com.knms.core.im.msg.Product;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.net.RxRequestApi;
import com.knms.util.SPUtils;
import com.knms.util.Tst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.shareuzi.bean.ShareEntity;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * 类描述：
 * 创建人：tdx
 * 创建时间：2017/8/29 17:02
 * 传参：
 * 返回:
 */
public class CouponControl extends BaseControl implements View.OnClickListener{
    /*******
     * 底部菜单view
     *******/
    View viewCollect, viewShare, viewChat;
    ImageView iconCollect;
    TextView tvCollectCount;
    TextView tvAddCart, tvBuy;

    private int collectNumber = 0;
    private boolean isChoose = false;

    public static final int REQEST_CODE_BUY = 0x00222;
    public String skuId;

    public CouponControl(BaseActivity activity, String goid, GoodsDetails.CommodityShowBoBean data) {
        super(activity, goid, data);
        init();
    }

    @Override
    protected void loadMenuUi(ViewStub viewStub) {
        viewStub.setLayoutResource(R.layout.stub_details_menu_can_buy);
        if (viewMenu == null) viewMenu = viewStub.inflate();
        viewCollect = viewMenu.findViewById(R.id.collect);
        viewShare = viewMenu.findViewById(R.id.share);
        viewChat = viewMenu.findViewById(R.id.contact_service);
        iconCollect = (ImageView) viewMenu.findViewById(R.id.img_collect);
        tvCollectCount = (TextView) viewMenu.findViewById(R.id.collect_amount);
        tvBuy = (TextView) viewMenu.findViewById(R.id.tv_buy);
        tvAddCart = (TextView) viewMenu.findViewById(R.id.tv_add_shoppingcart);

        viewCollect.setOnClickListener(this);
        viewShare.setOnClickListener(this);
        viewChat.setOnClickListener(this);
        tvBuy.setOnClickListener(this);

        if(data == null) viewMenu.setVisibility(View.GONE);
    }

    @Override
    public void updateMenuUi() {
        if(data == null) return;
        tvAddCart.setBackgroundColor(Color.parseColor("#E5E5E5"));
        tvAddCart.setTextColor(Color.parseColor("#ffffff"));
        tvAddCart.setEnabled(false);

        viewShare.setTag(data.shareData);
        viewChat.setTag(data);

        collectNumber = data.collectNumber;
        if (data.iscollectNumber == 0) {
            iconCollect.setImageResource(R.drawable.shou_cang_on);
            isChoose = true;
        } else {
            iconCollect.setImageResource(R.drawable.shou_cang_off);
            isChoose = false;
        }
        tvCollectCount.setText("收藏" + (data.collectNumber == 0 ? "" : data.collectNumber));
    }

    @Override
    public void startSnapUpBefore() {
        stopSnapUp();
    }
    @Override
    public void startSnapUp() {
        tvBuy.setTextColor(Color.parseColor("#333333"));
        tvBuy.setBackgroundColor(Color.parseColor("#FDD301"));
        tvBuy.setClickable(true);
    }
    @Override
    public void stopSnapUp() {
        tvBuy.setTextColor(Color.parseColor("#ffffff"));
        tvBuy.setBackgroundColor(Color.parseColor("#cbcbcb"));
        tvBuy.setClickable(false);
    }
    @Override
    public void updateStivkyUi() {}
    @Override
    public void updateFooterUi() {}
    @Override
    public void detachView() {
        super.detachView();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.collect:
                if (!SPUtils.isLogin()) {
                    activity.startActivityAnimGeneral(FasterLoginActivity.class, null);
                    break;
                }
                RxRequestApi.getInstance().getApiService().collect(goodsId, 1, isChoose ? 1 : 0)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<ResponseBody>() {
                            @Override
                            public void call(ResponseBody body) {
                                if (body.isSuccess()) {
                                    if (isChoose) {
                                        collectNumber--;
                                        iconCollect.setImageResource(R.drawable.shou_cang_off);
                                        isChoose = false;
                                    } else {
                                        collectNumber++;
                                        iconCollect.setImageResource(R.drawable.shou_cang_on);
                                        isChoose = true;
                                    }
                                    RxBus.get().post(BusAction.CANCEL_GOODS_COLLECTION, isChoose);
                                    tvCollectCount.setText("收藏" + (collectNumber == 0 ? "" : collectNumber));
                                } else {
                                    if ("已经收藏".equals(body.desc)){
                                        activity.refresh();//刷新列表
                                    }else if(!"需要登录".equals(body.desc)){
                                        Tst.showToast(body.desc);
                                    }
                                }
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                Tst.showToast(throwable.toString());
                            }
                        });
                break;
            case R.id.share:
                //TODO 分享
                ShareEntity data = (ShareEntity) v.getTag();
                if (data == null) return;
                OnekeyShare oks = new OnekeyShare();
                oks.show(activity, data);
                break;
            case R.id.contact_service:
                if (!SPUtils.isLogin()) {
                    activity.startActivityAnimGeneral(FasterLoginActivity.class, null);
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
                product.productType = "4";//分类商品详情
                product.productId = detail.showId;
                parmas.put("prodcut", product);
                activity.startActivityAnimGeneral(ChatActivity.class, parmas);
                break;
            case R.id.tv_buy:
                if (!SPUtils.isLogin()) {
                    activity.startActivityAnimGeneral(FasterLoginActivity.class, null);
                    return;
                }

                if (TextUtils.isEmpty(goodsId)) return;
                Intent intent = new Intent(activity, ChoiceSizeActivityDialog.class);
                intent.putExtra("data",mSkuBody);
                intent.putExtra("goid", goodsId);
                intent.putExtra("initSkuId", skuId);
//                intent.putExtra("initCount",item.buyCount);
                activity.startActivityForResult(intent, REQEST_CODE_BUY);
                break;
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;
        switch (requestCode) {
            case REQEST_CODE_BUY://得到的数据
                SkuModel resultSku = (SkuModel) data.getSerializableExtra("resultSku");
                skuId = data.getStringExtra("resultSkuId");
                int resultCount = data.getIntExtra("resultCount", 1);
                if (resultSku != null) {
                    skuId = resultSku.skuId;
                    List<RequestBuyParameters> list = new ArrayList<>();
                    RequestBuyParameters parameters = new RequestBuyParameters();
                    parameters.buyQuantity = resultCount;
                    parameters.specificationId = skuId;
                    list.add(parameters);
                    Map<String,Object> map=new HashMap<>();
                    map.put("buyParameters",list);
                    map.put("type",2);
                    activity.startActivityAnimGeneral(ConfirmOrderActivity.class, map);
                }
                break;
        }
    }
    protected SkuBody.ComdiBo mSkuBody;
    @Override
    protected void getProductAttr() {
        Map<String,Object> params = new HashMap<>();
        params.put("showId",goodsId);
        RxRequestApi.getInstance().getApiService().getSkuProduct(params)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<ResponseBody<SkuBody>>() {
                    @Override
                    public void call(ResponseBody<SkuBody> body) {
                        if(body.isSuccess1()){
                            totalStock = 0;
                            mSkuBody = body.data.comdiBo;
                            if(mSkuBody != null && mSkuBody.skuProducts != null){
                                for (SkuModel sku : mSkuBody.skuProducts) {
                                    totalStock += sku.stock;
                                }
                            }
                            if(totalStock < 1){
                                Tst.showToast("库存不足");
                                stopSnapUp();
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {}
                });
    }
}
