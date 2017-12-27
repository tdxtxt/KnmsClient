package com.knms.activity;

import android.content.Intent;
import android.graphics.Paint;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.knms.activity.base.HeadDetailNotifyBaseActivity;
import com.knms.adapter.AutoBrowseAdapter;
import com.knms.android.R;
import com.knms.bean.ResponseBody;
import com.knms.bean.myidle.MyIdle;
import com.knms.bean.user.User;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.net.RxRequestApi;
import com.knms.util.CommonUtils;
import com.knms.util.DialogHelper;
import com.knms.util.ImageLoadHelper;
import com.knms.util.SPUtils;
import com.knms.util.StrHelper;
import com.knms.util.Tst;

import java.util.HashMap;
import java.util.Map;

import cn.sharesdk.onekeyshare.OnekeyShare;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/9/5.
 * 下架详情
 */
public class UndercarriageDetailsActivity extends HeadDetailNotifyBaseActivity implements View.OnClickListener {
    private ViewPager vp_detail_img;
    private AutoBrowseAdapter adapter_auto;

    private String id;//我的闲置商品id
    private MyIdle myidle;
    private TextView tvCurrentPrice, tvCostPrice, tvContent, tvCollectAmount, tvBrowseAmount, tvVendorName,
            tvReleaseTime, tvCity, onOffer_edit_idle, soldOut_edit_idle, deleteIdle, putawayIdle, tvNumber, tvFreight
            ,tvDelete;
    private ImageView vendorHead;
    private LinearLayout layoutOnOffer, layoutSoldOut;
    private LinearLayout undercarriageLayout, shareLayout;
    private PullToRefreshScrollView pullToRefreshScrollView;

    @Override
    protected void getParmas(Intent intent) {
        id = intent.getStringExtra("id");
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_my_idle_details;
    }

    @Override
    protected void initView() {
        tvDelete= (TextView) findViewById(R.id.tv_delete);
        vp_detail_img = findView(R.id.ider_viewpager);
        tvCurrentPrice = (TextView) findViewById(R.id.current_price);
        tvCostPrice = (TextView) findViewById(R.id.cost_price);
        tvContent = (TextView) findViewById(R.id.content);
        tvCollectAmount = (TextView) findViewById(R.id.collect_amount);
        tvBrowseAmount = (TextView) findViewById(R.id.browse_amount);
        tvVendorName = (TextView) findViewById(R.id.vendor_name);
        tvReleaseTime = (TextView) findViewById(R.id.release_time);
        tvCity = (TextView) findViewById(R.id.city);
        tvNumber = (TextView) findViewById(R.id.img_page_size);
        vendorHead = (ImageView) findViewById(R.id.vendor_head_portrait);
        onOffer_edit_idle = (TextView) findViewById(R.id.tv_txt);
        undercarriageLayout = (LinearLayout) findViewById(R.id.undercarriage);
        shareLayout = (LinearLayout) findViewById(R.id.share);
        soldOut_edit_idle = (TextView) findViewById(R.id.edit_idle);
        deleteIdle = (TextView) findViewById(R.id.delete_idle);
        putawayIdle = (TextView) findViewById(R.id.reshelf);
        layoutOnOffer = (LinearLayout) findViewById(R.id.layout_on_offer);
        layoutSoldOut = (LinearLayout) findViewById(R.id.layout_sold_out);
        tvCostPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        tvFreight = (TextView) findViewById(R.id.freight);
        pullToRefreshScrollView= (PullToRefreshScrollView) findViewById(R.id.pull_to_refresh_scrollview);
        pullToRefreshScrollView.setMode(PullToRefreshBase.Mode.BOTH);
    }

    @Override
    protected void initData() {
        undercarriageLayout.setOnClickListener(this);
        shareLayout.setOnClickListener(this);
        onOffer_edit_idle.setOnClickListener(this);
        soldOut_edit_idle.setOnClickListener(this);
        deleteIdle.setOnClickListener(this);
        putawayIdle.setOnClickListener(this);
        tvDelete.setOnClickListener(this);
        reqApi();

    }

    @Override
    public String setStatisticsTitle() {
        return "闲置详情";
    }

    @Override
    protected void reqApi() {
        RxRequestApi.getInstance().getApiService().idleDetails(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<MyIdle>>() {
                    @Override
                    public void call(ResponseBody<MyIdle> idleDetailsResponseBody) {
                        if (idleDetailsResponseBody.isSuccess())
                            updateView(idleDetailsResponseBody.data);
                        else Tst.showToast(idleDetailsResponseBody.desc);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Tst.showToast(throwable.getMessage());
                    }
                });
    }

