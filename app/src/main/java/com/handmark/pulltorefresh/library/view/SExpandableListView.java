package com.handmark.pulltorefresh.library.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.knms.android.R;

/**
 * 类描述：支持加载更多的ExpandaleListview
 * 创建人：Administrator
 * 创建时间：2017/9/6 14:22
 * 传参：
 * 返回:
 */
public class SExpandableListView extends ExpandableListView implements AbsListView.OnScrollListener {
    /**
     * 是否支持下拉刷新
     */
//    private boolean pullRefreshEnabled = true;
    /**
     * 是否支持加载更多
     */
    private boolean loadingMoreEnabled = true;
//    private ArrowRefreshHeader mRefreshHeader;

    private LoadingListener mLoadingListener;
    private View loadMoreView;
    private boolean isNoMore;//ture-没有更多了
    private ProgressBar loadMorePb;
    private TextView loadMoreDesc;


    public SExpandableListView(Context context) {
        this(context, null);
    }

    public SExpandableListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SExpandableListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initSE(context);
    }

    private void initSE(Context context) {
        /**
         * 这里是footer的填充,注意指定他的父亲为当前的listview,
         * 这里footer不用指定layoutparem是因为footer 在填充的时候已经指定了他的父view
         */

        loadMoreView = LayoutInflater.from(context).inflate(R.layout.quick_view_load_more, this, false);
        loadMorePb = (ProgressBar) loadMoreView.findViewById(R.id.loading_progress);
        loadMoreDesc = (TextView) loadMoreView.findViewById(R.id.loading_text);
        loadMoreDesc.setTextColor(Color.parseColor("#aaaaaa"));
        loadMoreView.setVisibility(GONE);
    }

    @Override
    public void setAdapter(ExpandableListAdapter adapter) {
        if (loadingMoreEnabled) {
            addFooterView(loadMoreView);
        }
        super.setAdapter(adapter);
    }
    public void loadMoreComplete(){
        this.isNoMore = false;
        loadMoreView.setVisibility(GONE);
    }
    public void loadMoreEnd(){
        this.isNoMore = true;
        noMoreAction();
    }

    private void noMoreAction() {
        loadMoreView.setVisibility(VISIBLE);
        loadMorePb.setVisibility(GONE);
        loadMoreDesc.setText("没有更多数据");

    }

    private void loadMoreAction() {
        loadMoreView.setVisibility(VISIBLE);
        loadMorePb.setVisibility(VISIBLE);
        loadMoreDesc.setText("正在加载...");
        if (mLoadingListener != null) {
            mLoadingListener.onLoadMore();
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch (scrollState) {
            case OnScrollListener.SCROLL_STATE_IDLE:
                if (loadingMoreEnabled && getLastVisiblePosition() == getAdapter().getCount() - 1) {
                    if (isNoMore) {
                        noMoreAction();
                    } else {
                        loadMoreAction();
                    }
                }
                break;
            case OnScrollListener.SCROLL_STATE_FLING:
                break;
            case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                break;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    /**
     * 加载更多回调
     */
    public interface LoadingListener {
        void onLoadMore();
    }

    public void setmLoadingListener(LoadingListener mLoadingListener) {
        this.mLoadingListener = mLoadingListener;
    }

    /**
     * 设置是否支持加载更多
     *
     * @param loadingMoreEnabled
     */
    public void setLoadingMoreEnabled(boolean loadingMoreEnabled) {
        this.loadingMoreEnabled = loadingMoreEnabled;
        setOnScrollListener(this);
    }
}
