package com.knms.activity.login;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gson.reflect.TypeToken;
import com.knms.activity.CommWebViewActivity;
import com.knms.activity.FindPasswordActivity;
import com.knms.activity.base.HeadBaseActivity;
import com.knms.android.R;
import com.knms.app.KnmsApp;
import com.knms.bean.ResponseBody;
import com.knms.bean.user.User;
import com.knms.core.im.IMHelper;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.net.HttpConstant;
import com.knms.net.ReqApi;
import com.knms.oncall.AsyncHttpCallBack;
import com.knms.util.CommonUtils;
import com.knms.util.L;
import com.knms.util.SPUtils;
import com.knms.util.SystemInfo;
import com.knms.util.Tst;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.LoginInfo;

import android.content.Context;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;


public class AccountLoginActivity extends HeadBaseActivity implements OnClickListener, View.OnFocusChangeListener {

    private ImageView mCancelOne, mCancelTwo, mVisible;

    private TextView mFasterLogin, mLogin, mForgetPassword, mAgreement;
    private EditText mPhone;
    private EditText mPassword;

    private boolean isCheak = true;
    private String phoneNumber = "", password = "";
    private static final int MSG_SET_ALIAS = 1001;

    @Override
    protected int layoutResID() {
        return R.layout.activity_account_login;
    }

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("登录");
    }

    @Override
    protected void initView() {
        mVisible = (ImageView) findViewById(R.id.img_visible);
        mCancelOne = (ImageView) findViewById(R.id.cancel_one);
        mCancelTwo = (ImageView) findViewById(R.id.cancel_two);
        mFasterLogin = (TextView) findViewById(R.id.faster_login);
        mLogin = (TextView) findViewById(R.id.text_login);
        mForgetPassword = (TextView) findViewById(R.id.forget_password);
        mPhone = (EditText) findViewById(R.id.edit_phone);
        mPassword = (EditText) findViewById(R.id.edit_password);
        mAgreement = (TextView) findViewById(R.id.agreement);
    }

    @Override
    protected void initData() {
        mForgetPassword.setOnClickListener(this);
        mFasterLogin.setOnClickListener(this);
        mFasterLogin.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        mLogin.setOnClickListener(this);
        mCancelOne.setOnClickListener(this);
        mCancelTwo.setOnClickListener(this);
        mCancelOne.setVisibility(View.GONE);// 隐藏
        mCancelTwo.setVisibility(View.GONE);//  隐藏
        mVisible.setOnClickListener(this);
        mAgreement.setOnClickListener(this);
        mAgreement.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        mPhone.addTextChangedListener(mWatcherOne);
        mPassword.addTextChangedListener(mWatcherTwo);
        mPassword.setOnFocusChangeListener(this);
        mPhone.setOnFocusChangeListener(this);
        setRightMenuCallBack(new RightCallBack() {
            @Override
            public void setRightContent(TextView tv, ImageView icon) {
                tv.setText("注册");
                icon.setVisibility(View.GONE);
            }

            @Override
            public void onclick() {
                startActivityAnimGeneral(RegisterActivity.class, null);
            }
        });
        showPhone(SPUtils.getCurrentMobile());
    }

    @Override
    public String setStatisticsTitle() {
        return "账户密码登录";
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_visible:
                if (!isCheak) {
                    mPassword.setTransformationMethod(PasswordTransformationMethod
                            .getInstance());
                    mVisible.setImageResource(R.drawable.sign_75);
                    mPassword.setSelection((mPassword.getText() + "").length());
                    isCheak = true;
                } else {
                    mVisible.setImageResource(R.drawable.sign_03);
                    mPassword.setTransformationMethod(HideReturnsTransformationMethod
                            .getInstance());
                    mPassword.setSelection((mPassword.getText() + "").length());
                    isCheak = false;
                }
                break;
            case R.id.cancel_one:
                mPhone.setText(null);
                mCancelOne.setVisibility(View.GONE);// 隐藏
                break;
            case R.id.cancel_two:
                mPassword.setText(null);
                mCancelTwo.setVisibility(View.GONE);// 隐藏
                break;

            case R.id.faster_login:
                startActivityAnimGeneral(FasterLoginActivity.class, null);
                KnmsApp.getInstance().finishActivity(this);
                break;
            case R.id.text_login:
                hideKeyboard();
                if (TextUtils.isEmpty(phoneNumber)) {
                    Toast.makeText(this, "手机号为空", Toast.LENGTH_LONG).show();
                    break;
                }

                if (!CommonUtils.isMobileNO(phoneNumber)) {
                    Toast.makeText(this, "手机号格式不正确", Toast.LENGTH_LONG).show();
                    break;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(this, "密码不能为空", Toast.LENGTH_LONG).show();
                    break;
                }

                if (password.length() < 6) {
                    Toast.makeText(this, "密码不能少于6位", Toast.LENGTH_LONG).show();
                    break;
                }

                if (CommonUtils.isMobileNO(phoneNumber) && password.length() >= 6) {
                    HashMap<String, Object> hashMap = new HashMap<String, Object>();
                    // loginname=“********”，password=“********”
                    hashMap.put("loginname", phoneNumber);
                    hashMap.put("password", password);
                    hashMap.put("appcode", SystemInfo.getDeviceID());
                    showProgress();
                    ReqApi.getInstance().postMethod(HttpConstant.passLogin, hashMap, new AsyncHttpCallBack<User>() {
                        @Override
                        public void onSuccess(ResponseBody<User> body) {
                            hideProgress();
                            if (body.isSuccess()) {//登录成功
                                LoginInfo loginInfo = new LoginInfo(body.data.sid, body.data.token);
                                SPUtils.saveSerializable(SPUtils.KeyConstant.imAccount, loginInfo);
                                mHandler.sendMessage(mHandler.obtainMessage(MSG_SET_ALIAS, body.data.pushToken));
                                AbortableFuture<LoginInfo> loginRequest = NIMClient.getService(AuthService.class).login(loginInfo);
                                loginRequest.setCallback(new RequestCallbackWrapper() {
                                    @Override
                                    public void onResult(int code, Object result, Throwable exception) {
                                        L.i_im("im login, code=" + code);
                                        if (code == ResponseCode.RES_SUCCESS) {
                                            IMHelper.getInstance().setLoginTime();
                                        } else {
//                                            Tst.showToast("im 登录失败:" + code);
                                        }
                                    }
                                });

                                SPUtils.saveLoginStatus(true);
                                SPUtils.saveUser(body.data);
                                SPUtils.saveCurrentMoblie(body.data.mobile);
                                //需要广播一个通知，让所有注册该广播的人都能收到，并且更新登录内容
                                RxBus.get().post(BusAction.ACTION_LOGIN,body.data);
                                KnmsApp.getInstance().getUnreadObservable().sendData();
                                finshActivity();
                            } else
                                Tst.showToast(body.desc);
                        }

                        @Override
                        public void onFailure(String msg) {
                            Tst.showToast(msg);
                            hideProgress();
                        }

                        @Override
                        public Type setType() {
                            return new TypeToken<ResponseBody<User>>() {
                            }.getType();
                        }
                    });
                    break;
                }
                break;
            case R.id.forget_password:
                Map<String, Object> param = new HashMap<>();
                param.put("tel", CommonUtils.isMobileNO(phoneNumber) ? phoneNumber : "");
                startActivityAnimGeneral(FindPasswordActivity.class, param);
                break;
            case R.id.agreement:
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("url", "https://h5.kebuyer.com/document/buy_protocol.html");
                startActivityAnimGeneral(CommWebViewActivity.class, map);
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


    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (v.getId() == R.id.edit_phone) {
            if (!TextUtils.isEmpty(phoneNumber))
                mCancelOne.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
        } else if (v.getId() == R.id.edit_password) {
            if (!TextUtils.isEmpty(password))
                mCancelTwo.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
        }
    }

    private void updateLogBtnBg() {
        phoneNumber = mPhone.getText().toString().trim();
        password = mPassword.getText().toString().trim();
        if (CommonUtils.isMobileNO(phoneNumber) && CommonUtils.VerificationPwd(password) && mPassword.length() >= 6 && mPassword.length() <= 32) {
            mLogin.setBackgroundResource(R.drawable.bg_rectangle_btn);
            mLogin.setTextColor(getResources().getColor(R.color.color_black_333333));
        } else {
            mLogin.setBackgroundResource(R.drawable.bg_rectangle_gray);
            mLogin.setTextColor(getResources().getColor(R.color.color_gray_999999));
        }
    }


    private final TagAliasCallback mAliasCallback = new TagAliasCallback() {
        @Override
        public void gotResult(int code, String alias, Set<String> tags) {
            String logs;
            switch (code) {
                case 0:
                    logs = "Set tag and alias success";
                    Log.e("jpush", logs);
                    break;

                case 6002:
                    logs = "Failed to set alias and tags due to timeout. Try again after 60s.";
                    Log.e("jpush", logs);
                    ConnectivityManager conn = (ConnectivityManager) AccountLoginActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo info = conn.getActiveNetworkInfo();
                    if (info != null && info.isConnected()) {
                        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_SET_ALIAS, alias), 1000 * 60);
                    } else {
                        Log.e("jpush", "No network");
                    }
                    break;

                default:
                    logs = "Failed with errorCode = " + code;
                    Log.e("jpush", logs);
            }
        }

    };
    @Subscribe(tags = {@Tag(BusAction.CHANGE_PHONE)})
    public void  showPhone(String phoneNumber){
        if(mPhone != null) mPhone.setText(phoneNumber);
    }
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SET_ALIAS:
                    Log.e("jpus", "Set alias in handler." + msg.obj);
                    JPushInterface.setAliasAndTags(getApplicationContext(), (String) msg.obj, null, mAliasCallback);
                    break;
                default:
                    Log.i("jpus", "Unhandled msg - " + msg.what);
            }
        }
    };

}
