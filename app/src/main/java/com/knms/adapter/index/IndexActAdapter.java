package com.knms.adapter.index;

import android.view.ViewGroup;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.knms.android.R;
import com.knms.bean.product.Ad;
import com.knms.util.ImageLoadHelper;
import com.knms.util.LocalDisplay;
import com.knms.util.ScreenUtil;

import java.util.List;

/**
 * Created by Administrator on 2017/6/2.
 */

public class IndexActAdapter extends BaseQuickAdapter<Ad,BaseViewHolder>{
    int width,height;
    public IndexActAdapter(List<Ad> data) {
        super(R.layout.item_index_act, data);
        width = ScreenUtil.getScreenWidth();
        height = width * 8 / 15;
    }
    @Override
    protected void convert(BaseViewHolder helper, Ad item) {
        ImageView imageView = helper.getView(R.id.iv_ad);
        ViewGroup.LayoutParams lp = imageView.getLayoutParams();
        lp.width = width;
        lp.height = height;
        imageView.setLayoutParams(lp);
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        ImageLoadHelper.getInstance().displayImage(helper.getConvertView().getContext(),item.name,imageView,width,height);
        helper.setText(R.id.tv_title,item.cotitle);
    }
}
