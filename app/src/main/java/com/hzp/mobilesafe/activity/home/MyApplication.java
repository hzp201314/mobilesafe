package com.hzp.mobilesafe.activity.home;

import android.app.Application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

/**
 * created by hzp on 2019/5/25 16:49
 * 作者：codehan
 * 描述：自定义application
 * Application ： 相当于应用程序，程序在运行的是，先执行的application，再执行的activity
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("application启动了.....");
        //currentThread : 获取当前的线程
        //setUncaughtExceptionHandler : 设置监听异常
        Thread.currentThread().setUncaughtExceptionHandler(new MyUncaughtExceptionHandler());
    }

    private class MyUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
        //当有未捕获的异常的时候调用的方法
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            System.out.println("哥发现了异常，哥捕获了异常");
            try {
                ex.printStackTrace(new PrintStream(new File("mnt/sdcard/error.log")));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            //自己杀死自己（闪退）
            android.os.Process.killProcess(android.os.Process.myPid());//myPid() : 获取当前进程的pid
        }
    }
}
