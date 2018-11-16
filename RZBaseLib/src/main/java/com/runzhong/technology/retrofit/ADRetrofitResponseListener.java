package com.runzhong.technology.retrofit;

/**
 * Created by CN on 2018-3-13.
 */

public interface ADRetrofitResponseListener {
    void onSuccess(String data);

    void onNetError(int errorCode, String errorMsg);

    void onWebServiceError(int errorCode, String errorMsg);

    abstract class SimpleADRetrofitResponseListener implements ADRetrofitResponseListener {
        public void onWebServiceError(int errorCode, String errorMsg) {
            onNetError(errorCode, errorMsg);
        }
    }
}
