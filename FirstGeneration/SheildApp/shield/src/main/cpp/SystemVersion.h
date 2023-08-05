//
// Created by wisdom on 2023/1/10.
// runtime struct
//

#ifndef PROTECTAPP_SYSTEMVERSION_H
#define PROTECTAPP_SYSTEMVERSION_H

/**
 * instruction set
 */
enum class InstructionSet {
    kNone,
    kArm,
    kArm64,
    kThumb2,
    kRiscv64,
    kX86,
    kX86_64,
    kMips,
    kMips64,
    kLast
};

struct JavaVMExt {
    void *functions;
    void *runtime;
};
enum CalleeSaveType {
    kSaveAll,
    kRefsOnly,
    kRefsAndArgs,
    kLastCalleeSaveType  // Value used for iteration
};



/**
 * 基类 runtime
 */
struct RuntimeBase {
    bool image_dex2oat_enabled_;
    InstructionSet instruction_set_;
};

/**
 *  5.0，GcRoot中成员变量是指针类型，所以用void*代替GcRoot
 */
struct PartialRuntime50 {
    void *callee_save_methods_[kLastCalleeSaveType]; //5.0 5.1 void *
    void *pre_allocated_OutOfMemoryError_;
    void *pre_allocated_NoClassDefFoundError_;
    void *resolution_method_;
    void *imt_conflict_method_;
    void *default_imt_; //5.0 5.1

    InstructionSet instruction_set_;
    art::QuickMethodFrameInfo callee_save_method_frame_infos_[kLastCalleeSaveType]; // QuickMethodFrameInfo = uint32_t * 3

    void *compiler_callbacks_;
    bool is_zygote_;
    bool must_relocate_;
    bool is_concurrent_gc_enabled_;
    bool is_explicit_gc_disabled_;
    bool dex2oat_enabled_;
    bool image_dex2oat_enabled_;

    std::string compiler_executable_;
    std::string patchoat_executable_;
    std::vector<std::string> compiler_options_;
    std::vector<std::string> image_compiler_options_;
    std::string image_location_;

    std::string boot_class_path_string_;
    std::string class_path_string_;
    std::vector<std::string> properties_;
};

/**
 * 5.1，GcRoot中成员变量是指针类型，所以用void*代替GcRoot
 */
struct PartialRuntime51 {
    void *callee_save_methods_[kLastCalleeSaveType];  //5.0 5.1 void *
    void *pre_allocated_OutOfMemoryError_;
    void *pre_allocated_NoClassDefFoundError_;
    void *resolution_method_;
    void *imt_conflict_method_;
    // Unresolved method has the same behavior as the conflict method, it is used by the class linker
    // for differentiating between unfilled imt slots vs conflict slots in superclasses.
    void *imt_unimplemented_method_;
    void *default_imt_;  //5.0 5.1

    InstructionSet instruction_set_;
    art::QuickMethodFrameInfo callee_save_method_frame_infos_[kLastCalleeSaveType]; // QuickMethodFrameInfo = uint32_t * 3

    void *compiler_callbacks_;
    bool is_zygote_;
    bool must_relocate_;
    bool is_concurrent_gc_enabled_;
    bool is_explicit_gc_disabled_;
    bool dex2oat_enabled_;
    bool image_dex2oat_enabled_;

    std::string compiler_executable_;
    std::string patchoat_executable_;
    std::vector<std::string> compiler_options_;
    std::vector<std::string> image_compiler_options_;
    std::string image_location_;

    std::string boot_class_path_string_;
    std::string class_path_string_;
    std::vector<std::string> properties_;
};

/**
 * 6.0-7.1，GcRoot中成员变量是class类型，所以用int代替GcRoot
 */
struct PartialRuntime60 {
    // 64 bit so that we can share the same asm offsets for both 32 and 64 bits.
    uint64_t callee_save_methods_[kLastCalleeSaveType];
    int pre_allocated_OutOfMemoryError_;
    int pre_allocated_NoClassDefFoundError_;
    void *resolution_method_;
    void *imt_conflict_method_;
    // Unresolved method has the same behavior as the conflict method, it is used by the class linker
    // for differentiating between unfilled imt slots vs conflict slots in superclasses.
    void *imt_unimplemented_method_;

    // Special sentinel object used to invalid conditions in JNI (cleared weak references) and
    // JDWP (invalid references).
    int sentinel_;

    InstructionSet instruction_set_;
    art::QuickMethodFrameInfo callee_save_method_frame_infos_[kLastCalleeSaveType]; // QuickMethodFrameInfo = uint32_t * 3

    void *compiler_callbacks_;
    bool is_zygote_;
    bool must_relocate_;
    bool is_concurrent_gc_enabled_;
    bool is_explicit_gc_disabled_;
    bool dex2oat_enabled_;
    bool image_dex2oat_enabled_;

    std::string compiler_executable_;
    std::string patchoat_executable_;
    std::vector<std::string> compiler_options_;
    std::vector<std::string> image_compiler_options_;
    std::string image_location_;

    std::string boot_class_path_string_;
    std::string class_path_string_;
    std::vector<std::string> properties_;
};

/**
 * 8.0-8.1, GcRoot中成员变量是class类型，所以用int代替GcRoot
 */
struct PartialRuntime80 {
    // 64 bit so that we can share the same asm offsets for both 32 and 64 bits.
    uint64_t callee_save_methods_[3];
    int pre_allocated_OutOfMemoryError_;
    int pre_allocated_NoClassDefFoundError_;
    void *resolution_method_;
    void *imt_conflict_method_;
    // Unresolved method has the same behavior as the conflict method, it is used by the class linker
    // for differentiating between unfilled imt slots vs conflict slots in superclasses.
    void *imt_unimplemented_method_;

