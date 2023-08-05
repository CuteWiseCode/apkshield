#include <jni.h>
#include <string>
#include <jni.h>
#include <stdio.h>
#include <iosfwd>
#include <set>
#include <string>
#include <utility>
#include <memory>
#include <vector>
#include "quick/quick_method_frame_info.h"
#include "Logger.h"
#include "SystemVersion.h"
#include <iostream>
#include <sstream>


#if defined(__arm__)
static constexpr InstructionSet kRuntimeISA = InstructionSet::kArm;
#elif defined(__aarch64__)
static constexpr InstructionSet kRuntimeISA = InstructionSet::kArm64;
#elif defined(__mips__) && !defined(__LP64__)
static constexpr InstructionSet kRuntimeISA = InstructionSet::kMips;
#elif defined(__mips__) && defined(__LP64__)
static constexpr InstructionSet kRuntimeISA = InstructionSet::kMips64;
#elif defined(__i386__)
static constexpr InstructionSet kRuntimeISA = InstructionSet::kX86;
#elif defined(__x86_64__)
static constexpr InstructionSet kRuntimeISA = InstructionSet::kX86_64;
#else
static constexpr InstructionSet kRuntimeISA = InstructionSet::kNone;
#endif


template<typename T>
int findOffset(void *start, int regionStart, int regionEnd, T value) {

    if (NULL == start || regionEnd <= 0 || regionStart < 0) {
        return -1;
    }
    char *c_start = (char *) start;

    for (int i = regionStart; i < regionEnd; i += 4) {
        T *current_value = (T *) (c_start + i);
        if (value == *current_value) {
            LOGE("found offset: %d", i);
            return i;
        }
    }
    return -2;
}

void faillog(int order) {
    __android_log_print(ANDROID_LOG_DEBUG, "#execv %s", "寻址失败 %d", order);
}


/**
 * 偏移
 */
