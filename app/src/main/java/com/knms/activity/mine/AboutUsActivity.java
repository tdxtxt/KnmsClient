package com.knms.activity.mine;

import android.view.View;
import android.widget.TextView;

import com.knms.activity.CommWebViewActivity;
import com.knms.activity.base.HeadBaseActivity;
import com.knms.android.R;
import com.knms.util.SystemInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/12/5.
 */

public class AboutUsActivity extends HeadBaseActivity {

    private TextView tvAgreement;

    @Override
    protected int layoutResID() {
        return R.layout.activity_about_us;
    }

    @Override
    protected void initView() {
        ((TextView)findViewById(R.id.tv_ver_name)).setText(SystemInfo.getVerSerName());
        tvAgreement= (TextView) findViewById(R.id.tv_user_agreement);
        tvAgreement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String,Object> param=new HashMap<>();
                param.put("url","https://h5.kebuyer.com/document/buy_protocol.html");
                startActivityAnimGeneral(CommWebViewActivity.class,param);
            }
        });
    }


    @Override
    protected void initData() {

    }

    @Override
    public String setStatisticsTitle() {
        return "关于我们";
    }

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("关于我们");
    }

}
