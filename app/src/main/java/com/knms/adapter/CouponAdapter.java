package com.knms.adapter;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.knms.adapter.base.CommonAdapter;
import com.knms.adapter.base.ViewHolder;
import com.knms.android.R;
import com.knms.bean.ResponseBody;
import com.knms.bean.other.Coupons;
import com.knms.net.RxRequestApi;
import com.knms.util.Tst;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/9/18.
 */
public class CouponAdapter extends CommonAdapter<Coupons> {

    private List<Coupons> mData;

    public CouponAdapter(Context context, List<Coupons> mDatas) {
        super(context, R.layout.item_coupon, mDatas);
        this.mData=mDatas;
    }

    @Override
    public void convert(final ViewHolder helper, final Coupons data) {
        helper.setText(R.id.money, data.spmoney + "元");
        helper.setText(R.id.condition_money, "订单满" + data.spconditions + "元使用");
        helper.setText(R.id.valid_time, "使用时间" + data.spvalid + "-" + data.spinvalid);
        final TextView get = helper.getView(R.id.get);
        if(data.isReceive==1){
            get.setText("已领取");
            get.setTextColor(0xff999999);
            get.setBackgroundResource(R.drawable.textview_border_off);
        }
        get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RxRequestApi.getInstance().getApiService().getCoupons(data.spid)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<ResponseBody>() {
                            @Override
                            public void call(ResponseBody responseBody) {
                                if (responseBody.isSuccess()) {
                                    get.setText("已领取");
                                    get.setTextColor(0xff999999);
                                    get.setBackgroundResource(R.drawable.textview_border_off);
                                    mData.remove(data);
                                    data.isReceive=1;
                                    mData.add(data);

                                } else {
                                    Tst.showToast(responseBody.desc);
                                }
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                Tst.showToast(throwable.getMessage());
                            }
                        });
            }
        });
    }

    @Override
    public List<Coupons> getData() {
        return mData;
    }
}
