package com.example.android.datafrominternet.LBS;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.example.android.datafrominternet.Notebook.AddNote;
import com.example.android.datafrominternet.Notebook.NoteData;
import com.example.android.datafrominternet.R;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobGeoPoint;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * Created by ucla on 2018/2/14.
 */

public class Map extends AppCompatActivity {

    public LocationClient mLocationClient = null;

    private MyLocationListener myListener = new MyLocationListener();

    private MapView mapView;

    private BaiduMap baiduMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(myListener);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_map);

        getPermission();

        Bmob.initialize(this, "c12ad56b69a6e30cb8cc89b566379d19");

        mapView = (MapView) findViewById(R.id.map_view);
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        baiduMap.setMyLocationEnabled(false);
        mapView.onDestroy();
    }

    private void getPermission(){
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(Map.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(Map.this,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(Map.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(Map.this,permissions, 1);
        } else {
            requestLocation();
        }
    }

    private void requestLocation() {
        initLocation();
        mLocationClient.start();
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(5000);
        option.setCoorType("bd09ll");
        option.setOpenGps(true);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        mLocationClient.setLocOption(option);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }


    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(final BDLocation location) {

            final double Lat = location.getLatitude();
            final double Lon = location.getLongitude();
            final LatLng ll = new LatLng(Lat,Lon);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    MapStatusUpdate update = MapStatusUpdateFactory.newLatLngZoom(ll,18f);
                    baiduMap.animateMapStatus(update);

                    MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
                    locationBuilder.latitude(Lat);
                    locationBuilder.longitude(Lon);
                    MyLocationData locationdata = locationBuilder.build();
                    baiduMap.setMyLocationData(locationdata);

                    BmobGeoPoint point = new BmobGeoPoint(Lat, Lon);
                    pushBmobinfo(point);
                    findNeighbor(point);

                }
            });
        }

    }

    private String query(String number){
        BmobQuery<LocationData> query = new BmobQuery<LocationData>();
        query.addWhereEqualTo("phoneNumer", number);
        query.setLimit(1);
        final String objectId;
        query.findObjects(new FindListener<LocationData>() {
            @Override
            public void done(List<LocationData> object, BmobException e) {
                if(e==null){
                    objectId = object.get(0).getObjectId();
                }else{
                    Log.i("bmob","失败："+e.getMessage()+","+e.getErrorCode());
                }
            }
        });
        return objectId;
    }

    private void save(BmobGeoPoint point, String number, String username){

        LocationData locationData = new LocationData();
        locationData.setGpsAdd(point);
        locationData.setPhoneNumber(number);
        locationData.setUserName(username);
        locationData.save(new SaveListener<String>() {
            @Override
            public void done(String objectId, BmobException e) {
                if(e!=null){
                    Log.i("bmob","失败："+e.getMessage()+","+e.getErrorCode());
                } else {
                    Toast.makeText(Map.this,"同步修改数据失败",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void findNeighbor(BmobGeoPoint point){
        BmobQuery<LocationData> bmobQuery = new BmobQuery<LocationData>();
        bmobQuery.addWhereWithinKilometers("gpsAdd",point,3.0);
        bmobQuery.setLimit(6);    //获取最接近用户地点的6条数据
        bmobQuery.findObjects(new FindListener<LocationData>() {
            @Override
            public void done(List<LocationData> object,BmobException e) {
                if(e==null){
                    Log.d("test","查询成功：共" + object.size() + "条数据。");
                }else {
                    Log.d("test","查询失败：" + e.getMessage());
                }
            }
        });
    }

    private void update(String objectId, final String number, final String username, final BmobGeoPoint point){
        LocationData locationData = new LocationData();
        locationData.setGpsAdd(point);
        locationData.update(objectId, new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if(e==null){
                    Log.i("bmob","更新成功");
                }else if (e.getErrorCode()==9006){
                    save(point,number,username);
                }else {
                    Toast.makeText(Map.this,"同步地理位置失败",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void pushBmobinfo(BmobGeoPoint point){

        SharedPreferences preferences = getSharedPreferences("userinfo",MODE_PRIVATE);
        String username = preferences.getString("username",null);
        String number = preferences.getString("phonenumber",null);
        String objectId = null;
        objectId = query(number);
        update(objectId,number,username,point);

    }




}

