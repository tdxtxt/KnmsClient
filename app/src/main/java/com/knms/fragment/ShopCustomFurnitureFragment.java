package com.knms.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.google.gson.reflect.TypeToken;
import com.knms.activity.CustomFurnitureDetailsActivity;
import com.knms.adapter.ShopCustomFurnitureFragmentAdapter;
import com.knms.android.R;
import com.knms.app.KnmsApp;
import com.knms.bean.ResponseBody;
import com.knms.bean.shop.CustFurniture;
import com.knms.fragment.base.BaseFragment;
import com.knms.net.HttpConstant;
import com.knms.net.ReqApi;
import com.knms.oncall.AsyncHttpCallBack;
import com.knms.util.Tst;
import com.knms.view.clash.FullyLinearLayoutManager;
import com.knms.view.sticky.HeaderScrollHelper;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tdx on 2016/8/24.
 * 店铺 定制家具列表
 */
public class ShopCustomFurnitureFragment extends BaseFragment implements HeaderScrollHelper.ScrollableContainer{

    private RelativeLayout rl_status;
    private RecyclerView mRecyclerView;
    private ShopCustomFurnitureFragmentAdapter adapter;
    private int pageNum = 1;
    public String shopId;

    @Override
    public String getTitle() {
        return "定制家具";
    }

    public static ShopCustomFurnitureFragment newInstance(String shopId) {
        ShopCustomFurnitureFragment fragment = new ShopCustomFurnitureFragment();
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
        View view = inflater.inflate(R.layout.fragment_shop_custom_furnitre, null);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new FullyLinearLayoutManager(getmActivity()));
        rl_status = (RelativeLayout) view.findViewById(R.id.rl_status);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new ShopCustomFurnitureFragmentAdapter(getContext(),new ArrayList<CustFurniture>());
        mRecyclerView.setAdapter(adapter);

        adapter.loadMoreEnd(true);
        adapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                reqApi();
            }
        },mRecyclerView);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<CustFurniture>() {
            @Override
            public void onItemClick(BaseQuickAdapter<CustFurniture, ? extends BaseViewHolder> adapter, View view, int position) {
                CustFurniture item = adapter.getData().get(position);
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("id", item.inid);
                getmActivity().startActivityAnimGeneral(CustomFurnitureDetailsActivity.class, params);
            }
        });
        pageNum = 1;
        reqApi();
    }

    @Override
    public void reqApi() {
        KnmsApp.getInstance().hideLoadView(rl_status);
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("shopId", shopId);
        params.put("pageIndex", pageNum);
        ReqApi.getInstance().postMethod(HttpConstant.customList, params, new AsyncHttpCallBack<List<CustFurniture>>() {
            @Override
            public void onSuccess(ResponseBody<List<CustFurniture>> body) {
                hideProgress();
                if (body.isSuccess()) {
                    updateView(body.data);
                    pageNum++;
                } else {
                    Tst.showToast(body.desc);
                }
            }

            @Override
            public void onFailure(String msg) {
                hideProgress();
                Tst.showToast(msg);
            }

            @Override
            public Type setType() {
                return new TypeToken<ResponseBody<List<CustFurniture>>>() {
                }.getType();
            }
        });

    }

    private void updateView(List<CustFurniture> data) {
        if (data == null) return;
        if (pageNum == 1) {
            if (data.size() > 0) {
                adapter.setNewData(data);
            } else {
                KnmsApp.getInstance().showDataEmpty(rl_status, "商家暂未提供该服务", R.drawable.no_data_shop);
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

    public void scrollTo(){
        mRecyclerView.scrollToPosition(0);
    }

    public void refreshApi() {
        pageNum = 1;
        reqApi();
    }

    @Override
    public View getScrollableView() {
        return mRecyclerView;
    }
}
