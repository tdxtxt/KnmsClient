package com.knms.view.statuslayout;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.View;
import android.view.ViewStub;

/**
 * 类描述：
 * 创建人：Administrator
 * 创建时间：2017/11/22 16:16
 * 传参：
 * 返回:
 */
public abstract class AbsLayout {
    protected ViewStub mLayoutVs;

    protected View mContentView;

    protected void initLayout(@LayoutRes int layoutResId, Context context) {
        mLayoutVs = new ViewStub(context);
        mLayoutVs.setLayoutResource(layoutResId);
    }

    protected ViewStub getLayoutVs() {
        return mLayoutVs;
    }

    protected void setView(View contentView) {
        mContentView = contentView;
    }

    protected abstract void setData(Object... objects);
}
