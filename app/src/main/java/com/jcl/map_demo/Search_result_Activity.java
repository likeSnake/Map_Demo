package com.jcl.map_demo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.jcl.map_demo.Constant.Constant;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.MapboxMap;
import com.mapbox.maps.plugin.Plugin;
import com.mapbox.maps.plugin.animation.CameraAnimationsPlugin;
import com.mapbox.maps.plugin.animation.MapAnimationOptions;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.AnnotationType;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;

public class Search_result_Activity extends AppCompatActivity implements View.OnClickListener, LocationEngineCallback<LocationEngineResult> {

    private double latitude;// 纬度
    private double longitude;//经度
    private MapView mapView;
    private MapboxMap mapboxMap;
    private static final double DEFAULT_ZOOM_VALUE = 8.0;   // 初始地图缩放大小
    private CameraAnimationsPlugin cameraAnimationsPlugin;
    private ImageView menu_map;
    private ImageView My_position;
    private ImageView search_ic;
    //标点位置
    private Double markLongitude;
    private Double markLatitude;

    //位置更新之间的距离
    public static final float DEFAULT_DISPLACEMENT = 3.0f;
    //位置更新的最大等待时间（以毫秒为单位）。
    private static final long DEFAULT_MAX_WAIT_TIME = 5000L;
    //位置更新的最快间隔（以毫秒为单位）
    private static final long DEFAULT_FASTEST_INTERVAL = 1000L;
    //位置更新之间的默认间隔
    public static final long DEFAULT_INTERVAL = 5000L;

    private LocationEngine mLocationEngine;
    private PointAnnotationManager pointAnnotationManager;

    private AnnotationPlugin annotationPlugin;

    private LocationEngineRequest mLocationEngineRequest;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        markLongitude = Double.parseDouble(getIntent().getStringExtra("longitude"));
        markLatitude = Double.parseDouble(getIntent().getStringExtra("latitude"));
        My_position = findViewById(R.id.My_position);
        mapView = findViewById(R.id.mapview);
        mapboxMap = mapView.getMapboxMap();
        menu_map = findViewById(R.id.menu_map);
        search_ic = findViewById(R.id.search_ic);
        search_ic.setOnClickListener(this);
        menu_map.setOnClickListener(this);
        My_position.setOnClickListener(this);
        cameraAnimationsPlugin = mapView.getPlugin(Plugin.MAPBOX_CAMERA_PLUGIN_ID);
        mapboxMap.setCamera(new CameraOptions.Builder()
                .center(Point.fromLngLat(104.065948, 30.536823))
                //.center(Point.fromLngLat(DEFAULT_LOCATION_LONGITUDE, DEFAULT_LOCATION_LATITUDE))
                .zoom(DEFAULT_ZOOM_VALUE)
                .build()
        );
        mapboxMap.loadStyleUri(Constant.DEFAULT_MAP_STYLE[0], style -> {
        });
        moveCameraTo(Point.fromLngLat(markLongitude,markLatitude),16.0,1000);

        LocationComponentPlugin locationPlugin = mapView.getPlugin(Plugin.MAPBOX_LOCATION_COMPONENT_PLUGIN_ID);
        //地图标记插件
        annotationPlugin = mapView.getPlugin(Plugin.MAPBOX_ANNOTATION_PLUGIN_ID);
        // 注册点标记管理器
        pointAnnotationManager = (PointAnnotationManager)
                annotationPlugin .createAnnotationManager(AnnotationType.PointAnnotation, null);


