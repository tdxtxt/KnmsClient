package cn.shareuzi.channel;

import android.content.Context;

import cn.shareuzi.interfaces.IShareBase;


/**
 * Created by zhanglifeng
 */
public abstract class ShareBase implements IShareBase {

    Context context;

    public ShareBase(Context context) {
        this.context = context;
    }

}
