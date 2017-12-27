package com.knms.activity.main;

import android.app.Dialog;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseViewHolder;
import com.gyf.barlibrary.ImmersionBar;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshNestedScrollView;
import com.knms.activity.ScanQRCodeActivity;
import com.knms.activity.base.BaseActivity;
import com.knms.activity.fastfind.QuicklyGoodsAcitvity;
import com.knms.activity.goods.BaokMallActivity;
import com.knms.activity.goods.CustomFurnitureActivityF;
import com.knms.activity.goods.DiyInspirationActivity;
import com.knms.activity.goods.IdleFurnitureActivity;
import com.knms.activity.im.MessageCenterActivity;
import com.knms.activity.login.FasterLoginActivity;
import com.knms.activity.repair.RepairListActivity;
import com.knms.activity.search.SearchActivity;
import com.knms.adapter.AdvertisementAdapter;
import com.knms.adapter.IndexMenuAdapter;
import com.knms.adapter.NestedScrollViewOverScrollDecorAdapter;
import com.knms.adapter.baserecycler.BaseQuickAdapter;
import com.knms.adapter.index.IndexActAdapter;
import com.knms.adapter.index.IndexCustomFurnitureAdapter;
import com.knms.adapter.index.IndexStyleAdapter;
import com.knms.android.R;
import com.knms.bean.IndexAd;
import com.knms.bean.IndexData;
import com.knms.bean.IndexIdle;
import com.knms.bean.ResponseBody;
import com.knms.bean.product.Ad;
import com.knms.bean.product.Menu;
import com.knms.core.im.IMHelper;
import com.knms.net.RxApiService;
import com.knms.net.RxRequestApi;
import com.knms.oncall.BannerOnclick;
import com.knms.other.RetrofitCache;
import com.knms.util.ConstantObj;
import com.knms.util.DialogHelper;
import com.knms.util.ImageLoadHelper;
import com.knms.util.LocalDisplay;
import com.knms.util.SPUtils;
import com.knms.util.ScreenUtil;
import com.knms.util.ToolsHelper;
import com.knms.util.Tst;
import com.knms.view.banner.AutoViewPager;
import com.knms.view.banner.CirclePageIndicator;
import com.knms.view.clash.FullyGridLayoutManager;
import com.knms.view.clash.FullyLinearLayoutManager;
import com.umeng.analytics.MobclickAgent;

import me.everything.android.ui.overscroll.VerticalOverScrollBounceEffectDecorator;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tdx on 2016/8/25.
 */
public class HomePageActivityF extends BaseActivity {
    private RxApiService apiService;
    private CompositeSubscription mSubscriptions;
    private PullToRefreshNestedScrollView mRefreshScrollView;
    private ViewStub vs_banner_advertisement, vs_menu, vs_product_special, vs_mstj, vs_product_home_style, vs_banner_custom,
            vs_ad_a8, vs_ad_a9, vs_product_act,vs_product_idle, vs_knowledge,vs_ad_a12;
    @Override
    protected int layoutResID() {
        return R.layout.activity_tab_indexpage;
    }

    @Override
    protected void initView() {
        mRefreshScrollView = findView(R.id.nsv_indexpage);
        initHeadView();
        initViewStub();
    }

