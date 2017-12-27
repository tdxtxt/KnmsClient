package com.knms.activity;

import com.google.gson.reflect.TypeToken;
import com.knms.activity.base.HeadBaseActivity;
import com.knms.bean.ResponseBody;
import com.knms.net.HttpConstant;
import com.knms.net.ReqApi;
import com.knms.oncall.AsyncHttpCallBack;
import com.knms.util.CommonUtils;
import com.knms.util.Tst;
import com.knms.android.R;

import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.reflect.Type;
import java.util.HashMap;

public class ChangePasswordEndActivity extends HeadBaseActivity implements
        OnClickListener, View.OnFocusChangeListener {

    private ImageView mCancelOne, mCancelTwo;
    private EditText mNewPassword, mReconfirmNewPassword;
    private TextView mSave;

    //    private String oldPassword;
    private String newPassword = "";
    private String reconfirmNewPassword = "";
    private String username;

    @Override
    protected void getParmas(Intent intent) {
        username = intent.getStringExtra("username");
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_change_password_end;
    }

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("修改密码");
    }

    @Override
    protected void initView() {
        mCancelOne = (ImageView) findViewById(R.id.cancel_one);
        mCancelTwo = (ImageView) findViewById(R.id.cancel_two);
        mNewPassword = (EditText) findViewById(R.id.new_password);
        mReconfirmNewPassword = (EditText) findViewById(R.id.reconfirm_new_password);
        mSave = (TextView) findViewById(R.id.save);
    }

    @Override
    protected void initData() {
        mCancelOne.setOnClickListener(this);
        mCancelTwo.setOnClickListener(this);
        mSave.setOnClickListener(this);
        mNewPassword.addTextChangedListener(mWatcherOne);
        mReconfirmNewPassword.addTextChangedListener(mWatcherTwo);
        mReconfirmNewPassword.setOnFocusChangeListener(this);
        mNewPassword.setOnFocusChangeListener(this);

    }

    @Override
    public String setStatisticsTitle() {
        return "修改密码第二步";
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel_one:
                mNewPassword.setText(null);
                mCancelOne.setVisibility(View.GONE);//隐藏
                break;
            case R.id.cancel_two:
                mReconfirmNewPassword.setText(null);
                mCancelTwo.setVisibility(View.GONE);//隐藏
                break;
            case R.id.save:
                if (!newPassword.equals(reconfirmNewPassword)) {
                    Tst.showToast("两次密码不一致");
                } else if (!CommonUtils.VerificationPwd(newPassword) || newPassword.length() < 6 || newPassword.length() > 32) {
                    Tst.showToast("新密码请设置为6-32位字母数字组合");
                } else {
                    HashMap<String, Object> params = new HashMap<String, Object>();
                    params.put("username", username);
//                    params.put("oldpassword",oldPassword);
                    params.put("password", newPassword);
                    showProgress();
                    ReqApi.getInstance().postMethod(HttpConstant.updatePassword, params, new AsyncHttpCallBack() {
                        @Override
                        public void onSuccess(ResponseBody body) {
                            hideProgress();
                            if (body.isSuccess()) {//密码修改成功
                                finshActivity();
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

    private TextWatcher mWatcherOne = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int count,
                                  int after) {
            if (s.length() == 0) {
                mCancelOne.setVisibility(View.GONE);//隐藏
            } else {
                mCancelOne.setVisibility(View.VISIBLE);//显示
            }
            updateSavaBackgroud();
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
                mCancelTwo.setVisibility(View.GONE);//隐藏
            } else {
                mCancelTwo.setVisibility(View.VISIBLE);//显示
            }
            updateSavaBackgroud();
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private void updateSavaBackgroud() {
        newPassword = mNewPassword.getText().toString();
        reconfirmNewPassword = mReconfirmNewPassword.getText().toString();
        if (newPassword.equals(reconfirmNewPassword) && CommonUtils.VerificationPwd(newPassword) && newPassword.length() >= 6 && newPassword.length() <= 32) {
            mSave.setBackgroundResource(R.drawable.bg_rectangle_btn);
            mSave.setTextColor(getResources().getColor(R.color.color_black_333333));
        } else {
            mSave.setBackgroundResource(R.drawable.bg_rectangle_gray);
            mSave.setTextColor(getResources().getColor(R.color.color_gray_999999));
        }
    }


    @Override
    public void onFocusChange(View view, boolean b) {
        if (view.getId() == R.id.reconfirm_new_password) {
            if (!TextUtils.isEmpty(reconfirmNewPassword))
                mCancelTwo.setVisibility(b ? View.VISIBLE : View.GONE);
        } else if (view.getId() == R.id.new_password)
        {
            if (!TextUtils.isEmpty(newPassword))
                mCancelOne.setVisibility(b ? View.VISIBLE : View.GONE);
        }
    }
}
