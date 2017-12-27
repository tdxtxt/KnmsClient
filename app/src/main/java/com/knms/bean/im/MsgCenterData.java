package com.knms.bean.im;

import com.netease.nimlib.sdk.msg.model.RecentContact;

import java.util.List;

/**
 * Created by Administrator on 2016/10/12.
 */

public class MsgCenterData {
    public List<KnmsMsg> knmsMsgs;
    public List<RecentContact> recentContacts;
    public int totalCount;//所有未读消息
    public int knmsCount;//铠恩买手系统未读消息
    public int knmsKefuCount;//铠恩买手客服未读消息
    public int imCount;//聊天未读消息
}
