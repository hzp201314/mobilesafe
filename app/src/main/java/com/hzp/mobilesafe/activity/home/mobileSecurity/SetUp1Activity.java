package com.hzp.mobilesafe.activity.home.mobileSecurity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.hzp.mobilesafe.R;

public class SetUp1Activity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_set_up1 );
    }
    @Override
    public boolean pre_activity() {
        //第一个界面的上一步是不能执行跳转操作
        return true;
    }

    @Override
    public boolean next_activity() {
        Intent intent = new Intent(this,SetUp2Activity.class);
        startActivity(intent);
        return false;
    }

}