extern "C" JNIEXPORT jstring JNICALL
Java_com_qihoo_util_NativeUtil_stringFromJNI(
        JNIEnv *env,
        jclass clazz /* this */,
        jint addr,
        jboolean enable) {

    int currentInstructionSet = addr;
    std::string fails = "寻址失败啦";
    //获取javaVM 指针
    JavaVM *javaVM;
    env->GetJavaVM(&javaVM);
    const int MAX = 2000;
    __android_log_print(ANDROID_LOG_DEBUG, "#execv %s", "GetJavaVM");
    //将javaVM 强转为自定义的  JavaVMExt
    auto *javaVMExt = (JavaVMExt *) javaVM;
    void *runtime = javaVMExt->runtime;

    // 1. 获取 SDK 版本号 , 存储于 C 字符串 sdk_verison_str 中
    char sdk[128] = "0";

    // 获取版本号方法
    __system_property_get("ro.build.version.sdk", sdk);

    //将版本号转为 int 值
    int sdk_verison = atoi(sdk);

    /// ********************1、获取runtime 偏移地址******************
    //如果是arm则优先使用kThumb2查找，查找不到则再使用arm重试
    int isa = (int) kRuntimeISA;
    int instructionSetOffset = -1;
    instructionSetOffset = findOffset(runtime, 0, 100, isa == (int) InstructionSet::kArm
                                                       ? (int) InstructionSet::kThumb2
                                                       : isa);

    __android_log_print(ANDROID_LOG_DEBUG, "#execv ", "instructionSetOffset %d",
                        instructionSetOffset);
    if (instructionSetOffset < 0 && isa == (int) InstructionSet::kArm) {
        //如果是arm用thumb2查找失败，则使用arm重试查找
        __android_log_print(ANDROID_LOG_DEBUG, "#execv ", "retry find offset when thumb2 fail: %d",
                            InstructionSet::kArm);
        instructionSetOffset = findOffset(runtime, 0, 100, InstructionSet::kArm);
    }

    //如果kRuntimeISA找不到，则使用java层传入的currentInstructionSet，该值由java层反射获取到传入jni函数中
    if (instructionSetOffset <= 0) {
        __android_log_print(ANDROID_LOG_DEBUG, "#execv %s", " if (instructionSetOffset <= 0) ");
        isa = currentInstructionSet;
        __android_log_print(ANDROID_LOG_DEBUG, "#execv %s",
                            "retry find offset with currentInstructionSet: %d",
                            isa == (int) InstructionSet::kArm
                            ? (int) InstructionSet::kThumb2
                            : isa);
        instructionSetOffset = findOffset(runtime, 0, 100, isa == (int) InstructionSet::kArm
                                                           ? (int) InstructionSet::kThumb2 : isa);
        if (instructionSetOffset < 0 && isa == (int) InstructionSet::kArm) {
            __android_log_print(ANDROID_LOG_DEBUG, "#execv %s",
                                "retry find offset with currentInstructionSet when thumb2 fail: %d",
                                InstructionSet::kArm);
            //如果是arm用thumb2查找失败，则使用arm重试查找
            instructionSetOffset = findOffset(runtime, 0, 100, InstructionSet::kArm);
        }
        if (instructionSetOffset <= 0) {
            faillog(6);
            return env->NewStringUTF(fails.c_str());
        }
    }
    //强转成
    bool image_dex2oat_enabled_ = true;
    /// ************************2、获取image_dex2oat_enabled_ 偏移地址*************************
//   int image_dex2oattOffset = findOffset(runtime, instructionSetOffset, MAX, image_dex2oat_enabled_);



    if (sdk_verison == 33) {//android 13
    } else if (sdk_verison == 32 || sdk_verison == 31) {
        //android 12
    } else if (sdk_verison == 30) {
        //android 11
    } else if (sdk_verison == 29) {
        //android 10
    } else if (sdk_verison == 28) {
        //android 9
        auto partialRuntime = (PartialRuntime90 *) ((char *) runtime);

        //拿到对应版本的runtime 设置对应的 image_dex2oat_enabled_
        partialRuntime->image_dex2oat_enabled_ = enable;
        __android_log_print(ANDROID_LOG_DEBUG, "#execv %s", "image_dex2oat_enabled_");
        if (partialRuntime->instruction_set_ <= InstructionSet::kNone ||
            partialRuntime->instruction_set_ >= InstructionSet::kLast) {
            faillog(0);

        }
        image_dex2oat_enabled_ = partialRuntime->image_dex2oat_enabled_;
    } else if (sdk_verison == 26) {
        ///android 8.0
        auto partialRuntime = (PartialRuntime80 *) ((char *) runtime);


        __android_log_print(ANDROID_LOG_DEBUG, "#execv %s", "PartialRuntime80 赋值 %d"  , partialRuntime->instruction_set_);
        if (partialRuntime->instruction_set_ <= InstructionSet::kNone ||
            partialRuntime->instruction_set_ >= InstructionSet::kLast) {
            faillog(1);
        }
        //拿到对应版本的runtime 设置对应的 image_dex2oat_enabled_
        partialRuntime->image_dex2oat_enabled_ = enable;
        image_dex2oat_enabled_ = partialRuntime->image_dex2oat_enabled_;
    } else if (sdk_verison == 27) {
        /// android 8.1
        auto partialRuntime = (PartialRuntime81 *) ((char *) runtime);
        //拿到对应版本的runtime 设置对应的 image_dex2oat_enabled_

        __android_log_print(ANDROID_LOG_DEBUG, "#execv %s", "image_dex2oat_enabled_");
        if (partialRuntime->instruction_set_ <= InstructionSet::kNone ||
            partialRuntime->instruction_set_ >= InstructionSet::kLast) {
            faillog(2);
        }

        partialRuntime->image_dex2oat_enabled_ = enable;
        image_dex2oat_enabled_ = partialRuntime->image_dex2oat_enabled_;
    } else if (sdk_verison == 23 || sdk_verison == 24 || sdk_verison == 25) {
        //android 6.0 //android 7.1 android 7.0
        auto partialRuntime = (PartialRuntime60 *) ((char *) runtime);
        //拿到对应版本的runtime 设置对应的 image_dex2oat_enabled_
        partialRuntime->image_dex2oat_enabled_ = enable;
        __android_log_print(ANDROID_LOG_DEBUG, "#execv %s", "image_dex2oat_enabled_");
        if (partialRuntime->instruction_set_ <= InstructionSet::kNone ||
            partialRuntime->instruction_set_ >= InstructionSet::kLast) {
            faillog(3);

        }
        image_dex2oat_enabled_ = partialRuntime->image_dex2oat_enabled_;
    } else if (sdk_verison == 22) {
        //android 5.1
        auto partialRuntime = (PartialRuntime51 *) ((char *) runtime);
        //拿到对应版本的runtime 设置对应的 image_dex2oat_enabled_
        partialRuntime->image_dex2oat_enabled_ = enable;
        __android_log_print(ANDROID_LOG_DEBUG, "#execv %s", "image_dex2oat_enabled_");
        if (partialRuntime->instruction_set_ <= InstructionSet::kNone ||
            partialRuntime->instruction_set_ >= InstructionSet::kLast) {
            faillog(4);

        }
        image_dex2oat_enabled_ = partialRuntime->image_dex2oat_enabled_;
    } else if (sdk_verison == 21) {
        //android 5.0
        auto partialRuntime = (PartialRuntime50 *) ((char *) runtime);
        //拿到对应版本的runtime 设置对应的 image_dex2oat_enabled_
        partialRuntime->image_dex2oat_enabled_ = enable;
        __android_log_print(ANDROID_LOG_DEBUG, "#execv %s", "image_dex2oat_enabled_");
        if (partialRuntime->instruction_set_ <= InstructionSet::kNone ||
            partialRuntime->instruction_set_ >= InstructionSet::kLast) {
            faillog(5);

        }
        image_dex2oat_enabled_ = partialRuntime->image_dex2oat_enabled_;
    }


    std::ostringstream os1;
    os1 << image_dex2oat_enabled_;

    std::string hello = "Hello from C++ 当前dex2oat 值为：";
    hello.append(os1.str());

    LOGE(" 当前dex2oat 值为： %d",
         image_dex2oat_enabled_);

    return env->NewStringUTF(hello.c_str());
}

