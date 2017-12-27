package com.knms.activity.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.knms.activity.base.BaseActivity;
import com.knms.android.R;
import com.knms.app.KnmsApp;
import com.knms.bean.ResponseBody;
import com.knms.bean.order.neworder.RefundReasons;
import com.knms.net.RxRequestApi;
import com.knms.util.Tst;
import com.knms.view.wheel.SpWheelView;
import com.knms.view.wheel.WheelView;
import com.knms.view.wheel.adapter.AbstractWheelTextAdapter;

import java.util.ArrayList;
import java.util.List;

import rx.functions.Action1;

/**
 * Created by Administrator on 2017/8/29.
 */

public class ChooseReasonActivityDialog extends BaseActivity {

    private SpWheelView wheelView;
//    private RecedeReasonAdapter mAdapter;

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
    protected int layoutResID() {
        return R.layout.dialog_choose_refund_reason;
    }

    @Override
    protected void initView() {
        wheelView = findView(R.id.wv_all_reason);
        wheelView.setOffset(2);
        TextView cancel = findView(R.id.tvBtn_cancel);
        TextView confirm = findView(R.id.tvBtn_confirm);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KnmsApp.getInstance().finishActivity(ChooseReasonActivityDialog.this);

            }
        });
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getIntent().putExtra("reasons", wheelView.getSeletedItem());
                setResult(RESULT_OK, getIntent());
                KnmsApp.getInstance().finishActivity(ChooseReasonActivityDialog.this);

            }
        });

    }

    @Override
    protected void initData() {
        reqApi();
    }

    @Override
    protected void reqApi() {
        RxRequestApi.getInstance().getApiService().getRefundsReasons()
                .compose(this.<ResponseBody<RefundReasons>>applySchedulers())
                .subscribe(new Action1<ResponseBody<RefundReasons>>() {
                    @Override
                    public void call(ResponseBody<RefundReasons> refundReasonsResponseBody) {
                        if (refundReasonsResponseBody.isSuccess1()) {
                            if(refundReasonsResponseBody.data != null && refundReasonsResponseBody.data.refunReasonList != null){
                                List<String> itmes = refundReasonsResponseBody.data.refunReasonList;
                                wheelView.setItems(itmes);
                                if (itmes.size() > 1) {
                                    wheelView.setSeletion(itmes.size() / 2);
                                }
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Tst.showToast(throwable.getMessage());
                    }
                });
    }
    @Override
    public String setStatisticsTitle() {
        return "选择退款理由";
    }
}
