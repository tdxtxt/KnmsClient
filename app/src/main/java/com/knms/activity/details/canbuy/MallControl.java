package com.knms.activity.details.canbuy;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.knms.activity.base.BaseActivity;
import com.knms.activity.comment.CommentListActivity;
import com.knms.activity.dialog.ChoiceSizeActivityDialog;
import com.knms.activity.login.FasterLoginActivity;
import com.knms.activity.mall.cart.ShoppingCartActivityF;
import com.knms.activity.mall.order.ConfirmOrderActivity;
import com.knms.adapter.AttrAdapter;
import com.knms.adapter.CommentImgAdapter;
import com.knms.adapter.MixingContentAdapter;
import com.knms.adapter.RecommedProductAdapter;
import com.knms.adapter.baserecycler.BaseQuickAdapter;
import com.knms.android.R;
import com.knms.bean.AffectedNumber;
import com.knms.bean.Recommend;
import com.knms.bean.ResponseBody;
import com.knms.bean.comm.MixingContentBean;
import com.knms.bean.goodsdetails.GoodsDetails;
import com.knms.bean.order.neworder.RequestBuyParameters;
import com.knms.bean.sku.SkuBody;
import com.knms.bean.sku.base.SkuModel;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.net.RxRequestApi;
import com.knms.oncall.RecyclerItemClickListener;
import com.knms.util.CommonUtils;
import com.knms.util.ConstantObj;
import com.knms.util.DialogHelper;
import com.knms.util.ImageLoadHelper;
import com.knms.util.LocalDisplay;
import com.knms.util.SPUtils;
import com.knms.util.StrHelper;
import com.knms.util.ToolsHelper;
import com.knms.util.Tst;
import com.knms.view.CircleImageView;
import com.knms.view.Star;
import com.knms.view.clash.FullyGridLayoutManager;
import com.knms.view.indicator.ScrollViewTabIndicator;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.shareuzi.bean.ShareEntity;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * 类描述：
 * 创建人：tdx
 * 创建时间：2017/8/29 11:50
 * 传参：
 * 返回:
 */
public class MallControl extends BaseControl implements View.OnClickListener{
    /*******
     * 悬浮view
     *******/
    ScrollViewTabIndicator indicatorOne;//第一个
    ScrollViewTabIndicator indicatorTwo;//第二个
    /*******
     * 尾部view
     *******/
    RecyclerView recyclerViewAttr;//商品参数
    RecyclerView recyclerViewMix;//商品图文混合
    View viewAttr;//商品参数
    View viewComment;//商品评论
    View viewRecommend;//商品推荐

    private RecyclerView recyclerViewRecomm;
    private TextView tv_comment_count;//用户评论数量
    private TextView tv_comment_spesc;//评价商品规格
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

    AttrAdapter adapterAttr;
    MixingContentAdapter adapterMix;
    RecommedProductAdapter adapterRecommed;
    /*******
     * 底部菜单view
     *******/
    View viewCollect, viewShare, viewChat;
    ImageView iconCollect;
    TextView tvCollectCount;
    TextView tvAddCart, tvBuy;

    View executeView;

    public int heightBanner = LocalDisplay.dp2px(360 - 46);

    public static final int REQEST_CODE_ADD_CART = 0x00111;
    public static final int REQEST_CODE_BUY = 0x00222;

    public int collectNumber;
    public boolean isCollect;
    public String skuId;

    public MallControl(BaseActivity activity, String goid, GoodsDetails.CommodityShowBoBean data) {
        super(activity,goid,data);
        init();
        executeView = activity.findView(R.id.cart_anim_icon);
        loadRecommed();

    }
    @Override
    protected void loadStivkyUi(ViewStub viewStubOne, ViewStub viewStubTwo) {
        viewStubOne.setLayoutResource(R.layout.stub_details_stivky);
        viewStubTwo.setLayoutResource(R.layout.stub_details_stivky);
        if (viewStivkyOne == null) viewStivkyOne = viewStubOne.inflate();
        if (viewStivkyTwo == null) viewStivkyTwo = viewStubTwo.inflate();
        viewStubOne.setVisibility(View.INVISIBLE);
        indicatorOne = (ScrollViewTabIndicator) viewStivkyOne.findViewById(R.id.indicator);
        indicatorTwo = (ScrollViewTabIndicator) viewStivkyTwo.findViewById(R.id.indicator);

        if(data == null){
            indicatorOne.setVisibility(View.GONE);
            viewStivkyTwo.setVisibility(View.GONE);
        }
    }

