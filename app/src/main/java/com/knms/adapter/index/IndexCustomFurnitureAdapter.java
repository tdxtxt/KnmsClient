package com.knms.adapter.index;

import android.graphics.Color;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.knms.android.R;
import com.knms.bean.product.Ad;
import com.knms.util.ImageLoadHelper;
import com.knms.util.LocalDisplay;
import com.knms.util.ScreenUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/6/1.
 */

public class IndexCustomFurnitureAdapter extends BaseQuickAdapter<Ad,BaseViewHolder>{
    int width,height;
    public IndexCustomFurnitureAdapter(List data) {
        super(R.layout.item_img_70x70, data);
        width = (ScreenUtil.getScreenWidth() - LocalDisplay.dp2px(75)) / 2;
        height = width * 2 / 5;
    }

    @Override
    protected void convert(BaseViewHolder helper, Ad item) {
        ImageView imageView = helper.getView(R.id.iv_pic);
        imageView.setBackgroundColor(Color.parseColor("#232323"));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width,height);
        if(helper.getLayoutPosition() % 2 == 0){
            if((helper.getLayoutPosition() / 2) % 2 == 0){
                if(helper.getLayoutPosition() / 2 == 0){
                    lp.setMargins(LocalDisplay.dp2px(30),0,LocalDisplay.dp2px(7),LocalDisplay.dp2px(7));
                }else{
                    lp.setMargins(LocalDisplay.dp2px(30),LocalDisplay.dp2px(7),LocalDisplay.dp2px(7),LocalDisplay.dp2px(7));
                }
            }else {
                if(helper.getLayoutPosition() / 2 == getItemCount() / 2){
                    lp.setMargins(LocalDisplay.dp2px(30),LocalDisplay.dp2px(8),LocalDisplay.dp2px(7),0);
                }else{
                    lp.setMargins(LocalDisplay.dp2px(30),LocalDisplay.dp2px(8),LocalDisplay.dp2px(7),LocalDisplay.dp2px(7));
                }
            }
        }else{
            if((helper.getLayoutPosition() / 2) % 2 == 0){
                if(helper.getLayoutPosition() / 2 == 0){
                    lp.setMargins(LocalDisplay.dp2px(8),0,LocalDisplay.dp2px(30),LocalDisplay.dp2px(7));
                }else{
                    lp.setMargins(LocalDisplay.dp2px(8),LocalDisplay.dp2px(7),LocalDisplay.dp2px(30),LocalDisplay.dp2px(7));
                }
            }else {
                if(helper.getLayoutPosition() / 2 == getItemCount() / 2){
                    lp.setMargins(LocalDisplay.dp2px(8),LocalDisplay.dp2px(8),LocalDisplay.dp2px(30),0);
                }else{
                    lp.setMargins(LocalDisplay.dp2px(8),LocalDisplay.dp2px(8),LocalDisplay.dp2px(30),LocalDisplay.dp2px(7));
                }
            }
        }

//        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setLayoutParams(lp);
        ImageLoadHelper.getInstance().displayImage(helper.getConvertView().getContext(),item.name,imageView);
    }
}
