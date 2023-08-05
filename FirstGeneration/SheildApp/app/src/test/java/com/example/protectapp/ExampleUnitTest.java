package com.example.protectapp;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.Test;

import static org.junit.Assert.*;

import android.telephony.SignalThresholdInfo;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }


    @Test
    public void testUnzip() throws IOException {

        File input = new File("E:\\androidWorkSpace\\ProtectApp\\testData\\app-debug.apk");
        File output = new File("E:\\androidWorkSpace\\ProtectApp\\testData\\temp");
        if (!output.exists()) {
            output.createNewFile();
        }

        long l = System.currentTimeMillis();
//         decompression("E:\\androidWorkSpace\\ProtectApp\\testData\\app-debug.apk","E:\\androidWorkSpace\\ProtectApp\\testData\\temp");
//        unZip1(input, output);

        unZipttt(input,output);
        System.out.println("cost time " + (System.currentTimeMillis() - l));


    }


    public static void decompression(String fromPathStr, String targetPathStr) throws IOException {
        Path fromPath = Paths.get(fromPathStr);
        final Path targetPath = Paths.get(targetPathStr);
        FileSystem fs = FileSystems.newFileSystem(fromPath, null);
        long startTime = System.currentTimeMillis();

        Set<FileVisitOption> options = new HashSet<>();

        Files.walkFileTree(fs.getPath("/"), options, 2, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                if (file.getFileName().toString().endsWith(".dex")) {
                    System.out.println("file name " + file.getFileName());
                    Files.copy(file, targetPath.resolve(file.toString().substring(1)), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);

                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (dir.getParent() != null) {


                    if (dir.getFileName().toString().indexOf(".") == 0) { // MAC系统压缩自带的隐藏文件过滤
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    System.out.println("dir ------>" + dir.getFileName().toString());


                    Files.createDirectories(targetPath.resolve(dir.toString().substring(1)));
                }

                return FileVisitResult.CONTINUE;
            }
        });

    }


    public static void decompression1(String fromPathStr, String targetPathStr) throws IOException {
        Path fromPath = Paths.get(fromPathStr);
        final Path targetPath = Paths.get(targetPathStr);
        FileSystem fs = FileSystems.newFileSystem(fromPath, null);
        long startTime = System.currentTimeMillis();

        Set<FileVisitOption> options = new HashSet<>();

        Files.walkFileTree(fs.getPath("/"), options, 2, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                if (file.getFileName().toString().endsWith(".dex")) {
                    System.out.println("file name " + file.getFileName());
                    Files.copy(file, targetPath.resolve(file.toString().substring(1)), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);

                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (dir.getParent() != null) {


                    if (dir.getFileName().toString().indexOf(".") == 0) { // MAC系统压缩自带的隐藏文件过滤
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    System.out.println("dir ------>" + dir.getFileName().toString());


                    Files.createDirectories(targetPath.resolve(dir.toString().substring(1)));
                }

                return FileVisitResult.CONTINUE;
            }
        });

    }


    public static void unZip1(File zip, File dir) {
        try {
            long start = System.currentTimeMillis();
            dir.delete();

            ZipFile zipFile = new ZipFile(zip);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                String name = zipEntry.getName();

                if (!name.endsWith(".dex")) {
                    continue;
                }
                if (!zipEntry.isDirectory()) {
                    File file = new File(dir, name);
                    if (!file.getParentFile().exists()) file.getParentFile().mkdirs();

                    FileOutputStream fos = new FileOutputStream(file);
                    InputStream is = zipFile.getInputStream(zipEntry);

                    BufferedInputStream in = new BufferedInputStream(is);
                    BufferedOutputStream out = new BufferedOutputStream(fos);

                    byte[] buffer = new byte[20480];
                    int len;
                    while ((len = in.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                    }
                    in.close();
                    out.close();
                }
            }
            zipFile.close();
            long end = System.currentTimeMillis();
            Log.e("提示：", "unZip------- zipFile.close();----- " + (end - start));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void unZip(File zip, File dir) {
        try {
            long start = System.currentTimeMillis();
            dir.delete();

            ZipFile zipFile = new ZipFile(zip);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                String name = zipEntry.getName();

                if (!name.endsWith(".dex")) {
                    continue;
                }
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
            }
            zipFile.close();
            long end = System.currentTimeMillis();
            Log.e("提示：", "unZip------- zipFile.close();----- " + (end - start));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void unZipttt(File zip, File dir) {
        try {
            long start = System.currentTimeMillis();
            dir.delete();
            ThreadPoolUtils pool = new ThreadPoolUtils(5, 5, 10, "pool");
            ZipFile zipFile = new ZipFile(zip);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                String name = zipEntry.getName();

                if (!name.endsWith(".dex")) {
                    continue;
                }

                pool.execute(new ThreadPoolUtils.SimpleTask() {
                @Override
                public void run() {
                    try {


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
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            }
            zipFile.close();
            long end = System.currentTimeMillis();
            Log.e("提示：", "unZip------- zipFile.close();----- " + (end - start));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void decompressZIP(File file, File  dir) throws IOException {

        long start = System.currentTimeMillis();
        ThreadPoolUtils pool = new ThreadPoolUtils(5, 5, 10, "pool");

        ZipFile zipFile = new ZipFile(file, Charset.forName("GBK"));

        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        //使用线程池 提交任务  没有工具类 可自己new

        int size = 10;
//        final CountDownLatch countDownLatch = new CountDownLatch(size);
        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = entries.nextElement();
            String name = zipEntry.getName();

            if (!name.endsWith(".dex")) {
                continue;
            }

            try {

                File mfile = new File(dir, name);

                if (!mfile.getParentFile().exists()) mfile.getParentFile().mkdirs();
                FileOutputStream fos = new FileOutputStream(mfile);
                InputStream is = zipFile.getInputStream(zipEntry);
                byte[] buffer = new byte[30720];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
                is.close();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
//            pool.execute(new ThreadPoolUtils.SimpleTask() {
//                @Override
//                public void run() {
//
//                }
//            });

        }
//        threadPool.shutdown();


        zipFile.close();


    }


    private class FileWritingTask implements Runnable {
        private ZipFile zipFile;

        private String destPath;
        private ZipEntry zipEntry;
        private CountDownLatch countDownLatch;

        FileWritingTask(ZipFile zipFile, String destPath, ZipEntry zipEntry, CountDownLatch countDownLatch) {
            this.zipFile = zipFile;
            this.destPath = destPath;
            this.zipEntry = zipEntry;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            try {
                String name = zipEntry.getName();
                    //保留层级目录 解决文件重名问题

                    File file = new File(destPath + File.separator + name);
                    while (!file.exists()) {
//                        file=new File(destPath+File.separator+"(1)"+name);
                        File parentFile = file.getParentFile();
                        if (!parentFile.exists()) {
                            parentFile.mkdirs();
                        }
                        try {
                            InputStream inputStream = zipFile.getInputStream(this.zipEntry);

                            while (!file.exists()) {
                                Files.copy(inputStream, Paths.get(file.getPath()));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }


            } finally {
                countDownLatch.countDown();
            }
        }
    }


}