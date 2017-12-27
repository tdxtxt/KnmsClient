package com.handmark.pulltorefresh.library;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import com.knms.android.R;

/**
 * Created by tdx on 2016/6/12.
 * 注意：使用该控件有一bug，在加载更多时候，会出现无法立即显示出来，需要手动调用 recyclerView.scrollBy(0,20);//防止加载更多时候有些情况不会更新item到视图中
 */
public class PullToRefreshRecyclerView extends PullToRefreshBase<RecyclerView> {
    public PullToRefreshRecyclerView(Context context) {
        super(context);
    }

    public PullToRefreshRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PullToRefreshRecyclerView(Context context, Mode mode) {
        super(context, mode);
    }

    public PullToRefreshRecyclerView(Context context, Mode mode, AnimationStyle style) {
        super(context, mode, style);
    }

    @Override
    public final Orientation getPullToRefreshScrollDirection() {
        return Orientation.VERTICAL;
    }

    @Override
    protected RecyclerView createRefreshableView(Context context, AttributeSet attrs) {
        RecyclerView recyclerView;
        recyclerView = new RecyclerView(context, attrs);
        recyclerView.setId(R.id.recyclerview);
        return recyclerView;
    }

    @Override
    protected boolean isReadyForPullStart() {
        if (mRefreshableView.getChildCount() <= 0) {
            return true;
        }
        int firstVisiblePosition = mRefreshableView.getChildPosition(mRefreshableView.getChildAt(0));
        View view = getRefreshableView().getChildAt(0);
        if (firstVisiblePosition == 0) {
            if (view != null) {
                return view.getTop() >= getRefreshableView().getTop();
            }
            return mRefreshableView.getChildAt(0).getTop() >= mRefreshableView.getPaddingTop();
        }
        return false;
    }

    @Override
    protected boolean isReadyForPullEnd() {
        int lastVisiblePosition = mRefreshableView.getChildPosition(mRefreshableView.getChildAt(mRefreshableView.getChildCount() -1));
        if (lastVisiblePosition >= mRefreshableView.getAdapter().getItemCount()-1) {
            if(mRefreshableView.getChildCount() == 0){
                return false;
            }else {
                return mRefreshableView.getChildAt(mRefreshableView.getChildCount() - 1).getBottom() <= mRefreshableView.getBottom();
            }
        }
        return false;
    }
    public int getScollYDistance(RecyclerView recyclerView) {
        GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
        int position = layoutManager.findFirstVisibleItemPosition();
        View firstVisiableChildView = layoutManager.findViewByPosition(position);
        int itemHeight = firstVisiableChildView.getHeight();
        return (position / 2) * itemHeight - firstVisiableChildView.getTop();
    }
}
