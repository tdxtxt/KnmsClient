package com.knms.activity;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.knms.activity.base.HeadBaseActivity;
import com.knms.activity.login.FasterLoginActivity;
import com.knms.activity.mine.PersonalInformationActivity;
import com.knms.android.R;
import com.knms.app.KnmsApp;
import com.knms.bean.ResponseBody;
import com.knms.core.im.IMHelper;
import com.knms.net.HttpConstant;
import com.knms.net.ReqApi;
import com.knms.net.RxRequestApi;
import com.knms.oncall.AsyncHttpCallBack;
import com.knms.util.CommonUtils;
import com.knms.util.ConstantObj;
import com.knms.util.MD5Util;
import com.knms.util.SPUtils;
import com.knms.util.Tst;
import com.knms.view.TimerButton;

import java.lang.reflect.Type;
import java.util.HashMap;

import rx.functions.Action1;

public class ChangeBindPhoneActivity extends HeadBaseActivity implements
        OnClickListener, View.OnFocusChangeListener {

    private ImageView mCancelOne, mCancelTwo;
    private EditText mPhone, mVerificationCode;
    private TextView  mBind;
    private TimerButton mGetVerificationCode;
    private String phoneNumber;
    private String mCode;

    @Override
    protected int layoutResID() {
        return R.layout.activity_change_bind_phone;
    }

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("修改绑定手机");
    }

    @Override
    protected void initView() {
        mCancelOne = (ImageView) findViewById(R.id.cancel_one);
        mCancelTwo = (ImageView) findViewById(R.id.cancel_two);
        mPhone = (EditText) findViewById(R.id.phone);
        mVerificationCode = (EditText) findViewById(R.id.verification_code);
        mGetVerificationCode = (TimerButton) findViewById(R.id.get_verification_code);
        mBind = (TextView) findViewById(R.id.bind);
    }

    @Override
    protected void initData() {
        mCancelOne.setOnClickListener(this);
        mCancelTwo.setOnClickListener(this);
        mGetVerificationCode.setOnClickListener(this);
        mBind.setOnClickListener(this);
        mCancelOne.setVisibility(View.GONE);//隐藏
        mCancelTwo.setVisibility(View.GONE);//隐藏
        mPhone.addTextChangedListener(mWatcherOne);
        mVerificationCode.addTextChangedListener(mWatcherTwo);
        mPhone.setOnFocusChangeListener(this);
        mVerificationCode.setOnFocusChangeListener(this);
    }

    @Override
    public String setStatisticsTitle() {
        return "绑定手机";
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel_one:
                mPhone.setText(null);
                mCancelOne.setVisibility(View.GONE);//隐藏
                break;
            case R.id.cancel_two:
                mVerificationCode.setText(null);
                mCancelTwo.setVisibility(View.GONE);//隐藏
                break;
            case R.id.get_verification_code:
                if (CommonUtils.isMobileNO(phoneNumber)) {
                    showProgress();
                    RxRequestApi.getInstance().getApiService().sendMsgCheck(phoneNumber, ConstantObj.SEND_TYPE_NEW_MOBILE, MD5Util.getMD5String(phoneNumber))
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
            case R.id.bind:
                if (!CommonUtils.isMobileNO(phoneNumber) || TextUtils.isEmpty(phoneNumber)) {
                    Tst.showToast("请输入正确的手机号码");
                } else if (TextUtils.isEmpty(mCode)) {
                    Tst.showToast("请输入验证码");
                } else if (mCode.length() != 6) {
                    Tst.showToast("验证码错误");
                } else {
                    HashMap<String, Object> params = new HashMap<String, Object>();
                    params.put("newPhone", phoneNumber);
                    params.put("checkCode", mCode);
                    showProgress();
                    ReqApi.getInstance().postMethod(HttpConstant.updateMobile, params, new AsyncHttpCallBack() {
                        @Override
                        public void onSuccess(ResponseBody body) {
                            hideProgress();
                            if (body.isSuccess()) {//手机绑定成功
                                CommonUtils.logout();
                                KnmsApp.getInstance().finishActivity(PersonalInformationActivity.class);
                                KnmsApp.getInstance().finishActivity(ChangePhoneActivity.class);
                                finshActivity();
                                startActivityAnimGeneral(FasterLoginActivity.class, null);
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

    TextWatcher mWatcherOne = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int count,
                                  int after) {
            if (s.length() == 0) {
                mCancelOne.setVisibility(View.GONE);
            } else {
                mCancelOne.setVisibility(View.VISIBLE);
            }
            updateBindBackgroud();

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    TextWatcher mWatcherTwo = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int count,
                                  int after) {
            if (s.length() == 0) {
                mCancelTwo.setVisibility(View.GONE);
            } else {
                mCancelTwo.setVisibility(View.VISIBLE);
            }
            updateBindBackgroud();
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private void updateBindBackgroud() {
        phoneNumber = mPhone.getText().toString();
        mCode = mVerificationCode.getText().toString();
        if (CommonUtils.isMobileNO(phoneNumber) && mCode.length() == 6) {
            mBind.setBackgroundResource(R.drawable.bg_rectangle_btn);
            mBind.setTextColor(getResources().getColor(R.color.color_black_333333));
        } else {
            mBind.setBackgroundResource(R.drawable.bg_rectangle_gray);
            mBind.setTextColor(getResources().getColor(R.color.color_gray_999999));
        }
        if(!mGetVerificationCode.isEnabled())return;
        if (CommonUtils.isMobileNO(phoneNumber)) {
            mGetVerificationCode
                    .setBackgroundResource(R.drawable.sign_59);
            mGetVerificationCode.setTextColor(getResources().getColor(
                    R.color.button_text_on));
        } else {
            mGetVerificationCode
                    .setBackgroundResource(R.drawable.sign_57);
            mGetVerificationCode.setTextColor(getResources().getColor(
                    R.color.color_gray_999999));
        }
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        if (view.getId() == R.id.verification_code) {
            mCancelTwo.setVisibility(b ? View.VISIBLE : View.GONE);
        } else {
            mCancelOne.setVisibility(b ? View.VISIBLE : View.GONE);
        }
    }
}
