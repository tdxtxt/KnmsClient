package com.knms.activity.goods;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewStub;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshHeaderViewPager;
import com.knms.activity.base.HeadNotifyBaseFragmentActivity;
import com.knms.adapter.AdvertisementAdapter;
import com.knms.adapter.fragment.MallFragmentAdapter;
import com.knms.android.R;
import com.knms.bean.IndexAd;
import com.knms.bean.ResponseBody;
import com.knms.bean.mall.MallData;
import com.knms.bean.other.Tab;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.fragment.MallFragment;
import com.knms.net.RxRequestApi;
import com.knms.util.ConstantObj;
import com.knms.util.L;
import com.knms.util.LocalDisplay;
import com.knms.util.ToolsHelper;
import com.knms.view.PagerSlidingTabStrip;
import com.knms.view.banner.AutoViewPager;
import com.knms.view.banner.CirclePageIndicator;
import com.knms.view.sticky.HeaderScrollHelper;
import com.knms.view.sticky.HeaderViewPager;

import java.util.List;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subscriptions.CompositeSubscription;


/**
 * 支付商城活动列表
 */
public class MallActivity extends HeadNotifyBaseFragmentActivity {
    private PullToRefreshHeaderViewPager pullToRefreshStickyNavLayout;
    private HeaderViewPager headerViewPager;
    private PagerSlidingTabStrip pagerSlidingTabStrip;
    private ViewPager viewPager;
    private ViewStub viewStub;
    private View viewBanner;
    private View btnTop;

    private boolean isFrist = true;
    private String typeId;

    private CompositeSubscription mSubscription;

    @Override
    protected void getParmas(Intent intent) {
        typeId = intent.getStringExtra("typeId");
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_mall;
    }

    @Override
    protected void initView() {
        pullToRefreshStickyNavLayout = findView(R.id.pull_to_refresh);
        headerViewPager = pullToRefreshStickyNavLayout.getRefreshableView();
        btnTop = findViewById(R.id.btn_top);
        pullToRefreshStickyNavLayout.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        viewStub = findView(R.id.vs_banner_advertisement);
        pagerSlidingTabStrip = (PagerSlidingTabStrip) findViewById(R.id.id_stickynavlayout_indicator);
        viewPager = (ViewPager) findViewById(R.id.id_stickynavlayout_viewpager);
        pagerSlidingTabStrip.setAllCaps(true);
        pagerSlidingTabStrip.setIndicatorColor(getResources().getColor(R.color.no_color));
        pagerSlidingTabStrip.setShowSelectedTabBg(true);
//        pullToRefreshStickyNavLayout.getRefreshableView().setTopOffset(LocalDisplay.dp2px(46));
    }

    @Override
    protected void initData() {
        mSubscription = new CompositeSubscription();
        headerViewPager.setOnScrollListener(new HeaderViewPager.OnScrollListener() {
            @Override
            public void onScroll(int currentY, int maxY) {
                    btnTop.setVisibility(currentY>=maxY?View.VISIBLE:View.GONE);
            }
        });
        btnTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RxBus.get().post(BusAction.REFRESH_IDLE, false);
                headerViewPager.scrollTo(0, 0);
            }
        });
        pullToRefreshStickyNavLayout.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<HeaderViewPager>() {
            @Override
            public void onRefresh(PullToRefreshBase<HeaderViewPager> refreshView) {
                if(!isFrist) ((MallFragment)((MallFragmentAdapter)viewPager.getAdapter()).getItem(viewPager.getCurrentItem())).refresh(true);
                reqApi();
            }
        });
        pullToRefreshStickyNavLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                pullToRefreshStickyNavLayout.setRefreshing();
            }
        }, 500);
    }

    @Override
    public String setStatisticsTitle() {
        return "爆款推荐";
    }

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("爆款推荐");
    }
    @Override
    protected void reqApi() {
        mSubscription.add(Observable.zip(RxRequestApi.getInstance().getApiService().getMallClassify(),
                RxRequestApi.getInstance().getApiService().getAdvertisement("B"), new Func2<ResponseBody<List<Tab>>, ResponseBody<IndexAd>, MallData>() {
                    @Override
                    public MallData call(ResponseBody<List<Tab>> body1, ResponseBody<IndexAd> body2) {
                        MallData data = new MallData();
                        data.code = ConstantObj.OK;
                        if (body1.isSuccess()) {
                            data.tabs = body1.data;
                        }

                        if (body2.isSuccess()) {
                            data.indexAd = body2.data;
                        }

                        data.code = body1.code;
                        data.desc = body1.desc;
                        return data;
                    }
                }).compose(this.<MallData>applySchedulers())
                .onErrorResumeNext(new Func1<Throwable, Observable<? extends MallData>>() {
                    @Override
                    public Observable<? extends MallData> call(Throwable throwable) {
                        return null;
                    }
                })
                .subscribe(new Action1<MallData>() {
                    @Override
                    public void call(MallData body) {
                        pullToRefreshStickyNavLayout.onRefreshComplete();
                        if (ConstantObj.OK.equals(body.code)) {
                            updateBanner(body.indexAd);
                           if(isFrist)updateView(body.tabs);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        pullToRefreshStickyNavLayout.onRefreshComplete();
                    }
                }));
    }

    private void updateBanner(IndexAd indexAd) {
        if(indexAd != null && indexAd.b1 != null && indexAd.b1.size() > 0){//有数据
            if(viewBanner == null) viewBanner = viewStub.inflate();
            if(viewBanner != null) viewBanner.setVisibility(View.VISIBLE);
            AutoViewPager vp_banner_advertisement = (AutoViewPager)viewBanner.findViewById(R.id.vp_banner_advertisement);
            vp_banner_advertisement.setmTimer(7000);
            vp_banner_advertisement.setBoundaryLooping(indexAd.b1.size() != 1);
            CirclePageIndicator cpi_banner_advertisement = (CirclePageIndicator)viewBanner.findViewById(R.id.cpi_banner_advertisement);
            vp_banner_advertisement.setAdapter(new AdvertisementAdapter(this,indexAd.b1, LocalDisplay.dp2px(150)));
            vp_banner_advertisement.setOffscreenPageLimit(2);
            cpi_banner_advertisement.setViewPager(vp_banner_advertisement);
        }else{
            if(viewBanner != null) viewBanner.setVisibility(View.GONE);
        }
    }

    private void updateView(List<Tab> data) {
        if(!(data != null && data.size() > 0)) return;
        ToolsHelper.getInstance().sort(data, "seq");
        int position = viewPager.getCurrentItem();
        final MallFragmentAdapter fragmentAdapter = new MallFragmentAdapter(getSupportFragmentManager(),data);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setAdapter(fragmentAdapter);
        pagerSlidingTabStrip.setViewPager(viewPager);
        if(isFrist){
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
        viewPager.setCurrentItem(position);

        headerViewPager.setCurrentScrollableContainer((HeaderScrollHelper.ScrollableContainer) fragmentAdapter.getItem(position));
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                headerViewPager.setCurrentScrollableContainer((HeaderScrollHelper.ScrollableContainer) fragmentAdapter.getItem(position));
            }
        });

    }
    @Override
    protected void onDestroy() {
        if(mSubscription != null) mSubscription.unsubscribe();
        super.onDestroy();
    }
}
