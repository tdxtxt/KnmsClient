package com.knms.activity.coupons;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshRecyclerView;
import com.knms.activity.CommWebViewActivity;
import com.knms.activity.base.BaseActivity;
import com.knms.activity.base.HeadBaseActivity;
import com.knms.activity.base.HeadBaseFragmentActivity;
import com.knms.adapter.WelfareServiceListAdapter;
import com.knms.android.R;
import com.knms.app.KnmsApp;
import com.knms.bean.ResponseBody;
import com.knms.bean.welfareservice.CouponCenter;
import com.knms.net.RxRequestApi;
import com.knms.util.Tst;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2017/2/6.
 */

public class InvalidCouponActivity extends HeadBaseActivity {
    private RecyclerView mRecyclerView;
    private WelfareServiceListAdapter mAdapter;
    private RelativeLayout rl_status;
    private int type;
    private PullToRefreshRecyclerView refreshRecyclerView;
    private TextView tvNoData;

    @Override
    protected void getParmas(Intent intent) {
        type = intent.getIntExtra("type", 1);
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_invalid_coupon_layout;
    }


    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("无效券");
    }

    @Override
    protected void initView() {
        tvNoData = new TextView(this);
        tvNoData.setTextColor(0xff999999);
        tvNoData.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tvNoData.setGravity(Gravity.CENTER);
        tvNoData.setPadding(15, 15, 15, 15);
        tvNoData.setText("没有更多券了");
        refreshRecyclerView = (PullToRefreshRecyclerView) findViewById(R.id.ptr_invalid_coupon);
        refreshRecyclerView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        mRecyclerView = refreshRecyclerView.getRefreshableView();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        rl_status = (RelativeLayout) findViewById(R.id.rl_status);
        mAdapter = new WelfareServiceListAdapter(null, type, true);
        mRecyclerView.setAdapter(mAdapter);
        setRightMenuCallBack(new RightCallBack() {
            @Override
            public void setRightContent(TextView tv, ImageView icon) {
                tv.setText("使用攻略");
                tv.setTextColor(getResources().getColor(R.color.color_black_333333));
                icon.setVisibility(View.GONE);
            }

            @Override
            public void onclick() {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("url", "https://h5.kebuyer.com/document/coupon_tips.html");
                startActivityAnimGeneral(CommWebViewActivity.class, map);
            }
        });
        refreshRecyclerView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<RecyclerView>() {
            @Override
            public void onRefresh(PullToRefreshBase<RecyclerView> refreshView) {
                reqApi();

            }
        });
    }

    @Override
    protected void initData() {
        refreshRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshRecyclerView.setRefreshing();
            }
        }, 1000);

    }

    @Override
    protected void reqApi() {
        RxRequestApi.getInstance().getApiService().getMyCouponsList(type, 2)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<List<CouponCenter>>>() {
                    @Override
                    public void call(ResponseBody<List<CouponCenter>> listResponseBody) {
                        refreshRecyclerView.onRefreshComplete();
                        if (listResponseBody.isSuccess()) {
                            if (listResponseBody.data.size() == 0) {
                                KnmsApp.getInstance().showDataEmpty(rl_status, "您暂时没有优惠券", R.drawable.no_data_on_offer);
                                return;
                            }
                            mAdapter.setNewData(listResponseBody.data);
                            if (!mAdapter.hasFooterLayout()) {
                                mAdapter.addFooterView(tvNoData);
                            }
                        } else {
                            Tst.showToast(listResponseBody.desc);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        refreshRecyclerView.onRefreshComplete();
                        Tst.showToast(throwable.getMessage());
                    }
                });
    }

    @Override
    public String setStatisticsTitle() {
        return "无效优惠券";
    }

}
