package com.knms.bean.comm;

import com.chad.library.adapter.base.entity.MultiItemEntity;

/**
 * 类描述：
 * 创建人：Administrator
 * 创建时间：2017/12/13 11:05
 * 传参：
 * 返回:
 */
public class MixingContentBean implements MultiItemEntity {
    public final static int IMG = 1;//图片信息
    public final static int TEXT = 2;//文字信息
    public int type;
    public String txt;

    @Override
    public int getItemType() {
        return type;
    }
}
