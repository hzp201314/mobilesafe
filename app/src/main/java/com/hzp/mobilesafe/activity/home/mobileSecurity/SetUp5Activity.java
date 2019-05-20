package com.hzp.mobilesafe.activity.home.mobileSecurity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;

import com.hzp.mobilesafe.R;
import com.hzp.mobilesafe.utils.Constants;
import com.hzp.mobilesafe.utils.SharedPreferencesUtil;

public class SetUp5Activity extends BaseActivity {

    private CheckBox mProtected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_set_up5 );
        initView();
    }

    private void initView() {
        mProtected = (CheckBox) findViewById(R.id.setup5_cb_protected);

        //2.当再次进入界面的时候，获取保存的状态，设置checkbox的选中状态
        boolean b = SharedPreferencesUtil.getBoolean(getApplicationContext(), Constants.PROTECTED, false);
        mProtected.setChecked(b);//设置checkbox是否选中

        //1.监听checkbox的选中操作，保存选中的状态
        mProtected.setOnCheckedChangeListener( new OnCheckedChangeListener() {
            //当checkbox执行选中操作的时候调用方法
            //buttonView : checkbox
            //isChecked : checkbox的当前的选中的状态
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //保存checkbox的选中状态的操作
                SharedPreferencesUtil.saveBoolean(getApplicationContext(), Constants.PROTECTED, isChecked);
            }
        });
    }

    @Override
    public boolean pre_activity() {
        Intent intent = new Intent(this, SetUp4Activity.class);
        startActivity(intent);
        return false;
    }

    @Override
    public boolean next_activity() {
        //判断是否开启防盗保护
        //判断checkbox是否选中
        //isChecked : 获取checkbox是否选中的操作
        if (!mProtected.isChecked()) {
            Toast.makeText(getApplicationContext(), "请开启防盗保护", 0).show();
            return true;
        }

        // 保存设置完成的标示
        SharedPreferencesUtil.saveBoolean(getApplicationContext(),
                Constants.ISSJFDSETTING, true);

        Intent intent = new Intent(this, LostFindActivity.class);
        startActivity(intent);
        return false;
    }

}
