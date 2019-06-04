package com.hzp.mobilesafe.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hzp.mobilesafe.R;

public class CustomProgressBar extends RelativeLayout {


    private TextView mText;
    private ProgressBar mPB;
    private TextView mLeft;
    private TextView mRight;
    public CustomProgressBar(Context context) {
        //super(context);
        this(context,null);
    }

    public CustomProgressBar(Context context, AttributeSet attrs) {
        //super(context, attrs);
        this(context,attrs,-1);
    }

    public CustomProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        initView();
    }
    /**
     * 将进度条布局添加自定义控件中
     */
    private void initView() {
        View view = View.inflate(getContext(), R.layout.customprogress, null);
        this.addView(view);//将view对象添加到自定控件中

        //初始化控件
        mText = (TextView) view.findViewById(R.id.customprgress_tv_text);
        mPB = (ProgressBar) view.findViewById(R.id.customprgress_pb_progress);
        mLeft = (TextView) view.findViewById(R.id.customprgress_tv_left);
        mRight = (TextView) view.findViewById(R.id.customprgress_tv_right);
    }

    //核心操作，给初始化出来的控件设置数据
    /**
     * 提供给activity使用的，用来设置Text控件的值
     *@param text : activity传递过来的设置给textview显示的值
     */
    public void setText(String text){
        mText.setText(text);
    }

    /**
     * 提供给activity使用的，用来设置left控件的值
     *@param left ：activity传递过来的设置给textview显示的值
     */
    public void setLeft(String left){
        mLeft.setText(left);
    }

    /**
     * 提供给activity使用的，用来设置right控件的值
     *@param Right : activity传递过来的设置给textview显示的值
     */
    public void setRight(String Right){
        mRight.setText(Right);
    }

    /**
     * 提供给activity使用的，设置进度
     *@param progress ： activity传递过来的进度
     */
    public void setProgress(int progress){
        mPB.setProgress(progress);//设置progressbar的进度
    }

}
