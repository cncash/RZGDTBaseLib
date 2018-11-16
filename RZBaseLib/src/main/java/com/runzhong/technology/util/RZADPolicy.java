package com.runzhong.technology.util;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.runzhong.technology.bean.ADPlatform;

import java.util.List;

import static com.runzhong.technology.util.RZConst.AD_LOAD_POLICY;
import static com.runzhong.technology.util.RZConst.AD_LOAD_SPLASH_POLICY;

/**
 * Created by CN.
 */

public class RZADPolicy {
    private Context context;
    private List<ADPlatform> adPlatformList;
    public RZADPolicy(Context context,OnADLoadListener onADLoadListener){
        this.context = context;
        this.onADLoadListener = onADLoadListener;
    }
    public void loadADPolicy() {
        RZXmlUtil rzXmlUtil = new RZXmlUtil(context, AD_LOAD_POLICY);
        String data = rzXmlUtil.getString(AD_LOAD_SPLASH_POLICY);
        if (!TextUtils.isEmpty(data)) {
            try {
                adPlatformList = new Gson().fromJson(data, new TypeToken<List<ADPlatform>>() {
                }.getType());
                onFetchAdSuccess();
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(onADLoadListener!=null){
            onADLoadListener.onLoadData();
        }
    }
    private void onFetchAdSuccess() {
        if (adPlatformList != null && adPlatformList.size() > 0) {
            try {
                ADPlatform adPlatform = adPlatformList.get(0);
                if (adPlatform.isAdIsOpen()) {
                    fetchPlatformAd(adPlatform);
                } else {
                    if(onADLoadListener!=null){
                        onADLoadListener.onLoadData();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if(onADLoadListener!=null){
                    onADLoadListener.onLoadData();
                }
            }
        } else {
            if(onADLoadListener!=null){
                onADLoadListener.onLoadData();
            }
        }
    }

    public void fetchNextPlatformAd() {
        if (adPlatformList != null && adPlatformList.size() > 0) {
            fetchPlatformAd(adPlatformList.get(0));
        } else {
            if(onADLoadListener!=null){
                onADLoadListener.onLoadData();
            }
        }
    }

    private void fetchPlatformAd(ADPlatform adPlatform) {
        adPlatformList.remove(adPlatform);
        switch (adPlatform.getAdSupportType()) {
            case RZConst.AD_PLATFORM_GDT:
                if(onADLoadListener!=null){
                    onADLoadListener.onLoadGDTAD();
                }
                break;
            default:
                if(onADLoadListener!=null){
                    onADLoadListener.onLoadData();
                }
        }
    }
    private OnADLoadListener onADLoadListener;

    public RZADPolicy.OnADLoadListener getOnADLoadListener() {
        return onADLoadListener;
    }

    public void setOnADLoadListener(RZADPolicy.OnADLoadListener onADLoadListener) {
        this.onADLoadListener = onADLoadListener;
    }

    public interface OnADLoadListener{
        void onLoadGDTAD();
        void onLoadData();
    }
}
