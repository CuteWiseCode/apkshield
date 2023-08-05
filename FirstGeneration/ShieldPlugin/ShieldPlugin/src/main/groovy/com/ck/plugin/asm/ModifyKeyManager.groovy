package com.ck.plugin.asm


import com.ck.plugin.shield.Utils
import com.ck.plugin.shield.Zip
import com.ck.plugin.utils.LogUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

/**
 * @Author: ck
 * @Date: 2022/12/7
 * @Desc: 修改壳中的key
 */
class ModifyKeyManager {

    private String passWd
    private int count

    File modifyAESKey(File srcAARfile, File dstAarFile, String pwd, File newApkFile) throws FileNotFoundException {

        this.passWd =pwd

        File[] dexFiles = newApkFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.endsWith(".dex")
            }
        })
        this.count =dexFiles.size()+1

        LogUtil.log("starting modify key ,dex count ="+ count)
        if (srcAARfile == null) {
            System.out.println("srcAARfile null")
            throw new FileNotFoundException("srcAARfile null")
        }
        //1.解压 壳aar, 获取classes.jar 包
        LogUtil.log("unZip srcAARfile")
        Zip.unZip(srcAARfile, dstAarFile)


        //2.获取对应壳的jar 文件

        File[] jarFiles = dstAarFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.endsWith(".jar")
            }
        })



        LogUtil.log("listFiles jar")

        //3.遍历jar 修改对应的AES 秘钥
        for (File jarFile : jarFiles) {
            handleJarFile(jarFile,dstAarFile)
        }

        //4. 重新打包成aar
        File newAar = new File(dstAarFile.getParent() + File.separator + "newaar.aar")
        newAar.getParentFile().mkdirs()
        LogUtil.log("Zip.zip(dstAarFile, newAar)")
        Zip.zip(dstAarFile, newAar)

        return newAar

    }


    /**
     * 处理Jar中的class文件
     * @param
     * @param
     */
    void handleJarFile(File jarfile,File dstAarFile) {
        LogUtil.log("handleJarFile")
        if (jarfile.getAbsolutePath().endsWith(".jar")) {

            def jarName = jarfile.name
            def md5Name = DigestUtils.md5Hex(jarfile.absolutePath)
            if (jarName.endsWith(".jar")) {
                jarName = jarName.substring(0, jarName.length() - 4)
            }
            JarFile jarFile = new JarFile(jarfile)
            Enumeration enumeration = jarFile.entries()
            File tempFile = new File(dstAarFile.parent + File.separator + "temp.jar")

            JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(tempFile))
            //保存
            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = enumeration.nextElement()
                String entryName = jarEntry.name
                ZipEntry zipEntry = new ZipEntry(entryName)
                InputStream inputStream = jarFile.getInputStream(zipEntry)
                jarOutputStream.putNextEntry(zipEntry)
                LogUtil.log("entryName: $entryName")
                if (isClassFile(entryName) && entryName.contains("com/qihoo/util/a")) {
                    byte[] bytes = domodifyKey(entryName, IOUtils.toByteArray(inputStream))
                    jarOutputStream.write(bytes)
                } else if(isClassFile(entryName) && entryName.contains("com/qihoo/util/Zip")){
                    byte[] bytes = domodifyKey(entryName, IOUtils.toByteArray(inputStream))
                    jarOutputStream.write(bytes)
                }else{
                    jarOutputStream.write(IOUtils.toByteArray(inputStream))

                }
                jarOutputStream.closeEntry()
            }
            jarOutputStream.close()
            jarFile.close()

            FileOutputStream fos = new FileOutputStream(jarfile);
            byte[] fbytes = Utils.getBytes(tempFile)
            fos.write(fbytes)
            fos.flush()
            fos.close()

//            FileUtils.copyFile(tempFile, jarfile)
//            tempFile.delete()
        }
    }

    /**
     * 修改AES key
     * @param name
     * @param bytes
     * @return
     */
    byte[] domodifyKey(String name, byte[] bytes) {
        ClassReader classReader = new ClassReader(bytes)
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
        ClassVisitor classVisitor = new ModifyKeyClassVisitor(classWriter, name,this.passWd,this.count)
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
        return classWriter.toByteArray()
    }


    static boolean isClassFile(String name) {
        return (name.endsWith(".class") && !name.contains("R\$")
                && !name.contains("R.class") && "BuildConfig.class" != name)
    }

}
