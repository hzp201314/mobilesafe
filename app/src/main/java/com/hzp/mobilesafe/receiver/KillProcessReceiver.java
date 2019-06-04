package com.hzp.mobilesafe.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hzp.mobilesafe.engine.ProcessEngine;

/**
 * created by hzp on 2019/5/25 09:23
 * 作者：codehan
 * 描述：清理进程广播接收者
 */
public class KillProcessReceiver  extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //清理进程
        ProcessEngine.killALLProcess(context);
    }
}
