package com.knms.adapter;

import android.support.v4.widget.NestedScrollView;
import android.view.View;

import me.everything.android.ui.overscroll.adapters.IOverScrollDecoratorAdapter;

/**
 * Created by Administrator on 2017/3/30.
 */

public class NestedScrollViewOverScrollDecorAdapter implements IOverScrollDecoratorAdapter {
    protected final NestedScrollView mView;
    public NestedScrollViewOverScrollDecorAdapter(NestedScrollView view) {
        this.mView = view;
    }

    public View getView() {
        return this.mView;
    }

    public boolean isInAbsoluteStart() {
        return !this.mView.canScrollVertically(-1);
    }

    public boolean isInAbsoluteEnd() {
        return !this.mView.canScrollVertically(1);
    }
}
