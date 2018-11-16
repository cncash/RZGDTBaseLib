package com.runzhong.technology;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.runzhong.technology.bean.ADPlatform;
import com.runzhong.technology.retrofit.ADRetrofitManager;
import com.runzhong.technology.retrofit.ADRetrofitResponseListener;
import com.runzhong.technology.util.RZUtil;
import com.runzhong.technology.util.RZXmlUtil;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.runzhong.technology.retrofit.ADRetrofitManager.ERROR_NET_FAILED;
import static com.runzhong.technology.util.RZConst.AD_LOAD_POLICY;
import static com.runzhong.technology.util.RZConst.AD_LOAD_SPLASH_POLICY;
import static com.runzhong.technology.util.RZConst.AD_LOCAL_ERROR_EVENT_ID;
import static com.runzhong.technology.util.RZConst.AD_PLATFORM_KEY;
import static com.runzhong.technology.util.RZConst.AD_PLATFORM_LOCAL;
import static com.runzhong.technology.util.RZConst.AD_READ_LOCAL_EVENT_ID;
import static com.runzhong.technology.util.RZConst.ERROR_CODE;
import static com.runzhong.technology.util.RZConst.ERROR_MSG;
import static com.runzhong.technology.util.RZConst.TAG_SPLASH;

/**
 * Created by CN.
 */

public class RZManager {
    private static RZManager INSTANCE;
    private boolean isDebugMode;
    private Context mContext;
    private String appKeyUM;

    private RZXmlUtil rzXmlUtil;

    private int mLifecycleCount = 0;
    private long activityHideTime = 0;
    private static long INTERVAL_SPLASH_AD_SHOW = 5 * 60 * 1000;
    private OnSplashAdShowListener onSplashAdShowListener;

    private RZManager() {
    }

