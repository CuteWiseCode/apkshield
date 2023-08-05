package cn.wjdiankong.main;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;

/* loaded from: AXMLEditor2.jar:cn/wjdiankong/main/Utils.class */
public class Utils {
    public static byte[] byteConcat(byte[] src, byte[] subB, int start) {
        for (int i = 0; i < subB.length; i++) {
            src[i + start] = subB[i];
        }
        return src;
    }

    public static int byte2int(byte[] res) {
        int targets = (res[0] & 255) | ((res[1] << 8) & 65280) | ((res[2] << 24) >>> 8) | (res[3] << 24);
        return targets;
    }

    public static byte[] int2Byte(int value) {
        byte[] src = {(byte) (value & 255), (byte) ((value >> 8) & 255), (byte) ((value >> 16) & 255), (byte) ((value >> 24) & 255)};
        return src;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static byte[] shortToByte(short number) {
        int temp = number;
        byte[] b = new byte[2];
        for (int i = 0; i < b.length; i++) {
            b[i] = new Integer(temp & 255).byteValue();
            temp >>= 8;
        }
        return b;
    }

    public static short byte2Short(byte[] b) {
        short s0 = (short) (b[0] & 255);
        short s1 = (short) (b[1] & 255);
        short s = (short) (s0 | ((short) (s1 << 8)));
        return s;
    }

    public static String bytesToHexString(byte[] src1) {
        byte[] src = reverseBytes(src1);
        StringBuilder stringBuilder = new StringBuilder(XmlPullParser.NO_NAMESPACE);
        if (src == null || src.length <= 0) {
            return null;
        }
        for (byte b : src) {
            int v = b & 255;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv + " ");
        }
        return stringBuilder.toString();
    }

    public static char[] getChars(byte[] bytes) {
        Charset cs = Charset.forName("UTF-8");
        ByteBuffer bb = ByteBuffer.allocate(bytes.length);
        bb.put(bytes);
        bb.flip();
        CharBuffer cb = cs.decode(bb);
        return cb.array();
    }

    public static byte[] addByte(byte[] src, byte[] add) {
        if (src == null) {
            return null;
        }
        if (add == null) {
            return src;
        }
        byte[] newsrc = new byte[src.length + add.length];
        for (int i = 0; i < src.length; i++) {
            newsrc[i] = src[i];
        }
        for (int i2 = src.length; i2 < newsrc.length; i2++) {
            newsrc[i2] = add[i2 - src.length];
        }
        return newsrc;
    }

    public static byte[] insertByte(byte[] src, int start, byte[] insertB) {
        if (src == null || start > src.length) {
            return null;
        }
        byte[] newB = new byte[src.length + insertB.length];
        for (int i = 0; i < start; i++) {
            newB[i] = src[i];
        }
        for (int i2 = 0; i2 < insertB.length; i2++) {
            newB[i2 + start] = insertB[i2];
        }
        for (int i3 = start; i3 < src.length; i3++) {
            newB[i3 + insertB.length] = src[i3];
        }
        return newB;
    }

    public static byte[] removeByte(byte[] src, int start, int len) {
        if (src == null || start > src.length || start + len > src.length || start < 0 || len <= 0) {
            return null;
        }
        byte[] dest = new byte[src.length - len];
        for (int i = 0; i <= start; i++) {
            dest[i] = src[i];
        }
        int k = 0;
        for (int i2 = start + len; i2 < src.length; i2++) {
            dest[start + k] = src[i2];
            k++;
        }
        return dest;
    }

    public static byte[] copyByte(byte[] src, int start, int len) {
        if (src == null || start > src.length || start + len > src.length || start < 0 || len <= 0) {
            return null;
        }
        byte[] resultByte = new byte[len];
        for (int i = 0; i < len; i++) {
            resultByte[i] = src[i + start];
        }
        return resultByte;
    }

    public static byte[] replaceBytes(byte[] src, byte[] bytes, int start) {
        if (src == null) {
            return null;
        }
        if (bytes == null) {
            return src;
        }
        if (start > src.length) {
            return src;
        }
        if (start + bytes.length > src.length) {
            return src;
        }
        byte[] replaceB = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            replaceB[i] = src[i + start];
            src[i + start] = bytes[i];
        }
        return src;
    }

    public static byte[] reverseBytes(byte[] bytess) {
        byte[] bytes = new byte[bytess.length];
        for (int i = 0; i < bytess.length; i++) {
            bytes[i] = bytess[i];
        }
        if (bytes == null || bytes.length % 2 != 0) {
            return bytes;
        }
        int len = bytes.length;
        for (int i2 = 0; i2 < len / 2; i2++) {
            byte tmp = bytes[i2];
            bytes[i2] = bytes[(len - i2) - 1];
            bytes[(len - i2) - 1] = tmp;
        }
        return bytes;
    }

    public static String filterStringNull(String str) {
        if (str == null || str.length() == 0) {
            return str;
        }
        byte[] strByte = str.getBytes();
        ArrayList<Byte> newByte = new ArrayList<>();
        for (int i = 0; i < strByte.length; i++) {
            if (strByte[i] != 0) {
                newByte.add(Byte.valueOf(strByte[i]));
            }
        }
        byte[] newByteAry = new byte[newByte.size()];
        for (int i2 = 0; i2 < newByteAry.length; i2++) {
            newByteAry[i2] = newByte.get(i2).byteValue();
        }
        return new String(newByteAry);
    }

    public static String getStringFromByteAry(byte[] srcByte, int start) {
        if (srcByte == null) {
            return XmlPullParser.NO_NAMESPACE;
        }
        if (start < 0) {
            return XmlPullParser.NO_NAMESPACE;
        }
        if (start >= srcByte.length) {
            return XmlPullParser.NO_NAMESPACE;
        }
        byte val = srcByte[start];
        int i = 1;
        ArrayList<Byte> byteList = new ArrayList<>();
        while (val != 0) {
            byteList.add(Byte.valueOf(srcByte[start + i]));
            val = srcByte[start + i];
            i++;
        }
        byte[] valAry = new byte[byteList.size()];
        for (int j = 0; j < byteList.size(); j++) {
            valAry[j] = byteList.get(j).byteValue();
        }
        try {
            return new String(valAry, "UTF-8");
        } catch (Exception e) {
            System.out.println("encode error:" + e.toString());
            return XmlPullParser.NO_NAMESPACE;
        }
    }
}
