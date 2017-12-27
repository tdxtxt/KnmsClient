package com.handmark.pulltorefresh.library;

import android.content.Context;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.knms.android.R;

public class PullToRefreshNestedScrollView extends PullToRefreshBase<NestedScrollView> {

    public PullToRefreshNestedScrollView(Context context) {
        super(context);
    }

    public PullToRefreshNestedScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PullToRefreshNestedScrollView(Context context, Mode mode) {
        super(context, mode);
    }

    public PullToRefreshNestedScrollView(Context context, Mode mode, AnimationStyle style) {
        super(context, mode, style);
    }

    @Override
    public final Orientation getPullToRefreshScrollDirection() {
        return Orientation.VERTICAL;
    }

    @Override
    protected NestedScrollView createRefreshableView(Context context, AttributeSet attrs) {
        NestedScrollView scrollView;
        scrollView = new NestedScrollView(context, attrs);
        scrollView.setId(R.id.scrollview);
        return scrollView;
    }

    @Override
    protected boolean isReadyForPullStart() {
        return mRefreshableView.getScrollY() == 0;
    }

    @Override
    protected boolean isReadyForPullEnd() {
        View scrollViewChild = mRefreshableView.getChildAt(0);
        return null != scrollViewChild && mRefreshableView.getScrollY() >= (scrollViewChild.getHeight() - getHeight());
    }

}
