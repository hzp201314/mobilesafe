package com.hzp.mobilesafe.view;

import android.content.Context;
import android.graphics.Rect;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.widget.TextView;
/**
 * 自定义textview
 *
 *2016-10-7  下午5:08:24
 */
public class HomeTextView extends TextView {
	//在代码中使用的时候调用
	public HomeTextView(Context context) {
		//super(context);
		//HomeTextView homeTextView = new HomeTextView(context);
		this(context,null);
	}
	//在布局文件中使用的时候调用的方法
	//布局文件中的控件最终都会通过反射的形式，转化成代码，在转化的代码中new的时候调用的方法
	//控件的所有属性都会保存到AttributeSet
	public HomeTextView(Context context, AttributeSet attrs) {
		//super(context, attrs);
		this(context,attrs,-1);
	}
	//在控件的内部让两个参数的构造函数调用的
	public HomeTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		/**
		 * 		android:singleLine="true"
                android:ellipsize="marquee"
                android:focusableInTouchMode="true"
                android:marqueeRepeatLimit="marquee_forever"
		 */
		setSingleLine();//使用代码设置单行显示
		setEllipsize(TruncateAt.MARQUEE);//使用代码设置滚动操作
		setFocusableInTouchMode(true);//使用代码设置触摸获取焦点
		setMarqueeRepeatLimit(-1);//设置滚动次数
	}

	//是否允许textview拥有焦点，true:允许，false:不允许
	@Override
	public boolean isFocused() {
		return true;
	}
	//焦点切换调用的方法
	//focused : 焦点是否释放
	//direction : 焦点移动的方向
	//previouslyFocusedRect : 焦点从哪个控件过来
	@Override
	protected void onFocusChanged(boolean focused, int direction,
			Rect previouslyFocusedRect) {
		//当焦点被抢夺的时候，不能抢夺textview的焦点
		//如果焦点没有被抢夺，调用系统的方法，帮我们保留焦点
		//如果焦点被抢夺了，禁止调用系统的方法，禁止系统移除焦点
		if (focused) {
			super.onFocusChanged(focused, direction, previouslyFocusedRect);
		}
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	


}
