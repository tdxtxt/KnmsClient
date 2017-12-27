package com.knms.view.banner;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;


public class AutoViewPager extends LoopViewPager {
    private int mState;
    private int mTimer = 5000;//轮播时长
    private boolean isAuto = true;//是否自动轮播true自动轮播,false不自动轮播

    public void setmTimer(int mTimer) {
        this.mTimer = mTimer;
    }
    public void setAuto(boolean auto) {
        isAuto = auto;
    }
    private final Runnable mSettler = new Runnable() {
        @Override
        public void run() {
            if (mState == SCROLL_STATE_IDLE) {
                if (getAdapter() != null && getAdapter().getCount() != 0) {
                    setCurrentItem((getCurrentItem() + 1) % getAdapter().getCount(), true);
                }
            }
        }
    };

    private final ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            if(isAuto){
                removeCallbacks(mSettler);
                postDelayed(mSettler, mTimer);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            mState = state;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }
    };

    public AutoViewPager(Context context) {
        super(context);
        init();
    }

    public AutoViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        addOnPageChangeListener(mOnPageChangeListener);
        setOffscreenPageLimit(3);
    }

    @Override
    protected void onDetachedFromWindow() {//销毁View的时候调用
        super.onDetachedFromWindow();
        if(isAuto)removeCallbacks(mSettler);
    }
}
