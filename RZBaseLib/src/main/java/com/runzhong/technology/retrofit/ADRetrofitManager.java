package com.runzhong.technology.retrofit;


import com.runzhong.technology.util.RZConst;
import com.runzhong.technology.util.RZUtil;

import org.json.JSONObject;

import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by CN on 2018-3-13.
 */

public class ADRetrofitManager {
    private static long TIMEOUT = RZConst.TIME_OUT;
    private static ADRetrofitManager INSTANCE;
    private static ADHttpRequestRetrofitService ADHttpRequestRetrofitService;
    private static Retrofit retrofit;
    private static Map<String, List<Disposable>> disposableMap;
    private OkHttpClient okHttpClient;

    public static final int SUCCESS_CODE = 200;
    public static final int ERROR_EXCEPTION = 1000;
    public static final int ERROR_NET_FAILED = 1001;

    private ADRetrofitManager() {
        disposableMap = new HashMap<>();
        initRetrofit();
    }

    public static ADRetrofitManager getInstance() {
        if (INSTANCE == null) {
            synchronized (ADRetrofitManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ADRetrofitManager();
                }
            }
        }
        return INSTANCE;
    }

    private void initRetrofit() {
        if (retrofit == null) {
            synchronized (ADRetrofitManager.class) {
                if (retrofit == null) {
                    retrofit = new Retrofit.Builder()
                            .client(getOkInstance())//设置 HTTP Client  用于请求的连接
                            .addConverterFactory(ScalarsConverterFactory.create())   //字符串转换器
                            .addConverterFactory(GsonConverterFactory.create())//json转换器
                            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())//把 Retrofit 转成RxJava可用的适配类
                            .baseUrl("https://switch.api.xiangmaikeji.com/")//请求的父目录
                            .build();
                }
            }
        }
    }

    public static void setTIMEOUT(long TIMEOUT) {
        ADRetrofitManager.TIMEOUT = TIMEOUT;
    }

    public ADHttpRequestRetrofitService getADApiService() {
        if (ADHttpRequestRetrofitService == null) {
            ADHttpRequestRetrofitService = retrofit.create(ADHttpRequestRetrofitService.class);
        }
        return ADHttpRequestRetrofitService;
    }
    public void request(Observable observable, String tag, ADRetrofitResponseListener responseListener) {
        request(observable, tag, responseListener,  true);
    }

    public void request(Observable observable, final String tag, final ADRetrofitResponseListener responseListener, final boolean
            isNeedAnalyze) {
//                observable.subscribeOn(Schedulers.newThread())
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<String>() {
            private Disposable disposable;

            @Override
            public void onSubscribe(Disposable d) {
                disposable = d;
                if (tag != null) {
                    List<Disposable> disposableList = disposableMap.get(tag);
                    if (disposableList == null) {
                        disposableList = new ArrayList<>();
                    }
                    disposableList.add(disposable);
                    disposableMap.put(tag, disposableList);
                }
            }

            @Override
            public void onNext(String result) {
                RZUtil.log("response:"+result);
                try {
                    if (isNeedAnalyze) {
                        JSONObject jsonResult = new JSONObject(result);
                        int errorCode = jsonResult.getInt("statusCode");
                        if (SUCCESS_CODE == errorCode) {
                            if (responseListener != null) {
                                responseListener.onSuccess(jsonResult.getString("data"));
                            }
                        } else {
                            if (responseListener != null) {
                                String errorMsg = jsonResult.getString("errorMsg");
                                responseListener.onWebServiceError(errorCode, errorMsg);
                            }
                        }
                    } else {
                        if (responseListener != null) {
                            responseListener.onSuccess(result);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (responseListener != null) {
                        responseListener.onNetError(ERROR_EXCEPTION, "code exception");
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                if (responseListener != null) {
                    responseListener.onNetError(ERROR_NET_FAILED, e.getMessage());
                }
                if (disposable != null && !disposable.isDisposed()) {
                    disposable.dispose();
                }
                if (tag != null) {
                    List<Disposable> disposableList = disposableMap.get(tag);
                    if (disposableList != null) {
                        disposableList.remove(disposable);
                    }
                }
            }

            @Override
            public void onComplete() {
                if (disposable != null && !disposable.isDisposed()) {
                    disposable.dispose();
                }
                if (tag != null) {
                    List<Disposable> disposableList = disposableMap.get(tag);
                    if (disposableList != null) {
                        disposableList.remove(disposable);
                    }
                }
            }
        });
    }

    //撤销请求响应
    public void cancelDisposable(String tag) {
        if (tag != null) {
            List<Disposable> disposableList = disposableMap.get(tag);
            if (disposableList!=null) {
                for (Disposable disposable : disposableList) {
                    if (disposable != null && !disposable.isDisposed()) {
                        disposable.dispose();
                    }
                }
                disposableList.clear();
                disposableMap.remove(tag);
            }
        }
    }

    //撤销请求响应
    public void cancelAllDisposable() {
        if (!disposableMap.isEmpty()) {
            Iterator<String> iterator = disposableMap.keySet().iterator();
            if (iterator.hasNext()) {
                cancelDisposable(iterator.next());
            }
        }
        disposableMap.clear();
    }
    public OkHttpClient getOkInstance() {
        if (okHttpClient == null) {
            synchronized (OkHttpClient.class) {
                if (okHttpClient == null) {
                    try {
                        final TrustManager[] trustAllCerts = new TrustManager[] {
                                new X509TrustManager() {
                                    @Override
                                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                                    }
                                    @Override
                                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                                    }
                                    @Override
                                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                        return new java.security.cert.X509Certificate[]{};
                                    }
                                }
                        };
                        // Install the all-trusting trust manager
                        final SSLContext sslContext = SSLContext.getInstance("SSL");
                        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                        final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
                        okHttpClient = new OkHttpClient.Builder().readTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                                .writeTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                                .connectTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                                .addInterceptor(new ADRequestInterceptor()).sslSocketFactory(sslSocketFactory).build();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if(okHttpClient == null){
                        okHttpClient = new OkHttpClient.Builder().readTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                                .writeTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                                .connectTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                                .addInterceptor(new ADRequestInterceptor()).build();
                    }
                }
            }
        }
        return okHttpClient;
    }
}
