package com.qihoo.util;

/**
 * @Author: hzh
 * @Date: 2023/1/6
 * @Desc: java类作用描述
 */
public class NativeUtil {

    /**
     * 关闭dex2oat
     * @return
     */
    public static native String stringFromJNI(int addr,boolean enable);


//    public static native int unsealNative(int targetSdkVersion);
}
