package com.knms.activity.search;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseViewHolder;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshRecyclerView;
import com.knms.activity.ShopActivityF;
import com.knms.activity.base.BaseActivity;
import com.knms.activity.base.HeadBaseActivity;
import com.knms.activity.base.HeadNotifyBaseActivity;
import com.knms.activity.im.MessageCenterActivity;
import com.knms.adapter.ShopAdapter;
import com.knms.adapter.baserecycler.BaseQuickAdapter;
import com.knms.android.R;
import com.knms.app.KnmsApp;
import com.knms.bean.ResponseBody;
import com.knms.bean.other.Label;
import com.knms.bean.shop.Shop;
import com.knms.core.im.IMHelper;
import com.knms.net.RxRequestApi;
import com.knms.util.SPUtils;
import com.knms.util.Tst;
import com.knms.view.XEditText;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by tdx on 2016/9/22.
 * 搜索店铺列表
 */
public class ShopFromSearchActivity extends HeadBaseActivity {
    private String key;
    private int pageNum = 1;
    private ShopAdapter adapter;
    private PullToRefreshRecyclerView pullToRefreshRecycleView;
    private RecyclerView recyclerView;
    private RelativeLayout rl_status;
    private Subscription subscription;
    private XEditText etSearchCotent;
    private TextView tvRight;
    private ImageView ivRight;

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText(key);
    }

    @Override
    protected void getParmas(Intent intent) {
        key = intent.getStringExtra("key");
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_common_search;
    }

    @Override
    protected void initView() {
        etSearchCotent = findView(R.id.edit_title_center);
        pullToRefreshRecycleView = findView(R.id.refresh_recyclerView);
        recyclerView = pullToRefreshRecycleView.getRefreshableView();
        rl_status = findView(R.id.rl_status);
        setRightMenuCallBack(new RightCallBack() {
            @Override
            public void setRightContent(TextView tv, ImageView icon) {
                tvRight = tv;
                ivRight = icon;
                tv.setVisibility(View.GONE);
                tv.setTextColor(0xff333333);
                tv.setText("搜索");
            }

            @Override
            public void onclick() {
                if (tvRight.getVisibility() == View.VISIBLE) {
                    MobclickAgent.onEvent(ShopFromSearchActivity.this, "searchShopBtnClick");
                    key = etSearchCotent.getText().toString();
                    if (TextUtils.isEmpty(key)) Tst.showToast("请输入搜索关键字");
                    else {
                        etSearchCotent.clearFocus();
                        hideKeyboard();
                        pageNum = 1;
                        saveHistory(key);
                        refreshData();
                    }
                } else startActivityAnimGeneral(MessageCenterActivity.class, null);
            }
        });
        etSearchCotent.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    ivRight.setVisibility(View.GONE);
                    tvRight.setVisibility(View.VISIBLE);
                } else {
                    ivRight.setVisibility(View.VISIBLE);
                    tvRight.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    protected void initData() {
        etSearchCotent.setText(key);
//        etSearchCotent.setSelection(key.length());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ShopAdapter(this, false, true);
        recyclerView.setAdapter(adapter);
        pullToRefreshRecycleView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        adapter.setEnableLoadMore(true);
        adapter.setOnLoadMoreListener(new com.chad.library.adapter.base.BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                reqApi();
            }
        },recyclerView);
        adapter.setOnItemClickListener(new com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener<Shop>() {
            @Override
            public void onItemClick(com.chad.library.adapter.base.BaseQuickAdapter<Shop, ? extends BaseViewHolder> adapter, View view, int position) {
                if (adapter.getData().size() <= position) return;
                Shop shop = adapter.getData().get(position);
                Map<String, Object> parmas = new HashMap<>();
                parmas.put("shopId", shop.id);
                startActivityAnimGeneral(ShopActivityF.class, parmas);
            }
        });
        pullToRefreshRecycleView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<RecyclerView>() {
            @Override
            public void onRefresh(PullToRefreshBase<RecyclerView> refreshView) {
                recyclerView.scrollToPosition(0);
                pageNum = 1;
                reqApi();
            }
        });
        refreshData();
    }

    private void refreshData() {
        pullToRefreshRecycleView.postDelayed(new Runnable() {
            @Override
            public void run() {
                pullToRefreshRecycleView.setRefreshing();
            }
        }, 500);
    }

    @Override
    public String setStatisticsTitle() {
        return "搜索店铺列表";
    }

    @Override
    protected void reqApi() {
        subscription = RxRequestApi.getInstance().getApiService().searchShop(key.length() > 10 ? key.substring(0, 10) : key, pageNum)
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<ResponseBody<List<Shop>>>() {
                    @Override
                    public void call(ResponseBody<List<Shop>> body) {
                        pullToRefreshRecycleView.onRefreshComplete();
                        KnmsApp.getInstance().hideLoadView(rl_status);
                        if (body.isSuccess()) {
                            updateView(body.data);
                            pageNum++;
                        } else {
                            Tst.showToast(body.desc);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        KnmsApp.getInstance().hideLoadView(rl_status);
                        pullToRefreshRecycleView.onRefreshComplete();
                        Tst.showToast(throwable.toString());
                    }
                });
    }

    private void updateView(List<Shop> data) {
        if (pageNum == 1) {//刷新
            if (data != null && data.size() > 0) {
                adapter.setNewData(data);
            } else {
                //没有搜索到内容
                KnmsApp.getInstance().showDataEmpty(rl_status, "抱歉，没有找到相关的店铺", R.drawable.no_data_shop);
            }
        } else {
            if (data != null && data.size() > 0) {
                adapter.addData(data);
                adapter.loadMoreComplete();
            } else {
                adapter.loadMoreEnd();
            }
        }
    }

    private void saveHistory(String key) {
        List<Label> labels = new ArrayList<Label>();
        labels = SPUtils.getSerializable(SPUtils.KeyConstant.searchShopHistory, labels.getClass());
        if (labels == null) labels = new ArrayList<>();
        Label label = new Label();
        label.name = key;
        labels.remove(label);
        labels.add(0, label);
        SPUtils.saveSerializable(SPUtils.KeyConstant.searchShopHistory, labels);
    }


    Subscription subscriptionMsgCount;

    @Override
    protected void onResume() {
        super.onResume();
        if (subscriptionMsgCount != null) subscriptionMsgCount.unsubscribe();
        subscriptionMsgCount = IMHelper.getInstance().isUnreadMsg().subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean aBoolean) {
                if (aBoolean) ivRight.setImageResource(R.drawable.home_03);
                else ivRight.setImageResource(R.drawable.home_12);
            }
        });
    }

    float mPosX,mPosY,mCurPosX,mCurPosY;
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPosX = event.getX();
                mPosY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                mCurPosX = event.getX();
                mCurPosY = event.getY();
                if(mPosY - mCurPosY > 50||mCurPosY - mPosY > 50) {
                    hideKeyboard();
                }
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    protected void onDestroy() {
        if (subscriptionMsgCount != null) subscriptionMsgCount.unsubscribe();
        super.onDestroy();
    }
}
