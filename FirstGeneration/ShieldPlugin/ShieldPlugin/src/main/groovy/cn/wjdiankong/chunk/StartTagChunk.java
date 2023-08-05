package cn.wjdiankong.chunk;

import cn.wjdiankong.main.ChunkTypeNumber;
import cn.wjdiankong.main.Utils;
import java.util.ArrayList;

/* loaded from: AXMLEditor2.jar:cn/wjdiankong/chunk/StartTagChunk.class */
public class StartTagChunk implements Chunk {
    public byte[] size;
    public byte[] uri;
    public byte[] name;
    public byte[] flag;
    public byte[] attCount;
    public byte[] attribute;
    public ArrayList<AttributeData> attrList;
    public int offset;
    public byte[] type = Utils.int2Byte(ChunkTypeNumber.CHUNK_STARTTAG);
    public byte[] lineNumber = new byte[4];
    public byte[] unknown = new byte[4];
    public byte[] classAttr = new byte[4];

    public StartTagChunk() {
        this.flag = new byte[4];
        int flatInt = 0 | 1310740;
        this.flag = Utils.int2Byte(flatInt);
    }

    @Override // cn.wjdiankong.chunk.Chunk
    public byte[] getChunkByte() {
        byte[] bytes = new byte[getLen()];
        return Utils.byteConcat(Utils.byteConcat(Utils.byteConcat(Utils.byteConcat(Utils.byteConcat(Utils.byteConcat(Utils.byteConcat(Utils.byteConcat(Utils.byteConcat(Utils.byteConcat(bytes, this.type, 0), this.size, 4), this.lineNumber, 8), this.unknown, 12), this.uri, 16), this.name, 20), this.flag, 24), this.attCount, 28), this.classAttr, 32), this.attribute, 36);
    }

    public int getLen() {
        return this.type.length + this.size.length + this.lineNumber.length + this.unknown.length + this.uri.length + this.name.length + this.flag.length + this.attCount.length + this.classAttr.length + this.attribute.length;
    }

    public static StartTagChunk createChunk(int name, int attCount, int uri, byte[] attribute) {
        StartTagChunk chunk = new StartTagChunk();
        chunk.size = new byte[4];
        chunk.name = Utils.int2Byte(name);
        chunk.uri = Utils.int2Byte(uri);
        chunk.attCount = Utils.int2Byte(attCount);
        chunk.attribute = attribute;
        chunk.size = Utils.int2Byte(chunk.getLen());
        return chunk;
    }

    public static StartTagChunk createChunk(byte[] byteSrc, int offset) {
        StartTagChunk chunk = new StartTagChunk();
        chunk.offset = offset;
        chunk.type = Utils.copyByte(byteSrc, 0, 4);
        chunk.size = Utils.copyByte(byteSrc, 4, 4);
        chunk.lineNumber = Utils.copyByte(byteSrc, 8, 4);
        chunk.unknown = Utils.copyByte(byteSrc, 12, 4);
        chunk.uri = Utils.copyByte(byteSrc, 16, 4);
        chunk.name = Utils.copyByte(byteSrc, 20, 4);
        chunk.flag = Utils.copyByte(byteSrc, 24, 4);
        chunk.attCount = Utils.copyByte(byteSrc, 28, 4);
        int attrCount = Utils.byte2int(chunk.attCount);
        chunk.classAttr = Utils.copyByte(byteSrc, 32, 4);
        chunk.attribute = Utils.copyByte(byteSrc, 36, attrCount * 20);
        chunk.attrList = new ArrayList<>(attrCount);
        for (int i = 0; i < attrCount; i++) {
            Integer[] values = new Integer[5];
            AttributeData attrData = new AttributeData();
            for (int j = 0; j < 5; j++) {
                int value = Utils.byte2int(Utils.copyByte(byteSrc, 36 + (i * 20) + (j * 4), 4));
                attrData.offset = offset + 36 + (i * 20);
                switch (j) {
                    case 0:
                        attrData.nameSpaceUri = value;
                        break;
                    case 1:
                        attrData.name = value;
                        break;
                    case 2:
                        attrData.valueString = value;
                        break;
                    case 3:
                        value >>= 24;
                        attrData.type = value;
                        break;
                    case 4:
                        attrData.data = value;
                        break;
                }
                values[j] = Integer.valueOf(value);
            }
            chunk.attrList.add(attrData);
        }
        return chunk;
    }
}
