package com.qihoo.util;

import android.util.Log;


import org.zeroturnaround.zip.NameMapper;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;



public class Zip {


    public static void unZip(File zip, File dir) {
        try {
            long start = System.currentTimeMillis();
//            dir.delete();

            ZipFile zipFile = new ZipFile(zip);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                String name = zipEntry.getName();
                if (!name.endsWith(".dex")) {
                    continue;
                }


                long start1 = System.currentTimeMillis();
                if (!zipEntry.isDirectory()) {
                    File file = new File(dir, name);
                    if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
                    FileOutputStream fos = new FileOutputStream(file);
                    InputStream is = zipFile.getInputStream(zipEntry);
                    byte[] buffer = new byte[30720];
                    int len;
                    while ((len = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                    is.close();
                    fos.close();
                }

                Log.e("解压：", "unZip------- " + name + "   " + (System.currentTimeMillis() - start1));
            }
            zipFile.close();
            long end = System.currentTimeMillis();
            Log.e("提示：", "unZip------- zipFile.close();----- " + (end - start));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void unZip1(File zip, File dir) {
        try {
            long start = System.currentTimeMillis();
//            dir.delete();
            ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());


            ZipFile zipFile = new ZipFile(zip);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            int count = 0;
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                String name = zipEntry.getName();
//                Log.e("解压遍历：", "unZip----------- " + name);
                if (!name.endsWith(".dex")) {
                    continue;
                }

                threadPool.execute(new FileWritingTask(zipEntry, dir, zipFile));
                count++;
                if (getDexNumber() > 0 && count >= getDexNumber())
                    break;

            }

            threadPool.shutdown();
            try {
                threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            zipFile.close();
            long end = System.currentTimeMillis();
            Log.e("提示：", "unZip------- zipFile.close();----- " + (end - start));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static class FileWritingTask implements Runnable {

        private ZipEntry zipEntry;
        private File dir;
        private ZipFile zipFile;

        FileWritingTask(ZipEntry zipEntry, File dir, ZipFile zipFile) {
            this.zipEntry = zipEntry;
            this.dir = dir;
            this.zipFile = zipFile;
        }

        @Override
        public void run() {

            long start1 = System.currentTimeMillis();
            try {
                if (!zipEntry.isDirectory()) {
                    File file = new File(dir, zipEntry.getName());
                    if (!file.getParentFile().exists()) file.getParentFile().mkdirs();


//                    FileOutputStream fos = new FileOutputStream(file);
//                    InputStream is = zipFile.getInputStream(zipEntry);
//                    byte[] buffer = new byte[30720];
//                    int len;
//                    while ((len = is.read(buffer)) != -1) {
//                        fos.write(buffer, 0, len);
//                    }
//                    is.close();
//                    fos.close();

                    try (
                            InputStream inputStream = zipFile.getInputStream(this.zipEntry);
                            OutputStream outputStream = new FileOutputStream(file);
                    ) {
                        int read;
                        byte[] bytes = new byte[inputStream.available()];
                        while ((read = inputStream.read(bytes)) != -1) {
                            outputStream.write(bytes, 0, read);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


            Log.e("解压：", "unZip------- " + zipEntry.getName() + "   " + (System.currentTimeMillis() - start1));
        }
    }


    public static void unZip2(File zip, File dir) {
        try {
            long start = System.currentTimeMillis();
            File[] dexFiles = zip.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File file, String s) {
                    return s.endsWith(".dex");
                }
            });

            for (File dexFile : dexFiles) {
                File file = new File(dir, dexFile.getName());
                if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
                FileOutputStream fos = new FileOutputStream(file);
                InputStream is = new FileInputStream(dexFile);
                byte[] buffer = new byte[30720];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
                is.close();
                fos.close();
            }

            long end = System.currentTimeMillis();
            Log.e("提示：", "unZip------- zipFile.close();----- " + (end - start));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getDexNumber() {
        int a = new Random().nextInt();
        return a;
    }


}
