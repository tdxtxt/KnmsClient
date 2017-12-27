package com.knms.bean.im;

import java.io.Serializable;

/**
 * Created by tdx on 2016/9/28.
 */
public class KnmsMsg implements Serializable{
    private static final long serialVersionUID = 3098913516870267642L;
    public int id;
    public int notReadNumber;//未读消息数量
    public String content;//之前最后一条消息
    public String time;//之前最后一条消息时间
    public int role;//发送者角色 1：用户， 2：买手客服

    public int sendStatus;//1发送中，2发送失败，3发送成功


    @Override
    public String toString() {
        return "id:" + this.id + ";content:" + this.content + ";role:" + this.role + ";time:" + this.time;
    }
    @Override
    public boolean equals(Object arg0) {
        if(arg0 instanceof KnmsMsg){
            if(this.toString().equals(arg0.toString())){
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }
}
