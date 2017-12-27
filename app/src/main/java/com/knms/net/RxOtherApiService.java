package com.knms.net;

import com.knms.bean.ResponseBody;
import com.knms.bean.other.TipNum;
import com.knms.bean.product.Furniture;

import java.util.List;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;

/**
 * Created by Administrator on 2017/6/5.
 */

public interface RxOtherApiService {
    //用户获取小红点
    @FormUrlEncoded
    @POST("redspot")
    Observable<ResponseBody<TipNum>> getRedspot(@Field("userid") String userid);
}
