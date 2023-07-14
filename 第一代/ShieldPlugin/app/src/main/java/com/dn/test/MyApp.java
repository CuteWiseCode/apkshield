package com.dn.test;

import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.res.Configuration;

import androidx.annotation.NonNull;

/**
 * @Author: hzh
 * @Date: 2022/12/8
 * @Desc:
 */
public class MyApp extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        System.out.println("主app attachBaseContext :"+getClassLoader());

    }



    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("主app onCreate");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        System.out.println("主app onTerminate");
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        System.out.println("主app onConfigurationChanged");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        System.out.println("主app onLowMemory");
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        System.out.println("主app onTrimMemory");
    }

    @Override
    public void registerComponentCallbacks(ComponentCallbacks callback) {
        super.registerComponentCallbacks(callback);
        System.out.println("主app registerComponentCallbacks");
    }

    @Override
    public void unregisterComponentCallbacks(ComponentCallbacks callback) {
        super.unregisterComponentCallbacks(callback);
        System.out.println("主app unregisterComponentCallbacks");
    }

    @Override
    public void registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks callback) {
        super.registerActivityLifecycleCallbacks(callback);
        System.out.println("主app registerActivityLifecycleCallbacks");
    }

    @Override
    public void unregisterActivityLifecycleCallbacks(ActivityLifecycleCallbacks callback) {
        super.unregisterActivityLifecycleCallbacks(callback);
        System.out.println("主app unregisterActivityLifecycleCallbacks");
    }

    @Override
    public void registerOnProvideAssistDataListener(OnProvideAssistDataListener callback) {
        super.registerOnProvideAssistDataListener(callback);
    }

    @Override
    public void unregisterOnProvideAssistDataListener(OnProvideAssistDataListener callback) {
        super.unregisterOnProvideAssistDataListener(callback);
    }
}
