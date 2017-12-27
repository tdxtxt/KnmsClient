package com.knms.activity.fastfind;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.knms.activity.ShopActivityF;
import com.knms.activity.base.HeadBaseActivity;
import com.knms.adapter.PushMerchantAdapter;
import com.knms.android.R;
import com.knms.app.KnmsApp;
import com.knms.bean.ResponseBody;
import com.knms.bean.SaveParity;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.net.RxRequestApi;
import com.knms.oncall.LoadListener;
import com.knms.util.SPUtils;
import com.knms.util.Tst;
import com.knms.view.CirclePercentViews;
import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2017/3/23.
 */

public class NotifyMerchantActivity extends HeadBaseActivity {

    private String goid;
    private PushMerchantAdapter merchantAdapter;
    private RecyclerView merchantList;
    private ImageView ivProgressBar;
    private CirclePercentViews mCirclePercentView;
    private int merchantNumber = 0;
    private TextView tvMerchantNumber;
    private LinearLayoutManager linearLayoutManager;
    private Random random = new Random();
    private  int current = 1;
    private ImageView ivBack;
    private RelativeLayout rl_status;
    private boolean exists;

    @Override
    protected void getParmas(Intent intent) {
        super.getParmas(intent);
        goid = intent.getStringExtra("goid");
        exists=intent.getBooleanExtra("exists",false);
    }

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("极速比货");
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_bbgoods_success_layout;
    }

    @Override
    protected void initView() {
        ivBack= findView(R.id.iv_back);
        rl_status=findView(R.id.rl_status);
        tvMerchantNumber = findView(R.id.tv_push_merchant_number);
        ivProgressBar = findView(R.id.imageview_anim);
        mCirclePercentView = (CirclePercentViews) findViewById(R.id.circleView);
        merchantList = findView(R.id.all_merchant);
        merchantAdapter = new PushMerchantAdapter(this,null);
        linearLayoutManager = new LinearLayoutManager(this);
        merchantList.setLayoutManager(linearLayoutManager);
        merchantList.setAdapter(merchantAdapter);
        merchantAdapter.openLoadAnimation(BaseQuickAdapter.SLIDEIN_RIGHT);
        merchantList.setAdapter(merchantAdapter);
        ivBack.setEnabled(false);
        merchantAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                MobclickAgent.onEvent(NotifyMerchantActivity.this, "quickCompareResultAnyMerchantClick");
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("shopId", merchantAdapter.getData().get(position).shopId);
                startActivityAnimGeneral(ShopActivityF.class, map);
            }
        });
    }

    @Override
    protected void initData() {
        ((TextView)findView(R.id.tv_circle_title)).setText(exists?"已为您推荐商家":"已为您通知商家");
        ivProgressBar.postDelayed(new Runnable() {
            @Override
            public void run() {
                Animation rotate = AnimationUtils.loadAnimation(NotifyMerchantActivity.this, R.anim.progress_bar_rotate);
                ivProgressBar.startAnimation(rotate);
                reqApi();
            }
        },100);

    }

    @Override
    protected void reqApi() {
        KnmsApp.getInstance().hideLoadView(rl_status);
        RxRequestApi.getInstance().getApiService().saveParity(goid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<List<SaveParity>>>() {
                    @Override
                    public void call(ResponseBody<List<SaveParity>> saveParityResponseBody) {
                        ivBack.setEnabled(true);
                        if (saveParityResponseBody.isSuccess()) {
                            update(saveParityResponseBody.data);
                        } else{
                            failToLoad();
//                            Tst.showToast(saveParityResponseBody.desc);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        ivBack.setEnabled(true);
                        failToLoad();
                        Tst.showToast(throwable.getMessage());
                    }
                });
    }

    private void failToLoad(){
        ivProgressBar.clearAnimation();
        ivProgressBar.setVisibility(View.GONE);
        mCirclePercentView.setVisibility(View.VISIBLE);
        mCirclePercentView.setPercent(1);
        KnmsApp.getInstance().showDataEmpty(rl_status, "加载不成功", R.drawable.no_data_5, "点击重试", new LoadListener() {
            @Override
            public void onclick() {
                reqApi();
            }
        });
    }

    private void update(List<SaveParity> data) {
        merchantAdapter.addData(data);
        merchantNumber = data.size();
        int delayTime = random.nextInt(2) + 2;
        // 这一页的数量
        merchantList.postDelayed(new Runnable() {
            @Override
            public void run() {
                int layoutCount = linearLayoutManager.findLastVisibleItemPosition();
                merchantAdapter.setLayoutCount(layoutCount);
            }
        },50);
        final Subscription subscriber = Observable.timer(delayTime*1000/merchantNumber, TimeUnit.MILLISECONDS)
                .repeat()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        current+=1;
                        tvMerchantNumber.setText(current <= merchantNumber ? current + "" : merchantNumber + "");
                    }
                });
        Observable.just("").delay(delayTime, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        tvMerchantNumber.setText(merchantNumber + "");
                        ivProgressBar.clearAnimation();
                        ivProgressBar.setVisibility(View.GONE);
                        mCirclePercentView.setVisibility(View.VISIBLE);
                        mCirclePercentView.setPercent(100);
                        if (!subscriber.isUnsubscribed()) subscriber.unsubscribe();
                    }
                });
    }

    @Override
    public void finshActivity() {
        super.finshActivity();
        RxBus.get().post(BusAction.ACTION_LOGIN, SPUtils.getUser());
    }

    @Override
    public String setStatisticsTitle() {
        return null;
    }

}

