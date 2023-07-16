package com.ck.plugin.asm

import com.ck.plugin.utils.LogUtil
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter

/**
 * @Author: hzh
 * @Date: 2023/2/6
 * @Desc: ZipMethodAdapterVisitor
 */
class ZipMethodAdapterVisitor extends AdviceAdapter {


     String methodName
     String mKey
     int acc
     String desc
    int count

    ZipMethodAdapterVisitor(MethodVisitor mv, int access, String name, String desc,
                            int count) {
        super(Opcodes.ASM5, mv, access, name, desc)
        methodName = name
        this.acc = access
        this.desc = desc
        this.count = count
    }

    @Override
    void visitLdcInsn(Object value) {

        super.visitLdcInsn(value)
    }



    @Override
    void visitInsn(int opcode) {
        LogUtil.log("visitLdcInsn value = $opcode count = $count")
        if (methodName == "getDexNumber" && opcode == IRETURN )  {
            super.visitIntInsn(SIPUSH,count)
            LogUtil.log("visitLdcInsn after value = $opcode ")
        }
        super.visitInsn(opcode)
    }
}
