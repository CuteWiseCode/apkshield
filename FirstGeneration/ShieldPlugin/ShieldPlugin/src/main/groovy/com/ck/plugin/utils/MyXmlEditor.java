package com.ck.plugin.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

import cn.wjdiankong.chunk.AttributeData;
import cn.wjdiankong.chunk.StartTagChunk;
import cn.wjdiankong.chunk.StringChunk;
import cn.wjdiankong.main.ParserChunkUtils;
import cn.wjdiankong.main.Utils;

/**
 * @Author: ck
 * @Date: 2022/12/14
 * @Desc: 解析获取属性值
 */
public class MyXmlEditor {
public static String prefixStr = "http://schemas.android.com/apk/res/android";

    public MyXmlEditor() {
    }


    public static String parseAttrValue(String tag, String tagName, String attrName) {
         String attrValue ="";
        try {
            ParserChunkUtils.parserXml();
            Iterator var3 = ParserChunkUtils.xmlStruct.startTagChunkList.iterator();

            label81:
            while(true) {
                StartTagChunk chunk;
                String tagNameTmp;
                do {
                    if (!var3.hasNext()) {
                        return attrValue;
                    }

                    chunk = (StartTagChunk)var3.next();
                    int tagNameIndex = Utils.byte2int(chunk.name);
                    tagNameTmp = (String)ParserChunkUtils.xmlStruct.stringChunk.stringContentList.get(tagNameIndex);
                } while(!tag.equals(tagNameTmp));

                Iterator var7;
                AttributeData data;
                String attrNameTemp;
                int chunkSizeStart;
                if (tag.equals("application") || tag.equals("manifest")) {
                    var7 = chunk.attrList.iterator();

                    while(var7.hasNext()) {
                        data = (AttributeData)var7.next();
                        attrNameTemp = (String)ParserChunkUtils.xmlStruct.stringChunk.stringContentList.get(data.name);
                        if (attrName.equals(attrNameTemp)) {
                            attrValue = (String)ParserChunkUtils.xmlStruct.stringChunk.stringContentList.get(data.valueString);
                            System.out.println("获取到的属性值："+ attrValue);
                            return attrValue;

                        }
                    }
                }

                var7 = chunk.attrList.iterator();

                while(true) {
                    String value;
                    do {
                        do {
                            if (!var7.hasNext()) {
                                continue label81;
                            }

                            data = (AttributeData)var7.next();
                            attrNameTemp = (String)ParserChunkUtils.xmlStruct.stringChunk.stringContentList.get(data.name);
                        } while(!"name".equals(attrNameTemp));

                        value = (String)ParserChunkUtils.xmlStruct.stringChunk.stringContentList.get(data.valueString);
                    } while(!tagName.equals(value));

                    Iterator var23 = chunk.attrList.iterator();

                    while(var23.hasNext()) {
                         data = (AttributeData)var23.next();
                        String attrNameTemp1 = (String)ParserChunkUtils.xmlStruct.stringChunk.stringContentList.get(data.name);
                        if (attrName.equals(attrNameTemp1)) {
                            attrValue = (String)ParserChunkUtils.xmlStruct.stringChunk.stringContentList.get(data.valueString);
                            System.out.println("获取到的属性值1："+ attrValue);
                            return attrValue;
                        }
                    }
                }
            }
        } catch (IOException var21) {
            System.out.println("parse xml err:" + var21.toString());
        }

        return attrValue;
    }

    public static String getAttrValue(String tag, String tagName, String attrName) {
        try {
            ParserChunkUtils.parserXml();
            String attrV= parseAttrValue(tag, tagName, attrName);
            System.out.println("返回的属性值"+ attrV);
            return attrV;
        } catch (IOException var5) {
            System.out.println("parse xml err:" + var5.toString());
        }

        return "";

    }

    public static void addAttr(String tag, String tagName, String attrName, String attrValue) {
        try {
            ParserChunkUtils.parserXml();
            int[] type = getAttrType(attrValue);
            int attrname = getStrIndex(attrName);
            int attrvalue = getStrIndex(attrValue);
            int attruri = getStrIndex(prefixStr);
            int attrtype = type[0];
            int attrdata = type[1];
            AttributeData data = AttributeData.createAttribute(attruri, attrname, attrvalue, attrtype, attrdata);
            Iterator var11 = ParserChunkUtils.xmlStruct.startTagChunkList.iterator();

            while(true) {
                StartTagChunk chunk;
                String tagNameTmp;
                do {
                    if (!var11.hasNext()) {
                        return;
                    }

                    chunk = (StartTagChunk)var11.next();
                    int tagNameIndex = Utils.byte2int(chunk.name);
                    tagNameTmp = (String)ParserChunkUtils.xmlStruct.stringChunk.stringContentList.get(tagNameIndex);
                } while(!tag.equals(tagNameTmp));

                int countStart;
                byte[] modifyByte;
                if (tag.equals("application") || tag.equals("manifest")) {
                     countStart = chunk.offset + 28;
                    modifyByte = Utils.int2Byte(chunk.attrList.size() + 1);
                    ParserChunkUtils.xmlStruct.byteSrc = Utils.replaceBytes(ParserChunkUtils.xmlStruct.byteSrc, modifyByte, countStart);
                    int chunkSizeStart = chunk.offset + 4;
                    countStart = Utils.byte2int(chunk.size);
                    modifyByte = Utils.int2Byte(countStart + 20);
                    ParserChunkUtils.xmlStruct.byteSrc = Utils.replaceBytes(ParserChunkUtils.xmlStruct.byteSrc, modifyByte, chunkSizeStart);
                    ParserChunkUtils.xmlStruct.byteSrc = Utils.insertByte(ParserChunkUtils.xmlStruct.byteSrc, chunk.offset + countStart, data.getByte());
                    modifStringChunk();
                    modifyFileSize();
                    return;
                }

                Iterator var15 = chunk.attrList.iterator();

                while(var15.hasNext()) {
                    AttributeData attrData = (AttributeData)var15.next();
                    String attrNameTemp = (String)ParserChunkUtils.xmlStruct.stringChunk.stringContentList.get(attrData.name);
                    if ("name".equals(attrNameTemp)) {
                        countStart = chunk.offset + 28;
                        modifyByte = Utils.int2Byte(chunk.attrList.size() + 1);
                        ParserChunkUtils.xmlStruct.byteSrc = Utils.replaceBytes(ParserChunkUtils.xmlStruct.byteSrc, modifyByte, countStart);
                        int chunkSizeStart = chunk.offset + 4;
                        int chunkSize = Utils.byte2int(chunk.size);
                        byte[] modifyByteSize = Utils.int2Byte(chunkSize + 20);
                        ParserChunkUtils.xmlStruct.byteSrc = Utils.replaceBytes(ParserChunkUtils.xmlStruct.byteSrc, modifyByteSize, chunkSizeStart);
                        ParserChunkUtils.xmlStruct.byteSrc = Utils.insertByte(ParserChunkUtils.xmlStruct.byteSrc, chunk.offset + chunkSize, data.getByte());
                        modifStringChunk();
                        modifyFileSize();
                        return;
                    }
                }
            }
        } catch (IOException var23) {
            System.out.println("parse xml err:" + var23.toString());
        }
    }

