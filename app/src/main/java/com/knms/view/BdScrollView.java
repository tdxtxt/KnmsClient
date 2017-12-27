package com.knms.view;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ScrollView;

/**
 * Created by tdx on 2016/11/8.
 * 监听scrollview size的变化,可设置最大高度
 */

public class BdScrollView extends ScrollView {
    /**
     * DEBUG
     */
    private static final boolean DEBUG = true;

    /**
     * TAG
     */
    private static final String TAG = "BdScrollView";

    /**
     * 最大高度
     */
    private int mMaxHeight = -1;

    /**
     * 构造函数
     *
     * @param context context
     */
    public BdScrollView(Context context) {
        super(context);
    }

    /**
     * 构造函数
     *
     * @param context context
     * @param attrs attrs
     */
    public BdScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 构造函数
     *
     * @param context context
     * @param attrs attrs
     * @param defStyle defStyle
     */
    public BdScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (DEBUG) {
            Log.d(TAG, "onMeasure( " + widthMeasureSpec + ", " + heightMeasureSpec + ")");
        }
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        if (mMaxHeight > 0) {
            heightSize = Math.min(heightSize, mMaxHeight);
        }
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        int childHeight = getChildAt(0).getMeasuredHeight();
        int childWidth = getChildAt(0).getMeasuredWidth();
        if (childHeight > 0) {
            heightSize = Math.min(childHeight, heightSize);
        }
        if (childWidth > 0) {
            widthSize = Math.min(childWidth, widthSize);
        }
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (DEBUG) {
            Log.d(TAG, "onLayout( " + changed + ", " + l + ", " + t + ", " + r + ", " + b + ")");
        }
    }

    /**
     * 设置最大高度
     *
     * @param height height
     */
    public void setMaxHeight(int height) {
        mMaxHeight = height;
    }
}
