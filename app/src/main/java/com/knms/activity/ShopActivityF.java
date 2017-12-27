package com.knms.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.knms.activity.base.BaseFragmentActivity;
import com.knms.activity.comment.CommentListActivity;
import com.knms.activity.im.ChatActivity;
import com.knms.activity.im.MessageCenterActivity;
import com.knms.activity.login.FasterLoginActivity;
import com.knms.activity.main.MainActivity;
import com.knms.adapter.BaseFragmentAdapter;
import com.knms.adapter.CouponAdapter;
import com.knms.adapter.base.CommonAdapter;
import com.knms.adapter.base.ViewHolder;
import com.knms.android.R;
import com.knms.app.KnmsApp;
import com.knms.bean.ResponseBody;
import com.knms.bean.other.Coupons;
import com.knms.bean.shop.Shop;
import com.knms.core.im.IMHelper;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.fragment.ShopCommodityFragment;
import com.knms.fragment.ShopCustomFurnitureFragment;
import com.knms.fragment.ShopDecorationStyleFragment;
import com.knms.fragment.base.BaseFragment;
import com.knms.net.HttpConstant;
import com.knms.net.ReqApi;
import com.knms.net.RxRequestApi;
import com.knms.oncall.AsyncHttpCallBack;
import com.knms.oncall.LoadListener;
import com.knms.other.BestBlurOnSubscribe;
import com.knms.other.BitmapFromUrlSubscribe;
import com.knms.util.DialogHelper;
import com.knms.util.ImageLoadHelper;
import com.knms.util.LocalDisplay;
import com.knms.util.SPUtils;
import com.knms.util.ScreenUtil;
import com.knms.util.Tst;
import com.knms.view.CircleImageView;
import com.knms.view.PagerSlidingTabStrip;
import com.knms.view.listview.MaxHightListView;
import com.knms.view.sticky.HeaderScrollHelper;
import com.knms.view.sticky.HeaderViewPager;
import com.knms.view.tv.ExpandTextView;
import com.umeng.analytics.MobclickAgent;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/8/23.
 * 店铺
 */
public class ShopActivityF extends BaseFragmentActivity implements View.OnClickListener {
    private ImageView mImgBackground, mImgCall;
    private CircleImageView mShopHeadPortrait;
    private TextView mShopName, mCollectAmount, imgCollect;
    private LinearLayout mLayoutNotice, couponLayout, addCouponLayout;
//    private TextView mNoticeContent;
    private ExpandTextView expandTextView;
    private ImageView mGetCoupons, ivArrow;
    private PagerSlidingTabStrip tabStrip;
    private ViewPager mViewPager;
    private BaseFragmentAdapter mShopFragmentAdapter;
    private List<BaseFragment> fragments;

    private ArrayList<Coupons> couponsList;

    private boolean isChoose = false;
    private String shopId, sid;//店铺id
    private int position, collectNum = 0;//确定是商品、家装风格、定制家具
    private CouponAdapter adapter;
    private RelativeLayout rl_status, rl_border;
    private ImageButton btnTop;
    private TextView tvAddress;

    private Shop mShop;
    private List<String> phoneNumberList = new ArrayList<>();
    private TextView tvTitle;
    private HeaderViewPager scrollableLayout;

    public ImageView ivBtnBack, iconHeadToHome, iconHeadMsgTip;

    @Override
    protected void getParmas(Intent intent) {
        shopId = intent.getStringExtra("shopId");
        position = intent.getIntExtra("position", 0);
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_shop_new;
    }

