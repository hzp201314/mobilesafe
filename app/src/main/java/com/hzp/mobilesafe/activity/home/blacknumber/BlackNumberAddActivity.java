package com.hzp.mobilesafe.activity.home.blacknumber;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.hzp.mobilesafe.R;
import com.hzp.mobilesafe.db.dao.BlackNumberDao;

public class BlackNumberAddActivity extends Activity {

    private BlackNumberDao blackNumberDao;
    private EditText mBlackNumber;
    private RadioGroup mModes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_black_number_add );
        blackNumberDao = new BlackNumberDao(this);
        initView();
    }

    private void initView() {
        mBlackNumber = (EditText) findViewById(R.id.blacknumber_et_blacknumber);
        mModes = (RadioGroup) findViewById(R.id.blacknumber_rg_modes);
    }

    /**
     * 保存黑名单号码的操作
     *@param view
     * 2016-10-14 上午11:40:50
     */
    public void save(View view){
        //1.获取输入的号码，判断是否为空
        String blacknumber = mBlackNumber.getText().toString().trim();
        if (TextUtils.isEmpty(blacknumber)) {
            Toast.makeText(getApplicationContext(), "请输入黑名单号码", 0).show();
            //如果号码为空，不执行其他操作
            return;
        }
        //2.获取拦截类型，判断拦截类型是否选择
        int mode=-1;//初始化拦截类型
        //选中那个RadioButton，表示选中哪个类型
        int checkedRadioButtonId = mModes.getCheckedRadioButtonId();//获取被选中的RadioButton的id,返回RadioButton的id
        switch (checkedRadioButtonId) {
            case R.id.blacknumber_rbtn_call:
                //电话拦截
                mode = 0;
                break;
            case R.id.blacknumber_rbtn_sms:
                //短信拦截
                mode = 1;
                break;
            case R.id.blacknumber_rbtn_all:
                //全部拦截
                mode = 2;
                break;
            default:
                Toast.makeText(getApplicationContext(), "请选择拦截类型", 0).show();
                //break;
                return;
        }
        //3.添加数据
        //3.1.添加数据到数据库
        //3.1.1.判断数据库中是否已有号码
        //如果根据号码，没有从数据库中获取到拦截类型（返回-1）,表示该号码不在数据库中
        if (blackNumberDao.queryMode(blacknumber) == -1) {
            //3.1.2.将号码添加到数据库
            boolean add = blackNumberDao.add(blacknumber, mode);
            if (add) {
                Toast.makeText(getApplicationContext(), "号码添加成功", 0).show();

                //3.2.添加数据库成功，将数据返回给黑名单管理界面显示,并移除当前的界面
                Intent data = new Intent();
                data.putExtra("number", blacknumber);
                data.putExtra("mode", mode);
                setResult(Activity.RESULT_OK, data);

                finish();
            }else{
                Toast.makeText(getApplicationContext(), "系统繁忙，请稍候再试...", 0).show();
            }
        }else{
            Toast.makeText(getApplicationContext(), "号码已存在", 0).show();
        }


    }
    /**
     * 取消按钮的操作
     *@param view
     * 2016-10-14 上午11:41:03
     */
    public void cancel(View view){
        //移除activity操作
        finish();
    }
}
