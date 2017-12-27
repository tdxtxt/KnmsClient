package com.knms.activity.mall.order;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.knms.activity.ComplaintDetailsActivity;
import com.knms.activity.base.HeadNotifyBaseFragmentActivity;
import com.knms.activity.mall.order.fragment.AppOrderFragment;
import com.knms.activity.mall.order.fragment.MarketOrderFragment;
import com.knms.adapter.BaseFragmentAdapter;
import com.knms.android.R;
import com.knms.bean.ResponseBody;
import com.knms.bean.order.CommentReward;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.fragment.base.BaseFragment;
import com.knms.net.RxRequestApi;
import com.knms.util.LocalDisplay;
import com.knms.util.ScreenUtil;
import com.knms.view.FloatView;
import com.knms.view.PagerSlidingTabStrip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2017/7/17.
 * 我的订单（新）
 * 参数：position  ；值：0：app支付订单列表   1：商城支付订单列表
 */

public class MyOrderActivity extends HeadNotifyBaseFragmentActivity {
    private PagerSlidingTabStrip tabStrip;
    private ViewPager mViewPager;
    private BaseFragmentAdapter adapter;
    private List<BaseFragment> fragments;
    private int position=0;
    private FloatView ivPjyl;

    @Override
    protected void getParmas(Intent intent) {
        super.getParmas(intent);
        position=intent.getIntExtra("position",0);
        if (!TextUtils.isEmpty(intent.getStringExtra("complaintsId"))) {
            Map<String, Object> map = new HashMap<>();
            map.put("complaintsId", intent.getStringExtra("complaintsId"));
            startActivityAnimGeneral(ComplaintDetailsActivity.class, map);
        }
    }

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("我的订单");
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_my_order_layout;
    }

    @Override
    protected void initView() {
        ivPjyl = findView(R.id.iv_pjyl);
        tabStrip = findView(R.id.tabStrip);
        mViewPager = findView(R.id.viewpager);
    }

    @Override
    protected void initData() {
        fragments = new ArrayList<BaseFragment>();
        fragments.add(new AppOrderFragment());
        fragments.add(new MarketOrderFragment());
        adapter=new BaseFragmentAdapter(getSupportFragmentManager(),fragments);
        mViewPager.setAdapter(adapter);
        tabStrip.setViewPager(mViewPager);
        tabStrip.setTabPaddingLeftRight(LocalDisplay.dp2px(15));
        mViewPager.setCurrentItem(position);
        reqApi();
    }

    @Override
    protected void reqApi() {
        RxRequestApi.getInstance().getApiService().commentReward()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<CommentReward>>() {
                    @Override
                    public void call(ResponseBody<CommentReward> stringResponseBody) {
                        if (stringResponseBody.isSuccess() && stringResponseBody.data.status == 1) {
                            FrameLayout.LayoutParams layoutParamss = new FrameLayout.LayoutParams(LocalDisplay.dip2px(80),LocalDisplay.dip2px(80));
                            layoutParamss.leftMargin = ScreenUtil.getScreenWidth() - LocalDisplay.dp2px(100);
                            layoutParamss.topMargin = LocalDisplay.dp2px(20);
                            ivPjyl.setLayoutParams(layoutParamss);
                            ivPjyl.setUrl(stringResponseBody.data.url);
                            ivPjyl.setVisibility(View.VISIBLE);
                        } else
                            ivPjyl.setVisibility(View.GONE);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        ivPjyl.setVisibility(View.GONE);
                    }
                });
    }
    //接收生成付尾款订单通知
    @Subscribe(tags = {@Tag(BusAction.CREATE_ORDER_RETAINAGE)})
    public void receiveNotify(String str) {
        mViewPager.setCurrentItem(0);
    }
    @Override
    public String setStatisticsTitle() {
        return "我的订单";
    }
}
