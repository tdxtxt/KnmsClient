package com.knms.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.knms.android.R;
import com.knms.bean.shop.DeStyle;
import com.knms.util.ImageLoadHelper;
import com.knms.util.LocalDisplay;
import com.knms.util.ScreenUtil;

import java.util.List;

/**
 * Created by Administrator on 2016/8/31.
 */
public class ShopDecorationStyleFragmentAdapter extends BaseQuickAdapter<DeStyle,BaseViewHolder> {

    private Context mContext;
    public ShopDecorationStyleFragmentAdapter(Context context,List<DeStyle> data) {
        super(R.layout.item_custom_furniture, data);
        mContext = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, final DeStyle item) {
        ImageView iv_icon = helper.getView(R.id.iv_icon);
        iv_icon.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtil.getScreenWidth()));
        final TextView tv_xx = helper.getView(R.id.tv_xx);
        TextView tv_browse = helper.getView(R.id.tv_browse);
        tv_xx.setCompoundDrawablePadding(LocalDisplay.dp2px(5));
        tv_browse.setCompoundDrawablePadding(LocalDisplay.dp2px(5));
        tv_browse.setText(item.browseNumber + "");
        tv_xx.setText("收藏" + (item.collectNumber == 0 ? "" : item.collectNumber));
        helper.setText(R.id.tv_desc, item.cotitle);
        ImageLoadHelper.getInstance().displayImage(mContext,item.coInspirationPic, iv_icon,ScreenUtil.getScreenWidth(),ScreenUtil.getScreenWidth());
        if ("0".equals(item.iscollectNumber)) {//收藏
            Drawable drawable = helper.getConvertView().getResources().getDrawable(R.drawable.shou_cang_on);
            /// 这一步必须要做,否则不会显示.
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            tv_xx.setCompoundDrawables(drawable, null, null, null);
        } else {//未收藏
            Drawable drawable = helper.getConvertView().getResources().getDrawable(R.drawable.shou_cang);
            /// 这一步必须要做,否则不会显示.
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            tv_xx.setCompoundDrawables(drawable, null, null, null);
        }

    }
}
