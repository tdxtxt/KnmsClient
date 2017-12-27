package com.knms.activity;

import com.knms.activity.base.HeadBaseActivity;
import com.knms.android.R;
import android.widget.TextView;

public class HelpActivity extends HeadBaseActivity {

	@Override
	protected int layoutResID() {
		return R.layout.activity_help;
	}

	@Override
	public void setCenterTitleView(TextView tv_center) {
		tv_center.setText("帮助");
	}

	@Override
	protected void initView() {
	}

	@Override
	protected void initData() {
	}

	@Override
	public String setStatisticsTitle() {
		return "帮助";
	}

}
