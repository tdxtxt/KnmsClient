package com.knms.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshRecyclerView;
import com.knms.activity.ReleaseIdleActivity;
import com.knms.activity.UndercarriageDetailsActivity;
import com.knms.adapter.OnOfferFragmentAdapter;
import com.knms.android.R;
import com.knms.app.KnmsApp;
import com.knms.bean.ResponseBody;
import com.knms.bean.myidle.MyIdle;
import com.knms.bean.other.TipNum;
import com.knms.bean.user.User;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.core.storage.Svn;
import com.knms.fragment.base.BaseFragment;
import com.knms.net.HttpConstant;
import com.knms.net.ReqApi;
import com.knms.oncall.AsyncHttpCallBack;
import com.knms.oncall.LoadListener;
import com.knms.util.SPUtils;
import com.knms.util.Tst;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/8/30.
 * 我的闲置  出售中
 */
public class OnOfferFragment extends BaseFragment {
    private PullToRefreshRecyclerView refresh_layout;
    private RecyclerView recyclerView;
    private OnOfferFragmentAdapter adapter;
    private int pageNum = 1;
    private RelativeLayout rl_status;

    @Override
    public String getTitle() {
        return "出售中";
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RxBus.get().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_on_offer, null);
        refresh_layout = (PullToRefreshRecyclerView) view.findViewById(R.id.refresh_layout);
        recyclerView = refresh_layout.getRefreshableView();
        recyclerView.setLayoutManager(new LinearLayoutManager(getmActivity()));
        rl_status = (RelativeLayout) view.findViewById(R.id.rl_status);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new OnOfferFragmentAdapter(getmActivity(), new ArrayList<MyIdle>());
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<MyIdle>() {
            @Override
            public void onItemClick(BaseQuickAdapter<MyIdle, ? extends BaseViewHolder> adapter, View view, int position) {
                MyIdle item = adapter.getItem(position);
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("id", item.goid);
                getmActivity().startActivityAnimGeneral(UndercarriageDetailsActivity.class, params);
            }
        });

        refresh_layout.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        refresh_layout.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<RecyclerView>() {
            @Override
            public void onRefresh(PullToRefreshBase<RecyclerView> refreshView) {//下拉刷新
                pageNum = 1;
                reqApi();
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

    private void refreshData() {
        refresh_layout.postDelayed(new Runnable() {
            @Override
            public void run() {
                refresh_layout.setRefreshing();
            }
        }, 1000);
    }

    @Override
    public void reqApi() {
        KnmsApp.getInstance().hideLoadView(rl_status);
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("state", 0);//0出售中，3已下架
        params.put("pageIndex", pageNum);
        ReqApi.getInstance().postMethod(HttpConstant.myIdel, params, new AsyncHttpCallBack<List<MyIdle>>() {
            @Override
            public void onSuccess(ResponseBody<List<MyIdle>> body) {
                hideProgress();
                refresh_layout.onRefreshComplete();
                if (body.isSuccess()) {
                    updateView(body.data);
                    TipNum tipNum = Svn.getFromAccount("tipNum");
                    if(tipNum != null){
                        tipNum.idel = 0;
                        Svn.put2Account("tipNum",tipNum);
                    }
                    KnmsApp.getInstance().getUnreadObservable().sendData();
                    pageNum++;
                } else
                    Tst.showToast(body.desc);
            }

            @Override
            public void onFailure(String msg) {
                hideProgress();
                refresh_layout.onRefreshComplete();
                Tst.showToast(msg);
            }

            @Override
            public Type setType() {
                return new TypeToken<ResponseBody<List<MyIdle>>>() {
                }.getType();
            }
        });
    }

    private void updateView(List<MyIdle> data) {
        if (data == null) return;
        if (pageNum == 1) {
            if (data.size() > 0) {
                adapter.setNewData(data);
            } else {//传空的布局
                adapter.getData().clear();
                adapter.notifyDataSetChanged();
                KnmsApp.getInstance().showDataEmpty(rl_status, "一个能卖的宝贝都没有", R.drawable.no_data_undercarriage, "去发布", new LoadListener() {
                    @Override
                    public void onclick() {
                        Map<String,Object> parmas = new HashMap<String, Object>();
                        parmas.put("isShowRedMsg",0);
                        getmActivity().startActivityAnimGeneral(ReleaseIdleActivity.class,parmas);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        RxBus.get().unregister(this);
    }


    @Subscribe(tags = {@Tag(BusAction.REFRESH_IDLE)})
    public void updateData(String str) {
        refreshData();
    }

    @Subscribe(tags = {@Tag(BusAction.ACTION_LOGIN)})
    public void loginAccout(User user) {
        refreshData();
    }

}
