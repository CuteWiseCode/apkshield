package cn.wjdiankong.chunk;

import cn.wjdiankong.main.ParserChunkUtils;
import cn.wjdiankong.main.Utils;
import org.xmlpull.v1.XmlPullParser;

/* loaded from: AXMLEditor2.jar:cn/wjdiankong/chunk/AttributeData.class */
public class AttributeData {
    public int nameSpaceUri;
    public int name;
    public int valueString;
    public int type = 0;
    public int data = 0;
    public byte[] nameSpaceUriB;
    public byte[] nameB;
    public byte[] valueStringB;
    public byte[] typeB;
    public byte[] dataB;
    public int offset;

    public int getLen() {
        return 20;
    }

    public static AttributeData createAttribute(byte[] src) {
        AttributeData data = new AttributeData();
        data.nameSpaceUriB = Utils.copyByte(src, 0, 4);
        data.nameB = Utils.copyByte(src, 4, 4);
        data.valueStringB = Utils.copyByte(src, 8, 4);
        data.typeB = Utils.copyByte(src, 12, 4);
        data.dataB = Utils.copyByte(src, 16, 4);
        return data;
    }

    public byte[] getByte() {
        byte[] bytes = new byte[20];
        Utils.byteConcat(bytes, this.nameSpaceUriB, 0);
        Utils.byteConcat(bytes, this.nameB, 4);
        Utils.byteConcat(bytes, this.valueStringB, 8);
        Utils.byteConcat(bytes, this.typeB, 12);
        Utils.byteConcat(bytes, this.dataB, 16);
        return bytes;
    }

    public static AttributeData createAttribute(int uri, int name, int value, int type, int data1) {
        AttributeData data = new AttributeData();
        data.nameSpaceUriB = Utils.int2Byte(uri);
        data.nameB = Utils.int2Byte(name);
        data.valueStringB = Utils.int2Byte(value);
        data.typeB = Utils.int2Byte(type);
        data.dataB = Utils.int2Byte(data1);
        return data;
    }

    public String getNameSpaceUri() {
        if (this.nameSpaceUri < 0) {
            return XmlPullParser.NO_NAMESPACE;
        }
        return ParserChunkUtils.xmlStruct.stringChunk.stringContentList.get(this.nameSpaceUri);
    }

    public String getName() {
        if (this.name < 0) {
            return XmlPullParser.NO_NAMESPACE;
        }
        return ParserChunkUtils.xmlStruct.stringChunk.stringContentList.get(this.name);
    }

    public String getData() {
        if (this.data < 0) {
            return XmlPullParser.NO_NAMESPACE;
        }
        return ParserChunkUtils.xmlStruct.stringChunk.stringContentList.get(this.data);
    }
}
