package com.knms.activity.base;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.gyf.barlibrary.ImmersionBar;
import com.knms.android.R;
import com.knms.core.upgrade.UpdateHelper;

/**
 * Created by tdx on 2016/9/1.
 * 特此警告:继承该类请务必在布局文件中include【top_title_layout】布局
 */
public abstract class HeadBaseActivity extends BaseActivity{
    private RightCallBack callBack;
    protected TextView tv_title_center;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            initHeadView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initHeadView() throws Exception{
        findView(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finshActivity();
            }
        });
        tv_title_center = findView(R.id.tv_title_center);
        TextView tv_title_right = findView(R.id.tv_title_right);
        ImageView iv_icon_right = findView(R.id.iv_icon_right);
        FrameLayout fl_right = findView(R.id.fl_right);
        setCenterTitleView(tv_title_center);
        if(callBack != null){
            fl_right.setVisibility(View.VISIBLE);
            callBack.setRightContent(tv_title_right,iv_icon_right);
            fl_right.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callBack.onclick();
                }
            });
        }else{
            fl_right.setVisibility(View.INVISIBLE);
        }
    }
    public abstract void setCenterTitleView(TextView tv_center);

    public void setRightMenuCallBack(RightCallBack callBack) {
        this.callBack = callBack;
        try {
            initHeadView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface RightCallBack{
            public void setRightContent(TextView tv, ImageView icon);
            public void onclick();
    }
}
