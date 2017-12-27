package com.knms.bean;

import com.knms.bean.other.Classify;
import com.knms.bean.product.Ad;

import java.util.List;

/**
 * Created by tdx on 2016/8/26.
 */
public class ClassifyIndex {
    public List<Classify> classifys;
    public List<List<Ad>> adpois;
    public int getMaxSize(){
        int classifysSize = 0,adsSize = 0;
        if(classifys != null) classifysSize = classifys.size();
        if(adpois != null) adsSize = adpois.size();
        return classifysSize;
    }
}
