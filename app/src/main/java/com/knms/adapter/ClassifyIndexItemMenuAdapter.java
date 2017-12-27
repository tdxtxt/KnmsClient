package com.knms.adapter;

import android.content.Context;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.knms.android.R;
import com.knms.bean.other.Classify;
import com.knms.util.ImageLoadHelper;
import com.knms.util.LocalDisplay;

import java.util.List;

/**
 * Created by tdx on 2016/8/25.
 */
public class ClassifyIndexItemMenuAdapter extends BaseQuickAdapter<Classify,BaseViewHolder> {
    private Context mContext;
    public ClassifyIndexItemMenuAdapter(Context context,List<Classify> data) {
        super(R.layout.item_index_menu,data);
        mContext = context;
    }
    @Override
    protected void convert(BaseViewHolder helper, Classify item) {

//        RelativeLayout.LayoutParams LParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        helper.convertView.setLayoutParams(LParams);

        ImageView iv_icon = helper.getView(R.id.iv_icon);

//        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LocalDisplay.dp2px(80), LocalDisplay.dp2px(70));
//        iv_icon.setLayoutParams(lp);

        helper.setText(R.id.tv_name,item.name);
        ImageLoadHelper.getInstance().displayImage(mContext,item.photo,iv_icon,LocalDisplay.dp2px(80), LocalDisplay.dp2px(80));
    }
}
