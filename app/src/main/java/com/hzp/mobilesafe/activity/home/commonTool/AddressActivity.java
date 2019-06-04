package com.hzp.mobilesafe.activity.home.commonTool;

import android.app.Activity;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.hzp.mobilesafe.R;
import com.hzp.mobilesafe.db.dao.AddressDao;

public class AddressActivity extends Activity {

    private EditText mNumber;
    private TextView mAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_address );
        initView();
    }

    /**
     * 初始化控件
     *
     * 2016-10-16 下午3:15:30
     */
    private void initView() {
        mNumber = (EditText) findViewById(R.id.address_et_number);
        mAddress = (TextView) findViewById(R.id.address_tv_address);

        // 监听输入框时时输入内容的操作
        mNumber.addTextChangedListener( new TextWatcher() {
            // 在输入的内容改变的时候调用
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                String number = s.toString();// 获取输入的内容
                String address = AddressDao.getAddress(AddressActivity.this,
                        number);
                if (!TextUtils.isEmpty(address)) {
                    mAddress.setText("归属地:" + address);
                }
            }

            // 输入内容之前调用的方法
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            // 输入内容之后调用的方法
            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }
        });
    }

    /**
     * 查询按钮的点击事件
     *
     * @param view
     *
     */
    public void address(View view) {
        String number = mNumber.getText().toString().trim();
        if (!TextUtils.isEmpty( number )) {
            // 查询归属地
            String address = AddressDao.getAddress( this, number );
            if (!TextUtils.isEmpty( address )) {
                mAddress.setText( "归属地:" + address );
            }
        } else {
            Toast.makeText( getApplicationContext(), "请输入要查询的号码", 0 ).show();

            // loadAnimation : 加载动画 抖动效果
            Animation shake = AnimationUtils.loadAnimation( this, R.anim.shake );
            mNumber.startAnimation( shake );

            //振动 需要权限
            //振动的管理者
            Vibrator vibrator = (Vibrator) getSystemService( VIBRATOR_SERVICE );
            vibrator.vibrate( 100 );//振动，milliseconds：振动的时间，单位毫秒值
            //pattern : 振动的频率
            //repeat : 是否重复振动，-1不重复，非-1重复，值是多少，频率从哪里开始
            //vibrator.vibrate(new long[]{30l,60l,50l,70l}, 1);
        }
    }
}
