package com.knms.activity.search;

import android.content.Intent;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseViewHolder;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshRecyclerView;
import com.knms.activity.IdleDetailsActivity;
import com.knms.activity.base.HeadBaseActivity;
import com.knms.activity.base.HeadNotifyBaseActivity;
import com.knms.activity.im.MessageCenterActivity;
import com.knms.adapter.CowryClassificationRcyAdapter;
import com.knms.adapter.IdleAdapter;
import com.knms.adapter.SearchAdapter;
import com.knms.adapter.baserecycler.BaseQuickAdapter;
import com.knms.android.R;
import com.knms.app.KnmsApp;
import com.knms.bean.ResponseBody;
import com.knms.bean.idle.ReIdleClassify;
import com.knms.bean.other.Label;
import com.knms.bean.product.Idle;
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
 * 搜索闲置列表
 */
public class IdleFromSearchActivity extends HeadBaseActivity {
    private String key, typeId;
    private int pageNum = 1;
    private IdleAdapter adapter;
    private PullToRefreshRecyclerView pullToRefreshRecycleView;
    private RecyclerView recyclerView, searchRecyclerView;
    private RelativeLayout rl_status;
    private XEditText etSearchCotent;
    private TextView tvRight;
    private ImageView ivRight;

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText(key);
        tv_center.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finshActivity();
            }
        });
    }

    @Override
    protected void getParmas(Intent intent) {
        key = intent.getStringExtra("key");
        typeId = intent.getStringExtra("typeId");
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
        searchRecyclerView = findView(R.id.search_recyclerView);
        searchRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        rl_status = findView(R.id.rl_status);
        setRightMenuCallBack(new RightCallBack() {
            @Override
            public void setRightContent(TextView tv, ImageView icon) {
                tvRight=tv;
                ivRight=icon;
                tv.setVisibility(View.GONE);
                tv.setTextColor(0xff333333);
                tv.setText("搜索");
            }

            @Override
            public void onclick() {
                if (tvRight.getVisibility() == View.VISIBLE) {
                    MobclickAgent.onEvent(IdleFromSearchActivity.this, "searchIdleBtnClick");
                    key = etSearchCotent.getText().toString();
                    if (TextUtils.isEmpty(key)) Tst.showToast("请输入搜索关键字");
                    else {
                        reset(key,"");
                    }
                }else   startActivityAnimGeneral(MessageCenterActivity.class, null);
            }
        });
        etSearchCotent.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b){searchIdelMatch(key);ivRight.setVisibility(View.GONE);tvRight.setVisibility(View.VISIBLE);searchRecyclerView.setVisibility(View.VISIBLE);}
                else {ivRight.setVisibility(View.VISIBLE);tvRight.setVisibility(View.GONE);searchRecyclerView.setVisibility(View.GONE);}
            }
        });
    }

    @Override
    protected void initData() {
        etSearchCotent.setText(key);
//        etSearchCotent.setSelection(key.length());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new IdleAdapter(this);
        recyclerView.setAdapter(adapter);
        pullToRefreshRecycleView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        adapter.setOnItemClickListener(new com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener<Idle>() {
            @Override
            public void onItemClick(com.chad.library.adapter.base.BaseQuickAdapter<Idle, ? extends BaseViewHolder> adapter, View view, int position) {
                Idle item = adapter.getItem(position);
                if (item != null) {
                    Map<String, Object> params = new ArrayMap<String, Object>();
                    params.put("id", item.id);
                    startActivityAnimGeneral(IdleDetailsActivity.class, params);
                }
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
        adapter.setEnableLoadMore(true);
        adapter.setOnLoadMoreListener(new com.chad.library.adapter.base.BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                reqApi();
            }
        },recyclerView);
        refreshData();
        etSearchCotent.setTextChangedListener(new XEditText.TextChangedListener() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(TextUtils.isEmpty(s.toString())) searchRecyclerView.setAdapter(new SearchAdapter(null));
                else if(!TextUtils.isEmpty(s.toString())&&etSearchCotent.isFocusable())  searchIdelMatch(s.toString());
            }
            @Override
            public void onTextChangedAfter(Editable editable) {}
        });
    }

    private void refreshData(){
        pullToRefreshRecycleView.postDelayed(new Runnable() {
            @Override
            public void run() {
                pullToRefreshRecycleView.setRefreshing();
            }
        }, 500);
    }

    @Override
    public String setStatisticsTitle() {
        return "搜索闲置列表";
    }

    @Override
    protected void reqApi() {
        RxRequestApi.getInstance().getApiService().searchIdle(key.length()>10?key.substring(0,10):key, typeId, pageNum)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<ResponseBody<List<Idle>>>() {
                    @Override
                    public void call(ResponseBody<List<Idle>> body) {
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

    private void updateView(List<Idle> data) {
        if (pageNum == 1) {//刷新
            if (data != null && data.size() > 0) {
                adapter.setNewData(data);
            } else {
                //没有搜索到内容
                KnmsApp.getInstance().showDataEmpty(rl_status, "抱歉，没有找到相关的闲置", R.drawable.no_data_shop);
            }
        } else {
            if (data != null && data.size() > 0) {
                adapter.addData(data);
                adapter.loadMoreComplete();
                recyclerView.scrollBy(0, 20);//防止加载更多时候有些情况不会更新item到视图中
            } else {
                adapter.loadMoreEnd();
            }
        }
    }

    private void searchIdelMatch(final String keys) {
        RxRequestApi.getInstance().getApiService().searchIdelMatch(keys.length()>10?keys.substring(0,10):keys).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).cache()
                .subscribe(new Action1<ResponseBody<List<ReIdleClassify>>>() {
                    @Override
                    public void call(ResponseBody<List<ReIdleClassify>> body) {
                        if (body.isSuccess()) {
                            final CowryClassificationRcyAdapter adapter = new CowryClassificationRcyAdapter(body.data);
                            searchRecyclerView.setAdapter(adapter);
                            adapter.setOnRecyclerViewItemClickListener(new BaseQuickAdapter.OnRecyclerViewItemClickListener() {
                                @Override
                                public void onItemClick(View view, int position) {
                                    ReIdleClassify item = adapter.getItem(position);
                                    reset(item.name,item.id);
                                }
                            });
                        } else {
                            searchRecyclerView.setAdapter(null);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        searchRecyclerView.setAdapter(null);
                    }
                });
    }

    private void reset(String strKey,String strTypeId){
        key =strKey;
        typeId = strTypeId;
        pageNum = 1;
        etSearchCotent.clearFocus();
        etSearchCotent.setText(key);
        saveHistory(key);
        hideKeyboard();
        refreshData();
    }

    private void saveHistory(String key) {
        List<Label> labels = new ArrayList<Label>();
        labels = SPUtils.getSerializable(SPUtils.KeyConstant.searchIdleHistory, labels.getClass());
        if (labels == null) labels = new ArrayList<>();
        Label label = new Label();
        label.name = key;
        labels.remove(label);
        if(labels.size()==10) labels.remove(9);
        labels.add(0, label);
        SPUtils.saveSerializable(SPUtils.KeyConstant.searchIdleHistory, labels);
    }

    Subscription subscriptionMsgCount;
    @Override
    protected void onResume() {
        super.onResume();
        if(subscriptionMsgCount != null) subscriptionMsgCount.unsubscribe();
        subscriptionMsgCount = IMHelper.getInstance().isUnreadMsg().subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean aBoolean) {
                if(aBoolean) ivRight.setImageResource(R.drawable.home_03);
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
        if(subscriptionMsgCount != null) subscriptionMsgCount.unsubscribe();
        super.onDestroy();
    }
}
