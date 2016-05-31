package com.guoguoquan.www.guoguoquan;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.AMapUtils;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.maps2d.model.PolylineOptions;
import com.guoguoquan.www.guoguoquan.view.RevealLayout;
import com.guoguoquan.www.guoguoquan.view.StopwatchView;


public class MainActivity extends FragmentActivity implements LocationSource, AMapLocationListener, View.OnClickListener {

    private TextView tv_start_or_end,tv_distance;
    private ImageView iv_map1, iv_map2;
    private Boolean isStart = false;
    private StopwatchView stopwatch;

    private RevealLayout rl_reveallayout1, rl_reveallayout2;

    private AMap aMap;
    private MapView mapView;
    private LocationSource.OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    private LatLng oldeLatLng;
    private UiSettings mUiSettings;


    private int time = 0;
    private Boolean canGo = false;
    private Boolean startGo = false;
    private float distence;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_layout);
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        initView();
        init();
    }


    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
            mUiSettings = aMap.getUiSettings();
            setUpMap();
        }
    }

    private void initView() {
        tv_start_or_end = (TextView) findViewById(R.id.tv_start_or_end);
        stopwatch = (StopwatchView) findViewById(R.id.stopwatch);
        iv_map1 = (ImageView) findViewById(R.id.iv_map1);
        iv_map2 = (ImageView) findViewById(R.id.iv_map2);
        tv_distance= (TextView) findViewById(R.id.tv_distance);

        rl_reveallayout1 = (RevealLayout) findViewById(R.id.rl_reveallayout1);
        rl_reveallayout2 = (RevealLayout) findViewById(R.id.rl_reveallayout2);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        stopwatch.setMaxHeight(metrics.heightPixels * 5 / 8);
        stopwatch.setMinHeight(metrics.heightPixels * 1 / 8);
        tv_start_or_end.setOnClickListener(this);
        iv_map1.setOnClickListener(this);
        iv_map2.setOnClickListener(this);
    }


    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {

        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.mipmap.iv_start));// 设置小蓝点的图标
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(40.0038752316,116.4183201240), 16, 0, 30)));

        mUiSettings.setZoomControlsEnabled(false);
        mUiSettings.setMyLocationButtonEnabled(false);
    }


    /**
     * 定位成功后回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (mListener != null && amapLocation != null) {
            if (amapLocation != null && amapLocation.getErrorCode() == 0) {
                if (canGo == false)
                    canGo = true;
                if (startGo) {
                    if (time > 0) {
                        aMap.addPolyline((new PolylineOptions()).add(oldeLatLng, new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude())).color(Color.RED));
                        oldeLatLng = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());

                        distence+=AMapUtils.calculateLineDistance(oldeLatLng, new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude()));

                        tv_distance.setText(String.valueOf(distence)+" m");

                        aMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude()), 16, 0, 30)));
                    } else {
                        time++;
                        oldeLatLng = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
                        aMap.addMarker(new MarkerOptions().position(oldeLatLng).icon(BitmapDescriptorFactory.fromResource(R.mipmap.iv_start)));

                    }
                }

            } else {
                String errText = "定位失败," + amapLocation.getErrorCode() + ": " + amapLocation.getErrorInfo();
                Log.e("AmapErr", errText);
            }
        }
    }

    /**
     * 激活定位
     */
    @Override
    public void activate(LocationSource.OnLocationChangedListener listener) {
        mListener = listener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            mlocationClient.setLocationListener(this);
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            mlocationClient.setLocationOption(mLocationOption);
            mlocationClient.startLocation();
        }
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_start_or_end:
                if (!isStart) {
                    if(canGo==true)
                    {
                        startGo=true;
                        tv_start_or_end.setBackground(getDrawable(R.mipmap.bg_tv_end));
                        tv_start_or_end.setText("EDN");
                        isStart = true;
                        stopwatch.reset();
                        stopwatch.start();
                        Toast.makeText(MainActivity.this,"开始跑步",Toast.LENGTH_LONG).show();
                    }

                } else {
                    startGo=false;
                    tv_start_or_end.setBackground(getDrawable(R.mipmap.bg_tv_start));
                    tv_start_or_end.setText("START");
                    isStart = false;
                    stopwatch.pause();
                }
                break;
            case R.id.iv_map1:
                rl_reveallayout2.setVisibility(View.GONE);
                rl_reveallayout1.setContentShown(false);
                int x = (int) iv_map1.getX() + iv_map1.getWidth() / 2;
                int y = (int) iv_map1.getY() + iv_map1.getWidth() / 2;
                rl_reveallayout1.show(x, y, 1000);
                break;
            case R.id.iv_map2:
                rl_reveallayout2.setVisibility(View.VISIBLE);
                rl_reveallayout2.setContentShown(false);
                int x1 = (int) iv_map2.getX() + iv_map2.getWidth() / 2;
                int y1 = (int) iv_map2.getY() + iv_map2.getWidth() / 2;
                rl_reveallayout2.show(x1, y1, 1000);
                break;
        }
    }


    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}
