package com.knms.activity.mall.address;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshRecyclerView;
import com.knms.activity.base.HeadBaseActivity;
import com.knms.adapter.ShippingAddressAdapter;
import com.knms.android.R;
import com.knms.app.KnmsApp;
import com.knms.bean.AffectedNumber;
import com.knms.bean.ResponseBody;
import com.knms.bean.address.CreateAddress;
import com.knms.bean.address.ShippingAddres;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.net.RxRequestApi;
import com.knms.util.ConstantObj;
import com.knms.util.DialogHelper;
import com.knms.util.ToolsHelper;
import com.knms.util.Tst;
import com.umeng.analytics.MobclickAgent;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.everything.android.ui.overscroll.VerticalOverScrollBounceEffectDecorator;
import me.everything.android.ui.overscroll.adapters.RecyclerViewOverScrollDecorAdapter;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2017/7/14.
 * 收货信息管理
 * shippingAddres：收货信息列表，非必传
 */

public class ManagementAddressActivity extends HeadBaseActivity {

    private RecyclerView mRecyclerView;
    private ShippingAddressAdapter mAdapter;
    private TextView mTextView;
    private List<ShippingAddres.orderMailingAddressBos> list;
    private PullToRefreshRecyclerView pullToRefreshRecyclerView;
    private RelativeLayout rl_status;


    @Override
    protected void getParmas(Intent intent) {
        ShippingAddres shippingAddres = (ShippingAddres) intent.getSerializableExtra("shippingAddres");
        if (shippingAddres != null) list = shippingAddres.getOrderMailingAddressBos();
    }

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("管理收货信息");
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_shopping_address_list;
    }

    @Override
    protected void initView() {
        rl_status = findView(R.id.rl_status);
        mTextView = findView(R.id.tv_add_shippingAddress);
        pullToRefreshRecyclerView = findView(R.id.rv_shopping_address_list);
        mRecyclerView = pullToRefreshRecyclerView.getRefreshableView();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        new VerticalOverScrollBounceEffectDecorator(new RecyclerViewOverScrollDecorAdapter(mRecyclerView));
    }

    @Override
    protected void initData() {
        mAdapter = new ShippingAddressAdapter(null, true);
        mRecyclerView.setAdapter(mAdapter);
        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MobclickAgent.onEvent(ManagementAddressActivity.this,"address_clickSave");
                if(mAdapter.getData().size()>=10){
                    Tst.showToast("收货地址最多10条");
                    return;
                }
                Map<String,Object> map=new HashMap<String, Object>();
                map.put("isfristadd",mAdapter.getData().size()==0?true:false);
                startActivityForResultAnimGeneral(AddAddresActivity.class, map, REQUEST_CODE);
//                startActivityAnimGeneral(AddAddresActivity.class, null);
            }
        });
        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener<ShippingAddres.orderMailingAddressBos>() {
            @Override
            public void onItemChildClick(BaseQuickAdapter<ShippingAddres.orderMailingAddressBos, ? extends BaseViewHolder> adapter, View view, final int position) {
                switch (view.getId()) {
                    case R.id.tv_edit_address:
                        MobclickAgent.onEvent(ManagementAddressActivity.this,"address_clickEdit");
                        Map<String, Object> paramMap = new HashMap<String, Object>();
                        paramMap.put("addressinfo", adapter.getItem(position));
                        startActivityForResultAnimGeneral(AddAddresActivity.class, paramMap, REQUEST_CODE);
                        break;
                    case R.id.tv_delete_addres:
                        MobclickAgent.onEvent(ManagementAddressActivity.this,"address_clickDelete");
                        new DialogHelper().showPromptDialog(ManagementAddressActivity.this, null, "确定要删除该地址吗?", "取消", null, "确定", new DialogHelper.OnMenuClick() {
                            @Override
                            public void onLeftMenuClick() {

                            }

                            @Override
                            public void onCenterMenuClick() {

                            }

                            @Override
                            public void onRightMenuClick() {
                                deleteShippingAddress(mAdapter.getItem(position).addressid, position);
                            }
                        });
                        break;
                    case R.id.cb_default_address:
                        if(TextUtils.equals("1",adapter.getItem(position).addresstype)) return;
                        updateDefaultAddress(adapter.getItem(position).addressid, position);
                        break;
                }
            }
        });

        pullToRefreshRecyclerView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<RecyclerView>() {
            @Override
            public void onRefresh(PullToRefreshBase<RecyclerView> refreshView) {
                reqApi();
            }
        });

        if (list == null)
            pullToRefreshRecyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    pullToRefreshRecyclerView.setRefreshing();
                }
            }, 500);
        else
            update(list);
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
                            update(listResponseBody.data.getOrderMailingAddressBos());
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Tst.showToast(throwable.getMessage());
                    }
                });
    }

    private void update(List<ShippingAddres.orderMailingAddressBos> data) {
        if(data!=null&&data.size()>0)KnmsApp.getInstance().hideLoadView(rl_status);
        ToolsHelper.getInstance().sort(data, "addresstype");
        Collections.reverse(data);
        mAdapter.setNewData(data);
    }

    public void refresh() {
        reqApi();
    }

    private void deleteShippingAddress(String addressId, final int pos) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("addressId", addressId);
        paramMap.put("userId", ConstantObj.TEMP_USERID);
        RxRequestApi.getInstance().getApiService().deleteShippingAddress(paramMap)
                .compose(this.<ResponseBody<AffectedNumber>>applySchedulers())
                .subscribe(new Action1<ResponseBody<AffectedNumber>>() {
                    @Override
                    public void call(ResponseBody<AffectedNumber> stringResponseBody) {
                        Tst.showToast(stringResponseBody.desc);
                        if (stringResponseBody.isSuccess1()) {
                            RxBus.get().post(BusAction.REFRESH_SHIPPINGADDRES, "");
                            mAdapter.getData().remove(pos);
                            if( mAdapter.getData().size()>0)mAdapter.getData().get(0).addresstype = "1";
                            else KnmsApp.getInstance().showDataEmpty(rl_status, "您还没有收货信息哦！", R.drawable.no_order_img);
                            mAdapter.notifyDataSetChanged();

                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Tst.showToast(throwable.getMessage());
                    }
                });
    }

    private void updateDefaultAddress(String addressId, final int pos) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("userId", ConstantObj.TEMP_USERID);
        paramMap.put("addressId", addressId);
        RxRequestApi.getInstance().getApiService().updateDefaultShippingAddress(paramMap)
                .compose(this.<ResponseBody<ShippingAddres.orderMailingAddressBos>>applySchedulers())
                .subscribe(new Action1<ResponseBody<ShippingAddres.orderMailingAddressBos>>() {
                    @Override
                    public void call(ResponseBody<ShippingAddres.orderMailingAddressBos> orderMailingAddressBosResponseBody) {
                        if (orderMailingAddressBosResponseBody.isSuccess1()) {
                            RxBus.get().post(BusAction.REFRESH_SHIPPINGADDRES, "");
                            mAdapter.getData().get(mAdapter.getSelectPos()).addresstype = "2";
                            mAdapter.getData().get(pos).addresstype = "1";
                            update(mAdapter.getData());
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Tst.showToast(throwable.getMessage());
                    }
                });
    }

    private final int REQUEST_CODE = 0x00032;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        if (requestCode == REQUEST_CODE) {
            CreateAddress createAddress = (CreateAddress) data.getSerializableExtra("addresDetails");
            if (createAddress != null) reqApi();
        }
    }

    @Override
    public String setStatisticsTitle() {
        return "管理收货信息";
    }
}
