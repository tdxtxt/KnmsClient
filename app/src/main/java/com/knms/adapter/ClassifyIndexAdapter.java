package com.knms.adapter;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseViewHolder;
import com.knms.activity.base.BaseActivity;
import com.knms.activity.goods.ProductMainActivity;
import com.knms.adapter.base.ViewHolder;
import com.knms.adapter.baserecycler.BaseQuickAdapter;
import com.knms.android.R;
import com.knms.bean.ClassifyIndex;
import com.knms.bean.other.Classify;
import com.knms.oncall.BannerOnclick;
import com.knms.util.ImageLoadHelper;
import com.knms.util.ScreenUtil;
import com.knms.util.ToolsHelper;
import com.knms.view.clash.FullyGridLayoutManager;

import java.util.HashMap;

/**
 * Created by tdx on 2016/8/26.
 */
public class ClassifyIndexAdapter extends BaseAdapter{
    ClassifyIndex data;
    private BaseActivity mContext;

    public ClassifyIndexAdapter(BaseActivity context, ClassifyIndex data){
        mContext = context;
        this.data = data;
        if(data == null) data = new ClassifyIndex();
    }
    @Override
    public int getCount() {
        return data.getMaxSize();
    }

    @Override
    public Object getItem(int position) {
        return "no value";
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        ViewHolder viewHolder = ViewHolder.get(mContext, convertView, parent, R.layout.item_classify_index, position);
        if(data.classifys != null && data.classifys.size() > position){
            viewHolder.getView(R.id.rl_classify).setVisibility(View.VISIBLE);
            viewHolder.setText(R.id.tv_title_center,data.classifys.get(position).name);
            RecyclerView rcyView = viewHolder.getView(R.id.recycler_view_menu);
            GridLayoutManager layoutManager = new FullyGridLayoutManager(mContext,4){
                @Override
                public boolean canScrollVertically() {
                    return false;
                }
            };
            layoutManager.setSmoothScrollbarEnabled(true);
            rcyView.setLayoutManager(layoutManager);
            rcyView.setHasFixedSize(true);
            rcyView.setNestedScrollingEnabled(false);
            ToolsHelper.getInstance().sort(data.classifys.get(position).classifyChild,"seq");
            final ClassifyIndexItemMenuAdapter adapter = new ClassifyIndexItemMenuAdapter(mContext,data.classifys.get(position).classifyChild);
            adapter.setOnItemClickListener(new com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener<Classify>() {
                @Override
                public void onItemClick(com.chad.library.adapter.base.BaseQuickAdapter<Classify, ? extends BaseViewHolder> adapter, View view, int position) {
                    Classify item = adapter.getItem(position);
                    HashMap<String,Object> parmas = new HashMap<String, Object>();
                    parmas.put("id",item.id);
                    parmas.put("title",item.name);
                    mContext.startActivityAnimGeneral(ProductMainActivity.class,parmas);
                }
            });
            rcyView.setAdapter(adapter);
        }else{
            viewHolder.getView(R.id.rl_classify).setVisibility(View.GONE);
        }
        ImageView iv_ad = viewHolder.getView(R.id.iv_ad);
        if(data.adpois != null && data.adpois.size() > position
                && data.adpois.get(position) != null && data.adpois.get(position).size() > 0){
            iv_ad.setVisibility(View.VISIBLE);
            ImageLoadHelper.getInstance().displayImage(mContext,data.adpois.get(position).get(0).name,(ImageView) viewHolder.getView(R.id.iv_ad),ScreenUtil.getScreenWidth(), /*LocalDisplay.dp2px(110)*/0);
        }else{
            iv_ad.setVisibility(View.GONE);
        }
        iv_ad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BannerOnclick.advertisementClick(mContext,data.adpois.get(position).get(0));
            }
        });
        return viewHolder.getConvertView();
    }
}
