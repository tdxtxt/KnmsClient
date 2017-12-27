package com.knms.activity.main;

import android.os.Build;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.gyf.barlibrary.ImmersionBar;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshNestedScrollView;
import com.knms.activity.ScanQRCodeActivity;
import com.knms.activity.base.BaseActivity;
import com.knms.activity.im.MessageCenterActivity;
import com.knms.activity.login.FasterLoginActivity;
import com.knms.activity.search.SearchActivity;
import com.knms.adapter.ClassifyIndexAdapterF;
import com.knms.adapter.NestedScrollViewOverScrollDecorAdapter;
import com.knms.android.R;
import com.knms.bean.ClassifyIndexs;
import com.knms.bean.IndexAd;
import com.knms.bean.ResponseBody;
import com.knms.bean.other.Classify;
import com.knms.core.im.IMHelper;
import com.knms.net.RxApiService;
import com.knms.net.RxRequestApi;
import com.knms.other.RetrofitCache;
import com.knms.util.SPUtils;
import com.knms.util.Tst;
import com.knms.view.clash.FullyLinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import me.everything.android.ui.overscroll.VerticalOverScrollBounceEffectDecorator;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tdx on 2016/8/26.
 */
public class ClassificationActivityF extends BaseActivity{
    /**这里如果使用复用的RecyclerView或者ListView都会使滑动卡顿，所以必须一次性全部加载完毕**/
    private PullToRefreshNestedScrollView refreshLayout;
    private RecyclerView recyclerView;
    ClassifyIndexAdapterF adapter;
    private RxApiService apiService;
    private CompositeSubscription mSubscriptions;
    @Override
    protected int layoutResID() {
        return R.layout.activity_tab_classification;
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

    @Override
    protected void initView() {
        mSubscriptions = new CompositeSubscription();
        apiService = RxRequestApi.getInstance().getApiService();
        refreshLayout = findView(R.id.pull_to_refresh);
        new VerticalOverScrollBounceEffectDecorator(new NestedScrollViewOverScrollDecorAdapter(refreshLayout.getRefreshableView()));
        recyclerView = findView(R.id.recyclerView);
        LinearLayoutManager layoutManager = new FullyLinearLayoutManager(this,LinearLayoutManager.VERTICAL,false){
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        layoutManager.setAutoMeasureEnabled(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setNestedScrollingEnabled(false);

        initHeadView();
    }
    private void initHeadView() {
        //扫一扫
        findView(R.id.rich_scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityAnimGeneral(ScanQRCodeActivity.class,null);
            }
        });
        ////搜一搜
        findView(R.id.rlBtn_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityAnimGeneral(SearchActivity.class,null);
            }
        });
        //消息
        findView(R.id.information).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SPUtils.isLogin()){
                    startActivityAnimGeneral(MessageCenterActivity.class,null);
                }else{
                    startActivityAnimGeneral(FasterLoginActivity.class,null);
                }
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        IMHelper.getInstance().isUnreadMsg().subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean aBoolean) {
                if(aBoolean) ((ImageView)findView(R.id.information)).setImageResource(R.drawable.home_03);
                else ((ImageView)findView(R.id.information)).setImageResource(R.drawable.home_12);
            }
        });
    }
    @Override
    protected void initData() {
        refreshLayout.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        refreshLayout.setPullToRefreshOverScrollEnabled(true);
        refreshLayout.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<NestedScrollView>() {
            @Override
            public void onRefresh(PullToRefreshBase<NestedScrollView> refreshView) {
                reqApi();
            }
        });
        refreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing();
            }
        },100);
        adapter = new ClassifyIndexAdapterF(null);
        recyclerView.setAdapter(adapter);
    }
    @Override
    public String setStatisticsTitle() {
        return "分类";
    }
    @Override
    protected void reqApi() {
        mSubscriptions.add(Observable.zip(RetrofitCache.load("classifiac",apiService.getClassifys()),
                RetrofitCache.load("adclassifiac",apiService.getAdvertisement("E")),
                new Func2<ResponseBody<List<Classify>>, ResponseBody<IndexAd>, List<ClassifyIndexs>>() {
                    @Override
                    public List<ClassifyIndexs> call(ResponseBody<List<Classify>> body1, ResponseBody<IndexAd> body2) {
                        List<ClassifyIndexs> classifyIndexs = new ArrayList<ClassifyIndexs>();
                        List<Classify> classifys = body1.data;
                        if (classifys != null) {
                            ClassifyIndexs classifyIndexMenu;
                            ClassifyIndexs classifyIndexAd;
                            int position = 0;
                            for (Classify classify : classifys) {
                                classifyIndexMenu = new ClassifyIndexs(ClassifyIndexs.TYPE_MENU);
                                classifyIndexMenu.classify = classify;
                                classifyIndexs.add(classifyIndexMenu);
                                if(body2 != null && body2.data != null){
                                    if(position == 0 && body2.data.e1 != null && body2.data.e1.size() > 0){
                                        classifyIndexAd = new ClassifyIndexs(ClassifyIndexs.TYPE_AD);
                                        classifyIndexAd.ad = body2.data.e1.get(0);
                                        classifyIndexs.add(classifyIndexAd);
                                    }else if(position == 1 && body2.data.e2 != null && body2.data.e2.size() > 0){
                                        classifyIndexAd = new ClassifyIndexs(ClassifyIndexs.TYPE_AD);
                                        classifyIndexAd.ad = body2.data.e2.get(0);
                                        classifyIndexs.add(classifyIndexAd);
                                    }else if(position == 2 && body2.data.e3 != null && body2.data.e3.size() > 0){
                                        classifyIndexAd = new ClassifyIndexs(ClassifyIndexs.TYPE_AD);
                                        classifyIndexAd.ad = body2.data.e3.get(0);
                                        classifyIndexs.add(classifyIndexAd);
                                    }else if(position == 3 && body2.data.e4 != null && body2.data.e4.size() > 0){
                                        classifyIndexAd = new ClassifyIndexs(ClassifyIndexs.TYPE_AD);
                                        classifyIndexAd.ad = body2.data.e4.get(0);
                                        classifyIndexs.add(classifyIndexAd);
                                    }else if(position == 4 && body2.data.e5 != null && body2.data.e5.size() > 0){
                                        classifyIndexAd = new ClassifyIndexs(ClassifyIndexs.TYPE_AD);
                                        classifyIndexAd.ad = body2.data.e5.get(0);
                                        classifyIndexs.add(classifyIndexAd);
                                    }else if(position == 5 && body2.data.e6 != null && body2.data.e6.size() > 0){
                                        classifyIndexAd = new ClassifyIndexs(ClassifyIndexs.TYPE_AD);
                                        classifyIndexAd.ad = body2.data.e6.get(0);
                                        classifyIndexs.add(classifyIndexAd);
                                    }else if(position == 6 && body2.data.e7 != null && body2.data.e7.size() > 0){
                                        classifyIndexAd = new ClassifyIndexs(ClassifyIndexs.TYPE_AD);
                                        classifyIndexAd.ad = body2.data.e7.get(0);
                                        classifyIndexs.add(classifyIndexAd);
                                    }else if(position == 7 && body2.data.e8 != null && body2.data.e8.size() > 0){
                                        classifyIndexAd = new ClassifyIndexs(ClassifyIndexs.TYPE_AD);
                                        classifyIndexAd.ad = body2.data.e8.get(0);
                                        classifyIndexs.add(classifyIndexAd);
                                    }else if(position == 8 && body2.data.e9 != null && body2.data.e9.size() > 0){
                                        classifyIndexAd = new ClassifyIndexs(ClassifyIndexs.TYPE_AD);
                                        classifyIndexAd.ad = body2.data.e9.get(0);
                                        classifyIndexs.add(classifyIndexAd);
                                    }
                                    position ++;
                                }
                            }
                        }
                        return classifyIndexs;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext(new Func1<Throwable, Observable<? extends List<ClassifyIndexs>>>() {
                    @Override
                    public Observable<? extends List<ClassifyIndexs>> call(Throwable throwable) {
                        return Observable.empty();
                    }
                })
                .subscribe(new Action1<List<ClassifyIndexs>>() {
                    @Override
                    public void call(List<ClassifyIndexs> data) {
                        refreshLayout.onRefreshComplete();
                        updateView(data);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        refreshLayout.onRefreshComplete();
                        Tst.showToast(throwable.toString());
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        refreshLayout.onRefreshComplete();
                    }
                }));
    }
    private void updateView(List<ClassifyIndexs> classifyIndex) {
        adapter.setNewData(classifyIndex);
    }

    @Override
    protected void onDestroy() {
        if(mSubscriptions != null) mSubscriptions.unsubscribe();
        super.onDestroy();
    }
}
