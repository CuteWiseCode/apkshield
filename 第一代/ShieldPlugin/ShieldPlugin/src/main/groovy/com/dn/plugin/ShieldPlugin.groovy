package com.dn.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import com.dn.plugin.ext.ShieldExtension
import com.dn.plugin.shield.ManifestModifier
import com.dn.plugin.tasks.ShieldTask
import com.dn.plugin.utils.LogUtil
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class ShieldPlugin implements Plugin<Project> {

    def final EXT_NAME = "ashieldDex"
    def processDebugManifest = "processDebugManifest"
    def processReleaseManifest = "processReleaseManifest"
    def testtask = "packageDebug"
    boolean isModifyMeta = false

    @Override
    void apply(Project project) {

//        project.extensions.create(EXT_NAME, ShieldExtension)
        project.afterEvaluate({


            def android = project.extensions.android
            android.applicationVariants.all { variant ->
                createTask(project, variant)
                String taskName = "process${variant.name}ManifestForPackage"
//                String taskName = "package${variant.name}"
                LogUtil.log("task.name  $taskName")
                Map<Project, Set<Task>> allTasks = project.getAllTasks(true)
                for (Map.Entry<Project, Set<Task>> projectSetEntry : allTasks.entrySet()) {
                    Set<Task> value = projectSetEntry.getValue()
                    for (Task task : value) {

                        if (task.name.matches(processReleaseManifest)||task.name.matches(processDebugManifest) ) {
                            task.doFirst { t ->
                                for (File file : t.getInputs().getFiles().getFiles()) {
                                    processManifest(file)
                                }
                            }

                        }
                    }
                }
            }


        })

    }

    private void processManifest(File file){
        LogUtil.log("processManifest filepath----: " + file.absolutePath)
        if(file.isDirectory())
        {
             for(File subfile: file.listFiles())
             {
                 processManifest(subfile)
             }
        }else{
            if (!isModifyMeta && file.name == "AndroidManifest.xml") {
                isModifyMeta = true
                LogUtil.log("androidManifest file->" + file.name)
                //更新androidManifest 文件
                ManifestModifier.doModifyManifest(file, null)
            }
        }


    }

    private static void createTask(Project project, ApplicationVariant variant) {
        def variantName = variant.name.capitalize()
        def taskName = "shield${variantName}"
        if (project.tasks.findByPath(taskName) == null) {
            LogUtil.log(" taskName :$taskName")
            def task = project.task(taskName, type: ShieldTask)
            task.dependsOn "assemble${variantName}"

        }
    }


}