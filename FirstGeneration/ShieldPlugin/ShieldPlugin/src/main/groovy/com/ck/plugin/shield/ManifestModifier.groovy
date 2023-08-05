package com.ck.plugin.shield

import cn.wjdiankong.main.Main
import com.ck.plugin.utils.LogUtil
import com.ck.plugin.utils.MyXmlEditor
import com.wind.meditor.core.FileProcesser
import com.wind.meditor.property.AttributeItem
import com.wind.meditor.property.ModificationProperty
import com.wind.meditor.utils.NodeValue
import groovy.xml.XmlUtil
import org.gradle.api.Project;

/**
 * @Author: ck
 * @Date: 2022/12/6
 * @Desc: 修改Manifest.xml 文件
 */
class ManifestModifier {

    /**
     *
     * @param file
     * @param project
     */
    static void doModifyManifest(File xmlFile, Project project) {
        LogUtil.log("new XmlParser ->" )
        def xmlparser = new XmlParser(false, false)
        LogUtil.log("parse ->" )
        def rootNode = xmlparser.parse(xmlFile.path)
        LogUtil.log("rootNode ->" + rootNode)
        if (rootNode == null)
            return

        // 获取application  节点
        Node appNode = rootNode.children().find { ((Node) it).name() == "application" }
        //判断获取 android:name 属性 , TODO 在合适的时机调用原 application
        def appName = appNode.attribute("android:name")
        if (appName != null) {
            //将appName 插入application 节点下的 meta-data 属性中
            Map map = new HashMap()
            map.put("android:name", "ApplicationName")
            map.put("android:value", appName)
            Node node = new Node(null, "meta-data", map)
            appNode.children().add(node)
            appNode.attributes().replace("android:name", "com.stub.StubApp")
            LogUtil.log("完成meta-data添加属性")
        } else {
            appNode.attributes().put("android:name", "com.stub.StubApp")
            LogUtil.log("为application 添加 name")
        }

        def xmlText = XmlUtil.serialize(rootNode)
        xmlFile.withWriter { writer ->
            writer.write(xmlText)
        }
    }
    /**
     * 处理编译过的AndroidManifest.xml 文件
     * @param xmlFile
     * @param project
     */
    static void modifyManifest(File xmlFile) {
        //1. 获取Manifest 中的appName
        String appName = MyXmlEditor.getAppName(xmlFile)
        LogUtil.log("modifyManifest name " + appName)

        //2. 添加meta-data 节点
        //3.修改替换AppName
        ModificationProperty property = new ModificationProperty();
        property.addMetaData(new ModificationProperty.MetaData("ApplicationName", appName))
                .addApplicationAttribute(new AttributeItem(NodeValue.Application.NAME, "com.stub.StubApp"))
        LogUtil.log("modifyManifest property config" )

        String inputManifestFilePath = xmlFile.absolutePath
        String outputManifestFilePath = "${xmlFile.getParentFile().getParent()}${File.separator}AndroidManifesto.xml"
        File outFile = new File(outputManifestFilePath)
        if(!outFile.exists())
        {
            outFile.createNewFile()
        }

        // 处理manifest文件方法
        FileProcesser.processManifestFile(inputManifestFilePath, outputManifestFilePath, property);

        FileOutputStream fos1 = new FileOutputStream(inputManifestFilePath)
        File ins1 = new File(outputManifestFilePath)
        byte[] fbytes1 = Utils.getBytes(ins1)
        fos1.write(fbytes1)
        fos1.flush()
        fos1.close()


        File xmlVer = new File("${xmlFile.getParent()}${File.separator}XMLPULL_1_1_3_4c_VERSION")
        if(xmlVer.exists())
            xmlVer.delete()

    }

    /**
     * 处理编译过的AndroidManifest.xml 文件
     * @param xmlFile
     * @param project
     */
    static void modifyManifest1(File xmlFile) {
        //1. 获取Manifest 中的appName
        String appName = MyXmlEditor.getAppName(xmlFile)
        LogUtil.log("modifyManifest name " + appName)

        //2. 添加meta-data 节点
        String out = "${xmlFile.getParent()}${File.separator}AndroidManifesto.xml"

        //创建插入节点文件
        File insertXmlFile = new File("${xmlFile.getParentFile().getParent()}${File.separator}insert.xml")
        if (!insertXmlFile.exists()) {
            insertXmlFile.createNewFile()
        }
        String content = "<meta-data android:name=\"ApplicationName\" android:value=\"${appName}\" />"
        createFileByString(insertXmlFile, content)
        LogUtil.log("modifyManifest  createFileByString 完成")
        String[] args = ["-tag", "-i", insertXmlFile.absolutePath, xmlFile.absolutePath, out]
        Main.main(args)
        LogUtil.log("modifyManifest  添加meta-data 节点 完成")

        //3.修改替换AppName
        String tag = "application"
        String unitag = "package"
        String attr = "name"
        String attrV = "com.stub.StubApp"
        args = ["-attr", "-m", tag, unitag, attr, attrV, out, xmlFile.absolutePath]
        Main.main(args)
        LogUtil.log("modifyManifest  修改替换AppName 完成")

        //删除辅助文件
//        File vFile = new File("${xmlFile.getParent()}${File.separator}XMLPULL_1_1_3_4c_VERSION")
//        if (vFile.exists())
//            vFile.delete()
        File outFile = new File(out)
        if (outFile.exists())
            outFile.delete()
        if (insertXmlFile.exists())
            insertXmlFile.delete()


    }


    static void createFileByString(File file, String content) {
        //在文本文本中追加内容
        BufferedWriter out = null;
        try {
            LogUtil.log("modifyManifest  createFileByString")
            //FileOutputStream(file, true),第二个参数为true是追加内容，false是覆盖
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)))
            out.write(content)
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            try {
                if (out != null) {
                    out.close()
                }
            } catch (IOException e) {
                e.printStackTrace()
            }
        }

    }

}
