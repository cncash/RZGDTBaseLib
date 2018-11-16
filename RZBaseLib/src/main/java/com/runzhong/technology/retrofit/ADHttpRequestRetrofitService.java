package com.runzhong.technology.retrofit;



import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by CN on 2018-3-13.
 */

public interface ADHttpRequestRetrofitService {

    @POST("/SupportService/GetScreenSupports")
    @FormUrlEncoded
    Observable<String> getSplashAd(@Field("channel") String channel,@Field("packageName") String packageName,@Field("version") String version);
    @POST("/SupportService/GetSupports")
    @FormUrlEncoded
    Observable<String> getAd(@Field("channel") String channel,@Field("packageName") String packageName,@Field("version") String version,@Field("type") String type);

}
