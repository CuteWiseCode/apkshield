package com.dn.plugin.shield;

import static com.dn.plugin.ext.InputParam.SignatureType.SchemaV1;
import static com.dn.plugin.ext.InputParam.SignatureType.SchemaV2;
import static com.dn.plugin.ext.InputParam.SignatureType.SchemaV3;

import com.dn.plugin.ext.Configuration;
import com.dn.plugin.ext.InputParam;
import com.dn.plugin.utils.LogUtil;
import com.dn.plugin.utils.StringUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author hzh
 */

public class Signature {
    public static void signature(File unsignedApk, File signedApk, Configuration config, InputParam.SignatureType signatureType, int minSDKVersoin) throws Exception {
        String signatureAlgorithm = "MD5withRSA";
        String cmd[] = null;
        if (config.mUseSignAPK) {

            if (signatureType == SchemaV1) {
                LogUtil.log("正式 签名 SchemaV1");
                cmd = new String[]{
                        "jarsigner",
                        "-sigalg",
                        signatureAlgorithm,
                        "-digestalg",
                        config.digestAlg,
                        "-keystore",
                        config.mSignatureFile.getAbsolutePath(),
                        "-storepass",
                        config.mStorePass,
                        "-keypass",
                        config.mKeyPass,
                        "-signedjar",
                        signedApk.getAbsolutePath(),
                        unsignedApk.getAbsolutePath(),
                        config.mStoreAlias
                };
                runExec(cmd);
            } else if (signatureType == SchemaV2 || signatureType == SchemaV3) {
                LogUtil.log("正式 签名 SchemaV2 || SchemaV3");
                String params[] = new String[]{
                        "sign",
                        "--ks",
                        config.mSignatureFile.getAbsolutePath(),
                        "--ks-pass",
                        "pass:" + config.mStorePass,
                        "--min-sdk-version",
                        String.valueOf(minSDKVersoin),
                        "--ks-key-alias",
                        config.mStoreAlias,
                        "--key-pass",
                        "pass:" + config.mKeyPass,
                        "--v3-signing-enabled",
                        String.valueOf(signatureType == SchemaV3),
                        "--out",
                        signedApk.getAbsolutePath(),
                        unsignedApk.getAbsolutePath(),
                };
                ApkSignerTool.main(params);
            }

        } else {
            LogUtil.log("debug 签名");
            cmd = new String[]{"cmd.exe",
                    "/C ",
                    "jarsigner",
                    "-sigalg",
                    "MD5withRSA",
                    "-digestalg",
                    "SHA1",
                    "-keystore", "C:/Users/Administrator/.android/debug.keystore",
                    "-storepass", "android",
                    "-keypass", "android",
                    "-signedjar", signedApk.getAbsolutePath(),
                    unsignedApk.getAbsolutePath(),
                    "androiddebugkey"};
            runExec(cmd);
        }



    }


    public static String runExec(String[] argv) throws IOException, InterruptedException {
        Process process = null;
        String output;
        try {
            process = Runtime.getRuntime().exec(argv);
            output = StringUtil.readInputStream(process.getInputStream());
            process.waitFor();
            if (process.exitValue() != 0) {
                System.err.println(String.format("%s Failed! Please check your signature file.\n", argv[0]));
                throw new RuntimeException(StringUtil.readInputStream(process.getErrorStream()));
            }
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return output;
    }


}
