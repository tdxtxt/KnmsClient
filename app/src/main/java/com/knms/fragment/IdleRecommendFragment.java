package com.knms.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.chad.library.adapter.base.BaseViewHolder;
import com.knms.activity.IdleDetailsActivity;
import com.knms.adapter.IdleAdapter;
import com.knms.android.R;
import com.knms.app.KnmsApp;
import com.knms.bean.ResponseBody;
import com.knms.bean.product.Idle;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.fragment.base.BaseFragment;
import com.knms.net.RxRequestApi;
import com.knms.util.Tst;
import com.knms.view.sticky.HeaderScrollHelper;

import java.util.List;
import java.util.Map;

import me.everything.android.ui.overscroll.VerticalOverScrollBounceEffectDecorator;
import me.everything.android.ui.overscroll.adapters.RecyclerViewOverScrollDecorAdapter;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by tdx on 2016/9/1.
 * 闲置家具-推荐页面
 */
public class IdleRecommendFragment extends BaseFragment implements HeaderScrollHelper.ScrollableContainer{
    public int pageNum = 1;
    private RecyclerView recycleView;
    private IdleAdapter adapter;
    private RelativeLayout rlLoading;
    private Subscription subscription;

    public static IdleRecommendFragment newInstance() {
        IdleRecommendFragment fragment = new IdleRecommendFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RxBus.get().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_recyclerview, null);
        rlLoading= (RelativeLayout) view.findViewById(R.id.rl_loading);
        recycleView = (RecyclerView) view.findViewById(R.id.id_stickynavlayout_innerscrollview);
        recycleView.setLayoutManager(new LinearLayoutManager(getmActivity()));
        new VerticalOverScrollBounceEffectDecorator(new RecyclerViewOverScrollDecorAdapter(recycleView));
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        KnmsApp.getInstance().showDataEmpty(rlLoading, "正在加载中...", R.drawable.no_data_undercarriage);
        adapter = new IdleAdapter(getContext());
        recycleView.setAdapter(adapter);

        adapter.setEnableLoadMore(true);
        adapter.setOnLoadMoreListener(new com.chad.library.adapter.base.BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                reqApi();
            }
        },recycleView);
        adapter.setOnItemClickListener(new com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener<Idle>() {
            @Override
            public void onItemClick(com.chad.library.adapter.base.BaseQuickAdapter<Idle, ? extends BaseViewHolder> adapter, View view, int position) {
                if (adapter.getData().size() <= position) return;
                Idle item = adapter.getData().get(position);
                if (item != null) {
                    Map<String, Object> params = new ArrayMap<String, Object>();
                    params.put("id", item.id);
                    getmActivity().startActivityAnimGeneral(IdleDetailsActivity.class, params);
                }
            }
        });
        pageNum = 1;
//        showProgress();
        reqApi();
    }

    @Override
    public void reqApi() {
        subscription = RxRequestApi.getInstance().getApiService().getIdleRecommend(pageNum)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .cache()
                .subscribe(new Action1<ResponseBody<List<Idle>>>() {
                    @Override
                    public void call(ResponseBody<List<Idle>> body) {
                        KnmsApp.getInstance().hideLoadView(rlLoading);
                        if (body.isSuccess()) {
//                            hideProgress();
                            updateView(body.data);
                            pageNum++;
                        } else {
                            Tst.showToast(body.desc);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        KnmsApp.getInstance().hideLoadView(rlLoading);
//                        hideProgress();
                        Tst.showToast(throwable.toString());
                    }
                });
    }

    private void updateView(List<Idle> data) {
        if (data == null) return;
        if (pageNum == 1) {
            adapter.setNewData(data);
        } else {
            if (data != null && data.size() > 0) {
                adapter.addData(data);
                adapter.loadMoreComplete();
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

    @Subscribe(tags = {@Tag(BusAction.REFRESH_IDLE)})
    public void Refresh(Boolean isRefresh) {
        if (isRefresh){
            pageNum = 1;
            reqApi();
        }else{
            recycleView.scrollToPosition(0);
        }
    }

    @Override
    public View getScrollableView() {
        return recycleView;
    }
}
