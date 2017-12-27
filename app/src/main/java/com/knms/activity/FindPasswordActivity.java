package com.knms.activity;

import java.lang.reflect.Type;
import java.util.HashMap;

import com.google.gson.reflect.TypeToken;
import com.knms.activity.base.HeadBaseActivity;
import com.knms.bean.ResponseBody;
import com.knms.net.HttpConstant;
import com.knms.net.ReqApi;
import com.knms.net.RxRequestApi;
import com.knms.oncall.AsyncHttpCallBack;
import com.knms.util.CommonUtils;
import com.knms.util.ConstantObj;
import com.knms.util.MD5Util;
import com.knms.util.Tst;
import com.knms.android.R;
import com.knms.view.TimerButton;

import android.content.Intent;
import android.graphics.Paint;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import rx.functions.Action1;

import static android.R.attr.phoneNumber;

public class FindPasswordActivity extends HeadBaseActivity implements OnClickListener, View.OnFocusChangeListener {

    private ImageView mCancelOne, mCancelTwo;
    private EditText mPhone, mVerificationCode;
    private TextView mNextStep, mAgreement;
    private TimerButton mGetVerificationCode;
    String mPhoneNumber, mCode;

    @Override
    protected void getParmas(Intent intent) {
        super.getParmas(intent);
        mPhoneNumber = intent.getStringExtra("tel");
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_find_password;
    }

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("找回密码");
    }

    @Override
    protected void initView() {
        mCancelOne = (ImageView) findViewById(R.id.cancel_one);
        mCancelTwo = (ImageView) findViewById(R.id.cancel_two);
        mPhone = (EditText) findViewById(R.id.edit_phone);
        mVerificationCode = (EditText) findViewById(R.id.verification_code);
        mGetVerificationCode = (TimerButton) findViewById(R.id.get_verification_code);
        mNextStep = (TextView) findViewById(R.id.next_step);
        mAgreement = (TextView) findViewById(R.id.agreement);
    }

    @Override
    protected void initData() {

        mPhone.setText(mPhoneNumber);
        mGetVerificationCode.setOnClickListener(this);
        mNextStep.setOnClickListener(this);
        mAgreement.setOnClickListener(this);
        mAgreement.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);

        mCancelOne.setOnClickListener(this);
        mCancelTwo.setOnClickListener(this);
        mPhone.setOnFocusChangeListener(this);
        mVerificationCode.setOnFocusChangeListener(this);

        mPhone.addTextChangedListener(mWatcherOne);
        mVerificationCode.addTextChangedListener(mWatcherTwo);
    }

    @Override
    public String setStatisticsTitle() {
        return "找回密码第一步";
    }

    @Override
    public void onClick(View v) {
        mPhoneNumber = mPhone.getText().toString().trim();
        mCode = mVerificationCode.getText().toString().trim();
        switch (v.getId()) {
            case R.id.cancel_one:
                mPhone.setText(null);
                mCancelOne.setVisibility(View.GONE);// 隐藏
                break;
            case R.id.cancel_two:
                mVerificationCode.setText(null);
                mCancelTwo.setVisibility(View.GONE);// 隐藏
                break;
            case R.id.get_verification_code:
                // 还要判断用户是否已注册，如未注册 弹窗提示
                if (!CommonUtils.isMobileNO(mPhoneNumber)) {
                    Toast.makeText(this, "用户手机格式不正确", Toast.LENGTH_LONG).show();
                    break;
                }
                if (CommonUtils.isMobileNO(mPhoneNumber)) {
                    showProgress();
                    RxRequestApi.getInstance().getApiService().sendMsgCheck(mPhoneNumber, ConstantObj.SEND_TYPE_UPDATE_PSW, MD5Util.getMD5String(mPhoneNumber))
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
                    break;
                }
                break;
            case R.id.next_step:
                if (!CommonUtils.isMobileNO(mPhoneNumber)) {
                    Tst.showToast("手机号格式不正确");
                    return;
                } else if (TextUtils.isEmpty(mPhoneNumber)) {
                    Tst.showToast("手机号不可为空");
                    return;
                }
                if (TextUtils.isEmpty(mCode)) {
                    Tst.showToast("验证码不可为空");
                    return;
                } else if (mCode.length() != 6) {
                    Tst.showToast("验证码错误");
                    return;
                }
                HashMap<String, Object> hashMap = new HashMap<String, Object>();
                hashMap.put("username", mPhoneNumber);
                hashMap.put("sendType", "updatePasMsg");
                hashMap.put("checkCode", mCode);
                ReqApi.getInstance().postMethod(HttpConstant.verifyMsg, hashMap, new AsyncHttpCallBack() {
                    @Override
                    public void onSuccess(ResponseBody body) {
                        hideProgress();
                        if (body.isSuccess()) {
                            HashMap<String, Object> parmas = new HashMap<String, Object>();
                            parmas.put("phone", mPhoneNumber);
                            startActivityAnimGeneral(FindPasswordEndActivity.class, parmas);
                            finshActivity();
                        } else
                            Tst.showToast(body.desc);
                    }

                    @Override
                    public void onFailure(String msg) {
                        hideProgress();
                        Tst.showToast(msg);
                    }

                    @Override
                    public Type setType() {
                        return new TypeToken<ResponseBody>() {
                        }.getType();
                    }
                });

                break;
        }

    }

    private TextWatcher mWatcherOne = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int count,
                                  int after) {
            if (s.length() == 0) {
                mCancelOne.setVisibility(View.GONE);// 隐藏
            } else {
                mCancelOne.setVisibility(View.VISIBLE);// 显示
            }
            verificationData();
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private TextWatcher mWatcherTwo = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int count,
                                  int after) {
            if (s.length() == 0) {
                mCancelTwo.setVisibility(View.GONE);// 隐藏
            } else {
                mCancelTwo.setVisibility(View.VISIBLE);// 显示
            }
            verificationData();
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private void verificationData() {
        mPhoneNumber = mPhone.getText().toString().trim();
        mCode = mVerificationCode.getText().toString().trim();
        if (CommonUtils.isMobileNO(mPhoneNumber) && mCode.length() == 6) {
            mNextStep.setBackgroundResource(R.drawable.bg_rectangle_btn);
            mNextStep.setTextColor(getResources().getColor(R.color.color_black_333333));

        } else {
            mNextStep.setBackgroundResource(R.drawable.bg_rectangle_gray);
            mNextStep.setTextColor(getResources().getColor(R.color.color_gray_999999));
        }
        if (!mGetVerificationCode.isEnabled()) return;
        if (CommonUtils.isMobileNO(mPhoneNumber)) {
            mGetVerificationCode.setBackgroundResource(R.drawable.sign_59);
            mGetVerificationCode.setTextColor(getResources().getColor(
                    R.color.button_text_on));
        } else {
            mGetVerificationCode.setBackgroundResource(R.drawable.sign_57);
            mGetVerificationCode.setTextColor(getResources().getColor(
                    R.color.color_gray_999999));
        }
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        if (CommonUtils.isMobileNO(mPhoneNumber)) {
            mGetVerificationCode.setBackgroundResource(R.drawable.sign_59);
            mGetVerificationCode.setTextColor(getResources().getColor(
                    R.color.button_text_on));
        }
        if (view.getId() == R.id.edit_phone) {
            if (!TextUtils.isEmpty(mPhoneNumber))
                mCancelOne.setVisibility(b ? View.VISIBLE : View.GONE);
        } else if (view.getId() == R.id.verification_code) {
            if (!TextUtils.isEmpty(mCode))
                mCancelTwo.setVisibility(b ? View.VISIBLE : View.GONE);
        }
    }
}
