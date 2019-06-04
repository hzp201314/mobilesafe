package com.hzp.mobilesafe.engine;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;

import com.google.gson.Gson;
import com.hzp.mobilesafe.bean.SMSInfo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * created by hzp on 2019/5/25 09:34
 * 作者：codehan
 * 描述：读取短信，备份短信的操作
 */
public class SmsEngine {
    //崔小可
    public interface ReadSmsListener{
        //QQ  微信   电话
        /**设置最大进度方法**/
        public void setMax(int max);

        /*设置当前进度*/
        public void setProgress(int progress);
    }


    //坐班监督作用
    /**
     * 读取短信的操作
     *
     */
    public static void readSms(Context context, ReadSmsListener readSmsListener){
        List<SMSInfo> list = new ArrayList<SMSInfo>();
        //获取内容解析者
        ContentResolver contentResolver = context.getContentResolver();
        //设置地址
        Uri uri = Uri.parse("content://sms/");
        Cursor cursor = contentResolver.query(uri, new String[]{"address","date","type","body"}, null, null, null);

        //设置总进度
        //getCount();获取cursor保存数据的个数
        readSmsListener.setMax(cursor.getCount());
        //设置当前进度，每循环一次，加一个进度
        int progress = 0;//初始化当前的进度
        while(cursor.moveToNext()){
            SystemClock.sleep(100);
            String address = cursor.getString(0);
            String date = cursor.getString(1);
            String type = cursor.getString(2);
            String body = cursor.getString(3);

            Log.d( "SmsEngine", "readSms: "+"address:"+address+" date:"+date+" type:"+type+" body:"+body );

            SMSInfo smsInfo = new SMSInfo(address, date, type, body);
            list.add(smsInfo);

            //每循环一次，进度+1
            progress++;
            //将进度设置给dialog显示
            readSmsListener.setProgress(progress);
        }
        //备份短信
        Gson gson = new Gson();
        String json = gson.toJson(list);
        try {
            //将json串写到文件中
            FileWriter fileWriter = new FileWriter(new File("mnt/sdcard/sms.txt"));
            fileWriter.write(json);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
