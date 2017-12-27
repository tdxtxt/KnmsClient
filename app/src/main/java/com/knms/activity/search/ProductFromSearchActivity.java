package com.knms.activity.search;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshRecyclerView;
import com.knms.activity.base.HeadBaseActivity;
import com.knms.activity.details.ProductDetailsBaokActivity;
import com.knms.activity.details.ProductDetailsOrdinaryActivity;
import com.knms.activity.details.base.CannotBuyBaseDetailsActivity;
import com.knms.activity.details.canbuy.ProductDetailsActivity;
import com.knms.activity.im.MessageCenterActivity;
import com.knms.adapter.BrandAdapter;
import com.knms.adapter.ProductMainAdapter;
import com.knms.adapter.SearchAdapter;
import com.knms.adapter.StyleAdapter;
import com.knms.adapter.baserecycler.BaseQuickAdapter;
import com.knms.android.R;
import com.knms.app.KnmsApp;
import com.knms.bean.ResponseBody;
import com.knms.bean.other.Brand;
import com.knms.bean.other.Label;
import com.knms.bean.other.Style;
import com.knms.bean.product.ClassifyGood;
import com.knms.core.im.IMHelper;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.net.HttpConstant;
import com.knms.net.ReqApi;
import com.knms.net.RxRequestApi;
import com.knms.oncall.AsyncHttpCallBack;
import com.knms.util.ImageLoadHelper;
import com.knms.util.LocalDisplay;
import com.knms.util.SPUtils;
import com.knms.util.Tst;
import com.knms.view.VerticalDrawerLayout;
import com.knms.view.XEditText;
import com.knms.view.clash.FullyGridLayoutManager;
import com.umeng.analytics.MobclickAgent;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by tdx on 2016/8/30.
 * 分类商品
 */
public class ProductFromSearchActivity extends HeadBaseActivity implements View.OnClickListener, VerticalDrawerLayout.OnCloseListener {
    private String key, labelId;//标签id
    private String styleId, brandId;//风格id，品牌id
    private int isHot;//isHot是否人气（1：是, 0：否)
    private int pageNum = 1;
    TextView tvBtn_default, tvBtn_buzz, tv_price, tv_style, tv_brand;
    XEditText tv_name_serach;
    RelativeLayout rlBtn_price, rl_style, rl_brand, rl_status;
    LinearLayout ll_menu;
    VerticalDrawerLayout v_drawer_layout;
    RecyclerView recyclerView, recyclerView_content,searchRecyclerView;
    PullToRefreshRecyclerView refresh_recyclerView;
    ProductMainAdapter adatper;
    GridLayoutManager layoutManager;

    private ImageButton btnTop;
    private List<Brand> brandList;
    private List<Style> styleList;
    private Brand initBrand;
    private TextView tvClickCurrent;

    private TextView tvRight;
    private ImageView ivRight;


    @Override
    protected int layoutResID() {
        return R.layout.activity_product_main;
    }

    @Override
    protected void getParmas(Intent intent) {
        key = intent.getStringExtra("key");
        brandId = intent.getStringExtra("brandId");
        labelId = intent.getStringExtra("labelId");
    }

