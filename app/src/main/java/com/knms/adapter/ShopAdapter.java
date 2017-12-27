package com.knms.adapter;

import android.content.Context;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.knms.android.R;
import com.knms.bean.shop.Shop;
import com.knms.util.ImageLoadHelper;
import com.knms.view.CircleImageView;

import java.util.ArrayList;

/**
 * Created by tdx on 2016/9/7.
 */
public class ShopAdapter extends BaseQuickAdapter<Shop,BaseViewHolder> {
    boolean isShowCoupon = false;
    boolean isSearchResult=false;
    private Context mContext;
    public ShopAdapter(Context context,boolean isShowCoupon,boolean isSearchResult) {
        super(R.layout.item_shop, new ArrayList<Shop>(0));
        mContext = context;
        this.isShowCoupon = isShowCoupon;
        this.isSearchResult=isSearchResult;
    }
    @Override
    protected void convert(BaseViewHolder helper, Shop item) {
        helper.setText(R.id.tv_name,item.name)
                .setText(R.id.tv_num,isSearchResult?"收藏"+item.collectNumber:item.collectNumber+"人收藏");
        CircleImageView iv_logo = helper.getView(R.id.iv_logo);
        ImageLoadHelper.getInstance().displayImageHead(mContext,item.logo,iv_logo);
        if(!isShowCoupon){
            helper.getView(R.id.iv_coupon).setVisibility(View.GONE);
        }else{
            if(item.isprefer == 0){
                helper.getView(R.id.iv_coupon).setVisibility(View.VISIBLE);
            }else {
                helper.getView(R.id.iv_coupon).setVisibility(View.GONE);
            }
        }
    }
}
