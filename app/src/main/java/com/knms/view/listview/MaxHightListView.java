package com.knms.view.listview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by Administrator on 2017/3/1.
 */

public class MaxHightListView extends ListView {
    /**
     * listview高度
     */
    private int listViewHeight;
    public MaxHightListView(Context context) {
        super(context);
    }

    public MaxHightListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MaxHightListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MaxHightListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    public void setMaxHeight(int listViewHeight) {
        this.listViewHeight = listViewHeight;
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (listViewHeight > -1) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(listViewHeight,
                    MeasureSpec.AT_MOST);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
