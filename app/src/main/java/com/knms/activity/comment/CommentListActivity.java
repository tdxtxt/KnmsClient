package com.knms.activity.comment;

import android.content.Intent;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshRecyclerView;
import com.knms.activity.base.HeadNotifyBaseActivity;
import com.knms.adapter.CommentAdapter;
import com.knms.android.R;
import com.knms.bean.ResponseBody;
import com.knms.bean.remark.Comment;
import com.knms.net.RxApiService;
import com.knms.net.RxRequestApi;
import com.knms.oncall.RecyclerItemClickListener;
import com.knms.util.CommonUtils;
import com.knms.util.Tst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import static com.knms.android.R.id.get;
import static com.knms.android.R.id.layout;
import static com.knms.android.R.id.tv_center;

/**
 * Created by tdx on 2017/4/26.
 * 用户评论列表/店铺评价列表
 */

public class CommentListActivity extends HeadNotifyBaseActivity {
    private PullToRefreshRecyclerView refreshLayout;
    private RecyclerView recyclerView;
    private RelativeLayout layoutTitle;
    private CheckBox checkBox;
//    private AppBarLayout appBarLayout;
//    private CoordinatorLayout coordinatorLayout;

    CommentAdapter adapter;
    int totalCount;//总评论条数
    int productCommentSize = -1;//针对商品总条数
    String goid;//商品id
    String shopId;//店铺id
    String orderId;//订单id
    int pageNum = 1;

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("用户评价" + (totalCount > 0 ? "("+totalCount+")" : ""));
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_comment_list;
    }

    @Override
    protected void getParmas(Intent intent) {
        goid = intent.getStringExtra("goid");
        shopId = intent.getStringExtra("shopId");
        orderId = intent.getStringExtra("orderId");
        totalCount = intent.getIntExtra("totalCount",0);
    }

    @Override
    protected void initView() {
        tv_title_center = findView(R.id.tv_title_center);

        refreshLayout = findView(R.id.refresh_layout);
        checkBox = findView(R.id.checkbox);
        layoutTitle = findView(R.id.rl_top_layout);
        layoutTitle.setVisibility(View.GONE);

        recyclerView = refreshLayout.getRefreshableView();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CommentAdapter();
        recyclerView.setAdapter(adapter);

        refreshLayout.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        refreshLayout.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<RecyclerView>() {
            @Override
            public void onRefresh(PullToRefreshBase<RecyclerView> refreshView) {
                pageNum = 1;
                reqApi();
            }
        });
    }

    @Override
    protected void initData() {
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshLayout.setRefreshing();
                    }
                },200);
            }
        });
        //滑动黏性
        OverScrollDecoratorHelper.setUpOverScroll(recyclerView, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                Comment item = (Comment) adapter.getItem(position);
                Map<String,Object> params = new HashMap<String, Object>();
                params.put("commentId",item.id);
                startActivityAnimGeneral(CommentInfoActivity.class,params);
            }
        });
        if(TextUtils.isEmpty(orderId)){
            adapter.setEnableLoadMore(true);
            adapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
                @Override
                public void onLoadMoreRequested() {
                    reqApi();
                }
            },recyclerView);
        }
        refreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing();
            }
        },200);
    }

    @Override
    public String setStatisticsTitle() {
        return "用户评价";
    }

    @Override
    protected void reqApi() {
        Observable<ResponseBody<List<Comment>>> observable = null;
        if(!TextUtils.isEmpty(shopId)){
            observable = RxRequestApi.getInstance().getApiService().getCommentsByShopId(shopId,pageNum);
        }else if(!TextUtils.isEmpty(goid)){
//            layoutTitle.setVisibility(View.VISIBLE); //TODO 支付商城系统-筛选商品评价
            if(checkBox.isChecked()){
                observable = RxRequestApi.getInstance().getApiService().getProductCommentsByGoid(goid,pageNum);
            }else{
                observable = RxRequestApi.getInstance().getApiService().getShopCommentsByGoid(goid,pageNum);
            }
            initCommentSize();
        }else if(!TextUtils.isEmpty(orderId)){
            observable = RxRequestApi.getInstance().getApiService().getCommentsByOrder(orderId);
            adapter.setEnableLoadMore(false);
        }
        if(observable == null) return;
        observable.compose(this.<ResponseBody<List<Comment>>>applySchedulers())
                .subscribe(new Action1<ResponseBody<List<Comment>>>() {
                    @Override
                    public void call(ResponseBody<List<Comment>> body) {
                        refreshLayout.onRefreshComplete();
                        if (body.isSuccess()) {
                            updateView(body.data);
                            if(TextUtils.isEmpty(orderId)) pageNum++;
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        refreshLayout.onRefreshComplete();
                    }
                });
    }

    private void updateView(List<Comment> data) {
        if(data == null) return;
        if(pageNum == 1){
            adapter.setNewData(data);
            if(!(data != null && data.size() > 0)){
                CommonUtils.showNotData(adapter,R.drawable.no_data_on_offer,"该商品暂无评论");
            }
        }else{
            if(TextUtils.isEmpty(orderId)){
                if(data != null && data.size() > 0){
                    adapter.addData(data);
                    adapter.loadMoreComplete();
                }else{
                    adapter.loadMoreEnd();
                }
            }
        }
    }

    private void initCommentSize(){
        if(!TextUtils.isEmpty(goid)){
            if(!checkBox.isChecked()){
                tv_title_center.setText("用户评价" + (totalCount > 0 ? "("+totalCount+")" : ""));
            }else{
                if(productCommentSize >= 0){
                    tv_title_center.setText("用户评价" + (productCommentSize > 0 ? "(" + productCommentSize + ")" : ""));
                    return;
                }
                Observable.just(goid).flatMap(new Func1<String, Observable<ResponseBody<Integer>>>() {
                    @Override
                    public Observable<ResponseBody<Integer>> call(String s) {
                        return RxRequestApi.getInstance().getApiService().getProductCommentSizeByGoid(goid);
                    }
                }).compose(this.<ResponseBody<Integer>>applySchedulers())
                        .subscribe(new Action1<ResponseBody<Integer>>() {
                            @Override
                            public void call(ResponseBody<Integer> body) {
                                if (body.isSuccess()) {
                                    productCommentSize = body.data;
                                    tv_title_center.setText("用户评价" + (productCommentSize > 0 ? "(" + productCommentSize + ")" : ""));
                                }
                            }
                        });
            }
        }

    }
}
