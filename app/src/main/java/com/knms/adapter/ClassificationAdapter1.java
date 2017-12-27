package com.knms.adapter;

import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.knms.android.R;

public class ClassificationAdapter1 extends BaseAdapter {

	private Context context;
	private ArrayList<View> listItem;
	private LayoutInflater inflater;
	
	public ClassificationAdapter1(Context context,ArrayList<View> listItem) {
		this.listItem = listItem;
		this.context = context;
		inflater = LayoutInflater.from(context);
	}
	
	@Override
	public int getCount() {
//		return listItem!=null?listItem.size():0;
		return 8;
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
		if (convertView==null) {
			convertView = inflater.inflate(R.layout.gridview_item_classification, null);
		}
		
		
		return convertView;
	}

}
