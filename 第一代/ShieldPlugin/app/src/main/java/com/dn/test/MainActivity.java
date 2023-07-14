package com.dn.test;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import cn.wjdiankong.main.ParserChunkUtils;


public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        startService(new Intent(this,TestService.class));
        System.out.println("MainActivity:"+ Process.myPid());
//        System.out.println("TestService:"+ getClassLoader());


    }

    public static void print(){


        System.out.println("我是主进程的 ");
    }



}