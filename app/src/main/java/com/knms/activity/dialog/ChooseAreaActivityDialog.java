package com.knms.activity.dialog;

import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.knms.activity.ReleaseIdleActivity;
import com.knms.activity.base.BaseActivity;
import com.knms.adapter.CityWheelAdapter;
import com.knms.app.KnmsApp;
import com.knms.bean.ResponseBody;
import com.knms.bean.other.City;
import com.knms.net.RxRequestApi;
import com.knms.oncall.LoadListener;
import com.knms.util.Tst;
import com.knms.view.wheel.WheelView;
import com.knms.view.wheel.listener.OnWheelChangedListener;
import com.knms.android.R;

import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by tdx on 2016/9/9.
 */
public class ChooseAreaActivityDialog extends BaseActivity implements OnWheelChangedListener {
    WheelView wv_one;
    WheelView wv_two;
    RelativeLayout rl_status;
    Subscription subscription;
    CityWheelAdapter oneAdpter, twoAdpter;
    City currentCity, tempCity;

    @Override
    protected int layoutResID() {
        return R.layout.dialog_choose_location;
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
        wv_one = findView(R.id.wv_one);
        wv_two = findView(R.id.wv_two);
        rl_status = findView(R.id.rl_status);
    }

    @Override
    protected void initData() {
        wv_one.setVisibleItems(7);
        wv_two.setVisibleItems(7);
        wv_one.addChangingListener(this);
        wv_two.addChangingListener(this);
        findView(R.id.tvBtn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KnmsApp.getInstance().finishActivity(ChooseAreaActivityDialog.this);
            }
        });
        findView(R.id.tvBtn_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tempCity!=null)tempCity.name = currentCity==null?"":currentCity.name + tempCity.name;
                getIntent().putExtra("city", tempCity);
                setResult(RESULT_OK, getIntent());
                KnmsApp.getInstance().finishActivity(ChooseAreaActivityDialog.this);
            }
        });
        reqApi();
    }

    @Override
    public String setStatisticsTitle() {
        return "选择地址";
    }

    @Override
    protected void reqApi() {
        KnmsApp.getInstance().showLoadViewIng(rl_status);
        subscription = RxRequestApi.getInstance().getApiService().getCitys().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).cache()
                .subscribe(new Action1<ResponseBody<List<City>>>() {
                    @Override
                    public void call(ResponseBody<List<City>> body) {
                        KnmsApp.getInstance().hideLoadView(rl_status);
                        if (body.isSuccess()) {
                            updateViewOne(body.data);
                        } else {
                            Tst.showToast(body.desc);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Tst.showToast(throwable.toString());
                        KnmsApp.getInstance().showLoadSmailViewFaild(rl_status, new LoadListener() {
                            @Override
                            public void onclick() {
                                reqApi();
                            }
                        });
                    }
                });
    }

    private void updateViewOne(List<City> data) {
        if(data == null) return;
        oneAdpter = new CityWheelAdapter(this, data);
        currentCity = oneAdpter.getData().get(getOnePositionFromCode());
        wv_one.setViewAdapter(oneAdpter);
        wv_one.setCurrentItem(getOnePositionFromCode());
        updataViewTwo(data.get(wv_one.getCurrentItem()).subCitys);
    }

    private void updataViewTwo(List<City> data) {
        if (data != null && data.size() > 0) {
            twoAdpter = new CityWheelAdapter(this, data);
            wv_two.setViewAdapter(twoAdpter);
            wv_two.setCurrentItem(getTwoPositionFromCode());
            tempCity = twoAdpter.getData().get(getTwoPositionFromCode());
        } else {
            tempCity = oneAdpter.getData().get(wv_one.getCurrentItem());
        }
    }

    private int getOnePositionFromCode() {
        return 0;
    }

    private int getTwoPositionFromCode() {
        return 0;
    }

    @Override
    public void onChanged(WheelView wheel, int oldValue, int newValue) {
        int pCurrent = wheel.getCurrentItem();
        if (wheel == wv_one) {
            City item = oneAdpter.getData().get(pCurrent);
            currentCity = item;
            updataViewTwo(item.subCitys);
        } else if (wheel == wv_two) {
            tempCity = twoAdpter.getData().get(pCurrent);
        }
    }

    @Override
    protected void onDestroy() {
        if (subscription != null) subscription.unsubscribe();
        super.onDestroy();
    }
}
