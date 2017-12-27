package com.knms.activity.coupons;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.knms.activity.CommWebViewActivity;
import com.knms.activity.base.HeadBaseActivity;
import com.knms.activity.base.HeadBaseFragmentActivity;
import com.knms.adapter.MyCouponsViewPagerAdapter;
import com.knms.android.R;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.fragment.MyCouponsFragment;
import com.knms.fragment.base.BaseFragment;
import com.knms.util.LocalDisplay;
import com.knms.view.PagerSlidingTabStrip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Administrator on 2016/9/23.
 */
public class MyCouponsListActivity extends HeadBaseFragmentActivity {

    private PagerSlidingTabStrip tabStrip;
    private ViewPager mViewPager;
    private MyCouponsViewPagerAdapter adapter;
    private List<BaseFragment> fragments;
    private List<String> title = new ArrayList<>();

    private int couponsType = 0;

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("我的优惠券");
    }


    @Override
    protected void getParmas(Intent intent) {
        super.getParmas(intent);
        couponsType = intent.getIntExtra("type", 0);
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_my_coupon;
    }


    @Override
    protected void initView() {
        tabStrip = (PagerSlidingTabStrip) findViewById(R.id.tabStrip);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        onRightMenuCallBack(new RightCallBack() {
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
    }

    @Override
    protected void initData() {
        title.add("买手现金券");
        title.add("合作优惠券");
        fragments = new ArrayList<BaseFragment>();
        fragments.add(MyCouponsFragment.newInstance(1));
        fragments.add(MyCouponsFragment.newInstance(2));
        adapter = new MyCouponsViewPagerAdapter(getSupportFragmentManager(), fragments, title);
        mViewPager.setAdapter(adapter);
        tabStrip.setViewPager(mViewPager);
        tabStrip.setTabPaddingLeftRight(LocalDisplay.dp2px(15));
        mViewPager.setCurrentItem(couponsType);
        mViewPager.setOffscreenPageLimit(2);
    }

    @Override
    public String setStatisticsTitle() {
        return "我的优惠券";
    }

    @Override
    public void onRightMenuCallBack(RightCallBack callBack) {
        super.onRightMenuCallBack(callBack);
    }
}
