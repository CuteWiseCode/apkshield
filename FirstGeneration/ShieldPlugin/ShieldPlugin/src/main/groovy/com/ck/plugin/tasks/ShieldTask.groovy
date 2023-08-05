package com.ck.plugin.tasks

import com.ck.plugin.ext.BuildInfo
import com.ck.plugin.ext.InputParam
import com.ck.plugin.ext.PathNotExist
import com.ck.plugin.ext.ShieldExtension
import com.ck.plugin.utils.LogUtil
import com.google.gson.Gson
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskAction
import com.ck.plugin.Main

/**
 * The configuration properties.
 *
 * @author ck
 */
class ShieldTask extends DefaultTask {
    ShieldExtension configuration
    def android
    def buildConfigs = []

    ShieldTask() {
        description = 'shield APK'
        group = 'shield'
        outputs.upToDateWhen { false }
        android = project.extensions.android
        loadConfig(project)


        android.applicationVariants.all { variant ->
            variant.outputs.each { output ->
                // remove "shield"
                String variantName = this.name["shield".length()..-1]
                if (variantName.equalsIgnoreCase(variant.buildType.name as String)) {

                    def outputFile = null
                    try {
                        if (variant.metaClass.respondsTo(variant, "getPackageApplicationProvider")) {
                            outputFile = new File(variant.packageApplicationProvider.get().outputDirectory, output.outputFileName)
                        }
                    } catch (Exception ignore) {
                        // no-op
                    } finally {
                        outputFile = outputFile ?: output.outputFile
                    }

                    def variantInfo
                    if (variant.variantData.hasProperty("variantConfiguration")) {
                        variantInfo = variant.variantData.variantConfiguration
                    } else {
                        variantInfo = variant.variantData.variantDslInfo
                    }

                    def applicationId = variantInfo.applicationId instanceof Property
                            ? variantInfo.applicationId.get()
                            : variantInfo.applicationId

                    buildConfigs << new BuildInfo(
                            outputFile,
                            variantInfo.signingConfig,
                            applicationId,
                            variant.buildType.name,
                            variant.productFlavors,
                            variantName,
                            variant.mergedFlavor.minSdkVersion.apiLevel,
                            variant.mergedFlavor.targetSdkVersion.apiLevel,
                    )

                }
            }
        }
        if (!project.plugins.hasPlugin('com.android.application')) {
            throw new GradleException('generateShieldApk: Android Application plugin required')
        }
    }


    static useFolder(file) {
        //remove .apk from filename
        def fileName = file.name[0..-5]
        return "${file.parent}/Shield_${fileName}/"
    }

    def getZipAlignPath() {
        return "${android.getSdkDirectory().getAbsolutePath()}/build-tools/${android.buildToolsVersion}/zipalign"
    }

    @TaskAction
    run() {

        LogUtil.log("[Shield] configuartion:$configuration")
        LogUtil.log("[Shield] BuildConfigs:$buildConfigs")

        buildConfigs.each { config ->

            if (config.file == null || !config.file.exists()) {
                throw new PathNotExist("Original APK not existed")
            }
            RunGradleTask(config, config.file.getAbsolutePath(), config.minSDKVersion, config.targetSDKVersion)

        }
    }

    def RunGradleTask(config, String absPath, int minSDKVersion, int targetSDKVersion) {
        def signConfig = config.signConfig
        String packageName = config.packageName

        LogUtil.log("packageName:$packageName")
        InputParam.Builder builder = new InputParam.Builder()
                .setMappingFile(configuration.mappingFile)
                .setUse7zip(configuration.use7zip)
                .setMetaName(configuration.metaName)
                .setFixedResName(configuration.fixedResName)
                .setKeepRoot(configuration.keepRoot)
                .setMergeDuplicatedRes(configuration.mergeDuplicatedRes)
                .setCompressFilePattern(configuration.compressFilePattern)
                .setZipAlign(getZipAlignPath())
                .setOutBuilder(useFolder(config.file))
                .setApkPath(absPath)
                .setUseSign(configuration.useSign)
                .setDigestAlg(configuration.digestalg)
                .setMinSDKVersion(minSDKVersion)
                .setTargetSDKVersion(targetSDKVersion)

        if (configuration.finalApkBackupPath != null && configuration.finalApkBackupPath.length() > 0) {
            builder.setFinalApkBackupPath(configuration.finalApkBackupPath)
        } else {
            builder.setFinalApkBackupPath(absPath)
        }

        if (configuration.useSign) {
            if (signConfig == null) {
                throw new GradleException("can't the get signConfig for release build")
            }
            builder.setSignFile(signConfig.storeFile)
                    .setKeypass(signConfig.keyPassword)
                    .setStorealias(signConfig.keyAlias)
                    .setStorepass(signConfig.storePassword)
            if (signConfig.hasProperty('v3SigningEnabled') && signConfig.v3SigningEnabled) {
                builder.setSignatureType(InputParam.SignatureType.SchemaV3)
            } else if (signConfig.hasProperty('v2SigningEnabled') && signConfig.v2SigningEnabled) {
                builder.setSignatureType(InputParam.SignatureType.SchemaV2)
            }
        }
        InputParam inputParam = builder.create()
        Main.gradleRun(inputParam, project)
    }


    void loadConfig(Project project) {
        try {
            def configFile = new File("${project.rootProject.projectDir}/modules/plugins/shield/shield_config.json")
            if (configFile == null)
                throw new FileNotFoundException("shield_config  file not found,plzz place a file named 'shield_config.json' under rootProject's rootdir/modules/plugins/shield")

            def content = configFile.getText()

            configuration = new Gson().fromJson(content, ShieldExtension.class)
        } catch (Throwable e) {
            throw new RuntimeException("read shield_config file error", e)
        }
    }
}