    @Override
    protected void initBar() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            View barView = findView(R.id.view_sbo);
            ViewGroup.LayoutParams lp = barView.getLayoutParams();
            lp.height = ImmersionBar.getStatusBarHeight(this);
            barView.setLayoutParams(lp);
        }
    }
    private void initHeadView() {
        //扫一扫
        findView(R.id.rich_scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityAnimGeneral(ScanQRCodeActivity.class, null);
            }
        });
        //搜一搜
        findView(R.id.rlBtn_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityAnimGeneral(SearchActivity.class, null);
            }
        });
        //消息
        findView(R.id.information).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SPUtils.isLogin()) {
                    startActivityAnimGeneral(MessageCenterActivity.class, null);
                } else {
                    startActivityAnimGeneral(FasterLoginActivity.class, null);
                }
            }
        });
        findView(R.id.rl_border).setAlpha(0.0f);//设置透明度
        findView(R.id.rlBtn_search).setAlpha(0.7f);
        findView(R.id.rl_border).setBackgroundResource(R.color.color_white);

        ((ImageView) findView(R.id.rich_scan)).setImageResource(R.drawable.icon_scan);
    }

    Subscription subscriptionMsgCount;
    boolean isAlpha = true;//是否处于透明区域

    @Override
    protected void onResume() {
        super.onResume();
        if (subscriptionMsgCount != null) subscriptionMsgCount.unsubscribe();
        subscriptionMsgCount = IMHelper.getInstance().isUnreadMsg().subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean aBoolean) {
                if (aBoolean)
                    ((ImageView) findView(R.id.information)).setImageResource(isAlpha ? R.drawable.home_05 : R.drawable.home_03);
                else
                    ((ImageView) findView(R.id.information)).setImageResource(isAlpha ? R.drawable.home_14 : R.drawable.home_12);
            }
        });
    }

    @Override
    protected void initData() {
        mSubscriptions = new CompositeSubscription();
        apiService = RxRequestApi.getInstance().getApiService();
        mRefreshScrollView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        mRefreshScrollView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<NestedScrollView>() {
            @Override
            public void onRefresh(PullToRefreshBase<NestedScrollView> refreshView) {
                reqApi();
            }
        });
        mRefreshScrollView.getRefreshableView().setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (isBanner) {
                    float alpha = (float) (scrollY + LocalDisplay.dp2px(2)) / (float) (ScreenUtil.getScreenWidth()*3/5 + LocalDisplay.dp2px(2));
                    setHeadAlPha(alpha);
                } else {
                    isAlpha = false;
                    findView(R.id.rl_border).setAlpha(1f);
                    findView(R.id.rlBtn_search).setAlpha(1f);
                    findView(R.id.rl_border).setBackgroundResource(R.color.color_white);
                    ((ImageView) findView(R.id.rich_scan)).setImageResource(R.drawable.qie_83);
                }
            }
        });
        mRefreshScrollView.setRefreshing();
    }
    @Override
    public String setStatisticsTitle() {
        return "首页";
    }

    private void setHeadAlPha(float alpha) {
        NestedScrollView scrollView = mRefreshScrollView.getRefreshableView();
        Rect scrollBounds = new Rect();
        scrollView.getHitRect(scrollBounds);
        if (view_banner != null) {
            if (view_banner.getLocalVisibleRect(scrollBounds)) {
                //子控件至少有一个像素在可视范围内
                if (!isAlpha) onResume();
                findView(R.id.rl_border).setAlpha(alpha);
                findView(R.id.rlBtn_search).setAlpha(0.7f + alpha);
                findView(R.id.rl_border).setBackgroundResource(R.color.color_white);
                ((ImageView) findView(R.id.rich_scan)).setImageResource(R.drawable.icon_scan);
                isAlpha = true;
            } else {
                //子控件完全不在可视范围内
                if (isAlpha) onResume();
                findView(R.id.rl_border).setAlpha(1f);
                findView(R.id.rlBtn_search).setAlpha(1f);
                findView(R.id.rl_border).setBackgroundResource(R.color.color_white);
                ((ImageView) findView(R.id.rich_scan)).setImageResource(R.drawable.qie_83);
                isAlpha = false;
            }
        }
    }

    @Override
    protected void reqApi() {
        mSubscriptions.add(Observable.zip(RetrofitCache.load("indexData",apiService.getIndexData()), RetrofitCache.load("getAdvertisementA",apiService.getAdvertisement("A")),
                new Func2<ResponseBody<IndexIdle>, ResponseBody<IndexAd>, IndexData>() {
                    @Override
                    public IndexData call(ResponseBody<IndexIdle> body1, ResponseBody<IndexAd> body2) {
                        IndexData data = new IndexData();
                        if (body1.isSuccess()) {
                            data.indexIdle = body1.data;
                        }
                        if (body2.isSuccess()) {
                            data.indexAd = body2.data;
                        }
                        data.code = body1.code;
                        data.desc = body1.desc;
                        return data;
                    }
                }).doOnError(new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                mRefreshScrollView.onRefreshComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<IndexData>() {
                    @Override
                    public void call(IndexData body) {
                        mRefreshScrollView.onRefreshComplete();
//                        v_place_holder.setVisibility(View.GONE);
                        if(ConstantObj.OK.equals(body.code)){
                            new VerticalOverScrollBounceEffectDecorator(new NestedScrollViewOverScrollDecorAdapter(mRefreshScrollView.getRefreshableView()));
                            if (body.indexIdle != null) updataViewSelect(body.indexIdle);
                            if (body.indexAd != null) updataViewAds(body.indexAd);
                        }else{
                            Tst.showToast(body.desc);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }));
    }

    View view_banner, view_product_special,view_mstj, view_product_home_style, view_product_act, view_banner_custom, view_knowledge, view_ad_a8, view_ad_a9, view_ad_a10,view_ad_a12;
    boolean isBanner = true;//是否含有banner

    private void updataViewAds(final IndexAd indexAd) {
        if (indexAd == null) return;
        //banner广告
        if (indexAd.a1 != null && indexAd.a1.size() > 0) {
            isBanner = true;
            ToolsHelper.getInstance().sort(indexAd.a1, "imseq");
            if (view_banner == null) view_banner = vs_banner_advertisement.inflate();
            if (view_banner != null) view_banner.setVisibility(View.VISIBLE);
            view_banner.setLayoutParams(new LinearLayout.LayoutParams(ScreenUtil.getScreenWidth(),ScreenUtil.getScreenWidth()/5*3));
            AutoViewPager vp_banner_advertisement = (AutoViewPager) view_banner.findViewById(R.id.vp_banner_advertisement);
            vp_banner_advertisement.setmTimer(7000);
            CirclePageIndicator cpi_banner_advertisement = (CirclePageIndicator) view_banner.findViewById(R.id.cpi_banner_advertisement);
            vp_banner_advertisement.setAdapter(new AdvertisementAdapter(this,indexAd.a1, LocalDisplay.dp2px(231),true));
            vp_banner_advertisement.setOffscreenPageLimit(2);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.BOTTOM | Gravity.RIGHT;
            cpi_banner_advertisement.setLayoutParams(lp);
            cpi_banner_advertisement.setViewPager(vp_banner_advertisement);
        } else {
            if (view_banner != null) view_banner.setVisibility(View.GONE);
//            v_place_holder.setVisibility(View.VISIBLE);
            isBanner = false;
        }
        //活动专区 极速找货
        if(!(indexAd.aj1 != null && indexAd.aj1.size() > 0) &&
                !(indexAd.ah1 != null && indexAd.ah1.size() > 0)){
            if (view_ad_a12 != null) view_ad_a12.setVisibility(View.GONE);
        }else{
            if (view_ad_a12 == null) view_ad_a12 = vs_ad_a12.inflate();
            if (view_ad_a12 != null) view_ad_a12.setVisibility(View.VISIBLE);

            LinearLayout ll_ad = (LinearLayout) view_ad_a12.findViewById(R.id.ll_a10);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) ll_ad.getLayoutParams();
            lp.height = ScreenUtil.getScreenWidth() / 5;
            ll_ad.setLayoutParams(lp);

            if(indexAd.ah1 != null && indexAd.ah1.size() > 0){
                ImageView iv_ad_a = (ImageView) view_ad_a12.findViewById(R.id.iv_ad_10_a);
                ImageLoadHelper.getInstance().displayImage(this,indexAd.ah1.get(0).name, iv_ad_a);
                iv_ad_a.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BannerOnclick.advertisementClick(v.getContext(),indexAd.ah1.get(0));
                    }
                });
            }

            if(indexAd.aj1 != null && indexAd.aj1.size() > 0){
                ImageView iv_ad_b = (ImageView) view_ad_a12.findViewById(R.id.iv_ad_10_b);
                iv_ad_b.setScaleType(ImageView.ScaleType.CENTER_CROP);
                ImageLoadHelper.getInstance().displayImage(this,indexAd.aj1.get(0).name, iv_ad_b);
                iv_ad_b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        MobclickAgent.onEvent(HomePageActivityF.this, "quickCompareMainClick");
                        startActivityAnimGeneral(QuicklyGoodsAcitvity.class,null);
                    }
                });
            }
        }
        if (indexAd.aj1 != null && indexAd.aj1.size() > 0) {
            if (view_ad_a12 == null) view_ad_a12 = vs_ad_a12.inflate();
            if (view_ad_a12 != null) view_ad_a12.setVisibility(View.VISIBLE);

            LinearLayout ll_ad = (LinearLayout) view_ad_a12.findViewById(R.id.ll_a10);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) ll_ad.getLayoutParams();
            lp.height = ScreenUtil.getScreenWidth() / 5;
            ll_ad.setLayoutParams(lp);



        } else {
            if (view_ad_a12 != null) view_ad_a12.setVisibility(View.GONE);
        }

        //买手特价
        if(!(indexAd.am1 != null && indexAd.am1.size() > 0) &&
                !(indexAd.am2 != null && indexAd.am2.size() > 0) &&
                !(indexAd.am3 != null && indexAd.am3.size() > 0) &&
                !(indexAd.am4 != null && indexAd.am4.size() > 0) &&
                !(indexAd.am5 != null && indexAd.am5.size() > 0)){//若全部为空
            if (view_mstj != null) view_mstj.setVisibility(View.GONE);//隐藏
        }else{
            if(view_mstj == null) view_mstj = vs_mstj.inflate();
            if (view_mstj != null) view_mstj.setVisibility(View.VISIBLE);

            if(indexAd.am6 != null && indexAd.am6.size() > 0){
                TextView tvDesc = (TextView) view_mstj.findViewById(R.id.tv_mstj_desc);
                tvDesc.setText(indexAd.am6.get(0).cotitle);
            }

            LinearLayout ll_mstj_1 = (LinearLayout) view_mstj.findViewById(R.id.ll_mstj_1);
            LinearLayout ll_mstj_2 = (LinearLayout) view_mstj.findViewById(R.id.ll_mstj_2);
            LinearLayout.LayoutParams lp1 = (LinearLayout.LayoutParams) ll_mstj_1.getLayoutParams();
            lp1.height = ScreenUtil.getScreenWidth() * 16 / 50;
            ll_mstj_1.setLayoutParams(lp1);

            LinearLayout.LayoutParams lp2 = (LinearLayout.LayoutParams) ll_mstj_2.getLayoutParams();
            lp2.height = ScreenUtil.getScreenWidth() * 24 / 75;
            ll_mstj_2.setLayoutParams(lp2);

            if(indexAd.am1 != null && indexAd.am1.size() > 0){
                ImageView imageView1 = (ImageView) view_mstj.findViewById(R.id.iv_ad_mstj_1);
                ImageLoadHelper.getInstance().displayImage(this,indexAd.am1.get(0).name,imageView1);
                imageView1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BannerOnclick.advertisementClick(v.getContext(),indexAd.am1.get(0));
                    }
                });
            }

            if(indexAd.am2 != null && indexAd.am2.size() > 0){
                ImageView imageView2 = (ImageView) view_mstj.findViewById(R.id.iv_ad_mstj_2);
                ImageLoadHelper.getInstance().displayImage(this,indexAd.am2.get(0).name,imageView2);
                imageView2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BannerOnclick.advertisementClick(v.getContext(),indexAd.am2.get(0));
                    }
                });
            }

            if(indexAd.am3 != null && indexAd.am3.size() > 0){
                ImageView imageView3 = (ImageView) view_mstj.findViewById(R.id.iv_ad_mstj_3);
                ImageLoadHelper.getInstance().displayImage(this,indexAd.am3.get(0).name,imageView3);
                imageView3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BannerOnclick.advertisementClick(v.getContext(),indexAd.am3.get(0));
                    }
                });
            }

            if(indexAd.am4 != null && indexAd.am4.size() > 0){
                ImageView imageView4 = (ImageView) view_mstj.findViewById(R.id.iv_ad_mstj_4);
                ImageLoadHelper.getInstance().displayImage(this,indexAd.am4.get(0).name,imageView4);
                imageView4.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BannerOnclick.advertisementClick(v.getContext(),indexAd.am4.get(0));
                    }
                });
            }

            if(indexAd.am5 != null && indexAd.am5.size() > 0){
                ImageView imageView5 = (ImageView) view_mstj.findViewById(R.id.iv_ad_mstj_5);
                ImageLoadHelper.getInstance().displayImage(this,indexAd.am5.get(0).name,imageView5);
                imageView5.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BannerOnclick.advertisementClick(v.getContext(),indexAd.am5.get(0));
                    }
                });
            }

            view_mstj.findViewById(R.id.rlBtn_mstj).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MobclickAgent.onEvent(HomePageActivityF.this, "homePageRecommendGoodsListClick");
