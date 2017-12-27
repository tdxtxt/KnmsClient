package com.knms.core.upgrade.listener;

import com.knms.core.upgrade.pojo.UpdateInfo;
/**
 * Created by ShelWee on 14-5-16.
 */
public interface OnUpdateListener {
    public void noUpdata();//不更新
    public void nextUpdata();//下次更新
    public void onfail();//检查失败
}
