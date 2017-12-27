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

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshRecyclerView;
import com.knms.activity.ShopActivityF;
import com.knms.activity.comment.AddCommentsActivity;
import com.knms.activity.comment.CommentListActivity;
import com.knms.activity.dialog.ChooseReasonActivityDialog;
import com.knms.activity.dialog.PayActivity;
import com.knms.activity.login.FasterLoginActivity;
import com.knms.activity.mall.order.AppPayOrderDetailsActivity;
import com.knms.adapter.order.AppOrderAdapter;
import com.knms.android.R;
import com.knms.bean.AffectedNumber;
import com.knms.bean.ResponseBody;
import com.knms.bean.order.OrderDetail;
import com.knms.bean.order.neworder.AppOrder;
import com.knms.bean.order.neworder.OrderDetails;
import com.knms.bean.order.neworder.OrderTradingCommoditysBean;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.fragment.base.LazyLoadFragment;
import com.knms.net.RxRequestApi;
import com.knms.util.CommonUtils;
import com.knms.util.ConstantObj;
import com.knms.util.DialogHelper;
import com.knms.util.SPUtils;
import com.knms.util.Tst;
import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Administrator on 2017/7/17.
 */

public class AppOrderFragment extends LazyLoadFragment {

    private PullToRefreshRecyclerView refreshLayout;
    private RecyclerView recyclerView;
    private AppOrderAdapter mAdapter;
    private int pageIndex = 1;
    private int positions;

    private static final int REQEST_CODE_REASON = 0x00021;
    private String tradingId = "";

