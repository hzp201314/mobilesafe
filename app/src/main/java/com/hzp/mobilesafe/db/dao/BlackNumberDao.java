package com.hzp.mobilesafe.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.SystemClock;
import android.util.Log;

import com.hzp.mobilesafe.bean.BlackNumberInfo;
import com.hzp.mobilesafe.db.BlackNumberConstants;
import com.hzp.mobilesafe.db.BlackNumberOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * created by hzp on 2019/5/20 15:07
 * 作者：codehan
 * 描述：查询黑名单数据库操作
 */
public class BlackNumberDao {

    private final BlackNumberOpenHelper blackNumberOpenHelper;

    public BlackNumberDao(Context context) {
        blackNumberOpenHelper = new BlackNumberOpenHelper(context);
    }

    // 增删改查
    /**
     * 添加数据的操作
     *
     * @param blacknumber
     *            ： 号码
     * @param mode
     * @return       ： 拦截类型
     */
    public boolean add(String blacknumber, int mode) {
        //得到数据库
        SQLiteDatabase database = blackNumberOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        // key:数据库表中的字段名
        // values:添加的数据
        values.put( BlackNumberConstants.BLACKNUMBER, blacknumber);
        values.put(BlackNumberConstants.MODE, mode);
        // 参数1：表名
        // 参数2：Sqlite数据库不能直接添加null操作，如果添加数据是null，sqlite数据库会在添加数据相应的列中设置为null
        // 参数3：添加的数据
        long insert = database.insert(BlackNumberConstants.TABLE_NAME, null,
                values);

        // 判断是否添加成功的操作 =-1添加失败 返回flase；  ！=-1添加成功 返回true
        return insert != -1;
    }

    /**
     * 根据号码删除表中对应的记录
     *
     * @param blackNumber
     * @return 0：删除失败，otherwise：删除成功
     */
    public boolean delete(String blackNumber) {
        SQLiteDatabase database = blackNumberOpenHelper.getWritableDatabase();
        // whereClause : 查询条件
        // whereArgs : 查询条件的参数（具体的值）
        int delete = database.delete(BlackNumberConstants.TABLE_NAME,
                BlackNumberConstants.BLACKNUMBER + "=?",
                new String[] { blackNumber });

        Log.d( "111", "delete: "+delete );

        return delete != 0;
    }

    /**
     * 根据号码更新拦截类型
     *
     * @param blacknumber
     *            ： 号码
     * @param mode
     * @return           ： 更新的拦截类型
     */
    public boolean update(String blacknumber, int mode) {
        SQLiteDatabase database = blackNumberOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BlackNumberConstants.MODE, mode);
        // values : 要更新的数据
        // whereClause : 查询条件
        // whereArgs : 查询条件的参数（具体的值）
        int update = database.update(BlackNumberConstants.TABLE_NAME, values,
                BlackNumberConstants.BLACKNUMBER + "=?",
                new String[] { blacknumber });

        Log.d( "111", "update: "+update );

        // 如果update是0标示更新失败
        return update != 0;
    }

    // 查询单个数据
    /**
     * 根据黑名单号码查询拦截类型
     *
     * @param blackNumber
     * @return
     */
    public int queryMode(String blackNumber) {

        int mode = -1;// 设置初始的默认值

        SQLiteDatabase database = blackNumberOpenHelper.getReadableDatabase();
        // columns :设置查询哪一列的数据
        // selection : 查询条件
        // selectionArgs : 查询条件的参数
        // groupBy : 分组
        // having : 去重
        // orderBy : 排序
        Cursor cursor = database.query(BlackNumberConstants.TABLE_NAME,
                new String[] { BlackNumberConstants.MODE },
                BlackNumberConstants.BLACKNUMBER + "=?",
                new String[] { blackNumber }, null, null, null);
        // 因为是根据黑名单号码查询拦截类型，一个黑名单号码对应的是一个拦截类型
        if (cursor.moveToNext()) {
            mode = cursor.getInt(0);
        }
        // 关闭
        cursor.close();
        database.close();
        return mode;
    }

    // 查询全部数据


    /**
     *  查询全部数据的操作
     * @return
     */
    public List<BlackNumberInfo> queryAll() {

        SystemClock.sleep(2000);

        List<BlackNumberInfo> list = new ArrayList<BlackNumberInfo>();

        SQLiteDatabase database = blackNumberOpenHelper.getReadableDatabase();
        Cursor cursor = database.query(BlackNumberConstants.TABLE_NAME,
                new String[] { BlackNumberConstants.BLACKNUMBER,
                        BlackNumberConstants.MODE }, null, null, null, null,
                "_id desc");// desc倒序查询，asc/默认不写：正序查询
        while (cursor.moveToNext()) {
            String blacknumber = cursor.getString(0);
            int mode = cursor.getInt(1);

            // 将黑名单号码和拦截类型保存到bean类中，方便后面使用
            BlackNumberInfo blackNumberInfo = new BlackNumberInfo(blacknumber,
                    mode);

            // 将bean类存放到集合中，方便listview显示
            list.add(blackNumberInfo);
        }
        // 关闭数据库
        cursor.close();
        database.close();
        return list;
    }

    /**
     * 查询部分数据的操作
     *
     * @param maxNum
     * @param startIndex
     * @return
     */
    public List<BlackNumberInfo> queryPartAll(int maxNum, int startIndex) {

        SystemClock.sleep(2000);

        List<BlackNumberInfo> list = new ArrayList<BlackNumberInfo>();

        SQLiteDatabase database = blackNumberOpenHelper.getReadableDatabase();

        // 参数1：执行的sql语句
        // 参数2：sql语句所需的参数
        Cursor cursor = database
                .rawQuery(
                        "select blacknumber,mode from info order by _id desc limit ? offset ?",
                        new String[] { maxNum + "", startIndex + "" });

        while (cursor.moveToNext()) {
            String blacknumber = cursor.getString(0);
            int mode = cursor.getInt(1);

            // 将黑名单号码和拦截类型保存到bean类中，方便后面使用
            BlackNumberInfo blackNumberInfo = new BlackNumberInfo(blacknumber,
                    mode);

            // 将bean类存放到集合中，方便listview显示
            list.add(blackNumberInfo);
        }
        // 关闭数据库
        cursor.close();
        database.close();
        return list;
    }
}
