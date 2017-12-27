package com.knms.activity;

import android.app.Dialog;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.knms.activity.base.HeadDetailNotifyBaseActivity;
import com.knms.activity.im.ChatActivity;
import com.knms.activity.login.FasterLoginActivity;
import com.knms.adapter.AutoBrowseAdapter;
import com.knms.adapter.CustomFurnitureDetailsAdapter;
import com.knms.adapter.baserecycler.BaseQuickAdapter;
import com.knms.adapter.baserecycler.BaseViewHolder;
import com.knms.android.R;
import com.knms.bean.ResponseBody;
import com.knms.bean.customfurniture.CustomDetail;
import com.knms.bean.other.Pic;
import com.knms.bean.user.User;
import com.knms.core.im.msg.Product;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.net.HttpConstant;
import com.knms.net.ReqApi;
import com.knms.net.RxApiService;
import com.knms.net.RxRequestApi;
import com.knms.oncall.AsyncHttpCallBack;
import com.knms.util.DialogHelper;
import com.knms.util.ImageLoadHelper;
import com.knms.util.SPUtils;
import com.knms.util.Tst;
import com.knms.view.CircleImageView;
import com.knms.view.clash.FullyGridLayoutManager;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.shareuzi.bean.ShareEntity;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.knms.android.R.raw.msg;

/**
 * Created by Administrator on 2016/8/23.
 * 定制家具详情
 */
public class CustomFurnitureDetailsActivity extends HeadDetailNotifyBaseActivity implements View.OnClickListener {
    private TextView mTitle, mBrowseAmount, mContent, mTypeOfService, mShopName;
    private CircleImageView mShopHeadPortrait;
    private LinearLayout mShowDialog;
    private RelativeLayout gotoShop;
    private ViewPager auto_vp;
    private AutoBrowseAdapter adapter_auto;
    private TextView tv_num;

    private LinearLayout mCollect, mShare;
    private TextView mCollectAmount, mContactFactory;
    private ImageView imgCollect;

    private ListView mListView;
    private CustomFurnitureDetailsAdapter mAdapter;
    private ArrayList<CustomDetail.Service> listItem = new ArrayList<CustomDetail.Service>();
    private boolean isChoose = false;

    private String id;//商品id
    private String shopId;
    CustomDetail detail;
    private int collectNum = 0;
    private RecyclerView mServiceRecyclerview;
    private PullToRefreshScrollView pullToRefreshScrollView;

    @Override
    protected void getParmas(Intent intent) {
        id = intent.getStringExtra("id");
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_custom_fruniture_details;
    }

    @Override
    protected void initView() {
        mServiceRecyclerview = findView(R.id.rv_custom_service);
        mShopHeadPortrait = findView(R.id.shop_head_portrait);
        mTitle = findView(R.id.title);
        mBrowseAmount = findView(R.id.browse_amount);
        mContent = findView(R.id.content);
        mTypeOfService = findView(R.id.type_of_service);
        mShopName = findView(R.id.shop_name);
        mShowDialog = findView(R.id.show_dialog);
        gotoShop = findView(R.id.goto_shop);
        mCollect = findView(R.id.collect);
        mShare = findView(R.id.share);
        imgCollect = findView(R.id.img_collect);
        mCollectAmount = findView(R.id.collect_amount);
        mContactFactory = findView(R.id.contact_factory);
        auto_vp = findView(R.id.vp_detail_img);
        tv_num = findView(R.id.tv_num);
        pullToRefreshScrollView= (PullToRefreshScrollView) findViewById(R.id.pull_to_refresh_scrollview);
        pullToRefreshScrollView.setMode(PullToRefreshBase.Mode.BOTH);
    }

