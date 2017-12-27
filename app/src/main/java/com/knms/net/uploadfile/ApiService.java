package com.knms.net.uploadfile;

import com.knms.bean.ResponseBody;
import com.knms.bean.other.Pic;

import okhttp3.MultipartBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import rx.Observable;

/**
 * Created by Administrator on 2016/9/6.
 */
public interface ApiService {
    @Multipart //上传一张图片
    @POST("api/basis/imageupload")
    Observable<ResponseBody<Pic>> uploadImage(@Part MultipartBody.Part file);
}
