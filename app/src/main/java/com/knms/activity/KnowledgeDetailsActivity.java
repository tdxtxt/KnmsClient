package com.knms.activity;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshRecyclerView;
import com.knms.activity.base.BaseActivity;
import com.knms.activity.im.MessageCenterActivity;
import com.knms.activity.login.FasterLoginActivity;
import com.knms.activity.pic.ImgBrowerPagerActivity;
import com.knms.adapter.MixingContentAdapter;
import com.knms.android.R;
import com.knms.bean.ResponseBody;
import com.knms.bean.style.HomeFurnishing;
import com.knms.bean.user.User;
import com.knms.core.im.IMHelper;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.net.RxRequestApi;
import com.knms.util.ImageLoadHelper;
import com.knms.util.SPUtils;
import com.knms.util.Tst;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.shareuzi.bean.ShareEntity;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class KnowledgeDetailsActivity extends BaseActivity implements OnClickListener {

    private ImageView mBack, mShare, mInformation;
    private PullToRefreshRecyclerView pullToRefreshRecyclerView;
    private RecyclerView mLayoutContent;
    private MixingContentAdapter adapter;
    private LinearLayout mCollect;
    private ImageView imgCollect, mImgTitle;
    private TextView mCollectAmount, mBrowseAmount, mTitle, mTime;
    private String decorateId, collectStatus;
    private boolean isChoose = false;
    private int collectTotal;
    private View viewHead;


    @Override
    protected int layoutResID() {
        return R.layout.activity_knowledge_details;
    }


    @Override
    protected void getParmas(Intent intent) {
        super.getParmas(intent);
        decorateId = intent.getStringExtra("id");
    }

    @Override
    protected void initView() {
        viewHead = getLayoutInflater().inflate(R.layout.knowledge_recyclerview_head, null);

        mBack = (ImageView) findViewById(R.id.iv_back);
        mShare = (ImageView) findViewById(R.id.share);
        mInformation = (ImageView) findViewById(R.id.information);

        mCollect = (LinearLayout) findViewById(R.id.collect);
        imgCollect = (ImageView) findViewById(R.id.img_collect);
        mCollectAmount = (TextView) findViewById(R.id.collect_amount);
        mBrowseAmount = (TextView) findViewById(R.id.browse_amount);

        mTime = (TextView) viewHead.findViewById(R.id.time);
        mImgTitle = (ImageView) viewHead.findViewById(R.id.title_img);
        mTitle = (TextView) viewHead.findViewById(R.id.encyclopedia_title);

        pullToRefreshRecyclerView = (PullToRefreshRecyclerView) findViewById(R.id.layout_content);
        pullToRefreshRecyclerView.setMode(PullToRefreshBase.Mode.BOTH);
        mLayoutContent=pullToRefreshRecyclerView.getRefreshableView();

        mLayoutContent.setLayoutManager(new LinearLayoutManager(this)/*{
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        }*/);
        adapter = new MixingContentAdapter();
        mLayoutContent.setAdapter(adapter);

        adapter.addHeaderView(viewHead);
//        adapter.addFooterView(viewFooter);

        reqApi();
    }

    @Override
    protected void initData() {
        mBack.setOnClickListener(this);
        mShare.setOnClickListener(this);
        mInformation.setOnClickListener(this);
        mCollect.setOnClickListener(this);

    }

    @Override
    public String setStatisticsTitle() {
        return "家居百科详情";
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finshActivity();
                break;
            case R.id.share:
                //TODO 分享
                OnekeyShare oks = new OnekeyShare();
                oks.show(this, shareData);
                break;
            case R.id.information:
                if (!SPUtils.isLogin()) {
                    startActivityAnimGeneral(FasterLoginActivity.class, null);
                } else {
                    startActivityAnimGeneral(MessageCenterActivity.class, null);
                }
                break;

            case R.id.collect:
                if (!SPUtils.isLogin()) {
                    startActivityAnimGeneral(FasterLoginActivity.class, null);
                    break;
                }
                RxRequestApi.getInstance().getApiService().collect(decorateId, 3, isChoose ? 1 : 0)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<ResponseBody>() {
                            @Override
                            public void call(ResponseBody body) {
                                if(body.isSuccess()){
                                    isChoose = !isChoose;
                                    updateCollectState();
                                    mCollectAmount.setText(isChoose == false ? (--collectTotal) + "" : (++collectTotal) + "");
                                    RxBus.get().post(BusAction.CANCEL_INSP_COLLECTION, isChoose);
                                }else{
                                    if ("已经收藏".equals(body.desc)){
                                        reqApi();//刷新列表
                                    }else if(!"需要登录".equals(body.desc)){
                                        Tst.showToast(body.desc);
                                    }
                                }
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                Tst.showToast(throwable.toString());
                            }
                        });

                break;
        }

    }

    private void updateView(final HomeFurnishing data) {
        collectTotal = Integer.parseInt(data.collectTotal);
        collectStatus = data.collectStatus;
        isChoose = collectStatus.equals("1") ? false : true;
        updateCollectState();
        mTitle.setText(data.title);
        mTime.setText(data.releaseTime);
        mCollectAmount.setText(data.collectTotal);
        mBrowseAmount.setText(data.tourTotal);
        adapter.setNewData(data.mixingContent);
        ImageLoadHelper.getInstance().displayImage(KnowledgeDetailsActivity.this,data.coInspirationPic, mImgTitle);
        mImgTitle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("data", Arrays.asList(data.coInspirationPic));
                startActivityAnimGeneral(ImgBrowerPagerActivity.class, map);
            }
        });
    }

    ShareEntity shareData;

    @Override
    protected void reqApi() {
        RxRequestApi.getInstance().getApiService().getHomeFurnishing(decorateId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<HomeFurnishing>>() {
                    @Override
                    public void call(ResponseBody<HomeFurnishing> responseBody) {
                        if (responseBody.isSuccess()) {
                            if (responseBody.data != null) shareData = responseBody.data.shareData;
                            updateView(responseBody.data);
                        } else
                            Tst.showToast(responseBody.desc);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Tst.showToast(throwable.getMessage());
                    }
                });
    }

    private void updateCollectState() {
        if (isChoose) {
            imgCollect.setImageResource(R.drawable.shou_cang_on);
        } else {
            imgCollect.setImageResource(R.drawable.shou_cang_off);
        }
    }

    Subscription subscriptionMsgCount;

    @Override
    protected void onResume() {
        super.onResume();
        if (subscriptionMsgCount != null) subscriptionMsgCount.unsubscribe();
        subscriptionMsgCount = IMHelper.getInstance().isUnreadMsg().subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean aBoolean) {
                if (aBoolean)
                    ((ImageView) findView(R.id.information)).setImageResource(R.drawable.home_03);
                else ((ImageView) findView(R.id.information)).setImageResource(R.drawable.home_12);
            }
        });
    }
    @Subscribe(tags = {@Tag(BusAction.ACTION_LOGIN)})
    public void loginNotify(User user) {
        reqApi();
    }
    @Override
    protected void onDestroy() {
        if (subscriptionMsgCount != null) subscriptionMsgCount.unsubscribe();
        super.onDestroy();
    }
}
