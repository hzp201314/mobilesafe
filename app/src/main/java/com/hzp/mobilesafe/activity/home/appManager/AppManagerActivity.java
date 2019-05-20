package com.hzp.mobilesafe.activity.home.appManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.graphics.Color;
import android.text.format.Formatter;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.hzp.mobilesafe.R;
import com.hzp.mobilesafe.bean.AppInfo;
import com.hzp.mobilesafe.engine.AppEngine;
import com.hzp.mobilesafe.view.CustomProgressBar;

import java.util.List;

public class AppManagerActivity extends Activity {
    private CustomProgressBar mMemory;
    private CustomProgressBar mSD;
    private ListView mListView;

    private List<AppInfo> allAppInfos;

    /**用户程序的集合**/
    private List<AppInfo> userAppInfos;
    /**系统程序的集合**/
    private List<AppInfo> systemAppInfos;
    private TextView mCount;
    /**被点击条目应用程序的信息**/
    private AppInfo appInfo;

    private PopupWindow popupWindow;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_manager);
        initView();
        //设置显示存储空间信息
        setMemoryMsg();
        initData();
    }
    /**
     * 初始化控件
     *
     * 2016-10-18 下午2:42:44
     */
    private void initView() {
        mMemory = (CustomProgressBar) findViewById(R.id.app_cpb_memory);
        mSD = (CustomProgressBar) findViewById(R.id.app_cpb_sd);
        mListView = (ListView) findViewById(R.id.app_lv_apps);
        mCount = (TextView) findViewById(R.id.app_tv_count);

        //设置listview的滚动监听事件
        setListViewOnScrollListener();
        //设置listview的条目点击事件，显示气泡
        setListViewOnItemListener();
    }
    /**
     * 设置listview的滚动监听事件
     *
     * 2016-10-18 下午4:52:21
     */
    private void setListViewOnScrollListener() {
        mListView.setOnScrollListener(new OnScrollListener() {
            //滚动状态改变调用的方法
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // TODO Auto-generated method stub

            }
            //滚动的时候调用的方法
            //firstVisibleItem : 界面显示的第一个条目
            //visibleItemCount : 可见条目的总数
            //totalItemCount : listview的总条目数据
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {

                //listview滚动隐藏气泡
                hidepopupwindow();

                if (userAppInfos != null && systemAppInfos != null) {
                    //判断当前界面显示的第一个条目是否大于或者等于系统程序个数的条目的索引
                    if (firstVisibleItem >= userAppInfos.size()+1) {
                        mCount.setText("系统程序("+systemAppInfos.size()+")");
                    }else{
                        mCount.setText("用户程序("+userAppInfos.size()+")");
                    }
                }
            }
        });
    }
    /**
     * 设置listview的条目点击事件，显示气泡
     *
     * 2016-10-18 下午5:03:54
     */
    private void setListViewOnItemListener() {
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //显示气泡
                //1.屏蔽textview条目样式显示气泡
                if (position == 0 || position == userAppInfos.size()+1) {
                    return;
                }
                //2.获取被点击条目应用程序的信息，方便后期进行卸载，打开，分享，信息操作
                if (position <= userAppInfos.size()) {
                    //用户集合中获取数据
                    appInfo = userAppInfos.get(position-1);
                }else{
                    //系统集合中获取数据
                    appInfo = systemAppInfos.get(position - userAppInfos.size() - 2);
                }
                //3.显示气泡
                //判断气泡是否显示，如果显示，取消气泡显示，重新再新的条目显示气泡
                hidepopupwindow();
                View contentView = View.inflate(getApplicationContext(), R.layout.popupwindow_item, null);
                //contentView : 气泡的样式
                //width,height : 气泡的宽高
                popupWindow = new PopupWindow(contentView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                //设置popupwindow样式动画
                popupWindow.setAnimationStyle(R.style.AddressStyleAnimation);

                //显示气泡
                //popupWindow.showAsDropDown(view);//将气泡显示在哪个控件的下方
                //xoff,yoff:气泡距离view对象左边框和上边框的距离
                //getHeight() : 获取view对象的高度
                popupWindow.showAsDropDown(view, 60, -view.getHeight());
            }

        });
    }

    /**
     * 隐藏popupwindow
     *
     * 2016-10-18 下午5:26:48
     */
    private void hidepopupwindow() {
        if (popupWindow != null) {
            popupWindow.dismiss();
            popupWindow = null;
        }
    }

    /**
     * 显示存储空间信息操作
     *
     * 2016-10-18 下午2:44:11
     */
    private void setMemoryMsg() {
        //1.内存
        File dataDirectory = Environment.getDataDirectory();//内存存储空间文件
        //单位   b
        long totalSpace = dataDirectory.getTotalSpace();//获取总的空间大小
        long freeSpace = dataDirectory.getFreeSpace();//获取可用的空间大小
        //已用空间  = 总空间 - 可用空间
        long useSpace = totalSpace - freeSpace;
        //展示空间信息
        mMemory.setText("内存：");
        //需要将空间大小进行换算
        String userSize = Formatter.formatFileSize(this, useSpace);//将b空间大小进行相应的换算，参数：b/kb空间大小,得到的是带有单位的空间大小的字符串的表现形式
        mMemory.setLeft(userSize+"已用");
        String freeSize = Formatter.formatFileSize(this, freeSpace);
        mMemory.setRight(freeSize+"可用");
        //设置进度，已用的空间占用总空间的比例
        //50% = 50 / 100 * 100%
        //数学  3.1 -> 3    3.6 -> 4   程序  3.1 -> 3    3.6 -> 3   3.1+0.5f = 3.6 -> 3    3.6+0.5=4.1 -> 4
        int progress = (int) (useSpace * 100f / totalSpace + 0.5f);
        mMemory.setProgress(progress);


        //2.SD卡
        File sdDirectory = Environment.getExternalStorageDirectory();//获取SD卡存储空间的文件
        long sdtotalSpace = sdDirectory.getTotalSpace();
        long sdfreeSpace = sdDirectory.getFreeSpace();
        //已用空间  = 总空间 - 可用空间
        long sduseSpace = sdtotalSpace - sdfreeSpace;
        mSD.setText("SD：   ");
        String sdUseSize = Formatter.formatFileSize(this, sduseSpace);
        mSD.setLeft(sdUseSize+"已用");
        String sdFreeSize = Formatter.formatFileSize(this, sdfreeSpace);
        mSD.setRight(sdFreeSize+"可用");
        int sdprogress = (int) (sduseSpace * 100f / sdtotalSpace + 0.5f);
        mSD.setProgress(sdprogress);
    }

    /**
     * 获取数据展示数据
     *
     * 2016-10-18 下午3:48:59
     */
    private void initData() {
        new Thread(){
            public void run() {
                allAppInfos = AppEngine.getAllAppInfos(getApplicationContext());

                userAppInfos = new ArrayList<AppInfo>();
                systemAppInfos = new ArrayList<AppInfo>();
                //1.遍历集合中，将用户程序放到用户集合中，系统程序放到系统集合中
                for (AppInfo info : allAppInfos) {
                    if (info.isSystem) {
                        //系统程序
                        systemAppInfos.add(info);
                    }else{
                        //用户程序
                        userAppInfos.add(info);
                    }
                }

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        //设置textview显示用户程序的个数
                        mCount.setText("用户程序("+userAppInfos.size()+")");
                        mListView.setAdapter(new Myadapter());
                    }
                });
            };
        }.start();
    }

    private class Myadapter extends BaseAdapter{

        @Override
        public int getCount() {
            //return allAppInfos.size();
            //2.因为listview显示的数据要从两个集合中获取，所有listview的条目数应该是两个集合中的长度的总和
            //因为多了两个textview条目，所以总条目数要+2
            return userAppInfos.size()+systemAppInfos.size()+2;
        }
        //3.设置显示两种样式，并设置条目应该显示的样式
        //3.1.提供给系统调用的，设置listview有几种条目样式，返回几就表示有几种样式
        @Override
        public int getViewTypeCount() {
            return 2;
        }
        //不知道什么条目显示什么样式
        //3.2.提供给getview调用的，根据条目的索引设置条目应该显示什么样式
        //position : 条目的索引
        @Override
        public int getItemViewType(int position) {
            //需要自己根据自己的实际情况进行实现，返回的int类型值是样式的标示
            if (position == 0 || position == userAppInfos.size()+1) {
                //使用程序个数的样式
                return 0;
            }else{
                //使用条目的样式
                return 1;
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = null;
            //4.设置条目显示的样式
            //获取条目显示样式的标示
            int itemViewType = getItemViewType(position);
            if (itemViewType == 0) {
                //使用程序个数的样式
                if (position == 0) {
                    //用户程序个数
                    TextView textView = new TextView(getApplicationContext());
                    textView.setText("用户程序("+userAppInfos.size()+")");
                    textView.setTextColor(Color.BLACK);
                    textView.setBackgroundColor(Color.GRAY);
                    textView.setTextSize(18);
                    textView.setPadding(8, 8, 8, 8);
                    return textView;
                }else{
                    //系统程序个数
                    TextView textView = new TextView(getApplicationContext());
                    textView.setText("系统程序("+systemAppInfos.size()+")");
                    textView.setTextColor(Color.BLACK);
                    textView.setBackgroundColor(Color.GRAY);
                    textView.setTextSize(18);
                    textView.setPadding(8, 8, 8, 8);
                    return textView;
                }
            }else if(itemViewType == 1){
                //使用条目的样式
                ViewHolder viewHolder;
                if (convertView == null) {
                    view = View.inflate(getApplicationContext(), R.layout.app_listview_item, null);
                    viewHolder = new ViewHolder();
                    viewHolder.mIcon = (ImageView) view.findViewById(R.id.item_iv_icon);
                    viewHolder.mName = (TextView) view.findViewById(R.id.item_tv_name);
                    viewHolder.misSD = (TextView) view.findViewById(R.id.item_tv_issd);
                    viewHolder.mSize = (TextView) view.findViewById(R.id.item_tv_size);
                    view.setTag(viewHolder);
                }else{
                    view = convertView;
                    viewHolder = (ViewHolder) view.getTag();
                }
                //获取数据展示数据
                AppInfo appInfo;
                //AppInfo appInfo = allAppInfos.get(position);
                //要从两个集合中获取数据
                if (position <= userAppInfos.size()) {
                    //用户集合中获取数据
                    appInfo = userAppInfos.get(position-1);
                }else{
                    //系统集合中获取数据
                    appInfo = systemAppInfos.get(position - userAppInfos.size() - 2);
                }
                viewHolder.mIcon.setImageDrawable(appInfo.icon);
                viewHolder.mName.setText(appInfo.name);
                //设置空间大小
                String size = Formatter.formatFileSize(getApplicationContext(), appInfo.size);
                viewHolder.mSize.setText(size);
                //根据bean类中保存的标示，判断是否安装在手机内存还是SD卡中
                if (appInfo.isSD) {
                    viewHolder.misSD.setText("SD卡");
                }else{
                    viewHolder.misSD.setText("手机内存");
                }
            }
            return view;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

    }

    static class ViewHolder{
        TextView mName,misSD,mSize;
        ImageView mIcon;
    }


    @Override
    protected void onDestroy() {
        hidepopupwindow();
        super.onDestroy();
    }

}