    @Override
    protected void initView() {
        rl_border = findView(R.id.rl_border);
        scrollableLayout = findView(R.id.headviewpager);
//        stickyScrollView.setStickTop(LocalDisplay.dp2px(46));
        ivArrow = findView(R.id.iv_arrow);
        tvTitle = findView(R.id.tv_title_center);
        tvTitle.setVisibility(View.GONE);
        btnTop = findView(R.id.btn_top);
        mImgBackground = findView(R.id.img_background);
        mShopHeadPortrait = findView(R.id.shop_head_portrait);
        mShopName = findView(R.id.shop_name);
        mCollectAmount = findView(R.id.collect_amount);
        imgCollect = findView(R.id.is_collect);
        mLayoutNotice = findView(R.id.layout_notice);
//        mNoticeContent = findView(R.id.notice_content);
        expandTextView = findView(R.id.expand_text_view);
        mGetCoupons = findView(R.id.get_coupons);
        mViewPager = findView(R.id.id_stickynavlayout_viewpager);
        tabStrip = findView(R.id.id_stickynavlayout_indicator);
        couponLayout = (LinearLayout) findViewById(R.id.layout_coupon);
        addCouponLayout = (LinearLayout) findViewById(R.id.add_coupons);
        rl_status = (RelativeLayout) findViewById(R.id.rl_status);
        tvAddress = (TextView) findViewById(R.id.tv_store_address);
        mImgCall = (ImageView) findViewById(R.id.iv_call_store);
        iconHeadMsgTip = findView(R.id.information);
        ivBtnBack = findView(R.id.iv_back);
        iconHeadToHome = findView(R.id.home_page);
        iconHeadMsgTip = findView(R.id.information);
        init();

    }

    private void init() {
        tabStrip.setAllCaps(false);
        tabStrip.setIndicatorColor(getResources().getColor(R.color.tab_line));
        tabStrip.setIndicatorHeight(LocalDisplay.dp2px(1));

        scrollableLayout.setOnScrollListener(new HeaderViewPager.OnScrollListener() {
            @Override
            public void onScroll(int currentY, int maxY) {
                float alpha = 1.0f * currentY / LocalDisplay.dp2px(200 - 46);
                Log.e("alpha", alpha + "");
                rl_border.setAlpha(alpha);
                tvTitle.setVisibility(alpha >= 1 ? View.VISIBLE : View.GONE);
                ((ImageView) findView(R.id.iv_back)).setImageResource(alpha >= 0.5 ? R.drawable.back_b : R.drawable.icon_details_top_back);
                ((ImageView) findView(R.id.home_page)).setImageResource(alpha >= 0.5 ? R.drawable.home2 : R.drawable.home_07);
                ((ImageView) findView(R.id.information)).setImageResource(alpha >= 0.5 ? (isMsg ? R.drawable.home_03 : R.drawable.home_12) : (isMsg ? R.drawable.home_05 : R.drawable.home_14));
            }
        });
    }

