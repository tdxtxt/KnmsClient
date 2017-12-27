package com.handmark.pulltorefresh.library.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * Created by Administrator on 2016/10/27.
 */

public class XScrollView extends ScrollView {
//    private NotifyingScrollView.OnScrollChangedListener mOnScrollChangedListener;
//
//    public void setmOnScrollChangedListener(NotifyingScrollView.OnScrollChangedListener mOnScrollChangedListener) {
//        this.mOnScrollChangedListener = mOnScrollChangedListener;
//    }

    // endregion
    // region Interfaces
    public interface OnScrollChangedListener {
        void onScrollChanged(ScrollView who, int l, int t, int oldl, int oldt);
    }
    public XScrollView(Context context) {
        super(context);
    }

    public XScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public XScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
//        if (mOnScrollChangedListener != null) {
//            mOnScrollChangedListener.onScrollChanged(this, l, t, oldl, oldt);
//        }
    }
    /**
     * 滑动事件，这是控制手指滑动的惯性速度
     */
    @Override
    public void fling(int velocityY) {
        super.fling(velocityY / 2);
    }
}
