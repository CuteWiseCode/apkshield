package cn.wjdiankong.main;

import cn.wjdiankong.chunk.AttributeData;
import cn.wjdiankong.chunk.EndTagChunk;
import cn.wjdiankong.chunk.StartTagChunk;
import cn.wjdiankong.chunk.StringChunk;
import cn.wjdiankong.chunk.TagChunk;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/* loaded from: AXMLEditor2.jar:cn/wjdiankong/main/XmlEditor.class */
public class XmlEditor {
    public static int tagEndChunkOffset;
    public static int tagStartChunkOffset = 0;
    public static int subAppTagChunkOffset = 0;
    public static int subTagChunkOffsets = 0;
    public static String[] isNotAppTag = {"uses-permission", "uses-sdk", "compatible-screens", "instrumentation", "library", "original-package", "package-verifier", "permission", "permission-group", "permission-tree", "protected-broadcast", "resource-overlay", "supports-input", "supports-screens", "upgrade-key-set", "uses-configuration", "uses-feature"};
    public static String prefixStr = "http://schemas.android.com/apk/res/android";

    public static void removeTag(String tagName, String name) {
        try {
            ParserChunkUtils.parserXml();
            Iterator<TagChunk> it = ParserChunkUtils.xmlStruct.tagChunkList.iterator();
            while (it.hasNext()) {
                TagChunk tag = it.next();
                int tagNameIndex = Utils.byte2int(tag.startTagChunk.name);
                String tagNameTmp = ParserChunkUtils.xmlStruct.stringChunk.stringContentList.get(tagNameIndex);
                if (tagName.equals(tagNameTmp)) {
                    Iterator<AttributeData> it2 = tag.startTagChunk.attrList.iterator();
                    while (it2.hasNext()) {
                        AttributeData attrData = it2.next();
                        String attrName = ParserChunkUtils.xmlStruct.stringChunk.stringContentList.get(attrData.name);
                        if ("name".equals(attrName)) {
                            String value = ParserChunkUtils.xmlStruct.stringChunk.stringContentList.get(attrData.valueString);
                            if (name.equals(value)) {
                                int size = Utils.byte2int(tag.endTagChunk.size);
                                int delStart = tag.startTagChunk.offset;
                                int delSize = (tag.endTagChunk.offset - tag.startTagChunk.offset) + size;
                                ParserChunkUtils.xmlStruct.byteSrc = Utils.removeByte(ParserChunkUtils.xmlStruct.byteSrc, delStart, delSize);
                                modifyFileSize();
                                return;
                            }
                        }
                    }
                    continue;
                }
            }
        } catch (IOException e) {
            System.out.println("parse xml err:" + e.toString());
        }
    }

