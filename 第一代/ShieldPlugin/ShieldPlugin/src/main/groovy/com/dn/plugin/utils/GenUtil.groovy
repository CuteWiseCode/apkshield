package com.dn.plugin.utils

import com.dn.plugin.ShieldTransform
import org.gradle.api.Project

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

/**
 * @Author: hzh
 * @Date: 2022/11/23
 * @Desc: java类作用描述
 */
class GenUtil {

     static String pathJoin(String... p) {
         return p.join(File.separator)
     }

    /**
     * 生成对应类的 proguard
     */
   static void generateProgurdRules(Project mTProject, String obsClassName) {

        String path = pathJoin(mTProject.rootDir.path, "modules", "plugins", "junkcode", "junkcode.pro")
        File profile = new File(path)
        if (!profile.exists()) {
            try {
                profile.createNewFile()
            } catch (Throwable e) {
                e.printStackTrace()
            }
        }


        Files.write(Paths.get(profile.getPath()), obsClassName.getBytes(), StandardOpenOption.APPEND)
        ShieldTransform.log(obsClassName)
    }
}
