package com.knms.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.chad.library.adapter.base.BaseViewHolder;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshRecyclerView;
import com.knms.activity.ShopActivityF;
import com.knms.activity.main.MainActivity;
import com.knms.adapter.ShopAdapter;
import com.knms.adapter.baserecycler.BaseQuickAdapter;
import com.knms.android.R;
import com.knms.app.KnmsApp;
import com.knms.bean.ResponseBody;
import com.knms.bean.shop.Shop;
import com.knms.bean.user.User;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.fragment.base.BaseFragment;
import com.knms.net.RxRequestApi;
import com.knms.oncall.LoadListener;
import com.knms.util.Tst;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by tdx on 2016/9/7.
 * 我的店铺收藏
 */
public class MineShopFragment extends BaseFragment {
    PullToRefreshRecyclerView layout_refresh;
    RecyclerView recyclerView;
    RelativeLayout rl_status;
    ShopAdapter adapter;
    Subscription subscription;
    private boolean isNeedRefresh=true;
    int pageNum = 1, pos;
    int type;

    @Override
    public String getTitle() {
        return "店铺";
    }

    public static MineShopFragment newInstance(int type) {
        MineShopFragment fragment = new MineShopFragment();
        Bundle args = new Bundle();
        args.putInt("type", type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RxBus.get().register(this);
        if (getArguments() != null) {
            type = getArguments().getInt("type");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_comm_recyclerview, null);
        layout_refresh = (PullToRefreshRecyclerView) view.findViewById(R.id.refresh_recyclerView);
        rl_status = (RelativeLayout) view.findViewById(R.id.rl_status);
        recyclerView = layout_refresh.getRefreshableView();
        recyclerView.setLayoutManager(new LinearLayoutManager(getmActivity()));
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        adapter = new ShopAdapter(getContext(),true,false);
        recyclerView.setAdapter(adapter);
        layout_refresh.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        layout_refresh.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<RecyclerView>() {
            @Override
            public void onRefresh(PullToRefreshBase<RecyclerView> refreshView) {
                pageNum = 1;
                reqApi();
            }
        });
        refreshData();
        adapter.setOnItemClickListener(new com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener<Shop>() {
            @Override
            public void onItemClick(com.chad.library.adapter.base.BaseQuickAdapter<Shop, ? extends BaseViewHolder> adapter, View view, int position) {
                pos = position;
                Intent intent = new Intent(getActivity(), ShopActivityF.class);
                intent.putExtra("shopId", adapter.getData().get(position).id);
                startActivity(intent);
            }
        });
        adapter.setEnableLoadMore(true);
        adapter.setOnLoadMoreListener(new com.chad.library.adapter.base.BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                reqApi();
            }
        },recyclerView);
    }

    private void refreshData(){
        layout_refresh.postDelayed(new Runnable() {
            @Override
            public void run() {
                layout_refresh.setRefreshing();
            }
        }, 1000);
    }

    @Override
    public void reqApi() {
        KnmsApp.getInstance().hideLoadView(rl_status);
        if (subscription != null) subscription.unsubscribe();
        subscription = RxRequestApi.getInstance().getApiService().getCollectionShop(pageNum, 2, type)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<List<Shop>>>() {
                    @Override
                    public void call(ResponseBody<List<Shop>> body) {
                        layout_refresh.onRefreshComplete();
                        if (body.isSuccess()) {
                            updataView(body.data);
                            pageNum++;
                        } else {
                            Tst.showToast(body.desc);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        layout_refresh.onRefreshComplete();
                        Tst.showToast(throwable.toString());
                    }
                });
    }

    private void updataView(List<Shop> data) {
        if(pageNum == 1){
            if(data != null && data.size() > 0){
                adapter.setNewData(data);
                super.hasData = true;
            }else{
                super.hasData = false;
                String content;
                if (type == 1) {
                    content = "还没有浏览记录，快去逛逛吧";
                } else {
                    content = "还没有收藏哦，快去把喜欢的收藏起来";
                }                                                                                //去首页逛逛
                KnmsApp.getInstance().showDataEmpty(rl_status, content, R.drawable.no_data_1, "去逛逛", new LoadListener() {
                    @Override
                    public void onclick() {//去首页逛逛
                        Map<String, Object> param = new HashMap<String, Object>();
                        param.put("source", "mHomePage");
                        getmActivity().startActivityAnimGeneral(MainActivity.class, param);
                        getmActivity().finshActivity();
                    }
                });
            }
        }else{
            if(data != null && data.size() > 0){
                adapter.addData(data);
                adapter.loadMoreComplete();
            }else{
                adapter.loadMoreEnd();
            }
        }
    }

    @Override
    public void onDestroy() {
        if (subscription != null) subscription.unsubscribe();
        RxBus.get().unregister(this);
        super.onDestroy();
    }

    @Subscribe(tags = {@Tag(BusAction.CLEAR_BROWSE)})
    public void clearData(Integer clear) {
        if (clear == 1) {
            adapter.getData().clear();
            adapter.notifyDataSetChanged();
            pageNum = 1;
            updataView(adapter.getData());
        }
    }

    @Subscribe(tags = {@Tag(BusAction.CANCEL_SHOP_COLLECTION)})
    public void cancleCollection(Boolean b) {
        isNeedRefresh = b;
    }

    @Subscribe(tags = {@Tag(BusAction.ACTION_LOGIN)})
    public void loginAccout(User user) {
        refreshData();
    }
    @Override
    public void onResume() {
        super.onResume();
        if (!isNeedRefresh) {
            recyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    adapter.remove(pos);
                }
            },500);
        }
    }
}
