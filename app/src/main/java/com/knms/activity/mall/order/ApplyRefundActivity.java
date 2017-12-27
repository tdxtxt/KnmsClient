package com.knms.activity.mall.order;

import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.knms.activity.base.HeadNotifyBaseActivity;
import com.knms.activity.dialog.ChooseReasonActivityDialog;
import com.knms.activity.pic.CameraActivityF;
import com.knms.activity.pic.ImgBrowerPagerActivity;
import com.knms.android.R;
import com.knms.bean.ResponseBody;
import com.knms.bean.order.neworder.InitRefunds;
import com.knms.bean.order.neworder.OrderTradingCommoditysBean;
import com.knms.bean.order.neworder.RefundReasons;
import com.knms.bean.other.Pic;
import com.knms.core.compress.Luban;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.net.RxRequestApi;
import com.knms.net.uploadfile.RxUploadApi;
import com.knms.util.Arith;
import com.knms.util.CommonUtils;
import com.knms.util.ImageLoadHelper;
import com.knms.util.LocalDisplay;
import com.knms.util.Tst;
import com.knms.view.AddPhotoView;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2017/7/17.
 * 申请退款页
 * tradingCommodityId:订单下的商品ID
 * tradingId：订单ID
 */

public class ApplyRefundActivity extends HeadNotifyBaseActivity implements View.OnClickListener {

    private TextView tvRefundContent, tvRefundMoney, tvRecedeReason, tvProductName, tvProductAttr, tvNumber, tvRelease;
    private ImageView ivShowProductImg;
    private EditText editRefundsRemark;
    private AddPhotoView mAddPhotoView;

    private List<String> imgIds = new ArrayList<>();
    private Subscription subscription;

    Map<String, Object> map = new HashMap<>();
    OrderTradingCommoditysBean productDetails;
    String tradingState;

    private final int REQUST_CODE_PHOTO = 0x00026;
    private static final int REQEST_CODE_REASON = 0x00021;

