package com.knms.activity.login;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.reflect.TypeToken;
import com.knms.activity.FindPasswordActivity;
import com.knms.activity.base.HeadBaseActivity;
import com.knms.app.KnmsApp;
import com.knms.bean.ResponseBody;
import com.knms.net.HttpConstant;
import com.knms.net.ReqApi;
import com.knms.net.RxRequestApi;
import com.knms.oncall.AsyncHttpCallBack;
import com.knms.util.CommonUtils;
import com.knms.util.ConstantObj;
import com.knms.util.DialogHelper;
import com.knms.util.MD5Util;
import com.knms.util.Tst;
import com.knms.android.R;
import com.knms.view.TimerButton;

import android.graphics.Paint;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import rx.functions.Action1;

import static android.R.attr.phoneNumber;

/**
 * 注册第一步，验证码验证
 */
public class RegisterActivity extends HeadBaseActivity implements OnClickListener, View.OnFocusChangeListener {

    private ImageView mCancelOne, mCancelTwo;
    private EditText mPhone, mVerificationCode;
    private TextView mNextStep, mAgreement;
    private TimerButton mGetVerificationCode;
    private static final int GET_CODE = 11;
    private static final int NEXT_STEP = 12;

    private String mPhoneNumeber = "";
    private String mCode = "";

    @Override
    protected int layoutResID() {
        return R.layout.activity_register;
    }

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("注册");
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
        mNextStep.setOnClickListener(this);
        mGetVerificationCode.setOnClickListener(this);
        mAgreement.setOnClickListener(this);
        mAgreement.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);

        mCancelOne.setOnClickListener(this);
        mCancelTwo.setOnClickListener(this);
        mPhone.addTextChangedListener(mWatcherOne);
        mVerificationCode.addTextChangedListener(mWatcherTwo);
        mPhone.setOnFocusChangeListener(this);
        mVerificationCode.setOnFocusChangeListener(this);

    }

    @Override
    public String setStatisticsTitle() {
        return "注册第一步";
    }

    @Override
    public void onClick(View v) {
        mPhoneNumeber = mPhone.getText().toString().trim();
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

                if (!CommonUtils.isMobileNO(mPhoneNumeber)) {
                    Toast.makeText(this, "请输入正确的手机号码", Toast.LENGTH_LONG).show();
                    break;
                }
                if (CommonUtils.isMobileNO(mPhoneNumeber)) {
                    showProgress();
                    RxRequestApi.getInstance().getApiService().sendMsgCheck(mPhoneNumeber, ConstantObj.SEND_TYPE_REGISTER, MD5Util.getMD5String(mPhoneNumeber))
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
                if (!CommonUtils.isMobileNO(mPhoneNumeber) || mCode.length() != 6) {
                    Tst.showToast("请输入正确的手机号和验证码");
                    return;
                }
                HashMap<String, Object> hashMap = new HashMap<String, Object>();
                hashMap.put("username", mPhoneNumeber);
                hashMap.put("sendType", "registerMsg");
                hashMap.put("checkCode", mCode);
                showProgress();
                ReqApi.getInstance().postMethod(HttpConstant.verifyMsg, hashMap, new AsyncHttpCallBack() {
                    @Override
                    public void onSuccess(ResponseBody body) {
                        hideProgress();
                        if (body.isSuccess()) {
                            HashMap<String, Object> parmas = new HashMap<String, Object>();
                            parmas.put("phone", mPhoneNumeber);
                            startActivityAnimGeneral(FinishRegisterActivity.class, parmas);
                            KnmsApp.getInstance().finishActivity(RegisterActivity.this);
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
            updateLogBtnBg();
            if (s.length() == 0) {
                mCancelOne.setVisibility(View.GONE);// 隐藏
            } else {
                mCancelOne.setVisibility(View.VISIBLE);// 显示
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

    private TextWatcher mWatcherTwo = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int count,
                                  int after) {
            updateLogBtnBg();
            if (s.length() == 0) {
                mCancelTwo.setVisibility(View.GONE);// 隐藏
            } else {
                mCancelTwo.setVisibility(View.VISIBLE);// 显示
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

    private void updateLogBtnBg() {
        mPhoneNumeber = mPhone.getText().toString().trim();
        mCode = mVerificationCode.getText().toString().trim();
        if (CommonUtils.isMobileNO(mPhoneNumeber) && mCode.length() == 6) {
            mNextStep.setBackgroundResource(R.drawable.bg_rectangle_btn);
            mNextStep.setTextColor(getResources().getColor(R.color.color_black_333333));
        } else {
            mNextStep.setBackgroundResource(R.drawable.bg_rectangle_gray);
            mNextStep.setTextColor(getResources().getColor(R.color.color_gray_999999));
        }
        if (!mGetVerificationCode.isEnabled()) return;
        if (CommonUtils.isMobileNO(mPhoneNumeber)) {
            mGetVerificationCode.setBackgroundResource(R.drawable.sign_59);
            mGetVerificationCode.setTextColor(getResources().getColor(
                    R.color.button_text_on));
        } else {
            mGetVerificationCode.setBackgroundResource(R.drawable.sign_57);
            mGetVerificationCode.setTextColor(getResources().getColor(
                    R.color.color_gray_999999));
        }
    }

    private void showDialog() {
        DialogHelper.showAlertDialog(this, R.layout.dialog_register_account_there, new DialogHelper.OnEventListener() {
            @Override
            public void eventListener(View parentView, Object window) {
                TextView tvQuickLogin = (TextView) parentView.findViewById(R.id.quick_login);
                TextView tvForgotPwd = (TextView) parentView.findViewById(R.id.forgot_pwd);
                tvForgotPwd.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Map<String, Object> param = new HashMap<>();
                        param.put("tel", CommonUtils.isMobileNO(mPhoneNumeber) ? mPhoneNumeber : "");
                        startActivityAnimGeneral(FindPasswordActivity.class, param);
                        finshActivity();
                    }
                });
                tvQuickLogin.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivityAnimGeneral(FasterLoginActivity.class, null);
                        KnmsApp.getInstance().finishActivity(AccountLoginActivity.class);
                        finshActivity();
                    }
                });
            }
        });
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        if (view.getId() == R.id.edit_phone) {
            mCancelOne.setVisibility(b ? View.VISIBLE : View.GONE);
        } else if (view.getId() == R.id.verification_code) {
            mCancelTwo.setVisibility(b ? View.VISIBLE : View.GONE);
        }
    }

}
