package com.knms.activity.login;

import java.lang.reflect.Type;
import java.util.HashMap;

import com.google.gson.reflect.TypeToken;
import com.knms.activity.base.HeadBaseActivity;
import com.knms.bean.ResponseBody;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.net.HttpConstant;
import com.knms.net.ReqApi;
import com.knms.oncall.AsyncHttpCallBack;
import com.knms.util.CommonUtils;
import com.knms.util.SPUtils;
import com.knms.util.Tst;
import com.knms.android.R;

import android.content.Intent;
import android.graphics.Paint;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 注册第二步，设置密码注册成功
 */
public class FinishRegisterActivity extends HeadBaseActivity implements
        OnClickListener {

    private ImageView mCancel, mVisible;
    private EditText mSetPassword;
    private TextView mRegistration, mAgreement;

    private static final int REGISTER = 11;

    private boolean isCheak = false;

    String phone;

    @Override
    protected void getParmas(Intent intent) {
        phone = intent.getStringExtra("phone");
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_finish_register;
    }

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("注册");
    }

    @Override
    protected void initView() {
        mCancel = (ImageView) findViewById(R.id.cancel);
        mVisible = (ImageView) findViewById(R.id.visible);
        mSetPassword = (EditText) findViewById(R.id.set_password);
        mRegistration = (TextView) findViewById(R.id.registration);
        mAgreement = (TextView) findViewById(R.id.agreement);
    }

    @Override
    protected void initData() {
        mCancel.setOnClickListener(this);
        mVisible.setOnClickListener(this);
        mRegistration.setOnClickListener(this);
        mAgreement.setOnClickListener(this);
        mAgreement.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);

        mCancel.setVisibility(View.GONE);// 隐藏

        mSetPassword.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int count,
                                      int after) {
                if (s.length() == 0) {
                    mCancel.setVisibility(View.GONE);// 隐藏
                } else {
                    mCancel.setVisibility(View.VISIBLE);// 显示
                }
                if (s.length() >= 6 && s.length() <= 32 && CommonUtils.VerificationPwd(s + "")) {
                    mRegistration.setBackgroundResource(R.drawable.bg_rectangle_btn);
                    mRegistration.setTextColor(getResources().getColor(R.color.color_black_333333));
                } else {
                    mRegistration.setBackgroundResource(R.drawable.bg_rectangle_gray);
                    mRegistration.setTextColor(getResources().getColor(R.color.color_gray_999999));
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub
                mCancel.setVisibility(View.GONE);// 隐藏
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }
        });
    }

    @Override
    public String setStatisticsTitle() {
        return "注册设置密码";
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel:
                mSetPassword.setText(null);
                mCancel.setVisibility(View.GONE);// 隐藏
                break;
            case R.id.visible:
                if (isCheak) {
                    // 设置EditText文本为显示
                    mSetPassword
                            .setTransformationMethod(HideReturnsTransformationMethod
                                    .getInstance());
                    mVisible.setImageResource(R.drawable.sign_03);
                } else {
                    // 设置EditText文本为隐藏
                    mSetPassword
                            .setTransformationMethod(PasswordTransformationMethod
                                    .getInstance());
                    mVisible.setImageResource(R.drawable.sign_75);
                }
                isCheak = !isCheak;
                break;
            case R.id.registration:
                String mPassword = mSetPassword.getText().toString().trim();
                if (mPassword.length() < 6 || mPassword.length() > 32 || !CommonUtils.VerificationPwd(mPassword)) {
                    Toast.makeText(this, "请设置为6-32位字母数字组合", Toast.LENGTH_SHORT).show();
                    break;
                } else {

                    HashMap<String, Object> hashMap = new HashMap<String, Object>();
                    hashMap.put("username", phone);
                    hashMap.put("password", mPassword);
                    showProgress();
                    ReqApi.getInstance().postMethod(HttpConstant.register, hashMap, new AsyncHttpCallBack() {
                        @Override
                        public void onSuccess(ResponseBody body) {
                            if (body.isSuccess()) {//注册成功
                                SPUtils.saveCurrentMoblie(phone);
                                RxBus.get().post(BusAction.CHANGE_PHONE,SPUtils.getCurrentMobile());
                                finshActivity();
                            }
                            Tst.showToast(body.desc);
                            hideProgress();
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

                // Toast.makeText(this, "开发中...", Toast.LENGTH_LONG).show();
                break;
        }

    }


}
