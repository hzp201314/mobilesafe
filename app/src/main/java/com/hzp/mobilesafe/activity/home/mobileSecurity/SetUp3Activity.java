package com.hzp.mobilesafe.activity.home.mobileSecurity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.hzp.mobilesafe.R;
import com.hzp.mobilesafe.utils.Constants;
import com.hzp.mobilesafe.utils.SharedPreferencesUtil;

public class SetUp3Activity extends BaseActivity {

    private EditText mSafeNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_set_up3 );
        initView();
    }

    private void initView() {
        mSafeNumber = (EditText) findViewById(R.id.setup3_et_safenumber);

        //再次进入的界面的时候，获取保存的安全号码，如果有，设置个输入框回显，如果没有显示默认的信息
        String sp_safenumber = SharedPreferencesUtil.getString(getApplicationContext(), Constants.SAFENUMBER, "");
        if (!TextUtils.isEmpty(sp_safenumber)) {
            mSafeNumber.setText(sp_safenumber);
        }
    }

    @Override
    public boolean pre_activity() {
        Intent intent = new Intent(this,SetUp2Activity.class);
        startActivity(intent);
        return false;
    }

    @Override
    public boolean next_activity() {
        //判断用户是否输入安全号码
        String safenumber = mSafeNumber.getText().toString().trim();
        if (TextUtils.isEmpty(safenumber)) {
            Toast.makeText(getApplicationContext(), "请输入安全号码", 0).show();
            return true;
        }
        //如果输入的号码不为空，保存安全号码
        SharedPreferencesUtil.saveString(getApplicationContext(), Constants.SAFENUMBER, safenumber);

        Intent intent = new Intent(this,SetUp4Activity.class);
        startActivity(intent);
        return false;
    }

    /**
     * 选择安全号码的按钮的点击事件
     * @param view
     */
    public void selectContacts(View view) {
        Intent intent = new Intent(this,ContactActivity.class);
        startActivityForResult(intent, 0);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //接受ContactActivity传递过来的数据
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                String number = data.getStringExtra("number");//获取传递过来的数据
                mSafeNumber.setText(number);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
