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

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshRecyclerView;
import com.knms.activity.KnowledgeDetailsActivity;
import com.knms.activity.details.DecorationStyleDetailsActivity;
import com.knms.activity.main.MainActivity;
import com.knms.adapter.CustomFurnitureAdapter;
import com.knms.android.R;
import com.knms.app.KnmsApp;
import com.knms.bean.ResponseBody;
import com.knms.bean.product.Furniture;
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
 * 我的灵感收藏
 */
public class MineInspFragment extends BaseFragment {
    PullToRefreshRecyclerView layout_refresh;
    RecyclerView recyclerView;
    CustomFurnitureAdapter adapter;
    Subscription subscription;
    RelativeLayout rl_status;

    int type;
    int pageNum = 1, pos;
    boolean isNeedRefresh=true;

    @Override
    public String getTitle() {
        return "灵感";
    }

    public static MineInspFragment newInstance(int type) {
        MineInspFragment fragment = new MineInspFragment();
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
        rl_status = (RelativeLayout) view.findViewById(R.id.rl_status);
        layout_refresh = (PullToRefreshRecyclerView) view.findViewById(R.id.refresh_recyclerView);
        recyclerView = layout_refresh.getRefreshableView();
        recyclerView.setLayoutManager(new LinearLayoutManager(getmActivity()));
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        adapter = new CustomFurnitureAdapter(getContext(),new ArrayList<Furniture>(), true);
        recyclerView.setAdapter(adapter);
        layout_refresh.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        layout_refresh.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<RecyclerView>() {
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
        adapter.setOnItemClickListener(new com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener<Furniture>() {
            @Override
            public void onItemClick(com.chad.library.adapter.base.BaseQuickAdapter<Furniture, ? extends BaseViewHolder> adapter, View view, int position) {
                pos = position;
                if (adapter.getItem(position).type == 1) {//家居
                    Map<String, Object> param = new HashMap<String, Object>();
                    param.put("id", adapter.getItem(position).id);
                    param.put("pic", adapter.getItem(position).pic);
                    getmActivity().startActivityAnimGeneral(KnowledgeDetailsActivity.class, param);
                } else {
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("id", adapter.getData().get(position).id);
                    getmActivity().startActivityAnimGeneral(DecorationStyleDetailsActivity.class, params);
                }
            }
        });
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
        KnmsApp.getInstance().hideLoadView(rl_status);
        if (subscription != null) subscription.unsubscribe();
        subscription = RxRequestApi.getInstance().getApiService().getCollectionInsp(pageNum, 3, type)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<List<Furniture>>>() {
                    @Override
                    public void call(ResponseBody<List<Furniture>> body) {
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

    private void updataView(List<Furniture> data) {
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
                    content = "还没有收藏哦,快去把喜欢的收藏起来";
                }                                                                            //去首页逛逛
                KnmsApp.getInstance().showDataEmpty(rl_status, content, R.drawable.no_data_1, "去逛逛", new LoadListener() {
                    @Override
                    public void onclick() {//去首页逛逛
                        Intent intent = new Intent(getmActivity(), MainActivity.class);
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
//                recyclerView.scrollBy(0, 20);//防止加载更多时候有些情况不会更新item到视图中
            } else {
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
        if (clear == 2) {
            adapter.getData().clear();
            adapter.notifyDataSetChanged();
            pageNum = 1;
            updataView(adapter.getData());
        }
    }

    @Subscribe(tags = {@Tag(BusAction.CANCEL_INSP_COLLECTION)})
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
            adapter.remove(pos);
        }
    }
}
