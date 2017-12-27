package com.knms.activity;

import java.lang.reflect.Type;
import java.util.HashMap;

import com.google.gson.reflect.TypeToken;
import com.knms.activity.base.HeadBaseActivity;
import com.knms.bean.ResponseBody;
import com.knms.bean.user.User;
import com.knms.net.HttpConstant;
import com.knms.net.ReqApi;
import com.knms.net.RxRequestApi;
import com.knms.oncall.AsyncHttpCallBack;
import com.knms.util.CommonUtils;
import com.knms.util.ConstantObj;
import com.knms.util.MD5Util;
import com.knms.util.SPUtils;
import com.knms.util.Tst;
import com.knms.android.R;
import com.knms.view.TimerButton;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import rx.Subscription;
import rx.functions.Action1;

public class ChangePhoneActivity extends HeadBaseActivity implements OnClickListener {

    private ImageView mCancel;
    private TextView mNowPhone;
    private EditText mVerificationCode;
    private TextView mNextStep;
    private TimerButton mGetVerificationCode;

    private String phoneNumber;
    private String mCode;
    private int cont = 60;

    private Subscription subscription;

    @Override
    protected int layoutResID() {
        return R.layout.activity_change_phone;
    }

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("修改绑定手机");
    }

    @Override
    protected void initView() {
        mCancel = (ImageView) findViewById(R.id.cancel);
        mNowPhone = (TextView) findViewById(R.id.now_phone);
        mVerificationCode = (EditText) findViewById(R.id.verification_code);
        mGetVerificationCode = (TimerButton) findViewById(R.id.get_verification_code);
        mNextStep = (TextView) findViewById(R.id.next_step);

    }

    @Override
    protected void initData() {
        mCancel.setOnClickListener(this);
        mGetVerificationCode.setOnClickListener(this);
        mNextStep.setOnClickListener(this);
        mVerificationCode.addTextChangedListener(mWatcher);
        if (SPUtils.isLogin()) {
            User user = SPUtils.getUser();
            updateView(user);//刷新界面
        }
    }

    @Override
    public String setStatisticsTitle() {
        return null;
    }

    private void updateView(User user) {
        phoneNumber = user.mobile;
        mNowPhone.setText(CommonUtils.phoneNumberFormat(phoneNumber));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel:
                mVerificationCode.setText(null);
                mCancel.setVisibility(View.GONE);//隐藏
                break;
            case R.id.get_verification_code:
                if (CommonUtils.isMobileNO(phoneNumber)) {
                    showProgress();
                    RxRequestApi.getInstance().getApiService().sendMsgCheck(phoneNumber, ConstantObj.SEND_TYPE_UPDATE_MOBILE, MD5Util.getMD5String(phoneNumber))
                            .compose(this.<ResponseBody>applySchedulers())
                            .subscribe(new Action1<ResponseBody>() {
                                @Override
                                public void call(ResponseBody body) {
                                    hideProgress();
                                    if (body.isSuccess()) {
                                        mGetVerificationCode.startTimer();
                                    } else {
                                        Tst.showToast(body.desc);
                                    }
                                }
                            }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                    hideProgress();
                                    Tst.showToast(throwable.toString());
                                }
                            });
                }
                break;
            case R.id.next_step:
                mCode = mVerificationCode.getText().toString();
                if (!TextUtils.isEmpty(mCode)) {
                    HashMap<String, Object> params = new HashMap<String, Object>();
                    params.put("username", phoneNumber);
                    params.put("checkCode", mCode);
                    showProgress();
                    ReqApi.getInstance().postMethod(HttpConstant.verifyMsg, params, new AsyncHttpCallBack() {
                        @Override
                        public void onSuccess(ResponseBody body) {
                            hideProgress();
                            if (body.isSuccess()) {//短信验证成功
                                startActivityAnimGeneral(ChangeBindPhoneActivity.class, null);
                            }
                            Tst.showToast(body.desc);
                        }

                        @Override
                        public void onFailure(String msg) {
                            Tst.showToast(msg);
                            hideProgress();
                        }

                        @Override
                        public Type setType() {
                            return new TypeToken<ResponseBody>() {
                            }.getType();
                        }
                    });
                }
                break;
        }

    }

    private TextWatcher mWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int count,
                                  int after) {
            if (s.length() == 0) {
                mCancel.setVisibility(View.GONE);//隐藏
            } else {
                mCancel.setVisibility(View.VISIBLE);//显示
            }
            if (s.length() == 6) {
                mNextStep.setBackgroundResource(R.drawable.bg_rectangle_btn);
                mNextStep.setTextColor(getResources().getColor(R.color.color_black_333333));
            } else {
                mNextStep.setBackgroundResource(R.drawable.bg_rectangle_gray);
                mNextStep.setTextColor(getResources().getColor(R.color.color_gray_999999));
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
}
