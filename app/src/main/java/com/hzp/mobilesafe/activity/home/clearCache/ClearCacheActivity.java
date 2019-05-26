package com.hzp.mobilesafe.activity.home.clearCache;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.IPackageDataObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageStats;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hzp.mobilesafe.R;
import com.hzp.mobilesafe.bean.AppInfo;
import com.hzp.mobilesafe.bean.CacheInfo;
import com.hzp.mobilesafe.engine.AppEngine;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ClearCacheActivity extends Activity implements View.OnClickListener {

    private ImageView mLine;
    private PackageManager pm;
    private MyAsyncTask myAsyncTask;
    private Myadapter myadapter;
    private int maxProgress;

    /**保存展示的信息**/
    private List<CacheInfo> list = new ArrayList<CacheInfo>();
    /**保存有缓存的软件信息**/
    private List<CacheInfo> cachelist = new ArrayList<CacheInfo>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_clear_cache );
        initView();
        initData();
    }
    /**
     * 初始化控件
     *
     * 2016-10-24 下午4:43:08
     */
    private void initView() {
        mLine = (ImageView) findViewById(R.id.clearcache_iv_line);
        mListView = (ListView) findViewById(R.id.clearcache_lv_listview);

        mIcon = (ImageView) findViewById(R.id.clearcache_iv_icon);
        mPb = (ProgressBar) findViewById(R.id.clearcache_pb_progress);
        mPackageName = (TextView) findViewById(R.id.clearcache_tv_packageName);
        mCacheSize = (TextView) findViewById(R.id.clearcache_tv_cachesize);

        mScan = (RelativeLayout) findViewById(R.id.clearcache_rel_scan);
        mRelagain = (RelativeLayout) findViewById(R.id.clearcache_rel_againscan);

        mText = (TextView) findViewById(R.id.clearcache_tv_text);

        Button mAagin = (Button) findViewById(R.id.clearcache_btn_again);

        mAllClear = (Button) findViewById(R.id.clearcache_btn_allclear);

        mAagin.setOnClickListener(this);
    }

    /**
     * 加载数据，显示数据
     *
     * 2016-10-24 下午4:37:21
     */
    private void initData() {
        pm = getPackageManager();
        myAsyncTask = new MyAsyncTask();
        myAsyncTask.execute();
    }

    //查询系统中所有的应用程序的信息，并展示
    private class MyAsyncTask extends AsyncTask<Void, CacheInfo, Void>{

        @Override
        protected void onPreExecute() {

            //判断异步加载是否取消，如果取消不在执行我们的代码
            //isCancelled : 获取是否取消的标示
            if (myAsyncTask.isCancelled()) {
                return;
            }

            list.clear();//保证每次获取的都是新的数据
            mPb.setProgress(0);//保证每次进度都是从0开始
            cachelist.clear();//保证每次保存的都是新的数据

            myadapter = new Myadapter();
            mListView.setAdapter( myadapter );

            //2.1.3重新扫描，显示进度条布局，隐藏快速扫描布局
            mRelagain.setVisibility(View.GONE);
            mScan.setVisibility(View.VISIBLE);

            //1.实现线的平移动画
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.translate_clearcache_line);//加载一个xml文件设置的动画
            mLine.startAnimation(animation);

            //当加载数据的时候不能使用一键清理按钮
            mAllClear.setEnabled(false);//设置按钮不可用

            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            //2.获取系统中安装应用程序的信息，并且实现获取一个展示一个的效果
            List<AppInfo> allAppInfos = AppEngine.getAllAppInfos(getApplicationContext());

            maxProgress = allAppInfos.size();

            //遍历集合
            for (AppInfo appInfo : allAppInfos) {
                if (myAsyncTask.isCancelled()) {
                    return null;
                }
                //获取每个应用程序的信息，并展示
                //2.1.反射getPackageSizeInfo方法根据应用程序的包名，获取应用程序的缓存大小
                //参数1：应用程序的包名
                //参数2：应用程序缓存信息存放的aidl文件
                //pm.getPackageSizeInfo(appInfo.packageName, mStatsObserver);
                //因为getPackageSizeInfo系统隐藏了所以需要反射进行操作
                try {
                    Method method = PackageManager.class.getDeclaredMethod("getPackageSizeInfo", String.class,IPackageStatsObserver.class);
                    method.invoke(pm, appInfo.packageName,mStatsObserver);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                SystemClock.sleep(300);
            }
            //publishProgress(values);//调用publishProgress方法可以间接的调用onProgressUpdate,只能在AsyncTask内部使用，不能再外部使用

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            if (myAsyncTask.isCancelled()) {
                return;
            }

            //2.9.展示数据完成，自动滚动到listview的头条目
            mListView.smoothScrollToPosition(0);

            //2.1.1.展示数据完成，将进度条布局隐藏，显示快速扫描布局，并设置缓存信息
            mLine.clearAnimation();//清除线的执行动画
            mScan.setVisibility(View.GONE);
            mRelagain.setVisibility(View.VISIBLE);
            //设置缓存的展示信息
            long totalCache = 0;
            for (CacheInfo info : cachelist) {
                totalCache+=info.cacheSize;
            }
            mText.setText("总共有"+cachelist.size()+"个缓存软件，总共"+Formatter.formatFileSize(getApplicationContext(), totalCache));

            //扫描完成设置按钮可用
            mAllClear.setEnabled(true);

            super.onPostExecute(result);
        }

        //2.4.1.创建供外部调用的方法，在此方法内调用publishProgress
        public void update(CacheInfo cacheInfo){
            publishProgress(cacheInfo);
        }

        //onProgressUpdate不能直接调用
        @Override
        protected void onProgressUpdate(CacheInfo... values) {

            if (myAsyncTask.isCancelled()) {
                return;
            }

            //2.5.获取aidl传递的数据
            CacheInfo cacheInfo = values[0];
            //2.6.展示数据
            //2.7.判断如果有缓存信息，将有缓存的软件存放到集合中的首位
            if (cacheInfo.cacheSize > 0) {
                list.add(0, cacheInfo);
                //将有缓存的信息保存到缓存的集合中，方便快速扫描布局使用
                cachelist.add(cacheInfo);
            }else{
                list.add(cacheInfo);
            }
            //因为是每获取一个数据，展示一个数据，所以会频繁的调用onProgressUpdate方法，所以不能在onProgressUpdate给listview设置adapter
            myadapter.notifyDataSetChanged();
            //2.8.没加载一个数据，listview自动滚动到listview的底部
            mListView.smoothScrollToPosition(list.size());//滚动到listview的那个条目,position:条目的索引

            //2.1.0.设置没加载一个数据，显示进度条和包名缓存大小信息
            mIcon.setImageDrawable(cacheInfo.icon);
            mPackageName.setText(cacheInfo.packageName);
            mCacheSize.setText("缓存大小:"+ Formatter.formatFileSize(getApplicationContext(), cacheInfo.cacheSize));
            //当前展示的应用程序的个数，展示应用程序总个数的比例
            int progress = (int) (list.size() * 100f / maxProgress + 0.5f);
            mPb.setProgress(progress);

            super.onProgressUpdate(values);
        }
    }

    /**
     * 2.2.在aidl中获取缓存大小操作
     */
    IPackageStatsObserver.Stub mStatsObserver = new IPackageStatsObserver.Stub() {
        public void onGetStatsCompleted(PackageStats stats, boolean succeeded) {

            //2.3.将应用程序的信息获取出来，保存CachInfo bean类中
            CacheInfo cacheInfo = new CacheInfo();
            //获取缓存大小操作
            long cachesize = stats.cacheSize;//获取应用程序的缓存大小
            cacheInfo.cacheSize = cachesize;
            String packageName = stats.packageName;//获取应用程序的包名
            cacheInfo.packageName = packageName;
            //根据包名获取应用程序的图标和名称
            try {
                ApplicationInfo applicationInfo = pm.getApplicationInfo(packageName, 0);
                //获取应用程序的名称
                String name = applicationInfo.loadLabel(pm).toString();
                cacheInfo.name = name;
                //获取应用程序的图标
                Drawable icon = applicationInfo.loadIcon(pm);
                cacheInfo.icon = icon;
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }

            if (myAsyncTask.isCancelled()) {
                return;
            }

            //2.4.展示获取的应用程序信息
            myAsyncTask.update(cacheInfo);
        }
    };
    private ListView mListView;
    private TextView mCacheSize;
    private TextView mPackageName;
    private ProgressBar mPb;
    private ImageView mIcon;
    private RelativeLayout mScan;
    private RelativeLayout mRelagain;
    private TextView mText;
    private Button mAllClear;



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
            View view;
            ViewHolder viewHolder;
            if (convertView == null) {
                view = View.inflate(getApplicationContext(), R.layout.clearcache_listview_item, null);
                viewHolder = new ViewHolder();
                viewHolder.mIcon = (ImageView) view.findViewById(R.id.item_iv_icon);
                viewHolder.mName = (TextView) view.findViewById(R.id.item_tv_name);
                viewHolder.mSize = (TextView) view.findViewById(R.id.item_tv_size);
                viewHolder.mClear = (ImageView) view.findViewById(R.id.item_iv_clear);
                view.setTag(viewHolder);
            }else{
                view = convertView;
                viewHolder = (ViewHolder) view.getTag();
            }

            //获取数据展示数据
            final CacheInfo cacheInfo = list.get(position);
            viewHolder.mIcon.setImageDrawable(cacheInfo.icon);
            viewHolder.mName.setText(cacheInfo.name);
            String size = Formatter.formatFileSize(getApplicationContext(), cacheInfo.cacheSize);
            viewHolder.mSize.setText("缓存大小:"+size);
            //设置有缓存的展示清理按钮，没有的不展示
            if (cacheInfo.cacheSize > 0) {
                viewHolder.mClear.setVisibility(View.VISIBLE);
            }else{
                viewHolder.mClear.setVisibility(View.GONE);
            }


            //点击清理按钮跳转到系统的详情界面
            viewHolder.mClear.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.setData( Uri.parse("package:"+cacheInfo.packageName));
                    startActivityForResult(intent, 0);
                }
            });

            return view;
        }

    }

    static class ViewHolder{
        TextView mName,mSize;
        ImageView mIcon,mClear;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        initData();
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clearcache_btn_again:
                //2.1.2.快速扫描
                initData();
                break;
        }
    }


    //异步加载的问题，异步加载是不会停止的

    @Override
    protected void onDestroy() {
        //只是设置异步加载取消标示，true:可以取消，false:不可以取消，但是并不会真正的取消异步加载操作
        myAsyncTask.cancel(true);
        super.onDestroy();
    }

    /**
     * 一键清理的点击事件
     *@param view
     * 2016-10-25 上午11:20:18
     */
    public void clearall(View view) {
        //freeStorageAndNotify(long freeStorageSize, IPackageDataObserver observer)//请求使用的空间
        try {
            Method method = PackageManager.class.getDeclaredMethod( "freeStorageAndNotify", long.class, IPackageDataObserver.class );
            method.invoke( pm, Long.MAX_VALUE, new IPackageDataObserver.Stub() {
                //请求空间成功调用的方法
                @Override
                public void onRemoveCompleted(String packageName, boolean succeeded)
                        throws RemoteException {
                    //表示清理缓存成功，重新扫描
                    //android系统中所有的aidl都是在子线程中的执行
                    runOnUiThread( new Runnable() {
                        public void run() {
                            initData();
                        }
                    } );
                }
            } );
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