    public static void addTag(String insertXml) {
        try {
            ParserChunkUtils.parserXml();
            XmlPullParserFactory pullParserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser pullParser = pullParserFactory.newPullParser();
            pullParser.setInput(new FileInputStream(insertXml), "UTF-8");
            for (int event = pullParser.getEventType(); event != 1; event = pullParser.next()) {
                switch (event) {
                    case 2:
                        String tagName = pullParser.getName();
                        int name = getStrIndex(tagName);
                        int attCount = pullParser.getAttributeCount();
                        byte[] attribute = new byte[20 * attCount];
                        for (int i = 0; i < pullParser.getAttributeCount(); i++) {
                            int attruri = getStrIndex(prefixStr);
                            String attrName = pullParser.getAttributeName(i);
                            String[] strAry = attrName.split(":");
                            int[] type = getAttrType(pullParser.getAttributeValue(i));
                            int attrname = getStrIndex(strAry[1]);
                            int attrvalue = getStrIndex(pullParser.getAttributeValue(i));
                            int attrtype = type[0];
                            int attrdata = type[1];
                            AttributeData data = AttributeData.createAttribute(attruri, attrname, attrvalue, attrtype, attrdata);
                            attribute = Utils.byteConcat(attribute, data.getByte(), data.getLen() * i);
                        }
                        StartTagChunk startChunk = StartTagChunk.createChunk(name, attCount, -1, attribute);
                        if (isNotAppTag(tagName)) {
                            ParserChunkUtils.xmlStruct.byteSrc = Utils.insertByte(ParserChunkUtils.xmlStruct.byteSrc, subTagChunkOffsets, startChunk.getChunkByte());
                            subTagChunkOffsets += startChunk.getChunkByte().length;
                            break;
                        } else {
                            ParserChunkUtils.xmlStruct.byteSrc = Utils.insertByte(ParserChunkUtils.xmlStruct.byteSrc, subAppTagChunkOffset, startChunk.getChunkByte());
                            subAppTagChunkOffset += startChunk.getChunkByte().length;
                            break;
                        }
                    case 3:
                        String tagName2 = pullParser.getName();
                        int name2 = getStrIndex(tagName2);
                        EndTagChunk endChunk = EndTagChunk.createChunk(name2);
                        if (isNotAppTag(tagName2)) {
                            ParserChunkUtils.xmlStruct.byteSrc = Utils.insertByte(ParserChunkUtils.xmlStruct.byteSrc, subTagChunkOffsets, endChunk.getChunkByte());
                            subTagChunkOffsets += endChunk.getChunkByte().length;
                            break;
                        } else {
                            ParserChunkUtils.xmlStruct.byteSrc = Utils.insertByte(ParserChunkUtils.xmlStruct.byteSrc, subAppTagChunkOffset, endChunk.getChunkByte());
                            subAppTagChunkOffset += endChunk.getChunkByte().length;
                            break;
                        }
                }
            }
        } catch (IOException e) {
            System.out.println("parse xml err:" + e.toString());
        } catch (XmlPullParserException e2) {
            System.out.println("parse xml err:" + e2.toString());
        }
        modifStringChunk();
        modifyFileSize();
    }

    public static void removeAttr(String tag, String tagName, String attrName) {
        try {
            ParserChunkUtils.parserXml();
            Iterator<StartTagChunk> it = ParserChunkUtils.xmlStruct.startTagChunkList.iterator();
            while (it.hasNext()) {
                StartTagChunk chunk = it.next();
                int tagNameIndex = Utils.byte2int(chunk.name);
                String tagNameTmp = ParserChunkUtils.xmlStruct.stringChunk.stringContentList.get(tagNameIndex);
                if (tag.equals(tagNameTmp)) {
                    if (tag.equals("application") || tag.equals("manifest")) {
                        Iterator<AttributeData> it2 = chunk.attrList.iterator();
                        while (it2.hasNext()) {
                            AttributeData data = it2.next();
                            String attrNameTemp1 = ParserChunkUtils.xmlStruct.stringChunk.stringContentList.get(data.name);
                            if (attrName.equals(attrNameTemp1)) {
                                if (chunk.attrList.size() == 1) {
                                    removeTag(tag, tagName);
                                    return;
                                }
                                int countStart = chunk.offset + 28;
                                byte[] modifyByte = Utils.int2Byte(chunk.attrList.size() - 1);
                                ParserChunkUtils.xmlStruct.byteSrc = Utils.replaceBytes(ParserChunkUtils.xmlStruct.byteSrc, modifyByte, countStart);
                                int chunkSizeStart = chunk.offset + 4;
                                int chunkSize = Utils.byte2int(chunk.size);
                                byte[] modifyByteSize = Utils.int2Byte(chunkSize - 20);
                                ParserChunkUtils.xmlStruct.byteSrc = Utils.replaceBytes(ParserChunkUtils.xmlStruct.byteSrc, modifyByteSize, chunkSizeStart);
                                int delStart = data.offset;
                                int delSize = data.getLen();
                                ParserChunkUtils.xmlStruct.byteSrc = Utils.removeByte(ParserChunkUtils.xmlStruct.byteSrc, delStart, delSize);
                                modifyFileSize();
                                return;
                            }
                        }
                    }
                    Iterator<AttributeData> it3 = chunk.attrList.iterator();
                    while (it3.hasNext()) {
                        AttributeData attrData = it3.next();
                        String attrNameTemp = ParserChunkUtils.xmlStruct.stringChunk.stringContentList.get(attrData.name);
                        if ("name".equals(attrNameTemp)) {
                            String value = ParserChunkUtils.xmlStruct.stringChunk.stringContentList.get(attrData.valueString);
                            if (tagName.equals(value)) {
                                Iterator<AttributeData> it4 = chunk.attrList.iterator();
                                while (it4.hasNext()) {
                                    AttributeData data2 = it4.next();
                                    String attrNameTemp12 = ParserChunkUtils.xmlStruct.stringChunk.stringContentList.get(data2.name);
                                    if (attrName.equals(attrNameTemp12)) {
                                        if (chunk.attrList.size() == 1) {
                                            removeTag(tag, tagName);
                                            return;
                                        }
                                        int countStart2 = chunk.offset + 28;
                                        byte[] modifyByte2 = Utils.int2Byte(chunk.attrList.size() - 1);
                                        ParserChunkUtils.xmlStruct.byteSrc = Utils.replaceBytes(ParserChunkUtils.xmlStruct.byteSrc, modifyByte2, countStart2);
                                        int chunkSizeStart2 = chunk.offset + 4;
                                        int chunkSize2 = Utils.byte2int(chunk.size);
                                        byte[] modifyByteSize2 = Utils.int2Byte(chunkSize2 - 20);
                                        ParserChunkUtils.xmlStruct.byteSrc = Utils.replaceBytes(ParserChunkUtils.xmlStruct.byteSrc, modifyByteSize2, chunkSizeStart2);
                                        int delStart2 = data2.offset;
                                        int delSize2 = data2.getLen();
                                        ParserChunkUtils.xmlStruct.byteSrc = Utils.removeByte(ParserChunkUtils.xmlStruct.byteSrc, delStart2, delSize2);
                                        modifyFileSize();
                                        return;
                                    }
                                }
                                continue;
                            } else {
                                continue;
                            }
                        }
                    }
                    continue;
                }
            }
        } catch (IOException e) {
            System.out.println("parse xml err:" + e.toString());
        }
    }