    @Override
    protected void loadFooterUi(ViewStub viewStub) {
        viewStub.setLayoutResource(R.layout.stub_details_footer_cannot_buy);
        if (viewFooter == null) viewFooter = viewStub.inflate();
        viewAttr = viewFooter.findViewById(R.id.view_params);
        viewComment = viewFooter.findViewById(R.id.view_comment);
        viewRecommend = viewFooter.findViewById(R.id.ll_recommend);

        commentArrow = (ImageView) viewFooter.findViewById(R.id.iv_arrow_view);

        recyclerViewAttr = (RecyclerView) viewFooter.findViewById(R.id.recyclerView_params);
        LinearLayoutManager layoutManagerAttr = new LinearLayoutManager(activity);
        layoutManagerAttr.setSmoothScrollbarEnabled(true);
        layoutManagerAttr.setAutoMeasureEnabled(true);
        recyclerViewAttr.setLayoutManager(layoutManagerAttr);
        recyclerViewAttr.setHasFixedSize(true);
        recyclerViewAttr.setNestedScrollingEnabled(false);
        recyclerViewAttr.setFocusable(false);

        recyclerViewMix = (RecyclerView) viewFooter.findViewById(R.id.recyclerView_mix);
        LinearLayoutManager layoutManagerMix = new LinearLayoutManager(activity);
        layoutManagerMix.setSmoothScrollbarEnabled(true);
        layoutManagerMix.setAutoMeasureEnabled(true);
        recyclerViewMix.setHasFixedSize(true);
        recyclerViewMix.setNestedScrollingEnabled(false);
        recyclerViewMix.setFocusable(false);
        recyclerViewMix.setLayoutManager(layoutManagerMix);

        tv_comment_count = (TextView) viewFooter.findViewById(R.id.tv_comment_count);
        tv_comment_spesc = (TextView) viewFooter.findViewById(R.id.tv_specs);
        tv_time = (TextView) viewFooter.findViewById(R.id.tv_time);
        tv_name = (TextView) viewFooter.findViewById(R.id.tv_name);
        tvLayout_no_comment = (TextView) viewFooter.findViewById(R.id.tvLayout_no_comment);
        layout_comment = (LinearLayout) viewFooter.findViewById(R.id.layout_comment);
        ratingBar = (Star) viewFooter.findViewById(R.id.ratingBar);
        tv_comment_content = (TextView) viewFooter.findViewById(R.id.tv_comment_content);
        iv_avatar = (CircleImageView) viewFooter.findViewById(R.id.iv_avatar);
        iv_icon = (ImageView) viewFooter.findViewById(R.id.iv_icon);
        recyclerViewImage = (RecyclerView) viewFooter.findViewById(R.id.recyclerView);
        recyclerViewImage.setLayoutManager(new FullyGridLayoutManager(activity, 3));
        recyclerViewImage.setNestedScrollingEnabled(false);
        recyclerViewImage.setFocusable(false);

        recyclerViewRecomm = (RecyclerView) viewFooter.findViewById(R.id.recyclerView_recommend);
        GridLayoutManager layoutManagerRecomm = new GridLayoutManager(activity, 2) {
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

        if(data == null) viewFooter.setVisibility(View.GONE);
    }

    @Override
    protected void loadMenuUi(ViewStub viewStub) {
        viewStub.setLayoutResource(R.layout.stub_details_menu_can_buy);
        if (viewMenu == null) viewMenu = viewStub.inflate();
        viewCollect = viewMenu.findViewById(R.id.collect);
        viewShare = viewMenu.findViewById(R.id.share);
        viewChat = viewMenu.findViewById(R.id.contact_service);
        iconCollect = (ImageView) viewMenu.findViewById(R.id.img_collect);
        tvCollectCount = (TextView) viewMenu.findViewById(R.id.collect_amount);
        tvBuy = (TextView) viewMenu.findViewById(R.id.tv_buy);
        tvAddCart = (TextView) viewMenu.findViewById(R.id.tv_add_shoppingcart);

        viewCollect.setOnClickListener(this);
        viewShare.setOnClickListener(this);
        viewChat.setOnClickListener(this);
        tvAddCart.setOnClickListener(this);
        tvBuy.setOnClickListener(this);

        if(data == null) viewMenu.setVisibility(View.GONE);
    }

    @Override
    public void updateStivkyUi() {
        List<String> names = new ArrayList<>();
        names.add("商品参数");
        names.add("用户评价");
        names.add("为你推荐");
        List<View> views = new ArrayList<>();
        views.add(viewAttr);
        views.add(viewComment);
        views.add(viewRecommend);
        indicatorTwo.setScrollView(scrollView, onScrollChangeListener, names, views);
        indicatorOne.setScrollView(scrollView, indicatorTwo, names, views);
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
            if (scrollY <= 0) {
                layoutHeadAlpha.setAlpha(0f);
            } else if (scrollY <= heightBanner) {//滑动过程中，渐变
                float scale = (float) scrollY / heightBanner;
                layoutHeadAlpha.setAlpha(scale);
                float b = 0.3f;
                float scale1;
                if(scrollY <= heightBanner / 2f){
                    scale1 = (1 - 2.f * (float) scrollY / (float) heightBanner) * (1 - b) + b;
                    if (ContextCompat.getDrawable(activity, R.drawable.back_b).getConstantState().equals(ivBtnBack.getDrawable().getCurrent().getConstantState())) {
                        ivBtnBack.setImageResource(R.drawable.icon_details_top_back);
                        iconHeadToHome.setImageResource(R.drawable.icon_details_top_home);
                        iconHeadMsgTip.setImageResource(isMsg ? R.drawable.chat_red : R.drawable.icon_details_top_chat);
                        iconHeadShoppingCart.setImageResource(R.drawable.icon_details_top_shoppingcart);
                    }
                }else{
                    scale1 = (float) (2 * scrollY - heightBanner) / (float)heightBanner * (1 - b) + b;
                    if (ContextCompat.getDrawable(activity, R.drawable.icon_details_top_back).getConstantState().equals(ivBtnBack.getDrawable().getCurrent().getConstantState())) {
                        ivBtnBack.setImageResource(R.drawable.back_b);
                        iconHeadToHome.setImageResource(R.drawable.home_b);
                        iconHeadMsgTip.setImageResource(isMsg ? R.drawable.chat_red_b : R.drawable.chat_b);
                        iconHeadShoppingCart.setImageResource(R.drawable.shopping_b);
                    }
                }
                ivBtnBack.setAlpha(scale1);
                iconHeadToHome.setAlpha(scale1);
                iconHeadMsgTip.setAlpha(scale1);
                iconHeadShoppingCart.setAlpha(scale1);
            } else {
                layoutHeadAlpha.setAlpha(1f);
            }
        }
    };
    @Override
    public void updateFooterUi() {
        if(data == null) return;
        //设置商品评论
        if (data.goodsComment != null && data.goodsComment.count > 0) {
            viewComment.setEnabled(true);
            tvLayout_no_comment.setVisibility(View.GONE);
            commentArrow.setVisibility(View.VISIBLE);
            layout_comment.setVisibility(View.VISIBLE);
            tv_comment_count.setText("用户评价(" + data.goodsComment.count + ")");
            tv_comment_content.setText(data.goodsComment.content);
            if(TextUtils.isEmpty(data.goodsComment.spesc)){
                tv_comment_spesc.setVisibility(View.GONE);
            }else{
                tv_comment_spesc.setVisibility(View.VISIBLE);
                tv_comment_spesc.setText(data.goodsComment.spesc);
            }
            tv_name.setText(data.goodsComment.nikeName);
            tv_time.setText(StrHelper.displayTime(data.goodsComment.created, true, false, false));
            ratingBar.setMark((int) data.goodsComment.score);//设置评分
            ImageLoadHelper.getInstance().displayImageHead(activity, data.goodsComment.userPhoto, iv_avatar);
            if (data.goodsComment.state == 2) {//是否为优质评价
                iv_icon.setVisibility(View.VISIBLE);
            } else {
                iv_icon.setVisibility(View.GONE);
            }

            if (data.goodsComment.imgList != null && data.goodsComment.imgList.size() > 0) {
                CommentImgAdapter adapter = new CommentImgAdapter(data.goodsComment.imgList);
                adapter.setMaxItemCount(3);//最多3张图
                recyclerViewImage.setAdapter(adapter);
                recyclerViewImage.addOnItemTouchListener(new RecyclerItemClickListener(activity, recyclerViewImage, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        MobclickAgent.onEvent(activity, "clickGoodsDetailsFromCommentContent");
                        Map<String, Object> params = new HashMap<String, Object>();
                        params.put("goid", goodsId);
                        if (data != null && data.goodsComment != null)
                            params.put("totalCount", data.goodsComment.count);
                        activity.startActivityAnimGeneral(CommentListActivity.class, params);
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {
                    }
                }));
            }
            viewComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MobclickAgent.onEvent(activity, "clickGoodsDetailsFromCommentContent");
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("goid", goodsId);
                    if (data != null && data.goodsComment != null)
                        params.put("totalCount", data.goodsComment.count);
                    activity.startActivityAnimGeneral(CommentListActivity.class, params);
                }
            });
        } else {
            viewComment.setEnabled(false);
            tvLayout_no_comment.setVisibility(View.VISIBLE);
            layout_comment.setVisibility(View.GONE);
            commentArrow.setVisibility(View.GONE);
        }

        //设置商品参数
        if (data.showParameters != null && data.showParameters.size() > 0) {
            ToolsHelper.getInstance().sort(data.showParameters, "sorting");
            adapterAttr = new AttrAdapter(data.showParameters);
            recyclerViewAttr.setAdapter(adapterAttr);
        }
        //设置商品图文混合
        if(data.showMix != null && data.showMix.size() > 0){
            adapterMix = new MixingContentAdapter(data.showMix);
            recyclerViewMix.setAdapter(adapterMix);
        }
    }
    @Override
    public void updateMenuUi() {
        if(data == null) return;
        if (data.showStartTime > 0) {
            tvBuy.setTextColor(Color.parseColor("#ffffff"));
            tvBuy.setBackgroundColor(Color.parseColor("#cbcbcb"));
            tvBuy.setClickable(false);
        }
        viewShare.setTag(data.shareData);
        viewChat.setTag(data);

        collectNumber = data.collectNumber;
        if (data.iscollectNumber == 0) {
            iconCollect.setImageResource(R.drawable.shou_cang_on);
            isCollect = true;
        } else {
            iconCollect.setImageResource(R.drawable.shou_cang_off);
            isCollect = false;
        }
        tvCollectCount.setText("收藏" + (collectNumber == 0 ? "" : collectNumber));
    }
    /**
     * 开始抢购之前
     */
    @Override
    public void startSnapUpBefore() {
        tvBuy.setTextColor(Color.parseColor("#ffffff"));
        tvBuy.setBackgroundColor(Color.parseColor("#cbcbcb"));
        tvAddCart.setBackgroundColor(Color.parseColor("#FDEC93"));
        tvAddCart.setTextColor(Color.parseColor("#333333"));
        tvBuy.setClickable(false);
        tvAddCart.setClickable(true);
    }

    /**
     * 开始抢购
     */
    @Override
    public void startSnapUp() {
        tvBuy.setTextColor(Color.parseColor("#333333"));
        tvBuy.setBackgroundColor(Color.parseColor("#FDD301"));
        tvBuy.setClickable(true);
    }
    /**
     * 结束抢购
     */
    @Override
    public void stopSnapUp() {
        tvBuy.setTextColor(Color.parseColor("#ffffff"));
        tvBuy.setBackgroundColor(Color.parseColor("#cbcbcb"));
        tvAddCart.setBackgroundColor(Color.parseColor("#E5E5E5"));
        tvAddCart.setTextColor(Color.parseColor("#ffffff"));
        tvBuy.setClickable(false);
        tvAddCart.setClickable(false);
    }
    @Override
    public void detachView() {
        super.detachView();
    }

    boolean isReq = false;
    private void loadRecommed() {
        if (recyclerViewRecomm.getAdapter() != null) return;
        isReq = true;
        Map<String,Object> map=new HashMap<>();
        map.put("showId",goodsId);
        map.put("pageIndex",1);
        RxRequestApi.getInstance().getApiService().getRecommendList(map).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<Recommend>>() {
                    @Override
                    public void call(ResponseBody<Recommend> body) {
                        if (body.isSuccess1()) {
                            updateViewRecommed(body.data.commodityShowBos);
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

    private void updateViewRecommed(List<Recommend.CommodityShowBosBean> data) {
        //设置推荐商品
        if (data != null && data.size() > 0) {
            adapterRecommed = new RecommedProductAdapter(activity, data);
            recyclerViewRecomm.setAdapter(adapterRecommed);
            adapterRecommed.setOnRecyclerViewItemClickListener(new BaseQuickAdapter.OnRecyclerViewItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    MobclickAgent.onEvent(activity, "eachRecommendMerchantClick");
                    Recommend.CommodityShowBosBean item = adapterRecommed.getData().get(position);
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("id", item.showId);
                    activity.startActivityAnimGeneral(ProductDetailsActivity.class,params);
//                    if (0 == item.isRecommend) {//不是活动特价商品
//                        activity.startActivityAnimGeneral(ProductDetailsOrdinaryActivity.class, params);
//                    } else if (1 == item.isRecommend) {//是活动特价商品
//                        activity.startActivityAnimGeneral(ProductDetailsActivity.class, params);
//                    }
                }
            });
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.collect:
                if (!SPUtils.isLogin()) {
                    activity.startActivityAnimGeneral(FasterLoginActivity.class, null);
                    return;
                }
                //收藏类型type:0、收藏 1、取消收藏
                RxRequestApi.getInstance().getApiService().collect(goodsId, 1, isCollect ? 1 : 0)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<ResponseBody>() {
                            @Override
                            public void call(ResponseBody body) {
                                if (body.isSuccess()) {
                                    if (isCollect) {
                                        collectNumber--;
                                        iconCollect.setImageResource(R.drawable.shou_cang_off);
                                        isCollect = false;
                                    } else {
                                        collectNumber++;
                                        iconCollect.setImageResource(R.drawable.shou_cang_on);
                                        isCollect = true;
                                    }
                                    RxBus.get().post(BusAction.CANCEL_GOODS_COLLECTION, isCollect);
                                    tvCollectCount.setText("收藏" + (collectNumber == 0 ? "" : collectNumber));
                                } else {
                                    if ("已经收藏".equals(body.desc)){
                                        activity.refresh();//刷新列表
                                    }else if(!"需要登录".equals(body.desc)){
                                        Tst.showToast(body.desc);
                                    }
                                }
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
//                                Tst.showToast(throwable.toString());
                            }
                        });
                break;
            case R.id.share:
                //TODO 分享
                ShareEntity data = (ShareEntity) v.getTag();
                if (data == null) return;
                OnekeyShare oks = new OnekeyShare();
                oks.show(activity, data);
                break;
            case R.id.tv_add_shoppingcart://添加到购物车
                MobclickAgent.onEvent(activity,"product_addShopcart");
                if (!SPUtils.isLogin()) {
                    activity.startActivityAnimGeneral(FasterLoginActivity.class, null);
                    return;
                }

                if (TextUtils.isEmpty(goodsId)) return;
                Intent intent = new Intent(activity, ChoiceSizeActivityDialog.class);
                intent.putExtra("data",mSkuBody);
                intent.putExtra("goid", goodsId);
                intent.putExtra("initSkuId", skuId);
//                intent.putExtra("initCount",item.buyCount);
                activity.startActivityForResult(intent, REQEST_CODE_ADD_CART);
                break;
            case R.id.tv_buy:
                MobclickAgent.onEvent(activity,"product_buynow");
                if (!SPUtils.isLogin()) {
                    activity.startActivityAnimGeneral(FasterLoginActivity.class, null);
                    return;
                }

                if (TextUtils.isEmpty(goodsId)) return;
                intent = new Intent(activity, ChoiceSizeActivityDialog.class);
                intent.putExtra("data",mSkuBody);
                intent.putExtra("goid", goodsId);
                intent.putExtra("initSkuId", skuId);
//                intent.putExtra("initCount",item.buyCount);
                activity.startActivityForResult(intent, REQEST_CODE_BUY);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;
        switch (requestCode) {
            case REQEST_CODE_ADD_CART://得到的数据
                SkuModel resultSku = (SkuModel) data.getSerializableExtra("resultSku");
                int resultCount = data.getIntExtra("resultCount", 1);
                skuId = data.getStringExtra("resultSkuId");
                if (resultSku != null) {
                    skuId = resultSku.skuId;
                    addShoppingCart(skuId, resultCount);
                }
                break;
            case REQEST_CODE_BUY://得到的数据
                resultSku = (SkuModel) data.getSerializableExtra("resultSku");
                resultCount = data.getIntExtra("resultCount", 1);
                skuId = data.getStringExtra("resultSkuId");
                if (resultSku != null) {
                    skuId = resultSku.skuId;
                    List<RequestBuyParameters> list = new ArrayList<>();
                    RequestBuyParameters parameters = new RequestBuyParameters();
                    parameters.buyQuantity = resultCount;
                    parameters.specificationId = skuId;
                    list.add(parameters);
                    Map<String, Object> map = new HashMap<>();
                    map.put("buyParameters",list);
                    map.put("type",1);
                    activity.startActivityAnimGeneral(ConfirmOrderActivity.class, map);
                }
                break;
        }
    }
    //添加购物车
    private void addShoppingCart(String skuId, final int buyCount) {
        Map<String, Object> params = new HashMap<>();
        params.put("eventId", CommonUtils.getEventId());
        params.put("specificationId", skuId);
        params.put("planNumber", buyCount);
        params.put("userId", ConstantObj.TEMP_USERID);
        RxRequestApi.getInstance().getApiService().addShoppingCart(params)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<AffectedNumber>>() {
                    @Override
                    public void call(ResponseBody<AffectedNumber> body) {
                        if (body.isSuccess1()) {
                            addProductCount=buyCount;
                            if (mAnimation == null) initAnim(executeView);
                            executeView.setVisibility(View.VISIBLE);
                            executeView.startAnimation(mAnimation);
                            Tst.showToast("已加入购物车");
                        } else if("50".equals(body.code)){//购物车已满20件了
                            DialogHelper.showPromptDialog(activity, "", body.desc, "取消", "", "确认", new DialogHelper.OnMenuClick() {
                                @Override
                                public void onLeftMenuClick() {}
                                @Override
                                public void onCenterMenuClick() {}
                                @Override
                                public void onRightMenuClick() {
                                    activity.startActivityAnimGeneral(ShoppingCartActivityF.class,null);
                                }
                            });
                        } else {
                            Tst.showToast(body.desc);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Tst.showToast(throwable.getMessage());
                    }
                });
    }
    private Animation mAnimation;
    int addProductCount=0;
    private void initAnim(final View executeView) {
        mAnimation = AnimationUtils.loadAnimation(activity, R.anim.cart_anim);
        mAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationRepeat(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                executeView.setVisibility(View.INVISIBLE);
                if(!TextUtils.equals(showCartCount.getText().toString(),"99+"))showCartCount.setText(Integer.parseInt(showCartCount.getText().toString()) + addProductCount + "");
            }
        });
    }
    protected SkuBody.ComdiBo mSkuBody;
    @Override
    protected void getProductAttr() {
        Map<String,Object> params = new HashMap<>();
        params.put("showId",goodsId);
        RxRequestApi.getInstance().getApiService().getSkuProduct(params)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<ResponseBody<SkuBody>>() {
                    @Override
                    public void call(ResponseBody<SkuBody> body) {
                        if(body.isSuccess1()){
                            totalStock = 0;
                            mSkuBody = body.data.comdiBo;
                            if(mSkuBody != null && mSkuBody.skuProducts != null){
                                for (SkuModel sku : mSkuBody.skuProducts) {
                                    totalStock += sku.stock;
                                }
                            }
                            if(totalStock < 1){
                                Tst.showToast("商品暂无货");
                                stopSnapUp();
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {}
                });
    }
}