    private static void modifStringChunk() {
        try {
            StringChunk strChunk = ParserChunkUtils.xmlStruct.stringChunk;
            byte[] newStrChunkB = strChunk.getByte(ParserChunkUtils.xmlStruct.stringChunk.stringContentList);
            ParserChunkUtils.xmlStruct.byteSrc = Utils.removeByte(ParserChunkUtils.xmlStruct.byteSrc, ParserChunkUtils.stringChunkOffset, Utils.byte2int(strChunk.size));
            ParserChunkUtils.xmlStruct.byteSrc = Utils.insertByte(ParserChunkUtils.xmlStruct.byteSrc, ParserChunkUtils.stringChunkOffset, newStrChunkB);
        } catch (IOException var2) {
            System.out.println("parse xml err:" + var2.toString());
        }

    }

    public static void modifyFileSize() {
        byte[] newFileSize = Utils.int2Byte(ParserChunkUtils.xmlStruct.byteSrc.length);
        ParserChunkUtils.xmlStruct.byteSrc = Utils.replaceBytes(ParserChunkUtils.xmlStruct.byteSrc, newFileSize, 4);
    }

    public static int getStrIndex(String str) {
        if (str != null && str.length() != 0) {
            for(int i = 0; i < ParserChunkUtils.xmlStruct.stringChunk.stringContentList.size(); ++i) {
                if (((String)ParserChunkUtils.xmlStruct.stringChunk.stringContentList.get(i)).equals(str)) {
                    return i;
                }
            }

            ParserChunkUtils.xmlStruct.stringChunk.stringContentList.add(str);
            return ParserChunkUtils.xmlStruct.stringChunk.stringContentList.size() - 1;
        } else {
            return -1;
        }
    }


    public static int[] getAttrType(String tagValue) {
        int[] result = new int[2];
        if (!tagValue.equals("true") && !tagValue.equals("false")) {
            if (!tagValue.equals("singleTask") && !tagValue.equals("standard") && !tagValue.equals("singleTop") && !tagValue.equals("singleInstance")) {
                if (!tagValue.equals("minSdkVersion") && !tagValue.equals("versionCode")) {
                    if (tagValue.startsWith("@")) {
                        result[0] |= 1;
                        result[1] = 2130706432;
                    } else if (tagValue.startsWith("#")) {
                        result[0] |= 30;
                        result[1] = -1;
                    } else {
                        result[0] |= 3;
                        result[1] = getStrIndex(tagValue);
                    }
                } else {
                    result[0] |= 16;
                    result[1] = Integer.valueOf(tagValue);
                }
            } else {
                result[0] |= 16;
                if (tagValue.equals("standard")) {
                    result[1] = 0;
                } else if (tagValue.equals("singleTop")) {
                    result[1] = 1;
                } else if (tagValue.equals("singleTask")) {
                    result[1] = 2;
                } else {
                    result[1] = 3;
                }
            }
        } else {
            result[0] |= 18;
            if (tagValue.equals("true")) {
                result[1] = 1;
            } else {
                result[1] = 0;
            }
        }

        result[0] |= 134217728;
        result[0] = Utils.byte2int(Utils.reverseBytes(Utils.int2Byte(result[0])));
        return result;
    }



    public static String getAppName( File inputFile ) {

        if (!inputFile.exists()) {
            System.out.println("输入文件不存在...");
            return "";
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
                LogUtil.log("modifyManifest bos.toByteArray() ");
                ParserChunkUtils.xmlStruct.byteSrc = bos.toByteArray();
                try {
                    fis.close();
                    bos.close();
                } catch (Exception ignored) {
                }
            } catch (Throwable th) {
                LogUtil.log("modifyManifest Throwable "+ th.getCause().toString());
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
        LogUtil.log("modifyManifest 开始解析Manifest 获取Name");
        //解析Manifest
        String tag = "application";
        String unitag = "package";
        String attr = "name";

        String attrV = getAttrValue(tag, unitag, attr);
        System.out.println("解析返回的appName :" + attrV);

        //插入meta-data
        return attrV;

    }

}