    @Override
    protected void initData() {
        mShowDialog.setOnClickListener(this);
        gotoShop.setOnClickListener(this);
        mCollect.setOnClickListener(this);
        mShare.setOnClickListener(this);
        mContactFactory.setOnClickListener(this);
        adapter_auto = new AutoBrowseAdapter(CustomFurnitureDetailsActivity.this,new ArrayList<Pic>());
        auto_vp.setAdapter(adapter_auto);
        auto_vp.setOffscreenPageLimit(3);
        auto_vp.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                tv_num.setText(position + 1 + "/" + adapter_auto.getCount());
            }
            @Override
            public void onPageSelected(int position) { }
            @Override
            public void onPageScrollStateChanged(int state) { }
        });

        reqApi();
    }

    @Override
    public String setStatisticsTitle() {
        return "定制家具详情";
    }

    ShareEntity shareData;

    @Override
    protected void reqApi() {
//        showProgress();
        RxRequestApi.getInstance().getApiService().getCustomizedDetail(id)
                .compose(this.<ResponseBody<CustomDetail>>applySchedulers())
                .subscribe(new Action1<ResponseBody<CustomDetail>>() {
                    @Override
                    public void call(ResponseBody<CustomDetail> body) {
//                        hideProgress();
                        if (body.isSuccess()) {
                            if (body.data != null) shareData = body.data.shareData;
                            listItem.addAll(body.data.serviceList);
                            updateView(body.data);
                            detail = body.data;
                        } else
                            Tst.showToast(body.desc);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
//                        Tst.showToast(throwable.toString());
                        hideProgress();
                    }
                });
    }

    private void updateView(CustomDetail data) {
        if (data.serviceList == null || data.serviceList.size() == 0) {
            mShowDialog.setVisibility(View.GONE);
        } else {
            mServiceRecyclerview.setLayoutManager(new FullyGridLayoutManager(this, 3));
            data.serviceList = data.serviceList.size() > 3 ? data.serviceList.subList(0, 3) : data.serviceList;
            mServiceRecyclerview.setAdapter(new BaseQuickAdapter<CustomDetail.Service>(R.layout.item_custom_detail_service, data.serviceList) {
                @Override
                protected void convert(BaseViewHolder helper, CustomDetail.Service item) {
                    ImageLoadHelper.getInstance().displayImage(CustomFurnitureDetailsActivity.this,item.sephoto, (ImageView) helper.getView(R.id.service_img));
                    helper.setText(R.id.type_of_service, item.sename);
                    helper.setOnClickListener(R.id.ll_custom_service, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            showPopDialog();
                        }
                    });
                }
            });
        }
        collectNum = data.collectNumber;
        mTitle.setText(data.cotitle);
        mBrowseAmount.setText(data.browseNumber == 0 ? "浏览" : data.browseNumber + "次浏览");
        mContent.setText(data.coremark);
        mShopName.setText(data.shopName);
        mCollectAmount.setText("收藏" + (data.collectNumber == 0 ? "" : data.collectNumber));
        ImageLoadHelper.getInstance().displayImageHead(CustomFurnitureDetailsActivity.this,data.shopPhoto, mShopHeadPortrait);
        if (0 == data.iscollectNumber) {
            imgCollect.setImageResource(R.drawable.shou_cang_on);
            isChoose = true;
        } else {
            imgCollect.setImageResource(R.drawable.shou_cang_off);
            isChoose = false;
        }
        adapter_auto = new AutoBrowseAdapter(CustomFurnitureDetailsActivity.this,data.imglist);
        auto_vp.setAdapter(adapter_auto);
        shopId = data.shopid;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.show_dialog:
                showPopDialog();
                break;
            case R.id.goto_shop:
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("shopId", shopId);
                params.put("position", 2);
                startActivityAnimGeneral(ShopActivityF.class, params);
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
            case R.id.contact_factory:
                if (!SPUtils.isLogin()) {
                    startActivityAnimGeneral(FasterLoginActivity.class, null);
                    break;
                }
                if (detail == null) return;
                Map<String, Object> parmas = new HashMap<>();
                parmas.put("shopId",detail.shopid);
                parmas.put("sid", detail.ssmerchantid);
                Product product = new Product();
                product.content = detail.coremark;
                product.icon = (detail.imglist != null && detail.imglist.size() > 0) ? detail.imglist.get(0).url : "";
                product.price = "";
                product.productType = "3";
                product.productId = detail.inid;
                parmas.put("prodcut", product);
                startActivityAnimGeneral(ChatActivity.class, parmas);
                break;
        }
    }
    @Subscribe(tags = {@Tag(BusAction.ACTION_LOGIN)})
    public void loginNotify(User user) {
        reqApi();
    }
    private void showPopDialog() {
        DialogHelper.showBottomDialog(this, R.layout.dialog_custom_furniture_details, new DialogHelper.OnEventListener<Dialog>() {
            @Override
            public void eventListener(View parentView, final Dialog window) {
                mAdapter = new CustomFurnitureDetailsAdapter(CustomFurnitureDetailsActivity.this, listItem);
                mListView = (ListView) parentView.findViewById(R.id.listview);
                mListView.setAdapter(mAdapter);
                parentView.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        window.dismiss();
                    }
                });
            }
        });
    }
}
