package com.knms.activity.mine;

import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.knms.activity.base.HeadBaseActivity;
import com.knms.android.R;
import com.knms.bean.user.User;
import com.knms.util.CommonUtils;
import com.knms.util.QRCodeUtil;
import com.knms.util.SPUtils;

/**
 * Created by Administrator on 2017/2/6.
 */

public class MineQRcodeActivity extends HeadBaseActivity {
    private ImageView mImageView;
    private TextView mNickname;
    private User user;
    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("我的二维码");
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_mine_qrcode_layout;
    }

    @Override
    protected void initView() {
        user=SPUtils.getUser();
        mImageView=findView(R.id.iv_mine_qr_code);
        mNickname=findView(R.id.tv_nickname);
        ((PullToRefreshScrollView)findView(R.id.ptr_mine_code)).setMode(PullToRefreshBase.Mode.BOTH);
       QRCodeUtil.showThreadImage(this,user.usercode,mImageView,0);
    }

    @Override
    protected void initData() {
        mNickname.setText(CommonUtils.phoneNumberFormat(user.nickname));
    }

    @Override
    public String setStatisticsTitle() {
        return "我的二维码";
    }
}
