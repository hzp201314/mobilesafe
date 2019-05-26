package com.hzp.mobilesafe.activity.home.mobileSecurity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.hzp.mobilesafe.R;
import com.hzp.mobilesafe.utils.Constants;
import com.hzp.mobilesafe.utils.SharedPreferencesUtil;

public class SetUp2Activity extends BaseActivity {

    private RelativeLayout mRelSIM;
    private ImageView mIsLock;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_set_up2 );
        initView();
    }

    private void initView() {
        mRelSIM = (RelativeLayout) findViewById( R.id.setup2_rel_sim );
        mIsLock = (ImageView) findViewById( R.id.setup2_iv_islock );


        //2.再次进入界面，回显是否绑定的操作
        String sp_sim = SharedPreferencesUtil.getString( getApplicationContext(), Constants.SIM, "" );
        if (TextUtils.isEmpty( sp_sim )) {//未绑定
            mIsLock.setImageResource( R.drawable.unlock );
        } else {//绑定
            mIsLock.setImageResource( R.drawable.lock );
        }

        //1.点击绑定SIM卡
        //设置按钮的点击事件
        mRelSIM.setOnClickListener( new OnClickListener() {

            @Override
            public void onClick(View v) {
                //绑定/解绑SIM卡

                //绑定 -> 点击解绑
                //未绑定 -> 点击绑定
                //判断是否绑定
                String sp_sim = SharedPreferencesUtil.getString( getApplicationContext(), Constants.SIM, "" );
                if (TextUtils.isEmpty( sp_sim )) {//绑定
                    //未绑定 -> 点击绑定
                    //绑定SIM卡：本质保存SIM卡的序列号
                    //获取电话的管理者
                    TelephonyManager tel = (TelephonyManager) getSystemService( TELEPHONY_SERVICE );
                    //tel.getLine1Number();//获取和SIM卡绑定的电话号码，在中国有时候获取不到


                    if (checkSelfPermission( Manifest.permission.READ_PHONE_STATE ) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    Activity#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for Activity#requestPermissions for more details.

                    }else {
                        String sim = tel.getSimSerialNumber();//获取SIM卡的序列号，唯一标示
                        SharedPreferencesUtil.saveString(getApplicationContext(), Constants.SIM, sim);
                        //修改的图片
                        mIsLock.setImageResource(R.drawable.lock);
                    }

                }else{//解绑
                    //绑定 -> 点击解绑
                    SharedPreferencesUtil.saveString(getApplicationContext(), Constants.SIM, "");
                    //修改的图片
                    mIsLock.setImageResource(R.drawable.unlock);
                }

            }
        });


    }

    @Override
    public boolean pre_activity() {
        Intent intent = new Intent(this,SetUp1Activity.class);
        startActivity(intent);
        return false;
    }
    @Override
    public boolean next_activity() {

        //判断是否绑定sim卡，如果绑定跳转，如果没有绑定禁止跳转
        String sp_sim = SharedPreferencesUtil.getString(getApplicationContext(), Constants.SIM, "");
        if (TextUtils.isEmpty(sp_sim)) {
            Toast.makeText(getApplicationContext(), "请先绑定sim卡...", Toast.LENGTH_SHORT).show();
            return true;
        }
        Intent intent = new Intent(this,SetUp3Activity.class);
        startActivity(intent);
        return false;
    }
}
