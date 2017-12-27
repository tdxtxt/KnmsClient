package com.knms.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshRecyclerView;
import com.knms.activity.coupons.InvalidCouponActivity;
import com.knms.adapter.WelfareServiceListAdapter;
import com.knms.android.R;
import com.knms.app.KnmsApp;
import com.knms.bean.ResponseBody;
import com.knms.bean.other.TipNum;
import com.knms.bean.user.User;
import com.knms.bean.welfareservice.CouponCenter;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.core.storage.Svn;
import com.knms.fragment.base.BaseFragment;
import com.knms.net.RxRequestApi;
import com.knms.util.SPUtils;
import com.knms.util.Tst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/9/23.
 */
public class MyCouponsFragment extends BaseFragment {

    PullToRefreshRecyclerView layout_refresh;
    RecyclerView recyclerView;
    WelfareServiceListAdapter mAdapter;
    RelativeLayout rl_status;
    LinearLayout bottomLayout;


    TextView tvInvalid, tvNotMoreCoupon;
    View footerlayout;
    int sumNum = -1, curSumNum;

    private int type = 1, pageIndex = 1;
    private boolean isClick = false;

    public static MyCouponsFragment newInstance(int type) {
        MyCouponsFragment fragment = new MyCouponsFragment();
        Bundle args = new Bundle();
        args.putInt("type", type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getInt("type");
        }
        RxBus.get().register(this);
        sumNum = (int) SPUtils.getFromApp(type == 1 ? SPUtils.KeyConstant.cashCouponNumber : SPUtils.KeyConstant.cooperationCouponNumber, -1);
        isClick = (boolean) SPUtils.getFromApp(type == 1 ? SPUtils.KeyConstant.isNewCashInvalidCoupon : SPUtils.KeyConstant.isNewCooperationInvalidCoupon, false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_comm_recyclerview, null);
        initView(view);
        return view;
    }

    private void initView(View view) {
        bottomLayout = (LinearLayout) view.findViewById(R.id.ll_bottom_layout);
        footerlayout = getActivity().getLayoutInflater().inflate(R.layout.coupon_recycler_footer_layout, null);
        tvNotMoreCoupon = (TextView) footerlayout.findViewById(R.id.tv_select_invalid);
        tvInvalid = (TextView) view.findViewById(R.id.tv_select_invalid);
        rl_status = (RelativeLayout) view.findViewById(R.id.rl_status);
        layout_refresh = (PullToRefreshRecyclerView) view.findViewById(R.id.refresh_recyclerView);
        recyclerView = layout_refresh.getRefreshableView();
        recyclerView.setLayoutManager(new LinearLayoutManager(getmActivity()));
        layout_refresh.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        switch (type) {
            case 1:
                mAdapter = new WelfareServiceListAdapter(new ArrayList<CouponCenter>(), 1, false);
                recyclerView.setAdapter(mAdapter);
                break;
            case 2:
                mAdapter = new WelfareServiceListAdapter(new ArrayList<CouponCenter>(), 2, false);
                recyclerView.setAdapter(mAdapter);
                break;
        }
        layout_refresh.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<RecyclerView>() {
            @Override
            public void onRefresh(PullToRefreshBase<RecyclerView> refreshView) {
                reqApi();
            }
        });
        showProgress();
        reqApi();
        tvInvalid.setOnClickListener(clickListener);
        tvNotMoreCoupon.setOnClickListener(clickListener);
        if (isClick) {
            tvNotMoreCoupon.setTextColor(0xfffebf29);
            tvInvalid.setTextColor(0xfffebf29);
        }
    }


    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            tvNotMoreCoupon.setTextColor(0xff999999);
            tvInvalid.setTextColor(0xff999999);
            Map<String, Object> param = new HashMap<>();
            param.put("type", type);
            mActivity.startActivityAnimGeneral(InvalidCouponActivity.class, param);
            SPUtils.saveToApp(type == 1 ? SPUtils.KeyConstant.isNewCashInvalidCoupon : SPUtils.KeyConstant.isNewCooperationInvalidCoupon, false);

        }
    };


    @Override
    public void reqApi() {
        KnmsApp.getInstance().hideLoadView(rl_status);
        RxRequestApi.getInstance().getApiService().getMyCouponsList(type, 1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<List<CouponCenter>>>() {
                    @Override
                    public void call(ResponseBody<List<CouponCenter>> body) {
                        layout_refresh.onRefreshComplete();
                        hideProgress();
                        if (body.isSuccess()) {
                            updateView(body.data);
                            TipNum tipNum = Svn.getFromAccount("tipNum");
                            if(tipNum != null){
                                tipNum.coupon = 0;
                                Svn.put2Account("tipNum",tipNum);
                            }
                            KnmsApp.getInstance().getUnreadObservable().sendData();
                        } else {
                            Tst.showToast(body.desc);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        hideProgress();
                        layout_refresh.onRefreshComplete();
                        Tst.showToast(throwable.getMessage());
                    }
                });
    }

    public void updateView(List<CouponCenter> data) {

        curSumNum = 0;
        for (CouponCenter coupons :
                data) {
            curSumNum += coupons.quantity;
        }
        if (curSumNum < sumNum) {
            tvNotMoreCoupon.setTextColor(0xfffebf29);
            tvInvalid.setTextColor(0xfffebf29);
            SPUtils.saveToApp(type == 1 ? SPUtils.KeyConstant.isNewCashInvalidCoupon : SPUtils.KeyConstant.isNewCooperationInvalidCoupon, true);
//            SpannableStringBuilder builder = new SpannableStringBuilder("没有更多有效券了 | 查看无效券>>");
//            ForegroundColorSpan yellow = new ForegroundColorSpan(0xfffebf29);
//            builder.setSpan(yellow, 11, 18, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            tvInvalid.setText(builder);
        }
        sumNum = curSumNum;
        SPUtils.saveToApp(type == 1 ? SPUtils.KeyConstant.cashCouponNumber : SPUtils.KeyConstant.cooperationCouponNumber, sumNum);
        if (pageIndex == 1 && data.size() == 0) {
            KnmsApp.getInstance().showDataEmpty(rl_status, "您暂时没有可用的优惠券", R.drawable.no_data_on_offer);
            mAdapter.setNewData(new ArrayList<CouponCenter>());
            mAdapter.notifyDataSetChanged();
            recyclerView.removeAllViews();
            footerlayout.setVisibility(View.GONE);
            bottomLayout.setVisibility(View.VISIBLE);
            return;
        }
        bottomLayout.setVisibility(View.GONE);
        mAdapter.setNewData(data);
        footerlayout.setVisibility(View.VISIBLE);
        if (!mAdapter.hasFooterLayout())
            mAdapter.addFooterView(footerlayout);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        KnmsApp.getInstance().onDestroy();
        RxBus.get().unregister(this);
    }

    @Subscribe(tags = {@Tag(BusAction.ACTION_LOGIN)})
    public void loginAccout(User user) {
        SPUtils.saveToApp(type == 1 ? SPUtils.KeyConstant.cashCouponNumber : SPUtils.KeyConstant.cooperationCouponNumber, -1);
        SPUtils.saveToApp(type == 1 ? SPUtils.KeyConstant.isNewCashInvalidCoupon : SPUtils.KeyConstant.isNewCooperationInvalidCoupon, false);
        reqApi();
    }

    @Subscribe(tags = {@Tag(BusAction.REFRESH_COUPONS)})
    public void refresh(String str) {
        if (type == 1) reqApi();
    }

    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && recyclerView != null) {
            if (mAdapter.getData().size() == 0) reqApi();
        }
    }

}
