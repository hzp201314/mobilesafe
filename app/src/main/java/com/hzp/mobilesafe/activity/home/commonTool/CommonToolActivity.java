package com.hzp.mobilesafe.activity.home.commonTool;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.hzp.mobilesafe.R;
import com.hzp.mobilesafe.view.SettingView;

public class CommonToolActivity extends Activity implements View.OnClickListener {

    private SettingView mAddress;
    private SettingView mCommonNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_common_tool );
        initView();
    }

    private void initView() {
        mAddress = (SettingView) findViewById(R.id.commontool_sv_address);
        mCommonNumber = (SettingView) findViewById(R.id.commontool_sv_commonnumber);
        //设置点击事件
        mAddress.setOnClickListener(this);
        //设置常用号码的条目的点击事件
        mCommonNumber.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.commontool_sv_address:
                //跳转到号码归属地查询界面
                Intent intent = new Intent(CommonToolActivity.this,AddressActivity.class);
                startActivity(intent);
                break;
            case R.id.commontool_sv_commonnumber:
                //跳转到常用号码的操作
                Intent intent2 = new Intent(CommonToolActivity.this,CommonNumberActivity.class);
                startActivity(intent2);
                break;
        }
    }
}
