package com.handmark.pulltorefresh.library.internal;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.knms.android.R;

/**
 * Created by tdx on 2016/10/31.
 * 参考:http://blog.csdn.net/plmmmmlq/article/details/50068717
 */

public class FrameAnimationLayout extends LoadingLayout{
    private AnimationDrawable mAnimationDrawable;
    public FrameAnimationLayout(Context context, PullToRefreshBase.Mode mode, PullToRefreshBase.Orientation scrollDirection, TypedArray attrs) {
        super(context, mode, scrollDirection, attrs);
        mHeaderImage.setImageResource(R.drawable.animation_loading);
        mAnimationDrawable = (AnimationDrawable) mHeaderImage.getDrawable();
    }
    @Override
    protected int getDefaultDrawableResId() {
        return R.drawable.loading1;
    }

    @Override
    protected void onLoadingDrawableSet(Drawable imageDrawable) {

    }

    @Override
    protected void onPullImpl(float scaleOfLayout) {

    }

    @Override
    protected void pullToRefreshImpl() {
    }

    @Override
    protected void refreshingImpl() {
        mAnimationDrawable.start();
    }

    @Override
    protected void releaseToRefreshImpl() {

    }

    @Override
    protected void resetImpl() {

    }
}
