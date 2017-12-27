package com.knms.other;

import android.text.TextUtils;

import com.knms.core.sku.Sku;
import com.knms.bean.sku.base.SkuModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by Administrator on 2017/7/27.
 */

public class SkuOnSubscribe implements Observable.OnSubscribe<SkuDataUI> {
    List<SkuModel> data;
    SkuDataUI result;
    Map<String,SkuModel> dataMap = new HashMap<>();
    public SkuOnSubscribe(List<SkuModel> data) {
        this.data = data;
        result = new SkuDataUI();
    }

    @Override
    public void call(Subscriber<? super SkuDataUI> subscriber) {
        result.specs = Sku.getSpecs(data);
        result.skuProducts = data;
        SkuModel defaultSku = new SkuModel();
        if(result.imgs == null) result.imgs = new HashSet<>();
        int stock = 0;
        if(data != null && data.size() > 0){
            for (SkuModel model : data) {
                stock += model.stock;//得到总库存
                if(!TextUtils.isEmpty(model.imageUrl)) result.imgs.add(model.imageUrl);//得到图片集合
                dataMap.put(model.getKey(),model);
            }
        }
        
        defaultSku.stock = stock;
        result.defaultSku = defaultSku;
        //添加规格分组
        result.attrValueAdapters.clear();
        result.localData = Sku.skuCollection(dataMap);
        subscriber.onNext(result);
        subscriber.onCompleted();
    }
}
