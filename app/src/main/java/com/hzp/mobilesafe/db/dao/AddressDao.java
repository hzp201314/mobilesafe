package com.hzp.mobilesafe.db.dao;

import java.io.File;
import java.nio.charset.UnmappableCharacterException;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
/**
 * created by hzp on 2019/5/20 16:06
 * 作者：codehan
 * 描述：号码归属地数据库的查询操作
 */
public class AddressDao {
    /**
     * 根据号码查询号码归属地
     *
     * @return 2016-10-16 下午3:02:00
     */
    public static String getAddress(Context context, String number) {
        String location = "";
        // 查询数据库
        File file = new File(context.getFilesDir(), "address.db");
        // 1.打开数据库
        // 打开已有的数据
        // 参数1：数据库的路径
        // 参数2：游标工厂
        // 参数3：操作的权限
        // getAbsolutePath() : 获取文件的绝对路径
        SQLiteDatabase database = SQLiteDatabase.openDatabase(
                file.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
        // 2.查询数据库
        //判断号码的操作，号码的位数，号码的规则：第一位1，第二位34578，第三位0-9
        //正则表达式^1[34578]\d{9}$
        if (number.matches("^1[34578]\\d{9}$")) {
            // substring : 包含头不包含尾
            Cursor cursor = database.rawQuery(
                    "select location from data2 where id=(select outkey from data1 where id=?)",
                    new String[] { number.substring(0, 7) });
            //3.解析cursor获取数据
            if (cursor.moveToNext()) {
                location = cursor.getString(0);
            }
            //4.关闭数据库
            cursor.close();
        }else{
            //判断号码不是11位的情况
            switch (number.length()) {
                case 3://110  120 114  911
                    location="报警电话";
                    break;
                case 4://20年前
                    location="穿越电话";
                    break;
                case 5://10086 10010  100000
                    location = "客服电话";
                    break;
                case 6://虚拟电话
                    location="虚拟电话";
                    break;
                case 7://座机电话
                case 8:
                    location="座机电话";
                    break;

                default:// 010 1234567  10位    010 12345678  11位     0372 12345678 12位
                    //长途电话
                    //startsWith : 以什么样的字符开头
                    if (number.length() >= 10 && number.length() <= 12 && number.startsWith("0")) {
                        //不同的区号也是有不同的归属地的，所以也要根据区号查询归属
                        //查询3位区号的归属地  010 -->  10
                        String area = number.substring(1, 3);
                        Cursor cursor = database.rawQuery("select location from data2 where area=?", new String[]{area});
                        if (cursor.moveToNext()) {
                            location = cursor.getString(0);
                            //北京电信 -> 北京  河南安阳电信 -> 河南安阳
                            location = location.substring(0, location.length()-2);
                        }else{
                            //如果3位没有查询出来，直接查询4位,0372  372
                            String area4 = number.substring(1, 4);
                            Cursor cursor2 = database.rawQuery("select location from data2 where area=?", new String[]{area4});
                            if (cursor2.moveToNext()) {
                                location = cursor2.getString(0);
                                location = location.substring(0, location.length()-2);
                            }
                        }

                    }
                    break;
            }
        }

        database.close();
        return location;
    }
}
