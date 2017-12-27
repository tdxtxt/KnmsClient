package com.knms.bean.user;

import com.google.gson.annotations.SerializedName;
import com.netease.nimlib.sdk.auth.LoginInfo;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/8/23.
 */
public class User implements Serializable{
    public String sid;
    public String uicon;
    public String account;
    public String mobile;
    public String nickname;
    public int status;
    public String statusdesc;
    @SerializedName("imtoken")
    public String token;//聊天token
    @SerializedName("pushtoken")
    public String pushToken;
    public String usercode;
}