    public static void modifyAttr(String tag, String tagName, String attrName, String attrValue) {
        try {
            ParserChunkUtils.parserXml();
            removeAttr(tag, tagName, attrName);
            ParserChunkUtils.parserXml();
            addAttr(tag, tagName, attrName, attrValue);
        } catch (IOException e) {
            System.out.println("parse xml err:" + e.toString());
        }
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
            Iterator<StartTagChunk> it = ParserChunkUtils.xmlStruct.startTagChunkList.iterator();
            while (it.hasNext()) {
                StartTagChunk chunk = it.next();
                int tagNameIndex = Utils.byte2int(chunk.name);
                String tagNameTmp = ParserChunkUtils.xmlStruct.stringChunk.stringContentList.get(tagNameIndex);
                if (tag.equals(tagNameTmp)) {
                    if (tag.equals("application") || tag.equals("manifest")) {
                        int countStart = chunk.offset + 28;
                        byte[] modifyByte = Utils.int2Byte(chunk.attrList.size() + 1);
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
                    Iterator<AttributeData> it2 = chunk.attrList.iterator();
                    while (it2.hasNext()) {
                        AttributeData attrData = it2.next();
                        String attrNameTemp = ParserChunkUtils.xmlStruct.stringChunk.stringContentList.get(attrData.name);
                        if ("name".equals(attrNameTemp)) {
                            int countStart2 = chunk.offset + 28;
                            byte[] modifyByte2 = Utils.int2Byte(chunk.attrList.size() + 1);
                            ParserChunkUtils.xmlStruct.byteSrc = Utils.replaceBytes(ParserChunkUtils.xmlStruct.byteSrc, modifyByte2, countStart2);
                            int chunkSizeStart2 = chunk.offset + 4;
                            int chunkSize2 = Utils.byte2int(chunk.size);
                            byte[] modifyByteSize2 = Utils.int2Byte(chunkSize2 + 20);
                            ParserChunkUtils.xmlStruct.byteSrc = Utils.replaceBytes(ParserChunkUtils.xmlStruct.byteSrc, modifyByteSize2, chunkSizeStart2);
                            ParserChunkUtils.xmlStruct.byteSrc = Utils.insertByte(ParserChunkUtils.xmlStruct.byteSrc, chunk.offset + chunkSize2, data.getByte());
                            modifStringChunk();
                            modifyFileSize();
                            return;
                        }
                    }
                    continue;
                }
            }
        } catch (IOException e) {
            System.out.println("parse xml err:" + e.toString());
        }
    }

