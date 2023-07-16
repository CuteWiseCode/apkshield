package com.ck.plugin.asm


import com.ck.plugin.utils.LogUtil
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * @Author: hzh
 * @Date: 2022/12/8
 * @Desc:
 */
class ModifyKeyClassVisitor extends ClassVisitor {

    String mFilename
    def mAccess
    String mClassName
    boolean isInterface = false
    boolean isAbstractClazz = false
    def mPasswd
    int count


    ModifyKeyClassVisitor(ClassVisitor classVisitor, String filename,String pwd,int count) {
        super(Opcodes.ASM5, classVisitor)
        mFilename = filename
        mPasswd= pwd
        this.count =count
    }



    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces)
        mAccess = access
        mClassName = name
        this.isInterface = (access & Opcodes.ACC_INTERFACE) != 0
        this.isAbstractClazz = (access & Opcodes.ACC_ABSTRACT) != 0
    }

    @Override
    MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions)
        LogUtil.log("visitMethod name = $name   filename = $mFilename")
         if(mFilename.contains("com/qihoo/util/a")){
             return new KeyMethodAdapterVisitor(mv, access, name, descriptor, mPasswd)
         }else if(mFilename.contains("com/qihoo/util/Zip")){
             return new ZipMethodAdapterVisitor(mv, access, name, descriptor, count)
         }

         return  mv

    }

    @Override
    FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {

        FieldVisitor fv = super.visitField(access, name, descriptor, signature, value)
        return fv
    }





}
