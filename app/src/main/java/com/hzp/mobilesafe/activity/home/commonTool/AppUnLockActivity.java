package com.hzp.mobilesafe.activity.home.commonTool;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hzp.mobilesafe.R;

/**
 * 程序锁解锁界面
 */
public class AppUnLockActivity extends Activity {

    private String packageName;
    private ImageView mIcon;
    private TextView mName;
    private EditText mPsw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_app_un_lock );
        // 接受传递过来的加锁应用程序的包名
        packageName = getIntent().getStringExtra("packageName");

        initView();
    }

    /**
     * 初始化控件的操作
     *
     */
    private void initView() {
        mIcon = (ImageView) findViewById(R.id.appunlock_iv_icon);
        mName = (TextView) findViewById(R.id.appunlock_tv_name);
        mPsw = (EditText) findViewById(R.id.appunlock_et_psw);

        getMessage();
    }

    /**
     * 根据包名获取应用程序信息，展示应用程序的信息
     *
     */
    private void getMessage() {
        PackageManager pm = getPackageManager();
        try {
            ApplicationInfo applicationInfo = pm.getApplicationInfo(
                    packageName, 0);
            String name = applicationInfo.loadLabel(pm).toString();
            Drawable icon = applicationInfo.loadIcon(pm);

            // 设置显示
            mIcon.setImageDrawable(icon);
            mName.setText(name);
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /*
     * I/ActivityManager(1007): START { act=android.intent.action.MAIN
     * cat=[android.intent.category.HOME]
     * cmp=com.android.launcher/com.android.launcher2.Launcher u=0 } from pid
     * 1559
     */
    // 点击返回键调用的方法
    @Override
    public void onBackPressed() {
        // 跳转到桌面
        Intent intent = new Intent();
        intent.setAction("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        startActivity(intent);
        super.onBackPressed();
    }

    /**
     * 点击home键最小化再次打开其他加锁应用界面时信息不更新
     * 界面不可见时结束activity
     */
    @Override
    protected void onStop() {
        finish();
        super.onStop();
    }
    /**
     * 解锁的点击事件
     *@param view
     *
     */
    public void unlock(View view) {
        //获取输入的密码
        String psw = mPsw.getText().toString().trim();
        if ("123".equals( psw )) {
            //密码正确
            //告诉服务，该应用已经解锁，不要在加锁了
            //问题：如何告诉服务
            //解决：1.广播，2：回调
            Intent intent = new Intent();
            intent.setAction( "com.hzp.mobilesafe.UNLOCK" );
            //告诉服务解锁的时候那个应用程序
            intent.putExtra( "packageName", packageName );
            sendBroadcast( intent );//发送自定义的广播
            finish();
        } else {
            //密码错误
            Toast.makeText( getApplicationContext(), "密码错误", 0 ).show();
        }
    }
}