    @Override
    protected void initView() {
        btnTop = findView(R.id.top);
        rl_status = findView(R.id.rl_status);
        rl_style = findView(R.id.rl_style);
        rl_brand = findView(R.id.rl_brand);
        tv_style = findView(R.id.tv_style);
        tv_brand = findView(R.id.tv_brand);
        ll_menu = findView(R.id.ll_menu);
        tv_name_serach = findView(R.id.edit_title_center);
        tv_name_serach.setVisibility(View.VISIBLE);
        searchRecyclerView=findView(R.id.search_recyclerView);
        searchRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        tvBtn_default = findView(R.id.tvBtn_default);
        tvBtn_buzz = findView(R.id.tvBtn_buzz);
        rlBtn_price = findView(R.id.rlBtn_price);
        tv_price = findView(R.id.tv_price);

        v_drawer_layout = findView(R.id.v_drawer_layout);
        recyclerView = findView(R.id.recyclerView);
        refresh_recyclerView = findView(R.id.refresh_recyclerView);
        recyclerView_content = refresh_recyclerView.getRefreshableView();
        recyclerView.setLayoutManager(new FullyGridLayoutManager(this, 3, true));
        layoutManager =
//                new GridLayoutManager(this,2);
                new GridLayoutManager(this, 2);
        recyclerView_content.setLayoutManager(layoutManager);
        adatper = new ProductMainAdapter(this, new ArrayList<ClassifyGood>());
        recyclerView_content.setAdapter(adatper);
        recyclerView_content.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE://静止,没有滚动
                        ImageLoadHelper.getInstance().resume(ProductFromSearchActivity.this);
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING://正在被外部拖拽,一般为用户正在用手指滚动
                        ImageLoadHelper.getInstance().pause(ProductFromSearchActivity.this);
                        break;
                    case RecyclerView.SCROLL_STATE_SETTLING://自动滚动开始
                        ImageLoadHelper.getInstance().pause(ProductFromSearchActivity.this);
                        break;
                }
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (recyclerView_content.getChildCount() == 0)
                    return;
                int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                if (firstVisibleItem < 4)
                    btnTop.setVisibility(View.GONE);
                else
                    btnTop.setVisibility(View.VISIBLE);
            }
        });
        tvBtn_default.setTextColor(getResources().getColor(R.color.common_red));
        tvBtn_buzz.setTextColor(Color.parseColor("#6C6C6C"));
        tv_price.setTextColor(Color.parseColor("#6C6C6C"));
        setRightMenuCallBack(new RightCallBack() {
            @Override
            public void setRightContent(TextView tv, ImageView icon) {
                tvRight=tv;
                ivRight=icon;
                tvRight.setVisibility(View.GONE);
                tv.setTextColor(0xff333333);
                tv.setText("搜索");
            }

            @Override
            public void onclick() {
                if(tvRight.getVisibility()==View.VISIBLE) {
                    MobclickAgent.onEvent(ProductFromSearchActivity.this, "searchGoodsBtnClick");
                    key = tv_name_serach.getText().toString();
                    if (TextUtils.isEmpty(key)) Tst.showToast("请输入搜索关键字");
                    else {
                        resetData(key,"","");

                    }
                }else{
                    startActivityAnimGeneral(MessageCenterActivity.class,null);
                }
            }
        });
    }

    @Override
    protected void initData() {
        v_drawer_layout.setOnCloseListener(this);
        styleList = new ArrayList<Style>();
        styleList.add(0, new Style("全部"));
        brandList = new ArrayList<Brand>();
        brandList.add(0, new Brand("全部"));

        reqBrand();
        reqStyle();
        tv_name_serach.setText(key);
        tv_name_serach.setTextChangedListener(new XEditText.TextChangedListener() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(TextUtils.isEmpty(s.toString())) searchRecyclerView.setAdapter(new SearchAdapter(null));
                else if(!TextUtils.isEmpty(s.toString())&&tv_name_serach.isFocusable())  searchProductMatch(s.toString());
            }
            @Override
            public void onTextChangedAfter(Editable editable) {}
        });
        tv_name_serach.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b){ivRight.setVisibility(View.GONE);tvRight.setVisibility(View.VISIBLE);searchRecyclerView.setVisibility(View.VISIBLE);
                    searchProductMatch(key);
                    if(v_drawer_layout.isDrawerOpen())v_drawer_layout.closeDrawer();
                }
                else {ivRight.setVisibility(View.VISIBLE);tvRight.setVisibility(View.GONE);searchRecyclerView.setVisibility(View.GONE);}
            }
        });
        rl_style.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvClickCurrent = tv_style;
                if (v_drawer_layout.isDrawerOpen()) {
                    setTextViewDrawableRight(tv_style, false);
                    setTextViewDrawableRight(tv_brand, false);
                    v_drawer_layout.closeDrawer();
                } else {
                    setTextViewDrawableRight(tv_style, true);
                    final StyleAdapter adapter = new StyleAdapter(styleList);
                    adapter.mSelectedStyleId = styleId;
                    recyclerView.setAdapter(adapter);
                    adapter.setOnRecyclerViewItemClickListener(new BaseQuickAdapter.OnRecyclerViewItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            Style item = adapter.getData().get(position);
                            styleId = item.id;
                            adapter.mSelectedStyleId = styleId;
                            adapter.notifyItemRangeChanged(0, adapter.getItemCount());
                            tv_style.setText("全部".equals(item.name) ? "风格" : item.name);
                            refresh_recyclerView.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    v_drawer_layout.closeDrawer();
                                }
                            }, 100);
                            refresh_recyclerView.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    refresh_recyclerView.setRefreshing();
                                    setTextViewDrawableRight(tv_style, false);
                                }
                            }, 200);
                        }
                    });
                    v_drawer_layout.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            v_drawer_layout.openDrawerView();
                        }
                    }, 200);

                }
            }
        });
        rl_brand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvClickCurrent = tv_brand;
                if (v_drawer_layout.isDrawerOpen()) {
                    setTextViewDrawableRight(tv_brand, false);
                    setTextViewDrawableRight(tv_style, false);
                    v_drawer_layout.closeDrawer();
                } else {
                    setTextViewDrawableRight(tv_brand, true);
                     final BrandAdapter brandAdapter = new BrandAdapter(brandList);
                    if (brandList.size()> 12) {
                        recyclerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LocalDisplay.dp2px(176)));
                    }
                    brandAdapter.mSelectItembrandId = brandId;
                    recyclerView.setAdapter(brandAdapter);
                    brandAdapter.setOnRecyclerViewItemClickListener(new BaseQuickAdapter.OnRecyclerViewItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            Brand item = brandAdapter.getData().get(position);
                            brandId = item.id;
                            brandAdapter.mSelectItembrandId = brandId;
                            brandAdapter.notifyItemRangeChanged(0, brandAdapter.getItemCount());
                            tv_brand.setText("全部".equals(item.name) ? "大牌" : item.name);
                            refresh_recyclerView.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    v_drawer_layout.closeDrawer();
                                }
                            }, 100);
                            refresh_recyclerView.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    refresh_recyclerView.setRefreshing();
                                    setTextViewDrawableRight(tv_brand, false);
                                }
                            }, 200);
                        }
                    });
                    v_drawer_layout.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            v_drawer_layout.openDrawerView();
                        }
                    }, 200);
                }
            }
        });
        tvBtn_default.setOnClickListener(this);
        tvBtn_buzz.setOnClickListener(this);
        rlBtn_price.setOnClickListener(this);
        findView(R.id.iv_back).setOnClickListener(this);
        findView(R.id.top).setOnClickListener(this);
