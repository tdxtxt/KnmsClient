package com.knms.activity;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshRecyclerView;
import com.knms.activity.base.HeadNotifyBaseActivity;
import com.knms.adapter.ComplaintsAdapter;
import com.knms.adapter.OldComplaintsAdapter;
import com.knms.bean.ResponseBody;
import com.knms.bean.order.Complaints;
import com.knms.bean.order.Order;
import com.knms.bean.user.User;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.net.RxRequestApi;
import com.knms.util.CommonUtils;
import com.knms.util.DialogHelper;
import com.knms.util.Tst;
import com.knms.android.R;

import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * 订单详情
 */
public class OrderParticularsActivity extends HeadNotifyBaseActivity implements
        OnClickListener {

    private Intent mIntent;

    private TextView relationName, relationMobile, orderTime, deliveryTime, deliveryAddress,
            contractNumber, contractAmount, accountPaid, contractStatus, mRefund;
    private LinearLayout mComplaintPhone;
    private TextView mHotLine, mQuickComplaints, mOrderRemark;
    private RecyclerView recyclerView;
    private OldComplaintsAdapter mAdapter;

    private String orderId;
    private Order orderDetails;
    private View headView;

    private PullToRefreshRecyclerView pullToRefreshRecyclerView;

    @Override
    protected void getParmas(Intent intent) {
        orderDetails = (Order) intent.getSerializableExtra("orderDetails");
        orderId = orderDetails.orid;
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_order_particulars;
    }

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("订单详情");
    }

    @Override
    protected void initView() {
        headView = getLayoutInflater().inflate(R.layout.recyclerview_order_head, null);

        relationName = (TextView) headView.findViewById(R.id.relation_name);
        relationMobile = (TextView) headView.findViewById(R.id.relation_mobile);
        orderTime = (TextView) headView.findViewById(R.id.order_time);
        deliveryTime = (TextView) headView.findViewById(R.id.delivery_time);
        deliveryAddress = (TextView) headView.findViewById(R.id.delivery_address);
        contractNumber = (TextView) headView.findViewById(R.id.contract_number);
        contractAmount = (TextView) headView.findViewById(R.id.contract_amount);
        accountPaid = (TextView) headView.findViewById(R.id.account_paid);
        contractStatus = (TextView) headView.findViewById(R.id.contract_status);
        mRefund = (TextView) headView.findViewById(R.id.refund);
        mOrderRemark = (TextView) headView.findViewById(R.id.tv_order_remark);

        mComplaintPhone = findView(R.id.complaint_phone);
        mHotLine = findView(R.id.hot_line);
        mQuickComplaints = findView(R.id.quick_complaint);

        pullToRefreshRecyclerView = findView(R.id.pull_to_refresh);
        recyclerView = pullToRefreshRecyclerView.getRefreshableView();


        mAdapter = new OldComplaintsAdapter(new ArrayList<Complaints>());
        mAdapter.addHeaderView(headView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter);

        pullToRefreshRecyclerView.setMode(PullToRefreshBase.Mode.BOTH);

        pullToRefreshRecyclerView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<RecyclerView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<RecyclerView> refreshView) {
                reqApi();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<RecyclerView> refreshView) {
                pullToRefreshRecyclerView.onRefreshComplete();
            }
        });

        if (orderDetails.complaintstate == 1) {
            mQuickComplaints.setText("快捷投诉");
            mQuickComplaints.setTextColor(0xff333333);
            mQuickComplaints.setBackgroundResource(R.color.yellow);
        } else if (orderDetails.complaintstate == 2) {
            mQuickComplaints.setText("快捷投诉");
            mQuickComplaints.setBackgroundResource(R.color.gray_c7c7c7);
            mQuickComplaints.setTextColor(getResources().getColor(R.color.color_white));
        } else if (orderDetails.complaintstate == 3) {
            mQuickComplaints.setText("投诉");
            mQuickComplaints.setBackgroundResource(R.color.gray_c7c7c7);
            mQuickComplaints.setTextColor(getResources().getColor(R.color.color_white));
        }

        if (orderDetails.contractstatus == 1) {
            contractStatus.setText("付款中");
        } else if (orderDetails.contractstatus == 2) {
            contractStatus.setText("收款完成");
        } else if (orderDetails.contractstatus == 3) {
            contractStatus.setText("合同作废");
        }

    }

    @Override
    protected void initData() {
        relationName.setText(orderDetails.orreceivercontacts);
        relationMobile.setText(orderDetails.orreceiverphone);
        orderTime.setText(orderDetails.orcreated);
        deliveryTime.setText(orderDetails.ordeliverytime);
        deliveryAddress.setText(orderDetails.ordeliverylocation);
        contractNumber.setText(orderDetails.orcontractno);
        contractAmount.setText(CommonUtils.addMoneySymbol(orderDetails.orcontractamount));
        accountPaid.setText(CommonUtils.addMoneySymbol(orderDetails.oramountpaid));
        mRefund.setText(CommonUtils.addMoneySymbol(orderDetails.orrefundamount));
        headView.findViewById(R.id.refund_layout).setVisibility(orderDetails.orrefundamount.equals("0") ? View.GONE : View.VISIBLE);
        mComplaintPhone.setOnClickListener(this);
        mQuickComplaints.setOnClickListener(this);
        mHotLine.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);//添加下划线
//        if (!TextUtils.isEmpty(orderDetails.preferremark)&&TextUtils.equals("0",orderDetails.preferremark)) {
//            mOrderRemark.setVisibility(View.VISIBLE);
//            mOrderRemark.setText(orderDetails.preferremark);
//        }
        pullToRefreshRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                pullToRefreshRecyclerView.setRefreshing();
            }
        }, 200);
    }

    @Override
    public String setStatisticsTitle() {
        return "订单详情";
    }

    @Override
    protected void reqApi() {
        RxRequestApi.getInstance().getApiService().getComplaintsList(orderId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<List<Complaints>>>() {
                    @Override
                    public void call(ResponseBody<List<Complaints>> listResponseBody) {
                        pullToRefreshRecyclerView.onRefreshComplete();
                        if (listResponseBody.isSuccess())
                            updateView(listResponseBody.data);
                        else
                            Tst.showToast(listResponseBody.desc);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        pullToRefreshRecyclerView.onRefreshComplete();
                        Tst.showToast(throwable.getMessage());
                    }
                });
    }


    private void updateView(List<Complaints> data) {
        mAdapter.setNewData(data);
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
                        mIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
                                + "023-62410804"));
                        startActivity(mIntent);
                    }
                });

                break;
            case R.id.quick_complaint:
                if (orderDetails.complaintstate == 2 || orderDetails.complaintstate == 3) {
                    Tst.showToast("暂不能投诉");
                } else {
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("tel", orderDetails.orreceiverphone);
                    params.put("name", orderDetails.orreceivercontacts);
                    params.put("orderId", orderId);
                    params.put("orderType", 1);
                    startActivityAnimGeneral(QuickComplaintActivity.class, params);
                    finshActivity();
                }
                break;
        }

    }

    @Subscribe(tags = {@Tag(BusAction.ACTION_LOGIN)})
    public void loginAccout(User user) {
        finshActivity();
    }

    @Override
    public void finshActivity() {
        super.finshActivity();
    }


}
