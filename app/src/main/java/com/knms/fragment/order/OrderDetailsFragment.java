package com.knms.fragment.order;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.knms.activity.QuickComplaintActivity;
import com.knms.android.R;
import com.knms.bean.ResponseBody;
import com.knms.bean.order.Order;
import com.knms.bean.order.OrderDetail;
import com.knms.fragment.base.BaseFragment;
import com.knms.net.RxRequestApi;
import com.knms.util.CommonUtils;
import com.knms.util.DialogHelper;
import com.knms.util.Tst;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2017/4/21.
 */

public class OrderDetailsFragment extends BaseFragment implements View.OnClickListener {
    private View view;
    private String orderId;
    private TextView relationName, relationMobile, orderTime, deliveryTime, deliveryAddress,
            contractNumber, contractAmount, accountPaid, tvPreferamount, mRefund,
            tvTelephonehotline, tvquickComplaint;
    private LinearLayout refundLayout;
    private boolean isComplaint = false;
    private OrderDetail orderDetails;
    private PullToRefreshScrollView pullToRefreshScrollView;

    public static OrderDetailsFragment newInstance(String orderId) {
        OrderDetailsFragment fragment = new OrderDetailsFragment();
        Bundle args = new Bundle();
        args.putString("orderId", orderId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            orderId = getArguments().getString("orderId");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_order_details_layout, null);
        initView();
        return view;
    }

    public void initView() {
        refundLayout = (LinearLayout) view.findViewById(R.id.refund_layout);
        pullToRefreshScrollView = (PullToRefreshScrollView) view.findViewById(R.id.pulltorefresh_order_scrollview);
        tvPreferamount = (TextView) view.findViewById(R.id.tv_preferamount);
        tvquickComplaint = (TextView) view.findViewById(R.id.tv_quick_complaint);
        tvTelephonehotline = (TextView) view.findViewById(R.id.tv_telephone_hotline);
        relationName = (TextView) view.findViewById(R.id.relation_name);
        relationMobile = (TextView) view.findViewById(R.id.relation_mobile);
        orderTime = (TextView) view.findViewById(R.id.order_time);
        deliveryTime = (TextView) view.findViewById(R.id.delivery_time);
        deliveryAddress = (TextView) view.findViewById(R.id.delivery_address);
        contractNumber = (TextView) view.findViewById(R.id.contract_number);
        contractAmount = (TextView) view.findViewById(R.id.contract_amount);
        accountPaid = (TextView) view.findViewById(R.id.account_paid);
        mRefund = (TextView) view.findViewById(R.id.refund);
        pullToRefreshScrollView.setMode(PullToRefreshBase.Mode.BOTH);
        tvTelephonehotline.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        tvquickComplaint.setOnClickListener(this);
        tvTelephonehotline.setOnClickListener(this);
        reqApi();
    }

    @Override
    public void reqApi() {
        RxRequestApi.getInstance().getApiService().getOrderDetails(orderId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<OrderDetail>>() {
                    @Override
                    public void call(ResponseBody<OrderDetail> orderDetailResponseBody) {
                        if (orderDetailResponseBody.isSuccess()) {
                            orderDetails = orderDetailResponseBody.data;
                            initData();
                        } else Tst.showToast(orderDetailResponseBody.desc);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
    }

    public void initData() {
        if (orderDetails != null) {
            isComplaint = orderDetails.isComplaint == 2;
            if (isComplaint) {
                tvquickComplaint.setBackgroundResource(R.drawable.bg_rectangle_gray);
                tvquickComplaint.setTextColor(Color.parseColor("#999999"));
            }
            relationName.setText(orderDetails.orreceivercontacts);
            relationMobile.setText(orderDetails.orreceiverphone);
            orderTime.setText(orderDetails.orcreated);
            deliveryTime.setText(orderDetails.ordeliverytime);
            deliveryAddress.setText(orderDetails.ordeliverylocation);
            contractNumber.setText(orderDetails.orcontractno);
            contractAmount.setText(CommonUtils.addMoneySymbol(orderDetails.orcontractamount));
            accountPaid.setText(CommonUtils.addMoneySymbol(orderDetails.oramountpaid));
            if (TextUtils.equals(orderDetails.orrefundamount, "0"))
                refundLayout.setVisibility(View.GONE);
            else
                mRefund.setText(CommonUtils.addMoneySymbol(orderDetails.orrefundamount));
            if (TextUtils.equals(orderDetails.preferamount, "0"))
                tvPreferamount.setVisibility(View.GONE);
            else
                tvPreferamount.setText("(含使用买手优惠券" + orderDetails.preferamount + "元)");
        }
    }

    @Override
    public String getTitle() {
        return "订单详情";
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_quick_complaint:
                if (isComplaint) {
                    Tst.showToast("暂不能投诉");
                    return;
                }
                Intent intent = new Intent(getActivity(), QuickComplaintActivity.class);
                intent.putExtra("tel", orderDetails.orreceiverphone);
                intent.putExtra("name", orderDetails.orreceivercontacts);
                intent.putExtra("orderId", orderDetails.orid);
                intent.putExtra("orderType", 1);
                startActivity(intent);
                getActivity().finish();
                break;
            case R.id.tv_telephone_hotline:
                DialogHelper.showPromptDialog(getActivity(), "", "是否拨打023-62410804", "取消", "", "确定", new DialogHelper.OnMenuClick() {
                    @Override
                    public void onLeftMenuClick() {

                    }

                    @Override
                    public void onCenterMenuClick() {

                    }

                    @Override
                    public void onRightMenuClick() {
                        Intent mIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:023-62410804"
                        ));
                        startActivity(mIntent);
                    }
                });
                break;
        }
    }
}
