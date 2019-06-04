package com.hzp.mobilesafe.activity.home.blacknumber;

import java.io.Serializable;
import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.hzp.mobilesafe.R;
import com.hzp.mobilesafe.bean.BlackNumberInfo;
import com.hzp.mobilesafe.db.dao.BlackNumberDao;

/**
 * 骚扰拦截
 */
public class BlackNumberActivity extends Activity {
    /**添加请求码**/
    private static final int REQUESTADDCODE = 100;
    /**更新请求码**/
    protected static final int REQUESTUPDATECODE = 101;
    private ImageView mEmpty;
    private ListView mListView;
    private LinearLayout mLLLoading;
    private BlackNumberDao blackNumberDao;
    private List<BlackNumberInfo> list;
    private Myadapter myadapter;

    /**查询个数**/
    private final int MAXNUM=10;
    /**起始位置**/
    private int startIndex = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_black_number );
        blackNumberDao = new BlackNumberDao(this);
        initView();
    }

    private void initView() {
        mEmpty = (ImageView) findViewById(R.id.blacknumber_iv_empty);
        mListView = (ListView) findViewById(R.id.blacknumber_lv_listview);
        mLLLoading = (LinearLayout) findViewById(R.id.blacknumber_ll_loading);

        initData();

        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //跳转到更新界面
                Intent intent = new Intent(BlackNumberActivity.this,BlackNumberUpdateActivity.class);
                //传递被点击的条目的数据给更新界面
                intent.putExtra("number", list.get(position).blacknumber);
                intent.putExtra("mode", list.get(position).mode);
                //需要将更新那个条目的索引告诉更新界面
                intent.putExtra("position", position);
                //startActivity(intent);
                startActivityForResult(intent, REQUESTUPDATECODE);
            }
        });

        //监听listview的滚动状态，当停止滚动并且当前界面的最后一条数据是listview的最后一条数据，加载下一波数据
        mListView.setOnScrollListener(new OnScrollListener() {
            //当listview的滚动状态改变的时候调用的方法
            //scrollState : listview的滚动状态
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                //SCROLL_STATE_IDLE;//空闲的状态，停止滚动的状态
                //SCROLL_STATE_TOUCH_SCROLL;//触摸滚动的状态
                //SCROLL_STATE_FLING;//快速滚动的状态（惯性滚动状态）
                //判断listview的状态是否是停止滚动状态
                if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                    //判断当前界面的最后一条数据是listview的最后一条数据
                    int lastVisiblePosition = mListView.getLastVisiblePosition();//获取当前界面显示的最后一条数据，返回的时候条目的索引
                    if (lastVisiblePosition == list.size()-1) {//判断当前屏幕最后一条数据是不是list中最后一条
                        //重新设置起始位置
                        startIndex+=MAXNUM;
                        //加载下一波数据
                        initData();
                    }
                }
            }
            //listview滚动的时候调用的方法
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                // TODO Auto-generated method stub

            }
        });

    }

    /**
     * 获取数据展示数据
     *
     * 2016-10-14 上午10:32:51
     */
    private void initData() {
        //因为是查询数据库操作，属于耗时操作，需要隐藏显示进度条
        //加载数据之前，显示进度条
        mLLLoading.setVisibility(View.VISIBLE);
        new Thread(){
            public void run() {
                //list = blackNumberDao.queryAll();
                //加载分页数据的时候，会将上次加载的数据覆盖
                if (list == null) {
                    list = blackNumberDao.queryPartAll(MAXNUM, startIndex);
                }else{
                    //将一个集合跟另一个集合合并，谁调用的addALL方法最终得到那个集合
                    //A[1,2,3]
                    //B[4,5,6]  A.addAll(B)    A[1,2,3,4,5,6]
                    list.addAll( blackNumberDao.queryPartAll(MAXNUM, startIndex));
                }


                //查询数据库完成，展示数据
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (myadapter == null) {
                            myadapter = new Myadapter();
                            mListView.setAdapter(myadapter);//给listview设置adapter,listview会自动显示第一页的数据，重复执行该方法，相当于给listview重复设置新的adapter
                        }else{
                            myadapter.notifyDataSetChanged();//在原有的界面中刷新新的数据
                        }


                        //如果listview没有数据，就显示imageView，如果listview有数据，就隐藏imageView
                        mListView.setEmptyView(mEmpty);

                        //数据加载完成之后，隐藏进度条
                        mLLLoading.setVisibility(View.GONE);
                    }
                });
            };
        }.start();
    }

    /**
     * 添加按钮的点击事件
     *@param view
     * 2016-10-14 上午10:57:03
     */
    public void enteradd(View view){
        Intent intent = new Intent(this,BlackNumberAddActivity.class);
        //startActivity(intent);
        startActivityForResult(intent, REQUESTADDCODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //接受传递过来的数据
        //因为添加界面和更新界面都会传递数据过来，需要判断数据是谁传递过来
        if (requestCode == REQUESTADDCODE) {
            //接受添加界面传递的数据
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    //获取数据
                    String number = data.getStringExtra("number");
                    //defaultValue : 默认的值，没有传递数据过来的时候，使用的值
                    int mode = data.getIntExtra("mode", -1);

                    //将数据存放到bean类
                    BlackNumberInfo blackNumberInfo = new BlackNumberInfo(number, mode);
                    //list.add(blackNumberInfo);
                    list.add(0, blackNumberInfo);//将数据添加到集合哪个位置，location：位置；object：添加的数据
                    //更新界面，将新的数据刷新出来
                    myadapter.notifyDataSetChanged();//调用次方法，会重新调用adapter的方法，在getCount和getView方法会重新设置条目的操作
                }
            }
        }else if(requestCode == REQUESTUPDATECODE){
            //接受更新界面的数据
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    int updatemode = data.getIntExtra("mode", -1);
                    //将更新的拦截类型设置给更新的条目，重新刷新界面
                    int position = data.getIntExtra("position", -1);
                    list.get(position).mode = updatemode;
                    //更新界面
                    myadapter.notifyDataSetChanged();
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private class Myadapter extends BaseAdapter{

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = View.inflate(getApplicationContext(), R.layout.blacknumber_listview_item, null);
                viewHolder = new ViewHolder();
                viewHolder.mNumber = (TextView) convertView.findViewById(R.id.item_tv_number);
                viewHolder.mMode = (TextView) convertView.findViewById(R.id.item_tv_mode);
                viewHolder.mDelete = (ImageView) convertView.findViewById(R.id.item_iv_delete);
                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) convertView.getTag();
            }
            //获取数据，展示数据
            final BlackNumberInfo blackNumberInfo = list.get(position);
            viewHolder.mNumber.setText(blackNumberInfo.blacknumber);
            //因为数据库中的拦截类型是0,1,2
            switch (blackNumberInfo.mode) {
                case 0:
                    //电话拦截
                    viewHolder.mMode.setText("电话拦截");
                    break;
                case 1:
                    //短信拦截
                    viewHolder.mMode.setText("短信拦截");
                    break;
                case 2:
                    //全部拦截
                    viewHolder.mMode.setText("全部拦截");
                    break;
            }

            //设置删除按钮的点击事件
            viewHolder.mDelete.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    //弹出对话框，提醒用户删除黑名单号码
                    AlertDialog.Builder builder = new Builder(BlackNumberActivity.this);
                    builder.setMessage("您是否删除黑名单"+blackNumberInfo.blacknumber);
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //删除黑名单号码
                            //1.从数据库中删除
                            boolean delete = blackNumberDao.delete(blackNumberInfo.blacknumber);
                            if (delete) {
                                //2.从界面删除
                                list.remove(blackNumberInfo);
                                //更新界面
                                myadapter.notifyDataSetChanged();
                            }else{
                                Toast.makeText(getApplicationContext(), "系统繁忙，请稍候再试....", 0).show();
                            }
                            //隐藏对话框
                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton("取消", null);//如果只是隐藏对话框，可以直接设置为null
                    builder.show();
                }
            });

            return convertView;
        }

    }

    static class ViewHolder{
        TextView mNumber,mMode;
        ImageView mDelete;
    }
}
