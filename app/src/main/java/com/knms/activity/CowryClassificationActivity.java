package com.knms.activity;

import com.knms.activity.base.BaseActivity;
import com.knms.activity.base.HeadBaseActivity;
import com.knms.adapter.CowryClassificationAdapter;
import com.knms.app.KnmsApp;
import com.knms.bean.ResponseBody;
import com.knms.bean.idle.ReIdleClassify;
import com.knms.net.RxRequestApi;
import com.knms.oncall.LoadListener;
import com.knms.util.ToolsHelper;
import com.knms.util.Tst;
import com.knms.android.R;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * 宝贝分类
 */
public class CowryClassificationActivity extends HeadBaseActivity {
    private CowryClassificationAdapter mAdapter1;
    private CowryClassificationAdapter mAdapter2;
    private ListView lv_one, lv_two;
    private RelativeLayout rl_status;
    private String classifyId = "";
    Subscription subscription;

    @Override
    protected void getParmas(Intent intent) {
        super.getParmas(intent);
        classifyId = intent.getStringExtra("firstClassifId");
    }

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("宝贝分类");
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_cowry_classification;
    }

    @Override
    protected void initView() {
        lv_one = findView(R.id.listview_one);
        lv_two = findView(R.id.listview_two);
        rl_status = findView(R.id.rl_status);
    }

    @Override
    protected void initData() {
        mAdapter1 = new CowryClassificationAdapter(this, new ArrayList<ReIdleClassify>());
        mAdapter1.setBgColor(true);
        mAdapter2 = new CowryClassificationAdapter(this, new ArrayList<ReIdleClassify>());
        lv_one.setAdapter(mAdapter1);
        lv_two.setAdapter(mAdapter2);
        lv_one.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ReIdleClassify item = mAdapter1.getData().get(position);
                mAdapter1.mChooseId = position;
                mAdapter1.notifyDataSetChanged();
                if (item.subClassifys != null && item.subClassifys.size() > 0) {
                    mAdapter2.setNewData(mAdapter1.getData().get(position).subClassifys);
                } else {
                    getIntent().putExtra("classify", item);
                    setResult(RESULT_OK, getIntent());
                    finshActivity();
                }
            }
        });
        lv_two.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ReIdleClassify item = mAdapter2.getData().get(position);
                mAdapter2.mChooseId = position;
                mAdapter2.notifyDataSetChanged();

                getIntent().putExtra("classify", item);
                setResult(RESULT_OK, getIntent());
                finshActivity();
            }
        });
        reqApi();
    }

    @Override
    public String setStatisticsTitle() {
        return "商品分类";
    }

    @Override
    protected void reqApi() {
        KnmsApp.getInstance().showLoadViewIng(rl_status);
        subscription = RxRequestApi.getInstance().getApiService().getReIdleClassify().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).cache()
                .subscribe(new Action1<ResponseBody<List<ReIdleClassify>>>() {
                    @Override
                    public void call(ResponseBody<List<ReIdleClassify>> body) {
                        KnmsApp.getInstance().hideLoadView(rl_status);
                        if (body.isSuccess()) {
                            updateView(body.data);
                        } else {
                            Tst.showToast(body.desc);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Tst.showToast(throwable.toString());
                        KnmsApp.getInstance().showLoadViewFaild(rl_status, new LoadListener() {
                            @Override
                            public void onclick() {
                                reqApi();
                            }
                        });
                    }
                });
    }

    private void updateView(List<ReIdleClassify> data) {
        if (data != null) {
            ToolsHelper.getInstance().sort(data, "seq");
            mAdapter1.setNewData(data);
            if(classifyId.equals("")) {
                mAdapter1.mChooseId = 0;
                mAdapter1.notifyDataSetChanged();
                mAdapter2.setNewData(mAdapter1.getData().get(0).subClassifys);
                return;
            }
            for (int i = 0; i < data.size(); i++) {
                if (data.get(i).id.equals(classifyId)) {
                    mAdapter1.mChooseId = i;
                    mAdapter1.notifyDataSetChanged();
                    mAdapter2.setNewData(mAdapter1.getData().get(i).subClassifys);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (subscription != null) subscription.unsubscribe();
        super.onDestroy();
    }
}
