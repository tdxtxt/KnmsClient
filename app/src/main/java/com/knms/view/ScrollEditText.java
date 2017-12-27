package com.knms.view;

import android.content.Context;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;

/**
 * Created by Administrator on 2017/5/8.
 */

public class ScrollEditText extends EditText {

    public Layout mLayout;
    public int paddingTop;
    public int paddingBottom;
    public int mHeight;
    public int mLayoutHeight;

    public ScrollEditText(Context context) {
        super(context);
        init();
    }

    public ScrollEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScrollEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mLayout = getLayout();
        mLayoutHeight = mLayout.getHeight();
        paddingTop = getTotalPaddingTop();
        paddingBottom = getTotalPaddingBottom();
        mHeight = getHeight();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = super.onTouchEvent(event);
        getParent().requestDisallowInterceptTouchEvent(true);
        return result;
    }

    @Override
    protected void onScrollChanged(int horiz, int vert, int oldHoriz, int oldVert) {
        super.onScrollChanged(horiz, vert, oldHoriz, oldVert);
        //这里是滑动到底部的示例，滑动到顶部只用计算vert的值是否为0就可以
        //这里可以提前计算好一个值，不用每次进行计算，这里只是做示例
        if (vert == mLayoutHeight + paddingTop + paddingBottom - mHeight) {
            //这里触发父布局或祖父布局的滑动事件，下面这行代码只是示例作用，并没有实现真正的效果
            getParent().requestDisallowInterceptTouchEvent(false);
        }
    }


}
