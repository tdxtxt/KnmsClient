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
 * Created by tdx on 2016/8/24.
 */
public class IdleClassifyChildFragment extends BaseFragment implements HeaderScrollHelper.ScrollableContainer{
    private RecyclerView recyclerView;
    private IdleAdapter adapter;
    private int pageNum = 1;
    public String typeId = "";
    private RelativeLayout rlLoading;
    private Subscription mSubscription;

    public static IdleClassifyChildFragment newInstance(String typeId) {
        IdleClassifyChildFragment fragment = new IdleClassifyChildFragment();
        Bundle args = new Bundle();
        args.putString("typeId", typeId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            typeId = getArguments().getString("typeId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.layout_recyclerview, null);
        rlLoading= (RelativeLayout) view.findViewById(R.id.rl_loading);
        recyclerView = (RecyclerView) view.findViewById(R.id.id_stickynavlayout_innerscrollview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getmActivity()));
        new VerticalOverScrollBounceEffectDecorator(new RecyclerViewOverScrollDecorAdapter(recyclerView));
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        KnmsApp.getInstance().showDataEmpty(rlLoading, "正在加载中...", R.drawable.no_data_undercarriage);
        adapter = new IdleAdapter(getContext());
        recyclerView.setAdapter(adapter);
        adapter.setEnableLoadMore(true);
        adapter.setOnLoadMoreListener(new com.chad.library.adapter.base.BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                reqApi();
            }
        },recyclerView);
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
        refreshApi();
    }

    @Override
    public void reqApi() {
        if (mSubscription != null) mSubscription.unsubscribe();
        mSubscription = RxRequestApi.getInstance().getApiService().getIdleClassifyList(typeId, pageNum)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .cache()
                .subscribe(new Action1<ResponseBody<List<Idle>>>() {
                    @Override
                    public void call(ResponseBody<List<Idle>> body) {
//                        hideProgress();
                        KnmsApp.getInstance().hideLoadView(rlLoading);
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
                        KnmsApp.getInstance().hideLoadView(rlLoading);
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

    public void refreshApi() {
        pageNum = 1;
//        showProgress();
        reqApi();
    }

    public void scrollTo() {
        recyclerView.scrollToPosition(0);
    }


    @Override
    public void onDestroy() {
        if (mSubscription != null) mSubscription.unsubscribe();
        super.onDestroy();
    }

    @Override
    public View getScrollableView() {
        return recyclerView;
    }
}