    @Override
    protected void initData() {
        btnTop.setOnClickListener(this);
        mGetCoupons.setOnClickListener(this);
        imgCollect.setOnClickListener(this);
        addCouponLayout.setOnClickListener(this);
        mImgCall.setOnClickListener(this);
        findView(R.id.rlBtn_comment).setOnClickListener(this);
        findView(R.id.contact_service).setOnClickListener(this);
        ivBtnBack.setOnClickListener(this);
        iconHeadMsgTip.setOnClickListener(this);
        iconHeadToHome.setOnClickListener(this);

        fragments = new ArrayList<BaseFragment>();
        fragments.add(ShopCommodityFragment.newInstance(shopId));
        fragments.add(ShopDecorationStyleFragment.newInstance(shopId));
        fragments.add(ShopCustomFurnitureFragment.newInstance(shopId));
        mShopFragmentAdapter = new BaseFragmentAdapter(getSupportFragmentManager(), fragments);
        mViewPager.setAdapter(mShopFragmentAdapter);
        tabStrip.setViewPager(mViewPager);
        tabStrip.setShouldExpand(true);
        mViewPager.setOffscreenPageLimit(1);
        if (position < 3) {
            mViewPager.setCurrentItem(position);
        } else {
            mViewPager.setCurrentItem(0);
        }
        scrollableLayout.setCurrentScrollableContainer((HeaderScrollHelper.ScrollableContainer) fragments.get(position));
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                scrollableLayout.setCurrentScrollableContainer((HeaderScrollHelper.ScrollableContainer) fragments.get(position));
            }
        });
        reqApi();
    }

    @Override
    public String setStatisticsTitle() {
        return "店铺";
    }

    public Subscription subscriptionMsgCount;
    public boolean isMsg = false;

    @Override
    protected void onResume() {
        super.onResume();
        View top_layout = findView(R.id.top_layout);
        scrollableLayout.setTopOffset(LocalDisplay.getViewSize(top_layout)[1]);

        if (subscriptionMsgCount != null) subscriptionMsgCount.unsubscribe();
        subscriptionMsgCount = IMHelper.getInstance().isUnreadMsg().subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean aBoolean) {
                isMsg = aBoolean;
                if (ContextCompat.getDrawable(ShopActivityF.this, R.drawable.home_05).getConstantState().equals(iconHeadMsgTip.getDrawable().getCurrent().getConstantState())
                        || ContextCompat.getDrawable(ShopActivityF.this, R.drawable.home_14).getConstantState().equals(iconHeadMsgTip.getDrawable().getCurrent().getConstantState())) {
                    if (aBoolean) {
                        iconHeadMsgTip.setImageResource(R.drawable.home_05);
                    } else {
                        iconHeadMsgTip.setImageResource(R.drawable.home_14);
                    }
                } else {
                    if (aBoolean) {
                        iconHeadMsgTip.setImageResource(R.drawable.home_03);
                    } else {
                        iconHeadMsgTip.setImageResource(R.drawable.home_12);
                    }
                }
            }
        });

    }

    @Override
    protected void reqApi() {
        showProgress();
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("shopId", shopId);
        ReqApi.getInstance().postMethod(HttpConstant.shopDetail, params, new AsyncHttpCallBack<Shop>() {
            @Override
            public void onSuccess(ResponseBody<Shop> body) {
                hideProgress();
//                refreshStickyNavLayout.onRefreshComplete();
                if (body.isSuccess()) {
                    sid = body.data.ssmerchantid;
                    updateView(body.data);
                } else {
                    if (body.code.equals("5")) {
                        rl_border.setAlpha(1);  //去首页逛逛
                        KnmsApp.getInstance().showDataEmpty(rl_status, "店铺消失了", R.drawable.no_data_5, "去逛逛", new LoadListener() {
                            @Override
                            public void onclick() {
                                Map<String, Object> param = new HashMap<String, Object>();
                                param.put("source", "mHomePage");
                                startActivityAnimGeneral(MainActivity.class, param);
                                finshActivity();
                            }
                        });
                        return;
                    }
                    Tst.showToast(body.desc);
                }
            }

            @Override
            public void onFailure(String msg) {
                hideProgress();
//                refreshStickyNavLayout.onRefreshComplete();
                Tst.showToast(msg);
            }

            @Override
            public Type setType() {
                return new TypeToken<ResponseBody<Shop>>() {
                }.getType();
            }
        });

    }

    private void updateView(Shop data) {
        this.mShop = data;
        if (!TextUtils.isEmpty(mShop.name))
            tvTitle.setText(mShop.name.length() > 6 ? mShop.name.substring(0, 6) + "..." : mShop.name);
        if (!TextUtils.isEmpty(data.phone)) phoneNumberList.add(data.phone);
        if (!TextUtils.isEmpty(data.shopPhone)) phoneNumberList.add(data.shopPhone);
        tvAddress.setText(data.ssaddress);
        mShopName.setText(data.name);
        ivArrow.setVisibility(data.commentCount == 0 ? View.GONE : View.VISIBLE);
        ((TextView) findView(R.id.tv_shop_comment)).setText("用户评价" + "(" + data.commentCount + ")");
        collectNum = Integer.parseInt(data.collectNumber.equals("") ? "0" : data.collectNumber);
        mCollectAmount.setText(collectNum + "人收藏");
        couponsList = (ArrayList<Coupons>) data.preferList;

        if (couponsList == null || couponsList.size() == 0)
            couponLayout.setVisibility(View.GONE);
        else {
            View view = null;
            addCouponLayout.removeAllViews();
            for (int i = 0; i < couponsList.size(); i++) {
                view = getLayoutInflater().inflate(R.layout.coupons_txt, null);
                TextView textView = (TextView) view.findViewById(R.id.coupons_name);
                textView.setText("满" + couponsList.get(i).spconditions + "减" + couponsList.get(i).spmoney);
                view.setLayoutParams(new ViewGroup.LayoutParams(addCouponLayout.getWidth() / 3, ViewGroup.LayoutParams.WRAP_CONTENT));

                addCouponLayout.addView(view);
            }
        }
        if (data.mark == null || data.mark.equals(""))
            mLayoutNotice.setVisibility(View.GONE);
        else
//            mNoticeContent.setText(data.mark);
        expandTextView.setText(data.mark);
//        ImageLoadHelper.getInstance().displayImage(data.photo, mImgBackground);
        showImg(data.photo, mImgBackground);
        ImageLoadHelper.getInstance().displayImageHead(ShopActivityF.this, data.logo, mShopHeadPortrait);
        if ("0".equals(data.iscollect)) {
            Drawable img = getResources().getDrawable(R.drawable.shoucang);
            // 调用setCompoundDrawables时，必须调用Drawable.setBounds()方法,否则图片不显示
            img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
            imgCollect.setCompoundDrawables(img, null, null, null);
            imgCollect.setText("已添收藏");
            isChoose = true;
        } else {
            Drawable img = getResources().getDrawable(R.drawable.shoucang2);
            img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
            imgCollect.setCompoundDrawables(img, null, null, null);
            imgCollect.setText("收藏店铺");
            isChoose = false;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_call_store://联系电话
                MobclickAgent.onEvent(this, "clickShopCallPhone");
                DialogHelper.showBottomDialog(this, R.layout.dialog_dial_layout, new DialogHelper.OnEventListener<Dialog>() {
                    @Override
                    public void eventListener(View parentView, final Dialog window) {
                        MaxHightListView listView = (MaxHightListView) parentView.findViewById(R.id.listview_phone_number);
                        //Context context,int resId, List<T> mDatas
                        listView.setAdapter(new CommonAdapter<String>(ShopActivityF.this, R.layout.item_phone_number, phoneNumberList) {
                            @Override
                            public void convert(ViewHolder helper, final String data) {
                                helper.setText(R.id.tv_phone_number, data);
                                helper.getView(R.id.tv_phone_number).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intentPhone = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + data));
                                        startActivity(intentPhone);
                                    }
                                });
                            }
                        });
                        listView.setMaxHeight(LocalDisplay.dip2px(240));
                        parentView.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                window.dismiss();
                            }
                        });
                    }
                });
                break;
            case R.id.contact_service:
                if (!SPUtils.isLogin()) {
                    startActivityAnimGeneral(FasterLoginActivity.class, null);
                    return;
                }
                Map<String, Object> parmas = new HashMap<>();
                parmas.put("sid", sid);
                parmas.put("shopId", shopId);
                startActivityAnimGeneral(ChatActivity.class, parmas);
                break;
            case R.id.add_coupons:
            case R.id.get_coupons:
                DialogHelper.showBottomDialog(this, R.layout.dialog_coupon, new DialogHelper.OnEventListener<Dialog>() {
                    @Override
                    public void eventListener(View parentView, final Dialog window) {
                        adapter = new CouponAdapter(ShopActivityF.this, couponsList);
                        ((ListView) parentView.findViewById(R.id.list_view)).setAdapter(adapter);
                        parentView.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                window.dismiss();
                            }
                        });
                    }
                });
                break;
            case R.id.is_collect:
                if (!SPUtils.isLogin()) {
                    startActivityAnimGeneral(FasterLoginActivity.class, null);
                    return;
                }
                int type = isChoose ? 1 : 0;//0：收藏 1：取消收藏
                RxRequestApi.getInstance().getApiService().collect(shopId, 2, type)
                        .observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
                        .subscribe(new Action1<ResponseBody>() {
                            @Override
                            public void call(ResponseBody body) {
                                if (body.isSuccess()) {
                                    if (isChoose) {//之前已收藏了
                                        Drawable img = ContextCompat.getDrawable(ShopActivityF.this,R.drawable.shoucang2);
                                        img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
                                        imgCollect.setCompoundDrawables(img, null, null, null);
                                        imgCollect.setText("收藏店铺");
                                        isChoose = false;
                                        --collectNum;
                                    } else {//之前没收藏了
                                        Drawable img = ContextCompat.getDrawable(ShopActivityF.this,R.drawable.shoucang);
                                        img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
                                        imgCollect.setCompoundDrawables(img, null, null, null);
                                        imgCollect.setText("已添收藏");
                                        isChoose = true;
                                        ++collectNum;
                                    }
                                    RxBus.get().post(BusAction.CANCEL_SHOP_COLLECTION, isChoose);
                                    mCollectAmount.setText(collectNum + "人收藏");
                                } else {
                                    if ("已经收藏".equals(body.desc)){
                                        reqApi();//刷新列表
                                    }else if(!"需要登录".equals(body.desc)){
                                        Tst.showToast(body.desc);
                                    }
                                }
                            }
                        });
                break;
            case R.id.btn_top:
                if (mShopFragmentAdapter.getItem(mViewPager.getCurrentItem()) instanceof ShopCommodityFragment) {
                    ((ShopCommodityFragment) mShopFragmentAdapter.getItem(mViewPager.getCurrentItem())).scrollTo();
                } else if (mShopFragmentAdapter.getItem(mViewPager.getCurrentItem()) instanceof ShopDecorationStyleFragment) {
                    ((ShopDecorationStyleFragment) mShopFragmentAdapter.getItem(mViewPager.getCurrentItem())).scrollTo();
                } else if (mShopFragmentAdapter.getItem(mViewPager.getCurrentItem()) instanceof ShopCustomFurnitureFragment) {
                    ((ShopCustomFurnitureFragment) mShopFragmentAdapter.getItem(mViewPager.getCurrentItem())).scrollTo();
                }
