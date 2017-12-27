package com.knms.adapter;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.knms.android.R;
import com.knms.bean.product.ClassifyGood;
import com.knms.util.CommonUtils;
import com.knms.util.ImageLoadHelper;
import com.knms.util.L;
import com.knms.util.LocalDisplay;

import java.util.List;

/**
 * Created by Administrator on 2016/8/31.
 */
public class ProductMainAdapter extends BaseMultiItemQuickAdapter<ClassifyGood,BaseViewHolder> {
    private boolean isMine = false;
    private Context mContext;

    public ProductMainAdapter(Context context,List<ClassifyGood> data) {
        this(context,data,false);
    }

    public ProductMainAdapter(Context context,List<ClassifyGood> data, boolean isMine) {
        this(context,data,isMine,false);
    }

    public ProductMainAdapter(Context context,List<ClassifyGood> data, boolean isMine, boolean isMallProduct) {
        super(data);
        addItemType(1, R.layout.item_mall_commodity);//特价商品，需要显示价格
        addItemType(0,R.layout.item_commodity);//普通分类商品
        this.isMine = isMine;
        mContext = context;
    }


    @Override
    protected void convert(BaseViewHolder helper, ClassifyGood item) {
        ImageView img = helper.getView(R.id.img_commodity);
       if(item.getItemType()==1){//特价商品
            ImageLoadHelper.getInstance().displayImage(mContext,item.pic, img,LocalDisplay.dp2px(160),LocalDisplay.dp2px(160));
            helper.setText(R.id.commodity_name, item.title)
                    .setText(R.id.idle_browse_amount, CommonUtils.numberConvert(item.browseNumber))
                    .setText(R.id.current_price, item.realityPrice);
        } else {
            ImageLoadHelper.getInstance().displayImage(mContext,item.pic, img,LocalDisplay.dp2px(160),LocalDisplay.dp2px(160));
            helper.setText(R.id.commodity_name, item.title)
                    .setText(R.id.browse_amount, CommonUtils.numberConvert(item.browseNumber));
        }

    }
}
