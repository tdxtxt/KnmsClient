package com.knms.fragment.order;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.knms.activity.ComplaintDetailsActivity;
import com.knms.activity.comment.AddCommentActivity;
import com.knms.activity.comment.CommentInfoActivity;
import com.knms.activity.im.ChatActivity;
import com.knms.adapter.ComplaintsAdapter;
import com.knms.adapter.OrderStateAdapter;
import com.knms.adapter.base.CommonAdapter;
import com.knms.adapter.base.ViewHolder;
import com.knms.android.R;
import com.knms.bean.ResponseBody;
import com.knms.bean.order.OrderState;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.fragment.base.BaseFragment;
import com.knms.net.ReqApi;
import com.knms.net.RxRequestApi;
import com.knms.util.DialogHelper;
import com.knms.util.ImageLoadHelper;
import com.knms.util.LocalDisplay;
import com.knms.util.Tst;
import com.knms.view.CircleImageView;
import com.knms.view.TimeLineView;
import com.knms.view.clash.FzLinearLayoutManager;
import com.knms.view.listview.MaxHightListView;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observer;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2017/4/21.
 */

public class OrderStatusFragment extends BaseFragment implements View.OnClickListener {
    private View view;
    private RecyclerView complaintRecyclerView, stateRecyclerView;
    private OrderStateAdapter stateAdapter;
    private String orderId = "";
    private ComplaintsAdapter complaintsAdapter;
    private CircleImageView ivShopLogo;
    private TextView tvShopName, tvTelephoneConnection, tvOnlineConnection, tvSeeEvaluation, tvStateDescribe;
    private TimeLineView timeLineView;
    private PullToRefreshScrollView pullToRefreshScrollView;
    private RelativeLayout rlShopInfoLayout;
    private LinearLayout llSeeEvaluation;
    private int orderState = 1;
    private String[] strs;
    private int step = 1;
    private String shopId, imId;
    List<String> phoneNumberList = new ArrayList<String>();

