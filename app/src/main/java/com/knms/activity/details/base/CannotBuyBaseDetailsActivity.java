package com.knms.activity.details.base;

import android.app.Dialog;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.knms.activity.CommWebViewActivity;
import com.knms.activity.ShopActivityF;
import com.knms.activity.comment.CommentListActivity;
import com.knms.activity.details.ProductDetailsOrdinaryActivity;
import com.knms.activity.details.canbuy.ProductDetailsActivity;
import com.knms.activity.fastfind.NotifyMerchantActivity;
import com.knms.activity.im.ChatActivity;
import com.knms.activity.im.MessageCenterActivity;
import com.knms.activity.login.FasterLoginActivity;
import com.knms.activity.main.MainActivity;
import com.knms.adapter.AttrAdapter;
import com.knms.adapter.AutoBrowseAdapter;
import com.knms.adapter.CommentImgAdapter;
import com.knms.adapter.CouponAdapter;
import com.knms.adapter.RecommedProductBaokAdapter;
import com.knms.adapter.base.CommonAdapter;
import com.knms.adapter.base.ViewHolder;
import com.knms.adapter.baserecycler.BaseQuickAdapter;
import com.knms.adapter.baserecycler.BaseViewHolder;
import com.knms.android.R;
import com.knms.bean.Exists;
import com.knms.bean.ResponseBody;
import com.knms.bean.classification.ClassifyDetail;
import com.knms.bean.other.Coupons;
import com.knms.bean.product.ClassifyGood;
import com.knms.bean.user.User;
import com.knms.core.im.msg.Product;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.net.RxRequestApi;
import com.knms.oncall.RecyclerItemClickListener;
import com.knms.util.DialogHelper;
import com.knms.util.ImageLoadHelper;
import com.knms.util.LocalDisplay;
import com.knms.util.SPUtils;
import com.knms.util.StrHelper;
import com.knms.util.ToolsHelper;
import com.knms.util.Tst;
import com.knms.view.CircleImageView;
import com.knms.view.GuideView;
import com.knms.view.Star;
import com.knms.view.banner.AutoViewPager;
import com.knms.view.clash.FullyGridLayoutManager;
import com.knms.view.indicator.ScrollViewTabIndicator;
import com.knms.view.listview.MaxHightListView;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.shareuzi.bean.ShareEntity;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


/**
 * Created by Administrator on 2017/7/11.
 */

public abstract class CannotBuyBaseDetailsActivity extends BaseDetailsActivity implements View.OnClickListener{
    /*******头部view*******/
    RelativeLayout layoutHeadAlpha;
    ImageView ivBtnBack;
    ImageView iconHeadBbprice;
    ImageView iconHeadToHome;
    ImageView iconHeadMsgTip;
    /*******顶部view*******/
    ViewPager viewPagerBanner;
    TextView tvBannerPage;
    TextView tvDetailsTitle,tvDetailsContent,tvDetailsBrowserCount;
    LinearLayout layoutCoupons;
    LinearLayout layoutAddCoupon;
    RelativeLayout layoutService;
    RecyclerView recyclerViewService;
    RelativeLayout layoutShop;
    CircleImageView ivShopAvatar;
    TextView tvShopName;
    ImageView bbPrice;//比比货
    /*******悬浮view*******/
    ScrollViewTabIndicator indicatorOne;//第一个
    ScrollViewTabIndicator indicatorTwo;//第二个
    /*******尾部view*******/
    RecyclerView recyclerViewAttr;//商品参数
    View viewComment;//商品评论
    View viewRecommend;//商品推荐

    private RecyclerView recyclerViewRecomm;
    private TextView tv_comment_count;//用户评论数量
    private TextView tvLayout_no_comment;//暂无评论
    private LinearLayout layout_comment;//有评论
    private TextView tv_name;//名称
    private TextView tv_time;//评论时间
    private TextView tv_comment_content;//评论内容
    private ImageView iv_icon;//优质评论图片
    private Star ratingBar;
    private CircleImageView iv_avatar;//头像
    private RecyclerView recyclerViewImage;//评论图片
    private ImageView commentArrow;
    /*******底部菜单view*******/
    View viewCollect,viewShare,viewChat;
    ImageView iconCollect;
    TextView tvCollectCount;
    /*******adapter*******/
    AutoBrowseAdapter adapterBanner;
    AttrAdapter adapterAttr;
    RecommedProductBaokAdapter adapterRecommed;

