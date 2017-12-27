package com.knms.adapter;

import android.content.Context;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.knms.android.R;
import com.knms.bean.product.BosCommodityBody;
import com.knms.util.CommonUtils;
import com.knms.util.ImageLoadHelper;
import com.knms.util.LocalDisplay;

import java.util.List;

/**
 * Created by Administrator on 2016/8/31.
 */
public class BokCommodityAdapter extends BaseQuickAdapter<BosCommodityBody.SimpCommodity,BaseViewHolder> {
    public BokCommodityAdapter(List<BosCommodityBody.SimpCommodity> data) {
        super(R.layout.item_mall_commodity,data);
    }
    @Override
    protected void convert(BaseViewHolder helper, BosCommodityBody.SimpCommodity item) {
        ImageView img = helper.getView(R.id.img_commodity);
        ImageLoadHelper.getInstance().displayImage(mContext,item.pic, img,LocalDisplay.dp2px(160),LocalDisplay.dp2px(160));
        helper.setText(R.id.commodity_name, item.name)
              .setText(R.id.idle_browse_amount, CommonUtils.numberConvert(item.browseNumber))
              .setText(R.id.current_price, item.showPrice);
    }
}
