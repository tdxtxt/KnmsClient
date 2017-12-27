package com.knms.activity.search;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.knms.activity.ShopActivityF;
import com.knms.activity.base.BaseActivity;
import com.knms.adapter.CowryClassificationRcyAdapter;
import com.knms.adapter.LabelAdapter;
import com.knms.adapter.SearchAdapter;
import com.knms.adapter.baserecycler.BaseQuickAdapter;
import com.knms.android.R;
import com.knms.app.KnmsApp;
import com.knms.bean.ResponseBody;
import com.knms.bean.idle.ReIdleClassify;
import com.knms.bean.other.Label;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.net.RxRequestApi;
import com.knms.util.DialogHelper;
import com.knms.util.SPUtils;
import com.knms.util.Tst;
import com.knms.view.XEditText;
import com.knms.view.flowlayout.FlowLayout;
import com.knms.view.flowlayout.TagFlowLayout;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;


/**
 * Created by tdx on 2016/9/21.
 * 搜索界面
 */
public class SearchActivity extends BaseActivity implements View.OnClickListener {
    RelativeLayout rl_head, rl_recommend;
    TextView tv_product, tv_shop, tv_idle, tvBtn_search,tv_hotSearch;
    XEditText edt_search;
    RecyclerView recyclerView;
    TagFlowLayout label_layout_hot, label_layout_history;

    public static final int id_product = 11;
    static final int id_idle = 12;
    static final int id_shop = 13;


    String currentMode;
    //    Map<String,String> historyKey = new HashMap<>();
    final String ProductMode = "Product";
    final String IdleMode = "Idle";
    final String ShopMode = "Shop";

    RelativeLayout.LayoutParams lp_product, lp_shop, lp_idle;

    CompositeSubscription compositeSubscription = new CompositeSubscription();

    int type = 0;

    @Override
    protected void getParmas(Intent intent) {
        super.getParmas(intent);
        type = intent.getIntExtra("type", 0);
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_search;
    }

    @SuppressWarnings("ResourceType")
    @Override
    protected void initView() {
        tv_hotSearch=findView(R.id.tv_hot_search);
        rl_head = findView(R.id.rl_head);
        edt_search = findView(R.id.edt_search);
        recyclerView = findView(R.id.recyclerView);
        rl_recommend = findView(R.id.rl_recommend);
        tvBtn_search = findView(R.id.tvBtn_search);
        label_layout_hot = findView(R.id.label_layout_hot);
        label_layout_history = findView(R.id.label_layout_history);

        tv_product = new TextView(this);
        tv_shop = new TextView(this);
        tv_idle = new TextView(this);
        tv_product.setText("商品");
        tv_shop.setText("店铺");
        tv_idle.setText("闲置");
        tv_product.setTextSize(14);
        tv_idle.setTextSize(14);
        tv_shop.setTextSize(14);
        tv_product.setId(id_product);
        tv_shop.setId(id_shop);
        tv_idle.setId(id_idle);


        tv_product.setGravity(Gravity.CENTER);
        tv_shop.setGravity(Gravity.CENTER);
        tv_idle.setGravity(Gravity.CENTER);
    }

