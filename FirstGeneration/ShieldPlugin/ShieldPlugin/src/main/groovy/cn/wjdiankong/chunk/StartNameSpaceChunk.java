package cn.wjdiankong.chunk;

import cn.wjdiankong.main.Utils;

/* loaded from: AXMLEditor2.jar:cn/wjdiankong/chunk/StartNameSpaceChunk.class */
public class StartNameSpaceChunk {
    public byte[] type = new byte[4];
    public byte[] size = new byte[4];
    public byte[] lineNumber = new byte[4];
    public byte[] unknown = new byte[4];
    public byte[] prefix = new byte[4];
    public byte[] uri = new byte[4];

    public static StartNameSpaceChunk createChunk(byte[] byteSrc) {
        StartNameSpaceChunk chunk = new StartNameSpaceChunk();
        chunk.type = Utils.copyByte(byteSrc, 0, 4);
        chunk.size = Utils.copyByte(byteSrc, 4, 4);
        chunk.lineNumber = Utils.copyByte(byteSrc, 8, 4);
        chunk.unknown = Utils.copyByte(byteSrc, 12, 4);
        chunk.prefix = Utils.copyByte(byteSrc, 16, 4);
        chunk.uri = Utils.copyByte(byteSrc, 20, 4);
        return chunk;
    }
}
