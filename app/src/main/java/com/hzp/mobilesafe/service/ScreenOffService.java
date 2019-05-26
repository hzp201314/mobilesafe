package com.hzp.mobilesafe.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.hzp.mobilesafe.engine.ProcessEngine;

/**
 * created by hzp on 2019/5/25 08:27
 * 作者：codehan
 * 描述：
 */
public class ScreenOffService extends Service {
    private ScreenOffReceiver screenOffReceiver;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    private class ScreenOffReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //清理所有进程的操作
            ProcessEngine.killALLProcess(context);
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        //锁屏清理进程的操作
        //设置监听锁屏的广播接受者
        screenOffReceiver = new ScreenOffReceiver();
        //设置接受的广播事件
        IntentFilter filter = new IntentFilter();
        //ACTION_SCREEN_OFF : 锁屏
        //ACTION_SCREEN_ON : 解锁
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(screenOffReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(screenOffReceiver);
    }

}
