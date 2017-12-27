package com.knms.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.knms.activity.UndercarriageDetailsActivity;
import com.knms.adapter.UndercarriageFragmentAdapter;
import com.knms.android.R;
import com.knms.app.KnmsApp;
import com.knms.bean.ResponseBody;
import com.knms.bean.myidle.MyIdle;
import com.knms.bean.user.User;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.fragment.base.BaseFragment;
import com.knms.net.HttpConstant;
import com.knms.net.ReqApi;
import com.knms.oncall.AsyncHttpCallBack;
import com.knms.util.Tst;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/8/30.
 * <p>
 * 我的闲置   已下架
 */
public class UndercarriageFragment extends BaseFragment {

    private PullToRefreshRecyclerView refresh_layout;
    private RecyclerView recyclerView;
    private int pageNum = 1;
    private UndercarriageFragmentAdapter adapter;
    private RelativeLayout rl_status;

    @Override
    public String getTitle() {
        return "已下架";
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RxBus.get().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_undercarriage, null);
        refresh_layout = (PullToRefreshRecyclerView) view.findViewById(R.id.refresh_layout);
        recyclerView = refresh_layout.getRefreshableView();
        recyclerView.setLayoutManager(new LinearLayoutManager(getmActivity()));
        rl_status = (RelativeLayout) view.findViewById(R.id.rl_status);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        adapter = new UndercarriageFragmentAdapter(getmActivity(), new ArrayList<MyIdle>());
        recyclerView.setAdapter(adapter);
        adapter.setCallBack(new UndercarriageFragmentAdapter.CallBack() {
            @Override
            public void refresh() {
                refresh_layout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refresh_layout.setRefreshing();
                    }
                }, 1000);

            }
        });
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
            public void onRefresh(PullToRefreshBase<RecyclerView> refreshView) {
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

    private void refreshData(){
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
        params.put("state", 3);//0出售中，3已下架
        params.put("pageIndex", pageNum);
        ReqApi.getInstance().postMethod(HttpConstant.myIdel, params, new AsyncHttpCallBack<List<MyIdle>>() {
            @Override
            public void onSuccess(ResponseBody<List<MyIdle>> body) {
                hideProgress();
                refresh_layout.onRefreshComplete();
                if (body.isSuccess()) {
                    updateView(body.data);
                    pageNum ++;
                } else {
                    Tst.showToast(body.desc);
                }
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
            } else {//传空布局
                adapter.getData().clear();
                adapter.notifyDataSetChanged();
                KnmsApp.getInstance().showDataEmpty(rl_status, "您还没有下架的宝贝哦", R.drawable.no_data_undercarriage);
            }
        } else {
            if(data.size() > 0) {
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
