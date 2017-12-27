package com.knms.activity.mall.order;

import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshRecyclerView;
import com.knms.activity.ComplaintDetailsActivity;
import com.knms.activity.QuickComplaintActivity;
import com.knms.activity.base.HeadBaseActivity;
import com.knms.activity.base.HeadNotifyBaseActivity;
import com.knms.adapter.ComplaintsAdapter;
import com.knms.android.R;
import com.knms.bean.ResponseBody;
import com.knms.bean.order.Complaints;
import com.knms.net.RxRequestApi;
import com.knms.util.DialogHelper;
import com.knms.util.Tst;
import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2017/8/21.
 * App支付订单的投诉记录列表
 */

public class ComplaintsListActivity extends HeadNotifyBaseActivity implements View.OnClickListener {

    private PullToRefreshRecyclerView refreshRecyclerView;
    private RecyclerView mRecyclerView;
    private ComplaintsAdapter complaintsAdapter;
    private TextView mQuickComplaints;
    private LinearLayout mComplaintPhone;


    List<Complaints> listComplaints;
    boolean isComplaints;
    String phoneNumber,name,orderId;

    @Override
    protected void getParmas(Intent intent) {
        super.getParmas(intent);
        isComplaints=intent.getBooleanExtra("isComplaints",false);
        listComplaints= (List<Complaints>) intent.getSerializableExtra("complaintsList");
        phoneNumber=intent.getStringExtra("tel");
        name=intent.getStringExtra("name");
        orderId=intent.getStringExtra("orderId");
    }

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("投诉记录");
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_complaints_list;
    }

    @Override
    protected void initView() {
        refreshRecyclerView = findView(R.id.pull_to_refresh_recyclerview);
        mRecyclerView = refreshRecyclerView.getRefreshableView();
        complaintsAdapter = new ComplaintsAdapter(listComplaints,true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(complaintsAdapter);
        mQuickComplaints = findView(R.id.quick_complaint);
        mComplaintPhone = findView(R.id.complaint_phone);
        ((TextView)findViewById(R.id.hot_line)).getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);//添加下划线

        complaintsAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener<Complaints>() {
            @Override
            public void onItemChildClick(BaseQuickAdapter<Complaints, ? extends BaseViewHolder> adapter, View view, int position) {
                switch (view.getId()){
                    case R.id.tv_complaints_details1:
                    case R.id.tv_complaints_details2:
                    case R.id.tv_complaints_details3:
                        MobclickAgent.onEvent(ComplaintsListActivity.this, "orderComplaintSeeDetailsClick");
                        Intent intent = new Intent(ComplaintsListActivity.this, ComplaintDetailsActivity.class);
                        intent.putExtra("complaintsId", complaintsAdapter.getItem(position).ocid);
                        startActivity(intent);
                        break;
                }
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
        mQuickComplaints.setOnClickListener(this);
        mComplaintPhone.setOnClickListener(this);
    }

    @Override
    public String setStatisticsTitle() {
        return "投诉记录";
    }

    @Override
    protected void reqApi() {
        RxRequestApi.getInstance().getApiService().getComplaintsList(orderId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<List<Complaints>>>() {
                    @Override
                    public void call(ResponseBody<List<Complaints>> listResponseBody) {
                        refreshRecyclerView.onRefreshComplete();
                        if(listResponseBody.isSuccess())complaintsAdapter.setNewData(listResponseBody.data);
                        else Tst.showToast(listResponseBody.desc);
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.complaint_phone:
                new DialogHelper().showPromptDialog(this, "", "是否拨打023-62410804", "取消", "", "确定", new DialogHelper.OnMenuClick() {
                    @Override
                    public void onLeftMenuClick() {

                    }

                    @Override
                    public void onCenterMenuClick() {

                    }

                    @Override
                    public void onRightMenuClick() {
                     Intent  mIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
                                + "023-62410804"));
                        startActivity(mIntent);
                    }
                });

                break;
            case R.id.quick_complaint:
                if (!isComplaints||complaintsAdapter.getItem(0).getItemType()==2||complaintsAdapter.getItem(0).getItemType()==1) {
                    Tst.showToast("暂不能投诉");
                } else {
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("tel", phoneNumber);
                    params.put("name", name);
                    params.put("orderId", orderId);
                    params.put("orderType",2);
                    startActivityAnimGeneral(QuickComplaintActivity.class, params);
                    finshActivity();
                }
                break;
        }

    }
}
