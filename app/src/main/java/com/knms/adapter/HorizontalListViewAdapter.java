package com.knms.adapter;

import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.knms.android.R;

public class HorizontalListViewAdapter extends BaseAdapter {

	private Context context;
	private ArrayList<String> listItem;
	private LayoutInflater mInflater;
	
	public HorizontalListViewAdapter(Context context,ArrayList<String> listItem) {
		this.context = context;
		this.listItem = listItem;
		mInflater = LayoutInflater.from(context);
	}
	
	@Override
	public int getCount() {
//		return listItem!=null?listItem.size():0;
		return 6;
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
			convertView = mInflater.inflate(R.layout.horizontallistview_item, null);
		}
		viewHolder = (ViewHolder) convertView.getTag();
		
		if (viewHolder==null) {
			viewHolder = new ViewHolder();
			
			
			convertView.setTag(viewHolder);
		}
		
		
		
		return convertView;
	}
	
	class ViewHolder {
		
	}

}
