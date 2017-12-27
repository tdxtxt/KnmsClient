package com.knms.net.uploadfile;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.knms.net.HttpConstant;
import com.knms.net.RxApiService;
import com.knms.net.RxRequestApi;
import com.knms.util.L;
import com.knms.util.SPUtils;
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

/**
 * Created by tdx on 2016/9/6.
 */
public class RxUploadApi {
    /**连接超时**/
    public static final int CONNECTION_TIME_OUT = 1*60;//1分钟
    private static RxUploadApi instance;
    private OkHttpClient okHttpClient;
    private Retrofit retrofit;
    private Gson gson;
    private ApiService apiService;

    public static RxUploadApi getInstance() {
        if (instance == null) {
            synchronized (RxUploadApi.class) {
                if (instance == null) {
                    instance = new RxUploadApi();
                }
            }
        }
        return instance;
    }
    private RxUploadApi() {
        gson = new Gson();
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(HttpConstant.SOCKET_TIMEOUT, TimeUnit.MILLISECONDS)
                .writeTimeout(CONNECTION_TIME_OUT, TimeUnit.SECONDS)
                .readTimeout(CONNECTION_TIME_OUT, TimeUnit.SECONDS);
        builder.addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request original = chain.request();
                        String k_ex = (String) SPUtils.getFromApp(SPUtils.KeyConstant.k_ex,"");
                        String k_cc = ToolsHelper.getInstance().acckey(k_ex);
                        Request.Builder requestBuilder = original.newBuilder()
                                .addHeader("k_cc",k_cc);
                        Request request = requestBuilder.build();
                        return chain.proceed(request);
                    }
                })
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Response originalResponse = chain.proceed(chain.request());
                        if(!TextUtils.isEmpty(originalResponse.header("k_ex"))){
                            String k_ex = originalResponse.header("k_ex");
                            if(TextUtils.isEmpty(k_ex)){
                                SPUtils.saveToApp(SPUtils.KeyConstant.k_ex, k_ex);
                            }
                        }
                        return originalResponse;
                    }
                })
                .addInterceptor(new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                L.i_http(message);
            }
        }).setLevel(HttpLoggingInterceptor.Level.BODY));
        okHttpClient = builder.build();
    }
    protected Retrofit getRetrofit() {
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
    public ApiService getApiService() {
        if (apiService == null) {
            apiService = getRetrofit().create(ApiService.class);
        }
        return apiService;
    }
}