/**
 * 直接赋值
 */
extern "C" JNIEXPORT jstring JNICALL
Java_com_qihoo_util_NativeUtil_stringFromJNI2(
        JNIEnv *env,
        jclass clazz /* this */,
        jint addr) {

    int currentInstructionSet = addr;
    std::string fails = "寻址失败啦";
    //获取javaVM 指针
    JavaVM *javaVM;
    env->GetJavaVM(&javaVM);
    const int MAX = 2000;
    __android_log_print(ANDROID_LOG_DEBUG, "#execv %s", "GetJavaVM");
    //将javaVM 强转为自定义的  JavaVMExt
    auto *javaVMExt = (JavaVMExt *) javaVM;
    void *runtime = javaVMExt->runtime;

    // 1. 获取 SDK 版本号 , 存储于 C 字符串 sdk_verison_str 中
    char sdk[128] = "0";

    // 获取版本号方法
    __system_property_get("ro.build.version.sdk", sdk);

    //将版本号转为 int 值
    int sdk_verison = atoi(sdk);


    /// ********************1、获取runtime 偏移地址******************
    //如果是arm则优先使用kThumb2查找，查找不到则再使用arm重试
    int isa = (int) kRuntimeISA;
    int instructionSetOffset = -1;
    instructionSetOffset = findOffset(runtime, 0, 100, isa == (int) InstructionSet::kArm
                                                       ? (int) InstructionSet::kThumb2
                                                       : isa);

    __android_log_print(ANDROID_LOG_DEBUG, "#execv ", "instructionSetOffset %d",
                        instructionSetOffset);
    if (instructionSetOffset < 0 && isa == (int) InstructionSet::kArm) {
        //如果是arm用thumb2查找失败，则使用arm重试查找
        __android_log_print(ANDROID_LOG_DEBUG, "#execv ", "retry find offset when thumb2 fail: %d",
                            InstructionSet::kArm);
        instructionSetOffset = findOffset(runtime, 0, 100, InstructionSet::kArm);
    }

    //如果kRuntimeISA找不到，则使用java层传入的currentInstructionSet，该值由java层反射获取到传入jni函数中
    if (instructionSetOffset <= 0) {
        __android_log_print(ANDROID_LOG_DEBUG, "#execv %s", " if (instructionSetOffset <= 0) ");
        isa = currentInstructionSet;
        __android_log_print(ANDROID_LOG_DEBUG, "#execv %s",
                            "retry find offset with currentInstructionSet: %d",
                            isa == (int) InstructionSet::kArm
                            ? (int) InstructionSet::kThumb2
                            : isa);
        instructionSetOffset = findOffset(runtime, 0, 100, isa == (int) InstructionSet::kArm
                                                           ? (int) InstructionSet::kThumb2 : isa);
        if (instructionSetOffset < 0 && isa == (int) InstructionSet::kArm) {
            __android_log_print(ANDROID_LOG_DEBUG, "#execv %s",
                                "retry find offset with currentInstructionSet when thumb2 fail: %d",
                                InstructionSet::kArm);
            //如果是arm用thumb2查找失败，则使用arm重试查找
            instructionSetOffset = findOffset(runtime, 0, 100, InstructionSet::kArm);
        }
        if (instructionSetOffset <= 0) {
            faillog(6);
            return env->NewStringUTF(fails.c_str());
        }
    }


    //强转成
    bool image_dex2oat_enabled_ = true;
    /// ************************2、获取image_dex2oat_enabled_ 便宜地址*************************



    if (sdk_verison == 33) {//android 13
    } else if (sdk_verison == 32 || sdk_verison == 31) {
        //android 12
    } else if (sdk_verison == 30) {
        //android 11
    } else if (sdk_verison == 29) {
        //android 10
    } else if (sdk_verison == 28) {
        //android 9
        auto partialRuntime = (PartialRuntime90 * )((char *) runtime);
        //拿到对应版本的runtime 设置对应的 image_dex2oat_enabled_
        partialRuntime->image_dex2oat_enabled_ = false;
        __android_log_print(ANDROID_LOG_DEBUG, "#execv %s", "image_dex2oat_enabled_");
        if (partialRuntime->instruction_set_ <= InstructionSet::kNone ||
            partialRuntime->instruction_set_ >= InstructionSet::kLast) {
            faillog(0);

        }
        image_dex2oat_enabled_ = partialRuntime->image_dex2oat_enabled_;
    } else if (sdk_verison == 26) {
        ///android 8.0
        auto partialRuntime = (PartialRuntime80 * )((char *) runtime);


        __android_log_print(ANDROID_LOG_DEBUG, "#execv %s", "PartialRuntime80 赋值");
        if (partialRuntime->instruction_set_ <= InstructionSet::kNone ||
            partialRuntime->instruction_set_ >= InstructionSet::kLast) {
            faillog(1);



            partialRuntime = (PartialRuntime80 * )((char *) runtime + currentInstructionSet);
            __android_log_print(ANDROID_LOG_DEBUG, "#execv %s", "使用java反射偏移 %d 重新赋值 ",
                                currentInstructionSet);

            if (partialRuntime->instruction_set_ <= InstructionSet::kNone ||
                partialRuntime->instruction_set_ >= InstructionSet::kLast) {
                __android_log_print(ANDROID_LOG_DEBUG, "#execv %s", "使用java反射偏移 %d 重新赋值 ： 失败！！！",
                                    currentInstructionSet);

            }
        }
        //拿到对应版本的runtime 设置对应的 image_dex2oat_enabled_
        partialRuntime->image_dex2oat_enabled_ = false;
        image_dex2oat_enabled_ = partialRuntime->image_dex2oat_enabled_;
    } else if (sdk_verison == 27) {
        /// android 8.1
        auto partialRuntime = (PartialRuntime81 * )((char *) runtime);
        //拿到对应版本的runtime 设置对应的 image_dex2oat_enabled_
        partialRuntime->image_dex2oat_enabled_ = false;
        __android_log_print(ANDROID_LOG_DEBUG, "#execv %s", "image_dex2oat_enabled_");
        if (partialRuntime->instruction_set_ <= InstructionSet::kNone ||
            partialRuntime->instruction_set_ >= InstructionSet::kLast) {
            faillog(2);

        }
        image_dex2oat_enabled_ = partialRuntime->image_dex2oat_enabled_;
    } else if (sdk_verison == 23 || sdk_verison == 24 || sdk_verison == 25) {
        //android 6.0 //android 7.1 android 7.0
        auto partialRuntime = (PartialRuntime60 * )((char *) runtime);
        //拿到对应版本的runtime 设置对应的 image_dex2oat_enabled_
        partialRuntime->image_dex2oat_enabled_ = false;
        __android_log_print(ANDROID_LOG_DEBUG, "#execv %s", "image_dex2oat_enabled_");
        if (partialRuntime->instruction_set_ <= InstructionSet::kNone ||
            partialRuntime->instruction_set_ >= InstructionSet::kLast) {
            faillog(3);

        }
        image_dex2oat_enabled_ = partialRuntime->image_dex2oat_enabled_;
    } else if (sdk_verison == 22) {
        //android 5.1
        auto partialRuntime = (PartialRuntime51 * )((char *) runtime);
        //拿到对应版本的runtime 设置对应的 image_dex2oat_enabled_
        partialRuntime->image_dex2oat_enabled_ = false;
        __android_log_print(ANDROID_LOG_DEBUG, "#execv %s", "image_dex2oat_enabled_");
        if (partialRuntime->instruction_set_ <= InstructionSet::kNone ||
            partialRuntime->instruction_set_ >= InstructionSet::kLast) {
            faillog(4);

        }
        image_dex2oat_enabled_ = partialRuntime->image_dex2oat_enabled_;
    } else if (sdk_verison == 21) {
        //android 5.0
        auto partialRuntime = (PartialRuntime50 * )((char *) runtime);
        //拿到对应版本的runtime 设置对应的 image_dex2oat_enabled_
        partialRuntime->image_dex2oat_enabled_ = false;
        __android_log_print(ANDROID_LOG_DEBUG, "#execv %s", "image_dex2oat_enabled_");
        if (partialRuntime->instruction_set_ <= InstructionSet::kNone ||
            partialRuntime->instruction_set_ >= InstructionSet::kLast) {
            faillog(5);

        }
        image_dex2oat_enabled_ = partialRuntime->image_dex2oat_enabled_;
    }


    std::ostringstream os1;
    os1 << image_dex2oat_enabled_;

    std::string hello = "Hello from C++ 当前dex2oat 值为：";
    hello.append(os1.str());

    LOGE(" 当前dex2oat 值为： %d",
         image_dex2oat_enabled_);

    return env->NewStringUTF(hello.c_str());
}



