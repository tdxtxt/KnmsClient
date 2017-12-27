package com.knms.activity;

import com.google.gson.reflect.TypeToken;
import com.knms.activity.base.BaseActivity;
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
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Type;
import java.util.HashMap;

public class FindPasswordEndActivity extends HeadBaseActivity implements
        OnClickListener, View.OnFocusChangeListener {

    private ImageView mCancelOne, mCancelTwo;
    private EditText mNewPassword, mReconfirmNewPassword;
    private TextView mCommit, mAgreement;
    private String mPassword="", mPasswordAgain="";
    private String phone="";

    @Override
    protected void getParmas(Intent intent) {
        phone = intent.getStringExtra("phone");
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_find_password_end;
    }

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("找回密码");
    }

    @Override
    protected void initView() {
        mCancelOne = (ImageView) findViewById(R.id.cancel_one);
        mCancelTwo = (ImageView) findViewById(R.id.cancel_two);
        mNewPassword = (EditText) findViewById(R.id.new_password);
        mReconfirmNewPassword = (EditText) findViewById(R.id.reconfirm_new_password);
        mCommit = (TextView) findViewById(R.id.commit);
        mAgreement = (TextView) findViewById(R.id.agreement);
    }

    @Override
    protected void initData() {
        mCancelOne.setOnClickListener(this);
        mCancelTwo.setOnClickListener(this);
        mCommit.setOnClickListener(this);
        mAgreement.setOnClickListener(this);
        mReconfirmNewPassword.setOnFocusChangeListener(this);
        mNewPassword.setOnFocusChangeListener(this);
        mAgreement.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        mNewPassword.addTextChangedListener(mWatcherOne);
        mReconfirmNewPassword.addTextChangedListener(mWatcherTwo);

    }

    @Override
    public String setStatisticsTitle() {
        return "找回密码第二步";
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel_one:
                mNewPassword.setText(null);
                mCancelOne.setVisibility(View.GONE);// 隐藏
                break;
            case R.id.cancel_two:
                mReconfirmNewPassword.setText(null);
                mCancelTwo.setVisibility(View.GONE);// 隐藏
                break;
            case R.id.commit:
                if (mPassword.length() < 6 || mPassword.length() > 32 || !CommonUtils.VerificationPwd(mPassword)) {
                    Toast.makeText(this, "请设置6-32位字母数字组合", Toast.LENGTH_LONG).show();
                    break;
                }

                if (!(mPasswordAgain.equals(mPassword))) {
                    Toast.makeText(this, "两次密码不一致", Toast.LENGTH_LONG).show();
                    break;
                }
                HashMap<String, Object> parmas = new HashMap<String, Object>();
                parmas.put("username", phone);
                parmas.put("password", mPassword);
                showProgress();
                ReqApi.getInstance().postMethod(HttpConstant.updatePassword, parmas, new AsyncHttpCallBack() {
                    @Override
                    public void onSuccess(ResponseBody body) {
                        if (body.isSuccess()) {//密码重置成功
                            SPUtils.saveCurrentMoblie(phone);
                            RxBus.get().post(BusAction.CHANGE_PHONE,SPUtils.getCurrentMobile());
                            finshActivity();
                        }
                        Tst.showToast(body.desc);
                        hideProgress();
                    }

                    @Override
                    public void onFailure(String msg) {
                        hideProgress();
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
            updateBtnBackground();
            if (s.length() == 0) {
                mCancelOne.setVisibility(View.GONE);//隐藏
            } else {
                mCancelOne.setVisibility(View.VISIBLE);//显示
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
            updateBtnBackground();
            if (s.length() == 0) {
                mCancelTwo.setVisibility(View.GONE);//隐藏
            } else {
                mCancelTwo.setVisibility(View.VISIBLE);//显示
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
        if (view.getId() == R.id.reconfirm_new_password) {
            if (!TextUtils.isEmpty(mPasswordAgain))
                mCancelTwo.setVisibility(b ? View.VISIBLE : View.GONE);
        } else if (view.getId() == R.id.new_password) {
            if (!TextUtils.isEmpty(mPassword))
                mCancelOne.setVisibility(b ? View.VISIBLE : View.GONE);
        }
    }

    private void updateBtnBackground() {
        mPassword = mNewPassword.getText().toString().trim();
        mPasswordAgain = mReconfirmNewPassword.getText().toString().trim();
        if (mPassword.length() < 6 || mPassword.length() > 32 || !CommonUtils.VerificationPwd(mPassword)) {
            mCommit.setBackgroundResource(R.drawable.bg_rectangle_gray);
            mCommit.setTextColor(getResources().getColor(R.color.color_gray_999999));
        } else if (mPasswordAgain.length() < 6 || mPasswordAgain.length() > 32||!CommonUtils.VerificationPwd(mPasswordAgain)) {
            mCommit.setBackgroundResource(R.drawable.bg_rectangle_gray);
            mCommit.setTextColor(getResources().getColor(R.color.color_gray_999999));
        } else {
            mCommit.setTextColor(getResources().getColor(R.color.color_black_333333));
            mCommit.setBackgroundResource(R.drawable.bg_rectangle_btn);

        }
    }
}
