package com.knms.bean.pay;

import java.util.List;

/**
 * 类描述：
 * 创建人：Administrator
 * 创建时间：2017/8/25 10:19
 * 传参：
 * 返回:
 */
public class PayTypeBody {
    public List<PayType> payMethodBos;
    public class PayType{
        public String payType;
    }
}
