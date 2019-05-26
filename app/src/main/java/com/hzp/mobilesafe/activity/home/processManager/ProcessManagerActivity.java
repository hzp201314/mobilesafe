package com.hzp.mobilesafe.activity.home.processManager;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SlidingDrawer;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
import android.widget.TextView;
import android.widget.Toast;

import com.hzp.mobilesafe.R;
import com.hzp.mobilesafe.bean.ProcessInfo;
import com.hzp.mobilesafe.engine.ProcessEngine;
import com.hzp.mobilesafe.service.ScreenOffService;
import com.hzp.mobilesafe.utils.Constants;
import com.hzp.mobilesafe.utils.ServiceUtil;
import com.hzp.mobilesafe.utils.SharedPreferencesUtil;
import com.hzp.mobilesafe.view.CustomProgressBar;
import com.hzp.mobilesafe.view.SettingView;

import java.util.ArrayList;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class ProcessManagerActivity extends Activity {

    private CustomProgressBar mMemory;
    private CustomProgressBar mCount;

    private List<ProcessInfo> runningProcessInfos;

    /** 用户进程的集合 **/
    private List<ProcessInfo> userProcessInfos;
    /** 系统进程的集合 **/
    private List<ProcessInfo> systemProcessInfos;
    private StickyListHeadersListView mListView;

    private Myadapter myadapter;
    private int runningProcessCount;
    private int allProcessCount;
    private ImageView mArrow1;
    private ImageView mArrow2;
    private SlidingDrawer mDrawer;
    private SettingView mIsShowSystem;
    private SettingView mScreenOff;
    private boolean isShowSystem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_process_manager );
        initView();
    }

    /**
     * 初始化控件
     *
     * 2016-10-20 上午11:53:48
     */
    private void initView() {
        mCount = (CustomProgressBar) findViewById(R.id.process_cpb_count);
        mMemory = (CustomProgressBar) findViewById(R.id.process_cpb_memory);
        mListView = (StickyListHeadersListView) findViewById(R.id.porcess_lv_listview);
        mArrow1 = (ImageView) findViewById(R.id.process_iv_arrow1);
        mArrow2 = (ImageView) findViewById(R.id.process_iv_arrow2);
        mDrawer = (SlidingDrawer) findViewById(R.id.process_sd_slidingdrawer);
        mIsShowSystem = (SettingView) findViewById(R.id.porcess_sv_isshowsystem);
        mScreenOff = (SettingView) findViewById(R.id.process_sv_screenoff);

        // 实现箭头的渐变动画
        setAnimation();

        // 设置展示进程和内存信息
        setMsg();

        // 加载显示数据
        initData();

        // 设置listview的条目点击事件
        onListViewItemClickListener();

        // 设置抽屉的打开和关闭的监听操作
        setOnSlidingDrawerListener();

        // 设置显示系统进程的条目的点击事件
        setOnIsShowSystemListener();

        // 设置锁屏清理进程的点击事件
        setOnScreenOffListener();

    }

    /**
     * 设置锁屏清理进程的点击事件
     *
     * 2016-10-21 上午10:20:31
     */
    private void setOnScreenOffListener() {
        mScreenOff.setOnClickListener( new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProcessManagerActivity.this,
                        ScreenOffService.class);
                if (ServiceUtil.isServiceRunning(ProcessManagerActivity.this,
                        "com.hzp.mobilesafe.service.ScreenOffService")) {
                    // 开启 -> 点击关闭服务
                    stopService(intent);
                } else {
                    // 关闭 -> 点击开启服务
                    startService(intent);
                }
                mScreenOff.toggle();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //回显锁屏清理进程的服务
        boolean b = ServiceUtil.isServiceRunning(this,
                "com.hzp.mobilesafe.service.ScreenOffService");
        mScreenOff.setToggleOn(b);
    }

    /**
     * 显示系统进程的条目的点击事件
     *
     * 2016-10-21 上午9:44:54
     */
    private void setOnIsShowSystemListener() {

        // 再次进入界面的时候，回显操作
        boolean b = SharedPreferencesUtil.getBoolean(getApplicationContext(),
                Constants.PROCESSISSHOWSYSTEM, true);
        // 需要将按钮及listview是否显示系统进程都进行回显示
        mIsShowSystem.setToggleOn(b);
        isShowSystem = b;

        mIsShowSystem.setOnClickListener( new OnClickListener() {

            @Override
            public void onClick(View v) {
                mIsShowSystem.toggle();
                // 当开启/关闭条目的时候，需要隐藏/显示系统进程，所以需要将开关的状态设置给隐藏显示系统进程的标示
                boolean istoggle = mIsShowSystem.istoggle();
                isShowSystem = istoggle;
                // 更新界面
                myadapter.notifyDataSetChanged();
                // 保存条目的开关状态，方便回显
                SharedPreferencesUtil.saveBoolean(getApplicationContext(),
                        Constants.PROCESSISSHOWSYSTEM, mIsShowSystem.istoggle());
            }
        });
    }

    /**
     * 抽屉的打开和关闭的监听操作
     *
     * 2016-10-21 上午9:34:14
     */
    private void setOnSlidingDrawerListener() {
        // 打开抽屉的监听
        mDrawer.setOnDrawerOpenListener( new OnDrawerOpenListener() {
            // 当抽屉打开的时候调用的方法
            @Override
            public void onDrawerOpened() {
                // 消除动画
                closeAnimation();
            }
        });
        // 关闭抽屉的监听
        mDrawer.setOnDrawerCloseListener( new OnDrawerCloseListener() {
            // 当抽屉关闭的时候调用的方法
            @Override
            public void onDrawerClosed() {
                // 重新开始执行动画
                setAnimation();
            }
        });
    }

    /**
     * 消除动画
     *
     * 2016-10-21 上午9:35:35
     */
    protected void closeAnimation() {
        mArrow1.clearAnimation();// 清除动画
        mArrow2.clearAnimation();

        // 更改显示的箭头的方向
        mArrow1.setImageResource(R.drawable.drawer_arrow_down);
        mArrow2.setImageResource(R.drawable.drawer_arrow_down);
    }

    /**
     * 实现箭头动画的
     *
     * 2016-10-21 上午9:27:33
     */
    private void setAnimation() {

        // 设置箭头的向上的图片
        mArrow1.setImageResource(R.drawable.drawer_arrow_up);
        mArrow2.setImageResource(R.drawable.drawer_arrow_up);

        // 半透明 -> 不透明
        AlphaAnimation animation1 = new AlphaAnimation(0.2f, 1.0f);
        animation1.setDuration(500);// 持续时间
        animation1.setRepeatCount( Animation.INFINITE);// 执行次数，一直执行
        animation1.setRepeatMode(Animation.REVERSE);// 执行的类型
        mArrow1.startAnimation(animation1);// 执行动画
        // 不透明 -> 半透明
        AlphaAnimation animation2 = new AlphaAnimation(1.0f, 0.2f);
        animation2.setDuration(500);// 持续时间
        animation2.setRepeatCount(Animation.INFINITE);// 执行次数，一直执行
        animation2.setRepeatMode(Animation.REVERSE);// 执行的类型
        mArrow2.startAnimation(animation2);// 执行动画
    }

    /**
     * listview的条目点击事件
     *
     * 2016-10-20 下午4:12:20
     */
    private void onListViewItemClickListener() {
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // 选中/取消选中的checkbox
                // 获取被点击条目对应的bean类
                // 获取数据展示数据
                ProcessInfo info;
                // 要从两个集合中获取数据,当listview的角标小于用户集合的长度的从用户集合中获取数据
                if (position < userProcessInfos.size()) {
                    // 用户集合中获取数据
                    info = userProcessInfos.get(position);
                } else {
                    // 系统集合中获取数据
                    info = systemProcessInfos.get(position
                            - userProcessInfos.size());
                }
                // 设置选中/取消选中checkbox
                if (info.ischecked) {
                    info.ischecked = false;
                } else {
                    // 判断如果是当前应用程序，不能被选中
                    if (!info.packageName.equals(getPackageName())) {
                        info.ischecked = true;
                    }

                }
                myadapter.notifyDataSetChanged();// 会重新调用adapter的getcount和getview方法
            }
        });
    }

    /**
     * 展示进程和内存信息
     *
     * 2016-10-20 上午11:54:51
     */
    private void setMsg() {
        // 展示进程信息
        setProcessCount();
        // 展示内存
        setMemory();
    }

    /**
     * 展示进程信息
     *
     * 2016-10-20 上午11:55:21
     */
    private void setProcessCount() {
        // 获取进程信息
        runningProcessCount = ProcessEngine.getRunningProcessCount(this);
        allProcessCount = ProcessEngine.getAllProcessCount(this);
        mCount.setText("进程数:");
        mCount.setLeft("正在运行" + runningProcessCount + "个");
        mCount.setRight("可有进程" + allProcessCount + "个");
        int progress = (int) (runningProcessCount * 100f / allProcessCount + 0.5f);
        mCount.setProgress(progress);
    }

    /**
     * 展示内存信息
     *
     * 2016-10-20 上午11:58:11
     */
    private void setMemory() {
        // 获取内存信息
        long freeMemory = ProcessEngine.getFreeMemory(this);
        long allMemory = ProcessEngine.getALLMemory(this);
        // 已用内存
        long useMemory = allMemory - freeMemory;
        mMemory.setText("内存： ");
        String userSize = Formatter.formatFileSize(this, useMemory);
        mMemory.setLeft("占用内存" + userSize);
        String freeSize = Formatter.formatFileSize(this, freeMemory);
        mMemory.setRight("可用内存" + freeSize);
        // 设置进度条
        int progress = (int) (useMemory * 100f / allMemory + 0.5f);
        mMemory.setProgress(progress);
    }

    /**
     * 获取数据展示数据
     *
     * 2016-10-18 下午3:48:59
     */
    private void initData() {
        new Thread() {

            public void run() {
                runningProcessInfos = ProcessEngine
                        .getRunningProcessInfo(ProcessManagerActivity.this);

                userProcessInfos = new ArrayList<ProcessInfo>();
                systemProcessInfos = new ArrayList<ProcessInfo>();
                // 1.遍历集合中，将用户程序放到用户集合中，系统程序放到系统集合中
                for (ProcessInfo info : runningProcessInfos) {
                    if (info.isSystem) {
                        // 系统进程
                        systemProcessInfos.add(info);
                    } else {
                        // 用户进程
                        userProcessInfos.add(info);
                    }
                }

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        myadapter = new Myadapter();
                        mListView.setAdapter(myadapter);
                    }
                });
            };
        }.start();
    }

    /** listview的adapter **/
    private class Myadapter extends BaseAdapter implements
            StickyListHeadersAdapter {

        @Override
        public int getCount() {
            return isShowSystem ? userProcessInfos.size()
                    + systemProcessInfos.size() : userProcessInfos.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            // 使用条目的样式
            ViewHolder viewHolder;
            if (convertView == null) {
                view = View.inflate(getApplicationContext(),
                        R.layout.process_listview_item, null);
                viewHolder = new ViewHolder();
                viewHolder.mIcon = (ImageView) view
                        .findViewById(R.id.item_iv_icon);
                viewHolder.mName = (TextView) view
                        .findViewById(R.id.item_tv_name);
                viewHolder.mSize = (TextView) view
                        .findViewById(R.id.item_tv_size);
                viewHolder.mIsChecked = (CheckBox) view
                        .findViewById(R.id.item_cb_ischecked);
                view.setTag(viewHolder);
            } else {
                view = convertView;
                viewHolder = (ViewHolder) view.getTag();
            }
            // 获取数据展示数据
            ProcessInfo info;
            // 要从两个集合中获取数据,当listview的角标小于用户集合的长度的从用户集合中获取数据
            if (position < userProcessInfos.size()) {
                // 用户集合中获取数据
                info = userProcessInfos.get(position);
            } else {
                // 系统集合中获取数据
                info = systemProcessInfos.get(position
                        - userProcessInfos.size());
            }
            viewHolder.mIcon.setImageDrawable(info.icon);
            viewHolder.mName.setText(info.name);
            // 设置空间大小
            String size = Formatter.formatFileSize(getApplicationContext(),
                    info.size);
            viewHolder.mSize.setText(size);

            // 根据保存在bean类中的checkbox的选中操作，动态的设置checkbox是选中还是不选中
            viewHolder.mIsChecked.setChecked(info.ischecked);

            // 判断如果是当前应用程序，checkbox隐藏
            if (info.packageName.equals(getPackageName())) {
                // 隐藏
                viewHolder.mIsChecked.setVisibility(View.GONE);
            } else {
                // 显示
                viewHolder.mIsChecked.setVisibility(View.VISIBLE);
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

        // 设置头的样式
        // 有一个条目运行一次
        @Override
        public View getHeaderView(int position, View convertView,
                                  ViewGroup parent) {
            TextView textView = new TextView(getApplicationContext());
            textView.setTextColor(Color.BLACK);
            textView.setBackgroundColor(Color.GRAY);
            textView.setTextSize(20);
            textView.setPadding(8, 8, 8, 8);
            // 获取数据的，根据数据的标示设置显示的文本
            // 获取数据展示数据
            ProcessInfo info;
            // 要从两个集合中获取数据,当listview的角标小于用户集合的长度的从用户集合中获取数据
            if (position < userProcessInfos.size()) {
                // 用户集合中获取数据
                info = userProcessInfos.get(position);
            } else {
                // 系统集合中获取数据
                info = systemProcessInfos.get(position
                        - userProcessInfos.size());
            }
            textView.setText(info.isSystem ? "系统进程("
                    + systemProcessInfos.size() + ")" : "用户进程("
                    + userProcessInfos.size() + ")");
            return textView;
        }

        // 设置头的id
        // position : listview的条目的索引
        // 设置几个id，表示有几个头条目要展示出来
        @Override
        public long getHeaderId(int position) {
            ProcessInfo info;
            // 要从两个集合中获取数据,当listview的角标小于用户集合的长度的从用户集合中获取数据
            if (position < userProcessInfos.size()) {
                // 用户集合中获取数据
                info = userProcessInfos.get(position);
            } else {
                // 系统集合中获取数据
                info = systemProcessInfos.get(position
                        - userProcessInfos.size());
            }
            return info.isSystem ? 0 : 1;
        }

    }

    static class ViewHolder {
        TextView mName, mSize;
        ImageView mIcon;
        CheckBox mIsChecked;
    }

    /**
     * 全选
     *
     * @param view
     *            2016-10-20 下午4:25:59
     */
    public void all(View view) {
        // 将所有的标示改为true
        for (ProcessInfo info : userProcessInfos) {
            // 判断如果是当前应用程序，不做全选操作
            if (!info.packageName.equals(getPackageName())) {
                info.ischecked = true;
            }
        }
        // 判断系统进程是否显示，显示，操作系统进程
        if (isShowSystem) {
            for (ProcessInfo info : systemProcessInfos) {
                // 判断如果是当前应用程序，不做全选操作
                info.ischecked = true;
            }
        }

        // 更新界面操作
        myadapter.notifyDataSetChanged();
    }

    /**
     * 反选
     *
     * @param view
     *            2016-10-20 下午4:25:59
     */
    public void unall(View view) {
        // 取反的操作
        for (ProcessInfo info : userProcessInfos) {
            // 判断如果是当前应用程序，不做全选操作
            if (!info.packageName.equals(getPackageName())) {
                info.ischecked = !info.ischecked;
            }
        }
        if (isShowSystem) {
            for (ProcessInfo info : systemProcessInfos) {
                // 判断如果是当前应用程序，不做全选操作
                info.ischecked = !info.ischecked;
            }
        }
        // 更新界面操作
        myadapter.notifyDataSetChanged();
    }

    /**
     * 清理进程
     *
     * @param view
     *            2016-10-20 下午4:45:56
     */
    public void clear(View view) {
        // 清理所有选中的正在运行的进程
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        // am.killBackgroundProcesses(packageName);//根据应用程序的报名，清除应用程序的进程
        /** 保存删除的bean类 **/
        List<ProcessInfo> deleteInfos = new ArrayList<ProcessInfo>();
        // 用户集合
        for (ProcessInfo info : userProcessInfos) {
            if (info.ischecked) {
                am.killBackgroundProcesses(info.packageName);
                // 当系统清理完数据之后，还有更新界面的数据，因为界面的数据使用两个集合中数据的，所以更新集合中的数据
                // userProcessInfos.remove(info);
                // 集合还在运行使用中，不能修改集合中的数据
                deleteInfos.add(info);
            }
        }

        // 系统集合
        if (isShowSystem) {
            for (ProcessInfo info : systemProcessInfos) {
                if (info.ischecked) {
                    am.killBackgroundProcesses(info.packageName);
                    deleteInfos.add(info);
                }
            }
        }

        // 遍历删除集合的数据，根据数据的是否是系统进程标示，分别从用户集合和系统集合中删除数据
        long deletememory = 0;
        for (ProcessInfo processInfo : deleteInfos) {
            if (processInfo.isSystem) {
                systemProcessInfos.remove(processInfo);
            } else {
                userProcessInfos.remove(processInfo);
            }
            deletememory += processInfo.size;
        }

        // 显示toast提醒用户，清理多少进程，释放多少内存
        Toast.makeText(
                getApplicationContext(),
                "清理"
                        + deleteInfos.size()
                        + "个进程，释放"
                        + Formatter.formatFileSize(getApplicationContext(),
                        deletememory) + "内存", 0).show();
        // 因为android系统中有些进程是清理不掉，但是在界面中要给用户的感觉是已经清理了，所以，要手动更改正在运行的进程的数量，不能重新获取
        // setProcessCount();
        // 重新获取正在运行的进程的数量
        runningProcessCount = runningProcessCount - deleteInfos.size();
        // 重新设置显示进度
        int progress = (int) (runningProcessCount * 100f / allProcessCount + 0.5f);
        mCount.setLeft("正在运行" + runningProcessCount + "个");
        mCount.setProgress(progress);
        setMemory();

        // 更新界面
        myadapter.notifyDataSetChanged();

    }

}