    private static void modifStringChunk() {
        try {
            StringChunk strChunk = ParserChunkUtils.xmlStruct.stringChunk;
            byte[] newStrChunkB = strChunk.getByte(ParserChunkUtils.xmlStruct.stringChunk.stringContentList);
            ParserChunkUtils.xmlStruct.byteSrc = Utils.removeByte(ParserChunkUtils.xmlStruct.byteSrc, ParserChunkUtils.stringChunkOffset, Utils.byte2int(strChunk.size));
            ParserChunkUtils.xmlStruct.byteSrc = Utils.insertByte(ParserChunkUtils.xmlStruct.byteSrc, ParserChunkUtils.stringChunkOffset, newStrChunkB);
        } catch (IOException e) {
            System.out.println("parse xml err:" + e.toString());
        }
    }

    public static void modifyFileSize() {
        byte[] newFileSize = Utils.int2Byte(ParserChunkUtils.xmlStruct.byteSrc.length);
        ParserChunkUtils.xmlStruct.byteSrc = Utils.replaceBytes(ParserChunkUtils.xmlStruct.byteSrc, newFileSize, 4);
    }

    public static int getStrIndex(String str) {
        if (str == null || str.length() == 0) {
            return -1;
        }
        for (int i = 0; i < ParserChunkUtils.xmlStruct.stringChunk.stringContentList.size(); i++) {
            if (ParserChunkUtils.xmlStruct.stringChunk.stringContentList.get(i).equals(str)) {
                return i;
            }
        }
        ParserChunkUtils.xmlStruct.stringChunk.stringContentList.add(str);
        return ParserChunkUtils.xmlStruct.stringChunk.stringContentList.size() - 1;
    }

    public static boolean isNotAppTag(String tagName) {
        String[] strArr;
        for (String str : isNotAppTag) {
            if (str.equals(tagName)) {
                return true;
            }
        }
        return false;
    }

    public static int[] getAttrType(String tagValue) {
        int[] result = new int[2];
        if (tagValue.equals("true") || tagValue.equals("false")) {
            result[0] = result[0] | 18;
            if (tagValue.equals("true")) {
                result[1] = 1;
            } else {
                result[1] = 0;
            }
        } else if (tagValue.equals("singleTask") || tagValue.equals("standard") || tagValue.equals("singleTop") || tagValue.equals("singleInstance")) {
            result[0] = result[0] | 16;
            if (tagValue.equals("standard")) {
                result[1] = 0;
            } else if (tagValue.equals("singleTop")) {
                result[1] = 1;
            } else if (tagValue.equals("singleTask")) {
                result[1] = 2;
            } else {
                result[1] = 3;
            }
        } else if (tagValue.equals("minSdkVersion") || tagValue.equals("versionCode")) {
            result[0] = result[0] | 16;
            result[1] = Integer.valueOf(tagValue).intValue();
        } else if (tagValue.startsWith("@")) {
            result[0] = result[0] | 1;
            result[1] = 2130706432;
        } else if (tagValue.startsWith("#")) {
            result[0] = result[0] | 30;
            result[1] = -1;
        } else {
            result[0] = result[0] | 3;
            result[1] = getStrIndex(tagValue);
        }
        result[0] = result[0] | 134217728;
        result[0] = Utils.byte2int(Utils.reverseBytes(Utils.int2Byte(result[0])));
        return result;
    }
}