    @Override
    protected void initData() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        lp_product = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp_product.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        lp_shop = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp_shop.addRule(RelativeLayout.CENTER_IN_PARENT);
        lp_idle = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp_idle.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        tv_shop.setOnClickListener(this);
        tv_idle.setOnClickListener(this);
        tv_product.setOnClickListener(this);
        tvBtn_search.setOnClickListener(this);
        findView(R.id.ivBtn_delete).setOnClickListener(this);
        findView(R.id.back).setOnClickListener(this);
        findView(R.id.back).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                hideKeyboard();
                return false;
            }
        });
        edt_search.setTextChangedListener(new XEditText.TextChangedListener() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(currentMode.equals(ShopMode)) return;
                searchMatch(s.toString());
            }
            @Override
            public void onTextChangedAfter(Editable editable) {}
        });

        label_layout_hot.setOnTagClickListener(new TagFlowLayout.OnTagClickListener() {
            @Override
            public boolean onTagClick(View view, int position, FlowLayout parent) {
                Label lableParent = (Label) view.getTag();
                Map<String, Object> params = new HashMap<String, Object>();
                if (ShopMode.equals(currentMode)) {
                    MobclickAgent.onEvent(SearchActivity.this,"shopHotSearchClick");
                    params.put("shopId", lableParent.id);
                    startActivityAnimGeneral(ShopActivityF.class, params);
                } else {
                    lableParent.id = "";
                    lableParent.parentId = "";
                    params.put("key", lableParent.name);
                    params.put("labelId", lableParent.id);
                    MobclickAgent.onEvent(SearchActivity.this,ProductMode.equals(currentMode)?"goodsHotSearchClick":"IdleHotSearchClick");
                    startActivityAnimGeneral(ProductMode.equals(currentMode)?ProductFromSearchActivity.class:IdleFromSearchActivity.class, params);
                }
                saveHistory(lableParent);
                initHistory();
                return false;
            }
        });
        label_layout_history.setOnTagClickListener(new TagFlowLayout.OnTagClickListener() {
            @Override
            public boolean onTagClick(View view, int position, FlowLayout parent) {
                Label lableParent = (Label) view.getTag();
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("key", lableParent.name);
                if (TextUtils.isEmpty(lableParent.parentId)) {
                    params.put("labelId", lableParent.id);
                } else {
                    params.put("labelId", lableParent.parentId);
                    params.put("brandId", lableParent.id);
                }
                String eventStr="";
                switch (type){
                    case 0:
                        eventStr="goodsHistorySearchClick";
                        break;
                    case 1:
                        eventStr="shopHistorySearchClick";
                        break;
                    case 2:
                        eventStr="IdleHistorySearchClick";
                        break;
                }
                skip(params,eventStr);
                return false;

            }
        });
        if (type == 0) setProduct();
        else if (type == 1) setShop();
        else if (type == 2) setIdle();
    }

    @Override
    public String setStatisticsTitle() {
        return "搜索页";
    }

    private void searchMatch(String key) {
        if (TextUtils.isEmpty(key)) {
            rl_recommend.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            rl_recommend.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        if (ProductMode.equals(currentMode)) {
            searchProductMatch(key);
        } else if (ShopMode.equals(currentMode)) {

        } else {
            searchIdelMatch(key);
        }
    }

    void setProduct() {
        if (ProductMode.equals(currentMode)) return;
        tv_shop.setTextColor(Color.parseColor("#333333"));
        tv_idle.setTextColor(Color.parseColor("#333333"));
        tv_product.setTextColor(Color.parseColor("#FFFFFF"));
        rl_head.removeAllViews();
        tv_idle.setBackgroundResource(R.drawable.icon_search_other);
        rl_head.addView(tv_idle, lp_idle);

        tv_shop.setBackgroundResource(R.drawable.icon_search_other);
        rl_head.addView(tv_shop, lp_shop);

        tv_product.setBackgroundResource(R.drawable.icon_search_current);
        rl_head.addView(tv_product, lp_product);
        edt_search.setHint("搜商品");
        currentMode = ProductMode;

//        rl_recommend.setVisibility(View.VISIBLE);
//        recyclerView.setAdapter(null);
        initProduct();

        if(!TextUtils.isEmpty(getKey()))searchMatch(getKey());

    }

    void setShop() {
        if (ShopMode.equals(currentMode)) return;
        tv_product.setTextColor(Color.parseColor("#333333"));
        tv_idle.setTextColor(Color.parseColor("#333333"));
        tv_shop.setTextColor(Color.parseColor("#FFFFFF"));
        rl_head.removeAllViews();

        tv_idle.setBackgroundResource(R.drawable.icon_search_other);
        rl_head.addView(tv_idle, lp_idle);

        tv_product.setBackgroundResource(R.drawable.icon_search_other);
        rl_head.addView(tv_product, lp_product);

        tv_shop.setBackgroundResource(R.drawable.icon_search_current);
        rl_head.addView(tv_shop, lp_shop);
        edt_search.setHint("搜店铺");
        currentMode = ShopMode;

        rl_recommend.setVisibility(View.VISIBLE);
        recyclerView.setAdapter(null);

        initShopHotLabels();
        initHistory();
    }

    void setIdle() {
        if (IdleMode.equals(currentMode)) return;
        tv_product.setTextColor(Color.parseColor("#333333"));
        tv_shop.setTextColor(Color.parseColor("#333333"));
        tv_idle.setTextColor(Color.parseColor("#FFFFFF"));
        rl_head.removeAllViews();

        tv_product.setBackgroundResource(R.drawable.icon_search_other);
        rl_head.addView(tv_product, lp_product);

        tv_shop.setBackgroundResource(R.drawable.icon_search_other);
        rl_head.addView(tv_shop, lp_shop);

        tv_idle.setBackgroundResource(R.drawable.icon_search_current);
        rl_head.addView(tv_idle, lp_idle);
        edt_search.setHint("搜闲置");
        currentMode = IdleMode;

//        rl_recommend.setVisibility(View.VISIBLE);
//        recyclerView.setAdapter(null);

        initIdleHotLabels();
        initHistory();

        if(!TextUtils.isEmpty(getKey()))searchMatch(getKey());
    }

    private void initProduct() {
        initHotLabels();
        initHistory();
    }

    //商品热门标签
    private void initHotLabels() {
        compositeSubscription.add(RxRequestApi.getInstance().getApiService().getHotLabels()
                .doOnNext(new Action1<ResponseBody<List<Label>>>() {
                    @Override
                    public void call(ResponseBody<List<Label>> listResponseBody) {

                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).cache()
                .subscribe(new Action1<ResponseBody<List<Label>>>() {
                    @Override
                    public void call(ResponseBody<List<Label>> body) {
                        hideProgress();
                        if (body.isSuccess()) {
                            tv_hotSearch.setVisibility(body.data==null||body.data.size()==0?View.GONE:View.VISIBLE);
                            LabelAdapter adapter = new LabelAdapter(body.data, false);
                            label_layout_hot.setAdapter(adapter);
                        } else {
                            tv_hotSearch.setVisibility(View.GONE);
                            label_layout_hot.removeAllViews();
                            Tst.showToast(body.desc);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable e) {
                        tv_hotSearch.setVisibility(View.GONE);
                        label_layout_hot.removeAllViews();
                        Tst.showToast(e.toString());
                        hideProgress();
                    }
                }));
    }

    //闲置热门标签
    private void initIdleHotLabels() {
        compositeSubscription.add(RxRequestApi.getInstance().getApiService().getIdleHotLabels()
                .doOnNext(new Action1<ResponseBody<List<Label>>>() {
                    @Override
                    public void call(ResponseBody<List<Label>> listResponseBody) {

                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).cache()
                .subscribe(new Action1<ResponseBody<List<Label>>>() {
                    @Override
                    public void call(ResponseBody<List<Label>> body) {
                        hideProgress();
                        if (body.isSuccess()) {
                            tv_hotSearch.setVisibility(body.data==null||body.data.size()==0?View.GONE:View.VISIBLE);
                            LabelAdapter adapter = new LabelAdapter(body.data, false);
                            label_layout_hot.setAdapter(adapter);
                        } else {
                            tv_hotSearch.setVisibility(View.GONE);
                            label_layout_hot.removeAllViews();
                            Tst.showToast(body.desc);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable e) {
                        tv_hotSearch.setVisibility(View.GONE);
                        label_layout_hot.removeAllViews();
                        Tst.showToast(e.toString());
                        hideProgress();
                    }
                }));
    }

    //店铺热门标签
    private void initShopHotLabels() {
        compositeSubscription.add(RxRequestApi.getInstance().getApiService().getShopHotLabels()
                .doOnNext(new Action1<ResponseBody<List<Label>>>() {
                    @Override
                    public void call(ResponseBody<List<Label>> listResponseBody) {

                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).cache()
                .subscribe(new Action1<ResponseBody<List<Label>>>() {
                    @Override
                    public void call(ResponseBody<List<Label>> body) {
                        hideProgress();
                        if (body.isSuccess()) {
                            tv_hotSearch.setVisibility(body.data==null||body.data.size()==0?View.GONE:View.VISIBLE);
                            LabelAdapter adapter = new LabelAdapter(body.data, false);
                            label_layout_hot.setAdapter(adapter);
                        } else {
                            tv_hotSearch.setVisibility(View.GONE);
                            label_layout_hot.removeAllViews();
                            Tst.showToast(body.desc);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable e) {
                        tv_hotSearch.setVisibility(View.GONE);
                        label_layout_hot.removeAllViews();
                        Tst.showToast(e.toString());
                        hideProgress();
                    }
                }));
    }

    private void initHistory() {
        compositeSubscription.add(Observable.create(new Observable.OnSubscribe<List<Label>>() {
            @Override
            public void call(Subscriber<? super List<Label>> subscriber) {
                List<Label> labels = new ArrayList<Label>();
                labels = SPUtils.getSerializable(searchHistorys[type], labels.getClass());
                subscriber.onNext(labels);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Label>>() {
                    @Override
                    public void call(List<Label> labels) {
                        if (labels != null && labels.size() > 0) {
                            findView(R.id.ll_history_search).setVisibility(View.VISIBLE);
                            label_layout_history.setAdapter(new LabelAdapter(labels, false));
                        } else {
                            findView(R.id.ll_history_search).setVisibility(View.GONE);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Tst.showToast(throwable.toString());
                    }
                }));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case id_product:
                type = 0;
                setProduct();
                break;
            case id_shop:
                type = 1;
                setShop();
                break;
            case id_idle:
                type = 2;
                setIdle();
                break;
            case R.id.tvBtn_search:
                String key = getKey();
                if (!TextUtils.isEmpty(key)) {
                    Map<String, Object> params = new HashMap<>();
                    params.put("key", key);
                    saveHistory(key);
                    String eventStr="";
                    switch (type){
                        case 0:
                            eventStr="searchGoodsBtnClick";
                            break;
                        case 1:
                            eventStr="searchShopBtnClick";
                            break;
                        case 2:
                            eventStr="searchIdleBtnClick";
                            break;
                    }
                    skip(params,eventStr);
                } else {
                    Tst.showToast("请输入搜索关键字");
                }
                break;
            case R.id.ivBtn_delete://删除历史记录
                DialogHelper.showPromptDialog(this, null, "确认要删除全部历史搜索记录?", "取消", null, "确定", new DialogHelper.OnMenuClick() {
                    @Override
                    public void onLeftMenuClick() {
                    }

                    @Override
                    public void onCenterMenuClick() {
                    }

                    @Override
                    public void onRightMenuClick() {
                        SPUtils.clearSerializable(searchHistorys[type], ArrayList.class);
                        initHistory();
                    }
                });
                break;
            case R.id.back:
                hideKeyboard();
                finshActivity();
                break;
        }
    }

    String[] searchHistorys = {SPUtils.KeyConstant.searchProductHistory, SPUtils.KeyConstant.searchShopHistory, SPUtils.KeyConstant.searchIdleHistory};

    private void saveHistory(String key) {
        List<Label> labels = new ArrayList<Label>();
        labels = SPUtils.getSerializable(searchHistorys[type], labels.getClass());
        if (labels == null) labels = new ArrayList<>();
        Label label = new Label();
        label.name = key;
        labels.remove(label);
        if (labels.size() == 10) labels.remove(9);
        labels.add(0, label);
        SPUtils.saveSerializable(searchHistorys[type], labels);
        initHistory();
    }

    public void saveHistory(Label label) {
        if (label == null) return;
        List<Label> labels = new ArrayList<Label>();
        labels = SPUtils.getSerializable(searchHistorys[type], labels.getClass());
        if (labels == null) labels = new ArrayList<>();
        if (labels.contains(label)) {
            labels.remove(label);
        }
        if (labels.size() == 10) labels.remove(9);
        labels.add(0, label);
        SPUtils.saveSerializable(searchHistorys[type], labels);
        initHistory();
    }

    private void searchProductMatch(String key) {
        RxRequestApi.getInstance().getApiService().searchProductMatch(key.length()>10?key.substring(0,10):key).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).cache()
                .subscribe(new Action1<ResponseBody<List<Label>>>() {
                    @Override
                    public void call(ResponseBody<List<Label>> body) {
                        if (body.isSuccess()) {
                            final SearchAdapter adapter = new SearchAdapter(body.data);
                            recyclerView.setAdapter(adapter);
                            adapter.setOnRecyclerViewItemClickListener(new BaseQuickAdapter.OnRecyclerViewItemClickListener() {
                                @Override
                                public void onItemClick(View view, int position) {
                                    Label lableParent = adapter.getItem(position);
                                    saveHistory(lableParent);
                                    Map<String, Object> params = new HashMap<String, Object>();
                                    params.put("key", lableParent.name);
                                    params.put("labelId", lableParent.id);
                                    startActivityAnimGeneral(ProductFromSearchActivity.class, params);
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

    private void searchIdelMatch(String key) {
        RxRequestApi.getInstance().getApiService().searchIdelMatch(key.length()>10?key.substring(0,10):key).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).cache()
                .subscribe(new Action1<ResponseBody<List<ReIdleClassify>>>() {
                    @Override
                    public void call(ResponseBody<List<ReIdleClassify>> body) {
                        if (body.isSuccess()) {
                            final CowryClassificationRcyAdapter adapter = new CowryClassificationRcyAdapter(body.data);
                            recyclerView.setAdapter(adapter);
                            adapter.setOnRecyclerViewItemClickListener(new BaseQuickAdapter.OnRecyclerViewItemClickListener() {
                                @Override
                                public void onItemClick(View view, int position) {
                                    ReIdleClassify item = adapter.getItem(position);
                                    Map<String, Object> params = new HashMap<String, Object>();
                                    params.put("key", item.name);
                                    params.put("typeId", item.id);
                                    startActivityAnimGeneral(IdleFromSearchActivity.class, params);
                                }
                            });
                        } else {
                            recyclerView.setAdapter(null);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        recyclerView.setAdapter(null);
                    }
                });
    }

    public String getKey() {
        return edt_search.getText().toString();
    }

    @Override
    protected void onDestroy() {
        if (compositeSubscription != null) compositeSubscription.unsubscribe();
        super.onDestroy();
    }

    private void skip(Map<String, Object> params,String eventName) {
        MobclickAgent.onEvent(this,eventName);
        if (ProductMode.equals(currentMode)) {
            startActivityAnimGeneral(ProductFromSearchActivity.class, params);
        } else if (ShopMode.equals(currentMode)) {
            startActivityAnimGeneral(ShopFromSearchActivity.class, params);
        } else if (IdleMode.equals(currentMode)) {
            startActivityAnimGeneral(IdleFromSearchActivity.class, params);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initHistory();
    }

    @Subscribe(tags = {@Tag(BusAction.SEARCH_PRODUCT)})
    public void searchProduct(Label label) {
        saveHistory(label);
        if (KnmsApp.getInstance().currentActivity() == this) {
            Map<String, Object> params = new HashMap<>();
            params.put("key", label.name);
            params.put("labelId", label.parentId);
            params.put("brandId", label.id);
            startActivityAnimGeneral(ProductFromSearchActivity.class, params);
        }
    }
}
