package com.knms.bean.order;

import java.util.List;

/**
 * 类描述：
 * 创建人：Administrator
 * 创建时间：2017/9/13 16:02
 * 传参：
 * 返回:
 */
public class OrderRetainageBody {
    public String tradingStatus;//订单状态 0，未成功 1 生成 成功 （当前状态为1 ）
    public List<String> tradingIds;
}
