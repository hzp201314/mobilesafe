package com.hzp.mobilesafe.activity.home.antiVirus;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.ArcProgress;
import com.hzp.mobilesafe.R;
import com.hzp.mobilesafe.bean.AppInfo;
import com.hzp.mobilesafe.db.dao.AntivirusDao;
import com.hzp.mobilesafe.engine.AppEngine;

import java.util.ArrayList;
import java.util.List;

public class AntiVirusActivity extends Activity implements View.OnClickListener {

    private MyAsyncTask myAsyncTask;

    /**保存数据的集合**/
    private List<AppInfo> list = new ArrayList<AppInfo>();
    /**保存病毒的集合**/
    private List<AppInfo> antiviruslist = new ArrayList<AppInfo>();

    private ListView mListView;

    private Myadapter myadapter;

    private ArcProgress mPb;

    private TextView mPcakageName;

    private int maxProgress;

    private LinearLayout mLLScan;

    private LinearLayout mLLAgainScan;

    private TextView mText;

    private LinearLayout mLLImage;

    private int width;

    private ImageView mRight;

    private ImageView mLeft;

    private MyReceiver myReceiver;

    private List<AppInfo> allAppInfos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_anti_virus );
        setRemoveReceiver();
        initView();
        initData();
    }

    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //接受卸载的应用的包名
            String dataString = intent.getDataString();//获取广播接受者接受的数据

            System.out.println(dataString);
            //接受的包名信息中回包含package:
            //遍历list集合，查看卸载的应用程序的包名是否在集合中，如果在，从集合中删除
            //因为list集合中的数据就是从allAppInfos中获取的，当加载完成的时候，list集合中的数据是和allAppInfos中数据是一样
            for (int i = 0; i < allAppInfos.size(); i++) {
                if (dataString.contains(allAppInfos.get(i).packageName)) {
                    list.remove(allAppInfos.get(i));
                    myadapter.notifyDataSetChanged();
                    antiviruslist.remove(allAppInfos.get(i));
                    if (antiviruslist.size() > 0) {
                        mText.setText("发现"+antiviruslist.size()+"个病毒");
                    }else{
                        mText.setText("手机非常安全");
                    }
                }
            }
        }

    }

    /**
     * 注册监听卸载的广播接受者
     *
     * 2016-10-26 上午11:47:56
     */
    private void setRemoveReceiver() {
        myReceiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        //ACTION_PACKAGE_ADDED : 安装广播事件
        //ACTION_PACKAGE_REMOVED : 卸载的广播事件
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        //设置接受卸载的应用的包名
        filter.addDataScheme("package");
        registerReceiver(myReceiver, filter);
    }
    /**
     * 初始化控件
     *
     * 2016-10-26 上午9:19:46
     */
    private void initView() {
        mListView = (ListView) findViewById(R.id.anti_lv_listview);

        mPb = (ArcProgress) findViewById(R.id.anti_acp_pb);
        mPcakageName = (TextView) findViewById(R.id.anti_tv_packageName);

        mLLAgainScan = (LinearLayout) findViewById(R.id.anti_ll_againscan);
        mLLScan = (LinearLayout) findViewById(R.id.anti_ll_scan);
        mText = (TextView) findViewById(R.id.anti_tv_text);

        Button mAgain = (Button) findViewById(R.id.anti_btn_again);
        mAgain.setOnClickListener(this);

        mLLImage = (LinearLayout) findViewById(R.id.anti_ll_image);

        mLeft = (ImageView) findViewById(R.id.anti_iv_left);
        mRight = (ImageView) findViewById(R.id.anti_iv_right);
    }
    /**
     * 加载数据
     *
     * 2016-10-26 上午9:12:42
     */
    private void initData() {
        myAsyncTask = new MyAsyncTask();
        myAsyncTask.execute();
    }

    private class MyAsyncTask extends AsyncTask<Void, AppInfo, Void> {


        @Override
        protected void onPreExecute() {

            if (myAsyncTask.isCancelled()) {
                return;
            }

            list.clear();//保证每次保存都是最新的数据
            mPb.setProgress(0);//保证每次都是从0进度开始
            antiviruslist.clear();//保证每次保存新的病毒数据

            //加载数据之前先给listview设置adapter，方便后面进行更新界面操作
            myadapter = new Myadapter();
            mListView.setAdapter(myadapter);

            //重新扫描，进度条布局显示，重新扫描布局隐藏
            mLLAgainScan.setVisibility(View.GONE);
            mLLImage.setVisibility(View.GONE);
            mLLScan.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            //1.加载系统中所有的应用程序信息
            allAppInfos = AppEngine.getAllAppInfos(getApplicationContext());

            maxProgress = allAppInfos.size();

            for (AppInfo appInfo : allAppInfos) {

                if (myAsyncTask.isCancelled()) {
                    return null;
                }

                //8.根据应用的签名信息，查询数据库判断应用是否是病毒
                boolean isantivirus = AntivirusDao.isAntivirus(getApplicationContext(), appInfo.md5);
                //将是否是病毒的标示，保存到bean类
                appInfo.isAntiVirus = isantivirus;

                //2.将应用程序的信息，传递给onProgressUpdate方法进行显示
                publishProgress(appInfo);
                SystemClock.sleep(300);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            if (myAsyncTask.isCancelled()) {
                return;
            }

            //6.加载完成，自动滚动到listview的第一个条目
            mListView.smoothScrollToPosition(0);

            //10.扫描完成，隐藏进度条布局，显示重新扫描的布局，显示病毒个数信息
            mLLScan.setVisibility(View.GONE);
            mLLAgainScan.setVisibility(View.VISIBLE);
            mLLImage.setVisibility(View.VISIBLE);
            //设置显示病毒的个数的信息
            if (antiviruslist.size() > 0) {
                mText.setText("发现"+antiviruslist.size()+"个病毒");
            }else{
                mText.setText("手机非常安全");
            }

            //11.将进度条的布局设置成图片
            mLLScan.setDrawingCacheEnabled(true);//是否可以绘制控件的图片，true:可以
            mLLScan.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);//设置绘制的图片的质量
            Bitmap drawingCache = mLLScan.getDrawingCache();//绘制图片，并返回使用bitmap保存的图片
            //12.拆分进度条布局的图片，放到相应的imageview进行操作
            Bitmap leftBitmap = getLeftBitmap(drawingCache);
            Bitmap rihtBitmap = getRihtBitmap(drawingCache);
            //把图片设置给imageview显示
            mLeft.setImageBitmap(leftBitmap);
            mRight.setImageBitmap(rihtBitmap);

            //13.开始动画
            //渐变+平移效果
            startAnimaition();
            super.onPostExecute(result);
        }

        @Override
        protected void onProgressUpdate(AppInfo... values) {

            if (myAsyncTask.isCancelled()) {
                return;
            }

            //3.接受doInBackground传递过来的数据
            AppInfo appInfo = values[0];
            //4.显示数据
            //9.如果是病毒，添加list集合第一个位置
            if (appInfo.isAntiVirus) {
                list.add(0,appInfo);
                //保存到病毒的集合中，方便重新扫描界面显示
                antiviruslist.add(appInfo);
            }else{
                list.add(appInfo);
            }
            myadapter.notifyDataSetChanged();
            //5.每加载一条数据，自动滚动到listview的最后一个条目
            mListView.smoothScrollToPosition(list.size());

            //7.每加载一个条目，显示一个进度，显示加载的应用程序的包名
            //加载条目占用总条目数的比例
            int progress = (int) (list.size() * 100f / maxProgress + 0.5f);
            mPb.setProgress(progress);//设置当前进度
            mPcakageName.setText(appInfo.packageName);

            super.onProgressUpdate(values);
        }

    }

    /**
     * 执行动画的操作
     *
     * 2016-10-26 上午11:23:55
     */
    public void startAnimaition() {
        //左右图片：平移+渐变    重新扫描的布局：渐变
        //mLeft.setTranslationX(translationX)
        //mLeft.setAlpha(alpha)
        //target : 执行动画的控件
        //propertyName : 执行动画的名称
        //values : 动画执行的值
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(mLeft, "translationX", 0,-width);
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(mLeft, "alpha", 1.0f,0.0f);

        ObjectAnimator animator3 = ObjectAnimator.ofFloat(mRight, "translationX", 0,width);
        ObjectAnimator animator4 = ObjectAnimator.ofFloat(mRight, "alpha", 1.0f,0.0f);

        ObjectAnimator animator5 = ObjectAnimator.ofFloat(mLLAgainScan, "alpha", 0.0f,1.0f);

        //组合动画
        AnimatorSet animatorSet = new AnimatorSet();
        //设置动画一起执行
        animatorSet.playTogether(animator1,animator2,animator3,animator4,animator5);
        animatorSet.setDuration(1000);
        //执行动画
        animatorSet.start();
    }

    /**
     * 拆分进度条图片左边的图片
     *@param drawingCache
     * 2016-10-26 上午11:05:35
     */
    public Bitmap getLeftBitmap(Bitmap drawingCache) {
        //1.设置新的图片的宽高
        width = drawingCache.getWidth() / 2;
        int height = drawingCache.getHeight();
        //2.创建保存新的图片的载体
        //config : 图片的配置信息，因为是按照原图片进行绘制的，所以配置信息必须和原图一致
        Bitmap createBitmap = Bitmap.createBitmap(width, height, drawingCache.getConfig());
        //3.绘制图片
        //bitmap : 绘制的图片保存的地方
        Canvas canvas = new Canvas(createBitmap);
        Matrix matrix = new Matrix();
        Paint paint = new Paint();
        //bitmap : 根据哪张图片绘制，原图
        //matrix : 矩阵
        //paint : 画笔
        canvas.drawBitmap(drawingCache, matrix, paint);

        return createBitmap;
    }

    /**
     * 拆分进度条图片右边的图片
     *@param drawingCache
     * 2016-10-26 上午11:05:35
     */
    public Bitmap getRihtBitmap(Bitmap drawingCache) {
        //1.设置新的图片的宽高
        int width = drawingCache.getWidth() / 2;
        int height = drawingCache.getHeight();
        //2.创建保存新的图片的载体
        //config : 图片的配置信息，因为是按照原图片进行绘制的，所以配置信息必须和原图一致
        Bitmap createBitmap = Bitmap.createBitmap(width, height, drawingCache.getConfig());
        //3.绘制图片
        //bitmap : 绘制的图片保存的地方
        Canvas canvas = new Canvas(createBitmap);
        Matrix matrix = new Matrix();
        //参数：画布x和y轴平移的距离
        matrix.postTranslate(-width, 0);
        Paint paint = new Paint();
        //bitmap : 根据哪张图片绘制，原图
        //matrix : 矩阵
        //paint : 画笔
        canvas.drawBitmap(drawingCache, matrix, paint);

        return createBitmap;
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
            View view;
            ViewHolder viewHolder;
            if (convertView == null) {
                view = View.inflate(getApplicationContext(), R.layout.antivirus_listview_item, null);
                viewHolder = new ViewHolder();
                viewHolder.mIcon = (ImageView) view.findViewById(R.id.item_iv_icon);
                viewHolder.mName = (TextView) view.findViewById(R.id.item_tv_name);
                viewHolder.mIsAntVirus = (TextView) view.findViewById(R.id.item_tv_isAntivirus);
                viewHolder.mClear = (ImageView) view.findViewById(R.id.item_iv_clear);
                view.setTag(viewHolder);
            }else{
                view = convertView;
                viewHolder = (ViewHolder) view.getTag();
            }
            //获取数据展示数据
            final AppInfo appInfo = list.get(position);
            viewHolder.mIcon.setImageDrawable(appInfo.icon);
            viewHolder.mName.setText(appInfo.name);

            //根据bean类中的病毒标示，设置显示不同的结果
            if (appInfo.isAntiVirus) {
                //病毒
                viewHolder.mIsAntVirus.setText("病毒");
                viewHolder.mIsAntVirus.setTextColor( Color.RED);
                viewHolder.mClear.setVisibility(View.VISIBLE);
            }else{
                //安全
                viewHolder.mIsAntVirus.setText("安全");
                viewHolder.mIsAntVirus.setTextColor(Color.GREEN);
                viewHolder.mClear.setVisibility(View.GONE);
            }

            //卸载操作
            viewHolder.mClear.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    //跳转到系统的卸载界面
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.DELETE");
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.setData( Uri.parse("package:"+appInfo.packageName));
                    startActivity(intent);
                }
            });
            return view;
        }

    }

    static class ViewHolder{
        TextView mName,mIsAntVirus;
        ImageView mIcon,mClear;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.anti_btn_again:
                //重新扫描
                //关门的动画
                backAnimation();
                break;
        }
    }
    /**
     * 关门的动画
     *
     * 2016-10-26 上午11:35:10
     */
    private void backAnimation() {
        //平移
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(mLeft, "translationX", -width,0);
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(mLeft, "alpha", 0.0f,1.0f);

        ObjectAnimator animator3 = ObjectAnimator.ofFloat(mRight, "translationX", width,0);
        ObjectAnimator animator4 = ObjectAnimator.ofFloat(mRight, "alpha", 0.0f,1.0f);

        ObjectAnimator animator5 = ObjectAnimator.ofFloat(mLLAgainScan, "alpha", 1.0f,0.0f);

        //组合动画
        AnimatorSet animatorSet = new AnimatorSet();
        //设置动画一起执行
        animatorSet.playTogether(animator1,animator2,animator3,animator4,animator5);
        animatorSet.setDuration(1000);
        //动画执行完，才会重新进行扫描操作
        animatorSet.addListener(new AnimatorListener() {
            //动画开始调用
            @Override
            public void onAnimationStart(Animator animation) {
                // TODO Auto-generated method stub

            }
            //动画重复调用
            @Override
            public void onAnimationRepeat(Animator animation) {
                // TODO Auto-generated method stub

            }
            //动画结束调用
            @Override
            public void onAnimationEnd(Animator animation) {
                initData();
            }
            //动画取消调用
            @Override
            public void onAnimationCancel(Animator animation) {
                // TODO Auto-generated method stub

            }
        });
        //执行动画
        animatorSet.start();
        //animatorSet.end();//动画停止
        //animatorSet.cancel();//取消动画
    }
    @Override
    protected void onDestroy() {
        myAsyncTask.cancel(true);
        unregisterReceiver(myReceiver);
        super.onDestroy();
    }

}
