package cn.wjdiankong.chunk;

import cn.wjdiankong.main.ChunkTypeNumber;
import cn.wjdiankong.main.Utils;

/* loaded from: AXMLEditor2.jar:cn/wjdiankong/chunk/EndTagChunk.class */
public class EndTagChunk implements Chunk {
    public byte[] type;
    public byte[] size;
    public byte[] lineNumber;
    public byte[] unknown;
    public byte[] uri;
    public byte[] name = new byte[4];
    public int offset;
    public String tagValue;

    public EndTagChunk() {
        this.type = new byte[4];
        this.size = new byte[4];
        this.lineNumber = new byte[4];
        this.unknown = new byte[4];
        this.uri = new byte[4];
        this.type = Utils.int2Byte(ChunkTypeNumber.CHUNK_ENDTAG);
        this.size = Utils.int2Byte(24);
        this.lineNumber = new byte[4];
        this.unknown = new byte[4];
        this.uri = Utils.int2Byte(-1);
    }

    public static EndTagChunk createChunk(int name) {
        EndTagChunk chunk = new EndTagChunk();
        chunk.name = Utils.int2Byte(name);
        return chunk;
    }

    @Override // cn.wjdiankong.chunk.Chunk
    public byte[] getChunkByte() {
        byte[] bytes = new byte[getLen()];
        return Utils.byteConcat(Utils.byteConcat(Utils.byteConcat(Utils.byteConcat(Utils.byteConcat(Utils.byteConcat(bytes, this.type, 0), this.size, 4), this.lineNumber, 8), this.unknown, 12), this.uri, 16), this.name, 20);
    }

    public int getLen() {
        return this.type.length + this.size.length + this.lineNumber.length + this.unknown.length + this.uri.length + this.name.length;
    }

    public static EndTagChunk createChunk(byte[] byteSrc, int offset) {
        EndTagChunk chunk = new EndTagChunk();
        chunk.offset = offset;
        chunk.type = Utils.copyByte(byteSrc, 0, 4);
        chunk.size = Utils.copyByte(byteSrc, 4, 4);
        chunk.lineNumber = Utils.copyByte(byteSrc, 8, 4);
        chunk.unknown = Utils.copyByte(byteSrc, 12, 4);
        chunk.uri = Utils.copyByte(byteSrc, 16, 4);
        chunk.name = Utils.copyByte(byteSrc, 20, 4);
        return chunk;
    }
}
