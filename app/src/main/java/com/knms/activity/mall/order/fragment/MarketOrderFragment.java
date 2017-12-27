package com.knms.activity.mall.order.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshRecyclerView;
import com.knms.activity.OrderParticularsActivity;
import com.knms.activity.ShopActivityF;
import com.knms.activity.comment.AddCommentActivity;
import com.knms.activity.comment.CommentInfoActivity;
import com.knms.activity.details.OrderDetailsActivity;
import com.knms.activity.login.FasterLoginActivity;
import com.knms.activity.mall.order.PayRetainageActivity;
import com.knms.adapter.MyOrderAdapter;
import com.knms.android.R;
import com.knms.bean.ResponseBody;
import com.knms.bean.order.Order;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.fragment.base.LazyLoadFragment;
import com.knms.net.RxRequestApi;
import com.knms.util.CommonUtils;
import com.knms.util.DialogHelper;
import com.knms.util.SPUtils;
import com.knms.util.Tst;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2017/7/17.
 */

public class MarketOrderFragment extends LazyLoadFragment {

    private View view;
    private int pageIndex;
    private PullToRefreshRecyclerView pullToRefreshRecyclerView;
    private RecyclerView recyclerView;
    private MyOrderAdapter mAdapter;
    private TextView tvToPay;


    @Override
    public String getTitle() {
        return "商场支付订单";
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = super.onCreateView(inflater, container, savedInstanceState);
        initView();
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RxBus.get().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RxBus.get().unregister(this);
    }


    @Override
    protected int setContentView() {
        return R.layout.layout_order_common_recyclerview;
    }

    @Override
    protected void lazyLoad() {
        if (isLoad) return;
        initData();
    }

