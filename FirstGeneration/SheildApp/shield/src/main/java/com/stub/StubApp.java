package com.stub;


import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;

//import com.lody.turbodex.TurboDex;
import com.qihoo.util.MyContext;
import com.qihoo.util.NativeUtil;
import com.qihoo.util.SpUtil;
import com.qihoo.util.StructSetUtil;
import com.qihoo.util.shareutil.RefinvokeMethod;
import com.qihoo.util.shareutil.ShareConstants;
import com.qihoo.util.shareutil.ShareFileLockHelper;
import com.qihoo.util.shareutil.ShareTinkerInternals;
//import com.taobao.android.dex.interpret.ARTUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author hzh
 **/
public class StubApp extends Application {
    public static final String TAG = "StubApp";
    private static Context mContext;
    private static final String INTERPRET_LOCK_FILE_NAME = "interpret.lock";

    static {
        System.loadLibrary("native-lib");
    }

    long originalStart = System.currentTimeMillis();

    /**
     * //TODO turbodex 方式
     * //        if(isArtMode())
     * //        {
     * //           boolean enable = TurboDex.enableTurboDex();
     * //            System.out.println(" StubApp 是否为art mode "+ isArtMode());
     * //            System.out.println(" StubApp 是否 enable dex "+ enable);
     * //        }
     * //TODO runtime 方式
     * //        int targetSdkVersion = base.getApplicationInfo().targetSdkVersion;
     * //        int i = NativeUtil.unsealNative(targetSdkVersion);
     * //        Log.w(TAG, "unsealNative : " + i);
     * //TODO artlas
     * //        ARTUtils.init(base);
     * //        ARTUtils.setIsDex2oatEnabled(false);
     * //        Log.w(TAG, "ARTUtils.isDex2oatEnabled(): " + ARTUtils.isDex2oatEnabled());
     *
     * @param base
     */
    @Override
    protected void attachBaseContext(Context base) {
        mContext = base;

        //TODO 仿 atlas 方式
        String str = NativeUtil.stringFromJNI(StructSetUtil.getCurrentInstructionSet(), false);
        Log.w(TAG, "stringFromJNI : " + str);


        super.attachBaseContext(new MyContext(base));

    }


