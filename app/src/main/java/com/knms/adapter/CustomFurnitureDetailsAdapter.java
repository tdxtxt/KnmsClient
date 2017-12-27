package com.knms.adapter;

import android.content.Context;
import android.widget.ImageView;

import com.knms.adapter.base.CommonAdapter;
import com.knms.adapter.base.ViewHolder;
import com.knms.android.R;
import com.knms.bean.customfurniture.CustomDetail;
import com.knms.util.ImageLoadHelper;

import java.util.List;


public class CustomFurnitureDetailsAdapter extends CommonAdapter<CustomDetail.Service> {

    public CustomFurnitureDetailsAdapter(Context context, List<CustomDetail.Service> mDatas) {
        super(context, R.layout.listview_item_custom_furniture_details,mDatas);
    }

    @Override
    public void convert(ViewHolder helper, CustomDetail.Service data) {
        helper.setText(R.id.custom_service_name,data.sename);
        helper.setText(R.id.custom_service_content,data.seremark);
        ImageLoadHelper.getInstance().displayImage(mContext,data.sephoto,(ImageView)helper.getView(R.id.custom_service_img));
    }

   /* private Context context;
    private ArrayList<CustomDetail.Service> listItem;
    private LayoutInflater inflater;
    private TextView get;

    public CustomFurnitureDetailsAdapter(Context context, ArrayList<CustomDetail.Service> listItem) {
        this.context = context;
        this.listItem = listItem;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return 4;
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
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listview_item_custom_furniture_details, null);
        }
        viewHolder = (ViewHolder) convertView.getTag();
        if (viewHolder == null) {
            viewHolder = new ViewHolder();

            convertView.setTag(viewHolder);
        }
        return convertView;
    }

    class ViewHolder {

    }
*/
}
