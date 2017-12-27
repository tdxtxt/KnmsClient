package com.knms.activity;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshRecyclerView;
import com.knms.activity.base.HeadBaseActivity;
import com.knms.adapter.MoreLabelAdapter;
import com.knms.bean.ResponseBody;
import com.knms.bean.other.Label;
import com.knms.net.RxRequestApi;
import com.knms.util.Tst;
import com.knms.android.R;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by tdx on 2016/9/19.
 */
public class MoreLableActivity extends HeadBaseActivity{
    MoreLabelAdapter adapter;
    PullToRefreshRecyclerView pullToRefreshRecycleView;
    RecyclerView recyclerView;
    List<Label> selects;
    Subscription subscription;

    @Override
    protected void getParmas(Intent intent) {
        selects = (List<Label>) intent.getSerializableExtra("selects");
        if(selects == null) selects = new ArrayList<>();
    }

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("选择需求标签");
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_common_layout;
    }

    @Override
    protected void initView() {
        pullToRefreshRecycleView = findView(R.id.refresh_recyclerView);
        recyclerView = pullToRefreshRecycleView.getRefreshableView();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void initData() {
        setRightMenuCallBack(new RightCallBack() {
            @Override
            public void setRightContent(TextView tv, ImageView icon) {
                icon.setVisibility(View.GONE);
                tv.setText("确认");
            }
            @Override
            public void onclick() {
                if(adapter == null) return;
                if(adapter.selects == null) return;
                Intent intent = new Intent();
                intent.putExtra("selects", (Serializable) adapter.selects);
                setResult(RESULT_OK,intent);
                finshActivity();
            }
        });
        pullToRefreshRecycleView.postDelayed(new Runnable() {
            @Override
            public void run() {
              pullToRefreshRecycleView.setRefreshing();
            }
        },1000);
        pullToRefreshRecycleView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<RecyclerView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<RecyclerView> refreshView) {
                reqApi();
            }
            @Override
            public void onPullUpToRefresh(PullToRefreshBase<RecyclerView> refreshView) {
            }
        });
    }

    @Override
    public String setStatisticsTitle() {
        return "选择需求标签";
    }

    @Override
    protected void reqApi() {
        subscription = RxRequestApi.getInstance().getApiService().getAllLabels().observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<ResponseBody<List<Label>>>() {
                    @Override
                    public void call(ResponseBody<List<Label>> body) {
                        pullToRefreshRecycleView.onRefreshComplete();
                        if(body.isSuccess()){
                            updateView(body.data);
                        }else{
                            Tst.showToast(body.desc);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        pullToRefreshRecycleView.onRefreshComplete();
                        Tst.showToast(throwable.toString());
                    }
                });
    }

    private void updateView(List<Label> data) {
        adapter = new MoreLabelAdapter(data,selects);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        if(subscription != null) subscription.unsubscribe();
        super.onDestroy();
    }
}
