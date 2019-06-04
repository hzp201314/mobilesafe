package com.hzp.mobilesafe.activity.home.traffic;

import android.app.Activity;
import android.net.TrafficStats;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hzp.mobilesafe.R;
import com.hzp.mobilesafe.bean.AppInfo;
import com.hzp.mobilesafe.engine.AppEngine;

import java.util.List;

/**
 * 流量统计
 */
public class TrafficActivity extends Activity {

    private ListView mListView;
    private List<AppInfo> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_traffic );
        initView();
    }
    /**
     * 初始化控件
     *
     */
    private void initView() {
        mListView = (ListView) findViewById(R.id.traffic_lv_listview);

        initData();
    }
    /**
     * 获取数据展示数据
     *
     */
    private void initData() {
        //系统中所有应用程序的信息
        new MyAsyncTaks().execute();
    }

    private class MyAsyncTaks extends AsyncTask<Void, Void, Void> {

        /*子线程中执行*/
        @Override
        protected Void doInBackground(Void... params) {
            list = AppEngine.getAllAppInfos(getApplicationContext());
            return null;
        }

        /*子线程执行之后*/
        @Override
        protected void onPostExecute(Void result) {
            mListView.setAdapter(new Myadapter());
            super.onPostExecute(result);
        }
    }

    private class Myadapter extends BaseAdapter {



        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View view;
            ViewHolder viewHolder;

            //TODO 复用缓存
            if(convertView==null) {
                view = View.inflate( getApplicationContext(), R.layout.traffic_listview_item, null );
                viewHolder=new ViewHolder();
                viewHolder.mIcon = (ImageView) view.findViewById( R.id.item_iv_icon );
                viewHolder.mName = (TextView) view.findViewById( R.id.item_tv_name );
                viewHolder.mTx = (TextView) view.findViewById( R.id.item_tv_tx );
                viewHolder.mRx = (TextView) view.findViewById( R.id.item_tv_rx );
                view.setTag( viewHolder );
            }else {
                view=convertView;
                viewHolder= (ViewHolder) view.getTag();
            }
            final AppInfo appInfo = list.get(position);
            viewHolder.mIcon.setImageDrawable(appInfo.icon);
            viewHolder.mName.setText(appInfo.name);

            //proc目录的资源，重启手机自动清零 市面上的应用是跟运营商合作暴露接口查询的流量信息（交钱）
            long uidTxBytes = TrafficStats.getUidTxBytes(appInfo.uid);//根据应用的uid获取应用的上传的流量
            viewHolder.mTx.setText("上传:"+ Formatter.formatFileSize(getApplicationContext(), uidTxBytes));

            long uidRxBytes = TrafficStats.getUidRxBytes(appInfo.uid);//根据应用的uid获取应用的下载的流量
            viewHolder.mRx.setText("下载:"+Formatter.formatFileSize(getApplicationContext(), uidRxBytes));

            return view;
        }

    }
    static class ViewHolder{
        ImageView mIcon ;
        TextView mName ;
        TextView mTx ;
        TextView mRx ;
    }


}
