package com.knms.activity.dialog;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.knms.activity.base.BaseActivity;
import com.knms.activity.mall.order.PayRetainageActivity;
import com.knms.adapter.NoPaymentOrderAdapter;
import com.knms.android.R;
import com.knms.app.KnmsApp;
import com.knms.bean.ResponseBody;
import com.knms.bean.order.Order;
import com.knms.net.RxRequestApi;
import com.knms.util.Tst;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2017/8/31.
 */

public class ChoosePayOrderActivityDialog extends BaseActivity {

    private RecyclerView recyclerView;
    private int pageIndex = 1;
    private NoPaymentOrderAdapter mAdapter;
    private ImageView ivCancel;
    private String orderId;

    @Override
    protected void getParmas(Intent intent) {
        orderId = intent.getStringExtra("orderid");
    }

    @Override
    protected int layoutResID() {
        return R.layout.dialog_pay_choose_order;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager m = getWindowManager();
        Display d = m.getDefaultDisplay(); // 为获取屏幕宽、高
        WindowManager.LayoutParams p = getWindow().getAttributes(); // 获取对话框当前的参数值
        p.width = (int) (d.getWidth() * 1.0); // 满屏宽度
        getWindow().setAttributes(p);
    }

    @Override
    protected void initView() {
        ivCancel = findView(R.id.iv_cancel);
        recyclerView = findView(R.id.rv_unpaid_all_order);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new NoPaymentOrderAdapter(null, orderId);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setEnableLoadMore(true);
        mAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                pageIndex++;
                reqApi();
            }
        }, recyclerView);
        findView(R.id.rl_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finshActivity();
            }
        });
    }

    @Override
    protected void initData() {
        reqApi();
        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<Order>() {
            @Override
            public void onItemClick(BaseQuickAdapter<Order, ? extends BaseViewHolder> adapter, View view, int position) {
                getIntent().putExtra("order", mAdapter.getItem(position));
                setResult(RESULT_OK, getIntent());
                finshActivity();
            }
        });
        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener<Order>() {
            @Override
            public void onItemChildClick(BaseQuickAdapter<Order, ? extends BaseViewHolder> adapter, View view, int position) {
                getIntent().putExtra("order", mAdapter.getItem(position));
                setResult(RESULT_OK, getIntent());
                finshActivity();
            }
        });
        ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finshActivity();
            }
        });
    }

    @Override
    protected void reqApi() {
        RxRequestApi.getInstance().getApiService().getMarketOrderList(pageIndex)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<List<Order>>>() {
                    @Override
                    public void call(ResponseBody<List<Order>> orderResponseBody) {
                        if (orderResponseBody.isSuccess()) updateView(orderResponseBody.data);
                        else Tst.showToast(orderResponseBody.desc);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Tst.showToast(throwable.getMessage());
                    }
                });
    }

    private void updateView(List<Order> data) {
        if (data == null) return;
        if (pageIndex == 1) {
            mAdapter.setNewData(data);
        } else {
            if (data != null && data.size() > 0) {
                mAdapter.addData(data);
                mAdapter.loadMoreComplete();
            } else {
                mAdapter.loadMoreEnd();
            }
        }

    }

    @Override
    public String setStatisticsTitle() {
        return "选择付尾款订单";
    }


    @Override
    public void finshActivity() {
        KnmsApp.getInstance().finishActivity(this);
    }

    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
    }
}
