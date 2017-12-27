package com.knms.activity.mall.address;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshRecyclerView;
import com.knms.activity.base.HeadBaseActivity;
import com.knms.adapter.ShippingAddressAdapter;
import com.knms.android.R;
import com.knms.app.KnmsApp;
import com.knms.bean.ResponseBody;
import com.knms.bean.address.CreateAddress;
import com.knms.bean.address.ShippingAddres;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.net.RxRequestApi;
import com.knms.util.ConstantObj;
import com.knms.util.ToolsHelper;
import com.knms.util.Tst;
import com.umeng.analytics.MobclickAgent;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import me.everything.android.ui.overscroll.VerticalOverScrollBounceEffectDecorator;
import me.everything.android.ui.overscroll.adapters.RecyclerViewOverScrollDecorAdapter;
import rx.functions.Action1;

/**
 * Created by Administrator on 2017/7/14.
 * 收货信息列表
 */

public class ShippingAddressListActivity extends HeadBaseActivity {

    private RecyclerView mRecyclerView;
    private ShippingAddressAdapter mAdapter;
    private ShippingAddres shippingAddres;
    private ShippingAddres.orderMailingAddressBos defaultsAddress;
    private PullToRefreshRecyclerView pullToRefreshRecyclerView;

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("选择收货信息");
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_shopping_address_list;
    }

    @Override
    protected void initView() {
        setRightMenuCallBack(new RightCallBack() {
            @Override
            public void setRightContent(TextView tv, ImageView icon) {
                icon.setVisibility(View.GONE);
                tv.setText("管理");
            }

            @Override
            public void onclick() {
                MobclickAgent.onEvent(ShippingAddressListActivity.this,"address_clickManager");
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("shippingAddres", shippingAddres);
                startActivityAnimGeneral(ManagementAddressActivity.class, map);
            }
        });
        findView(R.id.tv_add_shippingAddress).setVisibility(View.GONE);
        pullToRefreshRecyclerView = findView(R.id.rv_shopping_address_list);
        mRecyclerView=pullToRefreshRecyclerView.getRefreshableView();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        new VerticalOverScrollBounceEffectDecorator(new RecyclerViewOverScrollDecorAdapter(mRecyclerView));
    }

    @Override
    protected void initData() {
        mAdapter = new ShippingAddressAdapter(null);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<ShippingAddres.orderMailingAddressBos>() {
            @Override
            public void onItemClick(BaseQuickAdapter<ShippingAddres.orderMailingAddressBos, ? extends BaseViewHolder> adapter, View view, int position) {
                defaultsAddress = mAdapter.getItem(position);
                KnmsApp.getInstance().finishActivity(ShippingAddressListActivity.this);
            }
        });
        pullToRefreshRecyclerView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<RecyclerView>() {
            @Override
            public void onRefresh(PullToRefreshBase<RecyclerView> refreshView) {
                reqApi();
            }
        });

        pullToRefreshRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
             pullToRefreshRecyclerView.setRefreshing();
            }
        },500);

    }

    @Override
    protected void reqApi() {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", ConstantObj.TEMP_USERID);
        RxRequestApi.getInstance().getApiService().getShoppingAddressList(map)
                .compose(this.<ResponseBody<ShippingAddres>>applySchedulers())
                .subscribe(new Action1<ResponseBody<ShippingAddres>>() {
                    @Override
                    public void call(ResponseBody<ShippingAddres> listResponseBody) {
                        pullToRefreshRecyclerView.onRefreshComplete();
                        if (listResponseBody.isSuccess1()) {
                            shippingAddres = listResponseBody.data;
                            ToolsHelper.getInstance().sort(listResponseBody.data.getOrderMailingAddressBos(), "addresstype");
                            Collections.reverse(listResponseBody.data.getOrderMailingAddressBos());
                            mAdapter.setNewData(listResponseBody.data.getOrderMailingAddressBos());
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        pullToRefreshRecyclerView.onRefreshComplete();
                        Tst.showToast(throwable.getMessage());
                    }
                });
    }


    @Override
    public String setStatisticsTitle() {
        return "选择收货信息";
    }

    @Override
    public void finish() {
        getIntent().putExtra("addresDetails", (Serializable) backParameters());
        setResult(RESULT_OK,getIntent());
        super.finish();
    }

    private CreateAddress backParameters() {
        CreateAddress createAddress = new CreateAddress();
        if (defaultsAddress == null && mAdapter.getData().size() > 0)
            defaultsAddress = mAdapter.getItem(0);
        if (defaultsAddress == null) return createAddress;
        CreateAddress.OrderMailingAddressBoBean bean = new CreateAddress.OrderMailingAddressBoBean();
        bean.addressid = defaultsAddress.addressid;
        bean.mailingaddress = defaultsAddress.mailingaddress;
        bean.mailingname = defaultsAddress.mailingname;
        bean.mailingphone = defaultsAddress.mailingphone;
        createAddress.orderMailingAddressBo = bean;
        return createAddress;
    }

    @Subscribe(tags = {@Tag(BusAction.REFRESH_SHIPPINGADDRES)})
    public void updateAddress(String str) {
        reqApi();
    }

}