    private int heightBanner = LocalDisplay.dp2px(360 - 46);
    private boolean sendBBprice = false;//没有发送过

    @Override
    public View loadHeadView(ViewStub viewStub) {
        viewStub.setLayoutResource(R.layout.toolbar_top_tab_details);
        if(viewHead == null) viewHead = viewStub.inflate();
        layoutHeadAlpha = (RelativeLayout) viewHead.findViewById(R.id.rl_border);
        ivBtnBack = (ImageView) viewHead.findViewById(R.id.iv_back);
        iconHeadBbprice = (ImageView) viewHead.findViewById(R.id.bbparity);
        iconHeadToHome = (ImageView) viewHead.findViewById(R.id.home_page);
        iconHeadMsgTip = (ImageView) viewHead.findViewById(R.id.information);

        layoutHeadAlpha.setAlpha(0f);
        return viewHead;
    }
    @Override
    public View loadTopView(ViewStub viewStub) {
        viewStub.setLayoutResource(R.layout.stub_details_top_cannot_buy);
        if(viewTop == null) viewTop = viewStub.inflate();
        viewPagerBanner = (ViewPager) viewTop.findViewById(R.id.goods_img_viewpager);
        if(viewPagerBanner instanceof AutoViewPager) ((AutoViewPager) viewPagerBanner).setAuto(false);
        tvBannerPage = (TextView) viewTop.findViewById(R.id.tv_num);
        tvDetailsTitle = (TextView) viewTop.findViewById(R.id.title);
        tvDetailsBrowserCount = (TextView) viewTop.findViewById(R.id.browse_amount);
        tvDetailsContent = (TextView) viewTop.findViewById(R.id.content);
        layoutCoupons = (LinearLayout) viewTop.findViewById(R.id.coupons_layout);
        layoutAddCoupon = (LinearLayout) viewTop.findViewById(R.id.add_coupons);
        layoutService = (RelativeLayout) viewTop.findViewById(R.id.rl_serviceUI);
        recyclerViewService = (RecyclerView) viewTop.findViewById(R.id.recyclerView_service);
        recyclerViewService.setLayoutManager(new FullyGridLayoutManager(this, 3));
        recyclerViewService.setNestedScrollingEnabled(false);
        layoutShop = (RelativeLayout) viewTop.findViewById(R.id.goto_shop);
        ivShopAvatar = (CircleImageView) viewTop.findViewById(R.id.shop_head_portrait);
        tvShopName = (TextView) viewTop.findViewById(R.id.shop_name);
        bbPrice = (ImageView) viewTop.findViewById(R.id.iv_fast_bb_goods);
        bbPrice.setVisibility(isBbprice() ? View.VISIBLE : View.INVISIBLE);
        return viewTop;
    }

    @Override
    public View[] loadStivkyView(ViewStub viewStubOne, ViewStub viewStubTwo) {
        viewStubOne.setLayoutResource(R.layout.stub_details_stivky);
        viewStubTwo.setLayoutResource(R.layout.stub_details_stivky);
        if(viewStivkyOne == null) viewStivkyOne = viewStubOne.inflate();
        if(viewStivkyTwo == null) viewStivkyTwo = viewStubTwo.inflate();
        viewStubOne.setVisibility(View.INVISIBLE);
        indicatorOne = (ScrollViewTabIndicator) viewStivkyOne.findViewById(R.id.indicator);
        indicatorTwo = (ScrollViewTabIndicator) viewStivkyTwo.findViewById(R.id.indicator);
        return new View[]{viewStivkyOne,viewStivkyTwo};
    }