        locationPlugin.updateSettings(locationComponentSettings -> {
            locationComponentSettings.setEnabled(true);
            locationComponentSettings.setPulsingEnabled(true);  // 脉冲效果
            return null;
        });
        mLocationEngineRequest= new LocationEngineRequest.Builder(DEFAULT_INTERVAL)
                //要求最准确的位置
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                //请求经过电池优化的粗略位置
//            .setPriority(LocationEngineRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                //要求粗略〜10 km的准确位置
//            .setPriority(LocationEngineRequest.PRIORITY_LOW_POWER)
                //被动位置：除非其他客户端请求位置更新，否则不会返回任何位置
//            .setPriority(LocationEngineRequest.PRIORITY_NO_POWER)
                //设置位置更新之间的距离
                .setDisplacement(DEFAULT_DISPLACEMENT)
                //设置位置更新的最大等待时间（以毫秒为单位）。
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME)
                //设置位置更新的最快间隔（以毫秒为单位）
                .setFastestInterval(DEFAULT_FASTEST_INTERVAL)
                .build();

        addPointAnnotationInMap(markLongitude,markLatitude);
    }

    /**
     * 在地图中添加Point类型标记
     * @param longitude 添加坐标X
     * @param latitude  添加坐标Y
     */
    public void addPointAnnotationInMap(double longitude, double latitude) {
        if(annotationPlugin == null){
            return;
        }
        if (pointAnnotationManager == null) {
            pointAnnotationManager = (PointAnnotationManager)
                    annotationPlugin .createAnnotationManager(AnnotationType.PointAnnotation, null);
        }
        // Set options for the resulting symbol layer.
        Resources res = getResources();
        Bitmap bmp = BitmapFactory.decodeResource(res,R.mipmap.ic_red_marker);
        PointAnnotationOptions pointAnnotationOptions = new PointAnnotationOptions()
                .withPoint(Point.fromLngLat(longitude, latitude))
                .withIconImage(bmp);
        // Add the pointAnnotation to the map.
        pointAnnotationManager .create(pointAnnotationOptions);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLocationEngine = LocationEngineProvider.getBestLocationEngine(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
            return;
        }
        mLocationEngine.requestLocationUpdates(mLocationEngineRequest, this, Looper.getMainLooper());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mLocationEngine != null) {
            mLocationEngine.removeLocationUpdates(this);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.menu_map:
                System.out.println("选择地图类型");
                showPopupMenu(v);
                break;
            case R.id.My_position:
                // 定位到当前位置
                System.out.println("请求位置");
                System.out.println("缩放比例测试：");

                moveCameraTo(Point.fromLngLat(longitude,latitude),16.0,1000);
                break;
        }
    }


    //弹出按钮框
    private void showPopupMenu(final View view) {
        final PopupMenu popupMenu = new PopupMenu(this,view);
        //menu 布局
        popupMenu.getMenuInflater().inflate(R.menu.map_type,popupMenu.getMenu());
        //点击事件
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.map_1:
                        mapboxMap.loadStyleUri(Constant.DEFAULT_MAP_STYLE[0], style -> {});
                        break;
                    case R.id.map_2:
                        mapboxMap.loadStyleUri(Constant.DEFAULT_MAP_STYLE[1], style -> {});
                        break;
                    case R.id.map_3:
                        mapboxMap.loadStyleUri(Constant.DEFAULT_MAP_STYLE[4], style -> {});
                        break;
                    case R.id.map_4:
                        mapboxMap.loadStyleUri(Constant.DEFAULT_MAP_STYLE[5], style -> {});
                        break;
                    case R.id.map_5:
                        mapboxMap.loadStyleUri(Constant.DEFAULT_MAP_STYLE[2], style -> {});
                        break;
                    case R.id.map_6:
                        mapboxMap.loadStyleUri(Constant.DEFAULT_MAP_STYLE[3], style -> {});
                        break;
                    case R.id.map_7:
                        mapboxMap.loadStyleUri(Constant.DEFAULT_MAP_STYLE[6], style -> {});
                        break;
                    case R.id.map_8:
                        mapboxMap.loadStyleUri(Constant.DEFAULT_MAP_STYLE[7], style -> {});
                        break;
                }
                return false;
            }
        });
        //关闭事件
        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
            }
        });
        //显示菜单，不要少了这一步
        popupMenu.show();
    }

    /**
     * 将摄像头移动到指定位置
     * @param point 目标坐标
     * @param zoom  目标缩放比例
     * @param duration 滑动总时间 0为无动画
     */
    public void moveCameraTo(Point point, double zoom, int duration) {
        if (mapView == null) {
            return;
        }
        if (duration != 0 && cameraAnimationsPlugin != null) {
            cameraAnimationsPlugin.flyTo(new CameraOptions.Builder()
                            .center(point)
                            .zoom(zoom)
                            .build(),
                    new MapAnimationOptions.Builder().duration(duration).build());
        } else {
            mapboxMap.setCamera(new CameraOptions.Builder()
                    .center(point)
                    .zoom(zoom)
                    .build());
        }
    }


    //implements LocationEngineCallback<LocationEngineResult> 位置结果回调
    @Override
    public void onSuccess(LocationEngineResult result) {
        Location lastLocation = result.getLastLocation();
        System.out.println("位置结果回调");
        if (lastLocation != null) {
            latitude = lastLocation.getLatitude();//维度
            longitude = lastLocation.getLongitude();//经度
            //  Toast.makeText(this, "onSuccess LatLng: " + latitude + "," + longitude, Toast.LENGTH_SHORT).show();
            System.out.println(latitude+" "+longitude);

           // moveCameraTo(Point.fromLngLat(longitude,latitude),16.0,1000);
            /*mapboxMap.setCamera(new CameraOptions.Builder()
                    .center(Point.fromLngLat(longitude, latitude))
                    //.center(Point.fromLngLat(DEFAULT_LOCATION_LONGITUDE, DEFAULT_LOCATION_LATITUDE))
                    .zoom(16.0)
                    .build()
            );*/
        }

    }

    @Override
    public void onFailure(@NonNull Exception exception) {
        Toast.makeText(this, "onFailure : " + exception.getMessage(), Toast.LENGTH_SHORT).show();
    }
}