    @Override
    public String getTitle() {
        return "App支付订单";
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        initView(view);
        return view;
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

    private void initView(View view) {
        view.findViewById(R.id.tv_topay).setVisibility(View.GONE);
        refreshLayout = (PullToRefreshRecyclerView) view.findViewById(R.id.recyclerview_order);
        recyclerView = refreshLayout.getRefreshableView();
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        refreshLayout.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<RecyclerView>() {
            @Override
            public void onRefresh(PullToRefreshBase<RecyclerView> refreshView) {
                recyclerView.scrollToPosition(0);
                pageIndex = 1;
                reqApi();
            }
        });

        mAdapter = new AppOrderAdapter(null);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setEnableLoadMore(true);
        mAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                pageIndex++;
                reqApi();
            }
        }, recyclerView);
        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<AppOrder.OrderTradingBosBean>() {
            @Override
            public void onItemClick(BaseQuickAdapter<AppOrder.OrderTradingBosBean, ? extends BaseViewHolder> adapter, View view, int position) {
                if (!SPUtils.isLogin()) {
                    getmActivity().startActivityAnimGeneral(FasterLoginActivity.class, null);
                    return;
                }
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("orderId", mAdapter.getItem(position).tradingId);
                getmActivity().startActivityAnimGeneral(AppPayOrderDetailsActivity.class, map);
            }
        });
        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener<AppOrder.OrderTradingBosBean>() {
            @Override
            public void onItemChildClick(BaseQuickAdapter<AppOrder.OrderTradingBosBean, ? extends BaseViewHolder> adapter, View view, int position) {
                String tradingState = mAdapter.getItem(position).tradingStatus;
                tradingId = mAdapter.getItem(position).tradingId;
                positions = position;

                switch (view.getId()) {
                    case R.id.order_shopname:
                        if (adapter.getItem(position).shopIsHide.equals("2") || TextUtils.equals(adapter.getItem(position).orderType, "2"))
                            return;
                        MobclickAgent.onEvent(mActivity, "order_clickIntoShop");
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("shopId", adapter.getItem(position).shopId);
                        getmActivity().startActivityAnimGeneral(ShopActivityF.class, map);
                        break;
                    case R.id.btn_order_state_left:
                        if (mAdapter.getItem(position).tradingLocking.equals("1") && isRefund(position)) {
                            Tst.showToast("该订单有商品正在退款，请退款完成后再进行操作");
                            return;
                        }
                        if (mAdapter.getItem(position).tradingLocking.equals("1")) {
                            Tst.showToast("订单已被锁定，请稍后再试或联系客服");
                            return;
                        }

                        if (tradingState.equals("100")) {
                            MobclickAgent.onEvent(mActivity, "order_clickCancel");
                            Intent intent = new Intent(getActivity(), ChooseReasonActivityDialog.class);
                            startActivityForResult(intent, REQEST_CODE_REASON);
                        } else if (tradingState.equals("500") && TextUtils.equals(mAdapter.getItem(position).businessmenAppraise, "1") || tradingState.equals("600")
                                || TextUtils.equals(mAdapter.getItem(position).orderType, "2"))
                            showDialog(2, "确认删除该订单？", tradingId);
                        break;
                    case R.id.btn_order_state_right:
                        if (mAdapter.getItem(position).tradingLocking.equals("1") && isRefund(position)) {
                            Tst.showToast("该订单有商品正在退款，请退款完成后再进行操作");
                            return;
                        }
                        if (mAdapter.getItem(position).tradingLocking.equals("1")) {
                            Tst.showToast("订单已被锁定，请稍后再试或联系客服");
                            return;
                        }
                        if (tradingState.equals("100")) {
                            MobclickAgent.onEvent(mActivity, "order_clickGotopay");
                            Intent intent = new Intent(getmActivity(), PayActivity.class);
                            intent.putExtra("orderId", tradingId);
                            startActivityForResult(intent, TOPAY);
                        } else if (tradingState.equals("400")) showDialog(3, "是否确认收货?", tradingId);
                        else if (tradingState.equals("500") && TextUtils.equals(mAdapter.getItem(position).userAppraise, "0")) {
                            MobclickAgent.onEvent(getActivity(), "order_clickGotoComment");
                            Map<String, Object> params = new HashMap<>();
                            params.put("orderId", tradingId);
                            params.put("data", mAdapter.createSimpData(position));
                            getmActivity().startActivityAnimGeneral(AddCommentsActivity.class, params);
                        } else if (tradingState.equals("500") && !TextUtils.equals(mAdapter.getItem(position).userAppraise, "0")) {
                            MobclickAgent.onEvent(getActivity(), "order_clickLookComment");
                            Map<String, Object> params = new HashMap<>();
                            params.put("orderId", tradingId);
                            getmActivity().startActivityAnimGeneral(CommentListActivity.class, params);
                        }
                        break;
                }
            }
        });
    }

    private boolean isRefund(int pos) {
        for (OrderTradingCommoditysBean bean :
                mAdapter.getItem(pos).orderTradingCommoditys) {
            if (TextUtils.equals(bean.tradingCommodityType, "2")) return true;
        }
        return false;
    }

    private void initData() {
        refreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing();
            }
        }, 500);
    }

    private void showDialog(final int type, String title, final String tradingId) {
        new DialogHelper().showPromptDialog(getActivity(), null, title, "取消", null, "确定", new DialogHelper.OnMenuClick() {
            @Override
            public void onLeftMenuClick() {

            }

            @Override
            public void onCenterMenuClick() {

            }

            @Override
            public void onRightMenuClick() {
                if (type == 2) {
                    MobclickAgent.onEvent(mActivity, "order_clickDelete");
                    deteleTrading(tradingId);
                } else {
                    MobclickAgent.onEvent(mActivity, "order_clickConfirmReceipt");
                    confirmReceipt(tradingId);
                }
            }
        });
    }


    @Override
    public void reqApi() {
        isLoad = true;
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("pageIndex", pageIndex);
        RxRequestApi.getInstance().getApiService().getOrderList(paramMap)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<ResponseBody<AppOrder>>() {
                    @Override
                    public void call(ResponseBody<AppOrder> orderListResponseBody) {
                        refreshLayout.onRefreshComplete();
                        if (orderListResponseBody.isSuccess1())
                            updateView(orderListResponseBody.data.orderTradingBos);
                        else Tst.showToast(orderListResponseBody.desc);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        refreshLayout.onRefreshComplete();
                        Tst.showToast(throwable.getMessage());
                    }
                });
    }

    //取消订单
    private void cancelTrading(String tradingId, String recedeRemarks) {
        showProgress();
        Map<String, Object> map = new HashMap<>();
        map.put("tradingId", tradingId);
        map.put("recedeRemarks", recedeRemarks);
        RxRequestApi.getInstance().getApiService().cancelTrading(map)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<ResponseBody<OrderDetails>>() {
                    @Override
                    public void call(ResponseBody<OrderDetails> orderDetailsResponseBody) {
                        hideProgress();
                        if (orderDetailsResponseBody.isSuccess1()) {
                            Tst.showToast("成功取消订单");
                            mAdapter.getItem(positions).tradingStatus = orderDetailsResponseBody.data.orderTradingBo.tradingStatus;
                            mAdapter.getItem(positions).tradingStatusTitle = orderDetailsResponseBody.data.orderTradingBo.tradingStatusTitle;
                            mAdapter.notifyDataSetChanged();
                        }else Tst.showToast(orderDetailsResponseBody.desc);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        hideProgress();
                        Tst.showToast(throwable.getMessage());
                    }
                });
    }

    //确认收货
    private void confirmReceipt(String tradingId) {
        showProgress();
        Map<String, Object> map = new HashMap<>();
        map.put("tradingId", tradingId);
        RxRequestApi.getInstance().getApiService().confirmReceipt(map)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<ResponseBody<OrderDetails>>() {
                    @Override
                    public void call(ResponseBody<OrderDetails> orderDetailsResponseBody) {
                        hideProgress();
                        Tst.showToast(orderDetailsResponseBody.desc);
                        if (orderDetailsResponseBody.isSuccess1()) {
                            mAdapter.getItem(positions).tradingStatus = orderDetailsResponseBody.data.orderTradingBo.tradingStatus;
                            mAdapter.getItem(positions).tradingStatusTitle = orderDetailsResponseBody.data.orderTradingBo.tradingStatusTitle;
                            mAdapter.notifyDataSetChanged();
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

    //删除订单R
    private void deteleTrading(String tradingId) {
        showProgress();
        Map<String, Object> map = new HashMap<>();
        map.put("tradingId", tradingId);
        RxRequestApi.getInstance().getApiService().deleteTrading(map)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<ResponseBody<AffectedNumber>>() {
                    @Override
                    public void call(ResponseBody<AffectedNumber> affectedNumberResponseBody) {
                        hideProgress();
                        if (affectedNumberResponseBody.isSuccess1()) {
                            mAdapter.remove(positions);
                            mAdapter.notifyDataSetChanged();
                            if(mAdapter.getItemCount() == 0) CommonUtils.gotoGuangGuang(mAdapter, "今天是个下单的好日子，赶紧去逛逛");
                            Tst.showToast("成功删除订单");
                        }else Tst.showToast(affectedNumberResponseBody.desc);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        hideProgress();
                        Tst.showToast(throwable.getMessage());
                    }
                });
    }

    private void updateView(List<AppOrder.OrderTradingBosBean> data) {
        if (pageIndex == 1) {
            if (data != null && data.size() > 0)
                mAdapter.setNewData(data);
            else {
                CommonUtils.gotoGuangGuang(mAdapter, "今天是个下单的好日子，赶紧去逛逛");
            }
        } else {
            if (data != null && data.size() > 0) {
                mAdapter.addData(data);
                mAdapter.loadMoreComplete();
            } else {
                mAdapter.loadMoreEnd();
            }
        }
    }

    public final int TOPAY = 0x000989;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        switch (requestCode) {
            case TOPAY:
                boolean isPay = data.getBooleanExtra("isPay", false);
                if (isPay) RxBus.get().post(BusAction.PAYSUCCESS, BusAction.PAYSUCCESS);
                break;
            case REQEST_CODE_REASON:
                if (!TextUtils.isEmpty(data.getStringExtra("reasons"))) {
                    cancelTrading(tradingId, data.getStringExtra("reasons"));
                }
                break;
        }
    }

    @Subscribe(tags = {@Tag(BusAction.REFRESH_MY_ORDER), @Tag(BusAction.COMIT_COMMENTS), @Tag(BusAction.PAYSUCCESS), @Tag(BusAction.CREATE_ORDER_RETAINAGE)})
    public void RefreshOrder(String str) {
        initData();
    }
}
