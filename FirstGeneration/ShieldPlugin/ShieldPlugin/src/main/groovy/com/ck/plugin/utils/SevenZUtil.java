package com.ck.plugin.utils;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.commons.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;

/**
 * @Author: ck
 * @Date: 2022/12/7
 * @Desc: java类作用描述
 */
public class SevenZUtil {

    // 7z压缩
    public static void _7z(File outPutFile, File srcFile) throws IOException {
        try (SevenZOutputFile outputFile = new SevenZOutputFile(outPutFile);) {
            LogUtil.log("7z outputFile");

            for (File file : Objects.requireNonNull(srcFile.listFiles())) {
                _7zRecursive(outputFile, file, "");
            }

        }catch (Exception e){
            LogUtil.log("7z Exception");
            e.printStackTrace();
        }
    }
    // 递归压缩目录下的文件和目录
    private static void _7zRecursive(SevenZOutputFile _7zFile, File srcFile, String basePath) throws IOException {
        if (srcFile.isDirectory()) {
            LogUtil.log("7z srcFile.isDirectory()");
            File[] files = srcFile.listFiles();
            String nextBasePath = basePath + srcFile.getName() + "/";
            // 空目录
            if (files == null || files.length ==0) {
                SevenZArchiveEntry entry = _7zFile.createArchiveEntry(srcFile, nextBasePath);
                _7zFile.putArchiveEntry(entry);
                _7zFile.closeArchiveEntry();
            } else {
                for (File file : files) {
                    _7zRecursive(_7zFile, file, nextBasePath);
                }
            }
        } else {
            LogUtil.log("7z srcFile.isFile");
            SevenZArchiveEntry entry = _7zFile.createArchiveEntry(srcFile, basePath + srcFile.getName());
            _7zFile.putArchiveEntry(entry);
            byte[] bs = FileUtils.readFileToByteArray(srcFile);
            _7zFile.write(bs);
            _7zFile.closeArchiveEntry();
        }
    }
    // 7z解压
    public void un7z() throws IOException {
        String outPath = "/test";
        try (SevenZFile archive = new SevenZFile(new File("test.7z"))) {
            SevenZArchiveEntry entry;
            while ((entry = archive.getNextEntry()) != null) {
                File file = new File(outPath, entry.getName());
                if (entry.isDirectory()) {
                    file.mkdirs();
                }
                if (entry.hasStream()) {
                    final byte [] buf = new byte [1024];
                    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    for (int len = 0; (len = archive.read(buf)) > 0;) {
                        baos.write(buf, 0, len);
                    }
                    FileUtils.writeByteArrayToFile(file, baos.toByteArray());
                }
            }
        }
    }


    private static void addToArchiveCompression(SevenZOutputFile out, File file, String dir) throws IOException {
        String name = dir + File.separator + file.getName();
        if (file.isFile()){
            LogUtil.log("7z file.isFile()");
            SevenZArchiveEntry entry = out.createArchiveEntry(file, name);
            out.putArchiveEntry(entry);

            FileInputStream in = new FileInputStream(file);
            byte[] b = new byte[1024];
            int count = 0;
            while ((count = in.read(b)) > 0) {
                out.write(b, 0, count);
            }
            out.closeArchiveEntry();

        } else if (file.isDirectory()) {
            LogUtil.log("7z file.isDirectory()");
            File[] children = file.listFiles();
            if (children != null){
                for (File child : children){
                    addToArchiveCompression(out, child, name);
                }
            }
        } else {
            System.out.println(file.getName() + " is not supported");
        }
    }
}
