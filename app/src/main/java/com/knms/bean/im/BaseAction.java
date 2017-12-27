package com.knms.bean.im;

import android.app.Activity;
import android.content.Intent;

import com.knms.activity.base.BaseActivity;

import java.io.Serializable;

/**
 * Action基类。<br>
 * 注意：在子类中调用startActivityForResult时，requestCode必须用makeRequestCode封装一遍，否则不能再onActivityResult中收到结果。
 * requestCode仅能使用最低8位。
 */
public abstract class BaseAction implements Serializable {
    private BaseActivity activity;
    private int iconResId;

    private int titleId;
    private transient int index;
    public void setIndex(int index) {
        this.index = index;
    }
    /**
     * 构造函数
     *
     * @param iconResId 图标 res id
     */
    protected BaseAction(int iconResId, int titleId) {
        this.iconResId = iconResId;
        this.titleId = titleId;
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // default: empty
    }
    public abstract void onClick();

    protected int makeRequestCode(int requestCode) {
        if ((requestCode & 0xffffff00) != 0) {
            throw new IllegalArgumentException("Can only use lower 8 bits for requestCode");
        }
        return ((index + 1) << 8) + (requestCode & 0xff);
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public int getTitleId() {
        return titleId;
    }

    public void setTitleId(int titleId) {
        this.titleId = titleId;
    }

    public BaseActivity getActivity() {
        return activity;
    }

    public void setActivity(BaseActivity activity) {
        this.activity = activity;
    }
}
