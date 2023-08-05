package com.example.protectapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;


public class TestService extends Service {


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        System.out.println("haha");
        return null;
    }
}
