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
import com.knms.activity.details.DecorationStyleDetailsActivity;
import com.knms.adapter.ShopDecorationStyleFragmentAdapter;
import com.knms.android.R;
import com.knms.app.KnmsApp;
import com.knms.bean.ResponseBody;
import com.knms.bean.shop.DeStyle;
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
 * Created by Administrator on 2016/8/24.
 * 店铺 家装风格列表
 */
public class ShopDecorationStyleFragment extends BaseFragment implements HeaderScrollHelper.ScrollableContainer {

    private RecyclerView mRecyclerView;
    private ShopDecorationStyleFragmentAdapter adapter;
    private int pageNum = 1;
    private RelativeLayout rl_status;

    private String shopId;

    @Override
    public String getTitle() {
        return "家居风格";
    }

    public static ShopDecorationStyleFragment newInstance(String shopId){
        ShopDecorationStyleFragment fragment = new ShopDecorationStyleFragment();
        Bundle args = new Bundle();
        args.putString("shopId",shopId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments()!=null) {
            shopId = getArguments().getString("shopId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_shop_decoration_style,null);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new FullyLinearLayoutManager(getmActivity()));
        rl_status = (RelativeLayout) view.findViewById(R.id.rl_status);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new ShopDecorationStyleFragmentAdapter(getContext(),new ArrayList<DeStyle>());
        mRecyclerView.setAdapter(adapter);
        adapter.loadMoreEnd(true);
        adapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                reqApi();
            }
        },mRecyclerView);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<DeStyle>() {
            @Override
            public void onItemClick(BaseQuickAdapter<DeStyle, ? extends BaseViewHolder> adapter, View view, int position) {
//                DeStyle item = adapter.getItem(position);
                Map<String,Object> params = new HashMap<String, Object>();
                List<DeStyle> lists = adapter.getData();
                List<String> ids = new ArrayList<String>();
                for (DeStyle desytyle : lists){
                    ids.add(desytyle.inid);
                }
                params.put("ids",ids);
                params.put("shopId",shopId);
                params.put("position",position);
                getmActivity().startActivityAnimGeneral(DecorationStyleDetailsActivity.class,params);
            }
        });
        pageNum = 1;
        reqApi();
    }

    @Override
    public void reqApi() {
        KnmsApp.getInstance().hideLoadView(rl_status);
        HashMap<String,Object> params = new HashMap<String,Object>();
        params.put("shopId",shopId);
        params.put("pageIndex",pageNum);//第几页
        ReqApi.getInstance().postMethod(HttpConstant.decorationStyleList, params, new AsyncHttpCallBack<List<DeStyle>>() {
            @Override
            public void onSuccess(ResponseBody<List<DeStyle>> body) {
                hideProgress();
                if (body.isSuccess()) {
                    updateView(body.data);
                    pageNum ++;
                }else {
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
                return new TypeToken<ResponseBody<List<DeStyle>>>(){}.getType();
            }
        });

    }

    private void updateView(List<DeStyle> data) {
        if (data == null) return;
        if (pageNum == 1) {
            if (data.size()>0) {
                adapter.setNewData(data);
            }else {//传空布局
                KnmsApp.getInstance().showDataEmpty(rl_status,"商家暂未提供该服务",R.drawable.no_data_shop);
            }
        }else {
            if (data != null && data.size() > 0) {
                adapter.addData(data);
                adapter.loadMoreComplete();
            }else{
                adapter.loadMoreEnd();
            }
        }
    }
    public void refreshApi(){
        pageNum = 1;
        reqApi();
    }

    public void scrollTo(){
        mRecyclerView.scrollToPosition(0);
    }

    @Override
    public View getScrollableView() {
        return mRecyclerView;
    }
}
