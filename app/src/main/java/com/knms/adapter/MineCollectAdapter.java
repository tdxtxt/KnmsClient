package com.knms.adapter;

import android.content.Context;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.knms.android.R;
import com.knms.bean.product.ClassifyGood;
import com.knms.bean.product.CollectionProduct;
import com.knms.util.CommonUtils;
import com.knms.util.ImageLoadHelper;
import com.knms.util.LocalDisplay;

import java.util.List;

/**
 * Created by Administrator on 2016/8/31.
 */
public class MineCollectAdapter extends BaseQuickAdapter<CollectionProduct,BaseViewHolder> {

    public MineCollectAdapter(Context context, List<CollectionProduct> data) {
        super(R.layout.item_mall_commodity, data);
    }


    @Override
    protected void convert(BaseViewHolder helper, CollectionProduct item) {
        ImageView img = helper.getView(R.id.img_commodity);
        ImageLoadHelper.getInstance().displayImage(mContext,item.coInspirationPic, img,LocalDisplay.dp2px(160),LocalDisplay.dp2px(160));
        helper.setText(R.id.commodity_name, item.cotitle)
                .setText(R.id.idle_browse_amount, CommonUtils.numberConvert(item.browseNumber))
                .setText(R.id.current_price, CommonUtils.addMoneySymbol(item.realityPrice))
                .setVisible(R.id.current_price,item.gotype==1||item.goisrecommend==1);
        switch (item.gotype) {//0全新，1二手(闲置)，3定制
            case 0:
            case 6:
                helper.setImageResource(R.id.goods_type, 0);
                break;
            case 1:
                helper.setImageResource(R.id.goods_type, R.drawable.q_27);
                break;
            case 3:
                helper.setImageResource(R.id.goods_type, R.drawable.q_21);
                break;
        }
    }
}
