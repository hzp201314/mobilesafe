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
     * 2016-10-24 下午3:22:51
     */
    private void initView() {
        mListView = (ListView) findViewById(R.id.traffic_lv_listview);

        initData();
    }
    /**
     * 获取数据展示数据
     *
     * 2016-10-24 下午3:39:24
     */
    private void initData() {
        //系统中所有应用程序的信息
        new MyAsyncTaks().execute();
    }

    private class MyAsyncTaks extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            list = AppEngine.getAllAppInfos(getApplicationContext());
            return null;
        }

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
            View view = View.inflate(getApplicationContext(), R.layout.traffic_listview_item, null);

            ImageView mIcon = (ImageView) view.findViewById(R.id.item_iv_icon);
            TextView mName = (TextView) view.findViewById(R.id.item_tv_name);
            TextView mTx = (TextView) view.findViewById(R.id.item_tv_tx);
            TextView mRx = (TextView) view.findViewById(R.id.item_tv_rx);

            AppInfo appInfo = list.get(position);
            mIcon.setImageDrawable(appInfo.icon);
            mName.setText(appInfo.name);

            //proc目录的资源，重启手机自动清零
            long uidTxBytes = TrafficStats.getUidTxBytes(appInfo.uid);//根据应用的uid获取应用的上传的流量
            mTx.setText("上传:"+ Formatter.formatFileSize(getApplicationContext(), uidTxBytes));

            long uidRxBytes = TrafficStats.getUidRxBytes(appInfo.uid);//根据应用的uid获取应用的下载的流量
            mRx.setText("下载:"+Formatter.formatFileSize(getApplicationContext(), uidRxBytes));

            return view;
        }

    }


}
