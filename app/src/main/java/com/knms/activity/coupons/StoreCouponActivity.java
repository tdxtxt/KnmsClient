package com.knms.activity.coupons;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.knms.activity.base.HeadBaseActivity;
import com.knms.adapter.StoreAllCouponAdapter;
import com.knms.android.R;
import com.knms.app.KnmsApp;
import com.knms.bean.ResponseBody;
import com.knms.bean.user.User;
import com.knms.bean.welfareservice.StoreCoupon;
import com.knms.bean.welfareservice.StoreCouponClassify;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.net.RxRequestApi;
import com.knms.util.Tst;
import com.knms.view.clash.FullyLinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/9/26.
 */
public class StoreCouponActivity extends HeadBaseActivity implements StoreAllCouponAdapter.CallBack {

    private PullToRefreshScrollView refreshScrollView;
    private StoreAllCouponAdapter mAdapter;
    private String storeID = "";
    private RecyclerView rvUsable, rvCanReceive, rvHistory, rvOverdue;
    private TextView tvUsable, tvCanReceiver, tvHistory;

    @Override
    protected void getParmas(Intent intent) {
        super.getParmas(intent);
        storeID = intent.getStringExtra("id");
    }

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("码上用券");
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_store_coupon;
    }

    @Override
    protected void initView() {
        refreshScrollView = (PullToRefreshScrollView) findViewById(R.id.store_coupon_refreshScrollView);
        rvUsable = (RecyclerView) findViewById(R.id.rv_usable);
        rvCanReceive = (RecyclerView) findViewById(R.id.rv_canReceive);
        rvHistory = (RecyclerView) findViewById(R.id.rv_history);
        rvOverdue = (RecyclerView) findViewById(R.id.rv_overdue);
        tvUsable = (TextView) findViewById(R.id.tv_usable);
        tvCanReceiver = (TextView) findViewById(R.id.tv_canreceive);
        tvHistory = (TextView) findViewById(R.id.tv_history);
        rvHistory.setLayoutManager(new FullyLinearLayoutManager(this));
        rvCanReceive.setLayoutManager(new FullyLinearLayoutManager(this));
        rvUsable.setLayoutManager(new FullyLinearLayoutManager(this));
        rvOverdue.setLayoutManager(new FullyLinearLayoutManager(this));
    }

    @Override
    protected void initData() {

        refreshScrollView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ScrollView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
                reqApi();
            }
        });
        refreshScrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshScrollView.setRefreshing();
            }
        }, 1000);
    }

    @Override
    public String setStatisticsTitle() {
        return "码上用券";
    }

    @Override
    protected void reqApi() {
        RxRequestApi.getInstance().getApiService().getStoreCoupon(storeID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<StoreCoupon>>() {
                    @Override
                    public void call(ResponseBody<StoreCoupon> responseBody) {
                        refreshScrollView.onRefreshComplete();
                        if (responseBody.isSuccess()) {
                            UpdateView(responseBody.data);
                        } else
                            Tst.showToast(responseBody.desc);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        refreshScrollView.onRefreshComplete();
                        Tst.showToast(throwable.getMessage());
                    }
                });
    }

    private void UpdateView(StoreCoupon data) {

        if ((data.overdue == null || data.overdue.size() == 0) && (data.receiveValid == null || data.receiveValid.size() == 0)
                && (data.used == null || data.used.size() == 0) && (data.valid == null || data.valid.size() == 0)) {
            KnmsApp.getInstance().showDataEmpty(((RelativeLayout) findViewById(R.id.rl_not_data)), "没有扫出优惠券,请咨询店内客服", R.drawable.no_data_on_offer);
            return;
        }
        if (data.receiveValid != null && data.receiveValid.size() > 0)
            tvUsable.setVisibility(View.VISIBLE);
        else
            tvUsable.setVisibility(View.GONE);

        rvUsable.setAdapter(new StoreAllCouponAdapter(StoreCouponActivity.this, data.receiveValid, 1, storeID));
        if (data.valid != null && data.valid.size() > 0)
            tvCanReceiver.setVisibility(View.VISIBLE);
        else
            tvCanReceiver.setVisibility(View.GONE);

        rvCanReceive.setAdapter(new StoreAllCouponAdapter(StoreCouponActivity.this, data.valid, 2, storeID));
        if ((data.used != null && data.used.size() > 0) || (data.overdue != null && data.overdue.size() > 0))
            tvHistory.setVisibility(View.VISIBLE);
        else
            tvHistory.setVisibility(View.GONE);
        rvHistory.setAdapter(new StoreAllCouponAdapter(StoreCouponActivity.this, data.used, 3, storeID));
        rvOverdue.setAdapter(new StoreAllCouponAdapter(StoreCouponActivity.this, data.overdue, 4, storeID));

        ((StoreAllCouponAdapter) rvCanReceive.getAdapter()).setCallBack(this);
        ((StoreAllCouponAdapter) rvUsable.getAdapter()).setCallBack(this);

        rvUsable.setLayoutFrozen(true);
        rvCanReceive.setLayoutFrozen(true);
        rvHistory.setLayoutFrozen(true);
        rvOverdue.setLayoutFrozen(true);

    }

    @Override
    public void refresh() {
        refreshScrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshScrollView.setRefreshing();
            }
        }, 1000);
    }

    @Subscribe(tags = {@Tag(BusAction.ACTION_LOGIN)})
    public void loginAccout(User user) {
        refreshScrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshScrollView.setRefreshing();
            }
        }, 1000);
    }
}
