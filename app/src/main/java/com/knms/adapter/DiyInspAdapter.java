package com.knms.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.knms.adapter.base.CommonAdapter;
import com.knms.adapter.base.ViewHolder;
import com.knms.android.R;
import com.knms.bean.product.Desytyle;
import com.knms.util.ImageLoadHelper;
import com.knms.util.LocalDisplay;
import com.knms.util.ScreenUtil;

import java.util.List;

import static android.R.attr.data;

/**
 * Created by tdx on 2016/8/29.
 */
public class DiyInspAdapter extends BaseQuickAdapter<Desytyle,BaseViewHolder> {
    public DiyInspAdapter(List<Desytyle> data) {
        super(R.layout.item_custom_furniture, data);
    }


    @Override
    protected void convert(BaseViewHolder helper, Desytyle item) {
        ImageView iv_icon = helper.getView(R.id.iv_icon);
        iv_icon.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ScreenUtil.getScreenWidth()));
        final TextView tv_xx = helper.getView(R.id.tv_xx);
        TextView tv_browse = helper.getView(R.id.tv_browse);
        tv_xx.setCompoundDrawablePadding(LocalDisplay.dp2px(5));
        tv_browse.setCompoundDrawablePadding(LocalDisplay.dp2px(5));
        tv_browse.setText(item.tourTotal + "");
        tv_xx.setText(item.collectTotal.equals("0")?"收藏":item.collectTotal);
        if(iv_icon.getTag() != null){
            if(!iv_icon.getTag().toString().equals(item.imageUrl)){
                iv_icon.setTag(item.imageUrl);
                ImageLoadHelper.getInstance().displayImageShowProgress(mContext,item.imageUrl,iv_icon,ScreenUtil.getScreenWidth(),ScreenUtil.getScreenWidth());
            }
        }else{
            iv_icon.setTag(item.imageUrl);
            ImageLoadHelper.getInstance().displayImageShowProgress(mContext,item.imageUrl,iv_icon,ScreenUtil.getScreenWidth(),ScreenUtil.getScreenWidth());
        }

        helper.setText(R.id.tv_desc,item.title);

        if("0".equals(item.collectStatus)){//收藏
            Drawable drawable= helper.getConvertView().getResources().getDrawable(R.drawable.shou_cang_on);
            /// 这一步必须要做,否则不会显示.
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            tv_xx.setCompoundDrawables(drawable,null,null,null);
        }else{//未收藏
            Drawable drawable= helper.getConvertView().getResources().getDrawable(R.drawable.shou_cang);
            /// 这一步必须要做,否则不会显示.
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            tv_xx.setCompoundDrawables(drawable,null,null,null);
        }
    }
}
