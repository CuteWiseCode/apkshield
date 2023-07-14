package cn.wjdiankong.main;

import cn.wjdiankong.chunk.EndNameSpaceChunk;
import cn.wjdiankong.chunk.EndTagChunk;
import cn.wjdiankong.chunk.ResourceChunk;
import cn.wjdiankong.chunk.StartNameSpaceChunk;
import cn.wjdiankong.chunk.StartTagChunk;
import cn.wjdiankong.chunk.StringChunk;
import cn.wjdiankong.chunk.TagChunk;
import cn.wjdiankong.chunk.TextChunk;
import cn.wjdiankong.chunk.XmlStruct;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/* loaded from: AXMLEditor2.jar:cn/wjdiankong/main/ParserChunkUtils.class */
public class ParserChunkUtils {
    public static int resourceChunkOffset;
    public static int nextChunkOffset;
    public static int stringChunkOffset = 8;
    public static XmlStruct xmlStruct = new XmlStruct();
    public static boolean isApplication = false;
    public static boolean isManifest = false;
    public static List<TagChunk> tagChunkList = new ArrayList();

    public static void clear() {
        resourceChunkOffset = 0;
        nextChunkOffset = 0;
        isApplication = false;
        isManifest = false;
        tagChunkList.clear();
        xmlStruct.clear();
    }

    public static void parserXml() throws UnsupportedEncodingException {
        clear();
        parserXmlHeader(xmlStruct.byteSrc);
        parserStringChunk(xmlStruct.byteSrc);
        parserResourceChunk(xmlStruct.byteSrc);
        parserXmlContent(xmlStruct.byteSrc);
    }

    public static void parserXmlHeader(byte[] byteSrc) {
        byte[] xmlMagic = Utils.copyByte(byteSrc, 0, 4);
        byte[] xmlSize = Utils.copyByte(byteSrc, 4, 4);
        xmlStruct.magicNumber = xmlMagic;
        xmlStruct.fileSize = xmlSize;
    }

    public static void parserStringChunk(byte[] byteSrc) throws UnsupportedEncodingException {
        xmlStruct.stringChunk = StringChunk.createChunk(byteSrc, stringChunkOffset);
        byte[] chunkSizeByte = Utils.copyByte(byteSrc, 12, 4);
        resourceChunkOffset = stringChunkOffset + Utils.byte2int(chunkSizeByte);
    }

    public static void parserResourceChunk(byte[] byteSrc) {
        xmlStruct.resChunk = ResourceChunk.createChunk(byteSrc, resourceChunkOffset);
        byte[] chunkSizeByte = Utils.copyByte(byteSrc, resourceChunkOffset + 4, 4);
        int chunkSize = Utils.byte2int(chunkSizeByte);
        nextChunkOffset = resourceChunkOffset + chunkSize;
        XmlEditor.tagStartChunkOffset = nextChunkOffset;
    }

    public static void parserStartNamespaceChunk(byte[] byteSrc) {
        xmlStruct.startNamespaceChunk = StartNameSpaceChunk.createChunk(byteSrc);
    }

    public static void parserEndNamespaceChunk(byte[] byteSrc) {
        xmlStruct.endNamespaceChunk = EndNameSpaceChunk.createChunk(byteSrc);
    }

    public static void parserStartTagChunk(byte[] byteSrc, int offset) {
        StartTagChunk tagChunk = StartTagChunk.createChunk(byteSrc, offset);
        xmlStruct.startTagChunkList.add(tagChunk);
        TagChunk chunk = new TagChunk();
        chunk.startTagChunk = tagChunk;
        tagChunkList.add(chunk);
        byte[] tagNameByte = Utils.copyByte(byteSrc, 20, 4);
        int tagNameIndex = Utils.byte2int(tagNameByte);
        String tagName = xmlStruct.stringChunk.stringContentList.get(tagNameIndex);
        if (tagName.equals("application")) {
            isApplication = true;
        }
    }

    public static void parserEndTagChunk(byte[] byteSrc, int offset) {
        EndTagChunk tagChunk = EndTagChunk.createChunk(byteSrc, offset);
        TagChunk chunk = tagChunkList.remove(tagChunkList.size() - 1);
        chunk.endTagChunk = tagChunk;
        xmlStruct.endTagChunkList.add(tagChunk);
        xmlStruct.tagChunkList.add(chunk);
    }

    public static void parserTextChunk(byte[] byteSrc) {
        xmlStruct.textChunkList.add(TextChunk.createChunk(byteSrc));
    }

    public static void parserXmlContent(byte[] byteSrc) {
        while (!isEnd(byteSrc.length)) {
            byte[] chunkTagByte = Utils.copyByte(byteSrc, nextChunkOffset, 4);
            byte[] chunkSizeByte = Utils.copyByte(byteSrc, nextChunkOffset + 4, 4);
            int chunkTag = Utils.byte2int(chunkTagByte);
            int chunkSize = Utils.byte2int(chunkSizeByte);
            switch (chunkTag) {
                case ChunkTypeNumber.CHUNK_STARTNS /* 1048832 */:
                    parserStartNamespaceChunk(Utils.copyByte(byteSrc, nextChunkOffset, chunkSize));
                    isManifest = true;
                    break;
                case ChunkTypeNumber.CHUNK_ENDNS /* 1048833 */:
                    parserEndNamespaceChunk(Utils.copyByte(byteSrc, nextChunkOffset, chunkSize));
                    break;
                case ChunkTypeNumber.CHUNK_STARTTAG /* 1048834 */:
                    parserStartTagChunk(Utils.copyByte(byteSrc, nextChunkOffset, chunkSize), nextChunkOffset);
                    if (isApplication) {
                        XmlEditor.subAppTagChunkOffset = nextChunkOffset + chunkSize;
                        isApplication = false;
                    }
                    if (!isManifest) {
                        break;
                    } else {
                        XmlEditor.subTagChunkOffsets = nextChunkOffset + chunkSize;
                        isManifest = false;
                        break;
                    }
                case ChunkTypeNumber.CHUNK_ENDTAG /* 1048835 */:
                    parserEndTagChunk(Utils.copyByte(byteSrc, nextChunkOffset, chunkSize), nextChunkOffset);
                    break;
            }
            nextChunkOffset += chunkSize;
        }
    }

    public static boolean isEnd(int totalLen) {
        return nextChunkOffset >= totalLen;
    }
}
