package com.knms.activity.details;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.widget.TextView;

import com.knms.activity.base.HeadBaseFragmentActivity;
import com.knms.adapter.BaseFragmentAdapter;
import com.knms.android.R;
import com.knms.fragment.base.BaseFragment;
import com.knms.fragment.order.OrderDetailsFragment;
import com.knms.fragment.order.OrderStatusFragment;
import com.knms.util.LocalDisplay;
import com.knms.view.PagerSlidingTabStrip;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/4/21.
 */

public class OrderDetailsActivity extends HeadBaseFragmentActivity {

    private PagerSlidingTabStrip tabStrip;
    private ViewPager mViewPager;
    private BaseFragmentAdapter fragmentAdapter;
    private List<BaseFragment> fragments;
    private String orderId;
    private int currentItem;

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("订单信息");
    }

    @Override
    protected void getParmas(Intent intent) {
        super.getParmas(intent);
        orderId= (String) intent.getSerializableExtra("orderid");
        currentItem=intent.getIntExtra("position",0);
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_order_details;
    }

    @Override
    protected void initView() {
        tabStrip = (PagerSlidingTabStrip) findViewById(R.id.tabStrip);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
    }

    @Override
    protected void initData() {
        fragments = new ArrayList<BaseFragment>();
        fragments.add(OrderStatusFragment.newInstance(orderId));
        fragments.add(OrderDetailsFragment.newInstance(orderId));
        fragmentAdapter = new BaseFragmentAdapter(getSupportFragmentManager(),fragments);
        mViewPager.setAdapter(fragmentAdapter);
        tabStrip.setViewPager(mViewPager);
        tabStrip.setTabPaddingLeftRight(LocalDisplay.dp2px(15));
        mViewPager.setCurrentItem(currentItem);
    }

    @Override
    public String setStatisticsTitle() {
        return "订单信息";
    }
}
