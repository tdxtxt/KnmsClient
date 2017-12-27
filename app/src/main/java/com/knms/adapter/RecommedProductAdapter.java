package com.knms.adapter;

import android.content.Context;
import android.widget.ImageView;

import com.knms.adapter.baserecycler.BaseQuickAdapter;
import com.knms.adapter.baserecycler.BaseViewHolder;
import com.knms.android.R;
import com.knms.bean.Recommend;
import com.knms.bean.product.ClassifyGood;
import com.knms.util.CommonUtils;
import com.knms.util.ImageLoadHelper;
import com.knms.util.LocalDisplay;

import java.util.List;

/**
 * Created by Administrator on 2016/8/31.
 */
public class RecommedProductAdapter extends BaseQuickAdapter<Recommend.CommodityShowBosBean> {
    private Context mContext;

    public RecommedProductAdapter(Context context,List<Recommend.CommodityShowBosBean> data) {
        super(R.layout.item_mall_commodity, data);
        mContext = context;
    }
    @Override
    protected void convert(BaseViewHolder helper, Recommend.CommodityShowBosBean item) {
        ImageView img = helper.getView(R.id.img_commodity);
        if(img.getTag() != null){
            String tagUrl = img.getTag().toString();
            if(!tagUrl.equals(item.shopImage)){
                img.setTag(item.shopImage);
                ImageLoadHelper.getInstance().displayImage(helper.getConvertView().getContext(),item.shopImage, img,LocalDisplay.dp2px(160),LocalDisplay.dp2px(160));
            }
        }else{
            img.setTag(item.shopImage);
            ImageLoadHelper.getInstance().displayImage(mContext,item.shopImage, img,LocalDisplay.dp2px(160),LocalDisplay.dp2px(160));
        }
        helper.setText(R.id.commodity_name, item.showName)
                .setText(R.id.current_price,CommonUtils.addMoneySymbol(item.realityPrice))
                .setText(R.id.idle_browse_amount, CommonUtils.numberConvert(item.browseNumber));
    }
}
