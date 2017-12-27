package com.knms.activity.mall.order;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.knms.activity.base.HeadNotifyBaseActivity;
import com.knms.activity.dialog.PayActivity;
import com.knms.activity.mall.address.AddAddresActivity;
import com.knms.activity.mall.address.ShippingAddressListActivity;
import com.knms.adapter.order.PreviewOrderAdapter;
import com.knms.android.R;
import com.knms.bean.ResponseBody;
import com.knms.bean.address.CreateAddress;
import com.knms.bean.order.neworder.CreationOrderSuccessful;
import com.knms.bean.order.neworder.PreviewOrder;
import com.knms.bean.order.neworder.RequestBuyParameters;
import com.knms.bean.order.neworder.RequestCreateOrder;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.net.RxRequestApi;
import com.knms.util.CommonUtils;
import com.knms.util.ConstantObj;
import com.knms.util.ScreenUtil;
import com.knms.util.Tst;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.everything.android.ui.overscroll.VerticalOverScrollBounceEffectDecorator;
import me.everything.android.ui.overscroll.adapters.RecyclerViewOverScrollDecorAdapter;
import rx.functions.Action1;

/**
 * Created by Administrator on 2017/7/17.
 * 确认订单
 * buyParameters: List<RequestBuyParameters> 商品规格
 */

public class ConfirmOrderActivity extends HeadNotifyBaseActivity implements View.OnClickListener, View.OnLayoutChangeListener {
    private TextView tvNotDefalutAddres;

    private RelativeLayout showAddressLayout;
    TextView tvMailingAddress;
    TextView tvMailingName;
    TextView tvMailingPhone;
    TextView tvConmitOrder;
    TextView tvTotalMoney;

    PreviewOrderAdapter mAdapter;
    RecyclerView mRecyclerView;
    LinearLayoutManager layoutManager;
    LinearLayout bottomLayout;
    private View viewHead;


    private String addressId, systemEventId;
    private List<RequestCreateOrder.orderParameterBosBean> list = new ArrayList<>();
    List<RequestBuyParameters> listBuyParameters = new ArrayList<>();
    private int type = 1;//1.实体物品 2.虚拟物品


