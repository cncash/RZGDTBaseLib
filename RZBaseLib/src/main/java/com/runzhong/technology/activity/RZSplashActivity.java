package com.runzhong.technology.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qfxl.view.RoundProgressBar;
import com.qq.e.ads.splash.SplashAD;
import com.qq.e.ads.splash.SplashADListener;
import com.qq.e.comm.util.AdError;
import com.runzhong.technology.R;
import com.runzhong.technology.RZManager;
import com.runzhong.technology.bean.ADPlatform;
import com.runzhong.technology.util.ArithUtil;
import com.runzhong.technology.util.RZConst;
import com.runzhong.technology.util.RZUtil;

import java.util.List;


/**
 * Created by CN on 2017-11-28.
 */

public abstract class RZSplashActivity extends Activity implements RZManager.OnSplashADLoadListener{
    protected RoundProgressBar cutDownBar;
    private View rltSkip;
    private FrameLayout splash_container;
    private final int SPLASH_SHOW_TIME_NORMAL = 3000;
    private final int AD_SHOW_TIME = 5000;
    private final int FETCH_DELAY = RZConst.TIME_OUT;
    private long beginTime;
    private List<ADPlatform> adPlatformList;

    protected RelativeLayout rltSplashBg;
    protected RelativeLayout rltBottom;
    protected ImageView imgApp;
    protected TextView txtAppName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 解决初次安装后打开后按home返回后重新打开重启问题。。。
        if (!this.isTaskRoot()) { //判断该Activity是不是任务空间的源Activity，“非”也就是说是被系统重新实例化出来
            //如果你就放在launcher Activity中话，这里可以直接return了
            Intent mainIntent = getIntent();
            String action = mainIntent.getAction();
            if (mainIntent.hasCategory(Intent.CATEGORY_LAUNCHER) && action.equals(Intent.ACTION_MAIN)) {
                finish();
                return;//finish()之后该活动会继续执行后面的代码，你可以logCat验证，加return避免可能的exception
            }
        }
        setContentView(R.layout.activity_rz_splash);
        bindViews();
        rltSplashBg.setBackgroundResource(getSplashRes());
        initCutDown();
        onCreated(savedInstanceState);
    }
    private void bindViews() {
        rltSplashBg = findViewById(R.id.rltSplashBg);
        rltSkip = findViewById(R.id.rltSkip);
        splash_container = findViewById(R.id.splash_container);
        rltBottom = findViewById(R.id.rltBottom);
        imgApp = findViewById(R.id.imgApp);
        txtAppName = findViewById(R.id.txtAppName);
    }
    private void initCutDown() {
        cutDownBar = findViewById(R.id.skip_view);
        cutDownBar.setCenterText(String.valueOf(AD_SHOW_TIME/1000));
        cutDownBar.setProgressChangeListener(new RoundProgressBar.ProgressChangeListener() {
            @Override
            public void onFinish() {
                cutDownBar.setCenterText(String.valueOf(0));
                next();
            }

            @Override
            public void onProgressChanged(int progress) {
                int time = (int) (AD_SHOW_TIME-AD_SHOW_TIME*ArithUtil.div(progress,100))/1000;
                time+=1;
                cutDownBar.setCenterText(String.valueOf(time));
            }
        });
    }

    private boolean isSetSkipListener;
    private void setSkipListener(){
        if(!isSetSkipListener){
            isSetSkipListener = true;
            rltSkip.setEnabled(true);
            rltSkip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toMain(0);
                }
            });
        }
    }
    public void fetchSplashAD() {
        beginTime = System.currentTimeMillis();
        RZManager.getInstance().getSplash(getChannel(), getPackageName(), getVersion(), this);
    }

    /**
     * 拉取开屏广告，开屏广告的构造方法有3种，详细说明请参考开发者文档。
     */
    private void fetchGDTSplashAD() {
        RZUtil.log("==========拉取广点通=========");
        new SplashAD(this, (ViewGroup) findViewById(R.id.splash_container), rltSkip, getGDTAppId(), getGDTSplashPositionId(), new SplashADListener() {
            @Override
            public void onADDismissed() {
            }
            @Override
            public void onNoAD(AdError adError) {
                RZUtil.log("广点通Error:" + adError.getErrorMsg());
                RZManager.getInstance().onUMEvent(RZConst.AD_THIRD_PLATFORM_ERROR_EVENT_ID,
                        RZManager.getInstance().getErrorEventMap(RZConst.AD_PLATFORM_GDT,
                                String.valueOf(adError.getErrorCode()), adError.getErrorMsg()));
                fetchNextPlatformAd();
            }
            @Override
            public void onADPresent() {
            }
            @Override
            public void onADClicked() {
            }
            @Override
            public void onADTick(long l) {
                showCutDown();
            }
            @Override
            public void onADExposure() {
            }
        }, 0);
    }
    private void showCutDown(){
        if(cutDownBar.getVisibility() == View.GONE) {
            cutDownBar.setVisibility(View.VISIBLE);
            cutDownBar.setCountDownTimeMillis(AD_SHOW_TIME);
            cutDownBar.start();
        }
    }
    private boolean canJump;

    /**
     * 设置一个变量来控制当前开屏页面是否可以跳转，当开屏广告为普链类广告时，点击会打开一个广告落地页，此时开发者还不能打开自己的App主页。当从广告落地页返回以后，
     * 才可以跳转到开发者自己的App主页；当开屏广告是App类广告时只会下载App。
     */
    private void next() {
        if (canJump) {
            toMain(0);
        } else {
            canJump = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        canJump = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (canJump) {
            next();
        }
        canJump = true;
    }

    private void toMainByDelay() {
        long delay = getDelayFinishTime();
        toMain(delay);
    }

    private long getDelayFinishTime() {
        long errorTime = System.currentTimeMillis();
        long remainderTime = SPLASH_SHOW_TIME_NORMAL - (errorTime - beginTime);
        return remainderTime > 0 ? remainderTime : 0;
    }

    @Override
    public void onFetchAdSuccess(List<ADPlatform> adPlatformList) {
        this.adPlatformList = adPlatformList;
        if (adPlatformList != null && adPlatformList.size() > 0) {
            try {
                ADPlatform adPlatform = adPlatformList.get(0);
                if (adPlatform.isAdIsOpen()) {
                    fetchPlatformAd(adPlatform);
                } else {
                    toMainByDelay();
                }
            } catch (Exception e) {
                e.printStackTrace();
                toMainByDelay();
            }
        } else {
            toMainByDelay();
        }
    }

    private void fetchNextPlatformAd() {
        if (adPlatformList != null && adPlatformList.size() > 0) {
            fetchPlatformAd(adPlatformList.get(0));
        } else {
            toMainByDelay();
        }
    }

    private void fetchPlatformAd(ADPlatform adPlatform) {
        adPlatformList.remove(adPlatform);
        switch (adPlatform.getAdSupportType()) {
            case RZConst.AD_PLATFORM_TT:
                fetchNextPlatformAd();
                break;
            case RZConst.AD_PLATFORM_GDT:
                fetchGDTSplashAD();
                break;
            default:
                toMainByDelay();
        }
    }

    @Override
    public void onFetchAdError() {
        toMainByDelay();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }

    public abstract void onCreated(Bundle savedInstanceState);

    public abstract void toMain(long delay);

    public abstract int getSplashRes();

    public abstract String getChannel();

    public abstract String getVersion();

    public abstract String getGDTAppId();

    public abstract String getGDTSplashPositionId();

    public abstract String getTTCodeId();

    public abstract String getTTAppId();

}