    @Override
    public void onCreate() {
        super.onCreate();
        //1. 获取 application name
        String applicationName = "";
        ApplicationInfo ai;
        try {
            ai = this.getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            applicationName = ai.metaData.getString("ApplicationName");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (TextUtils.isEmpty(applicationName))
            return;

        Object ActivityThreadObj = RefinvokeMethod.invokeStaticMethod("android.app.ActivityThread",
                "currentActivityThread", new Class[]{}, new Object[]{});
        Object mBoundApplication = RefinvokeMethod.getField("android.app.ActivityThread",
                ActivityThreadObj, "mBoundApplication");
        Object info = RefinvokeMethod.getField("android.app.ActivityThread$AppBindData",
                mBoundApplication, "info");

        RefinvokeMethod.setField("android.app.LoadedApk", "mApplication", info, null);
        Object minitApplication = RefinvokeMethod.getField("android.app.ActivityThread",
                ActivityThreadObj, "mInitialApplication");
        ArrayList<Application> mAllApplications = (ArrayList<Application>) RefinvokeMethod.getField("android.app.ActivityThread",
                ActivityThreadObj, "mAllApplications");

        mAllApplications.remove(minitApplication);
        ApplicationInfo mApplicationInfo = (ApplicationInfo) RefinvokeMethod.getField("android.app.LoadedApk",
                info, "mApplicationInfo");
        ApplicationInfo appInfo = (ApplicationInfo) RefinvokeMethod.getField("android.app.ActivityThread$AppBindData",
                mBoundApplication, "appInfo");

        mApplicationInfo.className = applicationName;
        appInfo.className = applicationName;

        Application appplication = (Application) RefinvokeMethod.invokeMethod("android.app.LoadedApk",
                "makeApplication", info, new Class[]{boolean.class, Instrumentation.class}, new Object[]{false, null});
        RefinvokeMethod.setField("android.app.ActivityThread", "mInitialApplication", ActivityThreadObj,
                appplication);

        ArrayMap mProviderMap = (ArrayMap) RefinvokeMethod.getField("android.app.ActivityThread", ActivityThreadObj,
                "mProviderMap");

        for (Object mProviderClientRecord : mProviderMap.values()) {
            Object mLocalProvider = RefinvokeMethod.getField("android.app.ActivityThread$ProviderClientRecord",
                    mProviderClientRecord, "mLocalProvider");
            RefinvokeMethod.setField("android.content.ContentProvider", "mContext", mLocalProvider, appplication);
        }

        appplication.onCreate();


        System.out.println("走完onCreate 总耗时 " + (System.currentTimeMillis() - originalStart));

        if (Build.VERSION.SDK_INT < 29) {
            //主要android 10 以下进行手动触发dex2oat
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    new Dex2oatConsumer().consumeDex2oat();
                }
            }, 1000);

        }
    }

    /**
     * 开启子线程开启dex2oat
     */
    private void enableDex2oat() {

        String str = NativeUtil.stringFromJNI(StructSetUtil.getCurrentInstructionSet(), true);
        Log.w(TAG, "stringFromJNI : " + str);


        boolean aBoolean = SpUtil.getInstace().getBoolean(SpUtil.NAME2, false);
        if (aBoolean) return;
        File app = new File(getDir("tmp_apk", MODE_PRIVATE), "app");


        File odir = new File(app, "oat");
        if (!odir.exists())
            odir.mkdir();
        final String targetISA = ShareTinkerInternals.getCurrentInstructionSet();
        for (File file : app.listFiles()) {
            try {
                String fileName = file.getName();
                int index = fileName.lastIndexOf('.');
                if (index > 0) {
                    fileName = fileName.substring(0, index);
                }

                String result = file.getParentFile().getAbsolutePath() + "/oat/"
                        + targetISA + "/" + fileName + ShareConstants.ODEX_SUFFIX;
                interpretDex2Oat(file.getAbsolutePath(), result, targetISA);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    // 获取全局上下文
    public static Context getContext() {
        return mContext;
    }


    private static void interpretDex2Oat(String dexFilePath, String oatFilePath, String targetISA) throws Exception {
        // add process lock for interpret mode
        final File oatFile = new File(oatFilePath);
        if (!oatFile.exists()) {
            oatFile.getParentFile().mkdirs();
        }

        File lockFile = new File(new File(dexFilePath).getParentFile(), INTERPRET_LOCK_FILE_NAME);
        ShareFileLockHelper fileLock = null;
        try {
            fileLock = ShareFileLockHelper.getFileLock(lockFile);

            final List<String> commandAndParams = new ArrayList<>();
            commandAndParams.add("dex2oat");
            // for 7.1.1, duplicate class fix
            if (Build.VERSION.SDK_INT >= 24) {
                commandAndParams.add("--runtime-arg");
                commandAndParams.add("-classpath");
                commandAndParams.add("--runtime-arg");
                commandAndParams.add("&");
            }
            commandAndParams.add("--dex-file=" + dexFilePath);
            commandAndParams.add("--oat-file=" + oatFilePath);
            commandAndParams.add("--instruction-set=" + targetISA);
            if (Build.VERSION.SDK_INT > 25) {
                commandAndParams.add("--compiler-filter=quicken");
            } else {
                commandAndParams.add("--compiler-filter=interpret-only");
            }

            final ProcessBuilder pb = new ProcessBuilder(commandAndParams);
            pb.redirectErrorStream(true);
            final Process dex2oatProcess = pb.start();
            StreamConsumer.consumeInputStream(dex2oatProcess.getInputStream());
            StreamConsumer.consumeInputStream(dex2oatProcess.getErrorStream());
            try {
                final int ret = dex2oatProcess.waitFor();
                if (ret != 0) {
                    throw new IOException("dex2oat works unsuccessfully, exit code: " + ret);
                }
            } catch (InterruptedException e) {
                throw new IOException("dex2oat is interrupted, msg: " + e.getMessage(), e);
            }
        } finally {
            try {
                if (fileLock != null) {
                    fileLock.close();
                }
            } catch (IOException e) {
                Log.w(TAG, "release interpret Lock error", e);
            }
            SpUtil.getInstace().save(SpUtil.NAME2, true);
        }
    }

    private static class StreamConsumer {
        static final Executor STREAM_CONSUMER = Executors.newSingleThreadExecutor();

        static void consumeInputStream(final InputStream is) {
            STREAM_CONSUMER.execute(new Runnable() {
                @Override
                public void run() {
                    if (is == null) {
                        return;
                    }
                    final byte[] buffer = new byte[256];
                    try {
                        while ((is.read(buffer)) > 0) {
                            // To satisfy checkstyle rules.
                        }
                    } catch (IOException ignored) {
                        // Ignored.
                    } finally {
                        try {
                            is.close();
                        } catch (Exception ignored) {
                            // Ignored.
                        }
                    }
                }
            });
        }
    }


    private class Dex2oatConsumer {
        final Executor STREAM_CONSUMER = Executors.newSingleThreadExecutor();

        void consumeDex2oat() {
            STREAM_CONSUMER.execute(new Runnable() {
                @Override
                public void run() {
                    enableDex2oat();
                    SpUtil.getInstace().save(SpUtil.NAME2, true);
                }
            });
        }
    }

}
