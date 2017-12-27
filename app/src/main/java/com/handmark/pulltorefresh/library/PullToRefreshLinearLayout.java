package com.handmark.pulltorefresh.library;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.knms.android.R;

/**
 * Created by Administrator on 2016/11/8.
 */

public class PullToRefreshLinearLayout extends PullToRefreshBase<LinearLayout> {
    public PullToRefreshLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public PullToRefreshLinearLayout(Context context, Mode mode, AnimationStyle animStyle) {
        super(context, mode, animStyle);
        // TODO Auto-generated constructor stub
    }

    public PullToRefreshLinearLayout(Context context, Mode mode) {
        super(context, mode);
        // TODO Auto-generated constructor stub
    }

    public PullToRefreshLinearLayout(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    @Override
    public Orientation getPullToRefreshScrollDirection() {
        // TODO Auto-generated method stub
        return Orientation.VERTICAL;
    }

    @Override
    protected LinearLayout createRefreshableView(Context context, AttributeSet attrs) {
        // TODO Auto-generated method stub
        LinearLayout linearLayout;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            linearLayout = new LinearLayout(context, attrs);
        } else {
            linearLayout = new LinearLayout(context, attrs);
        }

        linearLayout.setId(R.id.linearlayout);
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
        return true;
    }
}
