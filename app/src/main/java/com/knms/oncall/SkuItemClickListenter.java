package com.knms.oncall;

import android.text.TextUtils;
import android.view.View;

import com.knms.adapter.AttrValueAdapter;
import com.knms.bean.sku.base.SkuModel;
import com.knms.bean.sku.base.Vaule;
import com.knms.other.SkuDataUI;
import com.knms.util.ToolsHelper;
import com.knms.view.flowlayout.FlowLayout;
import com.knms.view.flowlayout.TagFlowLayout;

/**
 * Created by Administrator on 2017/7/26.
 */

public class SkuItemClickListenter implements TagFlowLayout.OnTagClickListener{
    SkuDataUI mSkuDataUI;
    AttrValueAdapter currentAdapter;
    OnClick onClick;
    public SkuItemClickListenter(SkuDataUI skuDataUI,AttrValueAdapter adapter,OnClick onClick){
        this.mSkuDataUI = skuDataUI;
        this.currentAdapter = adapter;
        this.onClick = onClick;
    }
    @Override
    public boolean onTagClick(View view, int position, FlowLayout parent) {
        Vaule item = currentAdapter.getItem(position);
        //屏蔽不可点击
        if (item.status == 2) {
            return false;
        }

        // 设置当前单选点击
        for (Vaule vaule : currentAdapter.getData()) {
            if (vaule.equals(item)) {
                if(item.equals(currentAdapter.currentVaule)){//当前已选择了再次的点击
                    vaule.status = 0;
                    currentAdapter.currentVaule = null;
                }else{
                    //添加已经选择的对象
                    vaule.status = 1;
                    currentAdapter.currentVaule = vaule;
                }
            } else {
                vaule.status = (vaule.status == 2 ? 2 : 0);
            }
        }
        //存放当前被点击的按钮
        mSkuDataUI.selectAllVaules.clear();
        for (int i = 0; i < mSkuDataUI.attrValueAdapters.size(); i++) {
            if (mSkuDataUI.attrValueAdapters.get(i).currentVaule != null) {
                mSkuDataUI.selectAllVaules.add(mSkuDataUI.attrValueAdapters.get(i).currentVaule);
            }
        }
        ToolsHelper.getInstance().sort(mSkuDataUI.selectAllVaules,"id",true);
        //处理未选中的按钮
        mSkuDataUI.handleBtn();

        String key = TextUtils.join(";",mSkuDataUI.selectAllVaules);
        SkuModel skuModel = mSkuDataUI.localData.get(key);
        if(skuModel == null) skuModel = mSkuDataUI.defaultSku;
        onClick.clickItem(view, skuModel);
        return false;
    }
}
