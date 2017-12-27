package com.knms.core.toast;

import android.support.annotation.LayoutRes;
import android.view.View;

/**
 * 类描述：
 * 创建人：Administrator
 * 创建时间：2017/12/13 10:27
 * 传参：
 * 返回:
 */
public interface CustomToastSetting {
    CustomToastSetting view(View view);
    CustomToastSetting view(@LayoutRes int layout);
    CustomToastSetting processCustomView(ProcessViewCallback callback);
}
