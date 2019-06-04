package com.hzp.mobilesafe.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.RemoteViews;

import com.hzp.mobilesafe.R;
import com.hzp.mobilesafe.activity.home.processManager.ProcessManagerActivity;
import com.hzp.mobilesafe.engine.ProcessEngine;
import com.hzp.mobilesafe.receiver.WidgetReceiver;

/**
 * created by hzp on 2019/5/25 08:49
 * 作者：codehan
 * 描述：桌面小程序更新服务
 */
public class WidgetService extends Service {

    private AppWidgetManager appWidgetManager;
    private MyReceiver myReceiver;
    private AlarmManager alarmManager;
    private PendingIntent operation;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * 广播接收者
     * 在广播接收者里面更新桌面小控件操作
     */
    private class MyReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d( "WidgetService", "onReceive: widget更新了..." );
            ComponentName provider = new ComponentName(context, WidgetReceiver.class);
            //参数1：当前应用程序的包名
            //参数2：布局文件的id
            RemoteViews views =  new RemoteViews(context.getPackageName(), R.layout.process_widget);

            //远程布局不能findviewbyid
            //viewId : 控件的id
            //text : 显示的文本
            views.setTextViewText(R.id.process_count, "正在运行的软件:"+ ProcessEngine.getRunningProcessCount(context));//给相应控件设置文本
            views.setTextViewText(R.id.process_memory, "可用内存:"+ Formatter.formatFileSize(context, ProcessEngine.getFreeMemory(context)));

            //设置桌面小控件的点击事件
            Intent enter = new Intent(context,ProcessManagerActivity.class);
            PendingIntent enterprocess = PendingIntent.getActivity(context, 101, enter, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.process_ll_root, enterprocess);//设置控件的点击事件，点击事件通过pendingIntent进行操作

            //一键清理操作
            //因为点击一键清理，没有跳转，没有开启服务，只能发送自定义的广播
            //发送自定义广播需要定义广播接收者，法1：代码定义广播接收者处理; 法2：清单文件中注册
            Intent clearIntent = new Intent();
            clearIntent.setAction("com.hzp.mobilesafe.CLEAR_PROCESS");
            PendingIntent clear = PendingIntent.getBroadcast(context, 102, clearIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.btn_clear, clear);

            //ComponentName : 组件的标示
            //RemoteViews :远程布局，在当前应用程序中创建布局文件，但是没有在当前应用程序中使用，而是在其他应用程序中使用，这个布局文件对于当前应用程序来说就是远程布局
            appWidgetManager.updateAppWidget(provider, views);

        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        //更新桌面小控件内容
        appWidgetManager = AppWidgetManager.getInstance(this);
        //每隔一段时间更新一次，定时器，handler,timer，while(true){sleep(3000)},alarmManager(闹铃的管理者)
		/*Timer timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

			}
		}, 3000);*///3000 : 间隔时间

        //2.因为定时操作是每隔3秒钟发送一个自定义广播，所以需要创建广播接受者接受广播
        myReceiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.hzp.mobilesafe.WIDGET_UPDATE");
        registerReceiver(myReceiver, filter);

        //1.获取闹铃的管理者
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        //2.发送闹铃的间隔消息
        Intent intent = new Intent();
        intent.setAction("com.hzp.mobilesafe.WIDGET_UPDATE");
        //get....对应的intent中的startActivity,startService,sendbrodcast
        //getBroadcast : 发送广播，因为是我们自己发送，发送的肯定是自定义的广播事件
        //requestCode : 请求码
        //intent : pendingIntent中封装的intent,决定PendingIntent的具体操作
        //FLAG_CANCEL_CURRENT : 更新的时候，放弃老数据，直接创建新数据更新
        //FLAG_UPDATE_CURRENT : 更新的时候，直接更新老数据
        operation = PendingIntent.getBroadcast(this, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //type : 闹铃类型
            //RTC ：使用硬件的时间，发送闹铃不通过手机提醒
            //RTC_WAKEUP : 使用硬件的时间，发送闹铃通过手机提醒
            //ELAPSED_REALTIME : 使用真实的时间，发送闹铃不通过手机提醒
            //ELAPSED_REALTIME_WAKEUP : 使用真实的时间，发送闹铃通过手机提醒
        //triggerAtMillis : 当前的时间
        //intervalMillis : 间隔时间
        //operation : 执行的操作,PendingIntent:延迟意图，类似点击事件的使用方式，只有触发条件生效，才会执行intent操作
        alarmManager.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), 3000, operation);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myReceiver);
        //取消定时操作
        alarmManager.cancel(operation);//取消定时操作
    }





}
