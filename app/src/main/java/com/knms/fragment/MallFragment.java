package com.knms.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.knms.activity.details.canbuy.ProductDetailsActivity;
import com.knms.adapter.BokCommodityAdapter;
import com.knms.android.R;
import com.knms.bean.ResponseBody;
import com.knms.bean.product.BosCommodityBody;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.fragment.base.BaseFragment;
import com.knms.net.RxRequestApi;
import com.knms.util.Tst;
import com.knms.view.sticky.HeaderScrollHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.everything.android.ui.overscroll.VerticalOverScrollBounceEffectDecorator;
import me.everything.android.ui.overscroll.adapters.RecyclerViewOverScrollDecorAdapter;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2017/6/1.
 * 爆款商品分类子界面
 */

public class MallFragment extends BaseFragment implements HeaderScrollHelper.ScrollableContainer {
    private String id;
    private int pageNum = 1;

    private RecyclerView recyclerView;

    private BokCommodityAdapter adapter;

    public static MallFragment newInstance(String classifyId) {
        MallFragment fragment = new MallFragment();
        Bundle args = new Bundle();
        args.putString("id", classifyId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RxBus.get().register(this);
        if (getArguments() != null) {
            id = getArguments().getString("id");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_recyclerview, null);
        recyclerView = (RecyclerView) view.findViewById(R.id.id_stickynavlayout_innerscrollview);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
//        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),DividerItemDecoration.BOTH_SET,LocalDisplay.dip2px(3),Color.parseColor("#f5f5f5")));
        new VerticalOverScrollBounceEffectDecorator(new RecyclerViewOverScrollDecorAdapter(recyclerView));
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new BokCommodityAdapter(null);
        recyclerView.setAdapter(adapter);
        adapter.setEnableLoadMore(true);
        adapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                reqApi();
            }
        }, recyclerView);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<BosCommodityBody.SimpCommodity>() {
            @Override
            public void onItemClick(BaseQuickAdapter<BosCommodityBody.SimpCommodity, ? extends BaseViewHolder> adapter, View view, int position) {
                BosCommodityBody.SimpCommodity item = adapter.getItem(position);
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("id", item.id);
                getmActivity().startActivityAnimGeneral(ProductDetailsActivity.class, params);
            }
        });
        pageNum = 1;
        reqApi();
    }

    @Override
    public void reqApi() {
        Map<String, Object> params = new HashMap<>();
        params.put("classId", id);
        params.put("pageIndex", pageNum);
        RxRequestApi.getInstance().getApiService().getCommoditysByClassifyId(params)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<BosCommodityBody>>() {
                    @Override
                    public void call(ResponseBody<BosCommodityBody> body) {
                        if (body.isSuccess1()) {
                            updateView(body.data.commoditys);
                            pageNum++;
                        } else {
                            Tst.showToast(body.desc);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        adapter.loadMoreEnd();
                    }
                });
    }

    private void updateView(List<BosCommodityBody.SimpCommodity> data) {
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

    @Subscribe(tags = {@Tag(BusAction.REFRESH_IDLE)})
    public void refresh(Boolean isRefresh) {
        if (isRefresh) {
            pageNum = 1;
            reqApi();
        } else
            recyclerView.scrollToPosition(0);
    }

    @Override
    public void onDestroy() {
        RxBus.get().unregister(this);
        super.onDestroy();
    }

    @Override
    public View getScrollableView() {
        return recyclerView;
    }

    private void scrollToPosition(LinearLayoutManager manager) {
        manager.scrollToPositionWithOffset(0, 0);//滚动到第一行
    }
}