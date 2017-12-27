package com.knms.activity.goods;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshHeaderViewPager;
import com.knms.activity.base.HeadNotifyBaseFragmentActivity;
import com.knms.adapter.AdvertisementAdapter;
import com.knms.adapter.CustomFurniturePagerAdapter;
import com.knms.bean.IndexAd;
import com.knms.bean.ResponseBody;
import com.knms.bean.other.CustomData;
import com.knms.bean.other.CustomType;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.fragment.CustomFurnitureFragment;
import com.knms.net.RxApiService;
import com.knms.net.RxRequestApi;
import com.knms.util.ConstantObj;
import com.knms.util.LocalDisplay;
import com.knms.util.ScreenUtil;
import com.knms.util.ToolsHelper;
import com.knms.util.Tst;
import com.knms.view.PagerSlidingTabStrip;
import com.knms.android.R;
import com.knms.view.banner.AutoViewPager;
import com.knms.view.banner.CirclePageIndicator;
import com.knms.view.sticky.HeaderScrollHelper;
import com.knms.view.sticky.HeaderViewPager;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tdx on 2016/9/2.
 * 定制家具
 */
public class CustomFurnitureActivityF extends HeadNotifyBaseFragmentActivity {
    PagerSlidingTabStrip tabStrip;
    ViewPager vp;
    private String typeId;
    private CustomFurniturePagerAdapter fg_adapter;
    private PullToRefreshHeaderViewPager refresh;

    private RxApiService apiService;
    private CompositeSubscription mSubscriptions;
    private ImageButton btnTop;
    private boolean isFrist = true;

    private FrameLayout customBannerLayout;

    @Override
    protected void getParmas(Intent intent) {
        typeId = intent.getStringExtra("typeId");
    }

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("定制家具");
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_custom_furniture;
    }

    @Override
    protected void initView() {
        refresh = findView(R.id.pull_to_refresh);
        tabStrip = findView(R.id.id_stickynavlayout_indicator);
        vp = findView(R.id.id_stickynavlayout_viewpager);
        refresh.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        btnTop = (ImageButton) findViewById(R.id.btn_top);
        init();
    }

    @Override
    protected void initData() {
        refresh.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<HeaderViewPager>() {
            @Override
            public void onRefresh(PullToRefreshBase<HeaderViewPager> refreshView) {
                if(!isFrist) ((CustomFurnitureFragment)fg_adapter.getItem(vp.getCurrentItem())) .refresh(true);
                reqApi();
            }
        });
        refresh.postDelayed(new Runnable() {
            @Override
            public void run() {
                refresh.setRefreshing();
            }
        }, 500);

        refresh.getRefreshableView().setOnScrollListener(new HeaderViewPager.OnScrollListener() {
            @Override
            public void onScroll(int currentY, int maxY) {
                btnTop.setVisibility(currentY==maxY?View.VISIBLE:View.GONE);
            }
        });

