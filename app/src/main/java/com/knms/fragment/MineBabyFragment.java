package com.knms.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshRecyclerView;
import com.knms.activity.CustomFurnitureDetailsActivity;
import com.knms.activity.IdleDetailsActivity;
import com.knms.activity.details.ProductDetailsBaokActivity;
import com.knms.activity.details.ProductDetailsOrdinaryActivity;
import com.knms.activity.details.base.CannotBuyBaseDetailsActivity;
import com.knms.activity.details.canbuy.ProductDetailsActivity;
import com.knms.activity.main.MainActivity;
import com.knms.adapter.MineCollectAdapter;
import com.knms.android.R;
import com.knms.app.KnmsApp;
import com.knms.bean.ResponseBody;
import com.knms.bean.product.CollectionProduct;
import com.knms.bean.user.User;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.fragment.base.BaseFragment;
import com.knms.net.RxRequestApi;
import com.knms.oncall.LoadListener;
import com.knms.util.Tst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by tdx on 2016/9/7.
 * 我的宝贝收藏
 */
public class MineBabyFragment extends BaseFragment {
    PullToRefreshRecyclerView layout_refresh;
    RecyclerView recyclerView;
    RelativeLayout rl_status;
    MineCollectAdapter adapter;
    Subscription subscription;
    private int pageNum = 1;
    private int type, pos;//1浏览&0收藏
    private boolean isNeedRefresh=true;

    @Override
    public String getTitle() {
        return "宝贝";
    }

    public static MineBabyFragment newInstance(int type) {
        MineBabyFragment fragment = new MineBabyFragment();
        Bundle args = new Bundle();
        args.putInt("type", type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getInt("type");
        }
        RxBus.get().register(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_comm_recyclerview, null);
        layout_refresh = (PullToRefreshRecyclerView) view.findViewById(R.id.refresh_recyclerView);
        rl_status = (RelativeLayout) view.findViewById(R.id.rl_status);
        recyclerView = layout_refresh.getRefreshableView();
        recyclerView.setLayoutManager(new GridLayoutManager(getmActivity(), 2));
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        adapter = new MineCollectAdapter(getContext(),null);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(com.chad.library.adapter.base.BaseQuickAdapter adapter, View view, int position) {
                CollectionProduct item = (CollectionProduct) adapter.getItem(position);
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("id", item.goid);
                pos = position;
                if (item.gotype == 1)
                    getmActivity().startActivityAnimGeneral(IdleDetailsActivity.class, params);
                else if(item.gotype==3)
                    getmActivity().startActivityAnimGeneral(CustomFurnitureDetailsActivity.class, params);
                else if(item.gotype==6)
                    getmActivity().startActivityAnimGeneral(ProductDetailsActivity.class,params);
                else
                    getmActivity().startActivityAnimGeneral(item.goisrecommend == 1 ? ProductDetailsBaokActivity.class : ProductDetailsOrdinaryActivity.class, params);
            }
        });
        layout_refresh.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        layout_refresh.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<RecyclerView>() {
            @Override
            public void onRefresh(PullToRefreshBase<RecyclerView> refreshView) {
                pageNum = 1;
                reqApi();
            }
        });
        adapter.setEnableLoadMore(true);
        adapter.setOnLoadMoreListener(new com.chad.library.adapter.base.BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                reqApi();
            }
        },recyclerView);
        refreshData();
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
        if (subscription != null) subscription.unsubscribe();
        subscription = RxRequestApi.getInstance().getApiService().getCollectionBaby(pageNum, 1, type)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<List<CollectionProduct>>>() {
                    @Override
                    public void call(ResponseBody<List<CollectionProduct>> body) {
                        layout_refresh.onRefreshComplete();
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
                        layout_refresh.onRefreshComplete();
                        Tst.showToast(throwable.toString());
                    }
                });
    }

    private void updateView(List<CollectionProduct> data) {
        if (pageNum == 1) {
            if (data != null && data.size() > 0) {
                super.hasData = true;
                adapter.setNewData(data);
            } else {
                super.hasData = false;
                String content;
                if (type == 1) {
                    content = "还没有浏览记录，快去逛逛吧";
                } else {
                    content = "还没有收藏哦，快去把喜欢的收藏起来";
                }                                                                       //去首页逛逛
                KnmsApp.getInstance().showDataEmpty(rl_status, content, R.drawable.no_data_1, "去逛逛", new LoadListener() {
                    @Override
                    public void onclick() {//去首页逛逛
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        intent.putExtra("source", "mHomePage");
                        startActivity(intent);
                        getmActivity().finshActivity();
                    }
                });
            }
        } else {
            if (data != null && data.size() > 0) {
                adapter.addData(data);
                adapter.loadMoreComplete();
                recyclerView.scrollBy(0, 20);//防止加载更多时候有些情况不会更新item到视图中
            } else {
                adapter.loadMoreEnd();
            }
        }
    }

    @Subscribe
    public void subscribeObj(Object o) {
        if (o instanceof User) {
            reqApi();
        } else if (o.toString().equals("clear")) {
            adapter.setNewData(new ArrayList<CollectionProduct>());
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
        if (clear == 0) {
            adapter.getData().clear();
            adapter.notifyDataSetChanged();
            pageNum = 1;
            updateView(adapter.getData());
        }
    }

    @Subscribe(tags = {@Tag(BusAction.CANCEL_GOODS_COLLECTION)})
    public void cancleCollection(Boolean b) {
        isNeedRefresh = b;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isNeedRefresh) {
            adapter.remove(pos);
        }
    }

    @Subscribe(tags = {@Tag(BusAction.ACTION_LOGIN)})
    public void loginAccout(User user) {
        refreshData();
    }
}
