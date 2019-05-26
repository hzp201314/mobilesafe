package com.hzp.mobilesafe.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hzp.mobilesafe.db.AppLockConstants;
import com.hzp.mobilesafe.db.AppLockOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * created by hzp on 2019/5/25 09:40
 * 作者：codehan
 * 描述：
 */
public class AppLockDao {
    //清华同方面试题：同时操作数据库如何解决：Dao操作同步锁+OpenHelper单例模式（可选）

    private AppLockOpenHelper appLockOpenHelper;

    public AppLockDao(Context context) {
        appLockOpenHelper = new AppLockOpenHelper(context);
    }

    /**
     * 添加数据的操作
     *
     * @param packageName
     *            ： 包名
     * 2016-10-14 上午9:32:23
     */
    public boolean add(String packageName) {
        //参数：保证一个
        //synchronized (AppLockDao.class) {
        SQLiteDatabase database = appLockOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        // key:数据库表中的字段名
        // values:添加的数据
        values.put( AppLockConstants.PACKAGENAME, packageName);
        // 参数1：表名
        // 参数2：Sqlite数据库不能直接添加null操作，如果添加数据是null，sqlite数据库会在添加数据相应的列中设置为null
        // 参数3：添加的数据
        long insert = database.insert(AppLockConstants.TABLE_NAME, null,
                values);

        // 判断是否添加成功的操作
        return insert != -1;
        //}
    }

    /**
     * 程序锁解锁操作
     *
     * @param packageName
     *            2016-10-14 上午9:41:32
     */
    public boolean delete(String packageName) {
        SQLiteDatabase database = appLockOpenHelper.getWritableDatabase();
        // whereClause : 查询条件
        // whereArgs : 查询条件的参数（具体的值）
        int delete = database.delete(AppLockConstants.TABLE_NAME,
                AppLockConstants.PACKAGENAME + "=?",
                new String[] { packageName });

        System.out.println(delete);

        return delete != 0;
    }

    // 查询单个数据
    /**
     * 程序锁判断是否枷锁，判断数据库是否有相应的报名
     *
     * @param packageName
     * @return 2016-10-14 上午9:52:14
     */
    public boolean queryMode(String packageName) {
        //数据库是否存在数据
        boolean isHave = false;

        SQLiteDatabase database = appLockOpenHelper.getReadableDatabase();
        // columns :设置查询哪一列的数据
        // selection : 查询条件
        // selectionArgs : 查询条件的参数
        // groupBy : 分组
        // having : 去重
        // orderBy : 排序
        Cursor cursor = database.query(AppLockConstants.TABLE_NAME,
                new String[] { AppLockConstants.PACKAGENAME },
                AppLockConstants.PACKAGENAME + "=?",
                new String[] { packageName }, null, null, null);
        if (cursor.moveToNext()) {
            isHave = true;
        }
        // 关闭
        cursor.close();
        database.close();
        return isHave;
    }

    // 查询全部数据
    /**
     * 查询全部数据的操作
     *
     * 2016-10-14 上午10:21:12
     */
    public List<String> queryAll() {

        List<String> list = new ArrayList<String>();

        SQLiteDatabase database = appLockOpenHelper.getReadableDatabase();
        Cursor cursor = database.query(AppLockConstants.TABLE_NAME,
                new String[] { AppLockConstants.PACKAGENAME}, null, null, null, null,
                null);
        while (cursor.moveToNext()) {
            String packageName = cursor.getString(0);

            // 将bean类存放到集合中，方便listview显示
            list.add(packageName);
        }
        // 关闭数据库
        cursor.close();
        database.close();
        return list;
    }

}
