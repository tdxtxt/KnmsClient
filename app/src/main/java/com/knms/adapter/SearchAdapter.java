package com.knms.adapter;

import android.view.View;

import com.knms.activity.search.SearchActivity;
import com.knms.activity.base.BaseActivity;
import com.knms.activity.search.ProductFromSearchActivity;
import com.knms.adapter.baserecycler.BaseQuickAdapter;
import com.knms.adapter.baserecycler.BaseViewHolder;
import com.knms.android.R;
import com.knms.bean.other.Label;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.view.flowlayout.FlowLayout;
import com.knms.view.flowlayout.TagFlowLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tdx on 2016/9/21.
 */
public class SearchAdapter extends BaseQuickAdapter<Label>{
    public SearchAdapter(List<Label> data) {
        super(R.layout.item_search, data);
    }
    @Override
    protected void convert(final BaseViewHolder helper, Label item) {
        helper.setText(R.id.tv_label_name,item.name);
        TagFlowLayout tagFlowLayout = helper.getView(R.id.label_layout);
        if(item.subLables != null && item.subLables.size() > 0){
            tagFlowLayout.setVisibility(View.VISIBLE);
            SearchLabelAdapter adapter = new SearchLabelAdapter(item.subLables,false);
            tagFlowLayout.setAdapter(adapter);
            tagFlowLayout.setTag(item);
            tagFlowLayout.setOnTagClickListener(new TagFlowLayout.OnTagClickListener() {
                @Override
                public boolean onTagClick(View view, int position, FlowLayout parent) {
                    Label lableParent = (Label) parent.getTag();
                    Label lableChild = (Label) view.getTag();
                    lableChild.name=lableParent.name;
                    lableChild.parentId = lableParent.id;
                    RxBus.get().post(BusAction.SEARCH_PRODUCT,lableChild);
//                    Map<String,Object> params = new HashMap<String, Object>();
//                    params.put("key",getData().get(helper.getAdapterPosition()).name);
//                    params.put("labelId",lableChild.parentId);
//                    params.put("brandId",lableChild.id);
//                    if(parent.getContext() instanceof SearchActivity){
//                        SearchActivity activity = (SearchActivity) parent.getContext();
//                        activity.saveHistory(lableChild);
//                    }
//                    ((BaseActivity)parent.getContext()).startActivityAnimGeneral(ProductFromSearchActivity.class,params);
                    return false;
                }
            });
        }else{
            tagFlowLayout.setVisibility(View.GONE);
        }
    }
}
