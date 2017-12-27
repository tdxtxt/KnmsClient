package com.knms.core.toast;

import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;

/**
 * 类描述：
 * 创建人：Administrator
 * 创建时间：2017/12/13 10:26
 * 传参：
 * 返回:
 */
public interface PlainToastSetting {
    PlainToastSetting backgroundColor(@ColorInt int color);
    PlainToastSetting backgroundColorRes(@ColorRes int colorRes);
    PlainToastSetting textColor(@ColorInt int color);
    PlainToastSetting textColorRes(@ColorRes int color);
    PlainToastSetting textSizeSp(int sp);
    PlainToastSetting textBold(boolean bold);
    PlainToastSetting processPlainView(ProcessViewCallback callback);
}
