package com.knms.activity;

import com.google.gson.reflect.TypeToken;
import com.knms.activity.base.HeadBaseActivity;
import com.knms.bean.ResponseBody;
import com.knms.bean.user.User;
import com.knms.core.im.cache.NimUserInfoCache;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.net.HttpConstant;
import com.knms.net.ReqApi;
import com.knms.oncall.AsyncHttpCallBack;
import com.knms.util.CommonUtils;
import com.knms.util.SPUtils;
import com.knms.util.Tst;
import com.knms.android.R;

import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.reflect.Type;
import java.util.HashMap;

public class ChangeNicknameActivity extends HeadBaseActivity implements OnClickListener {

    private ImageView mCancel;
    private EditText mNickname;
    private TextView mSave;
    private String nickName;

    @Override
    protected int layoutResID() {
        return R.layout.activity_change_nickname;
    }

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("修改昵称");
    }

    @Override
    protected void initView() {
        mCancel = (ImageView) findViewById(R.id.cancel);
        mNickname = (EditText) findViewById(R.id.nickname);
        mSave = (TextView) findViewById(R.id.save);
    }

    @Override
    protected void initData() {
        mCancel.setOnClickListener(this);
        mSave.setOnClickListener(this);
        mNickname.addTextChangedListener(mWatcher);

        if ((Boolean) SPUtils.getFromApp(SPUtils.KeyConstant.loginState, false)) {
            User user = SPUtils.getUser();
            updateView(user);//刷新界面
        }

        if (!TextUtils.isEmpty(mNickname.getText())) {
            mNickname.setSelection(mNickname.getText().length());
        }

    }

    @Override
    public String setStatisticsTitle() {
        return "修改昵称";
    }

    private void updateView(User user) {
        mNickname.setText(CommonUtils.isMobileNO(user.nickname) ? CommonUtils.phoneNumberFormat(user.nickname) : user.nickname);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel:
                mNickname.setText(null);
                mCancel.setVisibility(View.GONE);
                mSave.setBackgroundResource(R.drawable.sign_45);
                mSave.setTextColor(getResources().getColor(R.color.color_gray_999999));
                break;
            case R.id.save:
                nickName = mNickname.getText().toString().trim();
                HashMap<String, Object> params = new HashMap<String, Object>();
                params.put("nickname", nickName);
                showProgress();
                ReqApi.getInstance().postMethod(HttpConstant.nicknameupdate, params, new AsyncHttpCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseBody<String> body) {
                        if (body.isSuccess()) {//修改昵称保存成功
                            User user = SPUtils.getUser();
                            if (user != null) {
                                user.nickname = TextUtils.isEmpty(body.data) ? nickName : body.data;
                                SPUtils.saveUser(user);
                            }
                            //需要广播一个通知，让所有注册该广播的人都能收到，并且更新登录内容
                            RxBus.get().post(BusAction.ACTION_LOGIN,user);
                            NimUserInfoCache.getInstance().getUserInfoFromRemote(user.sid, null);
                            hideProgress();
                            finshActivity();
                        } else {
                            Tst.showToast(body.desc);
                            hideProgress();
                        }
                    }
                    @Override
                    public void onFailure(String msg) {
                        Tst.showToast(msg);
                        hideProgress();
                    }
                    @Override
                    public Type setType() {
                        return new TypeToken<ResponseBody<String>>() {
                        }.getType();
                    }
                });
                break;
        }
    }

    private TextWatcher mWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int count,
                                  int after) {
            if (s.length() == 0) {
                mCancel.setVisibility(View.GONE);
            } else {
                mCancel.setVisibility(View.VISIBLE);
            }
            int mTextMaxLenght = 0;
            Editable editable = mNickname.getText();
            String str = editable.toString();
            int selEndIndex = Selection.getSelectionEnd(editable);
            for (int i = 0; i < str.length(); i++) {
                char charAt = str.charAt(i);
                if (charAt >= 32 && charAt <= 122) {
                    mTextMaxLenght++;
                } else {
                    mTextMaxLenght += 2;
                }
                if (mTextMaxLenght > 20) {
                    String newStr = str.substring(0, i);
                    mNickname.setText(newStr);
                    editable = mNickname.getText();
                    int newLen = editable.length();
                    if (selEndIndex > newLen) {
                        selEndIndex = editable.length();
                    }
                    Selection.setSelection(editable, selEndIndex);
                }

            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            String nickName = mNickname.getText().toString().trim();
            if (!TextUtils.isEmpty(nickName)) {
                if (CommonUtils.len(nickName) >= 4 && CommonUtils.len(nickName) <= 20) {
                    mSave.setBackgroundResource(R.drawable.sign_55);
                    mSave.setTextColor(getResources().getColor(
                            R.color.button_text_on));
                    mSave.setClickable(true);
                    return;
                }
            }
            mSave.setBackgroundResource(R.drawable.sign_45);
            mSave.setTextColor(getResources().getColor(
                    R.color.color_gray_999999));
            mSave.setClickable(false);
        }
    };

}
