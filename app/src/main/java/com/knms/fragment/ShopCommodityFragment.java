package com.knms.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.knms.activity.details.ProductDetailsBaokActivity;
import com.knms.activity.details.ProductDetailsOrdinaryActivity;
import com.knms.activity.details.base.CannotBuyBaseDetailsActivity;
import com.knms.activity.details.canbuy.ProductDetailsActivity;
import com.knms.adapter.ShopCommodityFragmentAdapter;
import com.knms.android.R;
import com.knms.app.KnmsApp;
import com.knms.bean.ResponseBody;
import com.knms.bean.shop.ShopCommodity;
import com.knms.fragment.base.BaseFragment;
import com.knms.net.RxRequestApi;
import com.knms.util.Tst;
import com.knms.view.clash.FullyGridLayoutManager;
import com.knms.view.sticky.HeaderScrollHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/8/24.
 * 店铺 商品列表
 */
public class ShopCommodityFragment extends BaseFragment implements HeaderScrollHelper.ScrollableContainer {

    private RelativeLayout rl_status;
    private RecyclerView mRecyclerView;
    private List<ShopCommodity> cacheData;
    private int pageNum = 1;
    private ShopCommodityFragmentAdapter adapter;

    private String shopId;

    @Override
    public String getTitle() {
        return "商品";
    }

    public static ShopCommodityFragment newInstance(String shopId) {
        ShopCommodityFragment fragment = new ShopCommodityFragment();
        Bundle args = new Bundle();
        args.putString("shopId", shopId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            shopId = getArguments().getString("shopId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_shop_commodity, null);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        FullyGridLayoutManager layoutManager = new FullyGridLayoutManager(getmActivity(), 2);
        mRecyclerView.setLayoutManager(new FullyGridLayoutManager(getmActivity(), 2));//类似gridview
        layoutManager.setSmoothScrollbarEnabled(true);
//        layoutManager.setAutoMeasureEnabled(true);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setFocusable(false);
        rl_status = (RelativeLayout) view.findViewById(R.id.rl_status);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new ShopCommodityFragmentAdapter(getContext(), new ArrayList<ShopCommodity>());
        mRecyclerView.setAdapter(adapter);
        adapter.loadMoreEnd(true);
        adapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                reqApi();
            }
        }, mRecyclerView);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<ShopCommodity>() {
            @Override
            public void onItemClick(BaseQuickAdapter<ShopCommodity, ? extends BaseViewHolder> adapter, View view, int position) {
                ShopCommodity item = adapter.getData().get(position);
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("id", item.goid);
                getmActivity().startActivityAnimGeneral(item.gotype==6?ProductDetailsActivity.class:item.goisrecommend == 1 ? ProductDetailsBaokActivity.class : ProductDetailsOrdinaryActivity.class, params);
//                getmActivity().startActivityAnimGeneral(item.goisrecommend == 1 ? ProductDetailsActivity.class : ProductDetailsOrdinaryActivity.class, params);
            }
        });
        pageNum = 1;
        reqApi();
    }

    @Override
    public void reqApi() {
        KnmsApp.getInstance().hideLoadView(rl_status);

        RxRequestApi.getInstance().getApiService().getShopGoodsList(shopId, pageNum)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<List<ShopCommodity>>>() {
                    @Override
                    public void call(ResponseBody<List<ShopCommodity>> body) {
                        hideProgress();
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
                        hideProgress();
                        Tst.showToast(throwable.getMessage());
                    }
                });

    }

    public void refreshApi() {
        pageNum = 1;
        reqApi();
    }

    public void scrollTo() {
        mRecyclerView.scrollToPosition(0);
    }

    private void updateView(List<ShopCommodity> data) {
        if (data == null) return;
        if (pageNum == 1) {
            if (data.size() > 0) {
                adapter.setNewData(data);
            } else {
                showDataEmpty(rl_status, "商家暂未提供该服务", R.drawable.no_data_shop);
            }
        } else {
            if (data != null && data.size() > 0) {
                adapter.addData(data);
                adapter.loadMoreComplete();
            } else {
                adapter.loadMoreEnd();
            }
        }
    }

    public void showDataEmpty(RelativeLayout layoutStatus, String text, int imgId) {
        layoutStatus.removeAllViews();
        View mLoadView = LayoutInflater.from(getmActivity()).inflate(R.layout.layout_view_no_data, null);
        mLoadView.setVisibility(View.VISIBLE);
        ((TextView) mLoadView.findViewById(R.id.tv_no_data)).setText(text);
        ((ImageView) mLoadView.findViewById(R.id.img_no_data)).setImageResource(imgId);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutStatus.addView(mLoadView, lp);
        layoutStatus.setVisibility(View.VISIBLE);
    }

    @Override
    public View getScrollableView() {
        return mRecyclerView;
    }
}
