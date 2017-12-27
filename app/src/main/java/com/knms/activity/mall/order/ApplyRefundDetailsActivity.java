package com.knms.activity.mall.order;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.knms.activity.base.HeadNotifyBaseActivity;
import com.knms.activity.im.ChatActivity;
import com.knms.activity.im.KnmsKefuChatActivity;
import com.knms.android.R;
import com.knms.bean.ResponseBody;
import com.knms.bean.order.neworder.RefundsDetails;
import com.knms.net.RxRequestApi;
import com.knms.util.CommonUtils;
import com.knms.util.DialogHelper;
import com.knms.util.ImageLoadHelper;
import com.knms.util.LocalDisplay;
import com.knms.util.Tst;

import java.util.HashMap;
import java.util.Map;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;
import rx.functions.Action1;

/**
 * Created by Administrator on 2017/7/18.
 * 退款详情页
 */

public class ApplyRefundDetailsActivity extends HeadNotifyBaseActivity implements View.OnClickListener {

    Map<String, Object> map = new HashMap<>();

    private TextView tvRefundTitle,tv_RefundResults, tvRefundMoney, tvRecedeReason, tvCreateTime, tvProductName, tvProductAttr, tvRecedeto, tvContactSeller, tvSellerPhone;
    private ImageView ivShowProductImg;

    @Override
    protected void getParmas(Intent intent) {
        super.getParmas(intent);
        map.put("tradingId", intent.getStringExtra("tradingId"));
        map.put("tradingCommodityId", intent.getStringExtra("tradingCommodityId"));
    }

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("退款详情");
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_applyrefund_details_layout;
    }

    @Override
    protected void initView() {
        tv_RefundResults = findView(R.id.tv_refund_remark);
        tvRefundTitle = findView(R.id.tv_refund_title);
        tvRecedeReason = findView(R.id.tv_recedeReason);
        tvRefundMoney = findView(R.id.tv_refund_money);
        tvCreateTime = findView(R.id.tv_createTime);
        ivShowProductImg = findView(R.id.iv_product_img);
        tvProductAttr = findView(R.id.tv_product_attribute);
        tvProductName = findView(R.id.tv_product_name);
        tvRecedeto = findView(R.id.tv_recedeto);
        tvContactSeller = findView(R.id.tv_contact_seller);
        tvContactSeller.setText("在线客服");
        tvSellerPhone = findView(R.id.tv_seller_phone);
        tvSellerPhone.setText("电话客服");
        OverScrollDecoratorHelper.setUpOverScroll((ScrollView) findView(R.id.scrollView));
        findView(R.id.ll_contact_seller).setOnClickListener(this);
       findView(R.id.ll_seller_phone).setOnClickListener(this);

    }

    @Override
    protected void initData() {
        reqApi();
    }

    @Override
    protected void reqApi() {
        RxRequestApi.getInstance().getApiService().refundsDetails(map)
                .compose(this.<ResponseBody<RefundsDetails>>applySchedulers())
                .subscribe(new Action1<ResponseBody<RefundsDetails>>() {
                    @Override
                    public void call(ResponseBody<RefundsDetails> refundsDetailsResponseBody) {
                        if (refundsDetailsResponseBody.isSuccess1()) {
                            updateView(refundsDetailsResponseBody.data);
                        } else Tst.showToast(refundsDetailsResponseBody.desc);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Tst.showToast(throwable.getMessage());
                    }
                });
    }

    private void updateView(RefundsDetails data) {
        if (TextUtils.equals(data.orderRecedeBos.get(0).recedeType, "1")) {
            tvRefundTitle.setText("退款中");
            tv_RefundResults.setText("退款处理中，请耐心等待");
        } else {
            tvRefundTitle.setText("退款成功");
            tv_RefundResults.setText("请确认查收");
            findView(R.id.tv_refunds_hint).setVisibility(View.GONE);
        }
        tvRecedeReason.setText("退款原因：" + data.orderRecedeBos.get(0).recedeReason);
        tvCreateTime.setText("申请时间：" + data.orderRecedeBos.get(0).createTime);
        tvRefundMoney.setText("退款金额：" + CommonUtils.addMoneySymbol(data.orderRecedeBos.get(0).recedeMoney));
        if (TextUtils.isEmpty(data.orderRecedeBos.get(0).recedeTo))
            tvRecedeto.setVisibility(View.GONE);
        else tvRecedeto.setText("退款去向：" + data.orderRecedeBos.get(0).recedeTo);
        tvProductName.setText(data.orderTradingCommodityBo.showName);
        tvProductAttr.setText(data.orderTradingCommodityBo.parameterBriefing);
        ImageLoadHelper.getInstance().displayImage(this, data.orderTradingCommodityBo.specificationImg, ivShowProductImg, LocalDisplay.dp2px(70), LocalDisplay.dp2px(70));

    }

    @Override
    public String setStatisticsTitle() {
        return "退款详情";
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ll_contact_seller:
                    startActivityAnimGeneral(KnmsKefuChatActivity.class, null);
                break;
            case R.id.ll_seller_phone:
                new DialogHelper().showPromptDialog(this, null, "是否拨打:023-63317666"  , "取消", null, "确定", new DialogHelper.OnMenuClick() {
                    @Override
                    public void onLeftMenuClick() {
                    }

                    @Override
                    public void onCenterMenuClick() {
                    }

                    @Override
                    public void onRightMenuClick() {
                        Intent mIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
                                + "023-63317666"));
                        startActivity(mIntent);
                    }
                });
                break;
        }
    }
}
