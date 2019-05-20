package com.hzp.mobilesafe.receiver;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.telephony.SmsMessage;
import android.util.Log;

import com.hzp.mobilesafe.R;
import com.hzp.mobilesafe.service.GPSService;

/**
 * created by hzp on 2019/5/20 13:56
 * 作者：codehan
 * 描述：
 */
public class SmsReceiver extends BroadcastReceiver {
    private static final String TAG="SmsReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        //接受解析短信的操作
        //获取到短信
        //70个汉字是一条短信，如果71个汉字是两条短信
        Object[] objs = (Object[]) intent.getExtras().get("pdus");
        for (Object obj : objs) {
            //转化成短信对象
            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) obj);
            //获取发件人的号码
            String sender = smsMessage.getOriginatingAddress();
            //获取短信的内容
            String body = smsMessage.getMessageBody();

            Log.d( TAG, "onReceive: 发件人："+sender+"  短信内容："+body );
        }
    }

    /**
     * 判断短信是否是指令
     *@param body
     * 2016-10-13 上午9:50:01
     */
    private void isMessage(String body,Context context) {
        //设备的管理者
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        //组件的标示,如果想要使用组件，获取组件一些信息，但是又没有办法去创建组件的对象来获取，就可以通过组件的标示来获取
        ComponentName componentName = new ComponentName(context, AdminReceiver.class);

        if ("#*location*#".equals(body)) {
            //Gps追踪
            Log.d( TAG, "isMessage: Gps追踪" );
            //如果是定位的指令，执行定位操作
            //因为定位需要和gps定位卫星通讯，需要很长的时间，onReceive方法不支持执行很长时间的操作，所以将操作放到服务中进行实现
            //开启定位服务，进行定位操作
            context.startService(new Intent(context,GPSService.class));
            //如果是指令，需要拦截短信，不能让系统接收到短信
            abortBroadcast();//拦截广播，终止广播的操作，在原生的系统中没有问题，但是在国内某些定制系统中，没有这个功能，比如小米
        }else if("#*alarm*#".equals(body)){
            //播放报警音乐
            Log.d( TAG, "isMessage: 播放报警音乐" );
            //resid : res下的资源的id
            MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.ylzs);
            //设置音量大小
            //leftVolume rightVolume : 左右声道，比例计算
            mediaPlayer.setVolume(1.0f, 1.0f);
            mediaPlayer.setLooping(true);//是否循环播放
            mediaPlayer.start();

            abortBroadcast();
        }else if("#*wipedata*#".equals(body)){
            //远程销毁数据
            Log.d( TAG, "isMessage: 远程销毁数据" );

            //判断设备管理员权限是否激活，激活，进行操作处理，没有激活不进行任何操作
            //isAdminActive : 判断设备管理器是否激活
            if (devicePolicyManager.isAdminActive(componentName)) {
                //flags ：设置是销毁机身内存还是SD卡中的数据
                devicePolicyManager.wipeData(0);//销毁数据,跟恢复出厂设置相似
            }

            abortBroadcast();
        }else if("#*lockscreen*#".equals(body)){
            //远程锁屏
            Log.d( TAG, "isMessage: 远程锁屏" );
            if (devicePolicyManager.isAdminActive(componentName)) {
                //设置锁屏的密码
                //password : 密码
                //flags : 密码的规则
                devicePolicyManager.resetPassword("123", 0);
                devicePolicyManager.lockNow();//锁屏的操作
            }
            abortBroadcast();
        }
    }

    /**
     * 判断短信是否是指令
     *@param body
     * 2016-10-13 上午9:50:01
     */
    /*private void isMessage(String body,Context context) {

        //设备的管理者
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        //组件的标示,如果想要使用组件，获取组件一些信息，但是又没有办法去创建组件的对象来获取，就可以通过组件的标示来获取
        ComponentName componentName = new ComponentName(context, AdminReceiver.class);

        if ("#*location*#".equals(body)) {
            //Gps追踪
            System.out.println("Gps追踪");

            //如果是定位的指令，执行定位操作
            //因为定位需要和gps定位卫星通讯，需要很长的时间，onReceive方法不支持执行很长时间的操作，所以将操作放到服务中进行实现
            //开启定位服务，进行定位操作
            context.startService(new Intent(context,GPSService.class));

            //如果是指令，需要拦截短信，不能让系统接收到短信
            abortBroadcast();//拦截广播，终止广播的操作，在原生的系统中没有问题，但是在国内某些定制系统中，没有这个功能，比如小米
        }else if("#*alarm*#".equals(body)){
            //播放报警音乐
            System.out.println("播放报警音乐");
            //resid : res下的资源的id
            MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.ylzs);
            //设置音量大小
            //leftVolume rightVolume : 左右声道，比例计算
            mediaPlayer.setVolume(1.0f, 1.0f);
            mediaPlayer.setLooping(true);//是否循环播放
            mediaPlayer.start();

            abortBroadcast();
        }else if("#*wipedata*#".equals(body)){
            //远程销毁数据
            System.out.println("远程销毁数据");

            //判断设备管理员权限是否激活，激活，进行操作处理，没有激活不进行任何操作
            //isAdminActive : 判断设备管理器是否激活
            if (devicePolicyManager.isAdminActive(componentName)) {
                //flags ：设置是销毁机身内存还是SD卡中的数据
                devicePolicyManager.wipeData(0);//销毁数据,跟恢复出厂设置相似
            }

            abortBroadcast();
        }else if("#*lockscreen*#".equals(body)){
            //远程锁屏
            System.out.println("远程锁屏");

            if (devicePolicyManager.isAdminActive(componentName)) {
                //设置锁屏的密码
                //password : 密码
                //flags : 密码的规则
                devicePolicyManager.resetPassword("123", 0);
                devicePolicyManager.lockNow();//锁屏的操作
            }


            abortBroadcast();
        }*/
}
