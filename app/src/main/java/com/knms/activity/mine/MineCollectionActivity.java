package com.knms.activity.mine;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.knms.activity.base.BaseFragmentActivity;
import com.knms.activity.im.MessageCenterActivity;
import com.knms.activity.login.FasterLoginActivity;
import com.knms.adapter.BaseFragmentAdapter;
import com.knms.bean.ResponseBody;
import com.knms.core.im.IMHelper;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.fragment.MineBabyFragment;
import com.knms.fragment.MineInspFragment;
import com.knms.fragment.MineShopFragment;
import com.knms.fragment.base.BaseFragment;
import com.knms.net.RxRequestApi;
import com.knms.util.DialogHelper;
import com.knms.util.SPUtils;
import com.knms.util.Tst;
import com.knms.view.PagerSlidingTabStrip;
import com.knms.android.R;

import java.util.ArrayList;
import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by tdx on 2016/9/7.
 * 我的收藏&浏览(宝贝店铺灵感)
 */
public class MineCollectionActivity extends BaseFragmentActivity implements View.OnClickListener, DialogHelper.OnMenuClick {
    private ImageView mImageViewBack;
    PagerSlidingTabStrip tabStrip;
    ViewPager viewPager;
    BaseFragmentAdapter adapter;
    List<BaseFragment> fragments;
    int type;//确定是收藏还是浏览 0-收藏；1-浏览
    public int currentPosition;//确定是宝贝、店铺、灵感

    private ImageView imgMsebtn;
    private TextView tvClearContent;


    @Override
    protected void getParmas(Intent intent) {
        type = intent.getIntExtra("type", 0);
        currentPosition = intent.getIntExtra("positon", 0);
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_mine_collection;
    }

    @Override
    protected void initView() {
        imgMsebtn = (ImageView) findViewById(R.id.information);
        tvClearContent = (TextView) findViewById(R.id.clear_browsing_history);
        tabStrip = findView(R.id.tabStrip);
        viewPager = findView(R.id.viewpager);
        mImageViewBack = findView(R.id.rich_scan);
        mImageViewBack.setOnClickListener(this);
        tvClearContent.setOnClickListener(this);
        imgMsebtn.setOnClickListener(this);
        if (type == 1) {
            imgMsebtn.setVisibility(View.GONE);
            tvClearContent.setVisibility(View.VISIBLE);
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

    @Override
    protected void initData() {
        fragments = new ArrayList<BaseFragment>();
        fragments.add(MineBabyFragment.newInstance(type));
        fragments.add(MineShopFragment.newInstance(type));
        fragments.add(MineInspFragment.newInstance(type));
        adapter = new BaseFragmentAdapter(getSupportFragmentManager(), fragments);
        viewPager.setAdapter(adapter);
        tabStrip.setViewPager(viewPager);
        viewPager.setOffscreenPageLimit(2);
        if (currentPosition < 3) {
            viewPager.setCurrentItem(currentPosition);
        } else {
            viewPager.setCurrentItem(0);
        }
        tabStrip.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            public void onPageSelected(int position) {
                currentPosition = position;
                if(type == 1) setClearBtnVisible(fragments.get(currentPosition).hasData);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public String setStatisticsTitle() {
        return type==0?"我的收藏":"我的浏览";
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.rich_scan:
                finshActivity();
                break;
            case R.id.information://消息
                if (!SPUtils.isLogin()) {
                    startActivityAnimGeneral(FasterLoginActivity.class, null);
                } else {
                    startActivityAnimGeneral(MessageCenterActivity.class, null);
                }
                break;
            case R.id.clear_browsing_history://清空浏览记录
                showDialog();
                break;
            default:
                break;
        }
    }
    public void setClearBtnVisible(boolean isVisible){
        tvClearContent.setVisibility(isVisible ? View.VISIBLE :View.GONE);
    }
    private void showDialog() {
        DialogHelper.showPromptDialog(this, null, "您确定清空浏览记录吗？", "取消", null, "确定", this);
    }
    @Override
    public void onLeftMenuClick() { }
    @Override
    public void onCenterMenuClick() { }
    @Override
    public void onRightMenuClick() {
        showProgress();
        RxRequestApi.getInstance().getApiService().cleanBrowsestate(currentPosition +1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody>() {
                    @Override
                    public void call(ResponseBody body) {
                        hideProgress();
                        if (body.isSuccess()) {
                            RxBus.get().post(BusAction.CLEAR_BROWSE, currentPosition);
                            setClearBtnVisible(false);
                        } else {
                            Tst.showToast(body.desc);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        hideProgress();
                        Tst.showToast(throwable.toString());
                    }
                });
    }
}
