package com.ck.plugin.shield;



import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author ck
 */
public class AES {
    private static final String algorithmStr = "AES/CBC/PKCS5Padding";

    private static Cipher encryptCipher;
    private static Cipher decryptCipher;

    public static void init(String password) {
        try {
        	// 生成一个实现指定转换的 Cipher 对象。
            encryptCipher = Cipher.getInstance(algorithmStr);
            decryptCipher = Cipher.getInstance(algorithmStr);// algorithmStr
            byte[] keyStr = password.getBytes();
            SecretKeySpec key = new SecretKeySpec(keyStr, "AES");
//            //指定加密类型
//            KeyGenerator keygen = KeyGenerator.getInstance("AES");
//            //指定秘钥长度
//            keygen.init(128,new SecureRandom());
//            SecretKey key = keygen.generateKey();
            encryptCipher.init(Cipher.ENCRYPT_MODE, key,new IvParameterSpec(new byte[encryptCipher.getBlockSize()]));
            decryptCipher.init(Cipher.DECRYPT_MODE, key,new IvParameterSpec(new byte[decryptCipher.getBlockSize()]));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }

    /**
    *
    * @param srcAPKfile  源文件所在位置
    * @param dstApkFile  目标文件
    * @return             加密后的新dex 文件
    * @throws Exception
    */
    public static File encryptAPKFile(File srcAPKfile, File dstApkFile) throws Exception {
        if (srcAPKfile == null) {
        	System.out.println("encryptAPKFile :srcAPKfile null");
            return null;
        }
//        File disFile = new File(srcAPKfile.getAbsolutePath() + "unzip");
//		Zip.unZip(srcAPKfile, disFile);
        Zip.unZip(srcAPKfile, dstApkFile);
        //获得所有的dex （需要处理分包情况）
        File[] dexFiles = dstApkFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.endsWith(".dex");
            }
        });

        File mainDexFile = null;
        byte[] mainDexData = null;

        for (File dexFile: dexFiles) {
        	//读数据
            byte[] buffer = Utils.getBytes(dexFile);
            //加密
            byte[] encryptBytes = AES.encrypt(buffer);

            if (dexFile.getName().endsWith("classes.dex")) {
                mainDexData = encryptBytes;
                mainDexFile = dexFile;
            }
           //写数据  替换原来的数据
            FileOutputStream fos = new FileOutputStream(dexFile);
            fos.write(encryptBytes);
            fos.flush();
            fos.close();
        }
        return mainDexFile;
    }

    public static void encryptDex(File dexFile) throws Exception {
        //读数据
        byte[] buffer = Utils.getBytes(dexFile);
        //加密
        byte[] encryptBytes = AES.encrypt(buffer);

        //写数据  替换原来的数据
        FileOutputStream fos = new FileOutputStream(dexFile);
        fos.write(encryptBytes);
        fos.flush();
        fos.close();
    }

    public static byte[] encrypt(byte[] content) {
        try {
            byte[] result = encryptCipher.doFinal(content);
            return result;
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] decrypt(byte[] content) {
        try {
            byte[] result = decryptCipher.doFinal(content);
            return result;
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }




}
