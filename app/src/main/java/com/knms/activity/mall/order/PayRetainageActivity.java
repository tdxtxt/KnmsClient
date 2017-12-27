package com.knms.activity.mall.order;

import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.knms.activity.base.HeadBaseActivity;
import com.knms.activity.dialog.ChoosePayOrderActivityDialog;
import com.knms.activity.dialog.PayActivity;
import com.knms.android.R;
import com.knms.bean.ResponseBody;
import com.knms.bean.basebody.StringBody;
import com.knms.bean.order.Order;
import com.knms.bean.order.OrderRetainageBody;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.net.RxRequestApi;
import com.knms.util.CommonUtils;
import com.knms.util.Tst;
import com.knms.view.XEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.RequestBody;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

import static android.R.attr.filter;

/**
 * Created by Administrator on 2017/7/17.
 * 付尾款
 */

public class PayRetainageActivity extends HeadBaseActivity implements View.OnClickListener {
    public final int TOPAY = 0x000989;
    private FrameLayout mChooserOrder;
    private TextView tvToPay, tvPleaseChoose, tvOrderCreateTime, tvOrderShopName;
    private Order orderDetails;
    private XEditText etPaymentAmount;

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("付款");
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_order_payretainage;
    }

    @Override
    protected void initView() {
        mChooserOrder = findView(R.id.chooser_pay_order);
        tvToPay = findView(R.id.tv_topay);
        tvPleaseChoose = findView(R.id.tv_please_choose);
        tvOrderCreateTime = findView(R.id.tv_order_createtime);
        tvOrderShopName = findView(R.id.tv_order_shopname);
        etPaymentAmount=findView(R.id.et_payment_amount);
    }

    @Override
    protected void initData() {
        mChooserOrder.setOnClickListener(this);
        tvToPay.setOnClickListener(this);
        etPaymentAmount.setTextChangedListener(new XEditText.TextChangedListener() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void onTextChangedAfter(Editable editable) {
                String price = editable.toString().trim();
                if (price.length() > 6&&Integer.parseInt(editable.toString())>999999) {
                    editable.delete(price.length() - 1, price.length());
                    Tst.showToast("最大付款金额不超过999999");
                }
            }
        });
    }

    @Override
    public String setStatisticsTitle() {
        return "付款";
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_topay:
                if(!(orderDetails != null && !TextUtils.isEmpty(orderDetails.orid))){
                    Tst.showToast("请选择付款订单");
                    return;
                }
                String moneyStr = etPaymentAmount.getText().toString();
                if(TextUtils.isEmpty(moneyStr)){
                    Tst.showToast("请输入付款金额");
                    return;
                }
                money = CommonUtils.str2Integer(moneyStr);//
                confirmAndCreateOrder();
                break;
            case R.id.chooser_pay_order:
                Intent intent = new Intent(PayRetainageActivity.this, ChoosePayOrderActivityDialog.class);
                intent.putExtra("orderid", orderDetails == null ? "" : orderDetails.orid);
                startActivityForResult(intent,CHOOSE_PAY_ORDER);
                break;
        }
    }

    public final int CHOOSE_PAY_ORDER = 0x000099;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        switch (requestCode){
            case CHOOSE_PAY_ORDER:
                orderDetails = (Order) data.getSerializableExtra("order");
                if (orderDetails != null) {
                    tvPleaseChoose.setVisibility(View.GONE);
                    tvOrderShopName.setText(orderDetails.orshopname + "  合同号" + orderDetails.orcontractno);
                    tvOrderCreateTime.setText("订货时间：" + orderDetails.orcreated + "合同金额：¥" + orderDetails.orcontractamount);
                }
                break;
            case TOPAY:
                /*boolean isPay = data.getBooleanExtra("isPay",false);
                if (isPay)*/RxBus.get().post(BusAction.CREATE_ORDER_RETAINAGE,"notify");//支付成功

                finshActivity();
                break;
        }
    }

    String systemEnventId;
    int money;
    long startMillis;
    Subscription subscription;
    private void confirmAndCreateOrder(){
        if(money <= 0){
            Tst.showToast("请输入大于0的整数");
            return;
        }
        showProgress();
        startMillis = System.currentTimeMillis();
        if(subscription != null) subscription.unsubscribe();
        subscription = Observable.just(systemEnventId)
                .flatMap(new Func1<String, Observable<ResponseBody<StringBody>>>() {
                    @Override
                    public Observable<ResponseBody<StringBody>> call(String str) {
                        if (TextUtils.isEmpty(str)){
                            return RxRequestApi.getInstance().getApiService().getSystemEventId();
                        }else {
                            ResponseBody<StringBody> body = new ResponseBody<>();
                            StringBody data = new StringBody();
                            data.systemEventId = str;
                            body.data = data;
                            return Observable.just(body);
                        }
                    }
                }).flatMap(new Func1<ResponseBody<StringBody>, Observable<ResponseBody<OrderRetainageBody>>>() {
                    @Override
                    public Observable<ResponseBody<OrderRetainageBody>> call(ResponseBody<StringBody> body) {
                        if (body.isSuccess1()) systemEnventId = body.data.systemEventId;
                        Map<String, Object> params = new HashMap<>();
                        params.put("orderCreatedEventId", systemEnventId);
                        params.put("money", money);
                        params.put("orderId", orderDetails.orid);
                        params.put("addressId", "addressidnull");
                        return RxRequestApi.getInstance().getApiService().retainageOrderConfirmAndCreate(params)
                                /*.retryWhen(new Func1<Observable<? extends Throwable>, Observable<?>>() {
                                    @Override
                                    public Observable<?> call(Observable<? extends Throwable> errors) {
                                        return errors.flatMap(new Func1<Throwable, Observable<?>>() {
                                            @Override
                                            public Observable<?> call(Throwable error) {
                                                return Observable.timer(1, TimeUnit.SECONDS);
                                            }
                                        });
                                    }
                                })*/.repeatWhen(new Func1<Observable<? extends Void>, Observable<?>>() {
                                    @Override
                                    public Observable<?> call(Observable<? extends Void> observable) {
                                        /**
                                         * 这个方法只会被调用一次。
                                         * 1 表示每次重复的调用（repeated call）会被延迟1s。
                                         */
                                        return observable.delay(200, TimeUnit.MILLISECONDS);
                                    }
                                }).takeUntil(new Func1<ResponseBody<OrderRetainageBody>, Boolean>() {
                                    @Override
                                    public Boolean call(ResponseBody<OrderRetainageBody> body) {
                                        /**
                                         *  true-停止轮询，false继续请求接口
                                         */
                                        long diff = System.currentTimeMillis() - startMillis;
                                        long second = TimeUnit.MILLISECONDS.toSeconds(diff);
                                        if(second > 5){//大于了5秒，停止轮询
                                            return true;
                                        }
                                        return body.isSuccess1();
                                    }
                                }).filter(new Func1<ResponseBody<OrderRetainageBody>, Boolean>() {
                                    @Override
                                    public Boolean call(ResponseBody<OrderRetainageBody> body) {
                                        /**
                                         * false -过滤，onNext() 不会被调用.但是 onComplete() 仍然会被传递
                                         * true -不过滤，onNext() 会被调用，
                                         */
                                        return body.isSuccess1();
                                    }
                                });
                    }
                })
                .compose(this.<ResponseBody<OrderRetainageBody>>applySchedulers())
                .subscribe(new Subscriber<ResponseBody<OrderRetainageBody>>() {
                    @Override
                    public void onCompleted() {
                        hideProgress();
                    }
                    @Override
                    public void onError(Throwable e) {
                        hideProgress();
                        Tst.showToast(e.toString());
                    }
                    @Override
                    public void onNext(ResponseBody<OrderRetainageBody> body) {
                        hideProgress();
                        if (body.isSuccess1() && "1".equals(body.data.tradingStatus)) {
                            Intent intent = new Intent(PayRetainageActivity.this, PayActivity.class);
                            if (body.data.tradingIds != null) {
                                intent.putStringArrayListExtra("orderIds", (ArrayList<String>) body.data.tradingIds);
                            }
                            startActivityForResult(intent, TOPAY);
                        }else{
                            if(body.isSuccess1()) Tst.showToast(body.desc);
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        if(subscription != null) subscription.unsubscribe();
        super.onDestroy();
    }
}
