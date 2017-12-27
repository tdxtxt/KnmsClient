package com.knms.core.toast;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 类描述：
 * 创建人：Administrator
 * 创建时间：2017/12/13 10:26
 * 传参：
 * 返回:
 */
public abstract class ProcessViewCallback {
    public void processCustomView(View view) {
    }

    public void processPlainView(LinearLayout outParent, TextView msgView) {
    }
}
