package com.qihoo.util;


import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class a {

    private static final String algorithmStr = "AES/CBC/PKCS5Padding";

    private static Cipher decryptCipher;


    public static a getInstance(){
        return new a();
    }

    public  void init() {
        try {
            String password = a();
            // 生成一个实现指定转换的 Cipher 对象。
            decryptCipher = Cipher.getInstance(algorithmStr);// algorithmStr
            byte[] keyStr = password.getBytes();
            SecretKeySpec key = new SecretKeySpec(keyStr, "AES");
            decryptCipher.init(Cipher.DECRYPT_MODE, key,new IvParameterSpec(new byte[decryptCipher.getBlockSize()]));

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }



    public static byte[] decrypt(byte[] content) {
        try {
            return decryptCipher.doFinal(content);
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }


    private String a(){
        return "abcdefghijklmnop";
    }



}
