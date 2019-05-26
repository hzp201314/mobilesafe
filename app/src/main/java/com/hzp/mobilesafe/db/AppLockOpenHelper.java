package com.hzp.mobilesafe.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * created by hzp on 2019/5/25 09:41
 * 作者：codehan
 * 描述：
 */
public class AppLockOpenHelper extends SQLiteOpenHelper {

    //设置数据库的信息，创建数据库
    //name : 数据库的名称
    //factory : 游标工厂
    //version : 数据库的版本
    public AppLockOpenHelper(Context context) {
        super(context, AppLockConstants.DB_NAME, null, AppLockConstants.DB_VERSION);
    }
    //创建表结构
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(AppLockConstants.DB_SQL);
    }
    //更新数据库的时候调用的方法
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
