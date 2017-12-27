package com.knms.activity.details.canbuy;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gyf.barlibrary.ImmersionBar;
import com.knms.activity.CommWebViewActivity;
import com.knms.activity.ShopActivityF;
import com.knms.activity.base.BaseActivity;
import com.knms.activity.im.MessageCenterActivity;
import com.knms.activity.login.FasterLoginActivity;
import com.knms.activity.main.MainActivity;
import com.knms.activity.mall.cart.ShoppingCartActivityF;
import com.knms.adapter.AutoBrowseAdapter;
import com.knms.adapter.NestedScrollViewOverScrollDecorAdapter;
import com.knms.adapter.base.CommonAdapter;
import com.knms.adapter.base.ViewHolder;
import com.knms.adapter.baserecycler.BaseQuickAdapter;
import com.knms.adapter.baserecycler.BaseViewHolder;
import com.knms.android.R;
import com.knms.bean.AffectedNumber;
import com.knms.bean.ResponseBody;
import com.knms.bean.classification.ClassifyDetail;
import com.knms.bean.goodsdetails.GoodsDetails;
import com.knms.bean.sku.SkuBody;
import com.knms.bean.sku.base.SkuModel;
import com.knms.net.RxRequestApi;
import com.knms.util.CommonUtils;
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

import me.everything.android.ui.overscroll.VerticalOverScrollBounceEffectDecorator;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static com.knms.util.LocalDisplay.getViewSize;

/**
 * 类描述：
 * 创建人：tdx
 * 创建时间：2017/8/29 11:15
 * 传参：
 * 返回:
 */
public abstract class BaseControl{
    protected BaseActivity activity;
    protected GoodsDetails.CommodityShowBoBean data;
    protected String goodsId;

    protected ViewStub viewsbHead,viewsbTop,viewsbFooter,viewsbStivkyOne,viewsbStivkyTwo,viewsbMenu;
    protected View viewHead,viewTop,viewFooter,viewMenu,viewStivkyOne,viewStivkyTwo;
    protected NestedScrollView scrollView;

    protected ImmersionBar mImmersionBar;
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
    TextView tvCurrentPrice, tvOldPrice, tvFreight, tvNumberSold,tvCouponPrice;
    AutoBrowseAdapter adapterBanner;

    CountdownView countdownView;

    public boolean isMsg = false;
    protected long totalStock = -1;


    public BaseControl(BaseActivity activity,String goid,GoodsDetails.CommodityShowBoBean data){
        this.data = data;
        this.goodsId = goid;
        this.activity = activity;

        scrollView = (NestedScrollView) activity.findViewById(R.id.scrollView);
        new VerticalOverScrollBounceEffectDecorator(new NestedScrollViewOverScrollDecorAdapter(scrollView));

        viewsbHead = (ViewStub) activity.findViewById(R.id.viewstub_head);
        viewsbTop = (ViewStub) activity.findViewById(R.id.viewstub_top);
        viewsbFooter = (ViewStub) activity.findViewById(R.id.viewstub_footer);
        viewsbStivkyOne = (ViewStub) activity.findViewById(R.id.viewstub_sticky_one);
        viewsbStivkyTwo = (ViewStub) activity.findViewById(R.id.viewstub_sticky_two);
        viewsbMenu = (ViewStub) activity.findViewById(R.id.viewstub_menu);
    }
    protected void init(){
        loadHeadUi(viewsbHead);
        loadTopUi(viewsbTop);
        loadStivkyUi(viewsbStivkyOne,viewsbStivkyTwo);
        loadFooterUi(viewsbFooter);
        loadMenuUi(viewsbMenu);

        initBar(viewHead.findViewById(R.id.view));

        updateTopUi();
        updateStivkyUi();
        updateFooterUi();
        updateMenuUi();
        getShoppingCartCount();
        getProductAttr();

    }

