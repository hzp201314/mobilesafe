package com.hzp.mobilesafe.service;

import org.json.JSONException;
import org.json.JSONObject;

import com.hzp.mobilesafe.utils.Constants;
import com.hzp.mobilesafe.utils.SharedPreferencesUtil;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

/**
 * created by hzp on 2019/5/20 14:08
 * 作者：codehan
 * 描述：GPS定位服务
 */
public class GPSService extends Service {
    private static final String TAG ="GPSService" ;
    private LocationManager locationManager;
    private MyLocationListener listener;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //定位操作
        //1.获取位置的管理者
        locationManager = (LocationManager) getSystemService( LOCATION_SERVICE );
        //2.定位实现
        listener = new MyLocationListener();
        //参数1：定位
        //参数2：最小的定位的间隔时间
        //参数3：最小的定位的间隔距离
        //参数4：定位的回调监听
        if (ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText( this,"GPS权限未打开",Toast.LENGTH_SHORT );
        }else {
            locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, listener );
        }
    }

    /**定位的回调函数**/
    private class MyLocationListener implements LocationListener{
        //当定位位置和定位时间改变的时候调用
        //location : 定位位置
        @Override
        public void onLocationChanged(Location location) {
            location.getSpeed();//获取定位的速度
            location.getTime();//定位的时间
            location.getBearing();//定位方位

            double latitude = location.getLatitude();//获取定位的维度
            double longitude = location.getLongitude();//获取定位的经度

            System.out.println("经度："+longitude+"  维度："+latitude);
            //只实现定位是没有用的，因为要是实现gps追踪，所以必须将经纬度坐标发送给安全号码，但是只发送经纬度坐标不够直观，所以最好是根据经纬度坐标获取实际的地理位置，将位置发送给安全号码
            getAddress(latitude,longitude);
        }
        //定位状态改变的时候调用的方法
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub

        }
        //定位方法可用调用的方法
        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub

        }
        //定位方式不可用调用的方法
        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub

        }

    }

    @Override
    public void onDestroy() {
        //3.当activity退出的时候，取消定位操作
        locationManager.removeUpdates(listener);//取消定位，并不会将手机中的GPS关闭，android高版本中不允许程序员通过代码开启和关闭GPS
        super.onDestroy();
    }

    /**
     * 根据经纬度坐标获取实际的地理位置
     *@param latitude
     *@param longitude
     * 2016-10-13 上午11:55:24
     */
    public void getAddress(double latitude, double longitude) {
        //http://api.jisuapi.com/geoconvert/coord2addr?lat=40.100666415433174&lng=116.36786782697178&type=google&appkey=125fc44b695f29ca
        HttpUtils httpUtils = new HttpUtils();
        //TODO 此处需要设置自己的API
        //设置请求参数
        RequestParams params = new RequestParams();
        params.addQueryStringParameter("lat", latitude+"");
        params.addQueryStringParameter("lng", longitude+"");
        params.addQueryStringParameter("type", "google");
        params.addQueryStringParameter("appkey", "125fc44b695f29ca");
        httpUtils.send(HttpMethod.GET, "http://api.jisuapi.com/geoconvert/coord2addr", params,new RequestCallBack<String>() {

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                //获取返回的实际地理位置的json数据
                String json = responseInfo.result;

                Log.d( TAG, "onSuccess: "+json );

                //解析json数据，将实际的地理位置获取出来
                processJson(json);
            }

            @Override
            public void onFailure(HttpException arg0, String arg1) {
                // TODO Auto-generated method stub

            }
        });
    }
    /**
     * 解析json数据
     *@param json
     * 2016-10-13 下午2:39:41
     */
    protected void processJson(String json) {
        /**
         * {
         "msg": "ok",
         "result": {
         "address": "中国北京市昌平区郑上路",
         "city": "北京市",
         "country": "中国",
         "description": "",
         "district": "",
         "lat": "40.100665000000006",
         "lng": "116.36786666666667",
         "province": "北京市",
         "type": "google"
         },
         "status": "0"
         }
         */
        // { : jsonObject,可以理解为bean类；"msg"：可以理解为bean类中保存数据的变量 ; [] : list集合
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONObject resultJsonObject = jsonObject.getJSONObject("result");//根据key得到对应的jsonobject
            String address = resultJsonObject.getString("address");

            Log.d( TAG, "processJson: "+address );
            //将解析出来的地址发送给安全号码就可以
            sendAddress(address);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    /**
     * 发送实际的地理位置给安全号码
     *@param address
     * 2016-10-13 下午2:49:32
     */
    private void sendAddress(String address) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage( SharedPreferencesUtil.getString(getApplicationContext(),
                Constants.SAFENUMBER, "5554"), null, address, null, null);
        //因为安全号码只需要一个地理位置就可以了，但是定位操作是会一直定位的，一直定位，只要定位成功，就会发送短信
        //所以，在发送短信成功之后，需要禁止定位操作
        this.stopSelf();//在当前服务中停止当前服务
    }
}
