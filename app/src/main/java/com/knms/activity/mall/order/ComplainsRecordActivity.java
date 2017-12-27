package com.knms.activity.mall.order;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshRecyclerView;
import com.knms.activity.base.HeadNotifyBaseActivity;
import com.knms.adapter.ComplaintsAdapter;
import com.knms.android.R;

/**
 * Created by Administrator on 2017/7/18.
 * App支付订单的投诉记录列表
 */

public class ComplainsRecordActivity extends HeadNotifyBaseActivity {

    private PullToRefreshRecyclerView refreshLayout;
    private RecyclerView recyclerView;
    private ComplaintsAdapter mAdapter;

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("投诉记录");
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_order_particulars;
    }

    @Override
    protected void initView() {
        refreshLayout=findView(R.id.pull_to_refresh);
        mAdapter=new ComplaintsAdapter(null);
        recyclerView=refreshLayout.getRefreshableView();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void reqApi() {

    }

    @Override
    public String setStatisticsTitle() {
        return "投诉记录";
    }
}
