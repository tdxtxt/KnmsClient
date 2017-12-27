package com.knms.adapter;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.knms.activity.base.BaseActivity;
import com.knms.activity.pic.ImgBrowerPagerActivity;
import com.knms.android.R;
import com.knms.bean.comm.MixingContentBean;
import com.knms.util.ImageLoadHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Administrator on 2016/11/18.
 */

public class MixingContentAdapter extends BaseMultiItemQuickAdapter<MixingContentBean,BaseViewHolder> {

    public MixingContentAdapter() {
        super(null);
        addItemType(MixingContentBean.IMG, R.layout.item_mix_img);
        addItemType(MixingContentBean.TEXT,R.layout.item_mix_text);
    }

    public MixingContentAdapter(List<MixingContentBean> data) {
        super(data);
        addItemType(MixingContentBean.IMG, R.layout.item_mix_img);
        addItemType(MixingContentBean.TEXT,R.layout.item_mix_text);
    }
    public List<String> getBeansByType(int type){
        List<String> data = new ArrayList<>();
        for (MixingContentBean bean : mData) {
            if(bean.getItemType() == type){
                data.add(bean.txt);
            }
        }
        return data;
    }
    @Override
    protected void convert(BaseViewHolder helper, final MixingContentBean item) {
        switch (item.getItemType()){
            case MixingContentBean.IMG:
                ImageView imgView = helper.getView(R.id.content_img);
                ImageLoadHelper.getInstance().displayKnowledgeImage(mContext,item.txt, imgView,false);
                imgView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Map<String,Object> map=new HashMap<String, Object>();
                        map.put("currentPath",item.txt);
                        map.put("data", getBeansByType(MixingContentBean.IMG));
                        ((BaseActivity)mContext).startActivityAnimGeneral(ImgBrowerPagerActivity.class,map);
                    }
                });
                break;
            case MixingContentBean.TEXT:
                if(TextUtils.isEmpty(item.txt)){
                    helper.setVisible(R.id.content_text,false);
                }else{
                    helper.setVisible(R.id.content_text,true);
                }
                helper.setText(R.id.content_text,item.txt.trim());
                break;
        }
    }
}
