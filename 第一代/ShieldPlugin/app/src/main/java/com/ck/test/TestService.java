package com.ck.test;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Process;

import androidx.annotation.Nullable;


public class TestService extends Service {


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        System.out.println("haha");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MainActivity.print();
        System.out.println("TestService:"+ Process.myPid());
        System.out.println("TestService:"+ getClassLoader());
        return super.onStartCommand(intent, flags, startId);
    }
}
