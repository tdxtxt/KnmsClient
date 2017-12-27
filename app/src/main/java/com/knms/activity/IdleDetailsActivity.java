package com.knms.activity;

import android.content.Intent;
import android.graphics.Paint;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.knms.activity.base.HeadDetailNotifyBaseActivity;
import com.knms.activity.im.ChatActivity;
import com.knms.activity.login.FasterLoginActivity;
import com.knms.adapter.AutoBrowseAdapter;
import com.knms.android.R;
import com.knms.bean.ResponseBody;
import com.knms.bean.idle.IdleDetails;
import com.knms.bean.other.Pic;
import com.knms.bean.user.User;
import com.knms.core.im.msg.Product;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.net.HttpConstant;
import com.knms.net.ReqApi;
import com.knms.net.RxRequestApi;
import com.knms.oncall.AsyncHttpCallBack;
import com.knms.util.CommonUtils;
import com.knms.util.ImageLoadHelper;
import com.knms.util.SPUtils;
import com.knms.util.StrHelper;
import com.knms.util.Tst;
import com.knms.view.CircleImageView;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.shareuzi.bean.ShareEntity;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * 闲置家具详情
 */
public class IdleDetailsActivity extends HeadDetailNotifyBaseActivity implements OnClickListener {
    private TextView mCurrentPrice, mCostPrice, mFreight, mBrowseAmount;
    private TextView mContent;
    private ViewPager auto_vp;
    private AutoBrowseAdapter adapter_auto;
    private TextView tv_num;
    private RelativeLayout mUser;
    private CircleImageView mUserHeadPortrait;
    private TextView mUserName, mReleaseTime;

    private LinearLayout mCollect, mShare;
    private TextView mLocation, mCollectAmount, mContactVendor, soldOutLayout;
    private ImageView imgCollect;
    private boolean isChoose = false;
    private int imgLen = 0, collectNum;

    private String id;
    private String usid;
    private IdleDetails idleDetails;
    private PullToRefreshScrollView pullToRefreshScrollView;

    @Override
    protected void getParmas(Intent intent) {
        id = intent.getStringExtra("id");
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_idle_details;
    }

    @Override
    protected void initView() {
        mCurrentPrice = (TextView) findViewById(R.id.current_price);
        mCostPrice = (TextView) findViewById(R.id.cost_price);
        mFreight = (TextView) findViewById(R.id.freight);
        mBrowseAmount = (TextView) findViewById(R.id.browse_amount);
        mContent = (TextView) findViewById(R.id.content);
        mUser = (RelativeLayout) findViewById(R.id.user);
        mUserHeadPortrait = (CircleImageView) findViewById(R.id.user_head_portrait);
        mUserName = (TextView) findViewById(R.id.user_name);
        mReleaseTime = (TextView) findViewById(R.id.release_time);
        mLocation = (TextView) findViewById(R.id.location);
        mCollect = (LinearLayout) findViewById(R.id.collect);
        imgCollect = (ImageView) findViewById(R.id.img_collect);
        mCollectAmount = (TextView) findViewById(R.id.collect_amount);
        mShare = (LinearLayout) findViewById(R.id.share);
        mContactVendor = (TextView) findViewById(R.id.contact_vendor);
        auto_vp = findView(R.id.vp_detail_img);
        auto_vp.setOffscreenPageLimit(3);
        tv_num = findView(R.id.tv_num);
        soldOutLayout = (TextView) findViewById(R.id.sold_out_layout);
        mCostPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        pullToRefreshScrollView= (PullToRefreshScrollView) findViewById(R.id.pull_to_refresh_scrollview);
        pullToRefreshScrollView.setMode(PullToRefreshBase.Mode.BOTH);
    }

    @Override
    protected void initData() {
        mUser.setOnClickListener(this);
        mLocation.setOnClickListener(this);
        mCollect.setOnClickListener(this);
        mShare.setOnClickListener(this);
        mContactVendor.setOnClickListener(this);
        findView(R.id.contact_vendor).setOnClickListener(this);

        adapter_auto = new AutoBrowseAdapter(this,new ArrayList<Pic>());
        auto_vp.setAdapter(adapter_auto);
        auto_vp.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                tv_num.setText(position + 1 + "/" + imgLen);
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        reqApi();
    }

    @Override
    public String setStatisticsTitle() {
        return "闲置家具详情";
    }

    ShareEntity shareData;