//        findView(R.id.iv_icon_right).setOnClickListener(this);
        adatper.setOnItemClickListener(new com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(com.chad.library.adapter.base.BaseQuickAdapter adapter, View view, int position) {
                ClassifyGood item = adatper.getItem(position);
                if (item != null) {
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("id", item.id);
                    startActivityAnimGeneral(item.gotype==6?ProductDetailsActivity.class:item.isRecommend == 1 ? ProductDetailsBaokActivity.class : ProductDetailsOrdinaryActivity.class, params);
                }
            }
        });
        refresh_recyclerView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);

        refresh_recyclerView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<RecyclerView>() {
            @Override
            public void onRefresh(PullToRefreshBase<RecyclerView> refreshView) {
                pageNum = 1;
                recyclerView_content.scrollToPosition(0);
                reqApi();
            }
        });
        adatper.setEnableLoadMore(true);
        adatper.setOnLoadMoreListener(new com.chad.library.adapter.base.BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                reqApi();
            }
        }, recyclerView);
        refreshData();
    }

    private void refreshData(){
        refresh_recyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                refresh_recyclerView.setRefreshing();
            }
        }, 500);
    }

    @Override
    public String setStatisticsTitle() {
        return "搜索商品列表";
    }


    private void reqStyle() {
        if (v_drawer_layout.isDrawerOpen()) {
            setTextViewDrawableRight(tv_style, false);
            v_drawer_layout.closeDrawer();
            return;
        }
        showProgress();
        ReqApi.getInstance().postMethod(HttpConstant.styles, null, new AsyncHttpCallBack<List<Style>>() {
            @Override
            public void onSuccess(ResponseBody<List<Style>> body) {
                hideProgress();
                if (body.isSuccess()) {
                    styleList.addAll(body.data);
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
                return new TypeToken<ResponseBody<List<Style>>>() {
                }.getType();
            }
        });
    }

    private void reqBrand() {
        showProgress();
        RxRequestApi.getInstance().getApiService().searchProductBrand(sub(key), labelId)
                .map(new Func1<ResponseBody<List<Label>>, ResponseBody<List<Brand>>>() {
                    @Override
                    public ResponseBody<List<Brand>> call(ResponseBody<List<Label>> body) {
                        ResponseBody<List<Brand>> responseBody = new ResponseBody<>();
                        responseBody.code = body.code;
                        responseBody.desc = body.desc;
                        List<Brand> brands = null;
                        if (body.data != null) {
                            brands = new ArrayList<Brand>();
                            for (Label leable : body.data) {
                                Brand brand = new Brand();
                                brand.name = leable.name;
                                brand.id = leable.id;
                                if (!TextUtils.isEmpty(brandId) && brandId.equals(leable.id)) {
                                    initBrand = brand;
                                }
                                brands.add(brand);
                            }
                        }
                        responseBody.data = brands;
                        return responseBody;
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<ResponseBody<List<Brand>>>() {
                    @Override
                    public void call(ResponseBody<List<Brand>> body) {
                        hideProgress();
                        if (body.isSuccess()) {
                            brandList.addAll(body.data);
                            tv_brand.setText(initBrand != null ? initBrand.name : "大牌");
                            setTextViewDrawableRight(tv_brand, false);
                        } else {
                            Tst.showToast(body.desc);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        hideProgress();
                        Tst.showToast(throwable.toString());
                    }
                });
    }

    @Override
    protected void reqApi() {
        if (pageNum == 1) KnmsApp.getInstance().hideLoadView(rl_status);
        RxRequestApi.getInstance().getApiService().searchProduct(sub(key), labelId, styleId, brandId, isHot, pageNum)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<ResponseBody<List<ClassifyGood>>>() {
                    @Override
                    public void call(ResponseBody<List<ClassifyGood>> body) {
                        refresh_recyclerView.onRefreshComplete();
                        if (body.isSuccess()) {
                            updateView(body.data);
                            pageNum++;
                        } else {
                            Tst.showToast(body.desc);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        refresh_recyclerView.onRefreshComplete();
                        Tst.showToast(throwable.toString());
                    }
                });
    }

    private void updateView(List<ClassifyGood> data) {
        if (pageNum == 1) {
            if (data != null && data.size() > 0) {
                adatper.setNewData(data);
            } else {
                adatper.setNewData(new ArrayList<ClassifyGood>(0));
                KnmsApp.getInstance().showDataEmpty(rl_status, "抱歉，没有找到相关的商品", R.drawable.no_data_shop);
            }
        } else {
            if (data != null && data.size() > 0) {
                adatper.addData(data);
                adatper.loadMoreComplete();
                recyclerView.scrollBy(0, 20);//防止加载更多时候有些情况不会更新item到视图中
            } else {
                adatper.loadMoreEnd();
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.tvBtn_default:
                refresh_recyclerView.onRefreshComplete();
                isHot = 0;
                tvBtn_default.setTextColor(getResources().getColor(R.color.common_red));
                tvBtn_buzz.setTextColor(Color.parseColor("#6C6C6C"));
                tv_price.setTextColor(Color.parseColor("#6C6C6C"));
                refresh_recyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refresh_recyclerView.setRefreshing(true);
                    }
                }, 100);
                break;
            case R.id.tvBtn_buzz:
                refresh_recyclerView.onRefreshComplete();
                isHot = 1;
                tvBtn_buzz.setTextColor(getResources().getColor(R.color.common_red));
                tvBtn_default.setTextColor(Color.parseColor("#6C6C6C"));
                tv_price.setTextColor(Color.parseColor("#6C6C6C"));
                refresh_recyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refresh_recyclerView.setRefreshing(true);
                    }
                }, 100);
                break;
            case R.id.rlBtn_price:
                refresh_recyclerView.onRefreshComplete();
                tv_price.setTextColor(getResources().getColor(R.color.common_red));
                tvBtn_default.setTextColor(Color.parseColor("#6C6C6C"));
                tvBtn_buzz.setTextColor(Color.parseColor("#6C6C6C"));
                refresh_recyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refresh_recyclerView.setRefreshing(true);
                    }
                }, 100);
                break;
            case R.id.top:
                layoutManager.scrollToPositionWithOffset(0, 0);//滚动到第一行
                break;
            case R.id.iv_back:
                finshActivity();
                break;
        }
    }

    private void setTextViewDrawableRight(TextView tv, boolean isOpen) {
        if (tv.getText().equals("风格") || tv.getText().equals("大牌")) {
            tv.setTextColor(getResources().getColor(R.color.color_black_6c6c6c));
            Drawable drawable = getResources().getDrawable(isOpen ? R.drawable.icon_gray_up : R.drawable.icon_only_down);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            tv.setCompoundDrawables(null, null, drawable, null);
            return;
        }
        tv.setTextColor(getResources().getColor(R.color.common_red));
        Drawable drawable = getResources().getDrawable(isOpen ? R.drawable.icon_only_up : R.drawable.icon_red_down);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        tv.setCompoundDrawables(null, null, drawable, null);
    }

    @Override
    public void onClose() {
        setTextViewDrawableRight(tvClickCurrent, false);
    }

    private void searchProductMatch(final String keys) {
        RxRequestApi.getInstance().getApiService().searchProductMatch(sub(keys)).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).cache()
                .subscribe(new Action1<ResponseBody<List<Label>>>() {
                    @Override
                    public void call(ResponseBody<List<Label>> body) {
                        if (body.isSuccess()) {
                            final SearchAdapter adapter = new SearchAdapter(body.data);
                            searchRecyclerView.setAdapter(adapter);
                            adapter.setOnRecyclerViewItemClickListener(new BaseQuickAdapter.OnRecyclerViewItemClickListener() {
                                @Override
                                public void onItemClick(View view, int position) {
                                    Label lableParent = adapter.getItem(position);
                                    resetData(lableParent.name,"",lableParent.id);

                                }
                            });
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
    }

    private void saveHistory(String key) {
        List<Label> labels = new ArrayList<Label>();
        labels = SPUtils.getSerializable(SPUtils.KeyConstant.searchProductHistory, labels.getClass());
        if (labels == null) labels = new ArrayList<>();
        Label label = new Label();
        label.name = key;
        labels.remove(label);
        if(labels.size()==10) labels.remove(9);
        labels.add(0, label);
        SPUtils.saveSerializable(SPUtils.KeyConstant.searchProductHistory, labels);
    }

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText(key);
    }

    private String sub(String valus){

        return valus.length() > 10 ? valus.substring(0, 10) : valus;
    }


    @Subscribe(tags = {@Tag(BusAction.SEARCH_PRODUCT)})
    public void searchProduct(Label label){
        resetData(label.name,label.id,label.parentId);
    }


    private void resetData(String strKey,String strBrand,String strLabelId){
        searchRecyclerView.setVisibility(View.GONE);
        tvBtn_default.setTextColor(getResources().getColor(R.color.common_red));
        tvBtn_buzz.setTextColor(Color.parseColor("#6C6C6C"));
        pageNum=1;
        styleId="";isHot=0;
        key=strKey;
        brandId=strBrand;
        labelId=strLabelId;
        tv_name_serach.clearFocus();
        tv_name_serach.setText(key);
        initBrand=null;
        tv_style.setText("风格");
        tv_brand.setText("大牌");
        setTextViewDrawableRight(tv_style, false);
        setTextViewDrawableRight(tv_brand, false);
        reqBrand();
        saveHistory(key);
        refreshData();
        hideKeyboard();
    }

    Subscription subscriptionMsgCount;
    @Override
    protected void onResume() {
        super.onResume();
        if(subscriptionMsgCount != null) subscriptionMsgCount.unsubscribe();
        subscriptionMsgCount = IMHelper.getInstance().isUnreadMsg().subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean aBoolean) {
                if(aBoolean) ivRight.setImageResource(R.drawable.home_03);
                else ivRight.setImageResource(R.drawable.home_12);
            }
        });
    }
    float mPosX,mPosY,mCurPosX,mCurPosY;
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPosX = event.getX();
                mPosY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                mCurPosX = event.getX();
                mCurPosY = event.getY();
                if(mPosY - mCurPosY > 50||mCurPosY - mPosY > 50) {
                    hideKeyboard();
                }
                break;
        }
        return super.dispatchTouchEvent(event);
    }
    @Override
    protected void onDestroy() {
        if(subscriptionMsgCount != null) subscriptionMsgCount.unsubscribe();
        super.onDestroy();
    }
}
