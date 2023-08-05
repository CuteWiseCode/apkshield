package cn.wjdiankong.chunk;

import java.util.ArrayList;

/* loaded from: AXMLEditor2.jar:cn/wjdiankong/chunk/XmlStruct.class */
public class XmlStruct {
    public byte[] byteSrc;
    public byte[] magicNumber;
    public byte[] fileSize;
    public StringChunk stringChunk;
    public ResourceChunk resChunk;
    public StartNameSpaceChunk startNamespaceChunk;
    public EndNameSpaceChunk endNamespaceChunk;
    public ArrayList<StartTagChunk> startTagChunkList = new ArrayList<>();
    public ArrayList<EndTagChunk> endTagChunkList = new ArrayList<>();
    public ArrayList<TextChunk> textChunkList = new ArrayList<>();
    public ArrayList<TagChunk> tagChunkList = new ArrayList<>();

    public void clear() {
        this.magicNumber = null;
        this.fileSize = null;
        this.stringChunk = null;
        this.resChunk = null;
        this.startNamespaceChunk = null;
        this.endNamespaceChunk = null;
        this.startTagChunkList.clear();
        this.endTagChunkList.clear();
        this.textChunkList.clear();
        this.tagChunkList.clear();
    }
}