    @Override
    protected void getParmas(Intent intent) {
        super.getParmas(intent);
        type = intent.getIntExtra("type", 1);
        listBuyParameters = (List<RequestBuyParameters>) intent.getSerializableExtra("buyParameters");
    }

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("确认订单");
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_confirm_order_layout;
    }

    @Override
    protected void initView() {
        viewHead = getLayoutInflater().inflate(R.layout.recyclerview_headview_addres, null);
        showAddressLayout = (RelativeLayout) viewHead.findViewById(R.id.view_address);
        tvNotDefalutAddres = (TextView) viewHead.findViewById(R.id.tv_notdefault_address);
        tvMailingAddress = (TextView) viewHead.findViewById(R.id.tv_mailingAddress);
        tvMailingName = (TextView) viewHead.findViewById(R.id.tv_mailingName);
        tvMailingPhone = (TextView) viewHead.findViewById(R.id.tv_mailingPhone);
        tvConmitOrder = findView(R.id.tv_conmit_order);
        mRecyclerView = findView(R.id.rv_preview_order);
        tvTotalMoney = findView(R.id.tv_total_money);
        bottomLayout = findView(R.id.confirm_order_bottom);
    }

    @Override
    protected void initData() {
        mAdapter = new PreviewOrderAdapter(null);
        layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        reqApi();
        tvConmitOrder.setOnClickListener(this);
        showAddressLayout.setOnClickListener(this);
        tvNotDefalutAddres.setOnClickListener(this);
        findView(R.id.root_layout).addOnLayoutChangeListener(this);
        mAdapter.setHeaderView(viewHead);
        new VerticalOverScrollBounceEffectDecorator(new RecyclerViewOverScrollDecorAdapter(mRecyclerView));

    }

    @Override
    protected void reqApi() {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", ConstantObj.TEMP_USERID);
        map.put("buyParameters", listBuyParameters);

        RxRequestApi.getInstance().getApiService().previewOrder(map)
                .compose(this.<ResponseBody<PreviewOrder>>applySchedulers())
                .subscribe(new Action1<ResponseBody<PreviewOrder>>() {
                    @Override
                    public void call(ResponseBody<PreviewOrder> previewOrderResponseBody) {
                        if (previewOrderResponseBody.isSuccess1()) {
                            systemEventId = previewOrderResponseBody.data.systemEventId;
                            tvTotalMoney.setText(CommonUtils.highlight("合计：" + CommonUtils.addMoneySymbol(previewOrderResponseBody.data.totalMoneyTitle), "[￥|¥]\\d+(\\.\\d+)?"));
                            mAdapter.setNewData(previewOrderResponseBody.data.orderTradingBos);
                            tvConmitOrder.setClickable(true);
                        } else {
                            tvConmitOrder.setClickable(false);
                            Tst.showToast(previewOrderResponseBody.desc);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        tvConmitOrder.setClickable(false);
                        Tst.showToast(throwable.getMessage());
                    }
                });

        if (type == 1) {
            Map<String, Object> stringMap = new HashMap<>();
            stringMap.put("userId", ConstantObj.TEMP_USERID);
            RxRequestApi.getInstance().getApiService().getDefaultAddress(stringMap)
                    .compose(this.<ResponseBody<CreateAddress>>applySchedulers())
                    .subscribe(new Action1<ResponseBody<CreateAddress>>() {
                        @Override
                        public void call(ResponseBody<CreateAddress> responseBody) {
                            if (!responseBody.isSuccess1()) {
//                                Tst.showToast(responseBody.desc);
                                tvNotDefalutAddres.setVisibility(View.VISIBLE);
                                showAddressLayout.setVisibility(View.GONE);
                                return;
                            }
                            if (responseBody.data != null) {
                                updateAddress(responseBody.data);
                            } else {
                                tvNotDefalutAddres.setVisibility(View.VISIBLE);
                                showAddressLayout.setVisibility(View.GONE);
                            }
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            tvNotDefalutAddres.setVisibility(View.VISIBLE);
                            showAddressLayout.setVisibility(View.GONE);
                            Tst.showToast(throwable.getMessage());
                        }
                    });
        }
    }

    @Override
    public String setStatisticsTitle() {
        return "确认订单";
    }

    public void updateAddress(CreateAddress address) {
        if (address.orderMailingAddressBo == null) {
            tvNotDefalutAddres.setVisibility(View.VISIBLE);
            showAddressLayout.setVisibility(View.GONE);
            addressId = "";
            return;
        }
        addressId = address.orderMailingAddressBo.addressid;
        showAddressLayout.setVisibility(View.VISIBLE);
        tvNotDefalutAddres.setVisibility(View.GONE);
        tvMailingAddress.setText("收货地址：" + address.orderMailingAddressBo.mailingaddress);
        tvMailingName.setText("收货人：" + address.orderMailingAddressBo.mailingname);
        tvMailingPhone.setText(address.orderMailingAddressBo.mailingphone);
    }


    private final int ADDRESS_CODE = 0x00032;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_notdefault_address:
                Map<String, Object> map = new HashMap<>();
                map.put("isfristadd", true);
                startActivityForResultAnimGeneral(AddAddresActivity.class, map, ADDRESS_CODE);
                break;
            case R.id.view_address:
                startActivityForResultAnimGeneral(ShippingAddressListActivity.class, null, ADDRESS_CODE);
                break;
            case R.id.tv_conmit_order:
                MobclickAgent.onEvent(ConfirmOrderActivity.this,"order_clickSubmit");
                createOder();
                break;
        }
    }

    boolean isToast = false;

    private void createOder() {
        list.clear();
        if (type == 1 && TextUtils.isEmpty(addressId)) {
            Tst.showToast("请填写收货地址");
            return;
        }
        for (int i = 0; i < mAdapter.getData().size(); i++) {
            PreviewOrder.OrderTradingBosBean trading = mAdapter.getData().get(i);
            if (!TextUtils.isEmpty(trading.tradingId)) {
                RequestCreateOrder.orderParameterBosBean requestCreateOrder = new RequestCreateOrder.orderParameterBosBean();
                requestCreateOrder.tradingId = trading.tradingId;
                requestCreateOrder.remarks = trading.tradingRemark;
                list.add(requestCreateOrder);
            }
        }

        final RequestCreateOrder createOrder = new RequestCreateOrder();
        createOrder.systemEventId = systemEventId;
        createOrder.userId = ConstantObj.TEMP_USERID;
        createOrder.addressId = type == 1 ? addressId : "addressidnull";
        createOrder.orderParameterBos = list;

        if (list.size() == 0) {
            Tst.showToast(mAdapter.getCheckMessage());
            return;
        } else if (!TextUtils.isEmpty(mAdapter.getCheckMessage()) && !isToast) {
            Tst.showToast(mAdapter.getCheckMessage());
            isToast = true;
            return;
        }

        RxRequestApi.getInstance().getApiService().createOrder(createOrder)
                .compose(this.<ResponseBody<CreationOrderSuccessful>>applySchedulers())
                .subscribe(new Action1<ResponseBody<CreationOrderSuccessful>>() {
                    @Override
                    public void call(ResponseBody<CreationOrderSuccessful> body) {
                        if (body.isSuccess1()) {
                            if (body.data.tradingStatus.equals("1")) {
                                RxBus.get().post(BusAction.CREATE_ORDER, BusAction.CREATE_ORDER);
                                Intent intent = new Intent(ConfirmOrderActivity.this, PayActivity.class);
                                if (body.data.tradingIds != null) {
                                    intent.putStringArrayListExtra("orderIds", (ArrayList<String>) body.data.tradingIds);
                                }
                                startActivityForResult(intent, TOPAY);
                            }else{
                                if(body.data.orderTradingBos==null||body.data.orderTradingBos.size()==0) return;
                                mAdapter.setNewData(body.data.orderTradingBos);
                                mAdapter.isToast(true);
                                isToast = true;
                            }
                        } else {
                            Tst.showToast(body.desc);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Tst.showToast(throwable.getMessage());
                    }
                });
    }

    public final int TOPAY = 0x000989;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        switch (requestCode) {
            case TOPAY:
                startActivityAnimGeneral(MyOrderActivity.class, null);
                finshActivity();
                break;
            case ADDRESS_CODE:
                CreateAddress createAddress = (CreateAddress) data.getSerializableExtra("addresDetails");
                if (createAddress != null) updateAddress(createAddress);
                break;
        }
    }

    int keyHeight = ScreenUtil.getScreenHeight() / 3;

    @Override
    public void onLayoutChange(View v, int left, int top, int right,
                               int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        //现在认为只要控件将Activity向上推的高度超过了1/3屏幕高，就认为软键盘弹起
        if (oldBottom != 0 && bottom != 0 && (oldBottom - bottom > keyHeight)) {
            bottomLayout.setVisibility(View.GONE);
        } else if (oldBottom != 0 && bottom != 0 && (bottom - oldBottom > keyHeight)) {
            bottomLayout.setVisibility(View.VISIBLE);
            mAdapter.clearEdittextFocus();
        }

    }

    @Override
    public void finshActivity() {
        hideKeyboard();
        super.finshActivity();
    }

}
