package com.knms.view.clash;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by tdx on 2016/9/5.
 */
public class SquareCenterRelativeLayout extends RelativeLayout {
    public SquareCenterRelativeLayout(Context context) {
        super(context);
    }

    public SquareCenterRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareCenterRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize(0, widthMeasureSpec), getDefaultSize(0, heightMeasureSpec));
        int childWidthSize = getMeasuredWidth();
        heightMeasureSpec = widthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidthSize, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
