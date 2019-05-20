package com.hzp.mobilesafe.service;



import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.ITelephony;
import com.hzp.mobilesafe.db.dao.BlackNumberDao;

import java.lang.reflect.Method;

public class BlackNumberService extends Service {
    private SmsReceiver smsReceiver;
    private BlackNumberDao blackNumberDao;
    private TelephonyManager tel;
    private MyPhoneStateListener listener;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }
    /**
     * 短信广播接受者
     *
     *2016-10-16  上午10:14:19
     */
    private class SmsReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //1.5.接受解析短信，获取发件人的拦截类型，进行拦截设置了
            //70个汉字是一条短信，如果71个汉字是两条短信
            Object[] objs = (Object[]) intent.getExtras().get("pdus");
            for (Object obj : objs) {
                //转化成短信对象
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) obj);
                //获取发件人的号码
                String sender = smsMessage.getOriginatingAddress();
                //获取短信的内容
                String body = smsMessage.getMessageBody();

                //判断发件人的拦截类型，设置是否拦截
                int mode = blackNumberDao.queryMode(sender);
                if (mode == 1 || mode == 2) {
                    //拦截短信
                    abortBroadcast();
                }

            }
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        blackNumberDao = new BlackNumberDao(this);
        //1.短信拦截
        //判断发件人的拦截类型
        //代码注册广播接受者
        //1.1.广播接受者
        smsReceiver = new SmsReceiver();
        //1.2.设置接受的广播事件
        IntentFilter filter = new IntentFilter();
        filter.setPriority(Integer.MAX_VALUE);
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");//设置接受的广播事件
        //1.3.注册广播接受者
        registerReceiver(smsReceiver, filter);

        //2.电话拦截
        //监听电话的状态
        //2.1.获取电话的管理者
        tel = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        //2.2.监听电话的事件
        listener = new MyPhoneStateListener();
        //listener : 回调监听
        //events : 监听的事件
        tel.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
    }
    /**电话的监听操作**/
    private class MyPhoneStateListener extends PhoneStateListener{
        //监听电话状态的方法
        //state : 电话的状态
        //incomingNumber : 来电号码
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE://空闲的状态，挂断的状态

                    break;
                case TelephonyManager.CALL_STATE_RINGING://响铃状态
                    //2.4.获取来电话的拦截类型，判断是否应该挂断电话
                    int mode = blackNumberDao.queryMode(incomingNumber);
                    if (mode == 0 || mode == 2) {
                        //挂断电话
                        endCall();

                        //删除通话记录
                        deleteCallLog(incomingNumber);
                    }

                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK://通话状态，接听状态

                    break;
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    }

    /**
     * 挂断电话
     *
     * 2016-10-16 上午10:37:23
     */
    public void endCall() {
        //1.5版本
        //tel.endCall();
        try {
            //因为ServiceManager被系统隐藏了，所以我们不能使用，如果想使用，需要通过反射进行操作
            //1.获取ServiceManager的.class文件的对象（字节码），根据类型的全类名，获取字节码文件
            Class<?> loadClass = Class.forName("android.os.ServiceManager");
            //2.从字节码文件中获取相应的方法
            //参数1：方法名
            //参数2：方法所需参数的类型的.class形式
            Method method = loadClass.getDeclaredMethod("getService", String.class);
            //3.执行获取到的方法
            //参数1：方法所在类型的对象，如果方法是静态的方法，可以为null，如果方法不是静态的，一定要写方法所在类的对象
            //参数2：方法执行所需的参数
            IBinder iBinder = (IBinder) method.invoke(null, Context.TELEPHONY_SERVICE);
            //4.将方法的返回结果设置给相应的方法，使用
            //ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
            ITelephony iTelephony = ITelephony.Stub.asInterface(iBinder);
            //5.挂断电话了
            iTelephony.endCall();
        }catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    /**
     * 删除通话记录的
     *
     * 2016-10-16 上午11:18:36
     */
    public void deleteCallLog(final String blacknumber) {
        //contacts2.db数据库的calls表中数据
        final ContentResolver contentResolver = getContentResolver();
        //需要查看Contacts的内容提供的源码,在清单文件中找到CallLogProvider，其中有authorities属性表示主机地址,再去查看CallLogPrivoder.java的源码通过UriMatcher找到分配的分地址
        //http://www.baidu.com/jdk
        final Uri uri = Uri.parse("content://call_log/calls");
        //发现始终都有保留有一个条通话记录，原因：因为通话记录是保存在数据库中的，写数据库的操作是耗时操作，需要时间的，当删除表中的数据的时候，系统还没有把最新的通话添加到表中，所以我们删除的都是老的
        //通话记录，当我们删除成功的之后，系统才把最新的通话记录添加到数据库中
        //解决：只要监听内容提供者数据库的变化，当系统将新的通话记录添加到数据库之后，再去执行删除操作
        //通过内容观察者，观察内容提供者数据是否发生变化了
        //注册内容观察者
        //参数1：uri地址
        //参数2：匹配模式，true:精确匹配，false:模糊匹配
        //参数3：内容观察者
        contentResolver.registerContentObserver(uri, true, new ContentObserver(new Handler()) {
            //当内容提供者数据发生变化调用的方法
            @Override
            public void onChange(boolean selfChange) {
                //删除通话记录
                contentResolver.delete(uri, "number=?", new String[]{blacknumber});
                //删除成功，内容观察者就没有了，所以要注销内容观察者
                contentResolver.unregisterContentObserver(this);
                super.onChange(selfChange);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //1.4.当服务退出，注销广播接受者
        unregisterReceiver(smsReceiver);
        //2.3.服务关闭，停止监听电话状态的操作
        tel.listen(listener, PhoneStateListener.LISTEN_NONE);//设置不在监听任何状态，停止监听操作
    }
}
