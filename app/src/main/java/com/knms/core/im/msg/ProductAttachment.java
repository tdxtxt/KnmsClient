package com.knms.core.im.msg;
import com.google.gson.Gson;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;

/**
 * Created by Administrator on 2016/10/20.
 */

public class ProductAttachment implements MsgAttachment {
    public final static int TYPE_SEND_STATE = 1112;//表示要显示发送的产品本地的假数据
    public final static int TYPE_SHOW_STATE = 1111;//表示发送后的产品，要与ios约定好;
    public int type = TYPE_SHOW_STATE;//默认为1111
    public Product value;
    public ProductAttachment() {
    }

    /**
     * 自定义消息有两种显示方式，一种是用来发送的
     * @return
     */
    public boolean isShowState(){
        return 1111 == this.type;
    }
    @Override
    public String toJson(boolean send) {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
