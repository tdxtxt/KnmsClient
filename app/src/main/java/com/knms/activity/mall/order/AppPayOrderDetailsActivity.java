package com.knms.activity.mall.order;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.knms.activity.QuickComplaintActivity;
import com.knms.activity.ShopActivityF;
import com.knms.activity.base.HeadNotifyBaseActivity;
import com.knms.activity.comment.AddCommentsActivity;
import com.knms.activity.comment.CommentListActivity;
import com.knms.activity.details.canbuy.ProductDetailsActivity;
import com.knms.activity.dialog.ChooseReasonActivityDialog;
import com.knms.activity.dialog.PayActivity;
import com.knms.activity.im.ChatActivity;
import com.knms.activity.im.KnmsKefuChatActivity;
import com.knms.adapter.NestedScrollViewOverScrollDecorAdapter;
import com.knms.adapter.order.TradingProductAdapter;
import com.knms.android.R;
import com.knms.bean.AffectedNumber;
import com.knms.bean.ResponseBody;
import com.knms.bean.order.Complaints;
import com.knms.bean.order.OrderDetail;
import com.knms.bean.order.neworder.OrderDetails;
import com.knms.bean.order.neworder.OrderTradingCommoditysBean;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.net.RxRequestApi;
import com.knms.util.CommonUtils;
import com.knms.util.ConstantObj;
import com.knms.util.DialogHelper;
import com.knms.util.LocalDisplay;
import com.knms.util.Tst;
import com.knms.view.DividerItemDecoration;
import com.umeng.analytics.MobclickAgent;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import me.everything.android.ui.overscroll.VerticalOverScrollBounceEffectDecorator;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2017/7/18.
 * App支付订单的详情页
 * orderId:订单ID
 */

public class AppPayOrderDetailsActivity extends HeadNotifyBaseActivity implements View.OnClickListener {

    private String orderId;
    private TextView tvTradingState, tvTradingShopName, tvMailingName, tvMailingAddress, tvMailingPhone, tvFreight,
            tvActualMoney, tvBuyerRemarks, tvBuyerRemarksLabel, btnQuickComplaints, btnBottomleft, btnBottomRight,
            tvOrderCreateTime, tvOrderPayNumber, tvOrderNumber, tvPayType, tvCountDown, tvCopyOrderId;

    private RecyclerView mRecyclerView;
    private TradingProductAdapter mAdapter;
    private LinearLayout llContactSeller, llSellerPhone, llCountdown;
    private String tradingState, tradingId, userAppraise, businessmenAppraise, sid, shopId, shopPhone, tradingLocking;

    private int orderCountdown;

    private SimpleDateFormat formatter;
    private boolean isComplaint;
    private NestedScrollView mScrollView;

    private static final int REQEST_CODE_REASON = 0x00031;
    private String orderType, shopIsHide;//	订单类型 1：实体 ，2 虚拟
    private String contactName, contactPhone;
    private OrderDetails orderDetails;

