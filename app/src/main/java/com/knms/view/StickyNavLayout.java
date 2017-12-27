package com.knms.view;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.OverScroller;

import com.knms.android.R;

public class StickyNavLayout extends LinearLayout implements NestedScrollingParent {
    private static final String TAG = "StickyNavLayout";
    private boolean isTopHidden = false;
    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        Log.e(TAG, "onStartNestedScroll");
        return true;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
        Log.e(TAG, "onNestedScrollAccepted");
    }

    @Override
    public void onStopNestedScroll(View target) {
        Log.e(TAG, "onStopNestedScroll");
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        Log.e(TAG, "onNestedScroll");
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        Log.e(TAG, "onNestedPreScroll");
        if(scrollLinsenter != null) scrollLinsenter.onScroll(dx,dy);
        boolean hiddenTop = dy > 0 && getScrollY() < mTopViewHeight;
        boolean showTop = dy < 0 && getScrollY() >= 0 && !ViewCompat.canScrollVertically(target, -1);

        if (hiddenTop || showTop) {
            scrollBy(0, dy);
            consumed[1] = dy;
        }
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        Log.e(TAG, "onNestedFling");
        return false;
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        Log.e(TAG, "onNestedPreFling");
        //down - //up+
        if (getScrollY() >= mTopViewHeight) return false;
        fling((int) velocityY);
        return true;
    }

    @Override
    public int getNestedScrollAxes() {
        Log.e(TAG, "getNestedScrollAxes");
        return 0;
    }

    private View mTop;
    private View mNav;
    private View mViewPager;

    private int mTopViewHeight;

    private OverScroller mScroller;
    private VelocityTracker mVelocityTracker;
    private int mTouchSlop;
    private int mMaximumVelocity, mMinimumVelocity;

    private float mLastY;
    private boolean mDragging;

    public StickyNavLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(LinearLayout.VERTICAL);

        mScroller = new OverScroller(context);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mMaximumVelocity = ViewConfiguration.get(context)
                .getScaledMaximumFlingVelocity();
        mMinimumVelocity = ViewConfiguration.get(context)
                .getScaledMinimumFlingVelocity();

    }

    private void initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTop = findViewById(R.id.id_stickynavlayout_topview);
        mNav = findViewById(R.id.id_stickynavlayout_indicator);
        mViewPager = findViewById(R.id.id_stickynavlayout_viewpager);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(mTop == null)mTop = findViewById(R.id.id_stickynavlayout_topview);
        if(mNav == null)mNav = findViewById(R.id.id_stickynavlayout_indicator);
        if(mViewPager == null)mViewPager = findViewById(R.id.id_stickynavlayout_viewpager);
        //不限制顶部的高度
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        getChildAt(0).measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        ViewGroup.LayoutParams params = mViewPager.getLayoutParams();

        params.height = getMeasuredHeight() - (mNav == null ? 0 : mNav.getMeasuredHeight());
        setMeasuredDimension(getMeasuredWidth(), mTop.getMeasuredHeight() + (mNav == null ? 0 : mNav.getMeasuredHeight()) + mViewPager.getMeasuredHeight());

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mTopViewHeight = mTop.getMeasuredHeight();
    }


    public void fling(int velocityY) {
        mScroller.fling(0, getScrollY(), 0, velocityY, 0, 0, 0, mTopViewHeight);
        invalidate();
    }

    @Override
    public void scrollTo(int x, int y) {
        if (y < 0) {
            y = 0;
        }
        if (y > mTopViewHeight) {
            y = mTopViewHeight;
        }
        if (y != getScrollY()) {
            super.scrollTo(x, y);
        }
        isTopHidden = getScrollY() == mTopViewHeight;
        //set  listener 设置悬浮监听回调
        if (listener != null) {
            listener.isStick(isTopHidden);
            listener.scrollPercent((float) getScrollY() / (float) mTopViewHeight);
        }
    }
    public boolean getScrollPosition(){
        if(null != mViewPager){
            View sv = mViewPager.findViewById(R.id.id_stickynavlayout_innerscrollview);
            if (null == sv) return true;
            View parent =null;
            if(mViewPager instanceof ViewPager) parent=mViewPager;
            else parent = mViewPager.findViewById(R.id.vp_content);
            if (null!= parent && parent instanceof ViewPager){
                ViewPager vp = (ViewPager) parent;
                if (vp.getAdapter() instanceof FragmentPagerAdapter||vp.getAdapter() instanceof FragmentStatePagerAdapter) {
                    Fragment fragment = (Fragment) vp.getAdapter().instantiateItem(vp,vp.getCurrentItem());
                    if (null != fragment) {
                        View v = fragment.getView();
                        if (null != v)
                            v = v.findViewById(R.id.id_stickynavlayout_innerscrollview);
                        if (null != v) sv = v;
                    }
                }
            }
            if( sv instanceof RecyclerView){
                RecyclerView recyclerView = (RecyclerView) sv;
                int firstVisiblePosition = recyclerView.getChildPosition(recyclerView.getChildAt(0));
                View view = recyclerView.getChildAt(0);
                if (firstVisiblePosition == 0) {
                    if (view != null) {
                        return view.getTop() >= recyclerView.getTop();
                    }
                    return recyclerView.getChildAt(0).getTop() >= recyclerView.getPaddingTop();
                }
                return false;
            }
        }
        return true;
    }
    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(0, mScroller.getCurrY());
            invalidate();
        }
    }
    private onStickStateChangeListener listener;

    /**
     * 悬浮状态回调
     */
    public interface onStickStateChangeListener {
        /**
         * 是否悬浮的回调
         *
         * @param isStick true 悬浮 ,false 没有悬浮
         */
        void isStick(boolean isStick);

        /**
         * 距离悬浮的距离的百分比
         *
         * @param percent 0~1(向上) or 1~0(向下) 的浮点数
         */
        void scrollPercent(float percent);
    }

    public void setOnStickStateChangeListener(onStickStateChangeListener listener) {
        this.listener = listener;
    }
    ScrollLinsenter scrollLinsenter;
    public void setScrollLinsenter(ScrollLinsenter linsenter){
        this.scrollLinsenter = linsenter;
    }
    public interface ScrollLinsenter {
        public void onScroll(int x, int y);
    }
}