//                refreshStickyNavLayout.getRefreshableView().scrollTo(0, 0);
                break;
            case R.id.rlBtn_comment:
                MobclickAgent.onEvent(this, "clickShopFromCommentLabel");
                if (mShop != null && mShop.commentCount > 0) {
                    Map<String, Object> params = new HashMap<>();
                    params.put("shopId", shopId);
                    params.put("totalCount", mShop.commentCount);
                    startActivityAnimGeneral(CommentListActivity.class, params);
                }
                break;
            case R.id.iv_back:
                finshActivity();
                break;
            case R.id.home_page:
                Map<String, Object> p = new HashMap<>();
                p.put("source", "mHomePage");
                startActivityAnimGeneral(MainActivity.class, p);
                break;
            case R.id.information:
                if (SPUtils.isLogin()) {
                    startActivityAnimGeneral(MessageCenterActivity.class, null);
                } else {
                    startActivityAnimGeneral(FasterLoginActivity.class, null);
                }
                break;
        }
    }

    private void showImg(final String url, final ImageView imgView) {
        Observable.create(new BitmapFromUrlSubscribe(url, ScreenUtil.getScreenWidth(), LocalDisplay.dp2px(200)))
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .flatMap(new Func1<Bitmap, Observable<Bitmap>>() {
                    @Override
                    public Observable<Bitmap> call(Bitmap bitmap) {
                        return Observable.create(new BestBlurOnSubscribe(ShopActivityF.this, bitmap, 15, 0.2f))
                                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
                    }
                })
                .subscribe(new Action1<Bitmap>() {
                    @Override
                    public void call(Bitmap bitmap) {
                        imgView.setImageBitmap(bitmap);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        ImageLoadHelper.getInstance().displayImage(ShopActivityF.this, url, imgView);
                    }
                });

    }
}
