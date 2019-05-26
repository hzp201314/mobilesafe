package com.hzp.mobilesafe.service;


import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.view.accessibility.AccessibilityEvent;

import com.hzp.mobilesafe.activity.home.commonTool.AppUnLockActivity;
import com.hzp.mobilesafe.db.dao.AppLockDao;

import java.util.List;

/**
 * created by hzp on 2019/5/25 10:18
 * 作者：codehan
 * 描述：
 */
public class AppLockService2 extends AccessibilityService {
    private List<String> list;
    private AppLockDao appLockDao;
    private MyReceiver myReceiver;
    private String unlockPackageName;

    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // 判断接受的是那种广播
            String action = intent.getAction();// 获取接受的广播事件
            // 判断是否解锁的还是锁屏的广播接受者
            if ("com.hzp.mobilesafe.UNLOCK".equals(action)) {
                // 接受传递过来的解锁的应用程序的包名
                unlockPackageName = intent.getStringExtra("packageName");
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                // 重新加锁
                unlockPackageName = null;
            }

        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        appLockDao = new AppLockDao(this);
        list = appLockDao.queryAll();

        // 注册接受解锁的广播接受者
        myReceiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.hzp.mobilesafe.UNLOCK");
        // 锁屏重新加锁的操作
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(myReceiver, filter);

        //内容观察者
        Uri uri = Uri.parse("content://com.hzp.mobilesafe.UPDATESQLITE");
        //notifyForDescendents : 匹配模式，true:精确匹配，false:模糊匹配
        getContentResolver().registerContentObserver(uri, true, new ContentObserver(null) {
            //当数据库中的数据更新的时候调用的方法
            public void onChange(boolean selfChange) {
                list.clear();
                list = appLockDao.queryAll();
            };
        });
    }

    // 监听用户的行为的操作
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 获取用户打开的应用程序的报名，判断报名是否是加锁应用程序，实现加锁和解锁的操作
        String packageName = event.getPackageName().toString();
        if (list.contains(packageName)) {
            // 判断加锁的应用程序的包名和解锁的应用程序的包名是否一致，一致，表示加锁的应用已经解锁，不需要弹出解锁界面
            if (!packageName.equals(unlockPackageName)) {
                // 弹出解锁界面
                Intent intent = new Intent(AppLockService2.this,
                        AppUnLockActivity.class);
                // FLAG_ACTIVITY_NEW_TASK:
                // 如果跳转的activity没有保存的任务栈，创建一个新的，如果已经有保存的任务栈，直接保存到已有的任务栈中
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// 给跳转的activity指定保存的任务栈
                intent.putExtra("packageName", packageName);
                startActivity(intent);
            }
        }
    }

    // 接受反馈信息的操作
    @Override
    public void onInterrupt() {
        // TODO Auto-generated method stub

    }

}