    @Override
    protected void getParmas(Intent intent) {
        super.getParmas(intent);
        map.put("tradingCommodityId", intent.getStringExtra("tradingCommodityId"));
        map.put("tradingId", intent.getStringExtra("tradingId"));
        tradingState = intent.getStringExtra("tradingState");
        productDetails = (OrderTradingCommoditysBean) intent.getSerializableExtra("details");
    }

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("申请退款");
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_apply_refund_layout;
    }

    @Override
    protected void initView() {
        tvRelease = findView(R.id.release);
        tvNumber = findView(R.id.tv_number);
        ivShowProductImg = findView(R.id.iv_product_img);
        tvProductAttr = findView(R.id.tv_product_attribute);
        tvProductName = findView(R.id.tv_product_name);
        tvRefundMoney = findView(R.id.tv_refunds_total_money);
        tvRecedeReason = findView(R.id.tv_recede_reason);
        tvRefundContent = findView(R.id.tv_refunds_content);
        editRefundsRemark = findView(R.id.edit_refunds_remark);
        mAddPhotoView = findView(R.id.addPhotoView);
    }

    @Override
    protected void initData() {
        editRefundsRemark.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                tvNumber.setText(s.length() + "/200");
            }
        });
        tvRecedeReason.setOnClickListener(this);
        tvRelease.setOnClickListener(this);
        mAddPhotoView.setAddListener(new AddPhotoView.AddListener() {
            @Override
            public void onclick() {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("photos", mAddPhotoView.getPhotos());
                startActivityForResultAnimGeneral(CameraActivityF.class, params, REQUST_CODE_PHOTO);
            }

            @Override
            public void otherClick(int position) {
                Intent intent = new Intent(ApplyRefundActivity.this, ImgBrowerPagerActivity.class);
                intent.putExtra("position", position);
                intent.putExtra("pics", (Serializable) mAddPhotoView.getPhotos());
                intent.putExtra("showDeleteBtn", true);
                startActivity(intent);
            }
        });
        reqApi();
    }

    @Override
    protected void reqApi() {
        RxRequestApi.getInstance().getApiService().initRefunds(map)
                .compose(this.<ResponseBody<InitRefunds>>applySchedulers())
                .subscribe(new Action1<ResponseBody<InitRefunds>>() {
                    @Override
                    public void call(ResponseBody<InitRefunds> initRefundsResponseBody) {
                        if (initRefundsResponseBody.isSuccess1()) {
                            updateView(initRefundsResponseBody.data);
                        } else Tst.showToast(initRefundsResponseBody.desc);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Tst.showToast(throwable.getMessage());
                    }
                });

    }

    private void updateView(InitRefunds refunds) {
        String totalMoney = "", refundContent = "";
        if (TextUtils.equals("300", tradingState)) {
            totalMoney = "" + Arith.add(CommonUtils.str2Double(refunds.orderTradingCommodityBo.totalRealityMoney), CommonUtils.str2Double(refunds.orderTradingCommodityBo.totalTransportMoney));
        } else {
            totalMoney = refunds.orderTradingCommodityBo.totalRealityMoney;
        }

        if (!TextUtils.equals("300", tradingState) || TextUtils.isEmpty(refunds.orderTradingCommodityBo.totalTransportMoney) || CommonUtils.str2Double(refunds.orderTradingCommodityBo.totalTransportMoney) == 0.f) {
            refundContent = "最多" + totalMoney + "，不含配送费";
        } else {
            refundContent = "最多" + totalMoney + "，含配送费" + refunds.orderTradingCommodityBo.totalTransportMoney;
        }

        tvProductName.setText(refunds.orderTradingCommodityBo.showName);
        tvProductAttr.setText(refunds.orderTradingCommodityBo.parameterBriefing);
        ImageLoadHelper.getInstance().displayImage(this, refunds.orderTradingCommodityBo.specificationImg, ivShowProductImg, LocalDisplay.dp2px(70), LocalDisplay.dp2px(70));
        tvRefundMoney.setText(CommonUtils.addMoneySymbol(totalMoney));
        tvRefundContent.setText(CommonUtils.addMoneySymbol(refundContent));
    }

    private void submitRefunds() {
        map.put("refundsReason", tvRecedeReason.getText().toString());
        map.put("recedeRemarks", editRefundsRemark.getText().toString());
        map.put("refundsImgs", imgIds);
        RxRequestApi.getInstance().getApiService().refunds(map)
                .compose(this.<ResponseBody>applySchedulers())
                .subscribe(new Action1<ResponseBody>() {
                    @Override
                    public void call(ResponseBody body) {
                        Tst.showToast(body.desc);
                        if (body.isSuccess1()) {
                            RxBus.get().post(BusAction.REFRESH_MY_ORDER, "");
                            RxBus.get().post(BusAction.SUBMIT_REFUND_SUCCESS, "");
                            startActivityAnimGeneral(ApplyRefundDetailsActivity.class, map);
                            finshActivity();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Tst.showToast(throwable.getMessage());
                    }
                });
    }


    @Override
    public String setStatisticsTitle() {
        return "申请退款";
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_recede_reason:
                chooseRefundsReason();
                break;
            case R.id.release:
                uploadService();
                break;
        }
    }

    private void chooseRefundsReason() {
        RxRequestApi.getInstance().getApiService().getRefundsReasons()
                .compose(this.<ResponseBody<RefundReasons>>applySchedulers())
                .subscribe(new Action1<ResponseBody<RefundReasons>>() {
                    @Override
                    public void call(ResponseBody<RefundReasons> refundReasonsResponseBody) {
                        if (refundReasonsResponseBody.isSuccess1()) {
                            Intent intent = new Intent(ApplyRefundActivity.this, ChooseReasonActivityDialog.class);
                            startActivityForResult(intent, REQEST_CODE_REASON);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Tst.showToast(throwable.getMessage());
                    }
                });

    }

    private void uploadService() {
        if (TextUtils.equals(tvRecedeReason.getText().toString(), "请选择")) {
            Tst.showToast("请填写退款原因 ");
            return;
        }
        showProgress();
        imgIds.clear();
        subscription = Observable.from(mAddPhotoView.getPhotos())
                .map(new Func1<Pic, File>() {
                    @Override
                    public File call(Pic pic) {
                        File file = new File(pic.url);
                        return Luban.with(ApplyRefundActivity.this).load(file).get();
                    }
                }).flatMap(new Func1<File, Observable<ResponseBody<Pic>>>() {
                    @Override
                    public Observable<ResponseBody<Pic>> call(File file) {
                        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
                        return RxUploadApi.getInstance().getApiService().uploadImage(body);
                    }
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }).onErrorResumeNext(new Func1<Throwable, Observable<? extends ResponseBody<Pic>>>() {
                    @Override
                    public Observable<? extends ResponseBody<Pic>> call(Throwable throwable) {
                        return Observable.empty();
                    }
                }).subscribe(new Action1<ResponseBody<Pic>>() {
                    @Override
                    public void call(ResponseBody<Pic> body) {
                        if (body.isSuccess() && body.data != null) {
                            imgIds.add(body.data.id);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        hideProgress();
                        Tst.showToast("发布失败");
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        if (mAddPhotoView.getPhotos().size() > 0 && imgIds.size() == 0) {
                            hideProgress();
                            Tst.showToast("发布失败");
                            return;
                        }
                        hideProgress();
                        submitRefunds();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mAddPhotoView.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        if (requestCode == REQUST_CODE_PHOTO) {
            if ((List<Pic>) data.getSerializableExtra("photos") != null) {
                mAddPhotoView.setNewData((List<Pic>) data.getSerializableExtra("photos"));
            }
        } else if (requestCode == REQEST_CODE_REASON) {
            if (!TextUtils.isEmpty(data.getStringExtra("reasons"))) {
                tvRecedeReason.setText(data.getStringExtra("reasons"));
            }
        }

    }

    @Subscribe(tags = {@Tag(BusAction.ACTION_DELETE_PIC)})
    public void deleteSelectPic(Pic pic) {
        mAddPhotoView.removeItem(pic);
    }

}
