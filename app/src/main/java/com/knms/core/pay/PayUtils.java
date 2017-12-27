package com.knms.core.pay;

import android.app.Activity;
import android.widget.Toast;

import com.knms.app.KnmsApp;
import com.knms.core.pay.alipay.Alipay;
import com.knms.core.pay.weixin.WXPay;
import com.knms.oncall.CallBack;
import com.knms.util.Tst;

/**
 * 工具类
 */
public class PayUtils {
    public static boolean isInstallWechat(){
        String wx_appid = "wxb3a87ea63aab0279";     //替换为自己的appid
        WXPay.init(KnmsApp.getInstance(), wx_appid);      //要在支付前调用
        return WXPay.getInstance().check();
    }
    /**
     * 微信支付
     * @param pay_param 支付服务生成的支付参数
     */
    public static void doWXPay(final String payId, String pay_param, final CallBack<String> callBack) {
        String wx_appid = "wxb3a87ea63aab0279";     //替换为自己的appid
        WXPay.init(KnmsApp.getInstance(), wx_appid);      //要在支付前调用
        WXPay.getInstance().doPay(pay_param, new WXPay.WXPayResultCallBack() {
            @Override
            public void onSuccess() {
                callBack.onCallBack(payId);
//                Tst.showToast("支付成功");
            }
            @Override
            public void onError(int error_code) {
                switch (error_code) {
                    case WXPay.NO_OR_LOW_WX:
                        Tst.showToast("未安装微信或微信版本过低");
                        break;

                    case WXPay.ERROR_PAY_PARAM:
                        Tst.showToast("参数错误");
                        break;

                    case WXPay.ERROR_PAY:
                        Tst.showToast("支付失败");
                        break;
                }
            }
            @Override
            public void onCancel() {
                Tst.showToast("支付取消");
            }
        });
    }
    /**
     * 支付宝支付
     * @param pay_param 支付服务生成的支付参数
     */
    public static void doAlipay(Activity activity, final String payId, String pay_param, final CallBack<String> callBack) {
        new Alipay(activity, pay_param, new Alipay.AlipayResultCallBack() {
            @Override
            public void onSuccess() {
//                Tst.showToast("支付成功");
                callBack.onCallBack(payId);
            }
            @Override
            public void onDealing() {
                Tst.showToast("支付处理中...");
            }
            @Override
            public void onError(int error_code) {
                switch (error_code) {
                    case Alipay.ERROR_RESULT:
                        Tst.showToast("支付失败:支付结果解析错误");
                        break;
                    case Alipay.ERROR_NETWORK:
                        Tst.showToast("支付失败:网络连接错误");
                        break;
                    case Alipay.ERROR_PAY:
                        Tst.showToast("支付错误:支付码支付失败");
                        break;
                    default:
                        Tst.showToast("支付错误");
                        break;
                }
            }
            @Override
            public void onCancel() {
                Tst.showToast("支付取消");
            }
        }).doPay();
    }
}
