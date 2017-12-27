package com.knms.activity;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.knms.activity.base.HeadBaseActivity;
import com.knms.activity.mine.AboutUsActivity;
import com.knms.android.BuildConfig;
import com.knms.android.R;
import com.knms.bean.ResponseBody;
import com.knms.core.im.IMHelper;
import com.knms.core.upgrade.UpdateHelper;
import com.knms.core.upgrade.pojo.UpdateInfo;
import com.knms.net.RxUpdateApi;
import com.knms.util.SDCardUtils;
import com.knms.util.SPUtils;
import com.knms.util.SystemInfo;

import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.shareuzi.bean.ShareEntity;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * 个人中心  设置界面
 */
public class SettingActivity extends HeadBaseActivity implements OnClickListener,
        OnCheckedChangeListener {

    private ToggleButton mPushNotification;
    private RelativeLayout mClearCache, mShare, mAboutUs;
    private TextView mCacheData, mVersion;

    @Override
    protected int layoutResID() {
        return R.layout.activity_setting;
    }

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("设置");
    }

    @Override
    protected void initView() {
        mPushNotification = (ToggleButton) findViewById(R.id.push_notification);
        mClearCache = (RelativeLayout) findViewById(R.id.clear_cache);
        mShare = (RelativeLayout) findViewById(R.id.share);
        mAboutUs = (RelativeLayout) findViewById(R.id.about_us);
        mCacheData = (TextView) findViewById(R.id.cache_data);
        mVersion = (TextView) findViewById(R.id.version_information);
        reqApi();
    }

    @Override
    protected void initData() {
        mPushNotification.setOnCheckedChangeListener(this);
        mClearCache.setOnClickListener(this);
        mShare.setOnClickListener(this);
        mAboutUs.setOnClickListener(this);
        findView(R.id.check_update).setOnClickListener(this);
        try {
            mCacheData.setText(SDCardUtils.setFileSize(SDCardUtils.getFolderSize(SDCardUtils.getCacheDirFile(this))));
        } catch (Exception e) {
            e.printStackTrace();
        }
        mPushNotification.setChecked(SPUtils.getNotificationStatus());
    }

    @Override
    public String setStatisticsTitle() {
        return "设置";
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clear_cache:
                // 清除机制待完善
                SDCardUtils.RecursionDeleteFile(SDCardUtils.getCacheDirFile(this));
                mCacheData.setText("0kb");
                Toast.makeText(this, "清除缓存成功", Toast.LENGTH_SHORT).show();
                break;
            case R.id.share:
                //TODO 分享
                ShareEntity testBean = new ShareEntity("铠恩买手", "铠恩买手");
                testBean.setUrl("http://down.kebuyer.com/"); //分享链接
                testBean.setImg("https://img.kebuyer.com/img/keicon.png");
//                ShareUtil.showShareDialog(this, testBean, ShareConstant.REQUEST_CODE);
                OnekeyShare oks = new OnekeyShare();
                oks.show(this, testBean);
                break;
            case R.id.about_us:
                startActivityAnimGeneral(AboutUsActivity.class, null);
                break;
            case R.id.check_update:
                UpdateHelper updateHelper = new UpdateHelper.Builder(this)
                        .isAutoInstall(false)
                        .isThinkTime(false)
                        .build();
                updateHelper.check();
                break;
        }

    }

    @Override
    public void onCheckedChanged(CompoundButton button, boolean isChecked) {
        switch (button.getId()) {
            case R.id.push_notification:
                SPUtils.saveNotificationStatus(isChecked);
                IMHelper.getInstance().enableNotification(isChecked);
                break;
        }
    }

    @Override
    protected void reqApi() {
        RxUpdateApi.getInstance().getApiService().clientupdate("userand", BuildConfig.SER_VERSION_CODE).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<UpdateInfo>>() {
                    @Override
                    public void call(ResponseBody<UpdateInfo> updateInfoResponseBody) {
                        if (updateInfoResponseBody.isSuccess()) {
                            UpdateInfo info = updateInfoResponseBody.data;
                            if (info.getUpdatecliverid().equals(BuildConfig.SER_VERSION_CODE)) {
                                mVersion.setTextColor(getResources().getColor(R.color.color_gray_999999));
                                mVersion.setText(SystemInfo.getVerSerName() + "已是最新版本");
                            } else {
                                mVersion.setTextColor(getResources().getColor(R.color.common_red));
                                mVersion.setText("有新版本，点击更新");
                            }

                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
    }
}
