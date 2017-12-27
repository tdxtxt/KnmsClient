package com.knms.activity.repair;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.knms.activity.base.HeadNotifyBaseActivity;
import com.knms.activity.im.ChatActivity;
import com.knms.activity.login.FasterLoginActivity;
import com.knms.android.R;
import com.knms.bean.repair.Repair;
import com.knms.util.DialogHelper;
import com.knms.util.ImageLoadHelper;
import com.knms.util.SPUtils;
import com.knms.view.CircleImageView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/10/17.
 */
public class RepairDetailActivity extends HeadNotifyBaseActivity implements View.OnClickListener{

    private Repair mRepairDetail;
    private CircleImageView mRepairHead;
    private TextView mRepairName, mRepairMaexperience, mRepairMoney, mRepairPhone, mRepairModle, mRepairMaremark;
    private String repairId;
    private LinearLayout repairTel,repairSendmsg;
    private PullToRefreshScrollView pullToRefreshScrollView;

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("详情");
    }

    @Override
    protected void getParmas(Intent intent) {
        mRepairDetail = (Repair) intent.getSerializableExtra("repairDetaile");
    }


    @Override
    protected int layoutResID() {
        return R.layout.activity_repair_detail_layout;
    }

    @Override
    protected void initView() {
        mRepairHead = (CircleImageView) findViewById(R.id.repair_img);
        mRepairMaexperience = (TextView) findViewById(R.id.repair_maexperience);
        mRepairName = (TextView) findViewById(R.id.repair_name);
        mRepairMaremark = (TextView) findViewById(R.id.repair_maremark);
        mRepairModle = (TextView) findViewById(R.id.repair_mode);
        mRepairMoney = (TextView) findViewById(R.id.repair_money);
        mRepairPhone = (TextView) findViewById(R.id.repair_phone);
        repairTel= (LinearLayout) findViewById(R.id.repair_tel);
        repairSendmsg= (LinearLayout) findViewById(R.id.repair_sendmsg);
        pullToRefreshScrollView= (PullToRefreshScrollView) findViewById(R.id.pull_to_refresh_scrollview_repair);
        pullToRefreshScrollView.setMode(PullToRefreshBase.Mode.BOTH);
        repairTel.setOnClickListener(this);
        repairSendmsg.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        if(mRepairDetail==null) return;
        ImageLoadHelper.getInstance().displayImageHead(RepairDetailActivity.this,mRepairDetail.usphoto, mRepairHead);
        mRepairModle.setText("服务方式：" + (mRepairDetail.maservicemode == 1 ? "上门" : "非上门"));
        mRepairPhone.setText("电话：" + mRepairDetail.usphone);
        mRepairMoney.setText(mRepairDetail.maservicemoney + "元/起步");
        mRepairMaremark.setText(mRepairDetail.maremark);
        mRepairName.setText(mRepairDetail.usnickname);
        mRepairMaexperience.setText("从业经验：" + mRepairDetail.maexperience+"年");
    }

    @Override
    public String setStatisticsTitle() {
        return "家具维修师傅详情";
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.repair_tel:
                new DialogHelper().showPromptDialog(this, "", "拨打"+mRepairDetail.usphone, "取消", "", "确定", new DialogHelper.OnMenuClick() {
                    @Override
                    public void onLeftMenuClick() {

                    }

                    @Override
                    public void onCenterMenuClick() {

                    }

                    @Override
                    public void onRightMenuClick() {
                        Intent mIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
                                + mRepairDetail.usphone));
                        startActivity(mIntent);
                    }
                });
                break;
            case R.id.repair_sendmsg:
                if(!SPUtils.isLogin()){
                    startActivityAnimGeneral(FasterLoginActivity.class,null);
                    return;
                }
                Map<String,Object> map=new HashMap<>();
                map.put("sid",mRepairDetail.maid);
                startActivityAnimGeneral(ChatActivity.class,map);
                break;
        }
    }
}
