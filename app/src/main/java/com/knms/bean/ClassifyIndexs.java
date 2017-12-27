package com.knms.bean;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.knms.bean.other.Classify;
import com.knms.bean.product.Ad;

import java.util.List;

/**
 * Created by tdx on 2016/8/26.
 */
public class ClassifyIndexs implements MultiItemEntity {
    public int currentType;
    public static final int TYPE_MENU = 0;
    public static final int TYPE_AD = 1;
    public Classify classify;
    public Ad ad;
    public ClassifyIndexs(){
    }
    public ClassifyIndexs(int type){
        this.currentType = type;
    }
    @Override
    public int getItemType() {
        if(currentType == 0) currentType = classify == null ? TYPE_AD : TYPE_MENU;
        return currentType;
    }
}
