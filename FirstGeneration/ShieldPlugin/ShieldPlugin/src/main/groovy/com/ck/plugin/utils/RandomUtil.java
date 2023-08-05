package com.ck.plugin.utils;

import java.util.Random;


/**
 * @Author: ck
 * @Date: 2022/10/13
 * @Desc: 生成一个随机的字符串
 */
public class RandomUtil {
    private static RandomUtil instance = null;

   public static RandomUtil getInstance() {
        if (instance == null) {
            instance = new RandomUtil();
        }

        return instance;
    }


    /**
     * 获取随机秘钥字符串
     * @return
     */
    public static String getRandomkey() {
        LogUtil.log("getRandomkey");
        int count = 16;
        Random random = new Random();
        LogUtil.log(" new Random();");
        String str = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder();
        LogUtil.log("source str "+str);
        for (int j = 0; j < count; j++) {
            int number = random.nextInt(26);
            sb.append(str.charAt(number));
        }

        return sb.toString();
    }

}