package com.hzp.mobilesafe.activity.home.mobileSecurity;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.hzp.mobilesafe.R;
import com.hzp.mobilesafe.receiver.AdminReceiver;

public class SetUp4Activity extends BaseActivity {

    private RelativeLayout mAdmin;
    private ImageView mIcon;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName componentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_set_up4 );
        initView();
    }

    private void initView() {
        mAdmin = (RelativeLayout) findViewById(R.id.setup4_rel_admin);
        mIcon = (ImageView) findViewById(R.id.setup4_iv_icon);

        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        componentName = new ComponentName(this, AdminReceiver.class);

        //2.再次进入界面的时候，回显激活状态
        if (devicePolicyManager.isAdminActive(componentName)) {
            mIcon.setImageResource(R.drawable.admin_activated);
        }else{
            mIcon.setImageResource(R.drawable.admin_inactivated);
        }


        //1.激活/取消激活超级管理员权限
        mAdmin.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 激活 -> 取消激活
                // 取消激活 -> 激活

                // 需要知道设备管理员权限是否激活
                if (devicePolicyManager.isAdminActive(componentName)) {
                    // 激活 -> 取消激活
                    // 当取消设备管理员权限的时候，还有将设置过的密码规则取消
                    devicePolicyManager.resetPassword("", 0);
                    devicePolicyManager.removeActiveAdmin(componentName);// 取消设备管理员权限
                    // 更改图标
                    mIcon.setImageResource(R.drawable.admin_inactivated);
                } else {
                    // 表示激活超级管理员权限
                    Intent intent = new Intent(
                            DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                    // 设置激活那个超级管理员权限
                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                            componentName);
                    // 设置描述信息
                    intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                            "手机卫士");
                    startActivityForResult(intent, 0);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // 当退出系统的激活界面的时候，判断设备管理员权限是否激活，激活，显示激活的图标，没有激活，显示没有激活的图标
        if (devicePolicyManager.isAdminActive(componentName)) {
            mIcon.setImageResource(R.drawable.admin_activated);
        } else {
            mIcon.setImageResource(R.drawable.admin_inactivated);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean pre_activity() {
        Intent intent = new Intent(this,SetUp3Activity.class);
        startActivity(intent);
        return false;
    }

    @Override
    public boolean next_activity() {
        //3.判断是否激活设备管理员权限，激活直接进行跳转，没有激活提醒用户，并禁止跳转
        if (!devicePolicyManager.isAdminActive(componentName)) {
            Toast.makeText(getApplicationContext(), "请先激活设备管理器", 0).show();
            return true;
        }
        Intent intent = new Intent(this,SetUp5Activity.class);
        startActivity(intent);
        return false;
    }
}
