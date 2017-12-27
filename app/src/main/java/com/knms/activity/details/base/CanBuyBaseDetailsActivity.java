package com.knms.activity.details.base;

import android.app.Dialog;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.knms.activity.CommWebViewActivity;
import com.knms.activity.ShopActivityF;
import com.knms.activity.mall.cart.ShoppingCartActivityF;
import com.knms.activity.im.MessageCenterActivity;
import com.knms.activity.login.FasterLoginActivity;
import com.knms.activity.main.MainActivity;
import com.knms.adapter.AutoBrowseAdapter;
import com.knms.adapter.base.CommonAdapter;
import com.knms.adapter.base.ViewHolder;
import com.knms.adapter.baserecycler.BaseQuickAdapter;
import com.knms.adapter.baserecycler.BaseViewHolder;
import com.knms.android.R;
import com.knms.bean.AffectedNumber;
import com.knms.bean.ResponseBody;
import com.knms.bean.classification.ClassifyDetail;
import com.knms.bean.goodsdetails.GoodsDetails;
import com.knms.net.RxRequestApi;
import com.knms.util.ConstantObj;
import com.knms.util.DialogHelper;
import com.knms.util.ImageLoadHelper;
import com.knms.util.LocalDisplay;
import com.knms.util.SPUtils;
import com.knms.util.Tst;
import com.knms.view.CircleImageView;
import com.knms.view.CountdownView;
import com.knms.view.banner.AutoViewPager;
import com.knms.view.clash.FullyGridLayoutManager;
import com.knms.view.listview.MaxHightListView;
import com.knms.view.tv.MsgTipText;
import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


/**
 * Created by Administrator on 2017/7/11.
 */

public abstract class CanBuyBaseDetailsActivity extends BaseDetailsActivity implements View.OnClickListener {
    /*******
     * 头部view
     *******/
    public RelativeLayout layoutHeadAlpha;
    public ImageView ivBtnBack;
    public ImageView iconHeadShoppingCart;
    public ImageView iconHeadToHome;
    public ImageView iconHeadMsgTip;
    public MsgTipText showCartCount;


    /*******
     * 顶部view
     *******/
    ViewPager viewPagerBanner;
    TextView tvBannerPage;
    TextView tvDetailsTitle, tvDetailsContent, tvDetailsBrowserCount;
    RelativeLayout layoutService;
    RecyclerView recyclerViewService;
    RelativeLayout layoutShop;
    CircleImageView ivShopAvatar;
    TextView tvShopName;
    TextView tvCurrentPrice, tvOldPrice, tvFreight, tvNumberSold;
    AutoBrowseAdapter adapterBanner;

    CountdownView countdownView;
//    LinearLayout llShowTime;


//    TextView tvDay, tvHour, tvMinute, tvSecond, tvTimeRemark;

//    private String showStartTimeActivity = "1";
//    private int showStartTime = 0;
//    private String showEndTimeActivity = "1";
//    private int showEndTime = 0;


    @Override
    protected void initView() {
        super.initView();
        ivContactMerchant.setVisibility(View.VISIBLE);
        ivContactMerchant.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        reqApi();
        getShoppingCartCount();
    }

    @Override
    public View loadHeadView(ViewStub viewStub) {
        viewStub.setLayoutResource(R.layout.top_product_details_layout);
        if (viewHead == null) viewHead = viewStub.inflate();
        layoutHeadAlpha = (RelativeLayout) viewHead.findViewById(R.id.rl_border);
        ivBtnBack = (ImageView) viewHead.findViewById(R.id.iv_back);
        iconHeadShoppingCart = (ImageView) viewHead.findViewById(R.id.iv_shopping_cart);
        iconHeadToHome = (ImageView) viewHead.findViewById(R.id.home_page);
        iconHeadMsgTip = (ImageView) viewHead.findViewById(R.id.information);
        showCartCount = (MsgTipText) viewHead.findViewById(R.id.tv_shppingcart_count);
        ivBtnBack.setOnClickListener(this);
        iconHeadMsgTip.setOnClickListener(this);
        iconHeadShoppingCart.setOnClickListener(this);
        iconHeadToHome.setOnClickListener(this);
        iconHeadShoppingCart.setOnClickListener(this);
        return viewHead;
    }

