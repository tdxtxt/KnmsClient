package com.knms.activity.goods;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshRecyclerView;
import com.knms.activity.base.HeadNotifyBaseActivity;
import com.knms.activity.details.ProductDetailsOrdinaryActivity;
import com.knms.activity.im.MessageCenterActivity;
import com.knms.activity.login.FasterLoginActivity;
import com.knms.activity.details.base.CannotBuyBaseDetailsActivity;
import com.knms.adapter.BrandAdapter;
import com.knms.adapter.ProductMainAdapter;
import com.knms.adapter.StyleAdapter;
import com.knms.adapter.baserecycler.BaseQuickAdapter;
import com.knms.android.R;
import com.knms.app.KnmsApp;
import com.knms.bean.ResponseBody;
import com.knms.bean.other.Brand;
import com.knms.bean.other.Style;
import com.knms.bean.product.ClassifyGood;
import com.knms.net.HttpConstant;
import com.knms.net.ReqApi;
import com.knms.oncall.AsyncHttpCallBack;
import com.knms.util.ImageLoadHelper;
import com.knms.util.LocalDisplay;
import com.knms.util.SPUtils;
import com.knms.util.Tst;
import com.knms.view.VerticalDrawerLayout;
import com.knms.view.clash.FullyGridLayoutManager;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Subscription;


/**
 * Created by tdx on 2016/8/30.
 * 分类商品
 */
public class ProductMainActivity extends HeadNotifyBaseActivity implements View.OnClickListener ,VerticalDrawerLayout.OnCloseListener{
    private String id, title;
    private String styleId, brandId;//styleId风格id;brandId品牌id
    private int isHot = 0;//isHot是否人气（1：是，0：否
    private int pageNum = 1;
    TextView tvBtn_default, tvBtn_buzz, tv_price, tv_style, tv_brand;
    RelativeLayout rlBtn_price, rl_style, rl_brand, rl_status;
    LinearLayout ll_menu;
    VerticalDrawerLayout v_drawer_layout;
    RecyclerView recyclerView, recyclerView_content;
    PullToRefreshRecyclerView refresh_recyclerView;
    ProductMainAdapter adapter;
    GridLayoutManager layoutManager;
    private ImageButton btnTop;
    private boolean initStyle = false, initBrand = false;
    private List<Style> styleList;
    private List<Brand> brandList;
    private TextView tvClickCurrent;


    @Override
    protected int layoutResID() {
        return R.layout.activity_product_main;
    }

    @Override
    protected void getParmas(Intent intent) {
        id = intent.getStringExtra("id");
        title = intent.getStringExtra("title");
        styleId = intent.getStringExtra("styleId");
        brandId = intent.getStringExtra("brandId");
        if (!TextUtils.isEmpty(styleId)) initStyle = true;
        if (!TextUtils.isEmpty(brandId)) initBrand = true;
    }

