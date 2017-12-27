package com.knms.adapter;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.knms.adapter.baserecycler.BaseQuickAdapter;
import com.knms.adapter.baserecycler.BaseViewHolder;
import com.knms.android.R;
import com.knms.bean.product.Menu;
import com.knms.util.ImageLoadHelper;
import com.knms.util.LocalDisplay;
import com.knms.util.ScreenUtil;
import java.util.List;

/**
 * Created by Administrator on 2016/8/25.
 */
public class IndexMenuAdapter extends BaseQuickAdapter<Menu> {
    int w;
    private Context mContext;
    public IndexMenuAdapter(Context context,List<Menu> data) {
        super(R.layout.item_index_menu,data);
        mContext = context;
            w = LocalDisplay.dp2px(80);
        if(getItemCount() > 0)
            w = ScreenUtil.getScreenWidth()/getItemCount();
    }
    @Override
    protected void convert(BaseViewHolder helper, Menu item) {
        RelativeLayout.LayoutParams LParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        helper.convertView.setLayoutParams(LParams);

        ImageView iv_icon = helper.getView(R.id.iv_icon);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(w, w);
        iv_icon.setLayoutParams(lp);

        helper.setText(R.id.tv_name,item.mename);
        ImageLoadHelper.getInstance().displayImage(mContext,item.mephoto,iv_icon);
    }
}