    // Special sentinel object used to invalid conditions in JNI (cleared weak references) and
    // JDWP (invalid references).
    int sentinel_;

    InstructionSet instruction_set_;
    art::QuickMethodFrameInfo callee_save_method_frame_infos_[9]; // QuickMethodFrameInfo = uint32_t * 3

    void *compiler_callbacks_;
    bool is_zygote_;
    bool must_relocate_;
    bool is_concurrent_gc_enabled_;
    bool is_explicit_gc_disabled_;
    bool dex2oat_enabled_;
    bool image_dex2oat_enabled_;

    std::string compiler_executable_;
    std::string patchoat_executable_;
    std::vector<std::string> compiler_options_;
    std::vector<std::string> image_compiler_options_;
    std::string image_location_;

    std::string boot_class_path_string_;
    std::string class_path_string_;
    std::vector<std::string> properties_;


};

/**
 * android  8.1 中的size 与8.0 有所不同
 */
struct PartialRuntime81 {
    static constexpr uint32_t kCalleeSaveSize = 4u;
    // 64 bit so that we can share the same asm offsets for both 32 and 64 bits.
    uint64_t callee_save_methods_[kCalleeSaveSize];
    int pre_allocated_OutOfMemoryError_;
    int pre_allocated_NoClassDefFoundError_;
    void *resolution_method_;
    void *imt_conflict_method_;
    // Unresolved method has the same behavior as the conflict method, it is used by the class linker
    // for differentiating between unfilled imt slots vs conflict slots in superclasses.
    void *imt_unimplemented_method_;

    // Special sentinel object used to invalid conditions in JNI (cleared weak references) and
    // JDWP (invalid references).
    int sentinel_;

    InstructionSet instruction_set_;
    art::QuickMethodFrameInfo callee_save_method_frame_infos_[kCalleeSaveSize]; // QuickMethodFrameInfo = uint32_t * 3

    void *compiler_callbacks_;
    bool is_zygote_;
    bool must_relocate_;
    bool is_concurrent_gc_enabled_;
    bool is_explicit_gc_disabled_;
    bool dex2oat_enabled_;
    bool image_dex2oat_enabled_;

    std::string compiler_executable_;
    std::string patchoat_executable_;
    std::vector<std::string> compiler_options_;
    std::vector<std::string> image_compiler_options_;
    std::string image_location_;

    std::string boot_class_path_string_;
    std::string class_path_string_;
    std::vector<std::string> properties_;
};

/**
 * 9.0, GcRoot中成员变量是class类型，所以用int代替GcRoot
 */
struct PartialRuntime90 {
    static constexpr uint32_t kCalleeSaveSize90 = 6u;
    // 64 bit so that we can share the same asm offsets for both 32 and 64 bits.
    uint64_t callee_save_methods_[kCalleeSaveSize90];
    int pre_allocated_OutOfMemoryError_;
    int pre_allocated_NoClassDefFoundError_;
    void *resolution_method_;
    void *imt_conflict_method_;
    // Unresolved method has the same behavior as the conflict method, it is used by the class linker
    // for differentiating between unfilled imt slots vs conflict slots in superclasses.
    void *imt_unimplemented_method_;

    // Special sentinel object used to invalid conditions in JNI (cleared weak references) and
    // JDWP (invalid references).
    int sentinel_;

    InstructionSet instruction_set_;
    art::QuickMethodFrameInfo callee_save_method_frame_infos_[kCalleeSaveSize90]; // QuickMethodFrameInfo = uint32_t * 3

    void *compiler_callbacks_;
    bool is_zygote_;
    bool must_relocate_;
    bool is_concurrent_gc_enabled_;
    bool is_explicit_gc_disabled_;
    bool dex2oat_enabled_;
    bool image_dex2oat_enabled_;

    std::string compiler_executable_;
    std::string patchoat_executable_;
    std::vector<std::string> compiler_options_;
    std::vector<std::string> image_compiler_options_;
    std::string image_location_;

    std::string boot_class_path_string_;
    std::string class_path_string_;
    std::vector<std::string> properties_;
};

/**
 * android 10.0
 */
struct PartialRuntime10 {
    static constexpr uint32_t kCalleeSaveSize = 6u;

    // 64 bit so that we can share the same asm offsets for both 32 and 64 bits.
    uint64_t callee_save_methods_[kCalleeSaveSize];
    // Pre-allocated exceptions (see Runtime::Init).
    int pre_allocated_OutOfMemoryError_when_throwing_exception_;
    int pre_allocated_OutOfMemoryError_when_throwing_oome_;
    int pre_allocated_OutOfMemoryError_when_handling_stack_overflow_;
    int pre_allocated_NoClassDefFoundError_;
    void * resolution_method_;
    void* imt_conflict_method_;
    // Unresolved method has the same behavior as the conflict method, it is used by the class linker
    // for differentiating between unfilled imt slots vs conflict slots in superclasses.
    void* imt_unimplemented_method_;

    // Special sentinel object used to invalid conditions in JNI (cleared weak references) and
    // JDWP (invalid references).
    int sentinel_;

    InstructionSet instruction_set_;

    void* compiler_callbacks_;
    bool is_zygote_;
    bool is_system_server_;
    bool must_relocate_;
    bool is_concurrent_gc_enabled_;
    bool is_explicit_gc_disabled_;
    bool image_dex2oat_enabled_;

    std::string compiler_executable_;
    std::vector<std::string> compiler_options_;
    std::vector<std::string> image_compiler_options_;
    std::string image_location_;
    bool is_using_apex_boot_image_location_;

    std::vector<std::string> boot_class_path_;
    std::vector<std::string> boot_class_path_locations_;
    std::string class_path_string_;
    std::vector<std::string> properties_;
};



#endif //PROTECTAPP_SYSTEMVERSION_H
