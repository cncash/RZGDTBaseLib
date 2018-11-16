package com.runzhong.technology.util;

import android.content.Context;
import android.util.Log;

import com.runzhong.technology.RZManager;

/**
 * Created by CN.
 */

public class RZUtil {
    public static void log(String msg){
        if(RZManager.getInstance().isDebugMode()){
            Log.v("CN","=======================");
            Log.v("CN",msg);
        }
    }
    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
