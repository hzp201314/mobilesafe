package com.hzp.mobilesafe.db.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hzp.mobilesafe.bean.CommonNumberChildInfo;
import com.hzp.mobilesafe.bean.CommonNumberGroupsInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 查询常用电话号码数据库数据操作
 */
public class CommonNumberDao {
    //查询组的数据
    /**
     * 获取组的数据
     *
     */
    public static List<CommonNumberGroupsInfo> getGroup(Context context){

        List<CommonNumberGroupsInfo> list = new ArrayList<CommonNumberGroupsInfo>();

        File file = new File(context.getFilesDir(), "commonnum.db");
        SQLiteDatabase database = SQLiteDatabase.openDatabase(file.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
        Cursor cursor = database.query("classlist", new String[]{"name","idx"}, null, null, null, null, null);
        //解析cursor获取数据
        while(cursor.moveToNext()){
            String name = cursor.getString(0);
            String idx = cursor.getString(1);

            //根据组的idx获取组对应的孩子的数据
            List<CommonNumberChildInfo> child = getChild(context, idx);
            //如何绑定组合孩子的数据，在获取组的数据的时候，根据组的idx直接去获取孩子的数据，将获取的孩子的数据，保存到组的bean类
            //将获取的数据保存到bean类中
            CommonNumberGroupsInfo groupsInfo = new CommonNumberGroupsInfo(name, idx,child);
            //将bean类保存的list集合中，方便listview显示操作
            list.add(groupsInfo);
        }
        //关闭数据库
        cursor.close();
        database.close();
        return list;
    }
    //查询组的孩子的数据
    /**
     * 获取组的孩子的数据
     *  idx : 组的idx
     *
     */
    public static List<CommonNumberChildInfo> getChild(Context context,String idx){
        List<CommonNumberChildInfo> list = new ArrayList<CommonNumberChildInfo>();
        File file = new File(context.getFilesDir(), "commonnum.db");
        SQLiteDatabase database = SQLiteDatabase.openDatabase(file.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
        Cursor cursor = database.query("table"+idx, new String[]{"number","name"}, null, null,null, null, null);
        while(cursor.moveToNext()){
            String number = cursor.getString(0);
            String name = cursor.getString(1);
            CommonNumberChildInfo childInfo = new CommonNumberChildInfo(number, name);
            list.add(childInfo);
        }
        cursor.close();
        database.close();
        return list;
    }
}
