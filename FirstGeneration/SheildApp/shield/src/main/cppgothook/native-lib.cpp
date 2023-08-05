#include <jni.h>
#include <string>
#include <android/log.h>
#include <link.h>
#include <map>
#include <cstring>
#include <elf.h>
#include <sys/mman.h>
#include <dlfcn.h>
#include "Logger.h"

#define PAGE_SIZE 4096
#define PAGE_START(a) ((a) & ~(PAGE_SIZE-1))
#if defined(__LP64__)
#define ELFW(what) ELF64_ ## what

#define Elf_Dyn Elf64_Dyn
#define Elf_Sym Elf64_Sym
#define Elf_Rela Elf64_Rela
#else
#define ELFW(what) ELF32_ ## what
#define Elf_Dyn Elf32_Dyn
#define Elf_Sym Elf32_Sym
#define Elf_Rela Elf32_Rela

#endif

#define ELF32_R_SYM(x) ((x) >> 8)
#define ELF32_R_TYPE(x) ((x) & 0xff)
#define ELF64_R_SYM(i) ((i) >> 32)
#define ELF64_R_TYPE(i) ((i) & 0xffffffff)
static bool hooked = false;
static bool enable = true;
using namespace std;
#define DEX2OAT_BIN "/system/bin/dex2oat"


const char *myStrstr(const char *src, const char *sub) {
    __android_log_print(ANDROID_LOG_DEBUG, "Mrack", "src:%s ,sub %s", src, sub);

    const char *bp;
    const char *sp;
    if (!src || !sub) {
        return src;
    }
    /* 遍历src字符串  */
    while (*src) {
        /* 用来遍历子串 */
        bp = src;
        sp = sub;
        do {
            if (!*sp)  /*到了sub的结束位置，返回src位置   */
                return src;
        } while (*bp++ == *sp++);
        src++;
    }
    return NULL;
}


int (*org_execv)(const char *name, char **argv);

int my_execv(const char *name, char **argv) {

    __android_log_print(ANDROID_LOG_DEBUG,"#execv %s.", "%s", name);


    if(enable && strcmp(name, DEX2OAT_BIN) == 0) {
        __android_log_print(ANDROID_LOG_DEBUG, "Mrack", "hook函数名称:%s 成功 ",name);
        exit(0);
    }

    return org_execv(name, argv);
}

static int callback(struct dl_phdr_info *info,
                    size_t size, void *data) {
    auto *pInfo = new struct dl_phdr_info(*info);
//    if (strstr(pInfo->dlpi_name, "libnative-lib.so")) {
    if (strstr(pInfo->dlpi_name, "libc.so")) {
        __android_log_print(ANDROID_LOG_DEBUG, "Mrack", "函数名称:%u ", pInfo->dlpi_addr);
        for (int i = 0; i < pInfo->dlpi_phnum; ++i) {
            const auto *phdr = &(pInfo->dlpi_phdr[i]);
            __u8 *str_table = nullptr;
            Elf_Rela *jmprel = nullptr;
            Elf_Sym *sym_table = nullptr;


            if (phdr->p_type == PT_DYNAMIC) {
                // pInfo->dlpi_addr便是我们的基地址，也就是目标so 库的基地址
                auto *dyn = (Elf_Dyn *) (phdr->p_vaddr + pInfo->dlpi_addr);
                int i1 = 1;
                while (dyn->d_tag) {
                    if (dyn->d_tag == DT_STRTAB) {//是否DT_STRTAB
                        str_table = (__u8 *) (dyn->d_un.d_ptr + pInfo->dlpi_addr);;
                    }
                    if (dyn->d_tag == DT_JMPREL) {//是否DT_JMPREL
                        /**遍历出d_tag=DT_JMPREL类型的项的d_val值，这个值是指向重定位表的偏移*/
                        jmprel = (Elf_Rela *) (dyn->d_un.d_ptr + pInfo->dlpi_addr);
                    }
                    if (dyn->d_tag == DT_SYMTAB) {//是否DT_SYMTAB
                        sym_table = (Elf_Sym *) (dyn->d_un.d_ptr + pInfo->dlpi_addr);
                    }

                    //根据(基址+p_vaddr)确定.dynamic段的地址
                    dyn = reinterpret_cast<Elf_Dyn *>(phdr->p_vaddr + pInfo->dlpi_addr +
                                                      sizeof(Elf_Dyn) * i1++);
                }

                //遍历查找需要hook的导入函数
                if (jmprel && str_table && sym_table) {

                    for (int j = 0; jmprel->r_info; ++j) {
                        ElfW(Word) type = ELFW(R_TYPE)(jmprel[j].r_info);
                        ElfW(Word) sym = ELFW(R_SYM)(jmprel[j].r_info);

                        //从sym_table获取st_name(目标函数)在str_table的偏移
                        char *name = (char *) (sym_table[sym].st_name + str_table);
//                        if (strstr(name, "strstr")) {
                        __android_log_print(ANDROID_LOG_DEBUG, "Mrack", "函数名称:%s ", name);


                        if (strstr(name, "execv")) { /**=需要hook的函数*/
                            //根据(基址+ d_val)确定重定位表的地址
                            void *i2 = (void *) (jmprel[j].r_offset + pInfo->dlpi_addr);

                            //获取内存分页的起始地址(需要内存对齐)
                            ElfW(Addr) page_start = PAGE_START(
                                    jmprel[j].r_offset + pInfo->dlpi_addr);

                            //使用mprotect解除内存访问权限 https://man7.org/linux/man-pages/man2/mprotect.2.html
                            mprotect((ElfW(Addr) *) page_start, PAGE_SIZE, PROT_WRITE | PROT_READ);

                            //修改got表地址
                            *((ElfW(Addr) *) i2) = (ElfW(Addr)) my_execv + jmprel[i].r_addend;

                            //还原内存访问权限
                            mprotect((ElfW(Addr) *) page_start, PAGE_SIZE, PROT_READ);
                            __android_log_print(ANDROID_LOG_DEBUG, "module", "name:%s",
                                                sym_table[sym].st_name + str_table);


                            break;
                        }
                    }

                }


            }
        }
    }

    __android_log_print(ANDROID_LOG_DEBUG, "module", "库名称 %s", pInfo->dlpi_name);
    return 0;
}




extern "C"
JNIEXPORT jstring JNICALL
Java_com_qihoo_util_NativeUtil_stringFromJNI1(JNIEnv *env, jclass thiz) {
    std::string hello = "Hello from C++";
    dl_iterate_phdr(&callback, nullptr);
//    strstr("test1", "test2");
    return env->NewStringUTF(hello.c_str());
}