package com.hzp.mobilesafe.utils;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;

/**
 * created by hzp on 2019/5/20 16:02
 * 作者：codehan
 * 描述：动态获取服务示范开启的操作
 */
public class ServiceUtil {
    /**
     * 动态获取服务是否开启的操作
     *@param className ： 服务的全类名
     * 2016-10-16 上午9:37:35
     */
    public static boolean isServiceRunning(Context context, String className){
        //1.进程的管理者（活动的管理者）
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //2.获取进程中正在运行的服务的集合
        //参数：获取的服务的个数的上限，没有1000,有多少返回多少，超过1000，只返回1000个
        List<RunningServiceInfo> runningServices = am.getRunningServices(1000);
        //3.遍历正在运行的服务的集合中是否有我们的服务
        for (RunningServiceInfo runningServiceInfo : runningServices) {
            //获取正在运行的服务的组件标示
            ComponentName service = runningServiceInfo.service;
            //ShortClassName() : 获取短类名，不包含包名的类名
            //获取正在运行的服务的全类名
            String clsname = service.getClassName();
            //判断正在运行的服务的全类名跟我们的服务的全类名是否一致
            if (className.equals(clsname)) {
                return true;
            }
        }
        return false;
    }
}
