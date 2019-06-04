package com.hzp.mobilesafe.activity.home.commonTool;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.TextView;

import com.hzp.mobilesafe.R;
import com.hzp.mobilesafe.bean.CommonNumberGroupsInfo;
import com.hzp.mobilesafe.db.dao.CommonNumberDao;

/**
 * 常用电话号码
 */
public class CommonNumberActivity extends Activity {
    private ExpandableListView mListView;
    private List<CommonNumberGroupsInfo> list;

    /**保存被打开的组的索引**/
    private int expandGroup=-1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_number);
        initView();
        initData();
    }
    /**
     * 初始化控件
     *
     */
    private void initView() {
        mListView = (ExpandableListView) findViewById(R.id.commonnumber_elv_listview);

        //listview的组的点击事件，由getGroupView返回
        //groupPosition : 被点击的组的索引
        //id ： 被点击的组的id
        mListView.setOnGroupClickListener(new OnGroupClickListener() {
            //View : 被点击的组的view对象
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                //1.点击同一个组的，关闭 -> 打开     打开 -> 关闭
                //2.点击两个组的，打开订餐点击，点击公共服务，打开公共服务，关闭订餐电话

                //如果点击的是公共服务，判断公共服务的索引和保存订餐电话的索引是否一致
                if (expandGroup != groupPosition) {
                    mListView.collapseGroup(expandGroup);//关闭之前打开的组
                }

                //isGroupExpanded : 获取当前组是否打开，true:打开    false:关闭
                //groupPosition :被点击的组的索引
                if (mListView.isGroupExpanded(groupPosition)) {
                    //打开 -> 关闭
                    mListView.collapseGroup(groupPosition);//根据组的索引，关闭相应的组
                    //如果点击订餐电话之后，没有点击公共服务，还是点击的订餐电话】
                    expandGroup = -1;
                }else{
                    //关闭 -> 打开
                    mListView.expandGroup(groupPosition);//根据组的索引，打开相应的组
                    //将打开的组的索引保存，方便后面点击不同组的操作
                    expandGroup = groupPosition;
                }

                //返回true:执行我们自己的点击事件，返回false：执行的是系统的listview的点击事件，我们的就不会执行
                return true;
            }
        });

        //孩子的点击事件，实现拨打电话
        mListView.setOnChildClickListener(new OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {

                //拨打电话 （需要打电话权限）
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_DIAL);//设置打电话的标示
                intent.setData(Uri.parse("tel:"+list.get(groupPosition).child.get(childPosition).number));
                startActivity(intent);

                return true;
            }
        });

    }

    /**
     * 展示数据
     *
     */
    private void initData() {
        list = CommonNumberDao.getGroup(this);

        mListView.setAdapter(new MyAdapter());

    }

    private class MyAdapter extends BaseExpandableListAdapter{
        //获取组的个数，跟baseAdapter的getCount用法一样
        @Override
        public int getGroupCount() {
            return list.size();
        }
        //获取组的孩子的个数，跟baseAdapter的getCount用法一样
        @Override
        public int getChildrenCount(int groupPosition) {
            return list.get(groupPosition).child.size();
        }
        //根据组的索引获取组对应的数据，跟BaseAdapter中的getItem用法一样
        @Override
        public Object getGroup(int groupPosition) {
            return list.get(groupPosition);
        }
        //根据组中的孩子的索引获取孩子对应的数据，跟BaseAdapter中的getItem用法一样
        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return list.get(groupPosition).child.get(childPosition);
        }
        //获取组的id,跟BaseAdapter中的getItemId用户一样
        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }
        //获取组的孩子的id,跟BaseAdapter中的getItemId用户一样
        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }
        //提供给系统调用，告诉系统，我们是否设置了id，如果返回true:表示设置id,系统就会调用getGroupId和getChildId方法，如果返回false:表示没有设置id,不会调用getGroupId和getChildId
        @Override
        public boolean hasStableIds() {
            return true;
        }
        //设置组的样式，跟BaseAdapter中的getView方法用法一样
        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            TextView textView = new TextView(getApplicationContext());//新建一个TextView作为组的View对象
            textView.setText(list.get(groupPosition).name);
            textView.setTextColor(Color.BLACK);
            textView.setBackgroundColor(Color.GRAY);
            textView.setTextSize(20);
            textView.setPadding(18, 8, 8, 8);
            return textView;
        }
        //设置组的孩子的样式，跟BaseAdapter中的getView方法用法一样
        @Override
        public View getChildView(int groupPosition, int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {
            TextView textView = new TextView(getApplicationContext());
            textView.setText(list.get(groupPosition).child.get(childPosition).name+"\n"+list.get(groupPosition).child.get(childPosition).number);
            textView.setTextColor(Color.BLACK);
            textView.setBackgroundColor(Color.WHITE);
            textView.setTextSize(20);
            textView.setPadding(18, 8, 8, 8);
            return textView;
        }
        //设置孩子是否可以点击，返回true:孩子可以点击，返回false:孩子不可以点击
        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

    }
}
