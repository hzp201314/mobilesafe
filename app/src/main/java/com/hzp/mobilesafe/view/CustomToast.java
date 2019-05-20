package com.hzp.mobilesafe.view;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.hzp.mobilesafe.R;

/**
 * created by hzp on 2019/5/20 16:04
 * 作者：codehan
 * 描述：
 */
public class CustomToast {
    private Context mContext;
    private WindowManager windowManager;
    private View view;

    public CustomToast(Context context){
        this.mContext = context;
    }

    /**
     * 显示自定义的toast
     *@param address : 要显示的号码归属地
     * 2016-10-16 下午5:11:00
     */
    public void showToast(String address){
        //将一个veiw对象添加到窗口中显示就可以了
        //1.得到窗口的管理者
        windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        //2.设置toast的布局样式
        view = View.inflate(mContext, R.layout.custom, null);
        //初始化控件，显示号码归属地
        TextView mAddress = (TextView) view.findViewById(R.id.custom_tv_address);
        //将号码归属地设置给textview显示
        mAddress.setText(address);

        //LayoutParams : 使用代码设置控件的属性，效果跟布局文件中控件属性是一样的效果
        //LayoutParams使用规则：view对象添加到那个父控件，就使用那个父控件的LayoutParams
        //比如：将控件添加RelativeLayout可以使用layout_centerVertical属性，但是放到LinearLayout中就没有layout_centerVertical，所以如果控件想要使用layout_centerVertical就必须作为RelativeLayout子子控件才可以使用
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.height = WindowManager.LayoutParams.WRAP_CONTENT; //设置高度，包裹内容
        params.width = WindowManager.LayoutParams.WRAP_CONTENT; // 设置宽度，包裹内容
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE //设置不能获取焦点
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE   // 不能触摸
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;  // 保持屏幕长亮
        params.format = PixelFormat.TRANSLUCENT;  //设置背景半透明
        params.type = WindowManager.LayoutParams.TYPE_TOAST;//设置控件的类型是toast类型

        //3.将view对象添加windowManager中显示
        windowManager.addView(view, params);
    }

}
