package com.ck.shieldplugin;

import org.junit.Test;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

import cn.wjdiankong.main.ParserChunkUtils;

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
    public void test() {
        //插入meta-data
//        String input = "E:\\androidWorkSpace\\ShieldPlugin\\files\\AndroidManifest.xml";
//        String out = "E:\\androidWorkSpace\\ShieldPlugin\\files\\AndroidManifesto.xml";
//        String insertXml = "E:\\androidWorkSpace\\ShieldPlugin\\files\\insert.xml";
//
////        MyXmlEditor.addTag(insertXml);
//        String args[] ={"-tag","-i",insertXml,input,out};
//        Main.main(args);

        parseManifest();



    }


    private void parseManifest() {

        File inputFile = new File("E:\\androidWorkSpace\\ShieldPlugin\\files\\AndroidManifest.xml");
        File outputFile = new File("E:\\androidWorkSpace\\ShieldPlugin\\files\\AndroidManifesto.xml");
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
                } catch (Exception ignored) {
                }
            } catch (Throwable th) {
                try {
                    fis.close();
                    bos.close();
                } catch (Exception ignored) {
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

        //解析Manifest
          String tag = "application";
          String unitag = "package";
          String attr = "name";

          String attrV = MyXmlEditor.getAttrValue(tag, unitag, attr);
           System.out.println("解析返回的appName :"+attrV);

        //插入meta-data

//        if (!outputFile.exists()) {
//            outputFile.delete();
//        }
//        FileOutputStream fos = null;
//        try {
//            fos = new FileOutputStream(outputFile);
//            fos.write(ParserChunkUtils.xmlStruct.byteSrc);
//            fos.close();
//            if (fos == null) {
//                return;
//            }
//            try {
//                fos.close();
//            } catch (IOException e5) {
//                e5.printStackTrace();
//            }
//        } catch (Exception e6) {
//            if (fos == null) {
//                return;
//            }
//            try {
//                fos.close();
//            } catch (IOException e7) {
//                e7.printStackTrace();
//            }
//        } catch (Throwable th2) {
//            if (fos != null) {
//                try {
//                    fos.close();
//                } catch (IOException e8) {
//                    e8.printStackTrace();
//                }
//            }
//            throw th2;
//        }

    }


}