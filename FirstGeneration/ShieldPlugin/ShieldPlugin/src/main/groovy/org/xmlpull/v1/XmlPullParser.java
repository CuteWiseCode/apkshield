package org.xmlpull.v1;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/* loaded from: AXMLEditor2.jar:org/xmlpull/v1/XmlPullParser.class */
public interface XmlPullParser {
    public static final String NO_NAMESPACE = "";
    public static final int START_DOCUMENT = 0;
    public static final int END_DOCUMENT = 1;
    public static final int START_TAG = 2;
    public static final int END_TAG = 3;
    public static final int TEXT = 4;
    public static final int CDSECT = 5;
    public static final int ENTITY_REF = 6;
    public static final int IGNORABLE_WHITESPACE = 7;
    public static final int PROCESSING_INSTRUCTION = 8;
    public static final int COMMENT = 9;
    public static final int DOCDECL = 10;
    public static final String[] TYPES = {"START_DOCUMENT", "END_DOCUMENT", "START_TAG", "END_TAG", "TEXT", "CDSECT", "ENTITY_REF", "IGNORABLE_WHITESPACE", "PROCESSING_INSTRUCTION", "COMMENT", "DOCDECL"};
    public static final String FEATURE_PROCESS_NAMESPACES = "http://xmlpull.org/v1/doc/features.html#process-namespaces";
    public static final String FEATURE_REPORT_NAMESPACE_ATTRIBUTES = "http://xmlpull.org/v1/doc/features.html#report-namespace-prefixes";
    public static final String FEATURE_PROCESS_DOCDECL = "http://xmlpull.org/v1/doc/features.html#process-docdecl";
    public static final String FEATURE_VALIDATION = "http://xmlpull.org/v1/doc/features.html#validation";

    void setFeature(String str, boolean z) throws XmlPullParserException;

    boolean getFeature(String str);

    void setProperty(String str, Object obj) throws XmlPullParserException;

    Object getProperty(String str);

    void setInput(Reader reader) throws XmlPullParserException;

    void setInput(InputStream inputStream, String str) throws XmlPullParserException;

    String getInputEncoding();

    void defineEntityReplacementText(String str, String str2) throws XmlPullParserException;

    int getNamespaceCount(int i) throws XmlPullParserException;

    String getNamespacePrefix(int i) throws XmlPullParserException;

    String getNamespaceUri(int i) throws XmlPullParserException;

    String getNamespace(String str);

    int getDepth();

    String getPositionDescription();

    int getLineNumber();

    int getColumnNumber();

    boolean isWhitespace() throws XmlPullParserException;

    String getText();

    char[] getTextCharacters(int[] iArr);

    String getNamespace();

    String getName();

    String getPrefix();

    boolean isEmptyElementTag() throws XmlPullParserException;

    int getAttributeCount();

    String getAttributeNamespace(int i);

    String getAttributeName(int i);

    String getAttributePrefix(int i);

    String getAttributeType(int i);

    boolean isAttributeDefault(int i);

    String getAttributeValue(int i);

    String getAttributeValue(String str, String str2);

    int getEventType() throws XmlPullParserException;

    int next() throws XmlPullParserException, IOException;

    int nextToken() throws XmlPullParserException, IOException;

    void require(int i, String str, String str2) throws XmlPullParserException, IOException;

    String nextText() throws XmlPullParserException, IOException;

    int nextTag() throws XmlPullParserException, IOException;
}
