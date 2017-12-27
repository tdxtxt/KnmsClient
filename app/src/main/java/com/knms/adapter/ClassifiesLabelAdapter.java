package com.knms.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.knms.activity.fastfind.QuicklyGoodsAcitvity;
import com.knms.android.R;
import com.knms.bean.other.Classify;
import com.knms.bean.other.Label;
import com.knms.util.Tst;
import com.knms.view.flowlayout.TagFlowLayout;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/22.
 */

public class ClassifiesLabelAdapter extends BaseQuickAdapter<Classify,BaseViewHolder>{
    public List<Classify> selects;
    public int MAXLABEL = 2;
    public ClassifiesLabelAdapter(List<Classify> data) {
        super(R.layout.item_more_label, data);
        if(this.selects == null) this.selects = new ArrayList<>();
    }

    @Override
    public void setNewData(List<Classify> data) {
        if(this.selects != null) this.selects.clear();
        super.setNewData(data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Classify item) {
        TagFlowLayout flowLayout = helper.getView(R.id.tag_flow_layout);
        final ClassifiesAdapter  adapter = new ClassifiesAdapter(item.classifyChild);
        flowLayout.setAdapter(adapter);
        helper.setText(R.id.tv_title,item.name);
        adapter.setSelectdList(selects);
        flowLayout.setOnSelectListener(new TagFlowLayout.OnSelectListener<Classify>() {
            @Override
            public void onSelected(List<Classify> selectPosSet,Classify remove) {
                MobclickAgent.onEvent(mContext, "eachClassifyTagBtnClick");
                if(selects != null) selects.remove(remove);
                if(selects != null && selects.size() == MAXLABEL){//满了最大限制
                    Tst.showToast("最多可选择"+ MAXLABEL + "个家具种类标签哦~");
                    adapter.setSelectdList(selects);
                    return;
                }
                for (Classify item : selectPosSet){
                    if(selects.toString().contains(item.toString())){//包含了
                        continue;
                    }else{//不包含
                        selects.add(item);
                    }
                }
            }
        });
    }
}