    private void updateView(final MyIdle details) {
        myidle = details;
        //0、2：出售 1：下架
        if (details.coState == 0 || details.coState == 2) {
            layoutOnOffer.setVisibility(View.VISIBLE);
            layoutSoldOut.setVisibility(View.GONE);
        } else if (details.coState == 3) {
            layoutOnOffer.setVisibility(View.GONE);
            layoutSoldOut.setVisibility(View.VISIBLE);
        }else if(details.coState==1){
            tvDelete.setText("删除");
            tvDelete.setVisibility(View.VISIBLE);
            layoutOnOffer.setVisibility(View.GONE);
            layoutSoldOut.setVisibility(View.GONE);
        }else if(details.coState==4){
            tvDelete.setText("已删除");
            tvDelete.setEnabled(false);
        }
        //是否包邮
        if (details.isfreeshop == 0)
            tvFreight.setText("包邮");
        else if (details.freeshopprice == 0)
            tvFreight.setText("运费待议");
        else
            tvFreight.setText("运费¥" + details.freeshopprice);
        User user = SPUtils.getUser();
        adapter_auto = new AutoBrowseAdapter(this,details.imglist);
        vp_detail_img.setAdapter(adapter_auto);
        vp_detail_img.setOffscreenPageLimit(3);
        tvCurrentPrice.setText(CommonUtils.keepTwoDecimal(details.goprice));
        tvCostPrice.setText(details.orprice<0? "" : CommonUtils.keepTwoDecimal(details.orprice));
        tvContent.setText(details.coremark);
        tvCollectAmount.setText(details.collectNumber + "");
        tvBrowseAmount.setText(details.browseNumber + "");
        tvVendorName.setText(user.nickname);
        tvReleaseTime.setText(StrHelper.displayTime(details.goreleasetime, true, true));
        ImageLoadHelper.getInstance().displayImage(UndercarriageDetailsActivity.this,user.uicon, vendorHead);
        tvCity.setText(details.goareaname);

        vp_detail_img.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                tvNumber.setText(position + 1 + "/" + details.imglist.size());
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


    @Override
    public void onClick(View v) {
        Map<String, Object> param = new HashMap<>();
        param.put("idle", myidle);
        switch (v.getId()) {
            case R.id.undercarriage:
                showDialo(3);
                break;
            case R.id.share:
                OnekeyShare oks = new OnekeyShare();
                oks.show(this, myidle.shareData);
                break;
            case R.id.tv_txt:
                startActivityAnimGeneral(ReleaseIdleActivity.class, param);
                finshActivity();
                break;
            case R.id.tv_delete:
            case R.id.delete_idle:
                showDialo(4);
                break;
            case R.id.edit_idle:
                startActivityAnimGeneral(ReleaseIdleActivity.class, param);
                finshActivity();
                break;
            case R.id.reshelf:
                showDialo(0);
                break;
        }
    }

    private void showDialo(final int type) {
        String title = "";
        if (type == 3) {
            title = "确定要下架吗?";
        } else if (type == 4) {
            title = "确定要删除吗?";
        } else if (type == 0) {
            title = "确定要重新上架吗?";
        }
        DialogHelper.showPromptDialog(this, "", title, "取消", null, "确定", new DialogHelper.OnMenuClick() {
            @Override
            public void onLeftMenuClick() {

            }

            @Override
            public void onCenterMenuClick() {

            }

            @Override
            public void onRightMenuClick() {
                updateIdelState(type);
            }
        });
    }

    private void updateIdelState(int type) {
        if (type == 0 && myidle.coState == 1) {
            Tst.showToast("系统下架只能删除");
            return;
        }
        showProgress();
        RxRequestApi.getInstance().getApiService().updateIdelState(myidle.goid, type)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody>() {
                    @Override
                    public void call(ResponseBody responseBody) {
                        hideProgress();
                        Tst.showToast(responseBody.desc);
                        if (responseBody.isSuccess()) {
                            RxBus.get().post(BusAction.REFRESH_IDLE, "");
                            finshActivity();
                        }

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        hideProgress();
                        Tst.showToast(throwable.getMessage());
                    }
                });
    }
    @Subscribe(tags = {@Tag(BusAction.ACTION_LOGIN)})
    public void loginAccout(User user) {
        finshActivity();
    }

}
