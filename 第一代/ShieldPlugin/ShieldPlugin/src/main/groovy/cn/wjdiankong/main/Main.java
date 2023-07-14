package cn.wjdiankong.main;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/* loaded from: AXMLEditor2.jar:cn/wjdiankong/main/Main.class */
public class Main {
    private static final String CMD_TXT = "[usage java -jar AXMLEditor2.jar [-tag|-attr] [-i|-r|-m] [标签名|标签唯一ID|属性名|属性值] [输入文件|输出文件]\n举例说明：\n  1>插入属性：application的标签中插入android:debuggable=\"true\"属性，让程序处于可调式状态\n    java -jar AXMLEditor2.jar -attr -i application package debuggable true AndroidManifest.xml AndroidManifest_out.xml\n  2>删除属性：application标签中删除allowBackup属性，这样此app就可以进行沙盒数据备份\n    java -jar AXMLEditor2.jar -attr -r application allowBackup AndroidManifest.xml AndroidManifest_out.xml\n  3>更改属性：application的标签中修改android:debuggable=\"true\"属性，让程序处于可调式状态\n    java -jar AXMLEditor2.jar -attr -m application package debuggable true AndroidManifest.xml AndroidManifest_out.xml\n  4>插入标签：因为插入标签时一个标签内容比较多，所以命令方式不方便，而是输入一个需要插入标签内容的xml文件即可。\n    java -jar AXMLEditor2.jar -tag -i [insert.xml] AndroidManifest.xml AndroidManifest_out.xml\n  5>删除标签：删除android:name=\"cn.wjdiankong.demo.MainActivity\"的标签内容\n    java -jar AXMLEditor2.jar -tag -r activity cn.wjdiankong.demo.MainActivity AndroidManifest.xml AndroidManifest_out.xml";

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("参数有误...");
            System.out.println(CMD_TXT);
            return;
        }
        String inputfile = args[args.length - 2];
        String outputfile = args[args.length - 1];
        File inputFile = new File(inputfile);
        File outputFile = new File(outputfile);
        if (!inputFile.exists()) {
            System.out.println("输入文件不存在...");
            return;
        }
        FileInputStream fis = null;
        ByteArrayOutputStream bos = null;
        try {
            try {
                fis = new FileInputStream(inputFile);
                bos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                while (true) {
                    int len = fis.read(buffer);
                    if (len == -1) {
                        break;
                    }
                    bos.write(buffer, 0, len);
                }
                ParserChunkUtils.xmlStruct.byteSrc = bos.toByteArray();
                try {
                    fis.close();
                    bos.close();
                } catch (Exception e) {
                }
            } catch (Throwable th) {
                try {
                    fis.close();
                    bos.close();
                } catch (Exception e2) {
                }
                throw th;
            }
        } catch (Exception e3) {
            System.out.println("parse xml error:" + e3.toString());
            try {
                fis.close();
                bos.close();
            } catch (Exception e4) {
            }
        }
        doCommand(args);
        if (!outputFile.exists()) {
            outputFile.delete();
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(outputFile);
            fos.write(ParserChunkUtils.xmlStruct.byteSrc);
            fos.close();
            if (fos == null) {
                return;
            }
            try {
                fos.close();
            } catch (IOException e5) {
                e5.printStackTrace();
            }
        } catch (Exception e6) {
            if (fos == null) {
                return;
            }
            try {
                fos.close();
            } catch (IOException e7) {
                e7.printStackTrace();
            }
        } catch (Throwable th2) {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e8) {
                    e8.printStackTrace();
                }
            }
            throw th2;
        }
    }

    public static void testDemo() {
    }

    public static void doCommand(String[] args) {
        if ("-tag".equals(args[0])) {
            if (args.length < 2) {
                System.out.println("缺少参数...");
                System.out.println(CMD_TXT);
            } else if ("-i".equals(args[1])) {
                if (args.length < 3) {
                    System.out.println("缺少参数...");
                    System.out.println(CMD_TXT);
                    return;
                }
                String insertXml = args[2];
                File file = new File(insertXml);
                if (!file.exists()) {
                    System.out.println("插入标签xml文件不存在...");
                    return;
                }
                XmlEditor.addTag(insertXml);
                System.out.println("插入标签完成...");
            } else if ("-r".equals(args[1])) {
                if (args.length < 4) {
                    System.out.println("缺少参数...");
                    System.out.println(CMD_TXT);
                    return;
                }
                String tag = args[2];
                String tagName = args[3];
                XmlEditor.removeTag(tag, tagName);
                System.out.println("删除标签完成...");
            } else {
                System.out.println("操作标签参数有误...");
                System.out.println(CMD_TXT);
            }
        } else if ("-attr".equals(args[0])) {
            if (args.length < 2) {
                System.out.println("缺少参数...");
                System.out.println(CMD_TXT);
            } else if ("-i".equals(args[1])) {
                if (args.length < 6) {
                    System.out.println("缺少参数...");
                    System.out.println(CMD_TXT);
                    return;
                }
                String tag2 = args[2];
                String tagName2 = args[3];
                String attr = args[4];
                String value = args[5];
                XmlEditor.addAttr(tag2, tagName2, attr, value);
                System.out.println("插入属性完成...");
            } else if ("-r".equals(args[1])) {
                if (args.length < 5) {
                    System.out.println("缺少参数...");
                    System.out.println(CMD_TXT);
                    return;
                }
                String tag3 = args[2];
                String tagName3 = args[3];
                String attr2 = args[4];
                XmlEditor.removeAttr(tag3, tagName3, attr2);
                System.out.println("删除属性完成...");
            } else if ("-m".equals(args[1])) {
                if (args.length < 6) {
                    System.out.println("缺少参数...");
                    System.out.println(CMD_TXT);
                    return;
                }
                String tag4 = args[2];
                String tagName4 = args[3];
                String attr3 = args[4];
                String value2 = args[5];
                XmlEditor.modifyAttr(tag4, tagName4, attr3, value2);
                System.out.println("修改属性完成...");
            } else {
                System.out.println("操作属性参数有误...");
                System.out.println(CMD_TXT);
            }
        }
    }
}
