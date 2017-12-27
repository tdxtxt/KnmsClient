package com.knms.adapter;

import com.knms.adapter.baserecycler.BaseQuickAdapter;
import com.knms.adapter.baserecycler.BaseViewHolder;
import com.knms.android.R;
import com.knms.bean.other.Label;
import com.knms.util.Tst;
import com.knms.view.flowlayout.TagFlowLayout;
import java.util.ArrayList;
import java.util.List;

import static com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.M;

/**
 * Created by tdx on 2016/9/19.
 */
public class MoreLabelAdapter extends BaseQuickAdapter<Label>{
    public List<Label> selects;
    public int MAXLABEL = 10;
    public MoreLabelAdapter(List<Label> data,List<Label> selects) {
        super(R.layout.item_more_label, data);
        this.selects = selects;
        if(this.selects == null) this.selects = new ArrayList<>();
    }
    @Override
    protected void convert(BaseViewHolder helper, Label item) {
        helper.setText(R.id.tv_title,item.name + ":");
        TagFlowLayout flowLayout = helper.getView(R.id.tag_flow_layout);
        final LabelAdapter adapter = new LabelAdapter(item.subLables);
        flowLayout.setAdapter(adapter);
        adapter.setSelectdList(selects);
        flowLayout.setOnSelectListener(new TagFlowLayout.OnSelectListener<Label>() {
            @Override
            public void onSelected(List<Label> selectPosSet,Label remove) {
                if(selects != null) selects.remove(remove);
                if(selects!= null && selects.size() == MAXLABEL){//满了最大限制
                    Tst.showToast("最多可选择" + MAXLABEL + "个标签");
                    adapter.setSelectdList(selects);
                    return;
                }
                for (Label item : selectPosSet){
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
