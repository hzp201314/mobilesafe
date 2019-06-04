package com.hzp.mobilesafe.db;

/**
 * created by hzp on 2019/5/25 09:42
 * 作者：codehan
 * 描述：程序锁数据库信息及建表sql语句
 */
public interface AppLockConstants {
    /** 数据库的名称 **/
    public static final String DB_NAME = "applock.db";

    /** 数据库的版本 **/
    public static final int DB_VERSION = 1;

    // 为了方便后期修改表的名称和字段的名称，把表名和字段名也抽取出来
    /** 表名 **/
    public static final String TABLE_NAME = "info";

    /** _id字段的名称 **/
    public static final String ID = "_id";
    /** 包名，用于唯一标识已加锁程序 **/
    public static final String PACKAGENAME = "packagename";

    /** 创建表的sql语句 **/
    public static final String DB_SQL = "create table " + TABLE_NAME + "(" + ID
            + " integer primary key autoincrement," + PACKAGENAME
            + " varchar(100))";



}
