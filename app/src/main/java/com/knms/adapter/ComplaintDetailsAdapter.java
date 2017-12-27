package com.knms.adapter;

import android.content.Context;
import android.widget.ImageView;

import com.knms.adapter.baserecycler.BaseQuickAdapter;
import com.knms.adapter.baserecycler.BaseViewHolder;
import com.knms.android.R;
import com.knms.bean.other.Pic;
import com.knms.util.ImageLoadHelper;
import java.util.List;

public class ComplaintDetailsAdapter extends BaseQuickAdapter<Pic> {

    private Context mContext;
    public ComplaintDetailsAdapter(Context context,List<Pic> data) {
        super(R.layout.item_complaint_details, data);
        mContext = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, Pic item) {
        ImageView complaint_picture = helper.getView(R.id.complaint_picture);
        ImageLoadHelper.getInstance().displayImage(mContext,item.url, complaint_picture);
    }
}
