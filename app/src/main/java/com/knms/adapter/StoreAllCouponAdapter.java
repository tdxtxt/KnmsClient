package com.knms.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.knms.activity.base.BaseActivity;
import com.knms.adapter.baserecycler.BaseQuickAdapter;
import com.knms.adapter.baserecycler.BaseViewHolder;
import com.knms.android.R;
import com.knms.bean.ResponseBody;
import com.knms.bean.welfareservice.StoreCoupon;
import com.knms.net.RxRequestApi;
import com.knms.util.DialogHelper;
import com.knms.util.Tst;

import java.text.SimpleDateFormat;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/9/27.
 */
public class StoreAllCouponAdapter extends BaseQuickAdapter<StoreCoupon.ValidBean> {

    private String sparea[] = {"A", "B", "C"};
    private int type = 0;//标识优惠券类型是否为过期
    private CallBack callBack;
    private String countNumber;

    public StoreAllCouponAdapter(Context context, List<StoreCoupon.ValidBean> data, int type,String countNumber) {
        super(R.layout.item_common_welfareservice, data);
        this.type = type;
        this.countNumber=countNumber;
    }

    @Override
    protected void convert(final BaseViewHolder helper, final StoreCoupon.ValidBean item) {
//        final TextView tvGetCoupons = helper.getView(R.id.get_coupons);
//        final TextView tvTime = helper.getView(R.id.coupons_time_limit);
//        if (item.sptype == 1) {
//            helper.setBackgroundRes(R.id.welfare_service_centent_layout, R.drawable.yhq_03);
//            tvGetCoupons.setTextColor(0xffFEBF29);
//            tvTime.setTextColor(0xffA97700);
//            helper.setText(R.id.coupons_money, item.spmoney + "");
//            helper.setText(R.id.coupons_condition, "满" + item.spconditions + "元减" + item.spmoney + "元");
//            helper.setVisible(R.id.tv_rmb,true);
//        } else if (item.sptype == 2) {
//            helper.setBackgroundRes(R.id.welfare_service_centent_layout, R.drawable.yhq_04);
//            tvGetCoupons.setTextColor(0xff17D0B7);
//            tvTime.setTextColor(0xff008D7A);
//            helper.setText(R.id.coupons_money, item.spmoney + "折");
//            helper.setText(R.id.coupons_condition, "通用优惠券");
//            helper.setVisible(R.id.tv_rmb,false);
//        } else if (item.sptype == 3) {
//            helper.setBackgroundRes(R.id.welfare_service_centent_layout, R.drawable.yhq_05);
//            tvGetCoupons.setTextColor(0xffFB6161);
//            tvTime.setTextColor(0xffAE2D2D);
//            helper.setText(R.id.coupons_money, item.spmoney + "");
//            helper.setText(R.id.coupons_condition, "满" + item.spconditions + "元减" + item.spmoney + "元");
//            helper.setVisible(R.id.tv_rmb,true);
//        }
//        helper.setImageResource(R.id.coupons_state, 0);
//        if (type == 4) {
//            helper.setBackgroundRes(R.id.welfare_service_centent_layout, R.drawable.yhq_10);
//            tvGetCoupons.setVisibility(View.GONE);
//            tvTime.setTextColor(0xff999999);
//            helper.setImageResource(R.id.coupons_state, R.drawable.yhq1_08);
//        } else if (type == 1) {
//            tvGetCoupons.setText("立即使用");
//        } else if (type == 2) {
//            tvGetCoupons.setText("立即领取");
//        } else if (type == 3) {
//            helper.setBackgroundRes(R.id.welfare_service_centent_layout, R.drawable.yhq_10);
//            tvGetCoupons.setVisibility(View.GONE);
//            tvTime.setTextColor(0xff999999);
//            helper.setImageResource(R.id.coupons_state, R.drawable.yhq1_10);
//
//        }
//        helper.setText(R.id.coupons_name, item.spname);
//
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
//        helper.setText(R.id.coupons_time_limit, "使用期限：" + item.spvalid.split(" ")[0].replace("-", ".") + "—" + item.spinvalid.split(" ")[0].replace("-", "."));
//        helper.setOnClickListener(R.id.get_coupons, new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (tvGetCoupons.getText().equals("立即领取")) {
//                    RxRequestApi.getInstance().getApiService().getCoupons(item.spid)
//                            .subscribeOn(Schedulers.io())
//                            .observeOn(AndroidSchedulers.mainThread())
//                            .subscribe(new Action1<ResponseBody>() {
//                                @Override
//                                public void call(ResponseBody responseBody) {
//                                    if (responseBody.isSuccess()) {
//                                        Tst.showToast(responseBody.desc);
//                                        if (callBack != null) callBack.refresh();
//                                    } else if (!responseBody.desc.equals("需要登录"))
//                                        Tst.showToast(responseBody.desc);
//                                }
//                            }, new Action1<Throwable>() {
//                                @Override
//                                public void call(Throwable throwable) {
//                                    Tst.showToast(throwable.getMessage());
//                                }
//                            });
//                } else {
//                    showDialog(item.spid);
//                }
//            }
//        });
    }

    private void showDialog(final String spid) {
        DialogHelper.showAlertDialog((BaseActivity) mContext, R.layout.dialog_use_prefer, new DialogHelper.OnEventListener<Dialog>() {
            @Override
            public void eventListener(View parentView, final Dialog window) {
                TextView usePrefer = (TextView) parentView.findViewById(R.id.use_prefer);
                TextView cancel = (TextView) parentView.findViewById(R.id.cancel_use_prefer);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        window.dismiss();
                    }
                });
                usePrefer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        window.dismiss();
                        ((BaseActivity) mContext).showProgress();
                        RxRequestApi.getInstance().getApiService().usePrefer(spid,countNumber)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<ResponseBody>() {
                                    @Override
                                    public void call(ResponseBody responseBody) {
                                        ((BaseActivity) mContext).hideProgress();
                                        if (responseBody.isSuccess()) {
                                            Tst.showToast(responseBody.desc);
                                            if (callBack != null) callBack.refresh();
                                        } else Tst.showToast(responseBody.desc);
                                    }
                                }, new Action1<Throwable>() {
                                    @Override
                                    public void call(Throwable throwable) {
                                        ((BaseActivity) mContext).hideProgress();
                                        Tst.showToast(throwable.getMessage());
                                    }
                                });
                    }
                });
            }
        });
    }


    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    public interface CallBack {
        public void refresh();
    }
}