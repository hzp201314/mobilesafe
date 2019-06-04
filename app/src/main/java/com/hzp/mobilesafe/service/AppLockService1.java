package com.hzp.mobilesafe.service;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.IBinder;
import android.os.SystemClock;

import com.hzp.mobilesafe.activity.home.commonTool.AppUnLockActivity;
import com.hzp.mobilesafe.db.dao.AppLockDao;

import java.util.List;

/**
 * created by hzp on 2019/5/25 10:18
 * 作者：codehan
 * 描述：程序锁服务监听打开应用操作
 */
public class AppLockService1 extends Service {

    /**监听是否执行**/
    private boolean isRunning;
    private ActivityManager am;
    private AppLockDao appLockDao;
    private MyReceiver myReceiver;
    private String unlockPackageName;
    private List<String> list;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //判断接受的是那种广播
            String action = intent.getAction();//获取接受的广播事件
            //判断是否解锁的还是锁屏的广播接受者
            if ("com.hzp.mobilesafe.UNLOCK".equals(action)) {
                //接受传递过来的解锁的应用程序的包名
                unlockPackageName = intent.getStringExtra("packageName");
            }else if(Intent.ACTION_SCREEN_OFF.equals(action)){
                //重新加锁
                unlockPackageName=null;
            }

        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        appLockDao = new AppLockDao(this);
        isRunning = true;
        //时时刻刻监听用户打开的应用程序，如果是加锁的应用程序，弹出解锁界面
        //解决：每隔一段时间监听一次，只要间隔时间短一些

        //注册接受解锁的广播接受者
        myReceiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.hzp.mobilesafe.UNLOCK");
        //锁屏重新加锁的操作
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(myReceiver, filter);

        am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        isLock();
    }
    /**
     * 监听用户打开的应用程序的
     *
     */
    private void isLock() {
        new Thread(){
            public void run() {

                //因为每隔200毫秒，打开关闭查询一次数据，比较浪费资源，因为最终的目标就是查询包名在不在数据库中，所以可以先把数据库中的数据一次性查询出来，放到内存中（list）,
                //然后去内存中查询包名是否在数据中
                //1.获取数据库的所有数据，放到内存中
                list = appLockDao.queryAll();
                //但是，当有新的加锁应用的时候，发现数据库更新了，但是内存中的数据没有更新
                //解决：使用内容观察者，设置当数据库改变的时候，更新内存中的数据了
                //注册内容观察者观察更新通知消息、
                Uri uri = Uri.parse("content://com.hzp.mobilesafe.UPDATESQLITE");
                //notifyForDescendents : 匹配模式，true:精确匹配，false:模糊匹配
                getContentResolver().registerContentObserver(uri, true, new ContentObserver(null) {
                    //当数据库中的数据更新的时候调用的方法
                    public void onChange(boolean selfChange) {
                        list.clear();
                        list = appLockDao.queryAll();
                    };
                });

                //死循环，容易阻塞主线程
                while(isRunning){

                    //监听用户打开的应用程序了
                    //获取正在运行的任务栈
                    //maxNum : 获取正在运行的任务栈的上限个数
                    List<RunningTaskInfo> runningTasks = am.getRunningTasks(1);
                    for (RunningTaskInfo runningTaskInfo : runningTasks) {
                        ComponentName baseactivity = runningTaskInfo.baseActivity;//获取任务栈栈底的activity
                        //runningTaskInfo.topActivity;//获取栈顶的activity
                        String packageName = baseactivity.getPackageName();//根据应用程序的任务栈的activity获取应用程序的包名
                        //System.out.println(packageName);
                        //判断打开应用程序是否是加锁的应用程序
                        //2.判断包名是否在集合中（集合中的数据就是数据库中全部数据获取到内存中的操作）
                        //contains : 判断集合是否包含某个数据
                        if (list.contains(packageName)) {
                            //判断加锁的应用程序的包名和解锁的应用程序的包名是否一致，一致，表示加锁的应用已经解锁，不需要弹出解锁界面
                            if (!packageName.equals(unlockPackageName)) {
                                //弹出解锁界面
                                Intent intent = new Intent(AppLockService1.this,AppUnLockActivity.class);
                                //FLAG_ACTIVITY_NEW_TASK: 如果跳转的activity没有保存的任务栈，创建一个新的，如果已经有保存的任务栈，直接保存到已有的任务栈中
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//给跳转的activity指定保存的任务栈
                                intent.putExtra("packageName", packageName);
                                startActivity(intent);
                            }
                        }
                    }
                    SystemClock.sleep(200);
                }
            };
        }.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        //注销广播接受者操作
        unregisterReceiver(myReceiver);
    }
}
