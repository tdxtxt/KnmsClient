package com.handmark.pulltorefresh.library;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;

import com.knms.android.R;
import com.knms.view.sticky.HeaderViewPager;

/**
 * Created by Administrator on 2016/11/8.
 */

public class PullToRefreshHeaderViewPager extends PullToRefreshBase<HeaderViewPager> {
    public PullToRefreshHeaderViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public PullToRefreshHeaderViewPager(Context context, Mode mode, AnimationStyle animStyle) {
        super(context, mode, animStyle);
        // TODO Auto-generated constructor stub
    }

    public PullToRefreshHeaderViewPager(Context context, Mode mode) {
        super(context, mode);
        // TODO Auto-generated constructor stub
    }

    public PullToRefreshHeaderViewPager(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    @Override
    public Orientation getPullToRefreshScrollDirection() {
        // TODO Auto-generated method stub
        return Orientation.VERTICAL;
    }

    @Override
    protected HeaderViewPager createRefreshableView(Context context, AttributeSet attrs) {
        // TODO Auto-generated method stub
        HeaderViewPager linearLayout;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            linearLayout = new HeaderViewPager(context, attrs);
        } else {
            linearLayout = new HeaderViewPager(context, attrs);
        }

        linearLayout.setId(R.id.headviewpager);
        linearLayout.setOrientation(VERTICAL);
        return linearLayout;
    }

    @Override
    protected boolean isReadyForPullEnd() {
        // TODO Auto-generated method stub
        HeaderViewPager refreshableView = getRefreshableView();
        if (null != refreshableView) {
            return refreshableView.getScrollY() == 0;
        }

        return false;
    }

    @Override
    protected boolean isReadyForPullStart() {
        return mRefreshableView.canPtr();
    }
}