    @Override
    public View loadTopView(ViewStub viewStub) {
        viewStub.setLayoutResource(R.layout.stub_details_top_can_buy);
        if (viewTop == null) viewTop = viewStub.inflate();
        tvCurrentPrice = (TextView) viewTop.findViewById(R.id.current_price);
        tvOldPrice = (TextView) viewTop.findViewById(R.id.cost_price);
        tvOldPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        tvFreight = (TextView) viewTop.findViewById(R.id.tv_freight);
        tvNumberSold = (TextView) findViewById(R.id.tv_number_sold);
        viewPagerBanner = (ViewPager) viewTop.findViewById(R.id.goods_img_viewpager);
        if(viewPagerBanner instanceof AutoViewPager) ((AutoViewPager) viewPagerBanner).setAuto(false);
        tvBannerPage = (TextView) viewTop.findViewById(R.id.tv_num);
        tvDetailsTitle = (TextView) viewTop.findViewById(R.id.title);
        tvDetailsBrowserCount = (TextView) viewTop.findViewById(R.id.browse_amount);
        tvDetailsContent = (TextView) viewTop.findViewById(R.id.content);
        layoutService = (RelativeLayout) viewTop.findViewById(R.id.rl_serviceUI);
        recyclerViewService = (RecyclerView) viewTop.findViewById(R.id.recyclerView_service);
        recyclerViewService.setLayoutManager(new FullyGridLayoutManager(this, 3));
        recyclerViewService.setNestedScrollingEnabled(false);
        layoutShop = (RelativeLayout) viewTop.findViewById(R.id.goto_shop);
        ivShopAvatar = (CircleImageView) viewTop.findViewById(R.id.shop_head_portrait);
        tvShopName = (TextView) viewTop.findViewById(R.id.shop_name);

        countdownView = (CountdownView) viewTop.findViewById(R.id.view_countdown);

        layoutShop.setOnClickListener(this);
        return viewTop;
    }

