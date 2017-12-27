package com.knms.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshRecyclerView;
import com.knms.activity.KnowledgeDetailsActivity;
import com.knms.activity.details.DecorationStyleDetailsActivity;
import com.knms.activity.goods.DiyInspirationActivity;
import com.knms.adapter.DiyInspAdapter;
import com.knms.android.R;
import com.knms.bean.DiyInsp;
import com.knms.bean.ResponseBody;
import com.knms.bean.product.Desytyle;
import com.knms.fragment.base.BaseFragment;
import com.knms.net.RxRequestApi;
import com.knms.util.Tst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by tdx on 2016/8/24.
 */
public class DiyChildFragment extends BaseFragment {
    private PullToRefreshRecyclerView refresh_recyclerView;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private ImageButton btn_top;
    private DiyInspAdapter adapter;
    private int pageNum = 1;
    private String typeId = "";
    private int decorateType;

    private Subscription mSubscription;
    private Observable<ResponseBody<DiyInsp>> mObservable;

    public static DiyChildFragment newInstance(String typeId, int decorateType) {
        DiyChildFragment fragment = new DiyChildFragment();
        Bundle args = new Bundle();
        args.putString("typeId", typeId);
        args.putInt("decorateType", decorateType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            typeId = getArguments().getString("typeId");
            decorateType = getArguments().getInt("decorateType");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_diy_child, null);
        refresh_recyclerView = (PullToRefreshRecyclerView) view.findViewById(R.id.refresh_recyclerView);
        recyclerView = refresh_recyclerView.getRefreshableView();
        recyclerView.setLayoutManager(layoutManager = new LinearLayoutManager(getmActivity()));
        btn_top = (ImageButton) view.findViewById(R.id.btn_top);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new DiyInspAdapter(new ArrayList<Desytyle>());
        recyclerView.setAdapter(adapter);

        refresh_recyclerView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        refresh_recyclerView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<RecyclerView>() {
            @Override
            public void onRefresh(PullToRefreshBase<RecyclerView> refreshView) {
                pageNum = 1;
                reqApi();
            }
        });
        btn_top.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.scrollToPosition(0);
            }
        });
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<Desytyle>() {
            @Override
            public void onItemClick(BaseQuickAdapter<Desytyle, ? extends BaseViewHolder> adapter, View view, int position) {
                Desytyle item = adapter.getItem(position);
                if (item != null) {
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("id", item.id);
                    if (decorateType == DiyInspirationActivity.DecorateStyle) {//家装风格
//                            getmActivity().startActivityAnimGeneral(DecorationStyleDetailsActivity.class, params);
                        List<Desytyle> lists = adapter.getData();
                        List<String> ids = new ArrayList<String>();
                        for (Desytyle desytyle : lists){
                            ids.add(desytyle.id);
                        }
                        params.put("ids", ids);
                        params.put("position",position);
                        params.put("type",typeId);
                        getmActivity().startActivityAnimGeneral(DecorationStyleDetailsActivity.class,params);
                    } else {
                        params.put("pic", item.imageUrl);
                        getmActivity().startActivityAnimGeneral(KnowledgeDetailsActivity.class, params);
                    }
                }
            }
        });
        adapter.setEnableLoadMore(true);
        adapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                reqApi();
            }
        },recyclerView);
        refresh_recyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                refresh_recyclerView.setRefreshing();
            }
        }, 1000);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (recyclerView.getChildCount() == 0)
                    return;
                int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                if (firstVisibleItem < 3)
                    btn_top.setVisibility(View.GONE);
                else
                    btn_top.setVisibility(View.VISIBLE);
            }
        });
    }

//    public int getScrollY() {
//        View c = lv_custom_furnitre.getChildAt(0);
//        if (c == null ) {
//            return 0;
//        }
//        int firstVisiblePosition = lv_custom_furnitre.getFirstVisiblePosition();
//        int top = c.getTop();
//        return -top + firstVisiblePosition * c.getHeight() ;
//    }

    @Override
    public void reqApi() {
        mSubscription = RxRequestApi.getInstance().getApiService().getInspiration(decorateType,typeId,pageNum)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).cache()
                .subscribe(new Action1<ResponseBody<List<Desytyle>>>() {
                    @Override
                    public void call(ResponseBody<List<Desytyle>> body) {
                        refresh_recyclerView.onRefreshComplete();
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
                        refresh_recyclerView.onRefreshComplete();
                        Tst.showToast(throwable.toString());
                    }
                });
    }

    private void updateView(List<Desytyle> data) {
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
        if (mSubscription != null) {
            mSubscription.unsubscribe();
        }
        if (refresh_recyclerView != null) refresh_recyclerView.onRefreshComplete();
        super.onDestroy();
    }
}
