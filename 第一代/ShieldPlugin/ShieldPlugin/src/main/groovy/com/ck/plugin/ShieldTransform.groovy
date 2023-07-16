package com.ck.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import com.ck.plugin.ext.ShieldConfig
import com.ck.plugin.ext.ShieldExtension
import com.google.gson.Gson
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.IOUtils
import org.gradle.api.Project

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry
import com.ck.plugin.utils.GenUtil

/**
 * @Author: hzh
 * @Date: 2022/10/14
 * @Desc: ShieldTransform
 */
class ShieldTransform extends Transform {

    def packageRelease = "packageRelease"
    def packageDebug = "assembleDebug"
    def mProject
    def static mContent
    static ShieldExtension shieldExtension
    static ShieldConfig shieldConfig
    def final DEFAULT_CONFIG_FILE_NAME = "=shield_config.json"
    def final EXT_NAME = "shield"

    void setProject(mProject) {

        def android = mProject.extensions.getByType(AppExtension)
        if (!android) {
            throw IllegalArgumentException("must apply this plugin after 'com.android.application'")
        }

        this.mProject = mProject

    }



    @Override
    String getName() {
        return "ShieldTransform"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)
//        handleTransform(transformInvocation)
    }


    /**
     * to handle transform
     * @param inputParam
     * @param transformInvocation
     */
    void handleTransform(TransformInvocation transformInvocation) {

        shieldConfig.junkPackages.addAll(JunkCodeGenerateClassTask.newPackages)
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider()
        if (!isIncremental())
            outputProvider.deleteAll()

        //得到所有的输入
        Collection<TransformInput> inputs = transformInvocation.getInputs()

        inputs.each { it ->

            it.directoryInputs.each {
                handleDirectoryInput(it, outputProvider)
            }

            it.jarInputs.each { jarInput ->
                handleJarInput(jarInput, outputProvider)
            }

        }
    }

    /**
     * 处理Jar中的class文件
     * @param src
     * @param dest
     */
    void handleJarInput(JarInput jarInput, TransformOutputProvider outputProvider) {

        if (jarInput.file.getAbsolutePath().endsWith(".jar")) {
            //重名名输出文件,因为可能同名,会覆盖
            def jarName = jarInput.name
            def md5Name = DigestUtils.md5Hex(jarInput.file.absolutePath)
            if (jarName.endsWith(".jar")) {
                jarName = jarName.substring(0, jarName.length() - 4)
            }
            JarFile jarFile = new JarFile(jarInput.file)
            Enumeration enumeration = jarFile.entries()
            File tempFile = new File(jarInput.file.parent + File.separator + "temp.jar")
            //避免上次的缓存被重复插入
            if (tempFile.exists()) {
                tempFile.delete()
            }
            JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(tempFile))
            //保存
            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = enumeration.nextElement()
                String entryName = jarEntry.name
                ZipEntry zipEntry = new ZipEntry(entryName)
                InputStream inputStream = jarFile.getInputStream(zipEntry)
                jarOutputStream.putNextEntry(zipEntry)
                if (isClassFile(entryName) && isClassNeededAddJunkCode(entryName, "/")) {
                    byte[] bytes = addJunkCode(entryName, IOUtils.toByteArray(inputStream))
                    jarOutputStream.write(bytes)
                } else {
                    jarOutputStream.write(IOUtils.toByteArray(inputStream))
                }
                jarOutputStream.closeEntry()
            }
            jarOutputStream.close()
            jarFile.close()
            def dest = outputProvider.getContentLocation(jarName + "_" + md5Name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
            FileUtils.copyFile(tempFile, dest)
            tempFile.delete()
        }
    }

    /**
     * 处理目录下的class文件
     * @param directoryInput
     * @param outputProvider
     */
//    static void handleDirectoryInput(DirectoryInput directoryInput, TransformOutputProvider outputProvider) {
////        println("-------------------- handle class file:<$directoryInput.name> --------------------")
//        def dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
//        FileUtils.copyDirectory(directoryInput.file, dest)
//    }

    /**
     * 处理目录下的class文件
     * @param directoryInput
     * @param outputProvider
     */
    void handleDirectoryInput(DirectoryInput directoryInput, TransformOutputProvider outputProvider) {
        //是否为目录
        if (directoryInput.file.isDirectory()) {
            //列出目录所有文件（包含子文件夹，子文件夹内文件）
            directoryInput.file.eachFileRecurse {
                file ->
                    def name = file.name
                    addJunkCodeIfNeeded(name, file)
            }
        }
        def dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
        FileUtils.copyDirectory(directoryInput.file, dest)
    }

    private void addJunkCodeIfNeeded(String name, File file) {
        if (isClassFile(name) && isClassNeededAddJunkCode(file.absolutePath, File.separator)) {
            log("-------------------- handle class file:<$name> --------------------")
            byte[] bytes = addJunkCode(name, file.bytes)
            FileOutputStream fileOutputStream = new FileOutputStream(file.parentFile.absolutePath + File.separator + name)
            fileOutputStream.write(bytes)
            fileOutputStream.close()
        }
    }




    /**
     * 判断是否为需要处理class文件
     * @param name
     * @return
     */
    boolean isClassFile(String name) {
        return (name.endsWith(".class") && !name.contains("R\$")
                && !name.contains("R.class") && "BuildConfig.class" != name)
    }

    boolean isClassNeededAddJunkCode(String name, String seperator) {
        if (shieldConfig?.junkPackages)
            for (filename in shieldConfig?.junkPackages) {

                filename = filename.replace(".", seperator)


                if (name.contains(filename)) {
                    log("isClassNeededAddJunkCode path ${name} item ${filename} ----> true")
                    return true
                }
            }
        return false
    }

    void loadFileConfig(Project project) {
        try {
            def configFile = findConfigFile(project)
            if (configFile == null)
                throw new FileNotFoundException("shield config file not found,please place a file named 'shield_config.json' to rootProject's rootdir or your custom dir")

            def content = configFile.getText()

            shieldConfig = new Gson().fromJson(content, ShieldConfig.class)

            log("shield config ---> ${shieldConfig.toString()} ")
        } catch (Throwable e) {
            e.printStackTrace()
            throw new RuntimeException("read shield config file error", e)
        }
    }

    private File findConfigFile(Project project) {

        File file = null
        if (!shieldExtension.configFile.isEmpty()) {
            file = new File(shieldExtension.configFile)
            if (file.exists()) {
                log("junkcode config file found,path=${file.path}")
                return file
            }
        }
        String path = GenUtil.pathJoin(project.rootDir.path, "modules", "plugins", "junkcode", DEFAULT_CONFIG_FILE_NAME)
        file = new File(path)
        if (file.exists()) {
            log("junkcode config file found in root dir,path=${file.path}")
            return file
        }
        return null
    }

    static void log(String name) {
        println("shield-dex >>>: " + name)
    }

    String findPackageName(ApplicationVariant variant) {
        String packageName = null
        for (int i = 0; i < variant.sourceSets.size(); i++) {
            def sourceSet = variant.sourceSets[i]
            if (sourceSet.manifestFile.exists()) {
                def parser = new XmlParser()
                Node node = parser.parse(sourceSet.manifestFile)
                packageName = node.attribute("package")
                if (packageName != null) {
                    break
                }
            }
        }
        return packageName
    }
}