    private void initView() {
        tvToPay = (TextView) view.findViewById(R.id.tv_topay);
        pullToRefreshRecyclerView = (PullToRefreshRecyclerView) view.findViewById(R.id.recyclerview_order);
        recyclerView = pullToRefreshRecyclerView.getRefreshableView();
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new MyOrderAdapter(new ArrayList<Order>());
        recyclerView.setAdapter(mAdapter);
        mAdapter.setEnableLoadMore(true);
        mAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                reqApi();
            }
        }, recyclerView);

        pullToRefreshRecyclerView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);

        pullToRefreshRecyclerView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<RecyclerView>() {
            @Override
            public void onRefresh(PullToRefreshBase<RecyclerView> refreshView) {
                pageIndex = 1;
                recyclerView.scrollToPosition(0);
                reqApi();
            }
        });

    }


    private void initData() {
        tvToPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAdapter.getData().size() == 0) {
                    Tst.showToast("您没有订单需要付尾款");
                    return;
                }
                getmActivity().startActivityAnimGeneral(PayRetainageActivity.class, null);
            }
        });
        pullToRefreshRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                pullToRefreshRecyclerView.setRefreshing();
            }
        }, 500);
        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<Order>() {
            @Override
            public void onItemClick(BaseQuickAdapter<Order, ? extends BaseViewHolder> adapter, View view, int position) {
                if (!SPUtils.isLogin()) {
                    getmActivity().startActivityAnimGeneral(FasterLoginActivity.class, null);
                    return;
                }
                MobclickAgent.onEvent(getActivity(), "myOrderListItemClick");
                Map<String, Object> map = new HashMap<String, Object>();
                if (adapter.getItem(position).deliverystate == -1) {
                    map.put("orderDetails", adapter.getItem(position));
                    getmActivity().startActivityAnimGeneral(OrderParticularsActivity.class, map);
                } else {
                    map.put("orderid", adapter.getItem(position).orid);
                    getmActivity().startActivityAnimGeneral(OrderDetailsActivity.class, map);
                }
            }
        });
        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener<Order>() {
            @Override
            public void onItemChildClick(BaseQuickAdapter<Order, ? extends BaseViewHolder> adapter, View view, final int position) {
                Map<String, Object> map = new HashMap<String, Object>();
                final Order order = adapter.getItem(position);
                switch (view.getId()) {
                    case R.id.tv_order_evaluate:
                        if (order.deliverystate == 2) {
                            DialogHelper.showPromptDialog(getActivity(), null, "是否确认收货?", "取消", null, "确定", new DialogHelper.OnMenuClick() {
                                @Override
                                public void onLeftMenuClick() {
                                }

                                @Override
                                public void onCenterMenuClick() {
                                }

                                @Override
                                public void onRightMenuClick() {
                                    MobclickAgent.onEvent(getActivity(), "orderListConfirmGoodsClick");
                                    confirmGoods(order.orid, position);
                                }
                            });

                        } else if (order.deliverystate == 6 || order.deliverystate == 3) {
                            MobclickAgent.onEvent(getActivity(), "orderListToCommentClick");
                            Map<String, Object> params = new HashMap<>();
                            params.put("orderId", order.orid);
                            getmActivity().startActivityAnimGeneral(AddCommentActivity.class, params);
                        } else {
                            MobclickAgent.onEvent(getActivity(), "orderListSeeCommentClick");
                            Map<String, Object> params = new HashMap<>();
                            params.put("orderId", order.orid);
                            getmActivity().startActivityAnimGeneral(CommentInfoActivity.class, params);
                        }
                        break;
                    case R.id.goto_shop:
                        MobclickAgent.onEvent(getActivity(), "myOrderListShopClick");
                        if (TextUtils.equals("", adapter.getData().get(position).orshopid)) {
                            Tst.showToast("店铺已下线");
                            return;
                        }
                        map.put("shopId", adapter.getData().get(position).orshopid);
                        getmActivity().startActivityAnimGeneral(ShopActivityF.class, map);
                        break;
                    case R.id.tv_orderdetails:
                        if (adapter.getItem(position).deliverystate == -1) {
                            map.put("orderDetails", adapter.getItem(position));
                            getmActivity().startActivityAnimGeneral(OrderParticularsActivity.class, map);
                        } else {
                            map.put("orderid", adapter.getItem(position).orid);
                            map.put("position", 1);
                            getmActivity().startActivityAnimGeneral(OrderDetailsActivity.class, map);
                        }
                        break;
                    case R.id.iv_complaint:
                    case R.id.tv_order_state:
                        break;

                }

            }
        });

    }

    @Override
    public void reqApi() {
        isLoad = true;
        RxRequestApi.getInstance().getApiService().getMarketOrderList(pageIndex)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<List<Order>>>() {
                    @Override
                    public void call(ResponseBody<List<Order>> orderResponseBody) {
                        pullToRefreshRecyclerView.onRefreshComplete();
                        if (orderResponseBody.isSuccess()) updateView(orderResponseBody.data);
                        else Tst.showToast(orderResponseBody.desc);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        pullToRefreshRecyclerView.onRefreshComplete();
                        Tst.showToast(throwable.getMessage());
                    }
                });
    }

    private void confirmGoods(String id, final int position) {
        RxRequestApi.getInstance().getApiService().confirmGoods(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<String>>() {
                    @Override
                    public void call(ResponseBody<String> objectResponseBody) {
                        if (objectResponseBody.isSuccess()) {
                            mAdapter.getItem(position).deliverystate = 3;
                            mAdapter.notifyDataSetChanged();
                            Tst.showToast(objectResponseBody.data);
                        } else Tst.showToast(objectResponseBody.desc);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Tst.showToast(throwable.getMessage());
                    }
                });
    }


    private void updateView(List<Order> data) {
        if (pageIndex == 1) {
            if (data != null && data.size() > 0)
                mAdapter.setNewData(data);
            else {
                CommonUtils.gotoIndexGuangGuang(mAdapter, "今天是个下单的好日子，赶紧去逛逛");
            }
        } else {
            if (data != null && data.size() > 0) {
                mAdapter.addData(data);
                mAdapter.loadMoreComplete();
            } else {
                mAdapter.loadMoreEnd();
            }
        }
        pageIndex++;
    }

    @Subscribe(tags = {@Tag(BusAction.REFRESH_MY_ORDER)})
    public void RefreshOrder(String str) {
        pageIndex=1;
        reqApi();
    }
}
