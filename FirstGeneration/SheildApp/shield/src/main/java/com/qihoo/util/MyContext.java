package com.qihoo.util;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.util.Log;

import com.qihoo.util.shareutil.SharePatchFileUtil;
import com.qihoo.util.shareutil.ShareReflectUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.zip.ZipFile;

import dalvik.system.DexFile;

/**
 * @Author: hzh
 * @Date: 2022/12/16
 * @Desc: MyContext
 */
public class MyContext extends ContextWrapper {

    private static final String TAG = "MyContext";
    private ClassLoader pathClassLoader;


    public MyContext(Context base) {
        super(base);
        long originalStart = System.currentTimeMillis();
        pathClassLoader = base.getClassLoader();
        a.getInstance().init();
//        File apkFile = new File(getApplicationInfo().sourceDir);
        //data/data/包名/files/tmp_apk/
        File unZipFile = getDir("tmp_apk", MODE_PRIVATE);
        File app = new File(unZipFile, "app");
        if (!app.exists())
            app.mkdir();


        //用于判断是否解密
        if (!SpUtil.getInstace().getBoolean(SpUtil.NAME, false)) {
            //从assets 拷贝到tmp_apk 目录
            long ostart = System.currentTimeMillis();
            copyAssetsToDst(base, "dexs", app);
            System.out.println("拷贝总耗时 " + (System.currentTimeMillis() - ostart));
            long decryptStart = System.currentTimeMillis();
            File[] files = app.listFiles();
            for (File file : files) {
                String name = file.getName();
                if (name.equals("classes.dex")) {
                } else if (name.endsWith(".dex")) {
                    try {
                        byte[] bytes = getBytes(file);
                        FileOutputStream fos = new FileOutputStream(file);
                        byte[] decrypt = a.decrypt(bytes);
                        fos.write(decrypt);
                        fos.flush();
                        fos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            SpUtil.getInstace().save(SpUtil.NAME, true);
            System.out.println("解密 耗时 " + (System.currentTimeMillis() - decryptStart));
        }


        List list = new ArrayList<>();
        for (File file : app.listFiles()) {
            if (file.getName().endsWith(".dex") && !file.getName().equals("classes.dex")) {
                list.add(file);
            }
        }


        long injectDexesInternal = System.currentTimeMillis();
        try {
            injectDexesInternal(pathClassLoader, list, unZipFile);
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        System.out.println("install 耗时 " + (System.currentTimeMillis() - injectDexesInternal));


        long l1 = System.currentTimeMillis();
        System.out.println("加载总耗时 " + (l1 - originalStart));

    }

    private void copyAssetsToDst(Context context, String srcPath, File dstPath) {

        try {
            String fileNames[] = context.getAssets().list(srcPath);

            if (fileNames.length > 0) {

                File file = dstPath;
                if (!file.exists()) file.mkdirs();
                for (String fileName : fileNames) {
                    System.out.println("fileNames.length > 0 file name:" + fileName);
//                    if (!srcPath.equals("")) { // assets 文件夹下的目录
//                        copyAssetsToDst(context, srcPath + File.separator + fileName, new File(dstPath.getAbsolutePath() + File.separator + fileName));
//                    } else { // assets 文件夹
//                        copyAssetsToDst(context, fileName, new File(dstPath.getAbsolutePath() + File.separator + fileName));
//                    }

                    InputStream is = context.getAssets().open(srcPath + File.separator + fileName);
                    FileOutputStream fos = new FileOutputStream(new File(dstPath.getAbsolutePath() + File.separator + fileName));
                    byte[] buffer = new byte[30720];
                    int byteCount;
                    while ((byteCount = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, byteCount);
                    }
                    fos.flush();
                    is.close();
                    fos.close();
                }
            } else {

            }

        } catch (Exception e) {
            e.printStackTrace();

        }


    }


    static void injectDexesInternal(ClassLoader cl, List<File> dexFiles, File optimizeDir) throws Throwable {
        if (Build.VERSION.SDK_INT >= 23) {
            System.out.println(" V23.install ");
            V23.install(cl, dexFiles, optimizeDir);
        } else if (Build.VERSION.SDK_INT >= 19) {
            V19.install(cl, dexFiles, optimizeDir);
        } else if (Build.VERSION.SDK_INT >= 14) {
            V14.install(cl, dexFiles, optimizeDir);
        } else {
            V4.install(cl, dexFiles, optimizeDir);
        }
    }

    @Override
    public ClassLoader getClassLoader() {

        return pathClassLoader;
    }

    /**
     * Installer for platform versions 23.
     */
    private static final class V23 {

        private static void install(ClassLoader loader, List<File> additionalClassPathEntries,
                                    File optimizedDirectory)
                throws IllegalArgumentException, IllegalAccessException,
                NoSuchFieldException, InvocationTargetException, NoSuchMethodException, IOException {
            /* The patched class loader is expected to be a descendant of
             * dalvik.system.BaseDexClassLoader. We modify its
             * dalvik.system.DexPathList pathList field to append additional DEX
             * file entries.
             */
            long l1 = System.currentTimeMillis();
            Field pathListField = ShareReflectUtil.findField(loader, "pathList");
            Object dexPathList = pathListField.get(loader);
            ArrayList<IOException> suppressedExceptions = new ArrayList<IOException>();
            ShareReflectUtil.expandFieldArray(dexPathList, "dexElements", makePathElements(dexPathList,
                    new ArrayList<File>(additionalClassPathEntries), optimizedDirectory,
                    suppressedExceptions));
            if (suppressedExceptions.size() > 0) {
                for (IOException e : suppressedExceptions) {
                    Log.w(TAG, "Exception in makePathElement", e);
                    throw e;
                }

            }
        }

        /**
         * A wrapper around
         * {@code private static final dalvik.system.DexPathList#makePathElements}.
         */
        private static Object[] makePathElements(
                Object dexPathList, ArrayList<File> files, File optimizedDirectory,
                ArrayList<IOException> suppressedExceptions)
                throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

            Method makePathElements;
            try {
                makePathElements = ShareReflectUtil.findMethod(dexPathList, "makePathElements", List.class, File.class,
                        List.class);
            } catch (NoSuchMethodException e) {
                Log.e(TAG, "NoSuchMethodException: makePathElements(List,File,List) failure");
                try {
                    makePathElements = ShareReflectUtil.findMethod(dexPathList, "makePathElements", ArrayList.class, File.class, ArrayList.class);
                } catch (NoSuchMethodException e1) {
                    Log.e(TAG, "NoSuchMethodException: makeDexElements(ArrayList,File,ArrayList) failure");
                    try {
                        Log.e(TAG, "NoSuchMethodException: try use v19 instead");
                        return V19.makeDexElements(dexPathList, files, optimizedDirectory, suppressedExceptions);
                    } catch (NoSuchMethodException e2) {
                        Log.e(TAG, "NoSuchMethodException: makeDexElements(List,File,List) failure");
                        throw e2;
                    }
                }
            }

            long l1 = System.currentTimeMillis();
            Object[] invoke = (Object[]) makePathElements.invoke(dexPathList, files, optimizedDirectory, suppressedExceptions);
            System.out.println(" makePathElements 耗时：" + (System.currentTimeMillis() - l1));
            return invoke;
        }
    }

    /**
     * Installer for platform versions 19.
     */
    private static final class V19 {

        private static void install(ClassLoader loader, List<File> additionalClassPathEntries,
                                    File optimizedDirectory)
                throws IllegalArgumentException, IllegalAccessException,
                NoSuchFieldException, InvocationTargetException, NoSuchMethodException, IOException {
            /* The patched class loader is expected to be a descendant of
             * dalvik.system.BaseDexClassLoader. We modify its
             * dalvik.system.DexPathList pathList field to append additional DEX
             * file entries.
             */
            Field pathListField = ShareReflectUtil.findField(loader, "pathList");
            Object dexPathList = pathListField.get(loader);
            ArrayList<IOException> suppressedExceptions = new ArrayList<IOException>();
            ShareReflectUtil.expandFieldArray(dexPathList, "dexElements", makeDexElements(dexPathList,
                    new ArrayList<File>(additionalClassPathEntries), optimizedDirectory,
                    suppressedExceptions));
            if (suppressedExceptions.size() > 0) {
                for (IOException e : suppressedExceptions) {
                    Log.w(TAG, "Exception in makeDexElement", e);
                    throw e;
                }
            }
        }

        /**
         * A wrapper around
         * {@code private static final dalvik.system.DexPathList#makeDexElements}.
         */
        private static Object[] makeDexElements(
                Object dexPathList, ArrayList<File> files, File optimizedDirectory,
                ArrayList<IOException> suppressedExceptions)
                throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

            Method makeDexElements = null;
            try {
                makeDexElements = ShareReflectUtil.findMethod(dexPathList, "makeDexElements", ArrayList.class, File.class,
                        ArrayList.class);
            } catch (NoSuchMethodException e) {
                Log.e(TAG, "NoSuchMethodException: makeDexElements(ArrayList,File,ArrayList) failure");
                try {
                    makeDexElements = ShareReflectUtil.findMethod(dexPathList, "makeDexElements", List.class, File.class, List.class);
                } catch (NoSuchMethodException e1) {
                    Log.e(TAG, "NoSuchMethodException: makeDexElements(List,File,List) failure");
                    throw e1;
                }
            }

            return (Object[]) makeDexElements.invoke(dexPathList, files, optimizedDirectory, suppressedExceptions);
        }
    }

    /**
     * Installer for platform versions 14, 15, 16, 17 and 18.
     */
    private static final class V14 {

        private static void install(ClassLoader loader, List<File> additionalClassPathEntries,
                                    File optimizedDirectory)
                throws IllegalArgumentException, IllegalAccessException,
                NoSuchFieldException, InvocationTargetException, NoSuchMethodException {
            /* The patched class loader is expected to be a descendant of
             * dalvik.system.BaseDexClassLoader. We modify its
             * dalvik.system.DexPathList pathList field to append additional DEX
             * file entries.
             */
            Field pathListField = ShareReflectUtil.findField(loader, "pathList");
            Object dexPathList = pathListField.get(loader);
            ShareReflectUtil.expandFieldArray(dexPathList, "dexElements", makeDexElements(dexPathList,
                    new ArrayList<File>(additionalClassPathEntries), optimizedDirectory));
        }

        /**
         * A wrapper around
         * {@code private static final dalvik.system.DexPathList#makeDexElements}.
         */
        private static Object[] makeDexElements(
                Object dexPathList, ArrayList<File> files, File optimizedDirectory)
                throws IllegalAccessException, InvocationTargetException,
                NoSuchMethodException {
            Method makeDexElements =
                    ShareReflectUtil.findMethod(dexPathList, "makeDexElements", ArrayList.class, File.class);

            return (Object[]) makeDexElements.invoke(dexPathList, files, optimizedDirectory);
        }
    }

    /**
     * Installer for platform versions 4 to 13.
     */
    private static final class V4 {
        private static void install(ClassLoader loader, List<File> additionalClassPathEntries, File optimizedDirectory)
                throws IllegalArgumentException, IllegalAccessException,
                NoSuchFieldException, IOException {
            /* The patched class loader is expected to be a descendant of
             * dalvik.system.DexClassLoader. We modify its
             * fields mPaths, mFiles, mZips and mDexs to append additional DEX
             * file entries.
             */
            int extraSize = additionalClassPathEntries.size();

            Field pathField = ShareReflectUtil.findField(loader, "path");

            StringBuilder path = new StringBuilder((String) pathField.get(loader));
            String[] extraPaths = new String[extraSize];
            File[] extraFiles = new File[extraSize];
            ZipFile[] extraZips = new ZipFile[extraSize];
            DexFile[] extraDexs = new DexFile[extraSize];
            for (ListIterator<File> iterator = additionalClassPathEntries.listIterator();
                 iterator.hasNext(); ) {
                File additionalEntry = iterator.next();
                String entryPath = additionalEntry.getAbsolutePath();
                path.append(':').append(entryPath);
                int index = iterator.previousIndex();
                extraPaths[index] = entryPath;
                extraFiles[index] = additionalEntry;
                extraZips[index] = new ZipFile(additionalEntry);
                //edit by zhangshaowen
                String outputPathName = SharePatchFileUtil.optimizedPathFor(additionalEntry, optimizedDirectory);
                //for below 4.0, we must input jar or zip
                extraDexs[index] = DexFile.loadDex(entryPath, outputPathName, 0);
            }

            pathField.set(loader, path.toString());
            ShareReflectUtil.expandFieldArray(loader, "mPaths", extraPaths);
            ShareReflectUtil.expandFieldArray(loader, "mFiles", extraFiles);
            ShareReflectUtil.expandFieldArray(loader, "mZips", extraZips);
            try {
                ShareReflectUtil.expandFieldArray(loader, "mDexs", extraDexs);
            } catch (Exception e) {
                // Ignored.
            }
        }
    }


    private byte[] getBytes(File file) throws Exception {
        RandomAccessFile r = new RandomAccessFile(file, "r");
        byte[] buffer = new byte[(int) r.length()];
        r.readFully(buffer);
        r.close();
        return buffer;
    }

}
