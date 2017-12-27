package com.knms.activity.fastfind;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshRecyclerView;
import com.knms.activity.base.HeadNotifyBaseActivity;
import com.knms.activity.details.ProductDetailsOrdinaryActivity;
import com.knms.adapter.ClassifiesSelectedAdapter;
import com.knms.adapter.FindGoodsResultAdapter;
import com.knms.android.R;
import com.knms.bean.ResponseBody;
import com.knms.bean.other.Classify;
import com.knms.bean.other.Label;
import com.knms.bean.product.Goods;
import com.knms.net.RxRequestApi;
import com.knms.util.Tst;
import com.knms.view.flowlayout.TagFlowLayout;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2017/3/22.
 */

public class FastfindGoodsResultActivity extends HeadNotifyBaseActivity {

    private PullToRefreshRecyclerView pullToRefreshRecyclerView;
    private RecyclerView recyclerView;
    private FindGoodsResultAdapter mAdapter;
    private StringBuffer styles = new StringBuffer(), classifys = new StringBuffer(), classifysName = new StringBuffer();
    private int pageIndex = 1;
    private List<Label> styleLabels;
    private List<Classify> classifyLabels;
    private TagFlowLayout tagFlowLayout;
    private TextView tvNoData;
    private ImageButton btnTop;
    private GridLayoutManager layoutManager;
    private AppBarLayout appBarLayout;
    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void getParmas(Intent intent) {
        super.getParmas(intent);
        styleLabels = (List<Label>) intent.getSerializableExtra("StyleLabels");
        classifyLabels = (List<Classify>) intent.getSerializableExtra("ClassifyLabels");
        if (styleLabels == null) styleLabels = new ArrayList<>();
        if (classifyLabels == null) classifyLabels = new ArrayList<>();
        for (Classify classifyLabel :
                classifyLabels) {
            classifys.append(classifyLabel.id + ",");
            classifysName.append(classifyLabel.name + ",");
        }
        for (Label styleLabel :
                styleLabels) {
            styles.append(styleLabel.id + ",");
            Classify clf = new Classify();
            clf.id = styleLabel.id;
            clf.name = styleLabel.name;
            classifyLabels.add(clf);
        }
        if (styles.toString().endsWith(",")) styles.deleteCharAt(styles.length() - 1);
        if (classifys.toString().endsWith(",")) classifys.deleteCharAt(classifys.length() - 1);
        if (classifysName.toString().endsWith(","))
            classifysName.deleteCharAt(classifysName.length() - 1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findView(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MobclickAgent.onEvent(FastfindGoodsResultActivity.this, "quickFindGoodsResultBackBtnClick");
                finshActivity();
            }
        });
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_fastfindgoods_result_layout;
    }

    @Override
    protected void initView() {
        appBarLayout = findView(R.id.appbar);
        coordinatorLayout = findView(R.id.coordinatorLayout_f);
        btnTop = findView(R.id.iv_top);
        tvNoData = findView(R.id.tv_no_data);
        tagFlowLayout = findView(R.id.tag_flow_checked_layout);
        pullToRefreshRecyclerView = findView(R.id.ptr_rv_content);
        recyclerView = pullToRefreshRecyclerView.getRefreshableView();
        layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new FindGoodsResultAdapter(this,null);
        mAdapter.setEnableLoadMore(true);
        recyclerView.setAdapter(mAdapter);
        pullToRefreshRecyclerView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        pullToRefreshRecyclerView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<RecyclerView>() {
            @Override
            public void onRefresh(PullToRefreshBase<RecyclerView> refreshView) {
                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
                AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();
                if (behavior != null) {
                    behavior.onNestedFling(coordinatorLayout, appBarLayout, null, 0, -appBarLayout.getHeight(), true);
                }
                pageIndex = 1;
                reqApi();
            }
        });
        mAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                reqApi();
            }
        },recyclerView);
        findView(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MobclickAgent.onEvent(FastfindGoodsResultActivity.this, "eachStyleTagBtnClick");
            }
        });

    }

    @Override
    protected void initData() {
        tagFlowLayout.setAdapter(new ClassifiesSelectedAdapter(classifyLabels, false));
        recyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                pullToRefreshRecyclerView.setRefreshing();
            }
        }, 1000);

        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                Goods item = mAdapter.getItem(position);
                if (item != null) {
                    MobclickAgent.onEvent(FastfindGoodsResultActivity.this, "quickFindGoodsAnyItemClick");
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("id", item.goid);
                    startActivityAnimGeneral(ProductDetailsOrdinaryActivity.class,params);
                }
            }
        });
        btnTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
                AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();
                if (behavior != null) {
                    layoutManager.scrollToPositionWithOffset(0, 0);
//                    recyclerView.smoothScrollToPosition(0);
                    behavior.onNestedFling(coordinatorLayout, appBarLayout, null, 0, -appBarLayout.getHeight(), true);
                }
            }
        });


        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
    }

    @Override
    protected void reqApi() {
        RxRequestApi.getInstance().getApiService().fastFindGoodsList(styles.toString(), classifys.toString(), pageIndex)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<List<Goods>>>() {
                    @Override
                    public void call(ResponseBody<List<Goods>> listResponseBody) {
                        pullToRefreshRecyclerView.onRefreshComplete();
                        if (listResponseBody.code.equals("6")) {
                            tvNoData.setText("没有找到相关宝贝，推荐" + "\"" + classifysName + "\"" + "相关结果");
                            tvNoData.setVisibility(View.VISIBLE);
                            updateView(listResponseBody.data);
                            pageIndex++;
                        } else if (listResponseBody.isSuccess()) {
                            updateView(listResponseBody.data);
                            pageIndex++;
                        } else Tst.showToast(listResponseBody.desc);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        pullToRefreshRecyclerView.onRefreshComplete();
                        Tst.showToast(throwable.getMessage());
                    }
                });
    }

    public void updateView(List<Goods> data) {
        if (pageIndex == 1) {
            mAdapter.setNewData(data);
        } else {
            if(data != null && data.size() > 0){
                mAdapter.addData(data);
                mAdapter.loadMoreComplete();
//                recyclerView.scrollBy(0, 20);//防止加载更多时候有些情况不会更新item到视图中
            }else {
                mAdapter.loadMoreEnd();
            }
        }
    }

    @Override
    public String setStatisticsTitle() {
        return "快速找货列表";
    }

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("极速找货");
    }

}
