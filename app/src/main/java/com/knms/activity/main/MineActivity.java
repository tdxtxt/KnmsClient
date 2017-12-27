package com.knms.activity.main;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.knms.activity.CommWebViewActivity;
import com.knms.activity.FeedBackActivity;
import com.knms.activity.SettingActivity;
import com.knms.activity.base.BaseActivity;
import com.knms.activity.coupons.MyCouponsListActivity;
import com.knms.activity.im.KnmsKefuChatActivity;
import com.knms.activity.login.FasterLoginActivity;
import com.knms.activity.mine.MineCollectionActivity;
import com.knms.activity.mine.MinePriceRationActivity;
import com.knms.activity.mine.MyIdleActivity;
import com.knms.activity.mine.MyRepairListActivity;
import com.knms.activity.mine.PersonalInformationActivity;
import com.knms.android.BuildConfig;
import com.knms.android.R;
import com.knms.app.KnmsApp;
import com.knms.bean.ResponseBody;
import com.knms.bean.other.TipNum;
import com.knms.bean.user.User;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.net.RxRequestApi;
import com.knms.util.CommonUtils;
import com.knms.util.ConstantObj;
import com.knms.util.DialogHelper;
import com.knms.util.ImageLoadHelper;
import com.knms.util.LocalDisplay;
import com.knms.util.SPUtils;
import com.knms.view.CircleImageView;

