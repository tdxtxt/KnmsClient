package com.knms.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.knms.adapter.baserecycler.BaseQuickAdapter;
import com.knms.adapter.baserecycler.BaseViewHolder;
import com.knms.android.R;
import com.knms.bean.other.Pic;
import com.knms.bean.product.Idle;
import com.knms.util.CommonUtils;
import com.knms.util.ImageLoadHelper;
import com.knms.util.LocalDisplay;
import com.knms.util.ScreenUtil;
import com.knms.util.StrHelper;
import com.knms.view.clash.FullyLinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tdx on 2016/8/25.
 */
public class IdleIndexAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<Idle> mData = new ArrayList<>();

    public IdleIndexAdapter(List<Idle> data) {
        this.mData = data == null ? new ArrayList<Idle>() : data;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        BaseViewHolder baseViewHolder = null;
        this.mContext = parent.getContext();
        this.mLayoutInflater = LayoutInflater.from(mContext);
        baseViewHolder = createBaseViewHolder(parent, R.layout.horizontallistview_item);
        return baseViewHolder;
    }

    protected BaseViewHolder createBaseViewHolder(ViewGroup parent, int layoutResId) {
        return new BaseViewHolder(getItemView(layoutResId, parent));
    }

    protected View getItemView(int layoutResId, ViewGroup parent) {
        return mLayoutInflater.inflate(layoutResId, parent, false);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        convert((BaseViewHolder) holder, mData.get(position % mData.size()));
    }

    protected void convert(BaseViewHolder helper, final Idle item) {
        if (item == null) return;
        helper.convertView.setTag(item);
        final ImageView head = helper.getView(R.id.vendor_head_portrait);
        ImageLoadHelper.getInstance().displayImageHead(mContext, item.userPhoto, head);
        helper.setText(R.id.pet_name, CommonUtils.phoneNumberFormat(item.nickname + ""))
                .setText(R.id.publish_time, StrHelper.displayTime(item.leasetime + "", false, false))
                .setText(R.id.cost_price, CommonUtils.addMoneySymbol(CommonUtils.keepTwoDecimal(item.original)))
                .setText(R.id.current_price, CommonUtils.addMoneySymbol(CommonUtils.keepTwoDecimal(item.price)))
                .setText(R.id.tv_desc, item.coremark + "")
                .setText(R.id.tv_browseNumber, item.browseNumber + "")
                .setText(R.id.tv_collectNumber, item.collectNumber + "")
                .setText(R.id.tv_city, item.goareaname + "");
        TextView freeshopprice = helper.getView(R.id.cost_price);
        freeshopprice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);//中间加横线
        helper.setVisible(R.id.cost_price, item.original < 0 ? false : true);
        RecyclerView recyclerView = helper.getView(R.id.recycler_imgs);
        FullyLinearLayoutManager linearLayoutManager = new FullyLinearLayoutManager(helper.convertView.getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        item.imglist = item.imglist.size() < 3 ? item.imglist : item.imglist.subList(0, 3);
        recyclerView.setAdapter(new BaseQuickAdapter<Pic>(R.layout.item_img_70x70, item.imglist) {
            @Override
            protected void convert(BaseViewHolder helper, final Pic item) {
                ImageView iv_pic = helper.getView(R.id.iv_pic);
                ImageLoadHelper.getInstance().displayImage(mContext, item.url, iv_pic);
            }
        });
        recyclerView.setLayoutFrozen(true);

        if ("0".equals(item.iscollectNumber)) {//收藏
            helper.setImageResource(R.id.iv_collect, R.drawable.shou_cang_on);
        } else {//未收藏
            helper.setImageResource(R.id.iv_collect, R.drawable.shou_cang);
        }
    }

    public void setNewData(List<Idle> data) {
        if (data == null) return;
        this.mData.clear();
        this.mData.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
    }
}
