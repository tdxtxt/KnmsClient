package com.knms.activity.login;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


import com.google.gson.reflect.TypeToken;
import com.knms.activity.CommWebViewActivity;
import com.knms.activity.base.HeadBaseActivity;
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
import com.knms.net.RxRequestApi;
import com.knms.oncall.AsyncHttpCallBack;
import com.knms.util.CommonUtils;
import com.knms.util.ConstantObj;
import com.knms.util.L;
import com.knms.util.MD5Util;
import com.knms.util.SPUtils;
import com.knms.util.SystemInfo;
import com.knms.util.Tst;
import com.knms.android.R;
import com.knms.view.TimerButton;
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
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;
import rx.functions.Action1;

/**
 * 快速登录-短信登录界面
 */
public class FasterLoginActivity extends HeadBaseActivity implements OnClickListener, View.OnFocusChangeListener {

    private ImageView mCancelOne, mCancelTwo;
    private EditText mPhone, mVerificationCode;
    private TextView mAccountLogin, mLogin, mAgreement;
    private TimerButton mGetVerificationCode;
    private String mCode;
    private String phoneNumber;

    @Override
    protected int layoutResID() {
        return R.layout.activity_faster_login;
    }

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("登录");
    }

    @Override
    protected void initView() {
        mCancelOne = (ImageView) findViewById(R.id.cancel_one);
        mCancelTwo = (ImageView) findViewById(R.id.cancel_two);
        mGetVerificationCode = (TimerButton) findViewById(R.id.get_verification_code);
        mPhone = (EditText) findViewById(R.id.edit_phone);
        mVerificationCode = (EditText) findViewById(R.id.verification_code);
        mAccountLogin = (TextView) findViewById(R.id.account_login);
        mLogin = (TextView) findViewById(R.id.login);
        mAgreement = (TextView) findViewById(R.id.agreement);
    }

    @Override
    protected void initData() {
        mVerificationCode.setOnFocusChangeListener(this);
        mPhone.setOnFocusChangeListener(this);
        mAccountLogin.setOnClickListener(this);
        mAccountLogin.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        mGetVerificationCode.setOnClickListener(this);
        mLogin.setOnClickListener(this);
        mAgreement.setOnClickListener(this);
        mAgreement.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        mCancelOne.setOnClickListener(this);
        mCancelTwo.setOnClickListener(this);
        mPhone.addTextChangedListener(mWatcherOne);
        mVerificationCode.addTextChangedListener(mWatcherTwo);
        showPhone(SPUtils.getCurrentMobile());
    }

    @Override
    public String setStatisticsTitle() {
        return "快捷登录";
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.cancel_one:
                mPhone.setText(null);
                mCancelOne.setVisibility(View.GONE);// 隐藏
                mGetVerificationCode.setBackgroundResource(R.drawable.sign_57);
                mGetVerificationCode.setTextColor(getResources().getColor(
                        R.color.color_gray_999999));
                break;
            case R.id.cancel_two:
                mVerificationCode.setText(null);
                mCancelTwo.setVisibility(View.GONE);// 隐藏
                break;
            case R.id.get_verification_code:
                phoneNumber = mPhone.getText().toString();
                showProgress();
                RxRequestApi.getInstance().getApiService().sendMsgCheck(phoneNumber, ConstantObj.SEND_TYPE_LOGIN, MD5Util.getMD5String(phoneNumber))
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
            case R.id.account_login:
                startActivityAnimGeneral(AccountLoginActivity.class, null);
                KnmsApp.getInstance().finishActivity(this);
                break;
            case R.id.login:
                hideKeyboard();
                phoneNumber = mPhone.getText().toString();
                mCode = mVerificationCode.getText().toString();
                if (mCode.length() < 6) {
                    Tst.showToast("请输入正确的手机号和验证码");
                    break;
                }
                if (!TextUtils.isEmpty(mCode)) {
                    HashMap<String, Object> params = new HashMap<String, Object>();
                    // username=“********”，checkCode=“********”channel：ANDROID
                    params.put("username", phoneNumber);
                    params.put("checkCode", mCode);
                    params.put("appcode", SystemInfo.getDeviceID());
                    showProgress();
                    ReqApi.getInstance().postMethod(HttpConstant.msgLogin, params, new AsyncHttpCallBack<User>() {
                        @Override
                        public void onSuccess(ResponseBody<User> body) {
                            hideProgress();
                            if (body.isSuccess()) {//登录成功
                                handlerPush.sendMessage(handlerPush.obtainMessage(MSG_SET_ALIAS, body.data.pushToken));
                                LoginInfo loginInfo = new LoginInfo(body.data.sid, body.data.token);
                                SPUtils.saveSerializable(SPUtils.KeyConstant.imAccount, loginInfo);
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
                                RxBus.get().post(BusAction.ACTION_LOGIN, body.data);
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
            case R.id.agreement:
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("url", "https://h5.kebuyer.com/document/buy_protocol.html");
                startActivityAnimGeneral(CommWebViewActivity.class, map);
                break;
        }
    }
    private TextWatcher mWatcherOne = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int count, int after) {
            updateLogBtnBg();
            if (s.length() == 0) {
                mCancelOne.setVisibility(View.GONE);// 隐藏
            } else {
                mCancelOne.setVisibility(View.VISIBLE);// 显示
            }
            if(!mGetVerificationCode.isEnabled())return;
            if (s.length() == 11) {
                if (CommonUtils.isMobileNO(s.toString())) {
                    mGetVerificationCode
                            .setBackgroundResource(R.drawable.sign_59);
                    mGetVerificationCode.setTextColor(getResources().getColor(
                            R.color.button_text_on));
                } else {
                    mGetVerificationCode
                            .setBackgroundResource(R.drawable.sign_57);
                    mGetVerificationCode.setTextColor(getResources().getColor(
                            R.color.color_gray_999999));
                    Toast.makeText(FasterLoginActivity.this, "请输入正确的手机号码",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                mGetVerificationCode.setBackgroundResource(R.drawable.sign_57);
                mGetVerificationCode.setTextColor(getResources().getColor(
                        R.color.color_gray_999999));
            }
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    private TextWatcher mWatcherTwo = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int count, int after) {
            updateLogBtnBg();
            if (s.length() == 0) {
                mCancelTwo.setVisibility(View.GONE);// 隐藏
            } else {
                mCancelTwo.setVisibility(View.VISIBLE);// 显示
            }
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void afterTextChanged(Editable s) {}
    };

    private void updateLogBtnBg() {
        phoneNumber = mPhone.getText().toString().trim();
        mCode = mVerificationCode.getText().toString().trim();

        if (CommonUtils.isMobileNO(phoneNumber) && mCode.length() == 6) {
            mLogin.setBackgroundResource(R.drawable.bg_rectangle_btn);
            mLogin.setTextColor(getResources().getColor(R.color.color_black_333333));
        } else {
            mLogin.setBackgroundResource(R.drawable.bg_rectangle_gray);
            mLogin.setTextColor(getResources().getColor(R.color.color_gray_999999));
        }
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        if (view.getId() == R.id.edit_phone) {
            if (!TextUtils.isEmpty(phoneNumber))
                mCancelOne.setVisibility(b ? View.VISIBLE : View.GONE);
        } else if (view.getId() == R.id.verification_code) {
            if (!TextUtils.isEmpty(mCode))
                mCancelTwo.setVisibility(b ? View.VISIBLE : View.GONE);
        }
    }

    private static final int MSG_SET_ALIAS = 1001;
    private TagAliasCallback mAliasCallback = new TagAliasCallback() {
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
                    ConnectivityManager conn = (ConnectivityManager) FasterLoginActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
                    if(conn != null){
                        NetworkInfo info = conn.getActiveNetworkInfo();
                        if (info != null && info.isConnected()) {
                            handlerPush.sendMessageDelayed(handlerPush.obtainMessage(MSG_SET_ALIAS, alias), 1000 * 60);
                        } else {
                            Log.e("jpush", "No network");
                        }
                    }
                    break;

                default:
                    logs = "Failed with errorCode = " + code;
                    Log.e("jpush", logs);
            }
        }

    };

    final private Handler handlerPush = new Handler() {
        @Override
        public void handleMessage(Message msg) {
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
    @Subscribe(tags = {@Tag(BusAction.CHANGE_PHONE)})
    public void  showPhone(String phoneNumber){
        if(mPhone != null) mPhone.setText(phoneNumber);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
