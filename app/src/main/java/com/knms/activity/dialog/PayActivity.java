package com.knms.activity.dialog;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.knms.activity.base.BaseActivity;
import com.knms.android.R;
import com.knms.app.KnmsApp;
import com.knms.bean.ResponseBody;
import com.knms.bean.pay.OrderPayBody;
import com.knms.bean.pay.PayTypeBody;
import com.knms.core.pay.PayUtils;
import com.knms.net.RxRequestApi;
import com.knms.oncall.CallBack;
import com.knms.util.ConstantObj;
import com.knms.util.DialogHelper;
import com.knms.util.Tst;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


/**
 * Created by tdx on 2017/7/10.
 * 付款-支付选择界面
 * 传参:其中orderIds和orderId必须传一个，但不能都传
 * orderIds - List<String> 订单id数组
 * orderId - String  订单id
 * 返回值:
 * isPay - boolean 是否支付成功
 */
public class PayActivity extends BaseActivity implements CallBack<String> {
    RelativeLayout rlBtnWechatPay,rlBtnAlipay;
    public final String TypeWechatPay = "1";
    public final String TypeAliPay = "2";

    private List<String> orderIds;

    @Override
    protected void getParmas(Intent intent) {
        orderIds = intent.getStringArrayListExtra("orderIds");
        if(!(orderIds != null && orderIds.size() > 0)){
            if(orderIds == null)  orderIds = new ArrayList<>();
            String orderId = intent.getStringExtra("orderId");
            if(!TextUtils.isEmpty(orderId)) orderIds.add(orderId);
        }
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
    protected int layoutResID() {
        return R.layout.dialog_act_choice_pay;
    }

    @Override
    protected void initView() {
        rlBtnWechatPay = findView(R.id.rlBtn_wechat_pay);
        rlBtnAlipay = findView(R.id.rlBtn_alipay);

        rlBtnWechatPay.setVisibility(View.GONE);
        rlBtnAlipay.setVisibility(View.GONE);

        findView(R.id.ivBtn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWind();
            }
        });
    }

    @Override
    protected void initData() {
        findViewById(R.id.rl_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                showWind();
            }
        });
        findViewById(R.id.ll_layout).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        rlBtnAlipay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPayOrder(TypeAliPay);
            }
        });
        rlBtnWechatPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPayOrder(TypeWechatPay);
            }
        });
        reqApi();
    }
    private  void showWind(){
        DialogHelper.showPromptDialog(this, "确定要取消支付么?", "您的订单在24小时内未支付将被取消,请尽快完成支付", "继续支付", null, "确定",
                new DialogHelper.OnMenuClick() {
                    @Override
                    public void onLeftMenuClick() {
                    }
                    @Override
                    public void onCenterMenuClick() {}
                    @Override
                    public void onRightMenuClick() {
                        MobclickAgent.onEvent(PayActivity.this, "pay_cancelwindow");
                        getIntent().putExtra("isPay",false);
                        setResult(RESULT_OK, getIntent());
                        finish();
                    }
                });
    }
    @Override
    public void reqApi(){
        showProgress();
        Map<String,Object> params = new HashMap<>();
        params.put("terminalType","and");
        RxRequestApi.getInstance().getApiService().getPayMethod(params)
                .observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
                .subscribe(new Action1<ResponseBody<PayTypeBody>>() {
                    @Override
                    public void call(ResponseBody<PayTypeBody> body) {
                        hideProgress();
                        if (body.isSuccess1()) {
                            updateView(body.data);
                        }else{
                            Tst.showToast(body.desc);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        hideProgress();
                        Tst.showToast("请求失败");
                    }
                });
    }
    private void updateView(PayTypeBody data) {
        if(data != null){
            List<PayTypeBody.PayType> payMethods = data.payMethodBos;
            if(payMethods != null && payMethods.size() > 0){
                for (PayTypeBody.PayType type : payMethods) {
                    if(TypeWechatPay.equals(type.payType)) {//支持微信支付
                        if(PayUtils.isInstallWechat()){
                           rlBtnWechatPay.setVisibility(View.VISIBLE);
                        }
                    } else if(TypeAliPay.equals(type.payType)){//支持支付宝支付
                        rlBtnAlipay.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }
    private void createPayOrder(String payType){
        MobclickAgent.onEvent(PayActivity.this, "pay_clickAlipayAndWechat");
        showProgress();
        Map<String,Object> params = new HashMap<>();
        params.put("terminalType","and");
        params.put("userId", ConstantObj.TEMP_USERID);
        params.put("payType",payType);
        params.put("tradingIds",orderIds);
        RxRequestApi.getInstance().getApiService().createOrderPay(params)
                .compose(this.<ResponseBody<OrderPayBody>>applySchedulers())
                .subscribe(new Action1<ResponseBody<OrderPayBody>>() {
                    @Override
                    public void call(ResponseBody<OrderPayBody> body) {
                        hideProgress();
                        if(body.isSuccess1()){
                            if(body.data != null){
                                OrderPayBody.OrderPay orderPay = body.data.data;
                                if(orderPay != null){
                                    if(TypeAliPay.equals(orderPay.payType)){
                                        PayUtils.doAlipay(PayActivity.this,orderPay.payId,orderPay.paySysAppdata,PayActivity.this);
                                    }else if(TypeWechatPay.equals(orderPay.payType)){
                                        PayUtils.doWXPay(orderPay.payId,orderPay.paySysAppdata,PayActivity.this);
                                    }
                                }
                            }
                        }else{
                            Tst.showToast(body.desc);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        hideProgress();
                        Tst.showToast(throwable.getMessage());
                    }
                });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        showWind();
        showWind();
        return true;
    }

    @Override
    public void finshActivity() {
        KnmsApp.getInstance().finishActivity(this);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
    }
    long startMillis;
    Subscription subscription;
    /**
     * 本地支付成功回调
     * @param
     */
    @Override
    public void onCallBack(String payId) {
        Map<String,Object> params = new HashMap<>();
        params.put("userId",ConstantObj.TEMP_USERID);
        params.put("payId",payId);
        startMillis = System.currentTimeMillis();
        if(subscription != null) subscription.unsubscribe();
        subscription = RxRequestApi.getInstance().getApiService().checkPaySccess(params)
        .repeatWhen(new Func1<Observable<? extends Void>, Observable<?>>() {
            @Override
            public Observable<?> call(Observable<? extends Void> observable) {
                /**
                 * 这个方法只会被调用一次。
                 * 300 表示每次重复的调用（repeated call）会被延迟300ms。
                 */
                return observable.delay(300, TimeUnit.MILLISECONDS);
            }
        }).takeUntil(new Func1<ResponseBody, Boolean>() {
            @Override
            public Boolean call(ResponseBody body) {
                /**
                 *  true-停止轮询，false继续请求接口
                 */
                long diff = System.currentTimeMillis() - startMillis;
                long second = TimeUnit.MILLISECONDS.toSeconds(diff);
                if(second > 3){//大于了3秒，停止轮询
                    return true;
                }
                return body.isSuccess1();
            }
        }).filter(new Func1<ResponseBody, Boolean>() {
            @Override
            public Boolean call(ResponseBody body) {
                /**
                 * false -过滤，onNext() 不会被调用.但是 onComplete() 仍然会被传递
                 * true -不过滤，onNext() 会被调用，
                 */
                return body.isSuccess1();
            }
        })
        .compose(this.<ResponseBody>applySchedulers())
        .subscribe(new Action1<ResponseBody>() {
            @Override
            public void call(ResponseBody body) {
                if(body.isSuccess1()){//支付成功
                    getIntent().putExtra("isPay",true);
                    setResult(RESULT_OK, getIntent());
                    finish();
                }else{
                    Tst.showToast("支付校验中,请手动刷新查看数据");
                    getIntent().putExtra("isPay",false);
                    setResult(RESULT_OK, getIntent());
                    finish();
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Tst.showToast("网络不给力，请检查网络设置");
            }
        });
    }

    @Override
    protected void onDestroy() {
        if(subscription != null) subscription.unsubscribe();
        super.onDestroy();
    }
}
