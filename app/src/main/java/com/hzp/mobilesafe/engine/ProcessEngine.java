package com.hzp.mobilesafe.engine;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Service;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;


import com.hzp.mobilesafe.R;
import com.hzp.mobilesafe.bean.ProcessInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * created by hzp on 2019/5/24 21:13
 * 作者：codehan
 * 描述：获取进程数和内存信息
 */
public class ProcessEngine {
    /**
     * 获取正在运行的进程数
     *@return
     *
     */
    public static int getRunningProcessCount(Context context){
        //进程管理者
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //am.getRunningAppProcesses();//获取正在运行的进程的信息
        return am.getRunningAppProcesses().size();
    }
    /**
     * 获取总的进程数
     *@return
     *
     */
    public static int getAllProcessCount(Context context){
        //一个应用程序运行在一个进程中的，所以理论上一个应用程序对应一个进程
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> installedPackages = pm.getInstalledPackages(PackageManager.GET_ACTIVITIES | PackageManager.GET_PROVIDERS | PackageManager.GET_RECEIVERS | PackageManager.GET_SERVICES);
        //因为android允许应用的四大组件单独存放到一个一个进程，所以有时候就不是一个应用程序对应一个进程，可能是一个应用程序对应多个进程
        Set<String> set = new HashSet<String>();/*集合保存数据不能重复*/
        for (PackageInfo packageInfo : installedPackages) {
            //将应用程序的进程名称保存到set中
            //processName : 进程名称
            set.add(packageInfo.applicationInfo.processName);
            //获取四大组件所在的进程
            ActivityInfo[] activities = packageInfo.activities;//获取应用程序中清单文件中所有acitivity信息
            if (activities != null) {
                for (ActivityInfo activityInfo : activities) {
                    //activityInfo.processName : 组件所在的进程的名称
                    set.add(activityInfo.processName);
                }
            }
            ProviderInfo[] providers = packageInfo.providers;//获取应用程序中清单文件中所以内容提供者信息
            if (providers != null) {
                for (ProviderInfo providerInfo : providers) {
                    set.add(providerInfo.processName);
                }
            }
            ActivityInfo[] receivers = packageInfo.receivers;//获取应用程序中清单文件中所有广播接受者信息
            if (receivers != null) {
                for (ActivityInfo activityInfo : receivers) {
                    set.add(activityInfo.processName);
                }
            }
            ServiceInfo[] services = packageInfo.services;//获取应用程序中清单文件中所有服务信息
            if (services != null) {
                for (ServiceInfo serviceInfo : services) {
                    set.add(serviceInfo.processName);
                }
            }

        }
        return set.size();
    }

    /**
     * 获取空闲内存
     *@param context
     *@return
     *
     */
    public static long getFreeMemory(Context context){
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        MemoryInfo outInfo = new MemoryInfo();
        am.getMemoryInfo(outInfo);//获取进程内存信息，保存MemoryInfo对象中
        return outInfo.availMem;//获取可用的内存
        //outInfo.totalMem;//获取总的内存
    }

    /**
     * 获取总内存
     *@param context
     *@return
     *
     */
    @SuppressLint("NewApi")
    public static long getALLMemory(Context context){
        long totalmem=-1;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        MemoryInfo outInfo = new MemoryInfo();
        am.getMemoryInfo(outInfo);//获取进程内存信息，保存MemoryInfo对象中
        //return outInfo.availMem;//获取可用的内存
        //return outInfo.totalMem;//获取总的内存
        //因为totalMem字段是16版本以上才有的，如果想要在低版本中使用，需要处理版本兼容的问题
        //16版本以上使用totalMem，16版本以下，通过另外的方法进行实现
        //问题：如何知道当前的Android系统的sdk版本
        //Build.VERSION.SDK_INT : 获取当前android系统的sdk版本
        if (Build.VERSION.SDK_INT >= 16) {
            totalmem =  outInfo.totalMem;
        }else{
            //通过另外的方法进行实现
            totalmem = getALLMemory();
        }
        return totalmem;
    }

    /**
     * 重载方法，通过另外的方式获取进程的总内存
     *@return
     * @Deprecated : 方法过时
     */
    @Deprecated
    public static long getALLMemory(){
        //可以通过读取proc/meminfo中的第一行总内存的信息进行操作
        try {
            File file = new File("proc/meminfo");
            BufferedReader br = new BufferedReader(new FileReader(file));
            String readLine = br.readLine();

            //除去前边和后边的文本
            readLine = readLine.replace("MemTotal:", "");//使用newChar替换oldChar的文本
            readLine = readLine.replace("kB", "");
            //将获取数字的文本转化成long类型的数据
            Long valueOf = Long.valueOf(readLine);

            //需要将kb -> b   1kb = 1024b
            return valueOf * 1024;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 0;
    }



    /**
     * 获取正在运行的进程的信息
     *
     */
    public static List<ProcessInfo> getRunningProcessInfo(Context context){

        List<ProcessInfo> list = new ArrayList<ProcessInfo>();

        //1.获取进程的管理者
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        PackageManager pm = context.getPackageManager();
        //2.获取所有正在运行的进程的信息
        List<RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();
        //3.遍历集合，获取每个正在运行的进行的具体信息
        for (RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses) {

            ProcessInfo info = new ProcessInfo();
            //包名
            String packageName = runningAppProcessInfo.processName;//获取进程名称，进程的名称就是应用程序的包名
            //保存包名
            info.packageName = packageName;

            //获取占用内存的大小
            //int[] pids : 进程的id的数组，传递几个进程的id到数组中，最终就会返回几个进程占用的内存大小
            //runningAppProcessInfo.pid :获取进程id
            android.os.Debug.MemoryInfo[] memoryInfo = am.getProcessMemoryInfo(new int[]{runningAppProcessInfo.pid});//获取进程占用的内存的大小
            int totalPss = memoryInfo[0].getTotalPss();//获取内存大小，返回单位是kb
            long size = totalPss * 1024;//kb -> b
            //保存内存大小
            info.size = size;

            try {
                //获取名称图标和是否是系统进程
                //packageName : 包名
                //flags : 额外的信息标示
                ApplicationInfo applicationInfo = pm.getApplicationInfo(packageName, 0);
                //名称
                String name = applicationInfo.loadLabel(pm).toString();
                //保存名称
                info.name = name;

                //图标
                Drawable icon = applicationInfo.loadIcon(pm);
                //保存图标
                info.icon = icon;

                //是否是系统进程
                int flags = applicationInfo.flags;
                boolean isSystem;
                if ((flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) {
                    //系统进程
                    isSystem = true;
                }else{
                    //用户进程
                    isSystem = false;
                }
                //保存是否是系统进程
                info.isSystem = isSystem;

            } catch (NameNotFoundException e) {
                //名称信息没有找到的异常
                //没有获取到进程的名称等信息，设置默认的信息
                info.name = packageName;
                info.icon = context.getResources().getDrawable( R.drawable.ic_default);
                info.isSystem = true;
                e.printStackTrace();
            }
            //将bean类保存到集合中，方便listview进行操作
            list.add(info);
        }
        return list;
    }

    /**
     * 清理所有进程
     *
     */
    public static void killALLProcess(Context context){
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> allProcess = am.getRunningAppProcesses();
        for (RunningAppProcessInfo runningAppProcessInfo : allProcess) {
            //屏蔽不能清理当前应用程序的进程
            if (!runningAppProcessInfo.processName.equals(context.getPackageName())) {
                am.killBackgroundProcesses(runningAppProcessInfo.processName);
            }
        }
    }



}
