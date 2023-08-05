package org.xmlpull.v1;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/* loaded from: AXMLEditor2.jar:org/xmlpull/v1/XmlSerializer.class */
public interface XmlSerializer {
    void setFeature(String str, boolean z) throws IllegalArgumentException, IllegalStateException;

    boolean getFeature(String str);

    void setProperty(String str, Object obj) throws IllegalArgumentException, IllegalStateException;

    Object getProperty(String str);

    void setOutput(OutputStream outputStream, String str) throws IOException, IllegalArgumentException, IllegalStateException;

    void setOutput(Writer writer) throws IOException, IllegalArgumentException, IllegalStateException;

    void startDocument(String str, Boolean bool) throws IOException, IllegalArgumentException, IllegalStateException;

    void endDocument() throws IOException, IllegalArgumentException, IllegalStateException;

    void setPrefix(String str, String str2) throws IOException, IllegalArgumentException, IllegalStateException;

    String getPrefix(String str, boolean z) throws IllegalArgumentException;

    int getDepth();

    String getNamespace();

    String getName();

    XmlSerializer startTag(String str, String str2) throws IOException, IllegalArgumentException, IllegalStateException;

    XmlSerializer attribute(String str, String str2, String str3) throws IOException, IllegalArgumentException, IllegalStateException;

    XmlSerializer endTag(String str, String str2) throws IOException, IllegalArgumentException, IllegalStateException;

    XmlSerializer text(String str) throws IOException, IllegalArgumentException, IllegalStateException;

    XmlSerializer text(char[] cArr, int i, int i2) throws IOException, IllegalArgumentException, IllegalStateException;

    void cdsect(String str) throws IOException, IllegalArgumentException, IllegalStateException;

    void entityRef(String str) throws IOException, IllegalArgumentException, IllegalStateException;

    void processingInstruction(String str) throws IOException, IllegalArgumentException, IllegalStateException;

    void comment(String str) throws IOException, IllegalArgumentException, IllegalStateException;

    void docdecl(String str) throws IOException, IllegalArgumentException, IllegalStateException;

    void ignorableWhitespace(String str) throws IOException, IllegalArgumentException, IllegalStateException;

    void flush() throws IOException;
}
