package com.knms.bean.sku.base;

import java.util.List;

/**
 * Created by Administrator on 2017/7/25.
 */

public class Spec {
    public String name;//规格名称
    public List<Vaule> vaules;//属性值
    public int specSort;//排序

    @Override
    public String toString() {
        return name;
    }
}
