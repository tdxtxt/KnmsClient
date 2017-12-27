package com.knms.adapter;

import android.content.Context;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.knms.android.R;
import com.knms.bean.SaveParity;
import com.knms.util.ImageLoadHelper;

import java.util.List;

/**
 * Created by Administrator on 2017/3/24.
 */

public class PushMerchantAdapter extends BaseQuickAdapter<SaveParity,BaseViewHolder> {

    private Context mContext;
    public PushMerchantAdapter(Context context,List<SaveParity> data) {
        super(R.layout.item_push_merchant_layout,data);
        mContext = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, SaveParity item) {
        helper.setText(R.id.tv_merchant_name,item.shopName);
        ImageLoadHelper.getInstance().displayImageHead(mContext,item.shopLogo, (ImageView) helper.getView(R.id.iv_merchant_logo));
    }
}
