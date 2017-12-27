package com.knms.net;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.knms.app.KnmsApp;
import com.knms.core.upgrade.UpdateHelper;
import com.knms.util.L;
import com.knms.util.SPUtils;
import com.knms.util.SystemInfo;
import com.knms.util.ToolsHelper;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.knms.util.SPUtils.getFromApp;

/**
 * 类说明：
 *
 * @author 作者:tdx
 * @version 版本:1.0
 * @date 时间:2016年8月26日 上午10:59:07
 */
public class RxRequestApi {
    private OkHttpClient okHttpClient;
    private Retrofit retrofit;
    private Retrofit retrofitOther;
    private Gson gson;

    private RxApiService apiService;
    private RxOtherApiService apiOtherService;

    public static RxRequestApi getInstance() {
        return InstanceHolder.instance;
    }
    static class InstanceHolder {
        final static RxRequestApi instance = new RxRequestApi();
    }
    private RxRequestApi() {
        gson = new GsonBuilder().registerTypeAdapter(boolean.class, new TypeAdapter<Boolean>() {
            @Override
            public void write(JsonWriter out, Boolean value) throws IOException {
                if (value == null) {
                    out.nullValue();
                } else {
                    out.value(value);
                }
            }

            @Override
            public Boolean read(JsonReader in) throws IOException {
                JsonToken peek = in.peek();
                switch (peek) {
                    case BOOLEAN:
                        return in.nextBoolean();
                    case NULL:
                        in.nextNull();
                        return null;
                    case NUMBER:
                        return in.nextInt() != 0;
                    case STRING:
                        return Boolean.parseBoolean(in.nextString());
                    default:
                        throw new IllegalStateException("Expected BOOLEAN or NUMBER but was " + peek);
                }
            }
        }).create();

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(HttpConstant.SOCKET_TIMEOUT, TimeUnit.MILLISECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
//                .cache(new Cache(SDCardUtils.getCacheDirFile(KnmsApp.getInstance()),100 * 1024 * 1024))
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request original = chain.request();
                        String knmsid = (String) getFromApp(SPUtils.KeyConstant.knmsid, "");
                        String k_ex = (String) SPUtils.getFromApp(SPUtils.KeyConstant.k_ex, "");
                        String k_cc = "";
                        if (!TextUtils.isEmpty(k_ex)) k_cc = ToolsHelper.getInstance().acckey(k_ex);
                        // Request customization: add request headers
                        Request.Builder requestBuilder = original.newBuilder()
                                .addHeader("k_wf", SystemInfo.getNetworkState() + "")//用户网络类型
                                .addHeader("k_ci", SystemInfo.getVerSerCode())//客户端版本标识
                                .addHeader("k_iv", SystemInfo.getRelease())//系统版本
                                .addHeader("k_ih", android.os.Build.MODEL)//固件型号
                                .addHeader("Cookie", knmsid);//增加接口唯一标识码
                        if (!TextUtils.isEmpty(k_cc)) requestBuilder.addHeader("k_cc", k_cc);
                        Request request = requestBuilder.build();
                        return chain.proceed(request);
                    }
                })
                .addNetworkInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        if(KnmsApp.getInstance().currentActivity() != null){
                            UpdateHelper updateHelper = new UpdateHelper.Builder(KnmsApp.getInstance().currentActivity())
                                    .isAutoInstall(false)
                                    .isThinkTime(true)
                                    .build();
                            updateHelper.check();
                        }
                        Response originalResponse = chain.proceed(chain.request());
                        //Set-Cookie: knmsid=1b9ce2469fc249f1acb7f3cae7c75740e9ae68805ec04a23afdcb8f7658b3b1b; Path=/
                        if (!TextUtils.isEmpty(originalResponse.header("Set-Cookie"))) {
                            String value = originalResponse.header("Set-Cookie");
                            if (!TextUtils.isEmpty(value) && value.contains("knmsid") && value.contains(";")) {
                                String knmsid = value.split(";")[0];
                                if (!TextUtils.isEmpty(knmsid))
                                    SPUtils.saveToApp(SPUtils.KeyConstant.knmsid, knmsid);
                            }
                        }
                        if (!TextUtils.isEmpty(originalResponse.header("k_ex"))) {
                            String k_ex = originalResponse.header("k_ex");
                            if (!TextUtils.isEmpty(k_ex)) {
                                SPUtils.saveToApp(SPUtils.KeyConstant.k_ex, k_ex);
                            }
                        }
                        return originalResponse;
                    }
                })
                .addNetworkInterceptor(new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                    @Override
                    public void log(String message) {
                        L.i_http(message);
                    }
                }).setLevel(HttpLoggingInterceptor.Level.BODY));
        okHttpClient = builder.build();
    }

    public RxApiService getApiService() {
        if (apiService == null) {
            apiService = getRetrofit().create(RxApiService.class);
        }
        return apiService;
    }

    public RxOtherApiService getOtherApiService() {
        if (apiOtherService == null) {
            apiOtherService = getOtherRetrofit().create(RxOtherApiService.class);
        }
        return apiOtherService;
    }

    private Retrofit getRetrofit() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(HttpConstant.HOST)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .client(okHttpClient)
                    .build();
        }
        return retrofit;
    }
    private Retrofit getOtherRetrofit() {
        if (retrofitOther == null) {
            retrofitOther = new Retrofit.Builder()
                    .baseUrl(HttpConstant.HOSTPAY)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .client(okHttpClient)
                    .build();
        }
        return retrofitOther;
    }
}
