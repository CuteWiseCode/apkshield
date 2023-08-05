package com.ck.plugin.shield;


import com.ck.plugin.asm.ModifyKeyManager;
import com.ck.plugin.ext.AndrolibException;
import com.ck.plugin.ext.Configuration;
import com.ck.plugin.ext.DirectoryException;
import com.ck.plugin.ext.InputParam;
import com.ck.plugin.utils.ExtFile;
import com.ck.plugin.utils.LogUtil;
import com.ck.plugin.utils.SignUtils;
import com.ck.plugin.utils.TypedValue;

import org.gradle.api.Project;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * @author ck
 */
public class ApkShielder {

    private final Configuration config;
    private final ExtFile apkFile;
    private File mOutDir;
    private File mOutTempARSCFile;
    private File mOutARSCFile;
    private File mOutResFile;
    private File mRawResFile;
    private File mOutTempDir;
    private File mResMappingFile;
    private File mMergeDuplicatedResMappingFile;
    private HashMap<String, Integer> mCompressData;
    private Project mProject;

    public ApkShielder(Configuration config, File apkFile, Project project) {
        this.config = config;
        this.apkFile = new ExtFile(apkFile);
        this.mProject = project;
    }


    public Configuration getConfig() {
        return config;
    }

    public boolean hasDexs() throws AndrolibException {
        try {
            return apkFile.getDirectory().containsFile("classes.dex");
        } catch (DirectoryException ex) {
            throw new AndrolibException(ex);
        }
    }


    /**
     * 根据config来修改压缩的值
     */
    private void dealWithCompressConfig() {
        if (config.mUseCompress) {
            HashSet<Pattern> patterns = config.mCompressPatterns;
            if (!patterns.isEmpty()) {
                for (Entry<String, Integer> entry : mCompressData.entrySet()) {
                    String name = entry.getKey();
                    for (Iterator<Pattern> it = patterns.iterator(); it.hasNext(); ) {
                        Pattern p = it.next();
                        if (p.matcher(name).matches()) {
                            mCompressData.put(name, TypedValue.ZIP_DEFLATED);
                        }
                    }
                }
            }
        }
    }

    public HashMap<String, Integer> getCompressData() {
        return mCompressData;
    }

    public File getOutDir() {
        return mOutDir;
    }

    public void setOutDir(File outDir) throws AndrolibException {
        mOutDir = outDir;
    }

    public File getOutTempDir() {
        return mOutTempDir;
    }


