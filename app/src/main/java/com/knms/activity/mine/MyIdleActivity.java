package com.knms.activity.mine;

import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.knms.activity.base.HeadBaseFragmentActivity;
import com.knms.activity.base.HeadNotifyBaseFragmentActivity;
import com.knms.adapter.MyIdleFragmentAdapter;
import com.knms.bean.user.User;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.fragment.OnOfferFragment;
import com.knms.fragment.UndercarriageFragment;
import com.knms.fragment.base.BaseFragment;
import com.knms.util.LocalDisplay;
import com.knms.util.Tst;
import com.knms.view.PagerSlidingTabStrip;
import com.knms.android.R;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/8/29.
 * 我的闲置
 */
public class MyIdleActivity extends HeadNotifyBaseFragmentActivity {

    private PagerSlidingTabStrip tabStrip;
    private ViewPager mViewPager;
    private MyIdleFragmentAdapter adapter;
    private List<BaseFragment> mFragmentList = new ArrayList<BaseFragment>();

    @Override
    protected int layoutResID() {
        return R.layout.activity_my_idle;
    }
    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("我的闲置");
    }
    @Override
    protected void initView() {
        tabStrip = findView(R.id.pager_tab_strip);
        mViewPager = findView(R.id.view_pager);
        init();
    }

    private void init() {
        tabStrip.setAllCaps(false);
        tabStrip.setIndicatorColor(getResources().getColor(R.color.tab_line));
        tabStrip.setIndicatorHeight(LocalDisplay.dp2px(1));
    }

    @Override
    protected void initData() {
        mFragmentList.add(new OnOfferFragment());
        mFragmentList.add(new UndercarriageFragment());
        adapter = new MyIdleFragmentAdapter(getSupportFragmentManager(),mFragmentList);
        mViewPager.setAdapter(adapter);
        tabStrip.setViewPager(mViewPager);
        mViewPager.setCurrentItem(0);
        tabStrip.setShouldExpand(true);
    }

    @Override
    public String setStatisticsTitle() {
        return "我的闲置";
    }
}
