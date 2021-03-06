package com.example.ihc.proto_odroid_new;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

/**
 * Created by ihc on 2017-03-19.
 */
public class GpsInfo extends Service implements LocationListener {

    private Activity act;
    private Context mContext;

    // 현재 GPS 사용유무
    boolean isGPSEnabled = false;

    // 네트워크 사용유무
    boolean isNetworkEnabled = false;

    // GPS 상태값
    boolean isGetLocation = false;

    Location location;
    double lat; // 위도
    double lon; // 경도

    // 최소 GPS 정보 업데이트 거리 10미터
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;

    // 최소 GPS 정보 업데이트 시간 밀리세컨이므로 1분
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;

    protected LocationManager locationManager;

    public GpsInfo(){}

    public GpsInfo(Context context){
        this.mContext = context;

    }

    /*
    * 단순 권한체크
    * 권한설정이 되어있다면 true, 아니면 false반환
    */
    public boolean checkPermission(){
        boolean result = false;

        if(mContext == null)    return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //권한이 있다면
                result = true;
            }
        }
        return result;
    }

    /*
    * 권한검사(androidM이상 필수기능) 후 권한이 없다면 요청한다.(default 다이알로그를 띄운다)
    * 네트워크와 gps 사용여부를 확인하고 불가능하게 되어있다면 설정 다이알로그를 띄운다.
    * 모두 정상적으로 설정이 되어 있다면 true 반환, 아니면 false반환
    */
    public void requestPermission(Activity activity){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                //현재 ACCESS_FIND_LOCATION 권한 획득되어있는지 체크. 만약 획득이 되지 않았다면 진입

                //권한을 요청한다.
                activity.requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 3);
            }
        }
        if(!isGetLocation(activity))
            showSettingsAlert(activity);
    }

    public Location getLocationInService(){

        try {
            Log.d("겟로케이션인서비스","호출");
            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);

            // GPS 정보 가져오기
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);
            Log.d("지피에스가능",String.valueOf(isGPSEnabled));

            // 현재 네트워크 상태 값 알아오기
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            Log.d("네트워크가능",String.valueOf(isNetworkEnabled));

            if (!isGPSEnabled && !isNetworkEnabled) {
                // GPS 와 네트워크사용이 가능하지 않을때 소스 구현
                Log.d("지피에스,네트워크사용","불가능");
            } else {
                Log.d("지피에스,네트워크사용","가능");
                this.isGetLocation = true;
                // 네트워크 정보로 부터 위치값 가져오기
                if (isNetworkEnabled) {
                    Log.d("네트워크확인","네트워크확인");

                    //권한체크
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(mContext,android.Manifest.permission.ACCESS_FINE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED) {
                            Log.d("권한 없다","권한없다");
                        }else{
                            Log.d("위치가져오기","위치가져오기");
                            locationManager.requestLocationUpdates(
                                    LocationManager.NETWORK_PROVIDER,
                                    MIN_TIME_BW_UPDATES,
                                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                            if (locationManager != null) {
                                location = locationManager
                                        .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                                if (location != null) {
                                    // 위도 경도 저장
                                    lat = location.getLatitude();
                                    lon = location.getLongitude();
                                }
                            }
                            if (isGPSEnabled) {
                                if (location == null) {
                                    locationManager.requestLocationUpdates(
                                            LocationManager.GPS_PROVIDER,
                                            MIN_TIME_BW_UPDATES,
                                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                                    if (locationManager != null) {
                                        location = locationManager
                                                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                        if (location != null) {
                                            lat = location.getLatitude();
                                            lon = location.getLongitude();
                                        }
                                    }
                                }
                            }
                        }
                    }else{
                        //안드로이드 6.0이하버젼일때
                        Log.d("위치가져오기","위치가져오기");
                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            if (location != null) {
                                // 위도 경도 저장
                                lat = location.getLatitude();
                                lon = location.getLongitude();
                            }
                        }
                        if (isGPSEnabled) {
                            if (location == null) {
                                locationManager.requestLocationUpdates(
                                        LocationManager.GPS_PROVIDER,
                                        MIN_TIME_BW_UPDATES,
                                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                                if (locationManager != null) {
                                    location = locationManager
                                            .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                    if (location != null) {
                                        lat = location.getLatitude();
                                        lon = location.getLongitude();
                                    }
                                }
                            }
                        }
                    }
                }


            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if(location == null)
            Log.d("로케이션","널");
        return location;
    }


    /**
     * GPS 종료
     * */
    public void stopUsingGpsInService(){
        if(mContext == null)    return;
        if(locationManager != null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(mContext,android.Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    //현재 ACCESS_FIND_LOCATION 권한 획득되어있는지 체크. 만약 획득이 되지 않았다면 진입
                }else {
                    Log.d("지피에스서비스중지","중지");
                    locationManager.removeUpdates(GpsInfo.this);
                }
            }
        }
    }


    /**
     * GPS 나 wife 정보가 켜져있는지 확인합니다.
     * */
    public boolean isGetLocation(Activity activity) {
        boolean result = false;

        Log.d("겟로케이션인서비스","호출");
        locationManager = (LocationManager) activity
                .getSystemService(LOCATION_SERVICE);

        // GPS 정보 가져오기
        isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        Log.d("지피에스가능",String.valueOf(isGPSEnabled));

        // 현재 네트워크 상태 값 알아오기
        isNetworkEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        Log.d("네트워크가능",String.valueOf(isNetworkEnabled));

        //gps,네트웤 둘다 이용 불가능하다면 설정 다이알로그 띄운다.
        if(isGPSEnabled && isNetworkEnabled)
            result = true;

        return result;
    }

    /**
     * GPS 정보를 가져오지 못했을때
     * 설정값으로 갈지 물어보는 alert 창
     * */
    private void showSettingsAlert(final Activity activity){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);

        alertDialog.setTitle("GPS 사용유무셋팅");
        alertDialog.setMessage("GPS 셋팅이 되지 않았을수도 있습니다.\n 설정창으로 가시겠습니까?");

        // OK 를 누르게 되면 설정창으로 이동합니다.
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        activity.startActivity(intent);
                    }
                });
        // Cancle 하면 종료 합니다.
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void onLocationChanged(Location location) {
        // TODO Auto-generated method stub

    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }
}
