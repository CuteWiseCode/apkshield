package com.qihoo.util;

import android.os.Build;

import java.lang.reflect.Method;

/**
 * @Desc: 获取指令集，当c++寻址失败，则调用此工具来获取
 */
public class StructSetUtil {
    private static Integer currentInstructionSet = null;

    enum InstructionSet {
        kNone(0),
        kArm(1),
        kArm64(2),
        kThumb2(3),
        kX86(4),
        kX86_64(5),
        kMips(6),
        kMips64(7),
        kLast(8);

        private int instructionSet;

        InstructionSet(int instructionSet) {
            this.instructionSet = instructionSet;
        }

        public int getInstructionSet() {
            return instructionSet;
        }
    }

    /**
     * 当前指令集字符串，Android 5.0以上支持，以下返回null
     */
    private static String getCurrentInstructionSetString() {
        if (Build.VERSION.SDK_INT < 21) {
            return null;
        }
        try {
            Class<?> clazz = Class.forName("dalvik.system.VMRuntime");
            Method currentGet = clazz.getDeclaredMethod("getCurrentInstructionSet");
            return (String) currentGet.invoke(null);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 当前指令集枚举int值，Android 5.0以上支持，以下返回0
     */
    public static int getCurrentInstructionSet() {
        if (currentInstructionSet != null) {
            return currentInstructionSet;
        }
        try {
            String invoke = getCurrentInstructionSetString();
            if ("arm".equals(invoke)) {
                currentInstructionSet = InstructionSet.kArm.getInstructionSet();
            } else if ("arm64".equals(invoke)) {
                currentInstructionSet = InstructionSet.kArm64.getInstructionSet();
            } else if ("x86".equals(invoke)) {
                currentInstructionSet = InstructionSet.kX86.getInstructionSet();
            } else if ("x86_64".equals(invoke)) {
                currentInstructionSet = InstructionSet.kX86_64.getInstructionSet();
            } else if ("mips".equals(invoke)) {
                currentInstructionSet = InstructionSet.kMips.getInstructionSet();
            } else if ("mips64".equals(invoke)) {
                currentInstructionSet = InstructionSet.kMips64.getInstructionSet();
            } else if ("none".equals(invoke)) {
                currentInstructionSet = InstructionSet.kNone.getInstructionSet();
            }
        } catch (Throwable e) {
            currentInstructionSet = InstructionSet.kNone.getInstructionSet();
        }
        return currentInstructionSet != null ? currentInstructionSet : InstructionSet.kNone.getInstructionSet();
    }
}
