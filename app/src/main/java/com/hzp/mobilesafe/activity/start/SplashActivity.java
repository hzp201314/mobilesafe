package com.hzp.mobilesafe.activity.start;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.hzp.mobilesafe.R;
import com.hzp.mobilesafe.activity.home.HomeActivity;
import com.hzp.mobilesafe.service.ProtectedService;
import com.hzp.mobilesafe.url.ApkVersionUrl;
import com.hzp.mobilesafe.utils.Constants;
import com.hzp.mobilesafe.utils.PackageUtil;
import com.hzp.mobilesafe.utils.SharedPreferencesUtil;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class SplashActivity extends Activity {
    public static final String TAG=SplashActivity.class.getSimpleName();
    private TextView mSplashTvVersion;
    private String version;
    private String message;
    private int code;
    private ProgressDialog progressDialog;
    private String apkurl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        代码去除标题栏，1.必须放在setContentView之前执行，2.只在当前的activity生效
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);
        initView();
        initActivity();
        //拷贝数据库
        copyDB("address.db");

/*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //android8.0以上通过startForegroundService启动service
            startForegroundService(new Intent(this,ProtectedService.class));
        } else {
            startService(new Intent(this,ProtectedService.class));
        }*/


        //开启守护进程
//        startService(new Intent(this,ProtectedService.class));
        createshortcut();
    }
    /**
     * 创建快捷方式
     *
     * 2016-10-26 下午2:45:43
     */
    private void createshortcut() {
        //判断快捷方式是否创建
        boolean b = SharedPreferencesUtil.getBoolean(getApplicationContext(), Constants.SHORTCUT, false);
        if (!b) {
            Intent intent = new Intent();
            intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
            //设置快捷方式的名称
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "手机卫士95");
            //设置快捷方式的图标
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, bitmap);
            //设置快捷方式的操作
            Intent enter = new Intent(this,SplashActivity.class);
            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, enter);
            sendBroadcast(intent);

            SharedPreferencesUtil.saveBoolean(getApplicationContext(), Constants.SHORTCUT, true);
        }
    }
    /**
     * 拷贝数据库的方法
     *@param dbName
     * 2016-10-16 下午2:48:30
     */
    private void copyDB(String dbName) {
        //判断如果数据库已经拷贝成功，不需要再次拷贝
        File file = new File(getFilesDir(), dbName);
        if (!file.exists()) {
            //打开assets中保存的资源
            //1.获取assets目录的管理者
            AssetManager assets = getAssets();
            InputStream in=null;
            FileOutputStream out = null;
            try {
                //2.读取数据资源
                in = assets.open(dbName);
                //getFilesDir() : data -> data -> 应用程序的包名 -> files
                //getCacheDir() : data -> data -> 应用程序的包名 -> cache
                out = new FileOutputStream(file);
                //3.读写操作
                byte[] b = new byte[1024];//缓冲区域
                int len = -1; //保存读取的长度
                while((len = in.read(b)) != -1){
                    out.write(b, 0, len);
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }finally{
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void initView() {
        mSplashTvVersion = (TextView) findViewById(R.id.splash_tv_version);
    }

    private void initActivity() {
        //获取当前应用程序的版本名称，设置给textview展示
        mSplashTvVersion.setText("版本:"+PackageUtil.getVersionName(this));

        //延迟几秒钟时间，实现更新操作
        //r : handler接受消息执行的操作
        //delayMillis : 延迟时间
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                /*根据设置中心保存的开关状态，设置是否检查更新操作*/
                boolean b = SharedPreferencesUtil.getBoolean( getApplicationContext(), Constants.ISUPDATE, true );
                if(b) {
                    checkUpdate();
                }else {
                    enterHome();
                }
            }
        },2000);
    }

    /**
     * 检查版本更新
     */
    private void checkUpdate() {

        //1.链接服务器，获取服务器数据，判断是否有最新版本
        //1.1.链接服务器
        //  a.联网操作，耗时操作，子线程中 ；
        //  b.权限 联网的权限 <uses-permission android:name="android.permission.INTERNET" />
        //  c.httpURlConnection  httpclient(android6.0以后禁止使用)  xutils  volly  okhttp
        //connTimeout : 链接超时时间
        HttpUtils httpUtils=new HttpUtils(2000);
        //链接请求服务器
        //  method : 请求方式
        //  url ： 请求路径
        //  params : 请求参数   http://baidu.com/?name=ls&psw=123456
        //  RequestCallBack : 请求的回调监听
        //  ctrl+shift+u ：小转大 ,大转小,互转。
        httpUtils.send(HttpMethod.GET, ApkVersionUrl.CHECK_VERSION_URL, null, new RequestCallBack<String>() {
            //请求成功调用的方法
            //responseInfo : 保存服务器返回的数据
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                //1.2.链接成功，获取服务器返回的数据，问题：服务会返回那些数据，code:新版本的版本号   apkurl:新版本的下载路径    msg:更新版本的描述信息，更新内容的操作
                String json=responseInfo.result;
                Log.d(TAG, "onSuccess: json="+json);
                //1.2.1.解析服务器返回json
                processJson(json);

            }

            //请求失败调用的方法
            @Override
            public void onFailure(HttpException e, String s) {
                Toast.makeText(getApplicationContext(),"连接服务器失败",Toast.LENGTH_SHORT).show();
                enterHome();
            }
        });

    }

    /**
     * 解析json数据
     * @param json
     */
    private void processJson(String json) {
        try {
            //将json封装成JSONObject对象
            JSONObject jsonObject = new JSONObject(json);
            String status =jsonObject.isNull("status")?"":jsonObject.getString("status");
            String msg =jsonObject.isNull("msg")?"":jsonObject.getString("msg");
            String backMsg = jsonObject.isNull("backMsg") ? "" : jsonObject.getString("backMsg");
            if("ok".equals(status)){
                JSONObject jsonBackMsg = new JSONObject(backMsg);
                code = jsonBackMsg.isNull("code") ? 1 : jsonBackMsg.getInt("code");
                version = jsonBackMsg.isNull("version") ? "" : jsonBackMsg.getString("version");
                apkurl = jsonBackMsg.isNull("apkurl") ? "" : jsonBackMsg.getString("apkurl");
                message = jsonBackMsg.isNull("message") ? "" : jsonBackMsg.getString("message");
                Log.d( TAG, "processJson: 返回版本信息:code="+code+",version="+version+",apkurl="+apkurl+",message="+message );
                //1.3.判断是否有最新版本
                //判断最新版本的apk的版本号是否和当前应用程序的版本号一致，如果一致，没有最新版本，如果不一致，有最新版本
                if(code>PackageUtil.getVersionCode(this)){
                    //如果不一致，有最新版本
                    //2.弹出更新版本对话框
                    showUpdateDialog();
                }else{
                    //如果一致，没有最新版本
                    //跳转到首页
                    enterHome();
                }
            }else {
                Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"解析失败",Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * 更新版本对话框dialog
     */
    private void showUpdateDialog() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        //builder.setCancelable(false);//设置对话框是否可以消失，true:可以，false:不可以,屏蔽返回键的操作
        //设置标题
        builder.setTitle("最新版本:"+version);
        //设置图标
        builder.setIcon(R.drawable.ic_launcher);
        //设置描述信息
        builder.setMessage(message);

        //监听对话框消失的操作,点击空白处对话框会消失
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                //跳转到首页
                enterHome();
            }
        });

        //设置按钮
        builder.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //隐藏对话框
                dialog.dismiss();
                //3.下载最新版本的apk
                downloadAPK();
            }
        });
        builder.setNegativeButton("以后再说", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                //隐藏对话框
                dialog.dismiss();
                //跳转到首页
                enterHome();
            }
        });
        //显示对话框
        builder.show();
        //builder.create().show();//效果一样



    }

    /**
     *  3.下载最新版本apk
     */
    private void downloadAPK() {
        //判断SD卡是否可用，即SD卡是否挂载
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            //3.2.显示下载的进度条的对话框
            showProgressDialog();
            //3.1.链接服务器，下载最新版本
            HttpUtils httpUtils=new HttpUtils();
            //问题：
            // 1.下载路径；SAVE_URL="mnt/sdcard/mobilesafe.apk"
            // 2.写SD卡权限；<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
            // 3.判断SD卡是否挂载；
            // 4.生成一个2.0版本apk
            //url : 下载路径
            //target : 保存的路径
            //callback : 回调监听
            httpUtils.download(apkurl, ApkVersionUrl.SAVE_URL, new RequestCallBack<File>() {
                @Override
                public void onSuccess(ResponseInfo<File> responseInfo) {
                    //下载完成，隐藏对话框
                    progressDialog.dismiss();
                    //4.安装apk
                    installApk();
                }

                @Override
                public void onFailure(HttpException e, String s) {
                    //下载失败，隐藏对话框
                    progressDialog.dismiss();
                    enterHome();
                }

                //设置下载进度的操作
                //total : 下载总进度
                //current : 当前进度
                //isUploading : 是否支持回调上传
                @Override
                public void onLoading(long total, long current, boolean isUploading) {
                    super.onLoading(total, current, isUploading);
                    progressDialog.setMax((int) total);//设置总进度
                    progressDialog.setProgress((int) current);//设置当前进度
                }
            });
        }else {
            Toast.makeText(getApplicationContext(),"没有可用的SD卡",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 安装apk的操作
     */
    private void installApk() {
        /**
         * 一个应用打开另一个应用的activity采用隐式意图，需要满足activity的过滤条件
         * <intent-filter>
         <action android:name="android.intent.action.VIEW" />
         <category android:name="android.intent.category.DEFAULT" />
         <data android:scheme="content" />  content://通过内容提供者获取
         <data android:scheme="file" />  file:从文件中获取安装应用的程序
         <data android:mimeType="application/vnd.android.package-archive" />
         </intent-filter>
         */
        Intent intent = new Intent();
        /*设置安装成功后，弹出系统打开界面*/
        intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
        intent.setAction("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        //相互覆盖
		/*intent.setData(Uri.fromFile(new File(SAVEURL)));
		intent.setType("application/vnd.android.package-archive");*/
        intent.setDataAndType(Uri.fromFile(new File(ApkVersionUrl.SAVE_URL)), "application/vnd.android.package-archive");
        //startActivity(intent);
        //当跳转到的activity退出的时候，会调用当前activity的onActivityResult方法
        //requestCode :请求码，用于区别是哪个应用跳转过去的
//        startActivity(intent);
        startActivityForResult(intent, 0);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //跳转首页
        enterHome();
    }

    /**
     * 显示下载进度条对话框
     */
    private void showProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);//设置进度条的样式
        progressDialog.setCancelable(false);//设置对话框是否可以消失
        progressDialog.show();//显示对话框
    }

    /**
     * 跳转首页
     */
    private void enterHome() {
        Intent intent = new Intent(this,HomeActivity.class);
        startActivity(intent);
        //移除当前页面，防止返回键回到splash启动页
        finish();
    }

}
