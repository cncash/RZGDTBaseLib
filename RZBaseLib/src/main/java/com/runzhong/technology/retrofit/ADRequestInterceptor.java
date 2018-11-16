package com.runzhong.technology.retrofit;



import android.util.Log;

import com.runzhong.technology.RZManager;
import com.runzhong.technology.util.MD5Util;
import com.runzhong.technology.util.RZConst;
import com.runzhong.technology.util.RZUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import static com.runzhong.technology.util.RZConst.AD_PLATFORM_KEY;
import static com.runzhong.technology.util.RZConst.ERROR_CODE;
import static com.runzhong.technology.util.RZConst.ERROR_MSG;

/**
 * Created by CN on 2018-3-13.
 */

public class ADRequestInterceptor implements Interceptor{
    public static final String APP_ID = "0yfoZsFJJk7PeFwZ";
    private static final String APP_KEY = "AiPCKjWxSYCVJw9WS3kOqVuC8gZ7LFBq";
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        HttpUrl originalHttpUrl = original.url();
        final String time = String.valueOf(System.currentTimeMillis() / 1000);
        HttpUrl.Builder builderHttp = originalHttpUrl.newBuilder()
                .addQueryParameter("appTime",time)
                .addQueryParameter("appId", APP_ID)
                .addQueryParameter("appSign", MD5Util.getMD5String(MD5Util.encryptToSHA(time + APP_KEY  +
                        time)+APP_ID));
        HttpUrl url = builderHttp.build();
        Request.Builder requestBuilder = original.newBuilder()
                .url(url);

        Request request = requestBuilder.build();
        if("POST".equals(request.method())){
            StringBuilder sb = new StringBuilder();
            if (request.body() instanceof FormBody) {
                FormBody body = (FormBody) request.body();
                for (int i = 0; i < body.size(); i++) {
                    sb.append(body.encodedName(i) + "=" + body.encodedValue(i) + ",");
                }
                sb.delete(sb.length() - 1, sb.length());
            }
            RZUtil.log("postParams:"+sb);
        }else{
            RZUtil.log("getParams:"+originalHttpUrl.query());
        }
        RZManager.getInstance().onUMEvent(RZConst.AD_SERVICE_API_REQUEST_EVENT_ID);
        return chain.proceed(request);
    }
}
