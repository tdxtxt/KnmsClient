package com.knms.activity.goods;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshHeaderViewPager;
import com.knms.activity.ReleaseIdleActivity;
import com.knms.activity.base.BaseFragmentActivity;
import com.knms.activity.im.MessageCenterActivity;
import com.knms.activity.login.FasterLoginActivity;
import com.knms.activity.search.SearchActivity;
import com.knms.adapter.AdvertisementAdapter;
import com.knms.android.R;
import com.knms.bean.IndexAd;
import com.knms.bean.ResponseBody;
import com.knms.bean.myidle.MyIdle;
import com.knms.core.im.IMHelper;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.fragment.IdleClassifyFragment;
import com.knms.fragment.IdleRecommendFragment;
import com.knms.net.RxRequestApi;
import com.knms.util.LocalDisplay;
import com.knms.util.SPUtils;
import com.knms.util.ScreenUtil;
import com.knms.util.Tst;
import com.knms.view.banner.AutoViewPager;
import com.knms.view.banner.CirclePageIndicator;
import com.knms.view.menu.ExpandableButtonMenu;
import com.knms.view.menu.ExpandableMenuOverlay;
import com.knms.view.sticky.HeaderViewPager;

import java.util.HashMap;
import java.util.Map;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by tdx on 2016/9/1.
 * 闲置家具
 */
public class IdleFurnitureActivity extends BaseFragmentActivity implements View.OnClickListener {
    private RelativeLayout rl_head_recommend, rl_head_classify, rl_recommend,rl_recommend_s, rl_classify,rl_classify_s, rl_search, ll_menu_head;
    private LinearLayout ll_menu,ll_menu_s;
    private static PullToRefreshHeaderViewPager stickyLayout;

    private IdleClassifyFragment fragment_idle_classify;
    private IdleRecommendFragment fragment_idle_recommend;
    private ImageView backtrack, information;
    private ExpandableMenuOverlay menuOverlay;
    private ImageButton topBtn;
    private ImageView ivSearch;

    private FrameLayout idleBannerLayout;


    @Override
    protected int layoutResID() {
        return R.layout.activity_idle_furniture;
    }

