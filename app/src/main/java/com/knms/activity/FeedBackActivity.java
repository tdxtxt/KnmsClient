package com.knms.activity;

import com.google.gson.reflect.TypeToken;
import com.knms.activity.base.HeadBaseActivity;
import com.knms.bean.ResponseBody;
import com.knms.bean.user.User;
import com.knms.net.HttpConstant;
import com.knms.net.ReqApi;
import com.knms.oncall.AsyncHttpCallBack;
import com.knms.util.CommonUtils;
import com.knms.util.SPUtils;
import com.knms.util.Tst;
import com.knms.android.R;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Type;
import java.util.HashMap;

public class FeedBackActivity extends HeadBaseActivity implements OnClickListener {

    private EditText mFeedBackContent, mContactNumber;
    private TextView mWords,mCommit;
    private String feedBackContent;
    private String contactNumber="";

    @Override
    protected int layoutResID() {
        return R.layout.activity_feed_back;
    }

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("意见反馈");
    }

    @Override
    protected void initView() {
        mFeedBackContent = (EditText) findViewById(R.id.feedback_content);
        mContactNumber = (EditText) findViewById(R.id.contact_number);
        mWords = (TextView) findViewById(R.id.words);
        mCommit = (TextView) findViewById(R.id.commit);
    }

    @Override
    protected void initData() {
        mCommit.setOnClickListener(this);
        mFeedBackContent.addTextChangedListener(mTextWatcher);
        mContactNumber.setFocusableInTouchMode(true);
        if(SPUtils.isLogin()){
            mContactNumber.setFocusableInTouchMode(false);
            User user = SPUtils.getUser();
            updateView(user);//刷新界面
        }
    }

    @Override
    public String setStatisticsTitle() {
        return "意见反馈";
    }

    private void updateView(User user) {
        contactNumber=user.mobile;
        mContactNumber.setText(contactNumber.substring(0,3)+"****"+contactNumber.substring(7,11));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.commit:
                feedBackContent = mFeedBackContent.getText().toString().trim();
                if (TextUtils.isEmpty(feedBackContent)) {
                    Tst.showToast("您还未输入任何内容哦");
                    break;
                }

                if (!CommonUtils.isMobileNO(contactNumber.equals("")?mContactNumber.getText().toString().trim():contactNumber.trim())) {
                    Tst.showToast("请输入手机号码");
                    break;
                }

                    HashMap<String,Object> params = new HashMap<String,Object>();
                    params.put("mobile",contactNumber.equals("")?mContactNumber.getText().toString().trim():contactNumber.trim());
                    params.put("content",feedBackContent);
                    showProgress();
                    ReqApi.getInstance().postMethod(HttpConstant.feedback, params, new AsyncHttpCallBack() {
                        @Override
                        public void onSuccess(ResponseBody body) {
                            hideProgress();
                            Tst.showToast(body.desc);
                            if (body.isSuccess()) {//意见反馈提交成功
                                finshActivity();
                            }
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

                break;
        }

    }

    TextWatcher mTextWatcher = new TextWatcher() {
        private CharSequence temp;

        @Override
        public void onTextChanged(CharSequence s, int start, int count,
                                  int after) {
            temp = s;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            mWords.setText(temp.length() + "/200");
        }
    };

}
