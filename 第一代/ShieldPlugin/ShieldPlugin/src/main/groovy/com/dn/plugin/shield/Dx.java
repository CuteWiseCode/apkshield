package com.dn.plugin.shield;

import com.dn.plugin.utils.LogUtil;

import org.gradle.internal.impldep.com.google.api.client.util.ArrayMap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author hzh
 */
public class Dx {

    public static File jar2Dex(File aarFile, File fakeDex) throws IOException, InterruptedException {
//        File fakeDex = new File(outputFile.getParent() + File.separator + "temp");
        System.out.println("jar2Dex: aarFile.getParent(): " + fakeDex.getParent());
        //解压aar到 fakeDex 目录下
//        Zip.unZip(aarFile, fakeDex);
        //过滤找到对应的fakeDex 下的classes.jar  以及libs 下的jar（依赖库）
        File[] files = fakeDex.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                 System.out.println("fakeDex.listFiles  "+ s);
                return s.equals("classes.jar");
            }
        });

        //获取 libs下的Jar 包
        File[] libsfiles = null;
        File libdir = new File(fakeDex.getAbsolutePath(),"libs");
        if(libdir.exists()){
             libsfiles = libdir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File file, String s) {
                    System.out.println("libs.listFiles  "+ s);
                    return s.endsWith(".jar");
                }
            });


            if (libsfiles != null) {
                for (File fileslib : libsfiles) {
                    System.out.println("lib jar file name:"+ fileslib.getName());

                }
            }
        }




        LogUtil.log("fakeDex.listFiles");
        if (files == null || files.length <= 0) {
            throw new RuntimeException("the aar is invalidate");
        }
        File classes_jar = files[0];
        StringBuilder builder = new StringBuilder();
        builder.append(classes_jar.getAbsolutePath());

        if(libsfiles!=null && libsfiles.length>0){
            for (File libsfile : libsfiles) {
                builder.append(" ");
                builder.append(libsfile.getAbsolutePath());
            }
        }

        // 将classes.jar 变成classes.dex
        File aarDex = new File(classes_jar.getParentFile(), "classes.dex");
        LogUtil.log("new File(classes.dex)");
        //我们要将jar 转变成为dex 需要使用android tools 里面的dx.bat
        //使用java 调用windows 下的命令
        Dx.dxCommand(aarDex, builder.toString());
        return aarDex;
    }

    public static void dxCommand(File aarDex, String jarpath) throws IOException, InterruptedException {
        Runtime runtime = Runtime.getRuntime();
        String cmd ="";
        if(Utils.isWindows()){
            LogUtil.log("isWindows() ");
            cmd ="cmd.exe /C dx --dex --output=" + aarDex.getAbsolutePath() + " " +
                    jarpath;
        }else{
            LogUtil.log("notWindows() ");
            cmd =" dx --dex --output=" + aarDex.getAbsolutePath() + " " +
                    jarpath;
        }
        Process process = runtime.exec(cmd);
        LogUtil.log("runtime.exec(cmd)");
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
            System.out.println(new String(bos.toByteArray(), "GBK"));
            throw new RuntimeException("dx run failed");
        }
        process.destroy();
        LogUtil.log(" process.destroy();");
    }
}