    @Override
    protected void getParmas(Intent intent) {
        super.getParmas(intent);
        orderId = intent.getStringExtra("orderId");
    }

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("订单详情");
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_apporder_details_layout;
    }

    @Override
    protected void initView() {
        tvCopyOrderId = findView(R.id.tv_copy_orderid);
        llCountdown = findView(R.id.ll_close_down_countdown);
        tvCountDown = findView(R.id.tv_order_countdown);
        tvOrderCreateTime = findView(R.id.tv_order_createtime);
        tvOrderPayNumber = findView(R.id.tv_order_pay_order_number);
        tvOrderNumber = findView(R.id.tv_order_number);
        tvPayType = findView(R.id.tv_order_pay_type);
        llContactSeller = findView(R.id.ll_contact_seller);
        llSellerPhone = findView(R.id.ll_seller_phone);
        tvTradingState = findView(R.id.tv_tradingStatusTitle);
        tvTradingShopName = findView(R.id.order_shopname);
        tvMailingName = findView(R.id.tv_mailingName);
        tvMailingAddress = findView(R.id.tv_mailingAddress);
        tvMailingPhone = findView(R.id.tv_mailingPhone);
        tvFreight = findView(R.id.tv_freight);
        tvActualMoney = findView(R.id.tv_actualMoney);
        tvBuyerRemarks = findView(R.id.tv_buyerRemarks);
        tvBuyerRemarksLabel = findView(R.id.tv_buyerRemarks_label);
        btnQuickComplaints = findView(R.id.tv_quick_complaints);
        btnBottomleft = findView(R.id.btn_order_operation_left);
        btnBottomRight = findView(R.id.btn_order_operation_right);
        mRecyclerView = findView(R.id.recycler_order_item);
        findView(R.id.iv_arrow_right).setVisibility(View.GONE);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
//        linearLayoutManager.setSmoothScrollbarEnabled(true);
//        linearLayoutManager.setAutoMeasureEnabled(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setFocusable(false);

        mScrollView = findView(R.id.scrollView);
        new VerticalOverScrollBounceEffectDecorator(new NestedScrollViewOverScrollDecorAdapter(mScrollView));
    }

    @Override
    protected void initData() {
        formatter = new SimpleDateFormat("HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        btnBottomRight.setOnClickListener(this);
        btnBottomleft.setOnClickListener(this);
        btnQuickComplaints.setOnClickListener(this);
        llContactSeller.setOnClickListener(this);
        llSellerPhone.setOnClickListener(this);
        tvCopyOrderId.setOnClickListener(this);
        tvTradingShopName.setOnClickListener(this);
        reqApi();
    }

    @Override
    protected void reqApi() {
        showProgress();
        Map<String, Object> map = new HashMap<>();
        map.put("tradingId", orderId);
        RxRequestApi.getInstance().getApiService().getTradingDetails(map)
                .compose(this.<ResponseBody<OrderDetails>>applySchedulers())
                .subscribe(new Action1<ResponseBody<OrderDetails>>() {
                    @Override
                    public void call(ResponseBody<OrderDetails> orderDetailsResponseBody) {
                        hideProgress();
                        if (orderDetailsResponseBody.isSuccess1()) {
                            orderDetails = orderDetailsResponseBody.data;
                            updateView(orderDetails);
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

    private void updateView(OrderDetails data) {
        if (data == null) return;
        tradingLocking = data.orderTradingBo.tradingLocking;
        contactName = data.orderTradingBo.mailingName;
        contactPhone = data.orderTradingBo.mailingPhone;
        shopIsHide = data.orderTradingBo.shopIsHide;
        orderType = data.orderTradingBo.orderType;
        isComplaint = data.orderTradingBo.isComplaint == 1;
        sid = data.orderTradingBo.ssmerchantid;
        shopId = data.orderTradingBo.shopId;
        shopPhone = data.orderTradingBo.shopPhone;
        tradingState = data.orderTradingBo.tradingStatus;
        tradingId = data.orderTradingBo.tradingId;
        userAppraise = data.orderTradingBo.userAppraise;
        businessmenAppraise = data.orderTradingBo.businessmenAppraise;
        orderCountdown = data.orderTradingBo.orderCountdown;
        if (data.orderTradingBo.tradingStatus.equals("100") && data.orderTradingBo.orderCountdown > 0) {
            orderCountdown *= 1000;
            countDown();
        } else {
            llCountdown.setVisibility(View.GONE);
        }

        if (orderType.equals("2")) {
            ((TextView) findView(R.id.tv_contact_seller)).setText("联系客服");
            ((TextView) findView(R.id.tv_seller_phone)).setText("客服电话");
            findView(R.id.layout_address_view).setVisibility(View.GONE);
        } else {
            findView(R.id.layout_address_view).setVisibility(View.VISIBLE);
            tvMailingName.setText("收货人：" + data.orderTradingBo.mailingName);
            tvMailingAddress.setText("收货地址：" + data.orderTradingBo.mailingAddress);
            tvMailingPhone.setText(data.orderTradingBo.mailingPhone);
        }
        if (!TextUtils.isEmpty(data.orderTradingBo.buyerRemarks) && data.orderTradingBo.buyerRemarks.length() >= 4 &&
                TextUtils.equals(data.orderTradingBo.buyerRemarks.substring(0, 4), "尾款备注")) {
            tvBuyerRemarksLabel.setText("尾款备注");
            String str = data.orderTradingBo.buyerRemarks.replaceAll("尾款备注(/n|\n)?", "").replaceAll("/n", "<br/>");
            tvBuyerRemarks.setText(Html.fromHtml(str));
        } else {
            tvBuyerRemarksLabel.setText("买家备注");
            tvBuyerRemarks.setText(data.orderTradingBo.buyerRemarks);
        }

        ((TextView) findView(R.id.tv_pay_remark)).setText(data.orderTradingBo.tradingStatus.equals("100") || data.orderTradingBo.tradingStatus.equals("200") ? "需付款：" : "实付款：");

        tvTradingShopName.setText(data.orderTradingBo.shopName);
        tvTradingShopName.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this,R.drawable.icon_shop), null, TextUtils.equals("2", orderType) ? null : ContextCompat.getDrawable(this,R.drawable.qiepian_33), null);

        tvFreight.setText(CommonUtils.addMoneySymbol(data.orderTradingBo.tradingTransportMoneyTitle));
        tvActualMoney.setText(CommonUtils.addMoneySymbol(data.orderTradingBo.actualMoney));
        tvBuyerRemarks.setVisibility(TextUtils.isEmpty(data.orderTradingBo.buyerRemarks) ? View.GONE : View.VISIBLE);
        tvBuyerRemarksLabel.setVisibility(TextUtils.isEmpty(data.orderTradingBo.buyerRemarks) ? View.GONE : View.VISIBLE);
        mAdapter = new TradingProductAdapter(data.orderTradingBo.orderTradingCommoditys, data.orderTradingBo.tradingStatus, orderType);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL_LIST, LocalDisplay.dip2px(1), Color.parseColor("#e6e6e6")));
        tvOrderCreateTime.setText("创建时间：" + data.orderTradingBo.createTime);
        tvOrderNumber.setText("订单编号：" + data.orderTradingBo.tradingSerialid);
        tvOrderPayNumber.setText(TextUtils.isEmpty(data.orderTradingBo.payOrderId) ? "" : "支付订单号：" + data.orderTradingBo.payOrderId);
        tvPayType.setText(TextUtils.isEmpty(data.orderTradingBo.payTypeTitle) ? "" : "支付方式：" + data.orderTradingBo.payTypeTitle);
        tvOrderPayNumber.setVisibility(TextUtils.isEmpty(data.orderTradingBo.payOrderId) ? View.GONE : View.VISIBLE);
        tvPayType.setVisibility(TextUtils.isEmpty(data.orderTradingBo.payTypeTitle) ? View.GONE : View.VISIBLE);
        switch (tradingState) {
            case "100"://待付款
                btnBottomleft.setText("取消订单");
                btnBottomRight.setText("去付款");
                tvTradingState.setText(TextUtils.isEmpty(data.orderTradingBo.tradingStatusTitle) ? "待付款" : data.orderTradingBo.tradingStatusTitle);
                btnQuickComplaints.setVisibility(View.GONE);
                break;
            case "200":
                tvTradingState.setText(TextUtils.isEmpty(data.orderTradingBo.tradingStatusTitle) ? "付款确认中" : data.orderTradingBo.tradingStatusTitle);
                findView(R.id.ll_order_details_bottom).setVisibility(View.GONE);
                break;
            case "300"://待送货
                btnBottomleft.setVisibility(View.GONE);
                btnBottomRight.setVisibility(View.GONE);
                tvTradingState.setText(TextUtils.isEmpty(data.orderTradingBo.tradingStatusTitle) ? "待送货" : data.orderTradingBo.tradingStatusTitle);
                break;
            case "400"://送货完成
                btnBottomleft.setVisibility(View.GONE);
                tvTradingState.setText(TextUtils.isEmpty(data.orderTradingBo.tradingStatusTitle) ? "送货完成" : data.orderTradingBo.tradingStatusTitle);
                if (TextUtils.equals("2", orderType)) btnBottomRight.setVisibility(View.GONE);
                else btnBottomRight.setText("确认收货");
                break;
            case "500"://已完成(待评价，待回复，已回复)
                if (TextUtils.equals("2", orderType)) {
                    btnBottomleft.setText("删除订单");
                    tvTradingState.setText(TextUtils.isEmpty(data.orderTradingBo.tradingStatusTitle) ? "已完成" : data.orderTradingBo.tradingStatusTitle);
                    btnBottomRight.setVisibility(View.GONE);
                    btnQuickComplaints.setVisibility(View.GONE);
                } else {
                    btnBottomleft.setVisibility(TextUtils.equals(businessmenAppraise, "1") ? View.VISIBLE : View.GONE);
                    if (TextUtils.equals(data.orderTradingBo.userAppraise, "0")) {
                        tvTradingState.setText(TextUtils.isEmpty(data.orderTradingBo.tradingStatusTitle) ? "待评价" : data.orderTradingBo.tradingStatusTitle);
                        btnBottomRight.setText("去评价");
                    } else {
                        tvTradingState.setText(TextUtils.isEmpty(data.orderTradingBo.tradingStatusTitle) ? "待回复" : data.orderTradingBo.tradingStatusTitle);
                        btnBottomRight.setText("查看评价");
                    }

                }
                break;
            case "600"://已关闭
                btnBottomleft.setText("删除订单");
                tvTradingState.setText(TextUtils.isEmpty(data.orderTradingBo.tradingStatusTitle) ? "已关闭" : data.orderTradingBo.tradingStatusTitle);
                btnBottomRight.setVisibility(View.GONE);
                btnQuickComplaints.setVisibility(View.GONE);
                break;
            case "900"://订单被锁定（无操作）
            default:
                tvTradingState.setText(TextUtils.isEmpty(data.orderTradingBo.tradingStatusTitle) ? "订单被锁定" : data.orderTradingBo.tradingStatusTitle);
                findView(R.id.ll_order_details_bottom).setVisibility(View.GONE);
                break;
        }

        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<OrderTradingCommoditysBean>() {
            @Override
            public void onItemClick(BaseQuickAdapter<OrderTradingCommoditysBean, ? extends BaseViewHolder> adapter, View view, int position) {
                if (TextUtils.equals("e58cc22ef06e486d8dfdfd8ca4cec67568c6ce27", mAdapter.getItem(position).showId))
                    return;
                MobclickAgent.onEvent(AppPayOrderDetailsActivity.this, "order_clickIntoProduct");
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("id", mAdapter.getItem(position).showId);
                startActivityAnimGeneral(ProductDetailsActivity.class, map);
            }
        });

        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener<OrderTradingCommoditysBean>() {
            @Override
            public void onItemChildClick(BaseQuickAdapter<OrderTradingCommoditysBean, ? extends BaseViewHolder> adapter, View view, int position) {
                if (view.getId() == R.id.tv_refund) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("tradingId", orderId);
                    map.put("tradingCommodityId", mAdapter.getItem(position).tradingCommodityId);
                    map.put("tradingState", tradingState);
                    startActivityAnimGeneral(mAdapter.getItem(position).tradingCommodityType.equals("1") ? ApplyRefundActivity.class : ApplyRefundDetailsActivity.class, map);
                }
            }
        });
    }

    @Override
    public String setStatisticsTitle() {
        return "订单详情";
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_quick_complaints:
                if (tradingLocking.equals("1") && isRefund()) {
                    Tst.showToast("该订单有商品正在退款，请退款完成后再进行操作");
                    return;
                }
                if (tradingLocking.equals("1")) {
                    Tst.showToast("订单已被锁定，请稍后再试或联系客服");
                    return;
                }

                getComplaints();
                break;
            case R.id.btn_order_operation_left:
                if (tradingLocking.equals("1") && isRefund()) {
                    Tst.showToast("该订单有商品正在退款，请退款完成后再进行操作");
                    return;
                }
                if (tradingLocking.equals("1")) {
                    Tst.showToast("订单已被锁定，请稍后再试或联系客服");
                    return;
                }
                if (tradingState.equals("100")) {
                    Intent intent = new Intent(AppPayOrderDetailsActivity.this, ChooseReasonActivityDialog.class);
                    startActivityForResult(intent, REQEST_CODE_REASON);
                } else if (tradingState.equals("500") && TextUtils.equals(businessmenAppraise, "1") || tradingState.equals("600") || TextUtils.equals(orderType, "2"))
                    showDialog(2, "确认删除该订单？", tradingId);
                break;
            case R.id.btn_order_operation_right:
                if (tradingLocking.equals("1") && isRefund()) {
                    Tst.showToast("该订单有商品正在退款，请退款完成后再进行操作");
                    return;
                }
                if (tradingLocking.equals("1")) {
                    Tst.showToast("订单已被锁定，请稍后再试或联系客服");
                    return;
                }
                if (tradingState.equals("100")) {
                    Intent intent = new Intent(this, PayActivity.class);
                    intent.putExtra("orderId", tradingId);
                    startActivityForResult(intent, TOPAY);
                } else if (tradingState.equals("400")) showDialog(3, "是否确认收货?", tradingId);
                else if (tradingState.equals("500") && TextUtils.equals(userAppraise, "0")) {
                    MobclickAgent.onEvent(this, "order_clickGotoComment");
                    Map<String, Object> params = new HashMap<>();
                    params.put("orderId", tradingId);
                    params.put("data", mAdapter.createSimpData());
                    startActivityAnimGeneral(AddCommentsActivity.class, params);
                } else if (tradingState.equals("500") && !TextUtils.equals(userAppraise, "0")) {
                    MobclickAgent.onEvent(this, "order_clickLookComment");
                    Map<String, Object> params = new HashMap<>();
                    params.put("orderId", tradingId);
                    startActivityAnimGeneral(CommentListActivity.class, params);
                }
                break;
            case R.id.ll_contact_seller:
                if (orderType.equals("2")) {
                    startActivityAnimGeneral(KnmsKefuChatActivity.class, null);
                } else {
                    Map<String, Object> parmas = new HashMap<>();
                    parmas.put("sid", sid);
                    parmas.put("shopId", shopId);
                    startActivityAnimGeneral(ChatActivity.class, parmas);
                }
                break;
            case R.id.ll_seller_phone:
                if (TextUtils.equals(orderType, "2")) shopPhone = "023-63317666";
                if (TextUtils.isEmpty(shopPhone)) {
                    Tst.showToast("电话号码为空");
                    return;
                }
                showDialog(4, "是否拨打" + shopPhone, "");
                break;
            case R.id.tv_copy_orderid:
                if (!TextUtils.isEmpty(tvOrderNumber.getText())) {
                    CommonUtils.systemCopy(tvOrderNumber.getText() + "");
                    Tst.showToast("复制订单编号成功！");
                }
                break;
            case R.id.order_shopname:
                if (shopIsHide.equals("2") || TextUtils.equals(orderType, "2"))
                    return;
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("shopId", shopId);
                startActivityAnimGeneral(ShopActivityF.class, map);
                break;
        }
    }

    private boolean isRefund() {
        for (OrderTradingCommoditysBean bean :
                mAdapter.getData()) {
            if (TextUtils.equals(bean.tradingCommodityType, "2")) return true;
        }
        return false;
    }

    private void showDialog(final int type, String title, final String tradingId) {
        new DialogHelper().showPromptDialog(this, null, title, "取消", null, "确定", new DialogHelper.OnMenuClick() {
            @Override
            public void onLeftMenuClick() {

            }

            @Override
            public void onCenterMenuClick() {

            }

            @Override
            public void onRightMenuClick() {
                if (type == 2) deteleTrading(tradingId);
                else if (type == 3) confirmReceipt(tradingId);
                else if (type == 4) {
                    Intent mIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
                            + shopPhone));
                    startActivity(mIntent);
                }
            }
        });
    }

    //删除订单
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
                            Tst.showToast("成功删除订单");
                            RxBus.get().post(BusAction.REFRESH_MY_ORDER, "");
                            finshActivity();
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

    //取消订单
    private void cancelTrading(String tradingId, String reasons) {
        showProgress();
        Map<String, Object> map = new HashMap<>();
        map.put("tradingId", tradingId);
        map.put("recedeRemarks", reasons);
        RxRequestApi.getInstance().getApiService().cancelTrading(map)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<ResponseBody<OrderDetails>>() {
                    @Override
                    public void call(ResponseBody<OrderDetails> orderDetailsResponseBody) {
                        hideProgress();
                        if (orderDetailsResponseBody.isSuccess1()) {
                            Tst.showToast("成功取消订单");
                            RxBus.get().post(BusAction.REFRESH_MY_ORDER, "");
                            orderDetails.orderTradingBo.tradingStatus = orderDetailsResponseBody.data.orderTradingBo.tradingStatus;
                            orderDetails.orderTradingBo.tradingStatusTitle = orderDetailsResponseBody.data.orderTradingBo.tradingStatusTitle;
                            updateView(orderDetails);
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
                    public void call(ResponseBody<OrderDetails> body) {
                        hideProgress();
                        if (body.isSuccess1()) {
                            RxBus.get().post(BusAction.REFRESH_MY_ORDER, "");
                            Tst.showToast(body.desc);
                            orderDetails.orderTradingBo.tradingStatus = body.data.orderTradingBo.tradingStatus;
                            orderDetails.orderTradingBo.tradingStatusTitle = body.data.orderTradingBo.tradingStatusTitle;
                            updateView(orderDetails);
//                            btnBottomleft.setVisibility(View.GONE);
//                            btnBottomRight.setText("去评价");
//                            tvTradingState.setText("待评价");
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

    //快捷投诉

    private void getComplaints() {
        showProgress();
        RxRequestApi.getInstance().getApiService().getComplaintsList(orderId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<List<Complaints>>>() {
                    @Override
                    public void call(ResponseBody<List<Complaints>> listResponseBody) {
                        hideProgress();
                        if (listResponseBody.isSuccess()) {
                            Map<String, Object> params = new HashMap<String, Object>();
                            params.put("tel", contactPhone);
                            params.put("name", contactName);
                            params.put("orderId", orderId);
                            params.put("orderType", 2);
                            params.put("complaintsList", listResponseBody.data);
                            params.put("isComplaints", isComplaint);
                            if (listResponseBody.data.size() == 0)
                                MobclickAgent.onEvent(AppPayOrderDetailsActivity.this, "order_clickGotoComplaint");
                            else
                                MobclickAgent.onEvent(AppPayOrderDetailsActivity.this, "order_clickLoopComplaint");

                            startActivityAnimGeneral(listResponseBody.data.size() == 0 ? QuickComplaintActivity.class : ComplaintsListActivity.class, params);
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

    Subscription subscription;

    private void countDown() {
        new CountDownTimer(orderCountdown, 1000) {
            @Override
            public void onTick(long finish) {
                timeFormat();
            }

            @Override
            public void onFinish() {
                reqApi();
            }
        }.start();
    }

    private void timeFormat() {
        String hms = formatter.format(orderCountdown);
        orderCountdown -= 1000;
        tvCountDown.setText(hms);
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

    @Subscribe(tags = {@Tag(BusAction.COMIT_COMMENTS), @Tag(BusAction.PAYSUCCESS), @Tag(BusAction.SUBMIT_REFUND_SUCCESS)})
    public void RefreshOrder(String str) {
        reqApi();
    }

    @Override
    protected void onDestroy() {
        if (subscription != null && !subscription.isUnsubscribed()) subscription.unsubscribe();
        super.onDestroy();
    }
}
