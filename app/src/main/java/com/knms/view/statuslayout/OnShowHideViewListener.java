package com.knms.view.statuslayout;

import android.view.View;

/**
 * 类描述：
 * 创建人：Administrator
 * 创建时间：2017/11/22 16:17
 * 传参：
 * 返回:
 */
public interface OnShowHideViewListener {
    void onShowView(View view, int id);

    void onHideView(View view, int id);
}
