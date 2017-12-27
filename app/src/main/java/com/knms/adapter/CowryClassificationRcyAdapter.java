package com.knms.adapter;

import android.content.Context;
import android.widget.TextView;

import com.knms.adapter.baserecycler.BaseQuickAdapter;
import com.knms.adapter.baserecycler.BaseViewHolder;
import com.knms.android.R;
import com.knms.bean.idle.ReIdleClassify;
import com.knms.util.LocalDisplay;

import java.util.List;

public class CowryClassificationRcyAdapter extends BaseQuickAdapter<ReIdleClassify> {
	public CowryClassificationRcyAdapter(List<ReIdleClassify> mDatas) {
		super(R.layout.item_search, mDatas);
	}
	@Override
	protected void convert(BaseViewHolder helper, ReIdleClassify item) {
		TextView tv_title = helper.getView(R.id.tv_label_name);
		tv_title.setPadding(0, LocalDisplay.dp2px(8),0, LocalDisplay.dp2px(8));
		tv_title.setText(item.name);
	}
}
