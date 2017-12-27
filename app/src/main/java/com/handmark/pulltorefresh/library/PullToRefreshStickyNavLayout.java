package com.handmark.pulltorefresh.library;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.knms.android.R;
import com.knms.view.StickyNavLayout;

/**
 * Created by Administrator on 2016/11/8.
 */

public class PullToRefreshStickyNavLayout extends PullToRefreshBase<StickyNavLayout> {
    public PullToRefreshStickyNavLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public PullToRefreshStickyNavLayout(Context context, Mode mode, AnimationStyle animStyle) {
        super(context, mode, animStyle);
        // TODO Auto-generated constructor stub
    }

    public PullToRefreshStickyNavLayout(Context context, Mode mode) {
        super(context, mode);
        // TODO Auto-generated constructor stub
    }

    public PullToRefreshStickyNavLayout(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    @Override
    public Orientation getPullToRefreshScrollDirection() {
        // TODO Auto-generated method stub
        return Orientation.VERTICAL;
    }

    @Override
    protected StickyNavLayout createRefreshableView(Context context, AttributeSet attrs) {
        // TODO Auto-generated method stub
        StickyNavLayout linearLayout;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            linearLayout = new StickyNavLayout(context, attrs);
        } else {
            linearLayout = new StickyNavLayout(context, attrs);
        }

        linearLayout.setId(R.id.stickynavlayout);
        return linearLayout;
    }

    @Override
    protected boolean isReadyForPullEnd() {
        // TODO Auto-generated method stub
        LinearLayout refreshableView = getRefreshableView();

        if (null != refreshableView) {
            return refreshableView.getScrollY() == 0;
        }

        return false;
    }

    @Override
    protected boolean isReadyForPullStart() {
        return mRefreshableView.getScrollY() == 0 && mRefreshableView.getScrollPosition();
    }
}
