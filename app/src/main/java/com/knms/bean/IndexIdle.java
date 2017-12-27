package com.knms.bean;

import com.google.gson.annotations.SerializedName;
import com.knms.bean.product.Idle;
import com.knms.bean.product.Menu;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2016/8/24.
 */
public class IndexIdle implements Serializable {
    private static final long serialVersionUID = -2386143574181075167L;
    @SerializedName("idle")
    public List<Idle> idles;
    @SerializedName("menu")
    public List<Menu> menus;
}