    @Override
    protected void initView() {
        rl_style = findView(R.id.rl_style);
        rl_brand = findView(R.id.rl_brand);
        tv_style = findView(R.id.tv_style);
        tv_brand = findView(R.id.tv_brand);
        ll_menu = findView(R.id.ll_menu);
        rl_status = findView(R.id.rl_status);

        tvBtn_default = findView(R.id.tvBtn_default);
        tvBtn_buzz = findView(R.id.tvBtn_buzz);
        rlBtn_price = findView(R.id.rlBtn_price);
        tv_price = findView(R.id.tv_price);
//        ((TextView) findView(R.id.tv_name_main)).setText(title);
        btnTop = findView(R.id.top);
        findView(R.id.edit_title_center).setVisibility(View.GONE);
        v_drawer_layout = findView(R.id.v_drawer_layout);
        recyclerView = findView(R.id.recyclerView);
        refresh_recyclerView = findView(R.id.refresh_recyclerView);
        recyclerView_content = refresh_recyclerView.getRefreshableView();
        recyclerView.setLayoutManager(new FullyGridLayoutManager(this,3,true));
        layoutManager = new GridLayoutManager(this, 2);
        recyclerView_content.setLayoutManager(layoutManager);
        adapter = new ProductMainAdapter(this,new ArrayList<ClassifyGood>());
        recyclerView_content.setAdapter(adapter);
        recyclerView_content.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE://静止,没有滚动
                        ImageLoadHelper.getInstance().resume(ProductMainActivity.this);
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING://正在被外部拖拽,一般为用户正在用手指滚动
                        ImageLoadHelper.getInstance().pause(ProductMainActivity.this);
                        break;
                    case RecyclerView.SCROLL_STATE_SETTLING://自动滚动开始
                        ImageLoadHelper.getInstance().pause(ProductMainActivity.this);
                        break;
                }
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (recyclerView.getChildCount() == 0)
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
        rl_style.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvClickCurrent=tv_style;
                if (v_drawer_layout.isDrawerOpen()) {
                    setTextViewStyle(tv_style, false);
                    setTextViewStyle(tv_brand,false);
                    v_drawer_layout.closeDrawer();
                } else {
                    setTextViewStyle(tv_style, true);
                    final StyleAdapter adapter = new StyleAdapter(styleList);
                    recyclerView.setAdapter(adapter);
                    adapter.mSelectedStyleId = styleId;
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
                                    setTextViewStyle(tv_style, false);
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
                tvClickCurrent=tv_brand;
//                reqBrand();
                if (v_drawer_layout.isDrawerOpen()) {
                    setTextViewStyle(tv_brand, false);
                    setTextViewStyle(tv_style,false);
                    v_drawer_layout.closeDrawer();
                } else {
                    setTextViewStyle(tv_brand, true);
                    final BrandAdapter adapter = new BrandAdapter(brandList);
                    if(brandList.size()>12){
                        recyclerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LocalDisplay.dp2px(176)));
                    }
                    adapter.mSelectItembrandId = brandId;
                    recyclerView.setAdapter(adapter);
                    adapter.setOnRecyclerViewItemClickListener(new BaseQuickAdapter.OnRecyclerViewItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            Brand item = adapter.getData().get(position);
                            brandId = item.id;
                            adapter.mSelectItembrandId = brandId;
                            adapter.notifyItemRangeChanged(0, adapter.getItemCount());
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
                                    setTextViewStyle(tv_brand, false);
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
//        findView(R.id.iv_back).setOnClickListener(this);
        findView(R.id.top).setOnClickListener(this);
        findView(R.id.iv_icon_right).setOnClickListener(this);
        refresh_recyclerView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        refresh_recyclerView.setScrollingWhileRefreshingEnabled(true);//刷新可滑动
        refresh_recyclerView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<RecyclerView>() {
            @Override
            public void onRefresh(PullToRefreshBase<RecyclerView> refreshView) {
                recyclerView_content.scrollToPosition(0);
                pageNum = 1;
                reqApi();
            }
        });
        adapter.setOnItemClickListener(new com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(com.chad.library.adapter.base.BaseQuickAdapter adapter, View view, int position) {
                ClassifyGood item = (ClassifyGood) adapter.getItem(position);
                if (item != null) {
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("id", item.id);
                    startActivityAnimGeneral(ProductDetailsOrdinaryActivity.class, params);
                }
            }
        });
        adapter.setEnableLoadMore(true);
        adapter.setOnLoadMoreListener(new com.chad.library.adapter.base.BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                reqApi();
            }
        },recyclerView);
        refresh_recyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                refresh_recyclerView.setRefreshing();
            }
        }, 1000);
    }

    @Override
    public String setStatisticsTitle() {
        return "分类商品";
    }

    Subscription subscriptionMsgCount;

    @Override
    protected void onDestroy() {
        if (subscriptionMsgCount != null) subscriptionMsgCount.unsubscribe();
        super.onDestroy();
    }

    private void reqStyle() {
        showProgress();
        ReqApi.getInstance().postMethod(HttpConstant.styles, null, new AsyncHttpCallBack<List<Style>>() {
            @Override
            public void onSuccess(ResponseBody<List<Style>> body) {
                hideProgress();
                if (body.isSuccess()) {
                    if (body.data != null && body.data.size() > 0&&initStyle) {
                        for (Style item : body.data) {
                            if (styleId.equals(item.id)) {
                                tv_style.setText(item.name);
                                setTextViewStyle(tv_style,false);
                                break;
                            }
                        }
                    }
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
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);
        ReqApi.getInstance().postMethod(HttpConstant.brands, params, new AsyncHttpCallBack<List<Brand>>() {
            @Override
            public void onSuccess(ResponseBody<List<Brand>> body) {
                hideProgress();
                if (body.isSuccess()) {
                    if (body.data != null && body.data.size() > 0&&initBrand) {
                        for (Brand item : body.data) {
                            if (brandId.equals(item.id)) {
                                tv_brand.setText(item.name);
                                setTextViewStyle(tv_brand,false);
                                break;
                            }
                        }
                    }
                    brandList.addAll(body.data);
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
                return new TypeToken<ResponseBody<List<Brand>>>() {
                }.getType();
            }
        });
    }

    @Override
    protected void reqApi() {
        HashMap<String, Object> parmas = new HashMap<String, Object>();
        parmas.put("pageIndex", pageNum);
        parmas.put("id", id);
        if (!TextUtils.isEmpty(styleId)) parmas.put("styleid", styleId);
        if (!TextUtils.isEmpty(brandId)) parmas.put("brandid", brandId);
        parmas.put("isHot", isHot);
        ReqApi.getInstance().postMethod(HttpConstant.productList, parmas, new AsyncHttpCallBack<List<ClassifyGood>>() {
            @Override
            public void onSuccess(ResponseBody<List<ClassifyGood>> body) {
                refresh_recyclerView.onRefreshComplete();
                if (body.isSuccess()) {
                    updateView(body.data);
                    pageNum++;
                } else {
                    Tst.showToast(body.desc);
                }
            }

            @Override
            public void onFailure(String msg) {
                refresh_recyclerView.onRefreshComplete();
                Tst.showToast(msg);
            }

            @Override
            public Type setType() {
                return new TypeToken<ResponseBody<List<ClassifyGood>>>() {
                }.getType();
            }
        });
    }

    private void updateView(List<ClassifyGood> data) {
        if (data == null) return;
        if (pageNum == 1) {
            if (data.size() > 0) {
                KnmsApp.getInstance().hideLoadView(rl_status);
                adapter.setNewData(data);
            } else
                KnmsApp.getInstance().showDataEmpty(rl_status, "抱歉，没有找到相关的商品", R.drawable.no_data_shop);
        } else {
            if (data != null && data.size() > 0) {
                adapter.addData(data);
                adapter.loadMoreComplete();
//                recyclerView.scrollBy(0,20);//防止加载更多时候有些情况不会更新item到视图中
            } else {
                adapter.loadMoreEnd();
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.tvBtn_default://默认
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
            case R.id.tvBtn_buzz://人气排序
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
            case R.id.rlBtn_price://价格排序
                refresh_recyclerView.onRefreshComplete();
                tv_price.setTextColor(getResources().getColor(R.color.common_red));
                tvBtn_default.setTextColor(Color.parseColor("#6C6C6C"));
                tvBtn_buzz.setTextColor(Color.parseColor("#6C6C6C"));
                isHot = 1;
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
//            case R.id.iv_back:
//                finshActivity();
//                break;
            case R.id.iv_icon_right:
                if (SPUtils.isLogin()) {
                    startActivityAnimGeneral(MessageCenterActivity.class, null);
                } else {
                    startActivityAnimGeneral(FasterLoginActivity.class, null);
                }
                break;
        }
    }


    private void setTextViewStyle(TextView tv, boolean isOpen) {
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
        setTextViewStyle(tvClickCurrent,false);
    }

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText(title);
        tv_center.setVisibility(View.VISIBLE);
    }
}
