package com.knms.activity.dialog;

import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.knms.activity.base.BaseActivity;
import com.knms.android.R;
import com.knms.app.KnmsApp;
import com.knms.bean.welfareservice.CouponCenter;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.util.QRCodeUtil;

/**
 * Created by Administrator on 2017/2/6.
 */

public class QRcodeActivityDialog extends BaseActivity {
    ImageView iv_qrcode;
    private CouponCenter couponDetails;
    private TextView tvTitle,tvMoneyAndCount,tvTime,tvCode;

    @Override
    protected void getParmas(Intent intent) {
        couponDetails= (CouponCenter) intent.getSerializableExtra("coupons");
    }

    @Override
    protected int layoutResID() {
        return R.layout.dialog_qrcode;
    }
    @Override
    protected void initView() {
        iv_qrcode = findView(R.id.iv_qrcode);
        tvTitle=findView(R.id.tv_coupon_title);
        tvMoneyAndCount=findView(R.id.tv_coupon_money_count);
        tvTime=findView(R.id.tv_start_end_time);
        tvCode=findView(R.id.tv_qr_code);

    }
    @Override
    protected void initData() {
        tvTitle.setText(couponDetails.title);
        tvMoneyAndCount.setText("￥"+couponDetails.money+" ("+couponDetails.quantity+"张)");
        tvTime.setText((couponDetails.startTime+"—"+couponDetails.endTime).replace("-","."));
        tvCode.setText(couponDetails.qrcode.replaceAll(".{4}(?!$)", "$0 "));

        findView(R.id.ivBtn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        QRCodeUtil.showThreadImage(this,couponDetails.qrcode,iv_qrcode, 0);
    }
    @Override
    public String setStatisticsTitle() {
        return "优惠券二维码页";
    }


    @Override
    public void finish() {
        RxBus.get().post(BusAction.REFRESH_COUPONS,"");
        super.finish();
        overridePendingTransition(0, 0);
//        KnmsApp.getInstance().finishActivity(QRcodeActivityDialog.class);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
       return false;
    }
}
