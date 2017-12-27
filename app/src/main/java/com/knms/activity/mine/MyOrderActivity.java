package com.knms.activity.mine;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshRecyclerView;
import com.knms.activity.CommWebViewActivity;
import com.knms.activity.ComplaintDetailsActivity;
import com.knms.activity.OrderParticularsActivity;
import com.knms.activity.ShopActivityF;
import com.knms.activity.base.HeadNotifyBaseActivity;
import com.knms.activity.comment.AddCommentActivity;
import com.knms.activity.comment.CommentInfoActivity;
import com.knms.activity.details.OrderDetailsActivity;
import com.knms.adapter.MyOrderAdapter;
import com.knms.app.KnmsApp;
import com.knms.bean.ResponseBody;
import com.knms.bean.order.CommentReward;
import com.knms.bean.order.Order;
import com.knms.bean.other.TipNum;
import com.knms.bean.user.User;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.core.storage.Svn;
import com.knms.net.HttpConstant;
import com.knms.net.ReqApi;
import com.knms.net.RxRequestApi;
import com.knms.oncall.AsyncHttpCallBack;
import com.knms.util.DialogHelper;
import com.knms.util.LocalDisplay;
import com.knms.util.SPUtils;
import com.knms.util.ScreenUtil;
import com.knms.util.Tst;
import com.knms.android.R;
import com.knms.view.FloatView;
import com.umeng.analytics.MobclickAgent;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * 我的订单
 */

public class MyOrderActivity extends HeadNotifyBaseActivity {

    private PullToRefreshRecyclerView mRefreshView;
    private RecyclerView recyclerView;
    private MyOrderAdapter mAdapter;
    private RelativeLayout rl_status;
    private int pageIndex = 1;
    private FloatView ivPjyl;
    private boolean isRefreshData = false;


