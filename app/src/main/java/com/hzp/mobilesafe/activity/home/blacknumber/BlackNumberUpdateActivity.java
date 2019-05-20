package com.hzp.mobilesafe.activity.home.blacknumber;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.hzp.mobilesafe.R;
import com.hzp.mobilesafe.db.dao.BlackNumberDao;

public class BlackNumberUpdateActivity extends Activity {

    private BlackNumberDao blackNumberDao;
    private String blackNumber;
    private int mode;
    private int position;
    private EditText mBlackNumber;
    private RadioGroup mModes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_black_number_update );
        blackNumberDao = new BlackNumberDao(this);
        getValue();
        initView();
    }
    /**
     * 接受传递过来的回显数据
     *
     * 2016-10-14 下午3:16:05
     */
    private void getValue() {
        Intent intent = getIntent();// 获取跳转过来的intent
        blackNumber = intent.getStringExtra("number");
        mode = intent.getIntExtra("mode", -1);
        position = intent.getIntExtra("position", -1);
    }

    private void initView() {
        mBlackNumber = (EditText) findViewById(R.id.blacknumberupdate_et_blacknumber);
        mModes = (RadioGroup) findViewById(R.id.blacknumberupdate_rg_modes);

        // 将传递过来的数据设置控件显示
        setMsg();
    }

    /**
     * 将传递过来的数据设置给相应的控件显示
     *
     * 2016-10-14 下午3:20:14
     */
    private void setMsg() {
        // 1.显示号码
        mBlackNumber.setText( blackNumber );
        // 2.显示类型
        int checkId = -1;// 初始化Radiobutton的id保存变量
        // 因为传递来的是0,1,2,所以需要根据传递过来的数据，设置应该选中Radiobutton
        switch (mode) {
            case 0:
                // 电话的Radiobutton
                checkId = R.id.blacknumberupdate_rbtn_call;
                break;
            case 1:
                // 短信的Radiobutton
                checkId = R.id.blacknumberupdate_rbtn_sms;
                break;
            case 2:
                // 全部的Radiobutton
                checkId = R.id.blacknumberupdate_rbtn_all;
                break;
            default:
                // 如果不是012，设置默认选中全部Raddiobutton
                checkId = R.id.blacknumberupdate_rbtn_all;
                break;
        }
        mModes.check(checkId);// 设置选中哪个Radiobutton，id:Radiobutton的id
    }

    /**
     * 更新按钮的操作
     *
     * @param view
     *            2016-10-14 下午3:42:23
     */
    public void update(View view) {
        // 1.获取数据，跟添加界面一样的操作
        // 1.1.获取输入的号码，判断是否为空
        String updateblacknumber = mBlackNumber.getText().toString().trim();
        if (TextUtils.isEmpty(updateblacknumber)) {
            Toast.makeText(getApplicationContext(), "请输入黑名单号码", 0).show();
            // 如果号码为空，不执行其他操作
            return;
        }
        // 1.2.获取拦截类型，判断拦截类型是否选择
        int updatemode = -1;// 初始化拦截类型
        // 选中那个RadioButton，表示选中哪个类型
        int checkedRadioButtonId = mModes.getCheckedRadioButtonId();// 获取被选中的RadioButton的id,返回RadioButton的id
        switch (checkedRadioButtonId) {
            case R.id.blacknumberupdate_rbtn_call:
                // 电话拦截
                updatemode = 0;
                break;
            case R.id.blacknumberupdate_rbtn_sms:
                // 短信拦截
                updatemode = 1;
                break;
            case R.id.blacknumberupdate_rbtn_all:
                // 全部拦截
                updatemode = 2;
                break;
            default:
                Toast.makeText(getApplicationContext(), "请选择拦截类型", 0).show();
                // break;
                return;
        }
        // 2.更新数据库中的数据
        //2.1.更新数据库中的数据
        boolean update = blackNumberDao.update(updateblacknumber, updatemode);
        if (update) {
            Toast.makeText(getApplicationContext(), "更新成功", 0).show();

            //3.更新成功，将更新的数据回传给黑名单管理界面进行界面的更新显示操作
            Intent data = new Intent();
            //因为号码无法更改，所以不需要回传
            data.putExtra("mode", updatemode);
            data.putExtra("position", position );//将更新条目的索引回传给黑名单管理界面
            setResult(Activity.RESULT_OK, data);
            finish();
        }else{
            Toast.makeText(getApplicationContext(), "系统繁忙，请稍候再试..", 0).show();
        }
    }

    /**
     * 取消按钮的操作
     *
     * @param view
     *            2016-10-14 下午3:42:48
     */
    public void cancel(View view) {
        finish();
    }

}
