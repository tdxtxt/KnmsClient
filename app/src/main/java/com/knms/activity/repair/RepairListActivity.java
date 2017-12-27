package com.knms.activity.repair;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshRecyclerView;
import com.knms.activity.base.HeadNotifyBaseActivity;
import com.knms.adapter.MaintainListAdapter;
import com.knms.android.R;
import com.knms.bean.ResponseBody;
import com.knms.bean.repair.MyRepair;
import com.knms.bean.repair.Repair;
import com.knms.net.RxRequestApi;
import com.knms.util.SPUtils;
import com.knms.util.Tst;
import com.knms.view.menu.ExpandableButtonMenu;
import com.knms.view.menu.ExpandableMenuOverlay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/10/17.
 */
public class RepairListActivity extends HeadNotifyBaseActivity {

    PullToRefreshRecyclerView pulltoRefreshView;
    RecyclerView mRecyclerView;
    private int pageNum = 1;
    private MaintainListAdapter mAdapter;
    private ExpandableMenuOverlay mMenuOverlay;

    @Override
    protected int layoutResID() {
        return R.layout.activity_maintain_layout;
    }

    @Override
    protected void initView() {
        mMenuOverlay = findView(R.id.maintain_menu_overlay);
        pulltoRefreshView = findView(R.id.maintain_recyclerview);
        mRecyclerView = pulltoRefreshView.getRefreshableView();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        pulltoRefreshView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        mAdapter = new MaintainListAdapter(this,new ArrayList<Repair>());
        mAdapter.bindToRecyclerView(mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);
        pulltoRefreshView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<RecyclerView>() {
            @Override
            public void onRefresh(PullToRefreshBase<RecyclerView> refreshView) {
                pageNum = 1;
                reqApi();
            }
        });
        mAdapter.setEnableLoadMore(true);
        mAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                reqApi();
            }
        },mRecyclerView);
        pulltoRefreshView.postDelayed(new Runnable() {
            @Override
            public void run() {
                pulltoRefreshView.setRefreshing();
            }
        },1000);
        mMenuOverlay.setOnMenuButtonClickListener(new ExpandableButtonMenu.OnMenuButtonClick() {
            @Override
            public void onClick(ExpandableButtonMenu.MenuButton action) {
                    if(action== ExpandableButtonMenu.MenuButton.LEFT){
                        startActivityAnimGeneral(ReleaseRepairActivity.class, null);
                        mMenuOverlay.hide();
                    }else if((action== ExpandableButtonMenu.MenuButton.RIGHT)){
                        Map<String,Object> map=new HashMap<String, Object>();
                        map.put("isDraft",true);
                        startActivityAnimGeneral(ReleaseRepairActivity.class, map);
                        mMenuOverlay.hide();
                    }
            }
        });
        mMenuOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(SPUtils.getSerializable("draftRepair", MyRepair.class)!=null){
                    mMenuOverlay.show();
                }else{
                    mMenuOverlay.hide();
                    startActivityAnimGeneral(ReleaseRepairActivity.class, null);
                }
            }
        });
        mAdapter.setOnItemClickListener(new com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(com.chad.library.adapter.base.BaseQuickAdapter adapter, View view, int position) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("repairDetaile", mAdapter.getItem(position));
                startActivityAnimGeneral(RepairDetailActivity.class, map);
            }
        });
    }

    @Override
    protected void initData() {

    }

    @Override
    public String setStatisticsTitle() {
        return "家具维修师傅列表";
    }

    @Override
    protected void reqApi() {
        RxRequestApi.getInstance().getApiService().getRepairList(pageNum)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<List<Repair>>>() {
                    @Override
                    public void call(ResponseBody<List<Repair>> body) {
                        pulltoRefreshView.onRefreshComplete();
                        if (body.isSuccess()) {
                            updateView(body.data);
                            pageNum++;
                        } else {
                            Tst.showToast(body.desc);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        pulltoRefreshView.onRefreshComplete();
                        Tst.showToast(throwable.getMessage());
                    }
                });
    }

    private void updateView(List<Repair> data) {
        if(data == null) return;
        if(pageNum == 1){
            mAdapter.setNewData(data);
        }else{
            if(data.size() > 0){
                mAdapter.addData(data);
                mAdapter.loadMoreComplete();
            }else{
                mAdapter.loadMoreEnd();
            }
        }
    }

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("家具维修");
    }
}