    @Override
    protected void getParmas(Intent intent) {
        if (!TextUtils.isEmpty(intent.getStringExtra("complaintsId"))) {
            Map<String, Object> map = new HashMap<>();
            map.put("complaintsId", intent.getStringExtra("complaintsId"));
            startActivityAnimGeneral(ComplaintDetailsActivity.class, map);
        }
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_my_order;
    }

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("我的订单");
    }

    @Override
    protected void initView() {
        ivPjyl = findView(R.id.iv_pjyl);
        mRefreshView = findView(R.id.my_order_listview);
        recyclerView = mRefreshView.getRefreshableView();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        rl_status = findView(R.id.rl_status);
        mRefreshView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
    }

    @Override
    protected void initData() {
        mAdapter = new MyOrderAdapter(new ArrayList<Order>());
        recyclerView.setAdapter(mAdapter);

        mAdapter.setEnableLoadMore(true);
        mAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                reqApi();
            }
        }, recyclerView);


        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<Order>() {
            @Override
            public void onItemClick(BaseQuickAdapter<Order, ? extends BaseViewHolder> adapter, View view, int position) {
                MobclickAgent.onEvent(MyOrderActivity.this, "myOrderListItemClick");
                Map<String, Object> map = new HashMap<String, Object>();
                if (adapter.getItem(position).deliverystate == -1) {
                    map.put("orderDetails", adapter.getItem(position));
                    startActivityAnimGeneral(OrderParticularsActivity.class, map);
                } else {
                    map.put("orderid", adapter.getItem(position).orid);
                    startActivityAnimGeneral(OrderDetailsActivity.class, map);
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
                            DialogHelper.showPromptDialog(MyOrderActivity.this, null, "是否确认收货?", "取消", null, "确定", new DialogHelper.OnMenuClick() {
                                @Override
                                public void onLeftMenuClick() {
                                }

                                @Override
                                public void onCenterMenuClick() {
                                }

                                @Override
                                public void onRightMenuClick() {
                                    MobclickAgent.onEvent(MyOrderActivity.this, "orderListConfirmGoodsClick");
                                    confirmGoods(order.orid, position);
                                }
                            });

                        } else if (order.deliverystate == 6 || order.deliverystate == 3) {
                            MobclickAgent.onEvent(MyOrderActivity.this, "orderListToCommentClick");
                            Map<String, Object> params = new HashMap<>();
                            params.put("orderId", order.orid);
                            startActivityAnimGeneral(AddCommentActivity.class, params);
                        } else {
                            MobclickAgent.onEvent(MyOrderActivity.this, "orderListSeeCommentClick");
                            Map<String, Object> params = new HashMap<>();
                            params.put("orderId", order.orid);
                            startActivityAnimGeneral(CommentInfoActivity.class, params);
                        }
                        break;
                    case R.id.goto_shop:
                        MobclickAgent.onEvent(MyOrderActivity.this, "myOrderListShopClick");
                        if (TextUtils.equals("", adapter.getData().get(position).orshopid)) {
                            Tst.showToast("店铺已下线");
                            return;
                        }
                        map.put("shopId", adapter.getData().get(position).orshopid);
                        startActivityAnimGeneral(ShopActivityF.class, map);
                        break;
                    case R.id.tv_orderdetails:
                        if (adapter.getItem(position).deliverystate == -1) {
                            map.put("orderDetails", adapter.getItem(position));
                            startActivityAnimGeneral(OrderParticularsActivity.class, map);
                        } else {
                            map.put("orderid", adapter.getItem(position).orid);
                            map.put("position", 1);
                            startActivityAnimGeneral(OrderDetailsActivity.class, map);
                        }
                        break;
                    case R.id.iv_complaint:
                    case R.id.tv_order_state:
                        break;

                }

            }
        });
        mRefreshView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<RecyclerView>() {
            @Override
            public void onRefresh(PullToRefreshBase<RecyclerView> refreshView) {
                pageIndex = 1;
                reqApi();
            }
        });
        refreshData();
        commentReward();
    }

    private void commentReward() {
        RxRequestApi.getInstance().getApiService().commentReward()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<CommentReward>>() {
                    @Override
                    public void call(ResponseBody<CommentReward> stringResponseBody) {
                        if (stringResponseBody.isSuccess() && stringResponseBody.data.status == 1) {
                            FrameLayout.LayoutParams layoutParamss = new FrameLayout.LayoutParams(LocalDisplay.dip2px(65),LocalDisplay.dip2px(65));
                            layoutParamss.leftMargin = ScreenUtil.getScreenWidth() - LocalDisplay.dp2px(100);
                            layoutParamss.topMargin = LocalDisplay.dp2px(20);
                            ivPjyl.setLayoutParams(layoutParamss);
                            ivPjyl.setUrl(stringResponseBody.data.url);
                            ivPjyl.setVisibility(View.VISIBLE);
                        } else
                            ivPjyl.setVisibility(View.GONE);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        ivPjyl.setVisibility(View.GONE);
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

    @Override
    public String setStatisticsTitle() {
        return "我的订单";
    }

    private void refreshData() {
        mRefreshView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mRefreshView.setRefreshing();
            }
        }, 1000);
    }

    @Override
    protected void reqApi() {
        Map<String, Object> params = new HashMap<>();
        params.put("pageIndex", pageIndex);
        ReqApi.getInstance().postMethod(HttpConstant.myorder, params, new AsyncHttpCallBack<List<Order>>() {
            @Override
            public void onSuccess(ResponseBody<List<Order>> body) {
                mRefreshView.onRefreshComplete();
                if (body.isSuccess()) {
                    updateView(body.data);
                    pageIndex++;
                    TipNum tipNum = Svn.getFromAccount("tipNum");
                    if (tipNum != null) {
                        tipNum.order = 0;
                        Svn.put2Account("tipNum", tipNum);
                    }
                    KnmsApp.getInstance().getUnreadObservable().sendData();
                } else
                    Tst.showToast(body.desc);
            }

            @Override
            public void onFailure(String msg) {
                mRefreshView.onRefreshComplete();
                Tst.showToast(msg);
            }

            @Override
            public Type setType() {
                return new TypeToken<ResponseBody<List<Order>>>() {
                }.getType();
            }
        });
    }

    private void updateView(List<Order> data) {
        if (pageIndex == 1) {
            if (data.size() > 0 && data != null)
                mAdapter.setNewData(data);
            else
                KnmsApp.getInstance().showDataEmpty(rl_status, "今天是下单的好日子，赶紧去逛逛", R.drawable.no_order_img);
        } else {
            if (data != null && data.size() > 0) {
                mAdapter.addData(data);
                mAdapter.loadMoreComplete();
            } else {
                mAdapter.loadMoreEnd();
            }
        }
    }

    @Subscribe(tags = {@Tag(BusAction.REFRESH_MY_ORDER)})
    public void RefreshOrder(String str) {
        isRefreshData = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isRefreshData) {
            pageIndex = 1;
            refreshData();
            isRefreshData = false;
        }
    }

    @Subscribe(tags = {@Tag(BusAction.ACTION_LOGIN)})
    public void loginAccout(User user) {
        refreshData();
    }

}
