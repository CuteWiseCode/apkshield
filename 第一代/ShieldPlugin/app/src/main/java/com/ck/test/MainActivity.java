package com.ck.test;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Process;


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