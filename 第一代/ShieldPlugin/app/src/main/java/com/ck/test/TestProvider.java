package com.ck.test;

import androidx.core.content.FileProvider;


public class TestProvider extends FileProvider {
    @Override
    public boolean onCreate() {
        System.out.println("--------------onCreate---------TestProvider :"+ this.getClass().getClassLoader());
        return super.onCreate();
    }
}
