package com.hzp.mobilesafe.activity.home.mobileSecurity;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hzp.mobilesafe.R;
import com.hzp.mobilesafe.bean.ContactInfo;
import com.hzp.mobilesafe.engine.ContactsEngine;

import java.util.List;

public class ContactActivity extends Activity {
    private static final String TAG=ContactActivity.class.getSimpleName();
    private ProgressBar mLoading;
    private ListView mContacts;
    private List<ContactInfo> contactsInfos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_contact );
        iniView();
    }

    private void iniView() {
        mContacts = (ListView) findViewById(R.id.contact_lv_contacts);
        mLoading = (ProgressBar) findViewById(R.id.contact_pb_loading);

        intData();

        mContacts.setOnItemClickListener( new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //将选中的条目对应的号码传递给setUp3界面显示，同时还要移除界面
                Intent data = new Intent();
                data.putExtra("number", contactsInfos.get(position).number);
                //设置结果的方法，将结果返回给调用当前activity的activity
                //参数1：结果码
                //参数2：传递数据的意图
                setResult(Activity.RESULT_OK, data);
                //移除界面
                finish();
            }
        });
    }

    /**
     * 获取数据展示数据
     */
    private void intData() {
        //查询数据库是耗时操作，所以要放到子线程操作
        //在加载数据之前，显示进度条
        mLoading.setVisibility( View.VISIBLE );
        new Thread(  ){
            public void run(){
                contactsInfos= ContactsEngine.getContactsInfo(ContactActivity.this);

                runOnUiThread( new Runnable() {
                    @Override
                    public void run() {
                        mContacts.setAdapter( new MyAdapter() );
                        mLoading.setVisibility( View.GONE );
                    }
                } );

            }
        }.start();
    }

    private class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return contactsInfos.size();
        }

        @Override
        public Object getItem(int position) {
            return contactsInfos.get( position );
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            ViewHolder viewHolder;
            if (convertView==null){
                Log.d( TAG, "getView: 创建view对象，执行的findviewbyid操作" );
                view=View.inflate( getApplicationContext(),R.layout.contact_listview_item,null );
                //1.创建盒子
                viewHolder=new ViewHolder();
                //2.将findviewbyid放到盒子中
                viewHolder.mIcon = (ImageView) view.findViewById(R.id.item_iv_icon);
                viewHolder.mName = (TextView) view.findViewById(R.id.item_tv_name);
                viewHolder.mNumber = (TextView) view.findViewById(R.id.item_tv_number);
                //3.将盒子和view对象进行绑定，一起复用
                view.setTag( viewHolder );//将viewHolder和view对象绑定

            }else {
                Log.d( TAG, "getView: 复用view对象，使用创建好的findviewbyid操作" );
                view=convertView;
                //4.复用缓存，拿到缓存对象之后，就可以将和复用view对象绑定的veiwHolder获取出来
                viewHolder=(ViewHolder)view.getTag();//获取和view对象绑定的数据

            }
            //获取数据展示数据
            ContactInfo contactInfo = (ContactInfo) getItem(position);
            //5.使用盒子中保存的findviewbyid好的控件操作
            viewHolder.mName.setText(contactInfo.name);
            viewHolder.mNumber.setText(contactInfo.number);

            //显示头像，获取头像
            Bitmap bitmap = ContactsEngine.getContactPhoto(getApplicationContext(), contactInfo.id);
            if (bitmap != null) {
                viewHolder.mIcon.setImageBitmap(bitmap);
            }else{
                viewHolder.mIcon.setImageResource(R.drawable.ic_contact);
            }

            return view;
        }


    }
    //1.创建盒子
    static class ViewHolder {
        ImageView mIcon;
        TextView mName;
        TextView mNumber;
    }
}
