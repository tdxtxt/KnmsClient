package com.knms.adapter;

import android.content.Context;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.knms.android.R;
import com.knms.bean.product.Goods;
import com.knms.util.CommonUtils;
import com.knms.util.ImageLoadHelper;
import com.knms.util.LocalDisplay;

import java.util.List;

/**
 * Created by Administrator on 2017/3/22.
 */

public class FindGoodsResultAdapter extends BaseMultiItemQuickAdapter<Goods, BaseViewHolder> {

    private Context mContext;

    public FindGoodsResultAdapter(Context context, List data) {
        super(data);
        addItemType(1, R.layout.item_mall_commodity);//特价商品，需要显示价格
        addItemType(0, R.layout.item_commodity);//普通分类商品
        mContext = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, Goods item) {
        if (item.getItemType() == 1) {
            helper.setText(R.id.commodity_name, item.cotitle)
                    .setText(R.id.idle_browse_amount, item.browseNumber + "")
                    .setText(R.id.current_price, "￥" + CommonUtils.keepTwoDecimal(item.price));
            ImageLoadHelper.getInstance().displayImage(mContext, item.coInspirationPic, (ImageView) helper.getView(R.id.img_commodity), LocalDisplay.dp2px(160), LocalDisplay.dp2px(160));

        } else {
            helper.setText(R.id.commodity_name, item.cotitle);
            helper.setText(R.id.browse_amount, item.browseNumber + "");
            ImageLoadHelper.getInstance().displayImage(mContext, item.coInspirationPic, (ImageView) helper.getView(R.id.img_commodity), LocalDisplay.dp2px(160), LocalDisplay.dp2px(160));

        }
    }
}
