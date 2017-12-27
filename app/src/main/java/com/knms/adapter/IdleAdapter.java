package com.knms.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.knms.activity.IdleDetailsActivity;
import com.knms.activity.base.BaseActivity;
import com.knms.activity.base.BaseFragmentActivity;
import com.knms.android.R;
import com.knms.bean.other.Pic;
import com.knms.bean.product.Idle;
import com.knms.oncall.RecyclerItemClickListener;
import com.knms.util.CommonUtils;
import com.knms.util.ImageLoadHelper;
import com.knms.util.LocalDisplay;
import com.knms.util.StrHelper;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Administrator on 2016/9/2.
 */
public class IdleAdapter extends BaseQuickAdapter<Idle, BaseViewHolder> {
    private Context mContext;

    public IdleAdapter(Context context) {
        super(R.layout.item_idel_goods, new ArrayList<Idle>());
        mContext = context;
    }

    @Override
    protected void convert(final BaseViewHolder helper, final Idle item) {
        if (item == null) return;
        int num = 1;
        final ImageView imgCollect = helper.getView(R.id.is_collect_img);
        final TextView tvCollect = helper.getView(R.id.tv_collectNumber);
        ImageView head = helper.getView(R.id.vendor_head_portrait);
        ImageLoadHelper.getInstance().displayImageHead(mContext, item.userPhoto, head, LocalDisplay.dp2px(40), LocalDisplay.dp2px(40));
        helper.setText(R.id.pet_name, CommonUtils.phoneNumberFormat(item.nickname + ""))
                .setText(R.id.publish_time, StrHelper.displayTime(item.leasetime + "", false, false))
                .setText(R.id.cost_price, CommonUtils.addMoneySymbol(CommonUtils.keepTwoDecimal(item.original)))
                .setText(R.id.current_price, CommonUtils.addMoneySymbol(CommonUtils.keepTwoDecimal(item.price)))
                .setText(R.id.tv_desc, item.coremark)
                .setText(R.id.tv_browseNumber, item.browseNumber == 0 ? "浏览" : item.browseNumber + "")
                .setText(R.id.tv_collectNumber, item.collectNumber == 0 ? "收藏" : item.collectNumber + "")
                .setText(R.id.tv_city, item.goareaname + "");
        helper.setVisible(R.id.cost_price, item.original < 0 ? false : true);
        TextView freeshopprice = helper.getView(R.id.cost_price);
        freeshopprice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);//中间加横线

        final RecyclerView recyclerView = helper.getView(R.id.recycler_imgs);
        recyclerView.setTag(item);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(helper.convertView.getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(new BaseQuickAdapter<Pic, BaseViewHolder>(R.layout.item_img_70x70, item.imglist) {
            @Override
            protected void convert(BaseViewHolder helper, Pic item) {
                ImageView iv_pic = helper.getView(R.id.iv_pic);
                ImageLoadHelper.getInstance().displayImage(mContext, item.url, iv_pic, LocalDisplay.dp2px(70), LocalDisplay.dp2px(70));
            }
        });

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(helper.getConvertView().getContext(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (item != null) {
                    Map<String, Object> params = new ArrayMap<String, Object>();
                    params.put("id", ((Idle) recyclerView.getTag()).id);
                    if (helper.getConvertView().getContext() instanceof BaseFragmentActivity) {
                        ((BaseFragmentActivity) helper.getConvertView().getContext()).startActivityAnimGeneral(IdleDetailsActivity.class, params);
                    } else if (helper.getConvertView().getContext() instanceof BaseActivity) {
                        ((BaseActivity) helper.getConvertView().getContext()).startActivityAnimGeneral(IdleDetailsActivity.class, params);
                    } else {
                        Intent intent = new Intent(helper.getConvertView().getContext(), IdleDetailsActivity.class);
                        intent.putExtra("id", ((Idle) recyclerView.getTag()).id);
                        ((Activity) helper.getConvertView().getContext()).startActivity(intent);
                    }
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
            }
        }));

        if ("0".equals(item.iscollectNumber)) {//收藏
//            Drawable drawable= helper.getConvertView().getResources().getDrawable(R.drawable.shou_cang_on);
//            /// 这一步必须要做,否则不会显示.
//            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            imgCollect.setImageResource(R.drawable.shou_cang_on);
        } else {//未收藏
//            Drawable drawable= helper.getConvertView().getResources().getDrawable(R.drawable.shou_cang_off);
//            /// 这一步必须要做,否则不会显示.
//            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
//            tv_xx.setCompoundDrawables(drawable,null,null,null);
            imgCollect.setImageResource(R.drawable.shou_cang);
        }
//        recyclerView.setLayoutFrozen(true);
    }
}
