package com.ck.plugin.shield;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;


public class Zip {

    public static void unZip(File zip, File dir) {
        try {
            dir.delete();
            ZipFile zipFile = new ZipFile(zip);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                String name = zipEntry.getName();
                if (name.equals("META-INF/CERT.RSA") || name.equals("META-INF/CERT.SF") || name
                        .equals("META-INF/MANIFEST.MF")) {
                    continue;
                }
                if (!zipEntry.isDirectory()) {
                    File file = new File(dir, name);
                    if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
                    FileOutputStream fos = new FileOutputStream(file);
                    InputStream is = zipFile.getInputStream(zipEntry);
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                    is.close();
                    fos.close();
                }
            }
            zipFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copyJni(File from, File todir) {
        try {

            if (!todir.exists()) {//判断文件夹是否创建，没有创建则创建新文件夹
                todir.mkdirs();
            }
            if(from.exists()){
                for (File f : from.listFiles()) {

                    File file = new File(todir.getAbsolutePath() + File.separator + f.getName());
//                    file.delete();

                    if(!file.exists()) {
                        if (f.renameTo(file)) {
                            System.out.println("文件夹移动成功！" + f.getAbsolutePath());
                        } else {
                            System.out.println("文件夹移动失败！" + f.getAbsolutePath());
                        }
                    }

                    if(f.listFiles()!=null && f.listFiles().length>0)
                    for (File f1 : f.listFiles()) {
                        File file1 = new File(file.getAbsolutePath() + File.separator + f1.getName());
                        file1.delete();
                        if (f1.renameTo(file1)) {
                            System.out.println("文件移动成功！"+ f1.getAbsolutePath());
                        }else{
                            System.out.println("文件移动失败！"+ f1.getAbsolutePath());
                        }
                    }


                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void zip(File dir, File zip) throws Exception {
        zip.delete();
        CheckedOutputStream cos = new CheckedOutputStream(new FileOutputStream(
                zip), new CRC32());
        ZipOutputStream zos = new ZipOutputStream(cos);
        compress(dir, zos, "");
        zos.flush();
        zos.close();
    }

    private static void compress(File srcFile, ZipOutputStream zos,
                                 String basePath) throws Exception {
        if (srcFile.isDirectory()) {
            compressDir(srcFile, zos, basePath);
        } else {
            compressFile(srcFile, zos, basePath);
        }
    }

    private static void compressDir(File dir, ZipOutputStream zos,
                                    String basePath) throws Exception {
        File[] files = dir.listFiles();
        if (files.length < 1) {
            ZipEntry entry = new ZipEntry(basePath + dir.getName() + "/");
            zos.putNextEntry(entry);
            zos.closeEntry();
        }
        for (File file : files) {
            compress(file, zos, basePath + dir.getName() + "/");
        }
    }

    private static void compressFile(File file, ZipOutputStream zos, String dir)
            throws Exception {


        String dirName = dir + file.getName();

        String[] dirNameNew = dirName.split("/");

        StringBuffer buffer = new StringBuffer();

        if (dirNameNew.length > 1) {
            for (int i = 1; i < dirNameNew.length; i++) {
                buffer.append("/");
                buffer.append(dirNameNew[i]);

            }
        } else {
            buffer.append("/");
        }

        ZipEntry entry = new ZipEntry(buffer.toString().substring(1));
        entry.setMethod(ZipEntry.STORED);
        entry.setCompressedSize(file.length());
        entry.setSize(file.length());
        CRC32 crc = new CRC32();
        crc.update(getFileBytes(file));
        entry.setCrc(crc.getValue());

        zos.putNextEntry(entry);
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(
                file));
        int count;
        byte data[] = new byte[1024];
        while ((count = bis.read(data, 0, 1024)) != -1) {
            zos.write(data, 0, count);
        }
        bis.close();
        zos.closeEntry();
    }


    /**
     *
     * 方法名：getFileBytes<br>
     * 描述：获取文件的bytes<br>
     * 创建时间：2016-12-14 下午2:23:33<br>
     * @param file
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */

    public static byte[] getFileBytes(File file) throws FileNotFoundException,
            IOException {
        byte[] buffer;
        FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
        byte[] b = new byte[1000];
        int n;
        while ((n = fis.read(b)) != -1) {
            bos.write(b, 0, n);
        }
        fis.close();
        bos.close();
        buffer = bos.toByteArray();
        return buffer;
    }

}
