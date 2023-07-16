package com.ck.plugin;


import com.ck.plugin.ext.Configuration;
import com.ck.plugin.ext.InputParam;
import com.ck.plugin.shield.ApkShielder;
import com.ck.plugin.utils.FileOperation;
import com.ck.plugin.utils.LogUtil;
import com.ck.plugin.utils.StringUtil;

import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;

/**
 * @author hzh
 */
public class Main {

    public static final int ERRNO_ERRORS = 1;
    public static final int ERRNO_USAGE = 2;
    protected static long mRawApkSize;
    protected static String mRunningLocation;
    protected static long mBeginTime;

    /**
     * 是否通过命令行方式设置
     **/
    public boolean mSetSignThroughCmd;
    public boolean mSetMappingThroughCmd;
    public String m7zipPath;
    public String mZipalignPath;
    public String mFinalApkBackPath;

    protected Configuration config;
    protected File mOutDir;


    public static void gradleRun(InputParam inputParam, Project project) {
        Main m = new Main();
        m.run(inputParam, project);
    }

    private void run(InputParam inputParam, Project project) {
        synchronized (Main.class) {
            LogUtil.log("run");
            loadConfigFromGradle(inputParam);
            LogUtil.log("loadConfigFromGradle");
            Thread currentThread = Thread.currentThread();
            System.out.printf(
                    "\n-->Shield dex starting! Current thread# id: %d, name: %s\n",
                    currentThread.getId(),
                    currentThread.getName()
            );
            File finalApkFile = StringUtil.isPresent(inputParam.finalApkBackupPath) ?
                    new File(inputParam.finalApkBackupPath)
                    : null;
            LogUtil.log("finalApkFile");
            shieldDex(
                    new File(inputParam.outFolder),
                    finalApkFile,
                    inputParam.apkPath,
                    inputParam.signatureType,
                    inputParam.minSDKVersion,
                    project
            );
            System.out.printf("<--Shield Dex Done! You can find the output in %s\n", inputParam.apkPath);
            clean();
        }
    }

    protected void clean() {
        config = null;
    }

    private void loadConfigFromGradle(InputParam inputParam) {
        try {
            config = new Configuration(inputParam);
            LogUtil.log("config");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//  protected void shieldDex(
//      File outputDir, File outputFile, String apkFilePath, InputParam.SignatureType signatureType) {
//    shieldDex(outputDir, outputFile, apkFilePath, signatureType, 14 /*default min sdk*/);
//  }

    protected void shieldDex(

        File outputDir, File outputFile, String apkFilePath, InputParam.SignatureType signatureType, int minSDKVersoin, Project project) {
        File apkFile = new File(apkFilePath);
        LogUtil.log("shieldDex");
        if (!apkFile.exists()) {
            System.err.printf("The input apk %s does not exist", apkFile.getAbsolutePath());
            goToError();
        }
        mRawApkSize = FileOperation.getFileSizes(apkFile);
        LogUtil.log("mRawApkSize");
        try {
            ApkShielder shielder = new ApkShielder(config, apkFile,project);
            LogUtil.log("shielder");
            /* 默认使用V1签名 */
            shieldDexs(outputDir, shielder, apkFile,signatureType,minSDKVersoin);
        } catch (Exception e) {
            e.printStackTrace();
            goToError();
        }
    }

    private void shieldDexs(File outputFile, ApkShielder shielder, File apkFile, InputParam.SignatureType signatureType, int minSDKVersoin)
            throws Exception {
        LogUtil.log("shieldDexs ---");
        if (outputFile == null) {
            mOutDir = new File(mRunningLocation, apkFile.getName().substring(0, apkFile.getName().indexOf(".apk")));
        } else {
            mOutDir = outputFile;
        }
        shielder.setOutDir(mOutDir.getAbsoluteFile());
        shielder.shield(signatureType,minSDKVersoin);
    }


    protected void goToError() {
        System.exit(ERRNO_USAGE);
    }
}