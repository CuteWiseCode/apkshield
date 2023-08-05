package com.ck.plugin.utils;

import com.ck.plugin.shield.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * @Author: ck
 * @Date: 2023/1/16
 * @Desc: java类作用描述
 */
public class SignUtils {
    private SignUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    private static void exec(String[] cmd, String execName) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(cmd);
        System.out.println("start " + execName);
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw e;
        }
        if (process.exitValue() != 0) {
            InputStream inputStream = process.getErrorStream();
            int len;
            byte[] buffer = new byte[2048];
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while ((len = inputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            System.out.println(new String(bos.toByteArray(), "gbk"));
            throw new RuntimeException(execName + " execute fail");
        }
        System.out.println("finish " + execName);
        process.destroy();
    }

    /**
     * V1签名
     */
    private static String signature(File unsignedApk, String keyStore, String keyPwd, String alias, String alisaPwd)
            throws InterruptedException, IOException {
        String path = unsignedApk.getAbsolutePath();
        String v1Name = path.substring(0, path.indexOf(".apk")) + "_v1.apk";
        String[] cmd;
                if(Utils.isWindows())
                {
                    cmd=  new String[] { "cmd.exe",
                            "/C ", "jarsigner", "-sigalg", "SHA1withRSA", "-digestalg", "SHA1", "-keystore",
                            keyStore, "-storepass", keyPwd, "-keypass", alisaPwd, "-signedjar", v1Name,
                            unsignedApk.getAbsolutePath(), alias };
                }else{
                    cmd=  new String[] {"jarsigner", "-sigalg", "SHA1withRSA", "-digestalg", "SHA1", "-keystore",
                            keyStore, "-storepass", keyPwd, "-keypass", alisaPwd, "-signedjar", v1Name,
                            unsignedApk.getAbsolutePath(), alias };
                }

        LogUtil.log("v1 sign cmd->" + Arrays.toString(cmd));

        exec(cmd, "v1 sign");

//        FileUtils.delete(path);

        return v1Name;
    }

    // zipalign -p 4 input\app-release-unsigned.apk input\app-release-unsigned.apk
    private static String apkZipalign(String v1Apk) throws IOException, InterruptedException {
        String zipalignName = v1Apk.substring(0, v1Apk.indexOf(".apk")) + "_align.apk";
        String cmd[];
        if(Utils.isWindows()) {
             cmd = new String[]{"cmd.exe","/C ","zipalign", "-p", "4", v1Apk, zipalignName};
        }else{
            cmd = new String[]{"zipalign", "-p", "4", v1Apk, zipalignName};
        }

        exec(cmd, "zipalign");

//        FileUtils.delete(v1Apk);

        return zipalignName;
    }

    //apksigner.jar sign  --ks key.jks --ks-key-alias releasekey  --ks-pass pass:pp123456  --key-pass pass:pp123456  --out output.apk  input.apk
    public static void apkSignature(File unsignedApk, File signedApk, String keyStore, String keyPwd, String alias, String alisaPwd) throws IOException, InterruptedException {
        String v1Name = signature(unsignedApk, keyStore, keyPwd, alias, alisaPwd);
        String zipalignName = apkZipalign(v1Name);
        String cmd[];
        if(Utils.isWindows()) {
             cmd = new String[]{"cmd.exe","/C ","apksigner", "sign", "--ks", keyStore, "--ks-pass", "pass:" + keyPwd,
                     "--ks-key-alias", alias, "--key-pass", "pass:" + alisaPwd,
                     "--out", signedApk.getAbsolutePath(), zipalignName};
        }else{
            cmd = new String[]{"apksigner", "sign", "--ks", keyStore, "--ks-pass", "pass:" + keyPwd,
                    "--ks-key-alias", alias, "--key-pass", "pass:" + alisaPwd,
                    "--out", signedApk.getAbsolutePath(), zipalignName};
        }

        exec(cmd, "v2 sign");

//        FileUtils.delete(zipalignName);
//        FileUtils.delete(signedApk.getAbsolutePath() + ".idsig");
    }
}
