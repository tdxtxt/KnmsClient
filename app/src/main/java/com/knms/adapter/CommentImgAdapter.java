package com.knms.adapter;

import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.knms.android.R;
import com.knms.bean.other.Pic;
import com.knms.util.ImageLoadHelper;
import com.knms.util.LocalDisplay;

import java.util.List;

/**
 * Created by Administrator on 2017/5/2.
 */

public class CommentImgAdapter extends BaseQuickAdapter<Pic,BaseViewHolder>{
    int maxItemCount;
    public CommentImgAdapter(List<Pic> data) {
        super(R.layout.item_grid_img_80x80, data);
    }
    @Override
    protected void convert(BaseViewHolder helper, Pic item) {
        ImageView iv_img = helper.getView(R.id.iv_pic);
        ImageLoadHelper.getInstance().displayImage(helper.getConvertView().getContext(),item.url,iv_img, LocalDisplay.dp2px(70),LocalDisplay.dp2px(70));
    }
    public void setMaxItemCount(int maxItemCount){
        this.maxItemCount = maxItemCount;
    }

    @Override
    public int getItemCount() {
        return this.maxItemCount > 0 ? (mData.size() > this.maxItemCount ? this.maxItemCount : super.getItemCount())  : super.getItemCount();
    }
}
