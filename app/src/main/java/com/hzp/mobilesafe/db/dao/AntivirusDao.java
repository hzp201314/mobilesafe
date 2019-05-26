package com.hzp.mobilesafe.db.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;

/**
 * created by hzp on 2019/5/25 16:35
 * 作者：codehan
 * 描述：
 */
public class AntivirusDao {
    /**
     * 查询应用程序是否是病毒
     * md5 : 应用程序特征码的md5值
     *@return
     * 2016-10-26 上午9:46:23
     */
    public static boolean isAntivirus(Context context, String md5){
        boolean isAntiVirus=false;
        File file = new File(context.getFilesDir(), "antivirus.db");
        SQLiteDatabase database = SQLiteDatabase.openDatabase(file.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
        Cursor cursor = database.query("datable", new String[]{"md5"}, "md5=?", new String[]{md5}, null, null, null);
        if (cursor.moveToNext()) {
            isAntiVirus = true;
        }
        cursor.close();
        database.close();
        return isAntiVirus;
    }
}
