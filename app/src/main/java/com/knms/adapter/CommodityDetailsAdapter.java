package com.knms.adapter;

import java.util.ArrayList;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.knms.android.R;
import com.knms.bean.other.Coupons;

public class CommodityDetailsAdapter extends BaseAdapter {

	private Context context;
	private ArrayList<Coupons> listItem;
	private LayoutInflater inflater;
	private TextView get;

	public CommodityDetailsAdapter(Context context,ArrayList<Coupons> listItem) {
		this.context=context;
		this.listItem=listItem;
		inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return listItem.size();
	}

	@Override
	public Object getItem(int position) {
		return listItem.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder viewHolder = null;
		if (convertView==null) {
			convertView=inflater.inflate(R.layout.item_coupon, null);
		}
		viewHolder = (ViewHolder) convertView.getTag();
		if (viewHolder==null) {
			viewHolder=new ViewHolder();
			viewHolder.tvMoney= (TextView) convertView.findViewById(R.id.money);
			viewHolder.mGet = (TextView) convertView.findViewById(R.id.get);
			viewHolder.tvCondition= (TextView) convertView.findViewById(R.id.condition_money);
			viewHolder.tvTime= (TextView) convertView.findViewById(R.id.valid_time);
			convertView.setTag(viewHolder);
		}
		viewHolder.tvMoney.setText(listItem.get(position).spmoney+"元");
		viewHolder.tvCondition.setText("订单满"+listItem.get(position).spconditions+"元使用");
		get = viewHolder.mGet;
		get.setOnClickListener(onClickListener);
		return convertView;
	}

	class ViewHolder{
		TextView mGet,tvMoney,tvCondition,tvTime;
	}

	OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			get.setText("已领取");
			get.setTextColor(0xff999999);
			get.setBackgroundResource(R.drawable.textview_border_off);
		}
	};

}