    @Override
    protected void reqApi() {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("goid", id);
        ReqApi.getInstance().postMethod(HttpConstant.idleDetail, params, new AsyncHttpCallBack<IdleDetails>() {
            @Override
            public void onSuccess(ResponseBody<IdleDetails> body) {
                hideProgress();
                if (body.isSuccess()) {
                    if (body.data != null) shareData = body.data.shareData;
                    idleDetails = body.data;
                    usid = body.data.usid;
                    updateView(body.data);
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
                return new TypeToken<ResponseBody<IdleDetails>>() {
                }.getType();
            }
        });
    }

    private void updateView(IdleDetails data) {
        if (data.coState == 0 || data.coState == 2) {
            soldOutLayout.setVisibility(View.GONE);
        } else {
            soldOutLayout.setVisibility(View.VISIBLE);
        }
        mCurrentPrice.setText(CommonUtils.addMoneySymbol(CommonUtils.keepTwoDecimal(Double.parseDouble(data.goprice))));
        if (data.gooriginal != null&&Double.parseDouble(data.gooriginal)>=0) {
            mCostPrice.setText(CommonUtils.addMoneySymbol( CommonUtils.keepTwoDecimal(Double.parseDouble(data.gooriginal))));
        } else {//无原价则不显示
            mCostPrice.setVisibility(View.GONE);
        }
        mBrowseAmount.setText("浏览" + (data.browseNumber == 0 ? "" : data.browseNumber));
        mContent.setText(data.coremark);
        mUserName.setText(CommonUtils.phoneNumberFormat(data.usnickname));
        mReleaseTime.setText(StrHelper.displayTime(data.goreleasetime, false, true));
        mLocation.setText(data.goareaname);
        collectNum = data.collectNumber;
        mCollectAmount.setText("收藏" + (data.collectNumber == 0 ? "" : data.collectNumber));
        if(!TextUtils.isEmpty(data.userPhoto))ImageLoadHelper.getInstance().displayImageHead(IdleDetailsActivity.this,data.userPhoto, mUserHeadPortrait);
//        ImageLoader.getInstance().displayImage(data.userPhoto, mUserHeadPortrait, DisplayImageOptionsCofig.opt_default);
        if (data.iscollectNumber == 0) {//收藏：0是，1否
            imgCollect.setImageResource(R.drawable.shou_cang_on);
            isChoose = true;
        } else {
            imgCollect.setImageResource(R.drawable.shou_cang_off);
            isChoose = false;
        }
        if (data.gofreeshop == 0) {//包邮：0是，1否
            mFreight.setText("包邮");
        } else {
            if (data.gofreeshopprice == 0) {//运费（0：是，1：否）
                mFreight.setText("运费待议");
            } else {
                mFreight.setText("运费¥" + data.gofreeshopprice);
            }
        }
        imgLen = data.imglist.size();
        adapter_auto = new AutoBrowseAdapter(this,data.imglist);
        auto_vp.setAdapter(adapter_auto);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.user:
//                Toast.makeText(this, "开发中...", Toast.LENGTH_SHORT).show();
                break;
            case R.id.location:
//                Toast.makeText(this, "开发中...", Toast.LENGTH_SHORT).show();
                break;
            case R.id.collect:
                if (!SPUtils.isLogin()) {
                    startActivityAnimGeneral(FasterLoginActivity.class, null);
                    return;
                }

                int type = isChoose ? 1 : 0;//0：收藏 1：取消收藏
                RxRequestApi.getInstance().getApiService().collect(id, 1 ,type)
                        .observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
                        .subscribe(new Action1<ResponseBody>() {
                            @Override
                            public void call(ResponseBody body) {
                                if(body.isSuccess()){
                                    if(isChoose){//之前已收藏了
                                        imgCollect.setImageResource(R.drawable.shou_cang_off);
                                        isChoose = false;
                                        --collectNum;
                                    }else {//之前没收藏了
                                        imgCollect.setImageResource(R.drawable.shou_cang_on);
                                        isChoose = true;
                                        ++collectNum;
                                    }
                                    RxBus.get().post(BusAction.CANCEL_GOODS_COLLECTION, isChoose);
                                    mCollectAmount.setText("收藏" + (collectNum == 0 ? "" : collectNum));
                                }else {
                                    if ("已经收藏".equals(body.desc)){
                                        reqApi();//刷新列表
                                    }else if(!"需要登录".equals(body.desc)){
                                        Tst.showToast(body.desc);
                                    }
                                }
                            }
                        });
                break;
            case R.id.share:
                //TODO 分享
                OnekeyShare oks = new OnekeyShare();
                oks.show(this, shareData);
//                ShareUtil.showShareDialog(this, shareData, ShareConstant.REQUEST_CODE);
                break;
            case R.id.contact_vendor:
                if (!SPUtils.isLogin()) {
                    startActivityAnimGeneral(FasterLoginActivity.class, null);
                    return;
                }
                if (TextUtils.isEmpty(usid)) return;
                Map<String, Object> parmas = new HashMap<>();
                parmas.put("sid", usid);
                if (idleDetails != null) {
                    Product product = new Product();
                    product.content = idleDetails.coremark;
                    product.icon = (idleDetails.imglist != null && idleDetails.imglist.size() > 0) ? idleDetails.imglist.get(0).url : "";
                    product.price = idleDetails.goprice;
                    product.productType = "2";
                    product.productId = id;
                    product.userId = usid;
                    parmas.put("prodcut", product);
                }
                startActivityAnimGeneral(ChatActivity.class, parmas);
                break;
        }
    }
    @Subscribe(tags = {@Tag(BusAction.ACTION_LOGIN)})
    public void loginNotify(User user) {
        reqApi();
    }
}