//        refresh.getRefreshableView().setOnStickStateChangeListener(new StickyNavLayout.onStickStateChangeListener() {
//            @Override
//            public void isStick(boolean isStick) {
//                btnTop.setVisibility(isStick ? View.VISIBLE : View.GONE);
//            }
//
//            @Override
//            public void scrollPercent(float percent) {
//
//            }
//        });

        btnTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RxBus.get().post(BusAction.REFRESH_IDLE, false);
                refresh.getRefreshableView().scrollTo(0, 0);
            }
        });
    }

    @Override
    public String setStatisticsTitle() {
        return "定制家具";
    }

    private void init() {
        customBannerLayout = findView(R.id.activity_custom_banner);
        customBannerLayout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, ScreenUtil.getScreenWidth() / 2));
        mSubscriptions = new CompositeSubscription();
        apiService = RxRequestApi.getInstance().getApiService();
        tabStrip.setAllCaps(true);
        tabStrip.setIndicatorColor(getResources().getColor(R.color.no_color));
        tabStrip.setShowSelectedTabBg(true);
    }

    @Override
    protected void reqApi() {
        mSubscriptions.add(Observable.zip(apiService.getCustomizedType(), apiService.getAdvertisement("D"),
                new Func2<ResponseBody<List<CustomType>>, ResponseBody<IndexAd>, CustomData>() {
                    @Override
                    public CustomData call(ResponseBody<List<CustomType>> listResponseBody, ResponseBody<IndexAd> indexAdResponseBody) {
                        CustomData data = new CustomData();
                        data.code = ConstantObj.OK;
                        if (listResponseBody.isSuccess()) {
                            data.customtypeList = listResponseBody.data;
                        } else {
                            data.code = listResponseBody.code;
                            data.desc = listResponseBody.desc;
                        }
                        if (indexAdResponseBody.isSuccess()) {
                            data.indexAd = indexAdResponseBody.data;
                        } else {
                            data.code = indexAdResponseBody.code;
                            data.desc = indexAdResponseBody.desc;
                        }
                        if (!listResponseBody.isSuccess() && !indexAdResponseBody.isSuccess()) {
                            data.code = "-1";
                            data.desc = listResponseBody.desc + "&" + indexAdResponseBody.desc;
                        }
                        return data;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<CustomData>() {
                    @Override
                    public void call(CustomData o) {
                        refresh.onRefreshComplete();
                        if (o.customtypeList != null) {
                            updateView(o.customtypeList);
                        } else Tst.showToast(o.desc);
                        if (o.indexAd != null) updateBanner(o.indexAd);
                        else {
                            customBannerLayout.setVisibility(View.GONE);
                            Tst.showToast(o.desc);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        refresh.onRefreshComplete();
                        Tst.showToast(throwable.getMessage());
                    }
                }));
    }

    private void updateBanner(IndexAd indexAd) {
        if (indexAd.d1 == null || indexAd.d1.size() == 0) {
            customBannerLayout.setVisibility(View.GONE);
            return;
        }else customBannerLayout.setVisibility(View.VISIBLE);
        ToolsHelper.getInstance().sort(indexAd.d1, "imseq");
        AutoViewPager vp_banner_advertisement = (AutoViewPager) findViewById(R.id.vp_banner_advertisement);
        vp_banner_advertisement.setmTimer(7000);
        vp_banner_advertisement.setBoundaryLooping(indexAd.d1.size()!=1);
        CirclePageIndicator cpi_banner_advertisement = (CirclePageIndicator) findViewById(R.id.cpi_banner_advertisement);
        vp_banner_advertisement.setAdapter(new AdvertisementAdapter(this, indexAd.d1, LocalDisplay.dp2px(150)));
        vp_banner_advertisement.setOffscreenPageLimit(2);
        cpi_banner_advertisement.setViewPager(vp_banner_advertisement);
    }

    private void updateView(List<CustomType> data) {
        if(!isFrist)return;
        ToolsHelper.getInstance().sort(data, "seq");
        int position = vp.getCurrentItem();
        fg_adapter = new CustomFurniturePagerAdapter(getSupportFragmentManager(), data);
        //关闭预加载，默认一次只加载一个Fragment
//        vp.setOffscreenPageLimit(1);
        vp.setAdapter(fg_adapter);
        tabStrip.setViewPager(vp);
        if (isFrist) {
            if (!TextUtils.isEmpty(typeId)) {
                for (int i = 0; i < data.size(); i++) {
                    if (data.get(i).id.equals(typeId)) {
                        position = i;
                        break;
                    }
                }
            }
            isFrist = false;
        }
        vp.setCurrentItem(position);

        vp.setCurrentItem(position);

        refresh.getRefreshableView().setCurrentScrollableContainer((HeaderScrollHelper.ScrollableContainer) fg_adapter.getItem(position));
        vp.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                refresh.getRefreshableView().setCurrentScrollableContainer((HeaderScrollHelper.ScrollableContainer) fg_adapter.getItem(position));
            }
        });

    }
}
