package com.knms.activity.mine;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshRecyclerView;
import com.knms.activity.base.HeadNotifyBaseActivity;
import com.knms.activity.repair.ReleaseRepairActivity;
import com.knms.adapter.MineRepairAdapter;
import com.knms.android.R;
import com.knms.app.KnmsApp;
import com.knms.bean.ResponseBody;
import com.knms.bean.other.TipNum;
import com.knms.bean.repair.MyRepair;
import com.knms.bean.user.User;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.core.storage.Svn;
import com.knms.net.RxRequestApi;
import com.knms.oncall.LoadListener;
import com.knms.util.Tst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/10/18.
 */
public class MyRepairListActivity extends HeadNotifyBaseActivity {

    private MineRepairAdapter adapter;
    private RelativeLayout rl_status;
    private PullToRefreshRecyclerView refresh_layout;
    private RecyclerView recyclerView;

    private int pageNum = 1;

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("我的维修");
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
    }

    @Override
    protected void initData() {
        adapter = new MineRepairAdapter(this, new ArrayList<MyRepair>());

        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<MyRepair>() {
            @Override
            public void onItemClick(BaseQuickAdapter<MyRepair, ? extends BaseViewHolder> adapter, View view, int position) {
                MyRepair item = adapter.getItem(position);
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("myRepair", item);
                startActivityForResultAnimGeneral(MineRepairDetailActivity.class, params, 1);
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

        adapter.setCallBack(new MineRepairAdapter.CallBack() {
            @Override
            public void refresh() {
                refreshData("");
            }
        });
        adapter.setEnableLoadMore(true);
        adapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                reqApi();
            }
        },recyclerView);
        refreshData("");
    }

    @Override
    public String setStatisticsTitle() {
        return "我的维修列表";
    }

    @Subscribe(tags = {@Tag(BusAction.REFRESH_MAINTAIN)})
    public void refreshData(String str) {
        refresh_layout.postDelayed(new Runnable() {
            @Override
            public void run() {
                refresh_layout.setRefreshing();
            }
        }, 1000);
    }


    @Override
    protected void reqApi() {
        RxRequestApi.getInstance().getApiService().getMyRepairList(pageNum)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<List<MyRepair>>>() {
                    @Override
                    public void call(ResponseBody<List<MyRepair>> body) {
                        refresh_layout.onRefreshComplete();
                        if (body.isSuccess()) {
                            updateView(body.data);
                            TipNum tipNum = Svn.getFromAccount("tipNum");
                            if(tipNum != null){
                                tipNum.repair = 0;
                                Svn.put2Account("tipNum",tipNum);
                            }
                            KnmsApp.getInstance().getUnreadObservable().sendData();
                            pageNum ++;
                        } else {
                            Tst.showToast(body.desc);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        refresh_layout.onRefreshComplete();
                        Tst.showToast(throwable.getMessage());
                    }
                });

    }

    private void updateView(List<MyRepair> data) {
        if (pageNum == 1) {
            if (data.size() > 0) {
                KnmsApp.getInstance().hideLoadView(rl_status);
                adapter.setNewData(data);
            } else {
                KnmsApp.getInstance().showDataEmpty(rl_status, "您还没有发布过维修消息哦~", R.drawable.no_data_shop, "去发布", new LoadListener() {
                    @Override
                    public void onclick() {//进入发布界面
                        Map<String,Object> params = new HashMap<String, Object>();
                        params.put("isShowRedMsg",0);
                        startActivityAnimGeneral(ReleaseRepairActivity.class, params);
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

    @Subscribe(tags = {@Tag(BusAction.ACTION_LOGIN)})
    public void loginAccout(User user) {
        refreshData("");
    }



}
