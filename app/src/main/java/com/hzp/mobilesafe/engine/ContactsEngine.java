package com.hzp.mobilesafe.engine;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.ContactsContract;

import com.hzp.mobilesafe.bean.ContactInfo;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * created by hzp on 2019/5/20 11:56
 * 作者：codehan
 * 描述：获取系统联系人和联系人头像
 */
public class ContactsEngine {
    //1.获取联系人的名称和号码
    /**
     * 获取联系人的名称和号码
     * 还需要查询出联系人的唯一标示，方便使用唯一标示获取联系人的头像
     *
     * 2016-10-11 上午9:39:10
     */
    public static List<ContactInfo> getContactsInfo(Context context){

//        SystemClock.sleep(3000);


        List<ContactInfo> list = new ArrayList<ContactInfo>();

        //1.获取内容解析者
        ContentResolver contentResolver = context.getContentResolver();

        //2.查询操作
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID
        };
        //uri : 查询地址
        //projection : 查询的数据的字段名称
        //selection : 查询的条件      where id=....
        //selectionArgs : 查询条件的参数
        //sortOrder : 排序
        Cursor cursor = contentResolver.query(uri, projection, null, null, null);
        while(cursor.moveToNext()){
            String name = cursor.getString(0);
            String number = cursor.getString(1);
            int id = cursor.getInt(2);

            //为了方便之后使用，将获取的信息保存到bean类中
            ContactInfo contactInfo = new ContactInfo(name, number, id);
            //为了方便listview使用，将bean类保存到list集合中
            list.add(contactInfo);
        }
        return list;
    }

    //2.根据联系人的唯一标示，获取联系人对应的头像
    /**
     * 根据联系人的唯一标示，获取联系人对应的头像
     *@return
     * 2016-10-11 上午9:52:09
     */
    public static Bitmap getContactPhoto(Context context, int id){
        //获取内容解析者
        ContentResolver contentResolver = context.getContentResolver();
        //Uri uri = ContactsContract.Contacts.CONTENT_URI;
        //拼接路径
        //http://www.baidu.com/jdk
        //参数1：表的路径
        //参数2：联系人的具体的路径
        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, id+"");
        //获取联系人的头像，以流的形式返回
        InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(contentResolver, uri);
        //将返回的图片的流的信息转化成bitmap
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        //关流操作
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return bitmap;
    }
}
