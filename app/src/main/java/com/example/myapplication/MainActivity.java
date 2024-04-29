package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    TextView locationInfo;
//    定位的对象
    LocationClient mLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//问题星级：@@@ 百度定位api隐私报错问题
        LocationClient.setAgreePrivacy(true);

        locationInfo = (TextView) findViewById(R.id.locationInfo);
//        初始化locationClient对象
        try {
            mLocationClient = new LocationClient(getApplicationContext());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //注册定位监听器
        mLocationClient.registerLocationListener(new MylocationListener());
        //封装请求定位方法
        requestLocation();

        //百度地图服务端动态发送权限申请
        List<String> permissionList = new ArrayList<String>();
//        获取定位权限
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        //问题星级：@@ 因为manifest中最开始没有设置READ_PHONE_STATE权限，导致权限无法获取，从而使得onRequestPermissionsResult中一直执行【必须同意所有权限才能使用本程序】的方法，
        // 弹出提示后程序关闭实际上是执行了后面的finish（）方法。
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
//        获取外部存储权限
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
//
        if (!permissionList.isEmpty()){
            String [] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }else {
            requestLocation();
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }

                    }
                    requestLocation();
                    Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(this,"发生未知错误",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    //         请求位置监听
    private void requestLocation() {
        //初始化当前location参数
        initLocation();
        mLocationClient.start();//开启实时监听location
    }

    private void initLocation() {
//        设定监听地理位置参数  location Client Option位置 客户 选项
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选设置定位模式，默认高精度
        //LocationMode.Hight_Accuracy 高精度
        //LocationMode.Battery_Saving 低功耗
        //LocationMode.Device_Sensors 仅使用设备




        option.setCoorType("bd09ll");
        //可选，设置返回经纬度坐标类型，默认 GcJ02
        //GCJ02 国测局坐标
        //BD09ll 百度经纬度坐标
        //海外地区定位，无需设置坐标类型，统一返回WGS84类型坐标

        option.setScanSpan(1000);
        //可选，设置发起定位请求的间隔，int类型，单位ms
        //如果设置为0则代表单次定位，及仅定位一次，默认为0
        //如果设置非0，则需设置1000以上才有效

        option.setOpenGnss(true);
        //option.setOpenGnss(true);
        //可选，是否使用GPS ，默认false
        //使用高精度和仅用设备这两种定位模式时必须设置为true

        option.setLocationNotify(true);
        //可选，设置是否当GPS有效时按照1s/1次的频率输出GPS结果 ，默认false

        option.setIgnoreKillProcess(false);
        //可选，定位SDK内部是一个services，并放到了独立的进程里
        //设置是否在stop的时候杀死这个进程，默认（建议）不杀死。即setInoreKillprocess(true)

        /*option.setIgnorCacheException(false)；
        可选，设置是否搜集cache信息，默认搜集，即参数为false*/

        option.setWifiCacheTimeOut(5 * 60 * 1000);
        //可选 V7，2版本新增能力
        //如果设置了该接口，首次启动定位时，会先判断当前WiFi是否超出有效期，若超出有效期，会先重新扫描WiFi，然后定位

        option.setEnableSimulateGnss(false);
        //可选， 设置是否需要过滤gnss(GPS）仿真结果，默认需要，即参数为false

        option.setIsNeedAddress(true);
        //必选，显示用户地理信息，


        mLocationClient.setLocOption(option);
        //应用所有设置的Option参数。


    }

    //    设置实时定位监听器
    private  class  MylocationListener extends BDAbstractLocationListener{

    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        //bdLocation对象包含了经纬度等各种定位信息
        StringBuilder currentPosition = new StringBuilder();
        currentPosition.append("纬度：").append(bdLocation.getLatitude()).append("\n");
        currentPosition.append("经度：").append(bdLocation.getLongitude()).append("\n");
        currentPosition.append("国家：").append(bdLocation.getCountry()).append("\n");
        currentPosition.append("省：").append(bdLocation.getProvince()).append("\n");
        currentPosition.append("市：").append(bdLocation.getCity()).append("\n");
        currentPosition.append("区：").append(bdLocation.getDistrict()).append("\n");
        currentPosition.append("村镇").append(bdLocation.getTown()).append("\n");
        currentPosition.append("街道").append(bdLocation.getStreet()).append("\n");
        currentPosition.append("地址").append(bdLocation.getAddrStr()).append("\n");
        currentPosition.append("定位方式");

        //bdLocation.getLocType() 获取定位类型
        if (bdLocation.getLocType() == BDLocation.TypeCacheLocation){
            currentPosition.append("GPS");
        }else if (bdLocation.getLocType() == BDLocation.TypeNetWorkLocation){
            currentPosition.append("网络");
        }
        locationInfo.setText(currentPosition);



    }
}
}