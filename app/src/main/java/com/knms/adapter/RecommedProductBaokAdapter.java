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
public class RecommedProductBaokAdapter extends BaseQuickAdapter<ClassifyGood> {
    private Context mContext;

    public RecommedProductBaokAdapter(Context context, List<ClassifyGood> data) {
        super(R.layout.item_recommed_product, data);
        mContext = context;
    }
    @Override
    protected void convert(BaseViewHolder helper, ClassifyGood item) {
        ImageView img = helper.getView(R.id.img_commodity);
        if(img.getTag() != null){
            String tagUrl = img.getTag().toString();
            if(!tagUrl.equals(item.pic)){
                img.setTag(item.pic);
                ImageLoadHelper.getInstance().displayImage(helper.getConvertView().getContext(),item.pic, img,LocalDisplay.dp2px(160),LocalDisplay.dp2px(160));
            }
        }else{
            img.setTag(item.pic);
            ImageLoadHelper.getInstance().displayImage(mContext,item.pic, img,LocalDisplay.dp2px(160),LocalDisplay.dp2px(160));
        }
        helper.setText(R.id.commodity_name, item.title)
                .setText(R.id.browse_amount, CommonUtils.numberConvert(item.browseNumber));
    }
}