    protected void getProductAttr() {}
    protected void initBar(View view){
        if(view == null) return;
        mImmersionBar = ImmersionBar.with(activity);
        mImmersionBar.statusBarView(view)
//                .keyboardEnable(true)  //解决软键盘与底部输入框冲突问题
                .statusBarDarkFont(true, 0.1f)//状态栏字体是深色，不写默认为亮色
                .flymeOSStatusBarFontColor(R.color.status_bar_textcolor);  //修改flyme OS状态栏字体颜色;
        mImmersionBar.init();

        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) viewsbStivkyOne.getLayoutParams();
        int height = LocalDisplay.getViewSize(viewHead)[1];
        lp.setMargins(0,height,0,0);
        viewsbStivkyOne.setLayoutParams(lp);
    }

    protected void loadHeadUi(ViewStub viewStub){
        viewStub.setLayoutResource(R.layout.top_product_details_layout);
        if (viewHead == null) viewHead = viewStub.inflate();
        layoutHeadAlpha = (RelativeLayout) viewHead.findViewById(R.id.rl_border);
        ivBtnBack = (ImageView) viewHead.findViewById(R.id.iv_back);
        iconHeadShoppingCart = (ImageView) viewHead.findViewById(R.id.iv_shopping_cart);
        iconHeadToHome = (ImageView) viewHead.findViewById(R.id.home_page);
        iconHeadMsgTip = (ImageView) viewHead.findViewById(R.id.information);
        showCartCount = (MsgTipText) viewHead.findViewById(R.id.tv_shppingcart_count);
        ivBtnBack.setOnClickListener(onclickListener);
        iconHeadMsgTip.setOnClickListener(onclickListener);
        iconHeadShoppingCart.setOnClickListener(onclickListener);
        iconHeadToHome.setOnClickListener(onclickListener);
    }
    protected void loadTopUi(ViewStub viewStub){
        viewStub.setLayoutResource(R.layout.stub_details_top_can_buy);
        if (viewTop == null) viewTop = viewStub.inflate();
        tvCurrentPrice = (TextView) viewTop.findViewById(R.id.current_price);
        tvOldPrice = (TextView) viewTop.findViewById(R.id.cost_price);
        tvOldPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        tvFreight = (TextView) viewTop.findViewById(R.id.tv_freight);
        tvNumberSold = (TextView) viewTop.findViewById(R.id.tv_number_sold);
        viewPagerBanner = (ViewPager) viewTop.findViewById(R.id.goods_img_viewpager);
        if(viewPagerBanner instanceof AutoViewPager) ((AutoViewPager) viewPagerBanner).setAuto(false);
        tvBannerPage = (TextView) viewTop.findViewById(R.id.tv_num);
        tvDetailsTitle = (TextView) viewTop.findViewById(R.id.title);
        tvDetailsBrowserCount = (TextView) viewTop.findViewById(R.id.browse_amount);
        tvDetailsContent = (TextView) viewTop.findViewById(R.id.content);
        layoutService = (RelativeLayout) viewTop.findViewById(R.id.rl_serviceUI);
        recyclerViewService = (RecyclerView) viewTop.findViewById(R.id.recyclerView_service);
        recyclerViewService.setLayoutManager(new FullyGridLayoutManager(activity, 3));
        recyclerViewService.setNestedScrollingEnabled(false);
        layoutShop = (RelativeLayout) viewTop.findViewById(R.id.goto_shop);
        ivShopAvatar = (CircleImageView) viewTop.findViewById(R.id.shop_head_portrait);
        tvShopName = (TextView) viewTop.findViewById(R.id.shop_name);
        tvCouponPrice= (TextView) viewTop.findViewById(R.id.tv_coupon_price);

        countdownView = (CountdownView) viewTop.findViewById(R.id.view_countdown);

        layoutShop.setOnClickListener(onclickListener);
        if(data == null) viewTop.setVisibility(View.GONE);
    }
    protected void loadStivkyUi(ViewStub oneView,ViewStub twoView){}
    protected void loadFooterUi(ViewStub viewStub){}
    protected void loadMenuUi(ViewStub viewStub){}
    public void updateTopUi(){
        if (data == null) return;
        if("3".equals(data.showStatus)){
            countdownView.setVisibility(View.GONE);
            stopSnapUp();
        }else{
            countdownView.addCountDownListenter(new CountdownView.CountDownListener() {
                @Override
                public void startTime(String timeType) {
                    if(countdownView.StartTimeType.equals(timeType)){//开始抢购之前：倒计时 开始计时  实体商品---能加入购物车，但不能购买
                        startSnapUpBefore();
                    }else if(countdownView.EndTimeType.equals(timeType)){//结束抢购之前：倒计时 开始计时 实体商品---能加入购物车，也能购买
                        if(totalStock != 0) startSnapUp();
                    }
                }
                @Override
                public void finishTime(String timeType,boolean hasShow) {
                    if(countdownView.StartTimeType.equals(timeType)){//开始抢购: 倒计时 结束计时  实体商品---能加入购物车，也能购买
                        if(totalStock != 0) startSnapUp();
                    }else if(countdownView.EndTimeType.equals(timeType)){//结束抢购: 倒计时 结束计时  实体商品---不能加入购物车，也不能购买
                        stopSnapUp();
                    }
                }
            });
            countdownView.setVisibility(View.VISIBLE);
            countdownView.setStartTime("1".equals(data.showStartTimeActivity),data.showStartTime);
            countdownView.setEndTime("1".equals(data.showEndTimeActivity),data.showEndTime);
            countdownView.startCountDown();
        }

        tvCurrentPrice.setText(CommonUtils.addMoneySymbol(data.realityPrice));
        tvOldPrice.setText(CommonUtils.addMoneySymbol(data.showPrice));
        tvFreight.setText(CommonUtils.addMoneySymbol(data.showFreightTitle));
        tvNumberSold.setText(data.showSalesTitle);

        if("1".equals(data.showType)){
            layoutShop.setVisibility(View.VISIBLE);
            layoutShop.setTag(data.shopId);
            ImageLoadHelper.getInstance().displayImageHead(activity, data.shopImage, ivShopAvatar);
            tvShopName.setText(data.shopNickname);
        }else {
            tvCurrentPrice.setVisibility(View.GONE);
            tvCouponPrice.setVisibility(View.VISIBLE);
            tvFreight.setVisibility(View.GONE);
            layoutShop.setVisibility(View.GONE);
            tvCouponPrice.setText(data.realityPrice);
        }

        tvDetailsTitle.setText(data.showName);
        tvDetailsBrowserCount.setText(data.browseNumber+"次浏览");
        tvDetailsContent.setText(data.showDescription);

        adapterBanner = new AutoBrowseAdapter(activity, data.showImages);
        viewPagerBanner.setAdapter(adapterBanner);
        viewPagerBanner.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                tvBannerPage.setText(position + 1 + "/" + adapterBanner.getCount());
            }
            @Override
            public void onPageSelected(int position) {}
            @Override
            public void onPageScrollStateChanged(int state) {}
        });
        tvBannerPage.setText("1/" + data.showImages.size());

        /*******商家服务项目*******/
        List<ClassifyDetail.ServiceInfo> serviceInfos = data.serviceList;
        if (serviceInfos != null && serviceInfos.size() > 0) {
            layoutService.setVisibility(View.VISIBLE);
            serviceInfos = serviceInfos.size() > 3 ? serviceInfos.subList(0, 3) : serviceInfos;
            recyclerViewService.setAdapter(new BaseQuickAdapter<ClassifyDetail.ServiceInfo>(R.layout.item_custom_detail_service, serviceInfos) {
                @Override
                protected void convert(BaseViewHolder helper, ClassifyDetail.ServiceInfo item) {
                    ImageLoadHelper.getInstance().displayImage(activity, item.photo, (ImageView) helper.getView(R.id.service_img));
                    helper.setText(R.id.type_of_service, item.name);
                }
            });
            layoutService.setTag(serviceInfos);
            layoutService.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<ClassifyDetail.ServiceInfo> serviceInfos = (List<ClassifyDetail.ServiceInfo>) v.getTag();
                    MobclickAgent.onEvent(activity, "detailServiceClick");
                    showPopDialog(serviceInfos);
                }
            });
        } else {
            layoutService.setVisibility(View.GONE);
        }
    }
    private void showPopDialog(final List<ClassifyDetail.ServiceInfo> serviceInfos) {
        if (serviceInfos == null) return;
        DialogHelper.showBottomDialog(activity, R.layout.dialog_custom_furniture_details, new DialogHelper.OnEventListener<Dialog>() {
            @Override
            public void eventListener(View parentView, final Dialog window) {
                CommonAdapter adapter = new CommonAdapter<ClassifyDetail.ServiceInfo>(activity,
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
                                MobclickAgent.onEvent(activity, "serviceLearnMoreClick");
                                String url = (String) v.getTag();
                                Map<String, Object> param = new HashMap<String, Object>();
                                param.put("url", url);
                                ((BaseActivity)activity).startActivityAnimGeneral(CommWebViewActivity.class, param);
                            }
                        });
                        helper.setText(R.id.custom_service_name, data.name);
                        helper.setText(R.id.custom_service_content, data.remark);
                        ImageLoadHelper.getInstance().displayImage(activity, data.photo, (ImageView) helper.getView(R.id.custom_service_img), LocalDisplay.dp2px(20), LocalDisplay.dp2px(20));
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

    public void updateHeadUi(boolean isUnreadMsg){
        isMsg = isUnreadMsg;
        if (ContextCompat.getDrawable(activity, R.drawable.chat_red).getConstantState().equals(iconHeadMsgTip.getDrawable().getCurrent().getConstantState())
                || ContextCompat.getDrawable(activity, R.drawable.icon_details_top_chat).getConstantState().equals(iconHeadMsgTip.getDrawable().getCurrent().getConstantState())) {
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
    public abstract void updateStivkyUi();
    public abstract void updateFooterUi();
    public abstract void updateMenuUi();
    public abstract void startSnapUpBefore();
    public abstract void startSnapUp();
    public abstract void stopSnapUp();
    public void detachView(){
        if(subscription!=null) subscription.unsubscribe();
        countdownView.cancel();
        if(mImmersionBar != null) mImmersionBar.destroy();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){}
    protected View.OnClickListener onclickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.iv_back:
                    activity.finshActivity();
                    break;
                case R.id.home_page:
                    Map<String, Object> p = new HashMap<>();
                    p.put("source", "mHomePage");
                    activity.startActivityAnimGeneral(MainActivity.class, p);
                    break;
                case R.id.information:
                    if (SPUtils.isLogin()) {
                        activity.startActivityAnimGeneral(MessageCenterActivity.class, null);
                    } else {
                        activity.startActivityAnimGeneral(FasterLoginActivity.class, null);
                    }
                    break;
                case R.id.goto_shop:
                    String shopId = (String) v.getTag();
                    if (TextUtils.isEmpty(shopId)) return;
                    MobclickAgent.onEvent(activity, "shopBtnClick");
                    Map<String, Object> param = new HashMap<>();
                    param.put("shopId", shopId);
                    param.put("position", 0);
                    activity.startActivityAnimGeneral(ShopActivityF.class, param);
                    break;
                case R.id.iv_shopping_cart:
                    MobclickAgent.onEvent(activity,"product_clickShopcarticon");
                    if (SPUtils.isLogin()) {
                        activity.startActivityAnimGeneral(ShoppingCartActivityF.class,null);
                    } else {
                        activity.startActivityAnimGeneral(FasterLoginActivity.class, null);
                    }
                    break;
            }
        }
    };

    Subscription subscription;
    public void getShoppingCartCount() {
        if(!SPUtils.isLogin()){
            showCartCount.setText("");
            return;
        }
        Map<String,Object> params = new HashMap<>();
        params.put("userId", ConstantObj.TEMP_USERID);
        if (subscription != null) subscription.unsubscribe();
        subscription= RxRequestApi.getInstance().getApiService().getShoppingCartCount(params)
                .observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
                .subscribe(new Action1<ResponseBody<AffectedNumber>>() {
                    @Override
                    public void call(ResponseBody<AffectedNumber> body) {
                        if (body.isSuccess1()) {
                            int shoppingCartCount = body.data.getCount();
                            showCartCount.setText(shoppingCartCount > 99 ? "99+" : shoppingCartCount + "");
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Tst.showToast(throwable.getMessage());
                    }
                });
    }

    public void updateData(GoodsDetails.CommodityShowBoBean data){
        this.data=data;
    }
}