    public void updateView(final GoodsDetails data) {
        if (data == null) return;
        countdownView.addCountDownListenter(new CountdownView.CountDownListener() {
            @Override
            public void startTime(String timeType) {}
            @Override
            public void finishTime(String timeType,boolean hasShow) {
                if(countdownView.StartTimeType.equals(timeType)){
                    startSnapUp();
                }else if(countdownView.EndTimeType.equals(timeType)){
                    stopSnapUp();
                }
            }
        });
        if("0".equals(data.commodityShowBo.showStartTimeActivity) && "0".equals(data.commodityShowBo.showEndTimeActivity)){
            countdownView.setVisibility(View.GONE);
        }else{
            countdownView.setVisibility(View.VISIBLE);
            countdownView.setStartTime("1".equals(data.commodityShowBo.showStartTimeActivity),data.commodityShowBo.showStartTime);
            countdownView.setEndTime("1".equals(data.commodityShowBo.showEndTimeActivity),data.commodityShowBo.showEndTime);
            countdownView.startCountDown();
        }

        tvCurrentPrice.setText(data.commodityShowBo.realityPrice);
        tvOldPrice.setText(data.commodityShowBo.showPrice);
        tvFreight.setText(data.commodityShowBo.showFreightTitle);
        tvNumberSold.setText(data.commodityShowBo.showSalesTitle);

        layoutShop.setTag(data.commodityShowBo.shopId);
        tvDetailsTitle.setText(data.commodityShowBo.showName);
        tvDetailsBrowserCount.setText(data.commodityShowBo.browseNumber+"次浏览");
        tvDetailsContent.setText(data.commodityShowBo.showDescription);
        tvShopName.setText(data.commodityShowBo.shopNickname);
        adapterBanner = new AutoBrowseAdapter(this, data.commodityShowBo.showImages);
        viewPagerBanner.setAdapter(adapterBanner);
        viewPagerBanner.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                tvBannerPage.setText(position + 1 + "/" + adapterBanner.getCount());
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        tvBannerPage.setText("1/" + data.commodityShowBo.showImages.size());
        ImageLoadHelper.getInstance().displayImageHead(this, data.commodityShowBo.shopImage, ivShopAvatar);

        /*******商家服务项目*******/
        List<ClassifyDetail.ServiceInfo> serviceInfos = data.commodityShowBo.serviceList;
        if (serviceInfos != null && serviceInfos.size() > 0) {
            layoutService.setVisibility(View.VISIBLE);
            serviceInfos = serviceInfos.size() > 3 ? serviceInfos.subList(0, 3) : serviceInfos;
            recyclerViewService.setAdapter(new BaseQuickAdapter<ClassifyDetail.ServiceInfo>(R.layout.item_custom_detail_service, serviceInfos) {
                @Override
                protected void convert(BaseViewHolder helper, ClassifyDetail.ServiceInfo item) {
                    ImageLoadHelper.getInstance().displayImage(CanBuyBaseDetailsActivity.this, item.photo, (ImageView) helper.getView(R.id.service_img));
                    helper.setText(R.id.type_of_service, item.name);
                }
            });
            layoutService.setTag(serviceInfos);
            layoutService.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<ClassifyDetail.ServiceInfo> serviceInfos = (List<ClassifyDetail.ServiceInfo>) v.getTag();
                    MobclickAgent.onEvent(CanBuyBaseDetailsActivity.this, "detailServiceClick");
                    showPopDialog(serviceInfos);
                }
            });
        } else {
            layoutService.setVisibility(View.GONE);
        }
    }

    private void showPopDialog(final List<ClassifyDetail.ServiceInfo> serviceInfos) {
        if (serviceInfos == null) return;
        DialogHelper.showBottomDialog(this, R.layout.dialog_custom_furniture_details, new DialogHelper.OnEventListener<Dialog>() {
            @Override
            public void eventListener(View parentView, final Dialog window) {
                CommonAdapter adapter = new CommonAdapter<ClassifyDetail.ServiceInfo>(CanBuyBaseDetailsActivity.this,
                        R.layout.listview_item_custom_furniture_details, serviceInfos) {
                    @Override
                    public void convert(ViewHolder helper, ClassifyDetail.ServiceInfo data) {
                        if (TextUtils.isEmpty(data.detailUrl)) {
                            helper.getView(R.id.tvBtn_detail).setVisibility(View.GONE);
                            helper.getView(R.id.iv_arrow).setVisibility(View.GONE);
                        } else {
                            helper.getView(R.id.tvBtn_detail).setVisibility(View.VISIBLE);
                            helper.getView(R.id.iv_arrow).setVisibility(View.VISIBLE);
                        }
                        helper.getView(R.id.tvBtn_detail).setTag(data.detailUrl);
                        helper.getView(R.id.tvBtn_detail).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                MobclickAgent.onEvent(CanBuyBaseDetailsActivity.this, "serviceLearnMoreClick");
                                String url = (String) v.getTag();
                                Map<String, Object> param = new HashMap<String, Object>();
                                param.put("url", url);
                                startActivityAnimGeneral(CommWebViewActivity.class, param);
                            }
                        });
                        helper.setText(R.id.custom_service_name, data.name);
                        helper.setText(R.id.custom_service_content, data.remark);
                        ImageLoadHelper.getInstance().displayImage(CanBuyBaseDetailsActivity.this, data.photo, (ImageView) helper.getView(R.id.custom_service_img), LocalDisplay.dp2px(20), LocalDisplay.dp2px(20));
                    }
                };
                MaxHightListView listView = (MaxHightListView) parentView.findViewById(R.id.listview);
                listView.setMaxHeight(LocalDisplay.dip2px(240));
                listView.setAdapter(adapter);
                parentView.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        window.dismiss();
                    }
                });
            }
        });
    }

    @Override
    public void updateMsgTip(boolean isUnreadMsg) {
        if (ContextCompat.getDrawable(this, R.drawable.chat_red).getConstantState().equals(iconHeadMsgTip.getDrawable().getCurrent().getConstantState())
                || ContextCompat.getDrawable(this, R.drawable.icon_details_top_chat).getConstantState().equals(iconHeadMsgTip.getDrawable().getCurrent().getConstantState())) {
            if (isUnreadMsg) {
                iconHeadMsgTip.setImageResource(R.drawable.chat_red);
            } else {
                iconHeadMsgTip.setImageResource(R.drawable.icon_details_top_chat);
            }
        } else {
            if (isUnreadMsg) {
                iconHeadMsgTip.setImageResource(R.drawable.chat_red_b);
            } else {
                iconHeadMsgTip.setImageResource(R.drawable.chat_b);
            }
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finshActivity();
                break;
            case R.id.home_page:
                Map<String, Object> p = new HashMap<>();
                p.put("source", "mHomePage");
                startActivityAnimGeneral(MainActivity.class, p);
                break;
            case R.id.information:
                if (SPUtils.isLogin()) {
                    startActivityAnimGeneral(MessageCenterActivity.class, null);
                } else {
                    startActivityAnimGeneral(FasterLoginActivity.class, null);
                }
                break;

            case R.id.goto_shop:
                String shopId = (String) v.getTag();
                if (TextUtils.isEmpty(shopId)) return;
                MobclickAgent.onEvent(this, "shopBtnClick");
                Map<String, Object> param = new HashMap<>();
                param.put("shopId", shopId);
                param.put("position", 0);
                startActivityAnimGeneral(ShopActivityF.class, param);
                break;
            case R.id.iv_shopping_cart:
                startActivityAnimGeneral(ShoppingCartActivityF.class,null);
                break;
        }
    }

    @Override
    protected void reqApi() {
        if (getDetailApi() == null) return;
        getDetailApi().compose(this.<ResponseBody<GoodsDetails>>applySchedulers())
                .subscribe(new Action1<ResponseBody<GoodsDetails>>() {
                    @Override
                    public void call(ResponseBody<GoodsDetails> body) {
                        if (body.isSuccess1()) {
                            updateView(body.data);
                            updateDetails(body.data);
                            if (null != body.data) {
                                if (body.data.commodityShowBo.showStatus.equals("3")) {
                                    tvError.setVisibility(View.VISIBLE);
                                } else {
                                    tvError.setVisibility(View.GONE);
                                }
                            }
                        }else Tst.showToast(body.desc);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Tst.showToast(throwable.getMessage());
                    }
                });
    }

    int shoppingCartCount = 0;

    private void getShoppingCartCount() {
        Map<String,Object> params = new HashMap<>();
        params.put("userId",ConstantObj.TEMP_USERID);
        RxRequestApi.getInstance().getApiService().getShoppingCartCount(params)
                .compose(this.<ResponseBody<AffectedNumber>>applySchedulers())
                .subscribe(new Action1<ResponseBody<AffectedNumber>>() {
                    @Override
                    public void call(ResponseBody<AffectedNumber> affectedNumberResponseBody) {
                        if (affectedNumberResponseBody.isSuccess1()) {
                            shoppingCartCount = affectedNumberResponseBody.data.getCount();
                            showCartCount.setText(shoppingCartCount + "");
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Tst.showToast(throwable.getMessage());
                    }
                });
    }

    public abstract void startSnapUp();

    public abstract void stopSnapUp();

    public abstract void updateDetails(GoodsDetails data);

    public abstract Observable<ResponseBody<GoodsDetails>> getDetailApi();
}
