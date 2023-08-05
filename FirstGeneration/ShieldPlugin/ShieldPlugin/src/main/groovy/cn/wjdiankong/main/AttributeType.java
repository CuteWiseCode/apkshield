package cn.wjdiankong.main;

import cn.wjdiankong.chunk.AttributeData;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;

/* loaded from: AXMLEditor2.jar:cn/wjdiankong/main/AttributeType.class */
public class AttributeType {
    public static final int ATTR_NULL = 0;
    public static final int ATTR_REFERENCE = 1;
    public static final int ATTR_ATTRIBUTE = 2;
    public static final int ATTR_STRING = 3;
    public static final int ATTR_FLOAT = 4;
    public static final int ATTR_DIMENSION = 5;
    public static final int ATTR_FRACTION = 6;
    public static final int ATTR_FIRSTINT = 16;
    public static final int ATTR_HEX = 17;
    public static final int ATTR_BOOLEAN = 18;
    public static final int ATTR_FIRSTCOLOR = 28;
    public static final int ATTR_RGB8 = 29;
    public static final int ATTR_ARGB4 = 30;
    public static final int ATTR_RGB4 = 31;
    public static final int ATTR_LASTCOLOR = 31;
    public static final int ATTR_LASTINT = 31;
    public static final int COMPLEX_UNIT_PX = 0;
    public static final int COMPLEX_UNIT_DIP = 1;
    public static final int COMPLEX_UNIT_SP = 2;
    public static final int COMPLEX_UNIT_PT = 3;
    public static final int COMPLEX_UNIT_IN = 4;
    public static final int COMPLEX_UNIT_MM = 5;
    public static final int COMPLEX_UNIT_SHIFT = 0;
    public static final int COMPLEX_UNIT_MASK = 15;
    public static final int COMPLEX_UNIT_FRACTION = 0;
    public static final int COMPLEX_UNIT_FRACTION_PARENT = 1;
    public static final int COMPLEX_RADIX_23p0 = 0;
    public static final int COMPLEX_RADIX_16p7 = 1;
    public static final int COMPLEX_RADIX_8p15 = 2;
    public static final int COMPLEX_RADIX_0p23 = 3;
    public static final int COMPLEX_RADIX_SHIFT = 4;
    public static final int COMPLEX_RADIX_MASK = 3;
    public static final int COMPLEX_MANTISSA_SHIFT = 8;
    public static final int COMPLEX_MANTISSA_MASK = 16777215;
    private static final float[] RADIX_MULTS = {0.00390625f, 3.051758E-5f, 1.192093E-7f, 4.656613E-10f};
    private static final String[] DIMENSION_UNITS = {"px", "dip", "sp", "pt", "in", "mm", XmlPullParser.NO_NAMESPACE, XmlPullParser.NO_NAMESPACE};
    private static final String[] FRACTION_UNITS = {"%", "%p", XmlPullParser.NO_NAMESPACE, XmlPullParser.NO_NAMESPACE, XmlPullParser.NO_NAMESPACE, XmlPullParser.NO_NAMESPACE, XmlPullParser.NO_NAMESPACE, XmlPullParser.NO_NAMESPACE};
    public static HashMap<Integer, String> typeMap0 = new HashMap<>();
    public static HashMap<String, Integer> typeMap1 = new HashMap<>();

    public static String getAttributeData(AttributeData data) {
        if (data.type == 3) {
            return ParserChunkUtils.xmlStruct.stringChunk.stringContentList.get(data.data);
        }
        if (data.type == 2) {
            return String.format("?%s%08X", getPackage(data.data), Integer.valueOf(data.data));
        }
        if (data.type == 1) {
            return String.format("@%s%08X", getPackage(data.data), Integer.valueOf(data.data));
        }
        if (data.type == 4) {
            return String.valueOf(Float.intBitsToFloat(data.data));
        }
        if (data.type == 17) {
            return String.format("0x%08X", Integer.valueOf(data.data));
        }
        if (data.type == 18) {
            return data.data != 0 ? "true" : "false";
        } else if (data.type == 5) {
            return Float.toString(complexToFloat(data.data)) + DIMENSION_UNITS[data.data & 15];
        } else {
            if (data.type == 6) {
                return Float.toString(complexToFloat(data.data)) + FRACTION_UNITS[data.data & 15];
            }
            if (data.type >= 28 && data.type <= 31) {
                return String.format("#%08X", Integer.valueOf(data.data));
            }
            if (data.type >= 16 && data.type <= 31) {
                return String.valueOf(data.data);
            }
            return String.format("<0x%X, type 0x%02X>", Integer.valueOf(data.data), Integer.valueOf(data.type));
        }
    }

    private static String getPackage(int id) {
        if ((id >>> 24) == 1) {
            return "android:";
        }
        return XmlPullParser.NO_NAMESPACE;
    }

    public static float complexToFloat(int complex) {
        return (complex & (-256)) * RADIX_MULTS[(complex >> 4) & 3];
    }

    static {
        typeMap0.put(0, "ATTR_NULL");
        typeMap0.put(1, "ATTR_REFERENCE");
        typeMap0.put(2, "ATTR_ATTRIBUTE");
        typeMap0.put(3, "ATTR_STRING");
        typeMap0.put(4, "ATTR_FLOAT");
        typeMap0.put(5, "ATTR_DIMENSION");
        typeMap0.put(6, "ATTR_FRACTION");
        typeMap0.put(16, "ATTR_FIRSTINT");
        typeMap0.put(17, "ATTR_HEX");
        typeMap0.put(18, "ATTR_BOOLEAN");
        typeMap0.put(28, "ATTR_FIRSTCOLOR");
        typeMap0.put(29, "ATTR_RGB8");
        typeMap0.put(30, "ATTR_ARGB4");
        typeMap0.put(31, "ATTR_RGB4");
        for (Integer key : typeMap0.keySet()) {
            typeMap1.put(typeMap0.get(key), key);
        }
    }

    public static String getAttrType(int type) {
        return typeMap0.get(Integer.valueOf(type));
    }

    public static Integer getAttrType(String type) {
        return typeMap1.get(type);
    }
}
