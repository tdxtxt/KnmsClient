package com.knms.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.knms.android.R;
import com.knms.bean.product.Furniture;
import com.knms.util.ImageLoadHelper;
import com.knms.util.LocalDisplay;
import com.knms.util.ScreenUtil;

import java.util.List;

/**
 * Created by tdx on 2016/8/29.
 */
public class CustomFurnitureAdapter extends BaseQuickAdapter<Furniture,BaseViewHolder> {
    private boolean isShow = false;
    private Context mContext;

    public CustomFurnitureAdapter(Context context,List<Furniture> mDatas) {
        this(context,mDatas,false);
    }

    public CustomFurnitureAdapter(Context context,List<Furniture> mDatas, boolean isShowStyley) {
        super(R.layout.item_custom_furniture, mDatas);
        this.isShow = isShowStyley;
        mContext = context;
    }


    @Override
    protected void convert(BaseViewHolder helper, Furniture item) {
        ImageView iv_icon = helper.getView(R.id.iv_icon);
        iv_icon.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, ScreenUtil.getScreenWidth()));
        TextView tv_xx = helper.getView(R.id.tv_xx);
        TextView tv_browse = helper.getView(R.id.tv_browse);
        tv_xx.setCompoundDrawablePadding(LocalDisplay.dp2px(5));
        tv_browse.setCompoundDrawablePadding(LocalDisplay.dp2px(5));
        tv_browse.setText(item.browseNumber + "");
        tv_xx.setText("收藏" + (item.collectNumber==0 ? "" : item.collectNumber));
        helper.setText(R.id.tv_desc, item.title);
        if(iv_icon.getTag() != null){
            if(!iv_icon.getTag().toString().equals(item.pic)){
                iv_icon.setTag(item.pic);
                ImageLoadHelper.getInstance().displayImageShowProgress(mContext,item.pic, iv_icon,ScreenUtil.getScreenWidth(),ScreenUtil.getScreenWidth());
            }
        }else {
            iv_icon.setTag(item.pic);
            ImageLoadHelper.getInstance().displayImageShowProgress(mContext,item.pic, iv_icon,ScreenUtil.getScreenWidth(),ScreenUtil.getScreenWidth());
        }
        if(item.iscollectNumber==0){//收藏
            Drawable drawable= helper.getConvertView().getResources().getDrawable(R.drawable.shou_cang_on);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            tv_xx.setCompoundDrawables(drawable,null,null,null);
        }else{//未收藏
            Drawable drawable= helper.getConvertView().getResources().getDrawable(R.drawable.shou_cang);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            tv_xx.setCompoundDrawables(drawable,null,null,null);
        }
        if (isShow) {
            ImageView img_style = helper.getView(R.id.furniture_style);
            img_style.setVisibility(View.VISIBLE);
            img_style.setImageResource(item.type == 0 ? R.drawable.q_09 : R.drawable.q_10);
        }
    }
}
