package com.knms.net;

import android.text.TextUtils;
import com.google.gson.Gson;
import com.knms.bean.ResponseBody;
import com.knms.oncall.AsyncHttpCallBack;
import com.knms.util.L;
import com.knms.util.SPUtils;
import com.knms.util.SystemInfo;
import com.knms.util.ToolsHelper;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.apache.http.Header;
import java.util.Map;

/**
 * 类说明：
 *
 * @author 作者:tdx
 * @version 版本:1.0
 * @date 时间:2016年8月23日 上午8:59:07
 */
public class ReqApi {
    private static ReqApi reqApi;
    private AsyncHttpClient httpClient;

    private ReqApi() {
        httpClient = new AsyncHttpClient();// 创建异步请求的客户端对象
        httpClient.setTimeout(HttpConstant.SOCKET_TIMEOUT);
    }

    public static ReqApi getInstance() {
        if (reqApi == null) {
            synchronized (ReqApi.class) {
                reqApi = new ReqApi();
            }
        }
        return reqApi;
    }

    public void postMethod(String url, Map<String, Object> params, final AsyncHttpCallBack callBack) {
        try {
            RequestParams reqParams = new RequestParams();
            if (params != null) {
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    reqParams.put(entry.getKey(), entry.getValue());
                }
            }
            String knmsid = (String) SPUtils.getFromApp(SPUtils.KeyConstant.knmsid, "");
            if (!TextUtils.isEmpty(knmsid)) {
                httpClient.addHeader("Cookie", knmsid);
            }
            String k_ex = (String) SPUtils.getFromApp(SPUtils.KeyConstant.k_ex,"");
            String k_cc = "";
            if(!TextUtils.isEmpty(k_ex)) k_cc = ToolsHelper.getInstance().acckey(k_ex);
            if(!TextUtils.isEmpty(k_cc)) httpClient.addHeader("k_cc",k_cc);

            httpClient.addHeader("k_wf", SystemInfo.getNetworkState() + "");
            httpClient.addHeader("k_ci", SystemInfo.getVerSerCode());
            httpClient.addHeader("k_iv", SystemInfo.getRelease());
            httpClient.addHeader("k_ih", android.os.Build.MODEL);

            httpClient.post(url, reqParams, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    if (headers != null && headers.length > 0) {
                        for (Header header : headers) {
                            if ("Set-Cookie".equals(header.getName())) {
                                if (!TextUtils.isEmpty(header.getValue()) && header.getValue().contains("knmsid")) {
                                    String value = header.getValue();
                                    if(!TextUtils.isEmpty(value) && value.contains(";")){
                                        String knmsid = value.split(";")[0];
                                        if(!TextUtils.isEmpty(knmsid)) SPUtils.saveToApp(SPUtils.KeyConstant.knmsid, knmsid);
                                    }
                                }
                            }else if("k_ex".equals(header.getName())){
                                String k_ex = header.getValue();
                                if(!TextUtils.isEmpty(k_ex)){
                                    SPUtils.saveToApp(SPUtils.KeyConstant.k_ex, k_ex);
                                }
                            }
                        }
                    }
                    String result = new String(responseBody);
                    L.i_http("response:" + result);
                    ResponseBody body = new Gson().fromJson(result, callBack.setType());
                    callBack.onSuccess(body);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    error.printStackTrace();
                    callBack.onFailure(TextUtils.isEmpty(error.getMessage()) ? error.toString() : error.getMessage());
                }
            });
            L.i_http("url:" + url + "【params:" + reqParams.toString()+"】");
        } catch (Exception e) {
            callBack.onFailure(TextUtils.isEmpty(e.getMessage()) ? e.toString() : e.getMessage());
        }
    }
}
