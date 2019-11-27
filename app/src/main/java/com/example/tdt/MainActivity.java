package com.example.tdt;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.tianditu.android.maps.GeoPoint;
import com.tianditu.android.maps.MapView;
import com.tianditu.android.maps.MyLocationOverlay;
import com.tianditu.maps.GeoPointEx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private MapView mapView; // 天地图控件
    private MyLocationOverlay locationOverlay; // 定位覆盖物

    /**
     * 需要进行检测的权限集合
     * Key：权限
     * Value：说明
     */
    private static final HashMap<String, String> needPermissionsMap = new HashMap<String, String>() {
        {
            put(Manifest.permission.ACCESS_NETWORK_STATE, "网络状态");
            put(Manifest.permission.ACCESS_WIFI_STATE, "WIFI");
            put(Manifest.permission.INTERNET, "网络");
            put(Manifest.permission.CALL_PHONE, "拨打电话");
            put(Manifest.permission.ACCESS_COARSE_LOCATION, "粗略位置");
            put(Manifest.permission.READ_PHONE_STATE, "电话状态");
            put(Manifest.permission.WRITE_EXTERNAL_STORAGE, "写入存储");
        }
    };
    private static final int PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions(this);

        mapView = findViewById(R.id.map_view);

        // 设置LOGO位置为右下角
        mapView.setLogoPos(MapView.LOGO_RIGHT_BOTTOM);

        // 设置默认地图类型
        mapView.setMapType(MapView.TMapType.MAP_TYPE_VEC);

        // 设置在缩放动画过程中绘制overlay，默认为不绘制
        //mapView.setDrawOverlayWhenZooming(true);

        // 设置地图默认缩放级别
        mapView.getController().setZoom(12);

        locationOverlay = new MyLocationOverlay(this, mapView);
        mapView.addOverlay(locationOverlay);

        locationOverlay.enableMyLocation(); // 启用我的位置
        locationOverlay.setGpsFollow(true); // 设置跟随状态

        GeoPoint geoPoint = locationOverlay.getMyLocation();
        if (geoPoint == null) {
            double longitude = 116.3919236741;
            double latitude = 39.9057789520;

            geoPoint = GeoPointEx.Double2GeoPoint(longitude, latitude);
        }

        mapView.getController().setCenter(geoPoint); // 设置地图中心点
    }

    // 销毁Activity时被调用
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (locationOverlay != null) {
            locationOverlay.disableMyLocation(); // 禁用我的位置
            mapView.removeOverlay(locationOverlay);
        }
    }

    // 检测获取权限
    private void checkPermissions(Context context) {
        List<String> requestPermissionsList = new ArrayList<>();

        for (Map.Entry<String, String> entry : needPermissionsMap.entrySet()) {
            String permission = entry.getKey();

            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // 没有授权的权限
                requestPermissionsList.add(permission);
            }
        }

        if (!requestPermissionsList.isEmpty()) {
            // 申请未授权的权限
            ActivityCompat.requestPermissions(this, requestPermissionsList.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                for (int i = 0; i < grantResults.length; i++) {
                    // grantResults数组存储的申请的返回结果，
                    // PERMISSION_GRANTED 表示申请成功
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        // 授权成功
                    } else {
                        // 授权失败

                        String permissionText = needPermissionsMap.get(permissions[i]);
                        Log.e(TAG, "APP权限授权失败：" + permissions[i] + "(" + permissionText + ")");

                        // 提示用户获取权限失败，程序退出
                        final AlertDialog.Builder normalDialog = new AlertDialog.Builder(this);
                        normalDialog.setTitle("警告");
                        normalDialog.setMessage("APP获取" + permissionText + "权限失败，程序即将退出！");
                        normalDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
                        normalDialog.setCancelable(false); // 点击对话框外不消失
                        normalDialog.show();
                    }
                }
                break;
            default:
                break;
        }
    }
}
