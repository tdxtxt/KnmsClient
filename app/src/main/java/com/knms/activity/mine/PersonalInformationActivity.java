package com.knms.activity.mine;

import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.knms.activity.ChangeNicknameActivity;
import com.knms.activity.ChangePasswordActivity;
import com.knms.activity.ChangePhoneActivity;
import com.knms.activity.base.HeadBaseActivity;
import com.knms.activity.main.MineActivity;
import com.knms.activity.mall.address.AddAddresActivity;
import com.knms.activity.mall.address.ManagementAddressActivity;
import com.knms.android.BuildConfig;
import com.knms.android.R;
import com.knms.bean.ResponseBody;
import com.knms.bean.other.Pic;
import com.knms.bean.user.User;
import com.knms.core.im.cache.NimUserInfoCache;
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
import com.knms.util.DialogHelper;
import com.knms.util.ImageLoadHelper;
import com.knms.util.LocalDisplay;
import com.knms.util.SPUtils;
import com.knms.util.Tst;
import com.knms.view.CircleImageView;
import com.yuyh.library.imgsel.ImgSelActivity;
import com.yuyh.library.imgsel.ImgSelConfig;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class PersonalInformationActivity extends HeadBaseActivity implements
        OnClickListener {

    private CircleImageView mHeadPortrait;
    private TextView mNickname, mAccount, mBindPhone;
    private RelativeLayout mChangeHeadPortrait, mChangeNickname, mChangePhone,
            mChangePassword,mMineQrCode;
    private TextView mLogOut;
    private DialogHelper dialogHelper;

    private final int TOALBUM = 0x00011;

    @Override
    protected int layoutResID() {
        return R.layout.activity_personal_information;
    }

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("个人信息");
    }

    @Override
    protected void initView() {
        mHeadPortrait = (CircleImageView) findViewById(R.id.head_portrait);
        mNickname = (TextView) findViewById(R.id.tv_nickname);
        mAccount = (TextView) findViewById(R.id.account);
        mBindPhone = (TextView) findViewById(R.id.bind_phone);
        mChangeHeadPortrait = (RelativeLayout) findViewById(R.id.change_head_portrait);
        mChangeNickname = (RelativeLayout) findViewById(R.id.change_nickname);
        mChangePhone = (RelativeLayout) findViewById(R.id.change_phone);
        mChangePassword = (RelativeLayout) findViewById(R.id.change_password);
        mMineQrCode= (RelativeLayout) findViewById(R.id.mine_qr_code);
        mLogOut = (TextView) findViewById(R.id.log_out);
    }

    @Override
    protected void initData() {
        mHeadPortrait.setOnClickListener(this);
        mChangeHeadPortrait.setOnClickListener(this);
        mChangeNickname.setOnClickListener(this);
        mChangePhone.setOnClickListener(this);
        mChangePassword.setOnClickListener(this);
        mLogOut.setOnClickListener(this);
        mMineQrCode.setOnClickListener(this);
        findView(R.id.mine_address).setOnClickListener(this);
        reqApi();
//
//        if (SPUtils.isLogin()) {
//            User user = SPUtils.getUser();
//            updateView(user);//刷新界面
//        }
    }

    @Override
    public String setStatisticsTitle() {
        return "个人信息";
    }

    private void updateView(User user) {
        mNickname.setText(CommonUtils.phoneNumberFormat(user.nickname));
        mAccount.setText(CommonUtils.phoneNumberFormat(user.account));
        mBindPhone.setText(CommonUtils.phoneNumberFormat(user.mobile));
        ImageLoadHelper.getInstance().displayImageHead(this,user.uicon, mHeadPortrait);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_portrait:
            case R.id.change_head_portrait:
                ImgSelConfig config = new ImgSelConfig.Builder()
                        .multiSelect(true) // 是否多选
                        .uploadService(true)//是否上传服务器
                        .needCrop(true)//需要裁剪
                        .cropSize(1, 1, 200, 200)// 裁剪大小 needCrop为true的时候配置
                        .maxNum(1) // 最大选择图片数量
                        .build();
                // 跳转到图片选择器
                ImgSelActivity.startActivity(this, config, TOALBUM);
                break;
            case R.id.change_nickname:
                startActivityAnimGeneral(ChangeNicknameActivity.class, null);
                break;
            case R.id.change_phone:
                startActivityAnimGeneral(ChangePhoneActivity.class, null);
                break;
            case R.id.change_password:
                startActivityAnimGeneral(ChangePasswordActivity.class, null);
                break;
            case R.id.log_out:
                showDialog();
                break;
            case R.id.mine_qr_code:
                startActivityAnimGeneral(MineQRcodeActivity.class,null);
                break;
            case R.id.mine_address:
                startActivityAnimGeneral(ManagementAddressActivity.class,null);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;
        switch (requestCode) {
            case TOALBUM:
                List<Pic> pics = (List<Pic>) data.getSerializableExtra(ImgSelActivity.RESULT_REMOTE_PIC);
                if(pics != null && pics.size() > 0) {
                    setAvatar(pics.get(0).url);
                }
                break;
        }
    }

    @Override
    protected void reqApi() {
        User user = SPUtils.getUser();
        Observable observable;
        if(user != null && !TextUtils.isEmpty(user.usercode)){
            ResponseBody<User> responseBody = new ResponseBody<>();
            responseBody.data = user;
            responseBody.code = ConstantObj.OK;
            observable = Observable.just(responseBody);
        }else {
            observable = RxRequestApi.getInstance().getApiService().getUserInfo(BuildConfig.SER_VERSION_CODE);
        }
        observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<User>>() {
                    @Override
                    public void call(ResponseBody<User> userResponseBody) {
                        User user = userResponseBody.data;
                        updateView(user);
                        SPUtils.saveUser(user);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        User user = SPUtils.getUser();
                        updateView(user);
                    }
                });
    }

    private void setAvatar(final String url) {
        showProgress();
        Map<String, Object> parmas = new HashMap<String, Object>();
        parmas.put("imageUrl", url);
        ReqApi.getInstance().postMethod(HttpConstant.uiconupdate, parmas, new AsyncHttpCallBack() {
            @Override
            public void onSuccess(ResponseBody body) {
                hideProgress();
                if (body.isSuccess()) {//上传成功
                    ImageLoadHelper.getInstance().displayImageHead(PersonalInformationActivity.this,url, mHeadPortrait);
                    User user = SPUtils.getUser();
                    if (user != null) {
                        user.uicon = url;
                        SPUtils.saveUser(user);
                        RxBus.get().post(BusAction.ACTION_LOGIN,user);//发送通知给所有需要更新头像的地方
                        NimUserInfoCache.getInstance().getUserInfoFromRemote(user.sid, null);
                    }
                } else {
                    Tst.showToast(body.desc);
                }
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
    }

    private void showDialog() {
        dialogHelper = new DialogHelper();
        dialogHelper.showPromptDialog(this, null, "确定退出登录?", "取消", null, "确定", new DialogHelper.OnMenuClick() {
            @Override
            public void onLeftMenuClick() {

            }

            @Override
            public void onCenterMenuClick() {

            }

            @Override
            public void onRightMenuClick() {
                CommonUtils.logout();
                finshActivity();

            }
        });
    }

    @Subscribe(tags = {@Tag(BusAction.ACTION_LOGIN)})
    public void changeUser(User user) {
        updateView(user);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
