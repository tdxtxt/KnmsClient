package com.knms.adapter;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.knms.activity.base.BaseActivity;
import com.knms.activity.goods.ProductMainActivity;
import com.knms.android.R;
import com.knms.bean.ClassifyIndexs;
import com.knms.bean.other.Classify;
import com.knms.bean.product.Ad;
import com.knms.oncall.BannerOnclick;
import com.knms.util.ImageLoadHelper;
import com.knms.util.LocalDisplay;
import com.knms.util.ScreenUtil;
import com.knms.util.ToolsHelper;

import java.util.HashMap;
import java.util.List;


/**
 * Created by tdx on 2016/8/26.
 */
public class ClassifyIndexAdapterF extends BaseMultiItemQuickAdapter<ClassifyIndexs,BaseViewHolder> {
    public ClassifyIndexAdapterF(List<ClassifyIndexs> data) {
        super(data);
        addItemType(ClassifyIndexs.TYPE_MENU, R.layout.item_classify_index_menu);
        addItemType(ClassifyIndexs.TYPE_AD, R.layout.item_classify_index_ad);
    }
    @Override
    protected void convert(final BaseViewHolder helper, ClassifyIndexs item) {
        switch (helper.getItemViewType()) {
            case ClassifyIndexs.TYPE_MENU:
                Classify classify = item.classify;
                helper.setText(R.id.tv_title_center,classify.name);
                RecyclerView rcyView = helper.getView(R.id.recycler_view_menu);
                GridLayoutManager layoutManager = new GridLayoutManager(mContext, 4){
                    @Override
                    public boolean canScrollVertically() {
                        return false;
                    }
                };
                layoutManager.setAutoMeasureEnabled(true);
                rcyView.setLayoutManager(layoutManager);
                rcyView.setNestedScrollingEnabled(false);//解决滑动惯性问题
                rcyView.setFocusable(false);
                ToolsHelper.getInstance().sort(classify.classifyChild,"seq");
                if(null == rcyView.getAdapter()){
                    ClassifyIndexItemMenuAdapter adapter = new ClassifyIndexItemMenuAdapter(mContext,classify.classifyChild);
                    adapter.setOnItemClickListener(new OnItemClickListener<Classify>() {
                        @Override
                        public void onItemClick(BaseQuickAdapter<Classify, ? extends BaseViewHolder> adapter, View view, int position) {
                            Classify item = adapter.getItem(position);
                            HashMap<String,Object> parmas = new HashMap<String, Object>();
                            parmas.put("id",item.id);
                            parmas.put("title",item.name);
                            ((BaseActivity) helper.getConvertView().getContext()).startActivityAnimGeneral(ProductMainActivity.class,parmas);
                        }
                    });
                    rcyView.setAdapter(adapter);
                    int count = 20;
                    RecyclerView.RecycledViewPool pool = rcyView.getRecycledViewPool();
                    pool.setMaxRecycledViews(adapter.getItemViewType(0),count);
                    for(int index =0;index < count;index++) {
                        pool.putRecycledView(rcyView.getAdapter().createViewHolder(rcyView, adapter.getItemViewType(0)));
                    }
                }else{
                    ClassifyIndexItemMenuAdapter adapterc = (ClassifyIndexItemMenuAdapter) rcyView.getAdapter();
                    adapterc.setNewData(classify.classifyChild);
                }
                break;
            case ClassifyIndexs.TYPE_AD:
                Ad ad = item.ad;
                ImageView iv_ad = helper.getView(R.id.iv_ad);
                ImageLoadHelper.getInstance().displayImage(mContext,ad.name,iv_ad, ScreenUtil.getScreenWidth(), LocalDisplay.dp2px(110));
                iv_ad.setTag(ad);
                iv_ad.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Ad ad = (Ad) view.getTag();
                        BannerOnclick.advertisementClick(mContext,ad);
                    }
                });
                iv_ad.setFocusable(false);
                break;
        }
    }



   /* ClassifyIndex data;
    private BaseActivity mContext;
    public ClassifyIndexAdapterF(BaseActivity context,ClassifyIndex data){
        super(R.layout.item_classify_index,data.classifys);
        this.data = data;
        if(this.data == null) this.data = new ClassifyIndex();
        mContext = context;
    }
    @Override
    protected void convert(BaseViewHolder helper, Classify item) {
        final int position = helper.getLayoutPosition();
        helper.getView(R.id.rl_classify).setVisibility(View.VISIBLE);
        helper.setText(R.id.tv_title_center,item.name);
        RecyclerView rcyView = helper.getView(R.id.recycler_view_menu);
        RecyclerView.LayoutManager layoutManager = new FullyGridLayoutManager(mContext, 4) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
//        layoutManager.setAutoMeasureEnabled(true);
        rcyView.setLayoutManager(layoutManager);
        rcyView.setNestedScrollingEnabled(false);//解决滑动惯性问题
//            rcyView.setHasFixedSize(true);
        ToolsHelper.getInstance().sort(item.classifyChild,"seq");
        if(null == rcyView.getAdapter()){
            ClassifyIndexItemMenuAdapter adapter = new ClassifyIndexItemMenuAdapter(mContext,data.classifys.get(position).classifyChild);
            adapter.setOnItemClickListener(new OnItemClickListener<Classify>() {
                @Override
                public void onItemClick(BaseQuickAdapter<Classify, ? extends BaseViewHolder> adapter, View view, int position) {
                    Classify item = adapter.getItem(position);
                    HashMap<String,Object> parmas = new HashMap<String, Object>();
                    parmas.put("id",item.id);
                    parmas.put("title",item.name);
                    mContext.startActivityAnimGeneral(ProductMainActivity.class,parmas);
                }
            });
            rcyView.setAdapter(adapter);
        }else{
            ClassifyIndexItemMenuAdapter adapterc = (ClassifyIndexItemMenuAdapter) rcyView.getAdapter();
            adapterc.setNewData(data.classifys.get(position).classifyChild);
        }
//        rcyView.setAdapter(adapter);
        ImageView iv_ad = helper.getView(R.id.iv_ad);
        if(data.adpois != null && data.adpois.size() > position
                && data.adpois.get(position) != null && data.adpois.get(position).size() > 0){
            iv_ad.setVisibility(View.VISIBLE);
            ImageLoadHelper.getInstance().displayImage(mContext,data.adpois.get(position).get(0).name,(ImageView) helper.getView(R.id.iv_ad),ScreenUtil.getScreenWidth(), LocalDisplay.dp2px(110));
        }else{
            iv_ad.setVisibility(View.GONE);
        }
        iv_ad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BannerOnclick.advertisementClick(mContext,data.adpois.get(position).get(0));
            }
        });
    }*/
}
