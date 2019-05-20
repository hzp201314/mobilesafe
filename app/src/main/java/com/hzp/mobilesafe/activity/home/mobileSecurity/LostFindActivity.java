package com.hzp.mobilesafe.activity.home.mobileSecurity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hzp.mobilesafe.R;
import com.hzp.mobilesafe.utils.Constants;
import com.hzp.mobilesafe.utils.SharedPreferencesUtil;

public class LostFindActivity extends Activity{

    private TextView mAginEnter;
    private TextView mSafeNumber;
    private ImageView mIsProtected;
    private RelativeLayout mRelProtected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_lost_find );
        initView();
    }

    private void initView() {
        mAginEnter = (TextView) findViewById(R.id.lostfind_tv_aginenter);

        //设置重新进入设置向导的点击事件
        mAginEnter.setOnClickListener( new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LostFindActivity.this,SetUp1Activity.class);
                startActivity(intent);
                finish();
            }
        });

        //根据保存的安全号码和防盗保护是否开启的状态设置页面展示的信息
        mSafeNumber = (TextView) findViewById(R.id.lostfind_tv_safenumber);
        mIsProtected = (ImageView) findViewById(R.id.lostfind_iv_isprotected);
        mRelProtected = (RelativeLayout) findViewById(R.id.lostfind_rel_protected);
        //安全号码
        String sp_safenumber = SharedPreferencesUtil.getString(getApplicationContext(), Constants.SAFENUMBER,"");
        mSafeNumber.setText(sp_safenumber);
        //防盗保护是否开启的操作
        boolean sp_protected = SharedPreferencesUtil.getBoolean(getApplicationContext(), Constants.PROTECTED, false);
        if (sp_protected) {
            mIsProtected.setImageResource(R.drawable.lock);
        }else{
            mIsProtected.setImageResource(R.drawable.unlock);
        }
        //点击防盗保护是否开启的条目，实现快速开启和关闭防盗保护
        mRelProtected.setOnClickListener( new OnClickListener() {

            @Override
            public void onClick(View v) {
                //开启 -> 关闭      关闭 -> 开启
                //需要知道防盗保护是开启还是关闭
                boolean b = SharedPreferencesUtil.getBoolean(getApplicationContext(), Constants.PROTECTED, false);
                if (b) {
                    //开启 -> 关闭
                    SharedPreferencesUtil.saveBoolean(getApplicationContext(), Constants.PROTECTED, false);
                    //更改显示的图片
                    mIsProtected.setImageResource(R.drawable.unlock);
                }else{
                    //   关闭 -> 开启
                    SharedPreferencesUtil.saveBoolean(getApplicationContext(), Constants.PROTECTED, true);
                    //更改显示的图片
                    mIsProtected.setImageResource(R.drawable.lock);
                }
            }
        });
    }
}