    public static OrderStatusFragment newInstance(String orderId) {
        OrderStatusFragment fragment = new OrderStatusFragment();
        Bundle args = new Bundle();
        args.putString("order", orderId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RxBus.get().register(this);
        if (getArguments() != null) {
            orderId = getArguments().getString("order");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_order_state_layout, null);
        initView();
        return view;
    }

    private void initView() {
        pullToRefreshScrollView = (PullToRefreshScrollView) view.findViewById(R.id.pull_to_refresh_scrollview);
        complaintRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_order_complaint);
        stateRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_order_schedule);
        ivShopLogo = (CircleImageView) view.findViewById(R.id.iv_shop_logo);
        tvShopName = (TextView) view.findViewById(R.id.tv_shop_name);
        timeLineView = (TimeLineView) view.findViewById(R.id.timeLineView);
        tvTelephoneConnection = (TextView) view.findViewById(R.id.tv_telephone_connection);
        tvOnlineConnection = (TextView) view.findViewById(R.id.tv_online_connection);
        tvSeeEvaluation = (TextView) view.findViewById(R.id.tv_see_evaluation);
        tvStateDescribe = (TextView) view.findViewById(R.id.tv_state_describe);
        rlShopInfoLayout = (RelativeLayout) view.findViewById(R.id.rl_shopinfo_layout);
        llSeeEvaluation= (LinearLayout) view.findViewById(R.id.ll_see_evaluation);
        complaintRecyclerView.setLayoutManager(new FzLinearLayoutManager(getActivity()));
        stateRecyclerView.setLayoutManager(new FzLinearLayoutManager(getActivity()));
        complaintsAdapter = new ComplaintsAdapter(null);
        stateAdapter = new OrderStateAdapter(null);
        stateRecyclerView.setAdapter(stateAdapter);
        complaintRecyclerView.setAdapter(complaintsAdapter);
        tvTelephoneConnection.setOnClickListener(this);
        tvOnlineConnection.setOnClickListener(this);
        tvSeeEvaluation.setOnClickListener(this);
        pullToRefreshScrollView.setMode(PullToRefreshBase.Mode.BOTH);
        initData();

    }

    public void initData() {
        complaintRecyclerView.addOnItemTouchListener(new OnItemChildClickListener() {
            @Override
            public void onSimpleItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                MobclickAgent.onEvent(getActivity(), "orderComplaintSeeDetailsClick");
                Intent intent = new Intent(getActivity(), ComplaintDetailsActivity.class);
                intent.putExtra("complaintsId", complaintsAdapter.getItem(position).ocid);
                startActivity(intent);
            }
        });
        pullToRefreshScrollView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ScrollView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                reqApi();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                pullToRefreshScrollView.onRefreshComplete();
            }
        });
        reqApi();
    }


    @Override
    public void reqApi() {
        showProgress();
        RxRequestApi.getInstance().getApiService().getOrderState(orderId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<OrderState>>() {
                    @Override
                    public void call(ResponseBody<OrderState> listResponseBody) {
                        pullToRefreshScrollView.onRefreshComplete();
                        hideProgress();
                        if (listResponseBody.isSuccess()) {
                            updateView(listResponseBody.data);
                        } else
                            Tst.showToast(listResponseBody.desc);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        pullToRefreshScrollView.onRefreshComplete();
                        hideProgress();
                        Tst.showToast(throwable.getMessage());
                    }
                });
    }

    private void updateView(OrderState data) {

        orderState = data.state;

        String describeStr = "";
        switch (orderState) {
            case 0:
                describeStr = "客户已退货";
                break;
            case 1:
                describeStr = "生成订单，待商家送货";
                break;
            case 2:
                describeStr = "商家送货完成，待客户确认收货";
                break;
            case 3:
                describeStr = "客户确认收货，待客户评价";
                break;
            case 4:
                describeStr = "客户已评价，待商家回复";
                break;
            case 5:
                describeStr = "商家已回复评价";
                break;
            case 6:
                describeStr = "超过10天未收货，系统默认收货";
                break;
        }
        llSeeEvaluation.setVisibility(orderState<2?View.GONE:View.VISIBLE);
        if (orderState == 2) tvSeeEvaluation.setText("确认收货");
        else if (orderState == 6 || orderState == 3) tvSeeEvaluation.setText("去评价");
        else if (orderState == 4 || orderState == 5) tvSeeEvaluation.setText("查看评价");
        tvStateDescribe.setText(describeStr);

        if (orderState == 0) {
            strs = new String[]{"待送货", "客户退货"};
            step = 2;
        } else {
            strs = new String[]{"待送货", "送货完成", "确认收货", "客户评价", "商家回复"};
            step = orderState == 6 ? 3 : orderState;
        }
        timeLineView.setVisibility(View.VISIBLE);
        timeLineView.builder().pointStrings(strs, step)
                .startedCircleColor(Color.parseColor("#FFD400"))
                .underwayCircleColor(Color.parseColor("#FFD400"))
                .preCircleColor(Color.parseColor("#ECECEC"))
                .startedLineColor(Color.parseColor("#FFD400"))
                .preLineColor(Color.parseColor("#ECECEC"))
                .startedStringColor(Color.parseColor("#FEB92C"))
                .underwayStringColor(Color.parseColor("#FEB92C"))
                .preStringColor(Color.GRAY)
                .textSize(LocalDisplay.dp2px(14))
                .lineWidth(LocalDisplay.dp2px(4))
                .radius(LocalDisplay.dp2px(8))
                .load();


        if (data.shopinfo == null) rlShopInfoLayout.setVisibility(View.GONE);
        else {
           if(!TextUtils.isEmpty(data.shopinfo.shopphone))phoneNumberList.add(data.shopinfo.shopphone);
           if(!TextUtils.isEmpty(data.shopinfo.ssphone))phoneNumberList.add(data.shopinfo.ssphone);
            imId = data.shopinfo.ssmerchantid;
            shopId = data.shopinfo.ssid;
            ImageLoadHelper.getInstance().displayImageHead(getActivity(), data.shopinfo.sslogo, ivShopLogo);
            tvShopName.setText(data.shopinfo.ssname);
        }
        stateAdapter.setNewData(data.deliveryList);
        complaintsAdapter.setNewData(data.complaintList);

    }

    @Override
    public String getTitle() {
        return "订单状态";
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_telephone_connection:
                MobclickAgent.onEvent(getActivity(), "orderTelephoneConnectionClick");
                DialogHelper.showBottomDialog(getActivity(), R.layout.dialog_dial_layout, new DialogHelper.OnEventListener<Dialog>() {
                    @Override
                    public void eventListener(View parentView, final Dialog window) {
                        MaxHightListView listView = (MaxHightListView) parentView.findViewById(R.id.listview_phone_number);
                        //Context context,int resId, List<T> mDatas
                        listView.setAdapter(new CommonAdapter<String>(getActivity(), R.layout.item_phone_number, phoneNumberList) {
                            @Override
                            public void convert(ViewHolder helper, final String data) {
                                helper.setText(R.id.tv_phone_number, data);
                                helper.getView(R.id.tv_phone_number).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intentPhone = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + data));
                                        startActivity(intentPhone);
                                    }
                                });
                            }
                        });
                        listView.setMaxHeight(LocalDisplay.dip2px(240));
                        parentView.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                window.dismiss();
                            }
                        });
                    }
                });
                break;
            case R.id.tv_see_evaluation:
                if (orderState == 2) {
                    DialogHelper.showPromptDialog(getActivity(), null, "是否确认收货?", "取消", null, "确定", new DialogHelper.OnMenuClick() {
                        @Override
                        public void onLeftMenuClick() {
                        }

                        @Override
                        public void onCenterMenuClick() {
                        }

                        @Override
                        public void onRightMenuClick() {
                            MobclickAgent.onEvent(getActivity(), "orderStatusConfirmGoodsClick");
                            confirmGoods(orderId);
                        }
                    });
                } else if (orderState == 6 || orderState == 3) {
                    MobclickAgent.onEvent(getActivity(), "orderStatusToCommentClick");
                    Map<String,Object> params = new HashMap<>();
                    params.put("orderId",orderId);
                    getmActivity().startActivityAnimGeneral(AddCommentActivity.class,params);
                } else {
                    MobclickAgent.onEvent(getActivity(), "orderStatusSeeCommentClick");
                    Map<String,Object> params = new HashMap<>();
                    params.put("orderId",orderId);
                    getmActivity().startActivityAnimGeneral(CommentInfoActivity.class,params);
                }
                break;
            case R.id.tv_online_connection:
                MobclickAgent.onEvent(getActivity(), "orderOnlineContactClick");
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra("sid", imId);
                startActivity(intent);
                break;
        }
    }

    private void confirmGoods(String id) {
        showProgress();
        RxRequestApi.getInstance().getApiService().confirmGoods(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<String>>() {
                    @Override
                    public void call(ResponseBody<String> objectResponseBody) {
                        if (objectResponseBody.isSuccess()) {
                            timeLineView.setStep(3);
                            tvSeeEvaluation.setText("去评价");
                            tvStateDescribe.setText("商家送货完成，待客户确认收货");
                            reqApi();
                            RxBus.get().post(BusAction.REFRESH_MY_ORDER, "");
                            Tst.showToast(objectResponseBody.data);
                        }else Tst.showToast(objectResponseBody.desc);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        hideProgress();
                        Tst.showToast(throwable.getMessage());
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RxBus.get().unregister(this);
    }

    @Subscribe(tags = {@Tag(BusAction.REFRESH_MY_ORDER)})
    public void RefreshOrder(String str) {
        reqApi();
    }
}