import java.util.HashMap;
import java.util.Map;
import java.util.Observer;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MineActivity extends BaseActivity implements OnClickListener {

    private TextView mUserName, tvCoupons;
    private CircleImageView mHeadPortrait;
    private RelativeLayout mGotoLogin, mMyOrder, mFeedback, mHelp, mSetting;
    private LinearLayout mMyPriceRation, mMyIdle;
    private ImageView mOnlineService, mPhoneService;
    private DialogHelper dialog;
    private Observer unreadObserver;
    private ImageView iv_tip_order,iv_tip_bbprice,iv_tip_idle,iv_tip_repair,iv_tip_coupons;

    @Override
    protected int layoutResID() {
        return R.layout.activity_mine;
    }

    @Override
    protected void initView() {
        dialog = new DialogHelper();
        mHeadPortrait = (CircleImageView) findViewById(R.id.head_portrait);
        mUserName = (TextView) findViewById(R.id.user_name);
        mGotoLogin = (RelativeLayout) findViewById(R.id.goto_login);
        mMyOrder = (RelativeLayout) findViewById(R.id.my_order);
        mMyPriceRation = findView(R.id.my_price_relations);
        mMyIdle = (LinearLayout) findViewById(R.id.my_idle);
        tvCoupons = (TextView) findViewById(R.id.tv_coupons);

        mFeedback = (RelativeLayout) findViewById(R.id.feedback);
        mHelp = (RelativeLayout) findViewById(R.id.help);
        mSetting = (RelativeLayout) findViewById(R.id.setting);
        mOnlineService = (ImageView) findViewById(R.id.online_service);
        mPhoneService = (ImageView) findViewById(R.id.phone_service);

        iv_tip_order = findView(R.id.iv_tip_order);
        iv_tip_bbprice = findView(R.id.iv_tip_bbprice);
        iv_tip_idle = findView(R.id.iv_tip_idle);
        iv_tip_repair = findView(R.id.iv_tip_repair);
        iv_tip_coupons = findView(R.id.iv_tip_coupons);
    }

    @Override
    protected void initData() {
        mGotoLogin.setOnClickListener(this);
        mMyOrder.setOnClickListener(this);
        mMyPriceRation.setOnClickListener(this);
        mMyIdle.setOnClickListener(this);
        mFeedback.setOnClickListener(this);
        mHelp.setOnClickListener(this);
        mSetting.setOnClickListener(this);
        mOnlineService.setOnClickListener(this);
        mPhoneService.setOnClickListener(this);
        tvCoupons.setOnClickListener(this);
        findView(R.id.ll_collection_baby).setOnClickListener(this);
        findView(R.id.ll_brower).setOnClickListener(this);
        findView(R.id.my_maintain).setOnClickListener(this);

        reqApi();
        //设置小红点改变
        unreadObserver = new Observer() {
            @Override
            public void update(java.util.Observable observable, Object data) {
                if(data instanceof TipNum){
                    TipNum tipNum = (TipNum) data;
                    if(data == null) return;
                    if(tipNum.order > 0){ iv_tip_order.setVisibility(View.VISIBLE); }else{ iv_tip_order.setVisibility(View.GONE);}
                    if(tipNum.parity > 0){ iv_tip_bbprice.setVisibility(View.VISIBLE); }else{ iv_tip_bbprice.setVisibility(View.GONE);}
                    if(tipNum.idel > 0){ iv_tip_idle.setVisibility(View.VISIBLE); }else{ iv_tip_idle.setVisibility(View.GONE);}
                    if(tipNum.repair > 0){ iv_tip_repair.setVisibility(View.VISIBLE); }else{ iv_tip_repair.setVisibility(View.GONE);}
                    if(tipNum.coupon > 0){ iv_tip_coupons.setVisibility(View.VISIBLE); }else{ iv_tip_coupons.setVisibility(View.GONE);}
                }
            }
        };
        KnmsApp.getInstance().getUnreadObservable().addObserver(unreadObserver);
        KnmsApp.getInstance().getUnreadObservable().sendData();//初始化数据
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SPUtils.isLogin()) {
            User user = SPUtils.getUser();
            //刷新界面
            updateView(user);
        }else {
            findView(R.id.iv_mine_qrcode).setVisibility(View.GONE);
            mUserName.setText("登录");
            mHeadPortrait.setImageResource(R.drawable.icon_avatar);
        }
    }

    @Override
    protected void reqApi() {
        if (SPUtils.isLogin()) {
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
                            if(user != null && !TextUtils.isEmpty(user.nickname)){
                                updateView(user);
                            }else {
                                CommonUtils.logout();
                                mUserName.setText("登录");
                                mHeadPortrait.setImageResource(R.drawable.icon_avatar);
                            }
                        }
                    });
        }else{
            mUserName.setText("登录");
            mHeadPortrait.setImageResource(R.drawable.icon_avatar);
        }
    }

    @Override
    public String setStatisticsTitle() {
        return "我的";
    }


    private void updateView(User user) {
        if (user == null){
            findView(R.id.iv_mine_qrcode).setVisibility(View.GONE);
            return;
        }
        findView(R.id.iv_mine_qrcode).setVisibility(View.VISIBLE);
        mUserName.setText(CommonUtils.phoneNumberFormat(user.nickname));
        ImageLoadHelper.getInstance().displayImageHead(MineActivity.this,user.uicon, mHeadPortrait, LocalDisplay.dp2px(56),LocalDisplay.dp2px(56));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.goto_login:
                if (!SPUtils.isLogin()) {
                    startActivityAnimGeneral(FasterLoginActivity.class, null);
                } else {
                    startActivityAnimGeneral(PersonalInformationActivity.class, null);
                }
                break;
            case R.id.my_order:
                if (!SPUtils.isLogin()) {
                    startActivityAnimGeneral(FasterLoginActivity.class, null);
                } else {
                    startActivityAnimGeneral(com.knms.activity.mine.MyOrderActivity.class,null);
//                    startActivityAnimGeneral(MyOrderActivity.class, null);//TODO 支付商城系统-我的订单
                }
                break;
            case R.id.my_price_relations:
                if (!SPUtils.isLogin()) {
                    startActivityAnimGeneral(FasterLoginActivity.class, null);
                } else {
                    startActivityAnimGeneral(MinePriceRationActivity.class, null);
                }
                break;
            case R.id.my_idle:
                if (!SPUtils.isLogin()) {
                    startActivityAnimGeneral(FasterLoginActivity.class, null);
                } else {
                    startActivityAnimGeneral(MyIdleActivity.class, null);
                }
                break;

            case R.id.feedback:
                startActivityAnimGeneral(FeedBackActivity.class, null);
                break;
            case R.id.help:
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("url", "https://h5.kebuyer.com/document/help.html");
                startActivityAnimGeneral(CommWebViewActivity.class, map);
                break;
            case R.id.setting:
                startActivityAnimGeneral(SettingActivity.class, null);
                break;
            case R.id.online_service:
                if (!SPUtils.isLogin()) {
                    startActivityAnimGeneral(FasterLoginActivity.class, null);
                    break;
                }
                startActivityAnimGeneral(KnmsKefuChatActivity.class, null);

                break;
            case R.id.phone_service:
                showDialDialog();
                break;
            case R.id.ll_collection_baby:
                if (!SPUtils.isLogin()) {
                    startActivityAnimGeneral(FasterLoginActivity.class, null);
                    break;
                }
                Map<String, Object> parmas1 = new HashMap<String, Object>();
                parmas1.put("type", 0);
                parmas1.put("positon", 0);
                startActivityAnimGeneral(MineCollectionActivity.class, parmas1);
                break;
            case R.id.my_maintain:
                if (!SPUtils.isLogin()) {
                    startActivityAnimGeneral(FasterLoginActivity.class, null);
                } else {
                    startActivityAnimGeneral(MyRepairListActivity.class, null);
                }
                break;
            case R.id.ll_brower:
                if (!SPUtils.isLogin()) {
                    startActivityAnimGeneral(FasterLoginActivity.class, null);
                    break;
                }
                Map<String, Object> parmas4 = new HashMap<String, Object>();
                parmas4.put("type", 1);
                startActivityAnimGeneral(MineCollectionActivity.class, parmas4);
                break;
            case R.id.tv_coupons:
                if (!SPUtils.isLogin()) {
                    startActivityAnimGeneral(FasterLoginActivity.class, null);
                    break;
                }
                startActivityAnimGeneral(MyCouponsListActivity.class, null);
                break;
        }
    }

    @Subscribe(tags = {@Tag(BusAction.ACTION_LOGIN)})
    public void changeUser(User user) {
        //刷新界面
        updateView(user);
    }
    @Subscribe(tags = {@Tag(BusAction.ACTION_LOGOUT)})
    public void logout(String notify){
        mUserName.setText("登录");
//        mHeadPortrait.setImageResource(R.drawable.icon_avatar);
        ImageLoadHelper.getInstance().displayImage(this,R.drawable.icon_avatar,mHeadPortrait);
        findView(R.id.iv_mine_qrcode).setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        KnmsApp.getInstance().getUnreadObservable().deleteObserver(unreadObserver);
        super.onDestroy();
    }

    private void showDialDialog() {
        dialog.showPromptDialog(this, "", "是否拨打023-63317666", "取消", "", "确定", new DialogHelper.OnMenuClick() {
            @Override
            public void onLeftMenuClick() {}
            @Override
            public void onCenterMenuClick() {}
            @Override
            public void onRightMenuClick() {
                Intent mIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
                        + "023-63317666"));
                startActivity(mIntent);
            }
        });
    }
}
