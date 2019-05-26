package com.hzp.mobilesafe.activity.home.commonTool;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hzp.mobilesafe.R;
import com.hzp.mobilesafe.bean.AppInfo;
import com.hzp.mobilesafe.db.dao.AppLockDao;
import com.hzp.mobilesafe.engine.AppEngine;

import java.util.ArrayList;
import java.util.List;

public class AppLockActivity extends Activity implements View.OnClickListener {

    private AppLockDao appLockDao;
    private Button mBtnUnLock;
    private Button mBtnLock;
    private LinearLayout mLLUnLock;
    private LinearLayout mLLLock;
    private ListView mUnLockListView;
    private ListView mLockListView;
    private TextView mUnLockCount;
    private TextView mLockCount;
    private List<AppInfo> allAppInfos;
    private List<AppInfo> unLocks;
    private List<AppInfo> locks;
    private Myadapter unlockAdapter;
    private Myadapter lockAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_app_lock );
        appLockDao = new AppLockDao(this);
        initView();
        initData();
    }
    /**
     * 初始化控件
     *
     * 2016-10-22 下午3:15:24
     */
    private void initView() {
        mBtnUnLock = (Button) findViewById(R.id.applock_btn_unlock);
        mBtnLock = (Button) findViewById(R.id.applock_btn_lock);

        mLLUnLock = (LinearLayout) findViewById(R.id.applock_ll_unlock);
        mLLLock = (LinearLayout) findViewById(R.id.applock_ll_lock);

        mUnLockListView = (ListView) findViewById(R.id.applock_lv_unlocks);
        mLockListView = (ListView) findViewById(R.id.applock_lv_locks);

        mUnLockCount = (TextView) findViewById(R.id.applock_tv_unlockcount);
        mLockCount = (TextView) findViewById(R.id.applock_tv_lockcount);

        mBtnUnLock.setOnClickListener(this);
        mBtnLock.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.applock_btn_unlock:

                //未加锁的按钮 ： 蓝色背景，白色字体，未加锁列表显示   已加锁：白色背景，蓝色字体，已加锁列表隐藏
                mBtnUnLock.setBackgroundResource(R.drawable.shape_applock_btn_unlock_select);
                mBtnUnLock.setTextColor( Color.WHITE);
                mLLUnLock.setVisibility(View.VISIBLE);

                mBtnLock.setBackgroundResource(R.drawable.shape_applock_btn_lock_unselect);
                mBtnLock.setTextColor(Color.parseColor("#429ED6"));//parseColor：使用色值设置文本颜色
                mLLLock.setVisibility(View.GONE);

                break;
            case R.id.applock_btn_lock:
                //已加锁：蓝色背景，白色字体，已加锁列表显示  未加锁：白色背景，蓝色字体，未加锁的列表隐藏
                mBtnLock.setBackgroundResource(R.drawable.shape_applock_btn_lock_select);
                mBtnLock.setTextColor(Color.WHITE);
                mLLLock.setVisibility(View.VISIBLE);

                mBtnUnLock.setBackgroundResource(R.drawable.shape_applock_btn_unlock_unselect);
                mBtnUnLock.setTextColor(Color.parseColor("#429ED6"));//parseColor：使用色值设置文本颜色
                mLLUnLock.setVisibility(View.GONE);

                break;
        }
    }

    /**
     * 加载展示数据
     *
     * 2016-10-22 下午3:38:09
     */
    private void initData() {
		/*new Thread(){

			public void run() {
				allAppInfos = AppEngine.getAllAppInfos(getApplicationContext());

				//1.拆分加锁和未加锁数据
				//因为要分别在已加锁和未加锁的listview中展示数据
				unLocks = new ArrayList<AppInfo>();
				locks = new ArrayList<AppInfo>();
				//判断应用是否枷锁
				for (AppInfo info : allAppInfos) {
					if (appLockDao.queryMode(info.packageName)) {
						//加锁，保存在加锁的集合
						locks.add(info);
					}else{
						//未加锁，保存在未加锁的集合中
						unLocks.add(info);
					}
				}
				//2.数据加载完成，展示数据，展示数据，在主线程实现
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						unlockAdapter = new Myadapter(false);
						lockAdapter = new Myadapter(true);
						mUnLockListView.setAdapter(unlockAdapter);
						mLockListView.setAdapter(lockAdapter);
					}
				});
			};
		}.start();*/


        //异步加载操作
        //参数1：子线程执行所需的参数类型
        //参数2：更新进度的类型
        //参数3：子线程返回结果的类型
        //百度面试题：1.异步加载底层使用什么进行实现的：线程池；2：异步加载中执行多少个任务的时候就给new Thread一样的效率：5个；3：异步加载线程池最多可以有几个线程：128个
		/*new AsyncTask<String, Integer, String>(){
			//在子线程之前执行的操作
			@Override
			protected void onPreExecute() {//主线程中执行的
				super.onPreExecute();
				//实现比如显示进度条的操作
			}
			//在子线程之中执行的操作
			@Override
			protected String doInBackground(String... params) {//子线程中执行的
				//获取网络/数据库等耗时操作，相当于new Thread中的run方法
				return null;
			}
			//在子线程之后执行的操作
			//result : 子线程返回的数据
			@Override
			protected void onPostExecute(String result) {//主线程中执行
				//显示doInBackground加载出来的数据，展示数据，相当于runOnUiThread操作
				super.onPostExecute(result);
			}
			//更新获取数据的进度，相当于在doInBackground请求数据的时候，可以显示请求的进度
			@Override
			protected void onProgressUpdate(Integer... values) {//主线程中执行
				//相当于Splash界面中的下载操作中的onLoading，可以实现一边下载一边显示下载进度
				super.onProgressUpdate(values);
			}

		}.execute();*///execute() : 执行异步加载操作，相当于Thread的start方法
        MyAsyncTask myAsyncTask = new MyAsyncTask();
        myAsyncTask.execute();//执行异步
    }
    /**异步加载**/
    private class MyAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            allAppInfos = AppEngine.getAllAppInfos(getApplicationContext());

            //1.拆分加锁和未加锁数据
            //因为要分别在已加锁和未加锁的listview中展示数据
            unLocks = new ArrayList<AppInfo>();
            locks = new ArrayList<AppInfo>();
            //判断应用是否枷锁
            for (AppInfo info : allAppInfos) {
                if (appLockDao.queryMode(info.packageName)) {
                    //加锁，保存在加锁的集合
                    locks.add(info);
                }else{
                    //未加锁，保存在未加锁的集合中
                    unLocks.add(info);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            unlockAdapter = new Myadapter(false);
            lockAdapter = new Myadapter(true);
            mUnLockListView.setAdapter( unlockAdapter );
            mLockListView.setAdapter( lockAdapter );
            super.onPostExecute(result);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);
        }

    }

    private class Myadapter extends BaseAdapter {
        //3.根据不同情况，从不同的集合中获取数据展示数据

        /**标示是已加锁操作还是未加锁操作   true:已加锁    false:未加锁**/
        private boolean mIsLock;
        private TranslateAnimation leftToRight;
        private TranslateAnimation rigthToLeft;

        public Myadapter(boolean isLock){
            //从左往右
            //fromXType, 平移类型（参照物）.ABSOLUTE：绝对位置，RELATIVE_TO_SELF：以自己作为参照物；RELATIVE_TO_PARENT：以父控件作为参照物
            //fromXValue,
            //toXType,
            //toXValue
            leftToRight = new TranslateAnimation( Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 1, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
            leftToRight.setDuration(500);
            //从右往左
            rigthToLeft = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, -1, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
            rigthToLeft.setDuration(500);

            this.mIsLock = isLock;
        }

        @Override
        public int getCount() {
            setCount();
            if (mIsLock) {
                return locks.size();
            }else{
                return unLocks.size();
            }

        }

        @Override
        public Object getItem(int position) {
            if (mIsLock) {
                return locks.get(position);
            }else{
                return unLocks.get(position);
            }

        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View view;
            ViewHolder viewHolder;
            if (convertView == null) {
                view = View.inflate(getApplicationContext(), R.layout.applock_listview_item, null);
                viewHolder = new ViewHolder();
                viewHolder.mIcon = (ImageView) view.findViewById(R.id.item_iv_icon);
                viewHolder.mName = (TextView) view.findViewById(R.id.item_tv_name);
                viewHolder.mIsLock = (ImageView) view.findViewById(R.id.item_iv_islock);
                view.setTag(viewHolder);
            }else{
                view = convertView;
                viewHolder = (ViewHolder) view.getTag();
            }
            //获取数据展示数据
            final AppInfo appInfo = (AppInfo) getItem(position);
            viewHolder.mIcon.setImageDrawable(appInfo.icon);
            viewHolder.mName.setText(appInfo.name);

            //根据标示，判断显示加锁图标还是解锁图标
            if (mIsLock) {
                //已加锁操作，显示解锁图标
                viewHolder.mIsLock.setBackgroundResource(R.drawable.selector_applock_unlock);
            }else{
                //未加锁操作，显示加锁的图标
                viewHolder.mIsLock.setBackgroundResource(R.drawable.selector_applock_lock);
            }

            //实现加锁/解锁
            viewHolder.mIsLock.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    //自己不能给自己加锁，因为加锁的操作是由手机卫士来实现的
                    if (appInfo.packageName.equals(getPackageName())) {
                        Toast.makeText(getApplicationContext(), "自己不能给自己加锁", 0).show();
                        //不能执行其他操作
                        return;
                    }else{
                        //需要判断是加锁还是解锁操作
                        if (mIsLock) {
                            //条目执行动画
                            view.startAnimation(rigthToLeft);
                            //问题：因为动画执行需要时间，但是listview的特性是，删除一个条目，下一个条目回往上补充，造成动画条目不一致
                            //解决：监听动画，当动画执行完的时候，再次执行删除条目的操作
                            //动画监听
                            rigthToLeft.setAnimationListener(new Animation.AnimationListener() {
                                //动画开始执行的时候调用
                                @Override
                                public void onAnimationStart(Animation animation) {
                                    // TODO Auto-generated method stub

                                }
                                //动画重复执行的时候调用的方法
                                @Override
                                public void onAnimationRepeat(Animation animation) {
                                    // TODO Auto-generated method stub

                                }
                                //动画结束的时候调用的方法
                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    //是已加锁的内容，执行解锁的操作
                                    //1.删除数据库中的包名
                                    //2.从已加锁的界面删除数据，因为已加锁使用的时候已加锁的集合，所有要从已加锁的集合中删除
                                    //3.将删除的数据，添加到未加锁的集合中
                                    //4.刷新界面
                                    appLockDao.delete(appInfo.packageName);
                                    locks.remove(appInfo);
                                    unLocks.add(appInfo);
                                    lockAdapter.notifyDataSetChanged();
                                    unlockAdapter.notifyDataSetChanged();
                                }
                            });

                        }else{
                            //是未加锁的内容，执行的加锁的操作
                            view.startAnimation(leftToRight);
                            leftToRight.setAnimationListener(new AnimationListener() {

                                @Override
                                public void onAnimationStart(Animation animation) {
                                    // TODO Auto-generated method stub

                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {
                                    // TODO Auto-generated method stub

                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    //1.添加包名到数据库中
                                    //2.从未加锁的集合中删除
                                    //3.将删除的数据，添加到已加锁的集合中
                                    //4.刷新界面
                                    appLockDao.add(appInfo.packageName);
                                    unLocks.remove(appInfo);
                                    locks.add(appInfo);
                                    lockAdapter.notifyDataSetChanged();
                                    unlockAdapter.notifyDataSetChanged();
                                }
                            });

                        }
                    }

                }
            });

            return view;
        }

    }

    static class ViewHolder{
        ImageView mIcon,mIsLock;
        TextView mName;
    }
    /**
     * 设置Textview显示已加锁和未加锁个数
     *
     * 2016-10-22 下午4:52:56
     */
    public void setCount(){
        mUnLockCount.setText("未加锁("+ unLocks.size()+")");
        mLockCount.setText("已加锁("+ locks.size()+")");
    }


}
