package com.hzp.mobilesafe.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.hzp.mobilesafe.db.dao.AddressDao;
import com.hzp.mobilesafe.view.CustomToast;

public class AddressService extends Service {
    private TelephonyManager tel;
    private MyphoneStateListener listener;
    private CustomToast customToast;
    private MyOutGoingCall myOutGoingCall;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }
    private class MyOutGoingCall extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //显示外拨电话的号码归属地
            String number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            String address = AddressDao.getAddress(context, number);
            if (!TextUtils.isEmpty(address)) {
                customToast.showToast(address);
            }
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        customToast = new CustomToast(this);
        // 1.外拨显示号码归属地
        //代码注册广播接受者，监听外拨电话的广播事件
        //1.1.创建广播接受者
        myOutGoingCall = new MyOutGoingCall();
        //1.2.设置接受的广播事件
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.NEW_OUTGOING_CALL");//设置接受的外拨电话的广播事件
        //1.3.注册广播接受者
        registerReceiver(myOutGoingCall, filter);


        // 2.来电显示号码归属地
        // 监听电话的状态，当响铃状态的时候，显示来电号码的归属地
        tel = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        listener = new MyphoneStateListener();
        tel.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    private class MyphoneStateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:// 空闲状态，挂断状态
                    //隐藏自定义的toast
                    customToast.hideToast();
                    break;
                case TelephonyManager.CALL_STATE_RINGING:// 响铃的状态
                    //获取来电号码归属地，通过toast显示
                    String address = AddressDao.getAddress(AddressService.this, incomingNumber);
                    if (!TextUtils.isEmpty(address)) {
                        //Toast.makeText(getApplicationContext(), address, 0).show();
                        customToast.showToast(address);
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:// 通话的状态

                    break;
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //1.4.注销广播接受者
        unregisterReceiver(myOutGoingCall);
        //关闭服务的时候，停止监听电话状态
        tel.listen(listener, PhoneStateListener.LISTEN_NONE);//设置不再监听任何事件
    }

}
