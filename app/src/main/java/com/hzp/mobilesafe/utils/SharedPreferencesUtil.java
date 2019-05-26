package com.hzp.mobilesafe.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SharedPreferences的工具类
 *
 *2016-10-8  下午3:48:41
 */
public class SharedPreferencesUtil {

	private static SharedPreferences sp;

	/**
	 * 保存boolean信息的操作
	 *
	 * 静态内存一直存在
	 */
	public static void saveBoolean(Context context,String key,boolean value){
		//name : 保存的信息xml文件的名称
		//mode : 操作SharedPreferences的权限
		if (sp==null) {
			/*创建config.xml文件*/
			sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
		}
		//保存数据
		//key : 保存信息名称
		//value : 保存的信息
		sp.edit().putBoolean(key, value).commit();
	}
	/**
	 * 获取boolean值操作
	 *@return
	 *
	 */
	public static boolean getBoolean(Context context,String key,boolean defValue){
		if (sp==null) {
			sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
		}
		return sp.getBoolean(key, defValue);
	}
	
	
	
	/**
	 * 保存String信息的操作
	 *
	 * 2016-10-8 下午3:49:05
	 */
	public static void saveString(Context context,String key,String value){
		//name : 保存的信息xml文件的名称
		//mode : 操作SharedPreferences的权限
		if (sp==null) {
			sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
		}
		//保存数据
		//key : 保存信息名称
		//value : 保存的信息
		sp.edit().putString(key, value).commit();
	}
	/**
	 * 获取String值操作
	 *@return
	 * 2016-10-8 下午3:53:26
	 */
	public static String getString(Context context,String key,String defValue){
		if (sp==null) {
			sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
		}
		return sp.getString(key, defValue);
	}

	/**
	 * 保存int信息的操作
	 *
	 * 2016-10-8 下午3:49:05
	 */
	public static void saveInt(Context context,String key,int value){
		//name : 保存的信息xml文件的名称
		//mode : 操作SharedPreferences的权限
		if (sp==null) {
			sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
		}
		//保存数据
		//key : 保存信息名称
		//value : 保存的信息
		sp.edit().putInt(key, value).commit();
	}
	/**
	 * 获取int值操作
	 *@return
	 * 2016-10-8 下午3:53:26
	 */
	public static int getInt(Context context,String key,int defValue){
		if (sp==null) {
			sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
		}
		return sp.getInt(key, defValue);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