    @Override
    protected void initView() {
        idleBannerLayout = findView(R.id.idle_banner);
        idleBannerLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ScreenUtil.getScreenWidth() / 2));
        rl_head_recommend = findView(R.id.rl_head_recommend);
        rl_head_classify = findView(R.id.rl_head_classify);
        rl_recommend = findView(R.id.rl_recommend);
        rl_classify = findView(R.id.rl_classify);
        rl_recommend_s = findView(R.id.rl_recommend_s);
        rl_classify_s = findView(R.id.rl_classify_s);
        stickyLayout = findView(R.id.stickylayout);
        ll_menu = findView(R.id.ll_menu);
        ll_menu_s=findView(R.id.id_stickynavlayout_indicator);
        rl_search = findView(R.id.rl_search);
        ll_menu_head = findView(R.id.ll_menu_head);
        backtrack = findView(R.id.rich_scan);
        menuOverlay = findView(R.id.idle_menu_overlay);
        topBtn = (ImageButton) findViewById(R.id.top_button);
        information = findView(R.id.information);
        ivSearch = findView(R.id.iv_search);

    }

    int[] scrs = new int[2];

    @Override
    protected void initData() {
        fragment_idle_classify = IdleClassifyFragment.newInstance();
        fragment_idle_recommend = IdleRecommendFragment.newInstance();

        rl_head_recommend.setOnClickListener(this);
        rl_head_classify.setOnClickListener(this);
        rl_recommend.setOnClickListener(this);
        rl_classify.setOnClickListener(this);
        rl_recommend_s.setOnClickListener(this);
        rl_classify_s.setOnClickListener(this);
        rl_search.setOnClickListener(this);
        backtrack.setOnClickListener(this);
        topBtn.setOnClickListener(this);
        information.setOnClickListener(this);
        ivSearch.setOnClickListener(this);
        stickyLayout.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        stickyLayout.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<HeaderViewPager>() {
            @Override
            public void onRefresh(PullToRefreshBase<HeaderViewPager> refreshView) {
                RxBus.get().post(BusAction.REFRESH_IDLE, true);
                reqApi();
            }
        });

        stickyLayout.getRefreshableView().setOnScrollListener(new HeaderViewPager.OnScrollListener() {
            @Override
            public void onScroll(int currentY, int maxY) {

            }
        });

        stickyLayout.getRefreshableView().setOnScrollListener(new HeaderViewPager.OnScrollListener() {
            @Override
            public void onScroll(int currentY, int maxY) {
                if(ll_menu_s.getVisibility()==View.VISIBLE) return;
                if(currentY==maxY){
                    rl_search.setVisibility(View.GONE);
                    ll_menu_head.setVisibility(View.VISIBLE);
                    topBtn.setVisibility(View.VISIBLE);
                }else{
                    rl_search.setVisibility(View.VISIBLE);
                    ll_menu_head.setVisibility(View.GONE);
                    topBtn.setVisibility(View.GONE);
                }
            }
        });

        menuOverlay.setOnMenuButtonClickListener(new ExpandableButtonMenu.OnMenuButtonClick() {
            @Override
            public void onClick(ExpandableButtonMenu.MenuButton action) {
                Intent intent = new Intent(IdleFurnitureActivity.this, ReleaseIdleActivity.class);
                switch (action) {
                    case LEFT:
                        intent.putExtra("isDraft", false);
                        startActivity(intent);
                        menuOverlay.hide();
                        break;
                    case RIGHT:
                        intent.putExtra("isDraft", true);
                        startActivity(intent);
                        menuOverlay.hide();
                        break;
                }
            }
        });

        menuOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyIdle myIdle = SPUtils.getSerializable("draftIdle", MyIdle.class);
                if (myIdle == null) {
                    menuOverlay.hide();
                    startActivityAnimGeneral(ReleaseIdleActivity.class, null);
                } else {
                    menuOverlay.show();
                }
            }
        });
        reqApi();
        setRecommend();
    }

    @Override
    public String setStatisticsTitle() {
        return "闲置家具";
    }

    private void setClassify() {
        this.changeFragment(fragment_idle_classify);
        findView(R.id.view_line_classify).setVisibility(View.VISIBLE);
        findView(R.id.view_line_recommend).setVisibility(View.GONE);
        findView(R.id.view_line_classify_head).setVisibility(View.VISIBLE);
        findView(R.id.view_line_recommend_head).setVisibility(View.GONE);
        ((TextView) findView(R.id.tv_idle_classify)).setTextColor(0xff333333);
        ((TextView) findView(R.id.tv_idle_recommend)).setTextColor(0xff666666);

        findView(R.id.view_line_classify_s).setVisibility(View.VISIBLE);
        findView(R.id.view_line_recommend_s).setVisibility(View.GONE);
        findView(R.id.view_line_classify_head).setVisibility(View.VISIBLE);
        findView(R.id.view_line_recommend_head).setVisibility(View.GONE);
        ((TextView) findView(R.id.tv_idle_classify_s)).setTextColor(0xff333333);
        ((TextView) findView(R.id.tv_idle_recommend_s)).setTextColor(0xff666666);
    }

    private void setRecommend() {
        this.changeFragment(fragment_idle_recommend);
        findView(R.id.view_line_classify).setVisibility(View.GONE);
        findView(R.id.view_line_recommend).setVisibility(View.VISIBLE);
        findView(R.id.view_line_classify_head).setVisibility(View.GONE);
        findView(R.id.view_line_recommend_head).setVisibility(View.VISIBLE);
        ((TextView) findView(R.id.tv_idle_classify)).setTextColor(0xff666666);
        ((TextView) findView(R.id.tv_idle_recommend)).setTextColor(0xff333333);

        findView(R.id.view_line_classify_s).setVisibility(View.GONE);
        findView(R.id.view_line_recommend_s).setVisibility(View.VISIBLE);
        findView(R.id.view_line_classify_head).setVisibility(View.GONE);
        findView(R.id.view_line_recommend_head).setVisibility(View.VISIBLE);
        ((TextView) findView(R.id.tv_idle_classify_s)).setTextColor(0xff666666);
        ((TextView) findView(R.id.tv_idle_recommend_s)).setTextColor(0xff333333);
        stickyLayout.getRefreshableView().setCurrentScrollableContainer(fragment_idle_recommend);

    }

    private void changeFragment(Fragment fragment) {
        FragmentManager fm = super.getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.id_stickynavlayout_viewpager, fragment);
        transaction.commit();
    }

    @Override
    protected void reqApi() {
        RxRequestApi.getInstance().getApiService().getAdvertisement("C")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<IndexAd>>() {
                    @Override
                    public void call(ResponseBody<IndexAd> indexAdResponseBody) {
                        stickyLayout.onRefreshComplete();
                        if (indexAdResponseBody.isSuccess()) {
                            updateBanner(indexAdResponseBody.data);
                        } else {
                            idleBannerLayout.setVisibility(View.GONE);
                            Tst.showToast(indexAdResponseBody.desc);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        stickyLayout.onRefreshComplete();
                        Tst.showToast(throwable.getMessage());
                    }
                });
    }

    private void updateBanner(IndexAd indexAd) {
        if (indexAd.c1 == null || indexAd.c1.size() == 0) {
            idleBannerLayout.setVisibility(View.GONE);
            ll_menu.setVisibility(View.GONE);
            ll_menu_s.setVisibility(View.VISIBLE);
            findView(R.id.view).setVisibility(View.GONE);
            return;
        }else{
            idleBannerLayout.setVisibility(View.VISIBLE);
            ll_menu.setVisibility(View.VISIBLE);
            ll_menu_s.setVisibility(View.GONE);
            findView(R.id.view).setVisibility(View.VISIBLE);
        }
        AutoViewPager vp_banner_advertisement = (AutoViewPager) findViewById(R.id.vp_banner_advertisement);
        vp_banner_advertisement.setmTimer(7000);
        vp_banner_advertisement.setBoundaryLooping(indexAd.c1.size() != 1);
        CirclePageIndicator cpi_banner_advertisement = (CirclePageIndicator) findViewById(R.id.cpi_banner_advertisement);
        vp_banner_advertisement.setAdapter(new AdvertisementAdapter(this, indexAd.c1, LocalDisplay.dp2px(150)));
        vp_banner_advertisement.setOffscreenPageLimit(2);
        cpi_banner_advertisement.setViewPager(vp_banner_advertisement);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.rl_head_classify:
                setClassify();
                break;
            case R.id.rl_head_recommend:
                setRecommend();
                break;
            case R.id.rl_classify:
            case R.id.rl_classify_s:
                setClassify();
                break;
            case R.id.rl_recommend:
            case R.id.rl_recommend_s:
                setRecommend();
                break;
//            case R.id.release_button:
//                startActivityAnimGeneral(ReleaseIdleActivity.class, null);
//                break;
            case R.id.rl_search:
            case R.id.iv_search:
                Map<String,Object> param=new HashMap<>();
                param.put("type",2);
                startActivityAnimGeneral(SearchActivity.class, param);
                break;
            case R.id.rich_scan:
                finshActivity();
                break;
            case R.id.top_button:
                stickyLayout.getRefreshableView().scrollTo(0, 0);
                RxBus.get().post(BusAction.REFRESH_IDLE, false);
                break;
            case R.id.information:
                if (!SPUtils.isLogin())
                    startActivityAnimGeneral(FasterLoginActivity.class, null);
                else
                    startActivityAnimGeneral(MessageCenterActivity.class, null);
                break;
        }
    }

    Subscription subscriptionMsgCount;

    @Override
    protected void onResume() {
        super.onResume();
        if (subscriptionMsgCount != null) subscriptionMsgCount.unsubscribe();
        subscriptionMsgCount = IMHelper.getInstance().isUnreadMsg().subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean aBoolean) {
                if (aBoolean)
                    ((ImageView) findView(R.id.information)).setImageResource(R.drawable.home_03);
                else ((ImageView) findView(R.id.information)).setImageResource(R.drawable.home_12);
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (subscriptionMsgCount != null) subscriptionMsgCount.unsubscribe();
        super.onDestroy();
    }

    public static HeaderViewPager getPulltoLayout(){
        return  stickyLayout.getRefreshableView();
    }
}
