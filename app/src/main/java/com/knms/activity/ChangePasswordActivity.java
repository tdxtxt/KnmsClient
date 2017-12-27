package com.knms.activity;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import com.google.gson.reflect.TypeToken;
import com.knms.activity.base.HeadBaseActivity;
import com.knms.app.KnmsApp;
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

import rx.functions.Action1;

public class ChangePasswordActivity extends HeadBaseActivity implements OnClickListener, View.OnFocusChangeListener {

    private ImageView mCancelOne, mCancelTwo;
    private EditText mVerificationCode;
    private TextView mPhone;
    private TextView  mNextStep;
    private TimerButton mGetVerificationCode;

    private String phoneNumber;
    private String mCode = "";


    @Override
    protected int layoutResID() {
        return R.layout.activity_change_password;
    }

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("修改密码");
    }

    @Override
    protected void initView() {
        mCancelOne = (ImageView) findViewById(R.id.cancel_one);
        mCancelTwo = (ImageView) findViewById(R.id.cancel_two);
        mPhone = (TextView) findViewById(R.id.phone);
        mVerificationCode = (EditText) findViewById(R.id.verification_code);
        mGetVerificationCode = (TimerButton) findViewById(R.id.get_verification_code);
        mNextStep = (TextView) findViewById(R.id.next_step);
    }

    @Override
    protected void initData() {
        mCancelOne.setOnClickListener(this);
        mCancelTwo.setOnClickListener(this);
        mNextStep.setOnClickListener(this);
        mGetVerificationCode.setOnClickListener(this);
        mVerificationCode.setOnFocusChangeListener(this);
        mVerificationCode.addTextChangedListener(mWatcherTwo);
        if (SPUtils.isLogin()) {
            User user = SPUtils.getUser();
            updateView(user);//刷新界面
        }
    }

    @Override
    public String setStatisticsTitle() {
        return "修改密码第一步";
    }

    private void updateView(User user) {
        phoneNumber = user.mobile;
        mPhone.setText(CommonUtils.phoneNumberFormat(phoneNumber));
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
                    RxRequestApi.getInstance().getApiService().sendMsgCheck(phoneNumber, ConstantObj.SEND_TYPE_UPDATE_PSW, MD5Util.getMD5String(phoneNumber))
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
                if (TextUtils.isEmpty(phoneNumber) && !CommonUtils.isMobileNO(phoneNumber))
                    Tst.showToast("请输入正确的手机号");
                else if (TextUtils.isEmpty(mCode)) {
                    Tst.showToast("请输入验证码");
                } else {
                    HashMap<String, Object> params = new HashMap<String, Object>();
                    params.put("username", phoneNumber);
                    params.put("checkCode", mCode);
                    params.put("sendType", "updatePasMsg");//此处用的是找回密码的参数，修改密码未设置该参数
                    showProgress();
                    ReqApi.getInstance().postMethod(HttpConstant.verifyMsg, params, new AsyncHttpCallBack() {
                        @Override
                        public void onSuccess(ResponseBody body) {
                            hideProgress();
                            if (body.isSuccess()) {//短信验证成功
                                Map<String, Object> params = new HashMap<String, Object>();
                                params.put("username", phoneNumber);
                                startActivityAnimGeneral(ChangePasswordEndActivity.class, params);
                                KnmsApp.getInstance().finishActivity(ChangePasswordActivity.this);
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

    TextWatcher mWatcherTwo = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int count,
                                  int after) {
            if (s.length() == 0) {
                mCancelTwo.setVisibility(View.GONE);//隐藏
            } else {
                mCancelTwo.setVisibility(View.VISIBLE);//显示
            }
            mCode = mVerificationCode.getText().toString();
            if (mCode.length() == 6) {
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

    @Override
    public void onFocusChange(View view, boolean b) {
        if (view.getId() == R.id.verification_code) {
            if (!mCode.equals(""))
                mCancelTwo.setVisibility(b ? View.VISIBLE : View.GONE);
        }
    }

}
