package com.hzp.mobilesafe.activity.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;

import com.hzp.mobilesafe.R;
import com.hzp.mobilesafe.service.AddressService;
import com.hzp.mobilesafe.service.BlackNumberService;
import com.hzp.mobilesafe.utils.Constants;
import com.hzp.mobilesafe.utils.ServiceUtil;
import com.hzp.mobilesafe.utils.SharedPreferencesUtil;
import com.hzp.mobilesafe.view.MyDialog;
import com.hzp.mobilesafe.view.SettingView;

public class SettingActivity extends Activity {

    private SettingView mUpdate;
    private SettingView mBlackNumber;
    private SettingView mAddress;
    private SettingView mAddressStyle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_setting );
        initView();
    }
    /**
     * 初始化控件
     *
     * 2016-10-8 下午3:12:38
     */
    private void initView() {
        mUpdate = (SettingView) findViewById(R.id.setting_sv_update);
        mBlackNumber = (SettingView) findViewById(R.id.setting_sv_blacknumber);
        mAddress = (SettingView) findViewById(R.id.setting_sv_address);
        mAddressStyle = (SettingView) findViewById(R.id.setting_sv_addressstyle);
        //设置自动更新的条目的点击事件
        update();
        // 设置骚扰拦截的条目的点击事件
        blacknumber();
        // 设置号码归属地设置条目的点击事件，因为也是开启关闭服务，参考骚扰拦截
        address();
        //设置归属地显示风格的条目点击事件
        addressStyle();
    }
    /**
     * 设置自动更新的条目的点击事件
     *
     * 2016-10-8 下午3:13:29
     */
    private void update() {

        /*再次进入界面的时候，获取保存的开关状态，*/
        boolean b = SharedPreferencesUtil.getBoolean( getApplicationContext(), Constants.ISUPDATE, true );
        /*根据保存的开关状态，设置界面开关操作*/
        mUpdate.setToggleOn( b );

        mUpdate.setOnClickListener( new OnClickListener() {

            @Override
            public void onClick(View v) {
                //开启 ->点击关闭
                //关闭 -> 点击开启
                //问题：点击事件是在activity实现的，但是更改的图片是在自定义控件中
                //mUpdate.setToggleOn(true);
                //获取开关状态，根据开关状态来实现开启和关闭的切换操作
				/*if (mUpdate.istoggle()) {
					mUpdate.setToggleOn(false);
				}else{
					mUpdate.setToggleOn(true);
				}*/
				/*改变开关状态*/
                mUpdate.toggle();
                //开启关闭成功，保存开关状态
                SharedPreferencesUtil.saveBoolean( getApplicationContext(), Constants.ISUPDATE, mUpdate.istoggle() );
            }
        } );
    }

    /**
     * 开启关闭骚扰拦截服务的操作
     *
     * 2016-10-16 上午9:30:47
     */
    private void blacknumber() {

        // 1.开启/关闭服务
        mBlackNumber.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 关闭 -> 点击开启服务
                // 开启 -> 点击关闭服务
                // 需要知道服务是否开启
                // 不能SharedPreferences的原因：因为在系统的设置操作可以，可以手动的停止服务，手动停止服务因为是在系统的设置界面中的，所以不好更改SharedPreferences保存的值
                // 动态的获取服务是否开启
                Intent intent = new Intent(SettingActivity.this,
                        BlackNumberService.class);
                if (ServiceUtil.isServiceRunning(SettingActivity.this,
                        "com.hzp.mobilesafe.service.BlackNumberService")) {
                    // 开启 -> 点击关闭服务
                    stopService(intent);
                } else {
                    // 关闭 -> 点击开启服务
                    startService(intent);
                }
                mBlackNumber.toggle();
            }
        });
    }

    /**
     * 号码归属地操作
     *
     * 2016-10-16 下午4:52:11
     */
    private void address() {
        // 1.开启/关闭服务
        mAddress.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this,
                        AddressService.class);
                if (ServiceUtil.isServiceRunning(SettingActivity.this,
                        "com.hzp.mobilesafe.service.AddressService")) {
                    // 开启 -> 点击关闭服务
                    stopService(intent);
                } else {
                    // 关闭 -> 点击开启服务
                    startService(intent);
                }
                mAddress.toggle();
            }
        });
    }

    /**
     * 归属地显示风格设置
     *
     * 2016-10-17 上午10:34:46
     */
    private void addressStyle() {
        mAddressStyle.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //显示自定义的dialog
                MyDialog myDialog = new MyDialog(SettingActivity.this);
                myDialog.show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 2.再次进入的时候，判断服务是否开启，设置开关状态
        boolean b = ServiceUtil.isServiceRunning(this,
                "com.hzp.mobilesafe.service.BlackNumberService");
        mBlackNumber.setToggleOn(b);
        //回显号码归属地操作
        boolean isAddress = ServiceUtil.isServiceRunning(this,
                "com.hzp.mobilesafe.service.AddressService");
        mAddress.setToggleOn(isAddress);
    }

}
