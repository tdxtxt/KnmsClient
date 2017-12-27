package com.knms.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseViewHolder;
import com.knms.activity.CustomFurnitureDetailsActivity;
import com.knms.adapter.CustomFurnitureAdapter;
import com.knms.android.R;
import com.knms.bean.ResponseBody;
import com.knms.bean.product.Furniture;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.fragment.base.BaseFragment;
import com.knms.net.RxRequestApi;
import com.knms.util.Tst;
import com.knms.view.sticky.HeaderScrollHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.everything.android.ui.overscroll.VerticalOverScrollBounceEffectDecorator;
import me.everything.android.ui.overscroll.adapters.RecyclerViewOverScrollDecorAdapter;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by tdx on 2016/8/24.
 */
public class CustomFurnitureFragment extends BaseFragment implements HeaderScrollHelper.ScrollableContainer {
    private RecyclerView recycleView;
    private CustomFurnitureAdapter adapter;
    private int pageNum = 1;
    private String typeId = "";
    private String inid = "";

    private Subscription mSubscription;
    private Observable<ResponseBody<List<Furniture>>> mObservable;

    public static CustomFurnitureFragment newInstance(String typeId) {
        CustomFurnitureFragment fragment = new CustomFurnitureFragment();
        Bundle args = new Bundle();
        args.putString("id", typeId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RxBus.get().register(this);
        if (getArguments() != null) {
            typeId = getArguments().getString("id");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_recyclerview,null);
        recycleView = (RecyclerView) view.findViewById(R.id.id_stickynavlayout_innerscrollview);
        recycleView.setLayoutManager(new LinearLayoutManager(getmActivity()));
        new VerticalOverScrollBounceEffectDecorator(new RecyclerViewOverScrollDecorAdapter(recycleView));
        return view;
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new CustomFurnitureAdapter(getContext(),new ArrayList<Furniture>());
        adapter.setEnableLoadMore(true);
        adapter.setOnLoadMoreListener(new com.chad.library.adapter.base.BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                reqApi();
            }
        },recycleView);
        recycleView.setAdapter(adapter);
        adapter.setOnItemClickListener(new com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener<Furniture>() {
            @Override
            public void onItemClick(com.chad.library.adapter.base.BaseQuickAdapter<Furniture, ? extends BaseViewHolder> adapter, View view, int position) {
                if(adapter.getData().size() <= position) return;
                Furniture item = adapter.getData().get(position);
                if(item == null) return;
                Map<String,Object> params= new HashMap<String, Object>();
                params.put("id",item.id);
                getmActivity().startActivityAnimGeneral(CustomFurnitureDetailsActivity.class,params);
            }
        });
        pageNum = 1;
        reqApi();
    }

    @Override
    public void reqApi() {
        if (mSubscription != null) mSubscription.unsubscribe();
        mSubscription = RxRequestApi.getInstance().getApiService().getFurnitures(typeId,pageNum)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<List<Furniture>>>() {
                    @Override
                    public void call(ResponseBody<List<Furniture>> body) {
                        if(body.isSuccess()){
                            updateView(body.data);
                            pageNum ++;
                        }else{
                            Tst.showToast(body.desc);
                        }
                    }
                });
    }
    private void updateView(List<Furniture> data) {
        if(data == null) return;
        if(pageNum == 1){
            if (data.size() > 0) {
                adapter.setNewData(data);
            }else {
                adapter.setEmptyView(R.layout.layout_view_no_data);
            }
        }else{
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
        if (mSubscription != null) {
            mSubscription.unsubscribe();
        }
        RxBus.get().unregister(this);
        super.onDestroy();
    }

    @Subscribe(tags = {@Tag(BusAction.REFRESH_IDLE)})
    public void refresh(Boolean isRefresh) {
        if(isRefresh) {
            pageNum=1;
            reqApi();
        }else recycleView.scrollToPosition(0);
    }

    @Override
    public View getScrollableView() {
        return recycleView;
    }
}
