#include <jni.h>
#include "art.h"

//extern "C"
//JNIEXPORT jint JNICALL
//Java_me_weishu_reflection_Reflection_unsealNative(JNIEnv *env, jclass type, jint targetSdkVersion) {
//
//}
extern "C"
JNIEXPORT jint JNICALL
Java_com_qihoo_util_NativeUtil_unsealNative(JNIEnv *env, jclass clazz, jint targetSdkVersion) {
    return unseal(env, targetSdkVersion);
}