    public static RZManager getInstance() {
        if (INSTANCE == null) {
            synchronized (RZManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RZManager();
                }
            }
        }
        return INSTANCE;
    }

    public void init(Context mContext) {
        this.init(mContext, false);
    }

    public void init(Context mContext, boolean isDebugMode) {
        this.mContext = mContext;
        this.isDebugMode = isDebugMode;
        rzXmlUtil = new RZXmlUtil(mContext, AD_LOAD_POLICY);
    }

    public boolean isShowAd(){
        if(rzXmlUtil == null){
            rzXmlUtil = new RZXmlUtil(mContext, AD_LOAD_POLICY);
        }
        String data = rzXmlUtil.getString(AD_LOAD_SPLASH_POLICY);
        RZUtil.log("data:"+data);
        if (data != null && !data.equals("")) {
            try {
                List<ADPlatform> adPlatformList = new Gson().fromJson(data, new TypeToken<List<ADPlatform>>() {
                }.getType());
                if(adPlatformList!=null&&adPlatformList.size()>0){
                    return adPlatformList.get(0).isAdIsOpen();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    public void getSplash(String channel, String packageName, String version, final OnSplashADLoadListener onSplashADLoadListener) {
        ADRetrofitManager.getInstance().request(ADRetrofitManager.getInstance().getADApiService().getSplashAd(channel, packageName, version), TAG_SPLASH, new ADRetrofitResponseListener.SimpleADRetrofitResponseListener() {
            @Override
            public void onSuccess(String data) {
                rzXmlUtil.put(AD_LOAD_SPLASH_POLICY, data);
                if (onSplashADLoadListener != null) {
                    List<ADPlatform> adPlatformList = new Gson().fromJson(data, new TypeToken<List<ADPlatform>>() {
                    }.getType());
                    onSplashADLoadListener.onFetchAdSuccess(adPlatformList);
                }
            }

            @Override
            public void onNetError(int errorCode, String errorMsg) {
                onUMEvent(AD_LOCAL_ERROR_EVENT_ID, getErrorEventMap(AD_PLATFORM_LOCAL, String.valueOf(errorCode), errorMsg));
                RZUtil.log("errorCode:" + errorCode + "|errorMsg:" + errorMsg);
                if(errorCode == 4022044){
                    rzXmlUtil.remove(AD_LOAD_SPLASH_POLICY);
                }
                if (onSplashADLoadListener != null) {
                    if (errorCode == 502 || errorCode == ERROR_NET_FAILED) {
                        onUMEvent(AD_READ_LOCAL_EVENT_ID);
                        String data = rzXmlUtil.getString(AD_LOAD_SPLASH_POLICY);
                        if (data != null && !data.equals("")) {
                            try {
                                List<ADPlatform> adPlatformList = new Gson().fromJson(data, new TypeToken<List<ADPlatform>>() {
                                }.getType());
                                onSplashADLoadListener.onFetchAdSuccess(adPlatformList);
                                return;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    onSplashADLoadListener.onFetchAdError();
                }
            }
        });
    }

    public Map<String, String> getErrorEventMap(String platform, String errorCode, String errorMsg) {
        Map<String, String> params = new HashMap<>();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(ERROR_MSG,errorMsg);
            jsonObject.put(ERROR_CODE,errorCode);
            params.put(platform,jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        params.put(AD_PLATFORM_KEY, platform);
//        params.put(ERROR_MSG, errorMsg);
//        params.put(ERROR_CODE, errorCode);
        return params;
    }

    public interface OnSplashADLoadListener {
        void onFetchAdSuccess(List<ADPlatform> adPlatformList);

        void onFetchAdError();
    }

    public void cancelRequest(String tag) {
        if (tag != null) {
            ADRetrofitManager.getInstance().cancelDisposable(tag);
        } else {
            ADRetrofitManager.getInstance().cancelAllDisposable();
        }
    }

    public boolean isDebugMode() {
        return isDebugMode;
    }

    /**********************************************
     * 统计信息
     ***********************************************/
    public void initUMConfig(Context mContext, String appkey, String channel) {
        appKeyUM = appkey;
        UMConfigure.init(mContext, appkey, channel, UMConfigure.DEVICE_TYPE_PHONE, null);
        MobclickAgent.setScenarioType(mContext, MobclickAgent.EScenarioType.E_UM_NORMAL);
        MobclickAgent.setCatchUncaughtExceptions(true);
    }

    public void onUMResume(Context context) {
        if (isUMInit()) {
            MobclickAgent.onResume(context);
        }
    }

    public void onUMPause(Context context) {
        if (isUMInit()) {
            MobclickAgent.onPause(context);
        }
    }

    public void onUMPageStart(String viewName) {
        if (isUMInit()) {
            MobclickAgent.onPageStart(viewName);
        }
    }

    public void onUMPageEnd(String viewName) {
        if (isUMInit()) {
            MobclickAgent.onPageEnd(viewName);
        }
    }

    public void onUMEvent(String eventID) {
        if (isUMInit()) {
            MobclickAgent.onEvent(mContext, eventID);
        }
    }

    public void onUMEvent(String eventID, String label) {
        if (isUMInit()) {
            MobclickAgent.onEvent(mContext, eventID, label);
        }
    }

    public void onUMEvent(String eventID, Map<String, String> map) {
        if (isUMInit()) {
            MobclickAgent.onEvent(mContext, eventID, map);
        }
    }

    public void onUMEventValue(String eventID, Map<String, String> map, int du) {
        if (isUMInit()) {
            MobclickAgent.onEventValue(mContext, eventID, map, du);
        }
    }

    private boolean isUMInit() {
        if (appKeyUM == null) {
            throw new IllegalStateException("you doesn't init UMConfig!");
        }
        return true;
    }

    public void registerActivityLifecycle(Application application,OnSplashAdShowListener onSplashAdShowListener){
        this.onSplashAdShowListener = onSplashAdShowListener;
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            }
            @Override
            public void onActivityStarted(Activity activity) {
                mLifecycleCount++;
                //mLifecycleCount ==1，说明是从后台到前台
                if (mLifecycleCount == 1){
                    if(activityHideTime>0&&(System.currentTimeMillis()-activityHideTime)>INTERVAL_SPLASH_AD_SHOW){
                        if(RZManager.this.onSplashAdShowListener!=null){
                            RZManager.this.onSplashAdShowListener.onSplashAdShow();
                        }
                    }
                }
            }
            @Override
            public void onActivityResumed(Activity activity) {
            }
            @Override
            public void onActivityPaused(Activity activity) {
            }
            @Override
            public void onActivityStopped(Activity activity) {
                mLifecycleCount--;
                //如果mFinalCount ==0，说明是前台到后台
                if (mLifecycleCount == 0){
                    activityHideTime = System.currentTimeMillis();
                }
            }
            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }
            @Override
            public void onActivityDestroyed(Activity activity) {
            }
        });
    }

    public static void setIntervalSplashAd(long interval) {
        RZManager.INTERVAL_SPLASH_AD_SHOW = interval;
    }

    public boolean isAppForeground(){
        if(this.onSplashAdShowListener == null){
            throw new IllegalArgumentException("do not set onActivityLifecycleChangeListener yet!");
        }
        return mLifecycleCount == 1;
    }
    public interface OnSplashAdShowListener{
        void onSplashAdShow();
    }
}
