package com.knms.other;

import android.text.TextUtils;
import android.util.Log;

import com.knms.adapter.AttrValueAdapter;
import com.knms.bean.sku.base.SkuModel;
import com.knms.bean.sku.base.Spec;
import com.knms.bean.sku.base.Vaule;
import com.knms.util.ToolsHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Administrator on 2017/7/25.
 */

public class SkuDataUI {
    public List<Vaule> selectAllVaules = new ArrayList<>();//获取所有选取好的Vaule
    public List<AttrValueAdapter> attrValueAdapters = new ArrayList<>();//获取所有标签adapter
    public List<Spec> specs = new ArrayList<>();//规格
    public List<SkuModel> skuProducts;//服务器返回的产品
    public SkuModel defaultSku = new SkuModel();
    public Map<String,? extends SkuModel> localData;//分组本地数据
    public Set<String> imgs = new HashSet<>();;

    public String getToastMsg(){
        List<Spec> temp = new ArrayList<>();
        StringBuffer stringBuf = new StringBuffer();
        for (Vaule vaule : selectAllVaules) {
            stringBuf.append(vaule.specName);
        }
        for (Spec spec : specs) {
            if(!stringBuf.toString().contains(spec.name)){
                temp.add(spec);
            }
        }
        ToolsHelper.getInstance().sort(temp,"specSort");
        return TextUtils.join("、",temp);
    }

    public void notifyDataChanged(){
        if(attrValueAdapters != null && attrValueAdapters.size() > 0){
            for (AttrValueAdapter adapter : attrValueAdapters) {
                adapter.notifyDataChanged();
            }
        }
    }
    public void handleBtn(){
        //处理未选中的按钮
        for (int i = 0; i < attrValueAdapters.size(); i++) {
            for (Vaule entity : attrValueAdapters.get(i).getData()) {
                List<Vaule> cacheSelected = new ArrayList<>();

                cacheSelected.addAll(selectAllVaules);
                List<Vaule> tempVaules = new ArrayList<>();
                tempVaules.addAll(selectAllVaules);

                //移除相同属性值&同规格属性值
                if(tempVaules.size() > 0){
                    for (Vaule vaule : tempVaules) {
                        if(vaule.specName.equals(entity.specName)){//未点击属性和当前已选择属性为同一规格，移除已选属性
                            cacheSelected.remove(vaule);
                        }
                    }
                }

                if(!cacheSelected.contains(entity)) cacheSelected.add(entity);

                ToolsHelper.getInstance().sort(cacheSelected,"id",true);
                String key = TextUtils.join(";",cacheSelected);
                String kesk = key;
                if (localData.get(key) != null && localData.get(key).stock > 0) {//有库存
                    entity.status = (entity.status == 1 ? 1 : 0);
                    kesk = kesk + "@true";
                } else {
                    entity.status = 2;
                    kesk = kesk + "@false";
                }
                Log.i("mapKey",kesk);
            }
            attrValueAdapters.get(i).notifyDataChanged();
        }
    }
}