    @Override
    public View loadFooterView(ViewStub viewStub) {
        viewStub.setLayoutResource(R.layout.stub_details_footer_cannot_buy);
        if(viewFooter == null) viewFooter = viewStub.inflate();
        viewComment = viewFooter.findViewById(R.id.view_comment);
        viewRecommend = viewFooter.findViewById(R.id.ll_recommend);

        commentArrow = (ImageView) viewFooter.findViewById(R.id.iv_arrow_view);

        recyclerViewAttr = (RecyclerView) viewFooter.findViewById(R.id.recyclerView_params);
        LinearLayoutManager layoutManagerAttr = new LinearLayoutManager(this);
        layoutManagerAttr.setSmoothScrollbarEnabled(true);
        layoutManagerAttr.setAutoMeasureEnabled(true);
        recyclerViewAttr.setLayoutManager(layoutManagerAttr);
        recyclerViewAttr.setHasFixedSize(true);
        recyclerViewAttr.setNestedScrollingEnabled(false);
        recyclerViewAttr.setFocusable(false);

        tv_comment_count = (TextView) viewFooter.findViewById(R.id.tv_comment_count);
        tv_time = (TextView) viewFooter.findViewById(R.id.tv_time);
        tv_name = (TextView) viewFooter.findViewById(R.id.tv_name);
        tvLayout_no_comment = (TextView) viewFooter.findViewById(R.id.tvLayout_no_comment);
        layout_comment = (LinearLayout) viewFooter.findViewById(R.id.layout_comment);
        ratingBar = (Star) viewFooter.findViewById(R.id.ratingBar);
        tv_comment_content = (TextView) viewFooter.findViewById(R.id.tv_comment_content);
        iv_avatar = (CircleImageView) viewFooter.findViewById(R.id.iv_avatar);
        iv_icon = (ImageView) viewFooter.findViewById(R.id.iv_icon);
        recyclerViewImage = (RecyclerView) viewFooter.findViewById(R.id.recyclerView);
        recyclerViewImage.setLayoutManager(new FullyGridLayoutManager(this,3));
        recyclerViewImage.setNestedScrollingEnabled(false);
        recyclerViewImage.setFocusable(false);

        recyclerViewRecomm = (RecyclerView) viewFooter.findViewById(R.id.recyclerView_recommend);
        GridLayoutManager layoutManagerRecomm = new GridLayoutManager(this, 2){
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        layoutManagerRecomm.setSmoothScrollbarEnabled(true);
        layoutManagerRecomm.setAutoMeasureEnabled(true);
        recyclerViewRecomm.setLayoutManager(layoutManagerRecomm);
        recyclerViewRecomm.setHasFixedSize(true);
        recyclerViewRecomm.setNestedScrollingEnabled(false);//解决滑动惯性问题
        recyclerViewRecomm.setFocusable(false);

        return viewFooter;
    }

    @Override
    public View loadMenuView(ViewStub viewStub) {
        viewStub.setLayoutResource(R.layout.stub_details_menu_cannot_buy);
        if(viewMenu == null) viewMenu = viewStub.inflate();
        viewCollect = viewMenu.findViewById(R.id.collect);
        viewShare = viewMenu.findViewById(R.id.share);
        viewChat = viewMenu.findViewById(R.id.contact_service);
        viewChat.setBackgroundColor(Color.parseColor("#FFD400"));
        iconCollect = (ImageView) viewMenu.findViewById(R.id.img_collect);
        tvCollectCount = (TextView) viewMenu.findViewById(R.id.collect_amount);
        ((TextView)viewChat).setText(getChatText());
        return viewMenu;
    }

    @Override
    protected void initView() {
        super.initView();
        List<String> names = new ArrayList<>();
        names.add("商品参数");
        names.add("用户评价");
        names.add("为你推荐");
        List<View> views = new ArrayList<>();
        views.add(recyclerViewAttr);
        views.add(viewComment);
        views.add(viewRecommend);
        indicatorTwo.setScrollView(scrollView,onScrollChangeListener,names,views);
        indicatorOne.setScrollView(scrollView,indicatorTwo,names,views);

        showGriade(viewChat);
    }

    private int[] oneLocation = new int[2];
    private int[] twoLocation = new int[2];
    NestedScrollView.OnScrollChangeListener onScrollChangeListener = new NestedScrollView.OnScrollChangeListener() {
        @Override
        public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
            indicatorOne.getLocationOnScreen(oneLocation);
            indicatorTwo.getLocationOnScreen(twoLocation);
            if (twoLocation[1] <= oneLocation[1]) {//未置顶
                viewStivkyOne.setVisibility(View.VISIBLE);
                viewStivkyTwo.setVisibility(View.INVISIBLE);
            } else {
                viewStivkyOne.setVisibility(View.INVISIBLE);
                viewStivkyTwo.setVisibility(View.VISIBLE);
            }
            if(scrollY <= 0){
                layoutHeadAlpha.setAlpha(0f);
                if(isBbprice()) bbPrice.setAlpha(1f);
            }else if (scrollY <= heightBanner) {//滑动过程中，渐变
                float scale = (float) scrollY / heightBanner;
                layoutHeadAlpha.setAlpha(scale);
                if(isBbprice()) bbPrice.setAlpha(1 - scale);
                iconHeadBbprice.setVisibility(View.GONE);
                if(ContextCompat.getDrawable(CannotBuyBaseDetailsActivity.this,R.drawable.sign_63).getConstantState().equals(ivBtnBack.getDrawable().getCurrent().getConstantState())){
                    ivBtnBack.setImageResource(R.drawable.icon_details_top_back);
                    iconHeadToHome.setImageResource(R.drawable.home_07);
                    iconHeadMsgTip.setImageResource(isMsg ? R.drawable.home_05 : R.drawable.home_14);
                }
            }else{
                layoutHeadAlpha.setAlpha(1f);
                if(isBbprice()) bbPrice.setAlpha(0f);
                if(isBbprice()) iconHeadBbprice.setVisibility(View.VISIBLE);
                if(ContextCompat.getDrawable(CannotBuyBaseDetailsActivity.this,R.drawable.icon_details_top_back).getConstantState().equals(ivBtnBack.getDrawable().getCurrent().getConstantState())){
                    ivBtnBack.setImageResource(R.drawable.sign_63);
                    iconHeadToHome.setImageResource(R.drawable.home2);
                    iconHeadMsgTip.setImageResource(isMsg ? R.drawable.home_03 : R.drawable.home_12);
                }
            }
        }
    };
    @Override
    public void updateMsgTip(boolean isUnreadMsg) {
        if(ContextCompat.getDrawable(this,R.drawable.home_05).getConstantState().equals(iconHeadMsgTip.getDrawable().getCurrent().getConstantState())
                || ContextCompat.getDrawable(this,R.drawable.home_14).getConstantState().equals(iconHeadMsgTip.getDrawable().getCurrent().getConstantState())){
            if(isUnreadMsg){
                iconHeadMsgTip.setImageResource(R.drawable.home_05);
            }
            else{
                iconHeadMsgTip.setImageResource(R.drawable.home_14);
            }
        }else{
            if(isUnreadMsg){
                iconHeadMsgTip.setImageResource(R.drawable.home_03);
            }
            else{
                iconHeadMsgTip.setImageResource(R.drawable.home_12);
            }
        }
    }
    private void setViewChatEnable(boolean enable){
        viewChat.setClickable(enable);
        viewChat.setBackgroundColor(enable ? Color.parseColor("#FFD400") : Color.parseColor("#cbcbcb"));
    }
    public void updateDetails(final ClassifyDetail data) {
        if(data == null) return;
        layoutShop.setTag(data.usid);
        viewShare.setTag(data.shareData);
        viewChat.setTag(data);
        setViewChatEnable(data.coState == 0);
        collectNumber = data.collectNumber;
        if (data.iscollectNumber == 0) {
            iconCollect.setImageResource(R.drawable.shou_cang_on);
            isChoose = true;
        } else {
            iconCollect.setImageResource(R.drawable.shou_cang_off);
            isChoose = false;
        }
        tvDetailsTitle.setText(data.cotitle);
        tvDetailsBrowserCount.setText(data.browseNumber + "次浏览");
        tvDetailsContent.setText(data.coremark);
        tvShopName.setText(data.usnickname);
        tvCollectCount.setText("收藏" + (data.collectNumber == 0 ? "" : data.collectNumber));
        adapterBanner = new AutoBrowseAdapter(this,data.imglist);
        viewPagerBanner.setAdapter(adapterBanner);
        viewPagerBanner.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                tvBannerPage.setText(position + 1 + "/" + adapterBanner.getCount());
            }
            @Override
            public void onPageSelected(int position) {}
            @Override
            public void onPageScrollStateChanged(int state) {}
        });
        tvBannerPage.setText("1/" + data.imglist.size());
        ImageLoadHelper.getInstance().displayImageHead(this,data.userPhoto, ivShopAvatar);

        /*******商家优惠劵项目*******/
        List<Coupons> coupons = data.preferList;
        if(coupons != null && coupons.size() > 0){
            layoutCoupons.setVisibility(View.VISIBLE);
            layoutCoupons.setTag(data.preferList);
            View view = null;
            for (int i = 0; i < coupons.size(); i++) {
                view = getLayoutInflater().inflate(R.layout.coupons_txt, null);
                TextView textView = (TextView) view.findViewById(R.id.coupons_name);
                textView.setText("满" + coupons.get(i).spconditions + "减" + coupons.get(i).spmoney);
                view.setLayoutParams(new ViewGroup.LayoutParams(layoutAddCoupon.getWidth() / 3, ViewGroup.LayoutParams.WRAP_CONTENT));
                layoutAddCoupon.addView(view);
            }
        }else{
            layoutCoupons.setVisibility(View.GONE);
        }
        /*******商家服务项目*******/
        List<ClassifyDetail.ServiceInfo> serviceInfos = data.serviceInfos;
        if(serviceInfos != null && serviceInfos.size() > 0){
            layoutService.setVisibility(View.VISIBLE);
            serviceInfos = serviceInfos.size() > 3 ? serviceInfos.subList(0, 3) : serviceInfos;
            recyclerViewService.setAdapter(new BaseQuickAdapter<ClassifyDetail.ServiceInfo>(R.layout.item_custom_detail_service, serviceInfos) {
                @Override
                protected void convert(BaseViewHolder helper, ClassifyDetail.ServiceInfo item) {
                    ImageLoadHelper.getInstance().displayImage(CannotBuyBaseDetailsActivity.this,item.photo, (ImageView) helper.getView(R.id.service_img));
                    helper.setText(R.id.type_of_service, item.name);
                }
            });
            layoutService.setTag(serviceInfos);
            layoutService.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<ClassifyDetail.ServiceInfo> serviceInfos = (List<ClassifyDetail.ServiceInfo>) v.getTag();
                    MobclickAgent.onEvent(CannotBuyBaseDetailsActivity.this,"detailServiceClick");
                    showPopDialog(serviceInfos);
                }
            });
        }else{
            layoutService.setVisibility(View.GONE);
        }
        //设置商品参数
        if (data.attributes != null && data.attributes.size() > 0) {
            ToolsHelper.getInstance().sort(data.attributes, "weight");
            adapterAttr = new AttrAdapter(data.attributes);
            recyclerViewAttr.setAdapter(adapterAttr);
        }
        //设置商品评论
        if(data.goodsComment != null && data.goodsComment.count > 0){
            viewComment.setEnabled(true);
            tvLayout_no_comment.setVisibility(View.GONE);
            commentArrow.setVisibility(View.VISIBLE);
            layout_comment.setVisibility(View.VISIBLE);
            tv_comment_count.setText("用户评价(" + data.goodsComment.count + ")");
            tv_comment_content.setText(data.goodsComment.content);
            tv_name.setText(data.goodsComment.nikeName);
            tv_time.setText(StrHelper.displayTime(data.goodsComment.created,true,false,false));
            ratingBar.setMark((int)data.goodsComment.score);//设置评分
            ImageLoadHelper.getInstance().displayImageHead(this,data.goodsComment.userPhoto,iv_avatar);
            if(data.goodsComment.state == 2){//是否为优质评价
                iv_icon.setVisibility(View.VISIBLE);
            }else{
                iv_icon.setVisibility(View.GONE);
            }

            if(data.goodsComment.imgList != null && data.goodsComment.imgList.size() > 0){
                CommentImgAdapter adapter = new CommentImgAdapter(data.goodsComment.imgList);
                adapter.setMaxItemCount(3);//最多3张图
                recyclerViewImage.setAdapter(adapter);
                recyclerViewImage.addOnItemTouchListener(new RecyclerItemClickListener(this, recyclerViewImage, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        MobclickAgent.onEvent(CannotBuyBaseDetailsActivity.this, "clickGoodsDetailsFromCommentContent");
                        Map<String,Object> params = new HashMap<String, Object>();
                        params.put("goid",goodsId);
                        if(data != null && data.goodsComment != null) params.put("totalCount",data.goodsComment.count);
                        startActivityAnimGeneral(CommentListActivity.class,params);
                    }
                    @Override
                    public void onItemLongClick(View view, int position) {}
                }));
            }
            viewComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MobclickAgent.onEvent(CannotBuyBaseDetailsActivity.this, "clickGoodsDetailsFromCommentContent");
                    Map<String,Object> params = new HashMap<String, Object>();
                    params.put("goid",goodsId);
                    if(data != null && data.goodsComment != null) params.put("totalCount",data.goodsComment.count);
                    startActivityAnimGeneral(CommentListActivity.class,params);
                }
            });
        }else{
            viewComment.setEnabled(false);
            tvLayout_no_comment.setVisibility(View.VISIBLE);
            layout_comment.setVisibility(View.GONE);
            commentArrow.setVisibility(View.GONE);
        }
    }
    private void updateViewRecommed(List<ClassifyGood> data) {
        //设置推荐商品
        if (data != null && data.size() > 0) {
            adapterRecommed = new RecommedProductBaokAdapter(this,data);
            recyclerViewRecomm.setAdapter(adapterRecommed);
            adapterRecommed.setOnRecyclerViewItemClickListener(new BaseQuickAdapter.OnRecyclerViewItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    MobclickAgent.onEvent(CannotBuyBaseDetailsActivity.this,"eachRecommendMerchantClick");
                    ClassifyGood item = adapterRecommed.getData().get(position);
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("id", item.id);
                    if (0 == item.isRecommend) {//不是活动特价商品
                        startActivityAnimGeneral(ProductDetailsOrdinaryActivity.class, params);
                    } else if (1 == item.isRecommend) {//是活动特价商品
                        startActivityAnimGeneral(ProductDetailsActivity.class, params);
                    }
                }
            });
        }
    }

    @Override
    protected void reqApi() {
        if(getDetailApi() == null) return;
        getDetailApi().compose(this.<ResponseBody<ClassifyDetail>>applySchedulers())
                .subscribe(new Action1<ResponseBody<ClassifyDetail>>() {
                    @Override
                    public void call(ResponseBody<ClassifyDetail> body) {
                        if(body.isSuccess()){
                            updateDetails(body.data);
                            if(null != body.data){
                                if(body.data.coState != 0){
                                    tvError.setVisibility(View.VISIBLE);
                                }else{
                                    tvError.setVisibility(View.GONE);
                                }
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {}
                });
    }

    @Override
    protected void initData() {
        reqApi();
        loadRecommed();
        if(isBbprice() && SPUtils.isLogin()) checkBbprice();
        layoutCoupons.setOnClickListener(this);
        layoutShop.setOnClickListener(this);
        viewCollect.setOnClickListener(this);
        viewShare.setOnClickListener(this);
        viewChat.setOnClickListener(this);
        if(isBbprice()) iconHeadBbprice.setOnClickListener(this);
        if(isBbprice()) bbPrice.setOnClickListener(this);

        ivBtnBack.setOnClickListener(this);
        iconHeadToHome.setOnClickListener(this);
        iconHeadMsgTip.setOnClickListener(this);
    }
    int collectNumber = 0;
    boolean isChoose = false;
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finshActivity();
                break;
            case R.id.home_page:
                Map<String,Object> p = new HashMap<>();
                p.put("source","mHomePage");
                startActivityAnimGeneral(MainActivity.class, p);
                break;
            case R.id.information:
                if(SPUtils.isLogin()){
                    startActivityAnimGeneral(MessageCenterActivity.class,null);
                }else{
                    startActivityAnimGeneral(FasterLoginActivity.class,null);
                }
                break;
            case R.id.coupons_layout:
                final List<Coupons> coupons = (List<Coupons>) v.getTag();
                if(coupons == null) return;
                DialogHelper.showBottomDialog(this, R.layout.dialog_coupon, new DialogHelper.OnEventListener<Dialog>() {
                    @Override
                    public void eventListener(View parentView, final Dialog window) {
                        CouponAdapter adapter = new CouponAdapter(CannotBuyBaseDetailsActivity.this, coupons);
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
            case R.id.goto_shop:
                String shopId = (String) v.getTag();
                if(TextUtils.isEmpty(shopId)) return;

                MobclickAgent.onEvent(this,"shopBtnClick");
                Map<String, Object> param = new HashMap<>();
                param.put("shopId", shopId);
                param.put("position", 0);
                startActivityAnimGeneral(ShopActivityF.class, param);
                break;
            case R.id.collect:
                if (!SPUtils.isLogin()) {
                    startActivityAnimGeneral(FasterLoginActivity.class, null);
                    break;
                }
                RxRequestApi.getInstance().getApiService().collect(goodsId, 1, isChoose ? 1 : 0)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<ResponseBody>() {
                            @Override
                            public void call(ResponseBody body) {
                                if (body.isSuccess()) {
                                    if (isChoose) {
                                        collectNumber--;
                                        iconCollect.setImageResource(R.drawable.shou_cang_off);
                                        isChoose = false;
                                    } else {
                                        collectNumber++;
                                        iconCollect.setImageResource(R.drawable.shou_cang_on);
                                        isChoose = true;
                                    }
                                    RxBus.get().post(BusAction.CANCEL_GOODS_COLLECTION, isChoose);
                                    tvCollectCount.setText("收藏" + (collectNumber==0 ? "" : collectNumber));
                                } else {
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
            case R.id.share:
                //TODO 分享
                ShareEntity data = (ShareEntity) v.getTag();
                if(data == null) return;
                OnekeyShare oks = new OnekeyShare();
                oks.show(this, data);
                break;
            case R.id.contact_service:
                if (!SPUtils.isLogin()) {
                    startActivityAnimGeneral(FasterLoginActivity.class, null);
                    return;
                }
                ClassifyDetail detail = (ClassifyDetail) v.getTag();
                if (detail == null) return;
                Map<String, Object> parmas = new HashMap<>();
                parmas.put("sid", detail.sid);
                parmas.put("shopId", detail.usid);
                Product product = new Product();//构建product对象
                product.content = detail.coremark;
                product.icon = (detail.imglist != null && detail.imglist.size() > 0) ? detail.imglist.get(0).url : "";
                product.price = detail.goprice + "";
                product.productType = getProductType();//分类商品详情
                product.productId = detail.goid;
                parmas.put("prodcut", product);
                startActivityAnimGeneral(ChatActivity.class, parmas);
                break;
            case R.id.bbparity:
            case R.id.iv_fast_bb_goods:
                if(!SPUtils.isLogin()){
                    Tst.showToast("请先登录");
                    startActivityAnimGeneral(FasterLoginActivity.class,null);
                    return;
                }
                final Map<String,Object> paramMap=new HashMap<>();
                paramMap.put("goid",goodsId);
                paramMap.put("exists",sendBBprice);
                MobclickAgent.onEvent(this, "detailQuickCompareClick");
                if (!sendBBprice) {
                    DialogHelper.showPromptDialog(this, null, "我们将为您推荐提供同类商品的商家，并将您的需求发送给商家", "取消", null, "确定", new DialogHelper.OnMenuClick() {
                        @Override
                        public void onLeftMenuClick() {}
                        @Override
                        public void onCenterMenuClick() {}
                        @Override
                        public void onRightMenuClick() {
                            startActivityAnimGeneral(NotifyMerchantActivity.class, paramMap);
                        }
                    });
                } else {
                    startActivityAnimGeneral(NotifyMerchantActivity.class, paramMap);
                }
                break;
        }
    }

    @Override
    public String setStatisticsTitle() {
        return "商品详情";
    }
    boolean isReq = false;
    private void loadRecommed() {
        if (recyclerViewRecomm.getAdapter() != null) return;
        isReq = true;
        RxRequestApi.getInstance().getApiService().getRecommend(goodsId).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<List<ClassifyGood>>>() {
                    @Override
                    public void call(ResponseBody<List<ClassifyGood>> body) {
                        if (body.isSuccess()) {
                            updateViewRecommed(body.data);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        isReq = false;
                        throwable.printStackTrace();
                    }
                });
    }
    public void checkBbprice(){
        RxRequestApi.getInstance().getApiService().parityExists(goodsId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<Exists>>() {
                    @Override
                    public void call(ResponseBody<Exists> body) {
                        if(body.isSuccess()){
                            sendBBprice = (body.data.flag != 0);//0：未发布过，1：发布过
                        }else{
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {}
                });
    }
    protected void showGriade(View view) {}
    private void showPopDialog(final List<ClassifyDetail.ServiceInfo> serviceInfos) {
        if (serviceInfos == null) return;
        DialogHelper.showBottomDialog(this, R.layout.dialog_custom_furniture_details, new DialogHelper.OnEventListener<Dialog>() {
            @Override
            public void eventListener(View parentView, final Dialog window) {
                CommonAdapter adapter = new CommonAdapter<ClassifyDetail.ServiceInfo>(CannotBuyBaseDetailsActivity.this,
                        R.layout.listview_item_custom_furniture_details, serviceInfos) {
                    @Override
                    public void convert(ViewHolder helper, ClassifyDetail.ServiceInfo data) {
                        if (TextUtils.isEmpty(data.detailUrl)) {
                            helper.getView(R.id.tvBtn_detail).setVisibility(View.GONE);
                            helper.getView(R.id.iv_arrow).setVisibility(View.GONE);
                        } else {
                            helper.getView(R.id.tvBtn_detail).setVisibility(View.VISIBLE);
                            helper.getView(R.id.iv_arrow).setVisibility(View.VISIBLE);
                        }
                        helper.getView(R.id.tvBtn_detail).setTag(data.detailUrl);
                        helper.getView(R.id.tvBtn_detail).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                MobclickAgent.onEvent(CannotBuyBaseDetailsActivity.this,"serviceLearnMoreClick");
                                String url = (String) v.getTag();
                                Map<String, Object> param = new HashMap<String, Object>();
                                param.put("url", url);
                                startActivityAnimGeneral(CommWebViewActivity.class, param);
                            }
                        });
                        helper.setText(R.id.custom_service_name, data.name);
                        helper.setText(R.id.custom_service_content, data.remark);
                        ImageLoadHelper.getInstance().displayImage(CannotBuyBaseDetailsActivity.this,data.photo, (ImageView) helper.getView(R.id.custom_service_img), LocalDisplay.dp2px(20), LocalDisplay.dp2px(20));
                    }
                };
                MaxHightListView listView = (MaxHightListView) parentView.findViewById(R.id.listview);
                listView.setMaxHeight(LocalDisplay.dip2px(240));
                listView.setAdapter(adapter);
                parentView.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        window.dismiss();
                    }
                });
            }
        });
    }
    public boolean isBbprice(){
        if("5".equals(getProductType())){
            return true;
        }else{
            return false;
        }
    }

    @Subscribe(tags = {@Tag(BusAction.ACTION_LOGIN)})
    public void loginNotify(User user){
        reqApi();
        checkBbprice();
    }

    public abstract Observable<ResponseBody<ClassifyDetail>> getDetailApi();
    public abstract String getChatText();
    public abstract String getProductType();
}
