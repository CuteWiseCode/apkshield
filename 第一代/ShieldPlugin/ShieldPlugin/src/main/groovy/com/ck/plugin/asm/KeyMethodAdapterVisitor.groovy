package com.ck.plugin.asm


import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter

/**
 * @Author: hzh
 * @Date: 2022/12/8
 * @Desc: MethodAdapterVisitor
 */
class KeyMethodAdapterVisitor extends AdviceAdapter {


     String methodName
     String mKey
     int acc
     String desc

    KeyMethodAdapterVisitor(MethodVisitor mv, int access, String name, String desc,
                            String aesKey) {
        super(Opcodes.ASM5, mv, access, name, desc)
        methodName = name
        this.mKey = aesKey
        this.acc = access
        this.desc = desc
    }

    @Override
    void visitLdcInsn(Object value) {
        if (methodName == "a" && this.acc == ACC_PRIVATE && this.desc == "()Ljava/lang/String;")  {
            value = mKey
        }
        super.visitLdcInsn(value)
    }




}
