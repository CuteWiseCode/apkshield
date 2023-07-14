package cn.wjdiankong.chunk;

import cn.wjdiankong.main.Utils;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;

/* loaded from: AXMLEditor2.jar:cn/wjdiankong/chunk/StringChunk.class */
public class StringChunk {
    public byte[] type;
    public byte[] size;
    public byte[] strCount;
    public byte[] styleCount;
    public byte[] unknown;
    public byte[] strPoolOffset;
    public byte[] stylePoolOffset;
    public byte[] strOffsets;
    public byte[] styleOffsets;
    public byte[] strPool;
    public byte[] stylePool;
    public ArrayList<String> stringContentList;

    public byte[] getByte(ArrayList<String> strList) throws UnsupportedEncodingException {
        byte[] strB = getStrListByte(strList);
        byte[] src = new byte[0];
        byte[] src2 = Utils.addByte(Utils.addByte(Utils.addByte(Utils.addByte(Utils.addByte(Utils.addByte(Utils.addByte(src, this.type), this.size), Utils.int2Byte(strList.size())), this.styleCount), this.unknown), this.strPoolOffset), this.stylePoolOffset);
        byte[] strOffsets = new byte[0];
        ArrayList<byte[]> convertList = convertStrList(strList);
        int len = 0;
        for (int i = 0; i < convertList.size(); i++) {
            strOffsets = Utils.addByte(strOffsets, Utils.int2Byte(len));
            len += convertList.get(i).length + 4;
        }
        byte[] src3 = Utils.addByte(src2, strOffsets);
        int newStyleOffsets = src3.length;
        byte[] src4 = Utils.addByte(src3, this.styleOffsets);
        int newStringPools = src4.length;
        byte[] src5 = Utils.addByte(Utils.addByte(src4, strB), this.stylePool);
        if (this.styleOffsets != null && this.styleOffsets.length > 0) {
            src5 = Utils.replaceBytes(src5, Utils.int2Byte(newStyleOffsets), 28 + (strList.size() * 4));
        }
        byte[] src6 = Utils.replaceBytes(src5, Utils.int2Byte(newStringPools), 20);
        if (src6.length % 4 != 0) {
            src6 = Utils.addByte(src6, new byte[]{0, 0});
        }
        return Utils.replaceBytes(src6, Utils.int2Byte(src6.length), 4);
    }

    public int getLen() {
        return this.type.length + this.size.length + this.strCount.length + this.styleCount.length + this.unknown.length + this.strPoolOffset.length + this.stylePoolOffset.length + this.strOffsets.length + this.styleOffsets.length + this.strPool.length + this.stylePool.length;
    }

    public static StringChunk createChunk(byte[] byteSrc, int stringChunkOffset) throws UnsupportedEncodingException {
        StringChunk chunk = new StringChunk();
        chunk.type = Utils.copyByte(byteSrc, 0 + stringChunkOffset, 4);
        chunk.size = Utils.copyByte(byteSrc, 4 + stringChunkOffset, 4);
        int chunkSize = Utils.byte2int(chunk.size);
        chunk.strCount = Utils.copyByte(byteSrc, 8 + stringChunkOffset, 4);
        int chunkStringCount = Utils.byte2int(chunk.strCount);
        chunk.stringContentList = new ArrayList<>(chunkStringCount);
        chunk.styleCount = Utils.copyByte(byteSrc, 12 + stringChunkOffset, 4);
        int chunkStyleCount = Utils.byte2int(chunk.styleCount);
        chunk.unknown = Utils.copyByte(byteSrc, 16 + stringChunkOffset, 4);
        chunk.strPoolOffset = Utils.copyByte(byteSrc, 20 + stringChunkOffset, 4);
        chunk.stylePoolOffset = Utils.copyByte(byteSrc, 24 + stringChunkOffset, 4);
        chunk.strOffsets = Utils.copyByte(byteSrc, 28 + stringChunkOffset, 4 * chunkStringCount);
        chunk.styleOffsets = Utils.copyByte(byteSrc, 28 + stringChunkOffset + (4 * chunkStringCount), 4 * chunkStyleCount);
        int stringContentStart = stringChunkOffset + Utils.byte2int(chunk.strPoolOffset);
        int contentLen = chunkSize - Utils.byte2int(chunk.strPoolOffset);
        byte[] chunkStringContentByte = Utils.copyByte(byteSrc, stringContentStart, contentLen);
        int i = 0;
        while (true) {
            int endStringIndex = i;
            if (chunk.stringContentList.size() < chunkStringCount) {
                int stringSize = Utils.byte2Short(Utils.copyByte(chunkStringContentByte, endStringIndex, 2)) * 2;
                byte[] temp = stringSize > 0 ? Utils.copyByte(chunkStringContentByte, endStringIndex + 2, stringSize) : new byte[0];
                String str = new String(temp, "UTF-16LE");
                chunk.stringContentList.add(str);
                i = endStringIndex + 2 + stringSize + 2;
            } else {
                chunk.strPool = Utils.copyByte(chunkStringContentByte, 0, endStringIndex);
                chunk.stylePool = Utils.copyByte(chunkStringContentByte, endStringIndex, contentLen - endStringIndex);
                return chunk;
            }
        }
    }

    private byte[] getStrListByte(ArrayList<String> strList) throws UnsupportedEncodingException {
        byte[] src = new byte[0];
        ArrayList<byte[]> stringContentListInBytes = convertStrList(strList);
        for (int i = 0; i < stringContentListInBytes.size(); i++) {
            byte[] tempAry = new byte[0];
            short len = (short) (stringContentListInBytes.get(i).length / 2);
            byte[] lenAry = Utils.shortToByte(len);
            src = Utils.addByte(src, Utils.addByte(Utils.addByte(Utils.addByte(tempAry, lenAry), stringContentListInBytes.get(i)), new byte[]{0, 0}));
        }
        return src;
    }

    private ArrayList<byte[]> convertStrList(ArrayList<String> stringContentList) throws UnsupportedEncodingException {
        ArrayList<byte[]> destList = new ArrayList<>(stringContentList.size());
        Iterator<String> it = stringContentList.iterator();
        while (it.hasNext()) {
            String str = it.next();
            byte[] temp = str.getBytes("UTF-16LE");
            destList.add(temp);
        }
        return destList;
    }
}
