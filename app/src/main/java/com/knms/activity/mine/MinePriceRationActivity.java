package com.knms.activity.mine;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshRecyclerView;
import com.knms.activity.MinePriceRationDetailsActivity;
import com.knms.activity.base.HeadNotifyBaseActivity;
import com.knms.activity.pic.CameraActivityF;
import com.knms.adapter.MinePriceRationAdapter;
import com.knms.app.KnmsApp;
import com.knms.bean.ResponseBody;
import com.knms.bean.myparity.MyParity;
import com.knms.bean.other.TipNum;
import com.knms.bean.user.User;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.core.storage.Svn;
import com.knms.net.HttpConstant;
import com.knms.net.ReqApi;
import com.knms.net.RxRequestApi;
import com.knms.oncall.AsyncHttpCallBack;
import com.knms.oncall.LoadListener;
import com.knms.util.SPUtils;
import com.knms.util.Tst;
import com.knms.android.R;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/9/12.
 * <p>
 * 我的比价
 */
public class MinePriceRationActivity extends HeadNotifyBaseActivity {
    private RelativeLayout rl_status;
    private PullToRefreshRecyclerView refresh_layout;
    private RecyclerView recyclerView;
    private MinePriceRationAdapter adapter;

    private int pageNum = 1;

    @Override
    protected void getParmas(Intent intent) {
        super.getParmas(intent);
    }

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("求购/比货");
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_mine_price_ration;
    }

    @Override
    protected void initView() {
        rl_status = findView(R.id.rl_status);
        refresh_layout = findView(R.id.refresh_layout);
        recyclerView = refresh_layout.getRefreshableView();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(true);
    }

    @Override
    protected void initData() {
        adapter = new MinePriceRationAdapter(this, new ArrayList<MyParity>());

        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<MyParity>() {
            @Override
            public void onItemClick(BaseQuickAdapter<MyParity, ? extends BaseViewHolder> adapter, View view, int position) {
                MyParity item = adapter.getItem(position);
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("myParity", item);
//                startActivityAnimGeneral(MinePriceRationDetailsActivity.class,params);
                startActivityForResultAnimGeneral(MinePriceRationDetailsActivity.class, params, 1);
            }
        });
        refresh_layout.setMode(PullToRefreshBase.Mode.PULL_FROM_START);

        refresh_layout.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<RecyclerView>() {
            @Override
            public void onRefresh(PullToRefreshBase<RecyclerView> refreshView) {
                pageNum = 1;
                reqApi();
            }
        });

        adapter.setCallBack(new MinePriceRationAdapter.CallBack() {
            @Override
            public void refresh() {
                refreshData();
            }
        });
        adapter.setEnableLoadMore(true);
        adapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                reqApi();
            }
        },recyclerView);
        refreshData();
    }

    @Override
    public String setStatisticsTitle() {
        return "我的比比价列表";
    }

    private void  refreshData(){
        refresh_layout.postDelayed(new Runnable() {
            @Override
            public void run() {
                refresh_layout.setRefreshing();
            }
        }, 500);
    }

    @Override
    protected void reqApi() {
        RxRequestApi.getInstance().getApiService().getParitys(pageNum)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<ResponseBody<List<MyParity>>>() {
                    @Override
                    public void call(ResponseBody<List<MyParity>> body) {
                        hideProgress();
                        refresh_layout.onRefreshComplete();
                        if (body.isSuccess()) {
                            updateView(body.data);
                            TipNum tipNum = Svn.getFromAccount("tipNum");
                            if(tipNum != null){
                                tipNum.parity = 0;
                                Svn.put2Account("tipNum",tipNum);
                            }
                            KnmsApp.getInstance().getUnreadObservable().sendData();
                            pageNum++;
                        }else
                            Tst.showToast(body.desc);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        hideProgress();
                        refresh_layout.onRefreshComplete();
                        Tst.showToast(throwable.toString());
                    }
                });
    }

    private void updateView(List<MyParity> data) {
        if (pageNum == 1) {
            if (data.size() > 0) {
                KnmsApp.getInstance().hideLoadView(rl_status);
                adapter.setNewData(data);
            } else {
                KnmsApp.getInstance().showDataEmpty(rl_status, "您还没有发布过比货消息哦~", R.drawable.no_data_shop, "去发布", new LoadListener() {
                    @Override
                    public void onclick() {//进入发布界面
                        Map<String,Object> parmas = new HashMap<String, Object>();
                        parmas.put("isShowRedMsg",0);
                        startActivityAnimGeneral(CameraActivityF.class, parmas);
                    }
                });
            }
        } else {
            if(data.size() > 0){
                adapter.addData(data);
                adapter.loadMoreComplete();
            }else{
                adapter.loadMoreEnd();
            }
        }
    }
    @Subscribe(tags = {@Tag(BusAction.REFRESH_BBPRICE)})
    public void refresh(String str){
        refreshData();
    }

    @Subscribe(tags = {@Tag(BusAction.ACTION_LOGIN)})
    public void loginAccout(User user) {
        refreshData();
    }
}
