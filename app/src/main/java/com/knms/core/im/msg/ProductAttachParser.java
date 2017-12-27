package com.knms.core.im.msg;
import com.google.gson.Gson;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachmentParser;

/**
 * Created by Administrator on 2016/10/20.
 */

public class ProductAttachParser implements MsgAttachmentParser {
    Gson gson;
    @Override
    public MsgAttachment parse(String attach) {
        if(gson == null) gson = new Gson();
        return gson.fromJson(attach,ProductAttachment.class);
    }
}