//                    startActivityAnimGeneral(MallActivity.class, null);//TODO 支付商城系统-商品列表
                    startActivityAnimGeneral(BaokMallActivity.class, null);
                }
            });
        }

        // <!--爆款商品-->
        if(!(indexAd.ab1 != null && indexAd.ab1.size() > 0) &&
           !(indexAd.ab2 != null && indexAd.ab2.size() > 0) &&
           !(indexAd.ab3 != null && indexAd.ab3.size() > 0) &&
           !(indexAd.ab4 != null && indexAd.ab4.size() > 0) &&
           !(indexAd.ab5 != null && indexAd.ab5.size() > 0)){//若全部为空
            if (view_product_special != null) view_product_special.setVisibility(View.GONE);//隐藏
        }else{
            if (view_product_special == null) view_product_special = vs_product_special.inflate();
            if (view_product_special != null) view_product_special.setVisibility(View.VISIBLE);

            if(indexAd.ab6 != null && indexAd.ab6.size() > 0){
                TextView tvDesc = (TextView) view_product_special.findViewById(R.id.tv_special_desc);
                tvDesc.setText(indexAd.ab6.get(0).cotitle);
            }

            LinearLayout ll_special = (LinearLayout) view_product_special.findViewById(R.id.ll_special);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) ll_special.getLayoutParams();
            lp.height = ScreenUtil.getScreenWidth() * 14 / 25;
            ll_special.setLayoutParams(lp);

            if(indexAd.ab1 != null && indexAd.ab1.size() > 0){
                ImageView imageView1 = (ImageView) view_product_special.findViewById(R.id.iv_special_1);
                ImageLoadHelper.getInstance().displayImage(this,indexAd.ab1.get(0).name,imageView1);
                imageView1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BannerOnclick.advertisementClick(v.getContext(),indexAd.ab1.get(0));
                    }
                });
            }

            if(indexAd.ab2 != null && indexAd.ab2.size() > 0){
                ImageView imageView2 = (ImageView) view_product_special.findViewById(R.id.iv_special_2);
                ImageLoadHelper.getInstance().displayImage(this,indexAd.ab2.get(0).name,imageView2);
                imageView2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BannerOnclick.advertisementClick(v.getContext(),indexAd.ab2.get(0));
                    }
                });
            }

            if(indexAd.ab3 != null && indexAd.ab3.size() > 0){
                ImageView imageView3 = (ImageView) view_product_special.findViewById(R.id.iv_special_3);
                ImageLoadHelper.getInstance().displayImage(this,indexAd.ab3.get(0).name,imageView3);
                imageView3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BannerOnclick.advertisementClick(v.getContext(),indexAd.ab3.get(0));
                    }
                });
            }

            if(indexAd.ab4 != null && indexAd.ab1.size() > 0){
                ImageView imageView4 = (ImageView) view_product_special.findViewById(R.id.iv_special_4);
                ImageLoadHelper.getInstance().displayImage(this,indexAd.ab4.get(0).name,imageView4);
                imageView4.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BannerOnclick.advertisementClick(v.getContext(),indexAd.ab4.get(0));
                    }
                });
            }

            if(indexAd.ab5 != null && indexAd.ab5.size() > 0){
                ImageView imageView5 = (ImageView) view_product_special.findViewById(R.id.iv_special_5);
                ImageLoadHelper.getInstance().displayImage(this,indexAd.ab5.get(0).name,imageView5);
                imageView5.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BannerOnclick.advertisementClick(v.getContext(),indexAd.ab5.get(0));
                    }
                });
            }
            // moreAd.url = "com.kebuyer.user://h5.kebuyer.com/a/?module=activitylist";
            view_product_special.findViewById(R.id.rlBtn_baok).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BannerOnclick.advertisementClick(v.getContext(),indexAd.ab1.get(0));
                }
            });
        }

        // <!--定制家具-->
        if(indexAd.an2 != null && indexAd.an2.size() > 0){
            if (view_banner_custom == null) view_banner_custom = vs_banner_custom.inflate();
            if (view_banner_custom != null) view_banner_custom.setVisibility(View.VISIBLE);
            RecyclerView recyclerView = (RecyclerView) view_banner_custom.findViewById(R.id.recyclerView);
            recyclerView.setNestedScrollingEnabled(false);
            recyclerView.setLayoutManager(new FullyGridLayoutManager(this,2));
            IndexCustomFurnitureAdapter adapter = new IndexCustomFurnitureAdapter(indexAd.an2);
            recyclerView.setAdapter(adapter);
            adapter.setOnItemClickListener(new com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener<Ad>() {
                @Override
                public void onItemClick(com.chad.library.adapter.base.BaseQuickAdapter<Ad, ? extends BaseViewHolder> adapter, View view, int position) {
                    MobclickAgent.onEvent(HomePageActivityF.this, "homePageCustomFurnitureClassifyClick");
                    Ad item = adapter.getItem(position);
                    BannerOnclick.advertisementClick(HomePageActivityF.this,item);
                }
            });
            view_banner_custom.findViewById(R.id.rlBtn_custom).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MobclickAgent.onEvent(HomePageActivityF.this, "homePageCustomFurnitureListClick");
                    startActivityAnimGeneral(CustomFurnitureActivityF.class,null);
                }
            });
        }else{
            if (view_banner_custom != null) view_banner_custom.setVisibility(View.GONE);
        }
        //<!--家装风格-->
       if (indexAd.an4 != null && indexAd.an4.size() > 0) {
            ToolsHelper.getInstance().sort(indexAd.an4, "imseq");
            if (view_product_home_style == null) view_product_home_style = vs_product_home_style.inflate();
            if (view_product_home_style != null) view_product_home_style.setVisibility(View.VISIBLE);
            RecyclerView recyclerView = (RecyclerView) view_product_home_style.findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new FullyLinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
            recyclerView.setNestedScrollingEnabled(false);
            IndexStyleAdapter adapter = new IndexStyleAdapter(indexAd.an4);
            recyclerView.setAdapter(adapter);
            adapter.setOnItemClickListener(new com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener<Ad>() {
               @Override
               public void onItemClick(com.chad.library.adapter.base.BaseQuickAdapter<Ad, ? extends BaseViewHolder> adapter, View view, int position) {
                   Ad item = adapter.getItem(position);
                   BannerOnclick.advertisementClick(HomePageActivityF.this,item);
               }
           });
           view_product_home_style.findViewById(R.id.rlBtn_style).setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   startActivityAnimGeneral(DiyInspirationActivity.class,null);
               }
           });
        } else {
            if (view_product_home_style != null) view_product_home_style.setVisibility(View.GONE);
        }
        //<!--买手活动-->
        if(indexAd.an3 != null && indexAd.an3.size() > 0){
            if (view_product_act == null) view_product_act = vs_product_act.inflate();
            if (view_product_act != null) view_product_act.setVisibility(View.VISIBLE);
            RecyclerView recyclerView = (RecyclerView) view_product_act.findViewById(R.id.recyclerView);
            IndexActAdapter adapter = new IndexActAdapter(indexAd.an3);
            recyclerView.setLayoutManager(new FullyLinearLayoutManager(HomePageActivityF.this,LinearLayoutManager.VERTICAL,false));
            recyclerView.setNestedScrollingEnabled(false);
            recyclerView.setAdapter(adapter);
            adapter.setOnItemClickListener(new com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener<Ad>() {
                @Override
                public void onItemClick(com.chad.library.adapter.base.BaseQuickAdapter<Ad, ? extends BaseViewHolder> adapter, View view, int position) {
                    Ad item = adapter.getItem(position);
                    BannerOnclick.advertisementClick(HomePageActivityF.this,item);
                }
            });
            view_product_act.findViewById(R.id.rlBtn_act).setTag(indexAd.an3.get(0));
            view_product_act.findViewById(R.id.rlBtn_act).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Ad act0 = (Ad) v.getTag();
                    if(act0 != null) BannerOnclick.advertisementClick(HomePageActivityF.this, act0);
                }
            });
        }else{
            if (view_product_act != null) view_product_act.setVisibility(View.GONE);
        }
        //广告弹窗
        if (indexAd.a0 != null && indexAd.a0.size() > 0) {
            final String adUrl = indexAd.a0.get(0).name;
            if (!SPUtils.getFromApp(SPUtils.KeyConstant.homePageAd, "").equals(adUrl)) {
                DialogHelper.showAlertDialog(this, R.layout.dialog_homepage_ad, new DialogHelper.OnEventListener<Dialog>() {
                    @Override
                    public void eventListener(View parentView, final Dialog window) {
                        ImageView a0 = (ImageView) parentView.findViewById(R.id.a0);
                        a0.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtil.getScreenWidth()));
                        ImageView closeAd = (ImageView) parentView.findViewById(R.id.iv_a0_close);
                        ImageLoadHelper.getInstance().displayImage(HomePageActivityF.this,adUrl, a0,ScreenUtil.getScreenWidth()-LocalDisplay.dp2px(50),ScreenUtil.getScreenWidth()-LocalDisplay.dp2px(50));
                        a0.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                BannerOnclick.advertisementClick(HomePageActivityF.this, indexAd.a0.get(0));
                                window.dismiss();
                            }
                        });
                        closeAd.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                window.dismiss();
                            }
                        });

                    }
                });
                SPUtils.saveToApp(SPUtils.KeyConstant.homePageAd, adUrl);
            }
        }
    }
    View view_menu, view_product_idle;

    private void updataViewSelect(IndexIdle indexIdle) {
        if (indexIdle == null) return;
        if (indexIdle.menus != null && indexIdle.menus.size() > 0) {
            if (view_menu == null) view_menu = vs_menu.inflate();
            RecyclerView recyclerView_menu = (RecyclerView) view_menu.findViewById(R.id.recycler_view_menu);
            recyclerView_menu.setLayoutManager(new FullyGridLayoutManager(this, indexIdle.menus.size()));//这里用线性宫格显示 类似于gridView
            final IndexMenuAdapter adapter = new IndexMenuAdapter(this,indexIdle.menus);
            recyclerView_menu.setAdapter(adapter);
            adapter.setOnRecyclerViewItemClickListener(new BaseQuickAdapter.OnRecyclerViewItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    Menu item = adapter.getData().get(position);
                    if (ConstantObj.ShoppingMall.equals(item.meid)) {
//                        startActivityAnimGeneral(MallActivity.class, null);//TODO 支付商城系统-商品列表
                        startActivityAnimGeneral(BaokMallActivity.class, null);
                    } else if (ConstantObj.FurnitureInsp.equals(item.meid)) {
                        startActivityAnimGeneral(DiyInspirationActivity.class, null);
                    } else if (ConstantObj.FurnitureCustom.equals(item.meid)) {
                        startActivityAnimGeneral(CustomFurnitureActivityF.class, null);
                    } else if (ConstantObj.FurnitureIdle.equals(item.meid)) {
                        startActivityAnimGeneral(IdleFurnitureActivity.class, null);
                    } else if (ConstantObj.FurnitureRepair.equals(item.meid)) {
                        startActivityAnimGeneral(RepairListActivity.class, null);
                    }
                }
            });
        }
    }

    private void initViewStub() {
//        v_place_holder = findView(R.id.v_place_holder);
        vs_banner_advertisement = findView(R.id.vs_banner_advertisement);
        vs_product_special = findView(R.id.vs_product_special);
        vs_mstj = findView(R.id.vs_mstj);
        vs_product_home_style = findView(R.id.vs_product_home_style);
        vs_banner_custom = findView(R.id.vs_banner_custom);
        vs_product_idle = findView(R.id.vs_product_idle);
        vs_product_act = findView(R.id.vs_product_act);
        vs_ad_a8 = findView(R.id.vs_a8);
        vs_ad_a9 = findView(R.id.vs_a9);
        vs_knowledge = findView(R.id.vs_knowledge);
        vs_menu = findView(R.id.vs_menu);
//        vs_ad_a10 = findView(R.id.vs_a10);
        vs_ad_a12=findView(R.id.vs_a12);
    }

    @Override
    protected void onDestroy() {
        if (mSubscriptions != null) mSubscriptions.unsubscribe();
        super.onDestroy();
    }
}
