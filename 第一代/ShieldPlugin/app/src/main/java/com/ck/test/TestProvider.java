package com.ck.test;

import androidx.core.content.FileProvider;

/**
 * @Author: hzh
 * @Date: 2022/12/15
 * @Desc: java类作用描述
 */
public class TestProvider extends FileProvider {
    @Override
    public boolean onCreate() {
        System.out.println("--------------onCreate---------TestProvider :"+ this.getClass().getClassLoader());
        return super.onCreate();
    }
}
