package com.knms.adapter.index;

import android.widget.ImageView;
import android.widget.LinearLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.knms.android.R;
import com.knms.bean.product.Ad;
import com.knms.util.ImageLoadHelper;
import com.knms.util.LocalDisplay;
import com.knms.util.ScreenUtil;

import java.util.List;


/**
 * Created by Administrator on 2017/6/1.
 */

public class IndexStyleAdapter extends BaseQuickAdapter<Ad,BaseViewHolder>{
    int width;
    int height;
    public IndexStyleAdapter(List data) {
        super(R.layout.item_img_70x70, data);
        width = (ScreenUtil.getScreenWidth() - LocalDisplay.dp2px(20)) * 2 / 3;
        height = width * 5 / 8;
    }
    @Override
    protected void convert(BaseViewHolder helper, Ad item) {
        ImageView imageView = helper.getView(R.id.iv_pic);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width,height);
        lp.setMargins(0,0,0,LocalDisplay.dp2px(10));
        imageView.setLayoutParams(lp);
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        ImageLoadHelper.getInstance().displayImage(helper.getConvertView().getContext(),item.name,imageView,width,height);
    }
}