    public void shield(InputParam.SignatureType signatureType, int minSDKVersoin) throws Exception {
        LogUtil.log("shield() ---");
        if (hasDexs()) {
            System.out.printf("shielding dexs\n");
            String pwd = getRandomkey();
            LogUtil.log("pwd "+pwd);
            AES.init(pwd);

            //1.解压apk
            File apkFile = new File(this.apkFile.getAbsolutePath());
            File newApkFile = new File(apkFile.getParent() + File.separator + "temp");
            if (newApkFile.exists()) {
                    File[]files = newApkFile.listFiles();
                    for(File file: files){
                        if (file.isFile()) {
                            file.delete();
                        }
                    }
                newApkFile.delete();
                newApkFile.mkdirs();
            }

            LogUtil.log("加密dex");
            //2.加密dex
            File mainDexFile = AES.encryptAPKFile(apkFile, newApkFile);

            LogUtil.log("重命名 dex");
            //3.重命名 dex
            if (newApkFile.isDirectory()) {
                File[] listFiles = newApkFile.listFiles();
                for (File file : listFiles) {
                    if (file.isFile()) {
                        if (file.getName().endsWith(".dex")) {
                            String name = file.getName();
                            System.out.println("rename step1:" + name);
                            int cursor = name.indexOf(".dex");
//                            String newName = file.getParent() + File.separator + name.substring(0, cursor) + "_" + ".dex";
                            String newName = newApkFile.getAbsolutePath() + File.separator +"assets" + File.separator +"dexs"+ File.separator + name.substring(0, cursor) + "_" + ".dex";
                           File dir = new File(newApkFile.getAbsolutePath() + File.separator +"assets" + File.separator +"dexs");
                           if(!dir.exists())
                               dir.mkdir();

                            System.out.println("rename step2:" + newName);
                            file.renameTo(new File(newName));
                        }else if(file.getName().contains("AndroidManifest.xml")){
//                            LogUtil.log("starting parse AndroidManifest.xml"+ file.getAbsolutePath());
//                            //处理AndroidManifest.xml
//                            ManifestModifier.modifyManifest(file);
                        }
                    }
                }
            }


            LogUtil.log("获取壳dex");
            //5.获取壳dex
            File aarFile = new File(mProject.getRootProject().getRootDir()+File.separator+"modules/plugins/shield/shield-release.aar");
            //壳解压，对应源apk文件夹/shield/..
            File outputAarDir = new File(apkFile.getParent() + File.separator + "shield");
            //遍历删除缓存文件
            if (outputAarDir.exists()) {
                File[]files = newApkFile.listFiles();
                for(File file: files){
                    if (file.isFile()) {
                        file.delete();
                    }
                }
                outputAarDir.delete();
                outputAarDir.mkdirs();
            }


            //6. 动态修改壳中AES 秘钥
            ModifyKeyManager modifyKeyManager = new ModifyKeyManager();
            aarFile = modifyKeyManager.modifyAESKey(aarFile,outputAarDir,pwd,newApkFile);

            //拷贝jni
            File inputJniDir = new File(apkFile.getParent() + File.separator + "shield"+File.separator+"jni");
            File outputJniDir = new File(newApkFile.getAbsolutePath() + File.separator +"lib");
            Zip.copyJni(inputJniDir,outputJniDir);

            File aarDex = Dx.jar2Dex(aarFile,outputAarDir);

            File tempMainDex = new File(newApkFile.getPath() + File.separator + "classes.dex");
            if (!tempMainDex.exists()) {
                tempMainDex.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(tempMainDex);
            byte[] fbytes = Utils.getBytes(aarDex);
            fos.write(fbytes);
            fos.flush();
            fos.close();



            LogUtil.log("打包签名");
            //签名
            File unsignedApk = new File(newApkFile.getParent()+"/result/apk-unsigned.apk");

            unsignedApk.getParentFile().mkdirs();

            Zip.zip(newApkFile, unsignedApk);


            //
//            //对齐
//            LogUtil.log("对齐");
//            File alignedApk = new File(newApkFile.getParent()+"/result/alignedApk.apk");
//            if (!alignedApk.exists()) {
//                alignedApk.createNewFile();
//            }
//            //签名之后进行对齐
//            alignApk(signedApk,alignedApk);

            LogUtil.log("签名");
            File signedApk = new File(newApkFile.getParent()+"/result/apk-signed.apk");


//            Signature.signature(unsignedApk, signedApk,config,signatureType,minSDKVersoin);
            SignUtils.apkSignature(unsignedApk,signedApk,config.mSignatureFile.getAbsolutePath(),config.mStorePass,config.mStoreAlias,config.mKeyPass);



            //覆盖原来apk
            FileOutputStream fos1 = new FileOutputStream(this.apkFile);
            byte[] fbytes1 = Utils.getBytes(signedApk);
            fos1.write(fbytes1);
            fos1.flush();
            fos1.close();


        }
    }

    String getRandomkey() {
        LogUtil.log("getRandomkey");
        int count = 16;
        Random random = new Random();
        LogUtil.log(" new Random();");
        String str = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder();
        LogUtil.log("source str "+str);
        for (int j = 0; j < count; j++) {
            int number = random.nextInt(26);
            sb.append(str.charAt(number));
        }

        return sb.toString();
    }


    private void alignApk(File before, File after) throws IOException, InterruptedException {
        LogUtil.log("zipaligning apk: %s\n"+ before.getName());
        if (!before.exists()) {
            throw new IOException(String.format("can not found the raw apk file to zipalign, path=%s",
                    before.getAbsolutePath()
            ));
        }
        String cmd = Utils.isPresent(config.mZipalignPath) ? config.mZipalignPath : TypedValue.COMMAND_ZIPALIGIN;
        ProcessBuilder pb = new ProcessBuilder(cmd, "4", before.getAbsolutePath(), after.getAbsolutePath());
        Process pro = pb.start();
        //destroy the stream
        pro.waitFor();
        pro.destroy();
    }



}
