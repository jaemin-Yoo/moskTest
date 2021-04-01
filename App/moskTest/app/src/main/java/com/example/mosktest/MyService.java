package com.example.mosktest;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MyService extends Service {

    public static Intent serviceIntent;
    public String TAG = "Log";

    //Service
    private Thread mThread;

    //SQLite
    SQLiteDatabase locationDB;
    private final String dbname = "Mosk";
    private final String tablename = "location";
    private final String tablehome = "place";

    //GPS
    private String preTime;
    private double Latitude, Longitude;
    private double pre_lat = 0.0, pre_lng = 0.0;
    private double Lat_h = 0.0, Long_h = 0.0;
    private LocationManager lm;
    private Location location;
    private long timer, past, now = 0;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        serviceIntent = intent;

        initializeNotification();

        if (mThread == null) {
            locationDB = this.openOrCreateDatabase(dbname, MODE_PRIVATE, null);

            lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Permission
            }
            location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location == null){
                location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (location != null){
                Latitude = location.getLatitude();
                Longitude = location.getLongitude();

                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 50, gpsLocationListener); //Location Update (1분마다 30m거리 이동 시)
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 50, gpsLocationListener);
            }
        }
        return START_STICKY;
    }

    final LocationListener gpsLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            Latitude = location.getLatitude();
            Longitude = location.getLongitude();
            Log.d(TAG, "Update: "+Latitude+" "+Longitude);

            now = System.currentTimeMillis();
            if (past != 0){
                timer = (now - past)/1000; // 타이머 계산
                Log.d(TAG, "Timer: "+timer);
            }

            if (timer > 300) {
                //5분이상 장소에 머물렀을 시 위치저장
                Cursor cursor_h = locationDB.rawQuery("SELECT * FROM "+tablehome, null);
                if (cursor_h.getCount() != 0){
                    while(cursor_h.moveToNext()) {
                        Lat_h = cursor_h.getDouble(0);
                        Long_h = cursor_h.getDouble(1);

                        if (getDistance(Lat_h, Long_h, pre_lat, pre_lng) > 50){
                            locationDB.execSQL("INSERT INTO "+tablename+"(preTime, Latitude, Longitude) VALUES('"+preTime+"', "+pre_lat+", "+pre_lng+")");
                            Log.d(TAG, "위치저장!");
                        }
                    }
                } else{
                    locationDB.execSQL("INSERT INTO "+tablename+"(preTime, Latitude, Longitude) VALUES('"+preTime+"', "+pre_lat+", "+pre_lng+")");
                    Log.d(TAG, "위치저장!");
                }
            }

            //움직이고 있을 때 위치업데이트
            Date mDate = new Date(now); // 위치업데이트 시간 구하기
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            preTime = simpleDateFormat.format(mDate);

            past = System.currentTimeMillis();
            pre_lat = Latitude;
            pre_lng = Longitude;

            Log.d(TAG, "위치업데이트!");
            Log.d(TAG, "위치업데이트 시간 : "+preTime);

            // DB 데이터 확인
            Cursor cursor = locationDB.rawQuery("SELECT * FROM "+tablename, null);
            while(cursor.moveToNext()){
                String pretime = cursor.getString(0);
                String curtime = cursor.getString(1);
                double Lat = cursor.getDouble(2);
                double Long = cursor.getDouble(3);
                Log.d(TAG,"저장된 데이터: "+pretime+" "+curtime+" "+Lat+" "+Long);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(@NonNull String provider) {}

        @Override
        public void onProviderDisabled(@NonNull String provider) {}
    };

    public double getDistance(double pre_lat, double pre_lng, double aft_lat, double aft_lng) {
        double distance = 0;
        Location locationA = new Location("A");
        locationA.setLatitude(pre_lat);
        locationA.setLongitude(pre_lng);

        Location locationB = new Location("B");
        locationB.setLatitude(aft_lat);
        locationB.setLongitude(aft_lng);

        distance = locationA.distanceTo(locationB);
        return distance; // m 단위
    }

    public void initializeNotification(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1");
        builder.setSmallIcon(R.mipmap.ic_launcher);
        NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
        style.bigText("설정을 보려면 누르세요.");
        style.setBigContentTitle(null);
        style.setSummaryText("서비스 동작중");
        builder.setContentText("동선 저장 중..");
        builder.setContentTitle("Mosk");
        builder.setOngoing(true);
        builder.setStyle(style);
        builder.setWhen(0);
        builder.setShowWhen(false);
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        builder.setContentIntent(pendingIntent);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(new NotificationChannel("1", "undead_service", NotificationManager.IMPORTANCE_NONE));
        }
        Notification notification = builder.build();
        startForeground(1, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("TAG", "onDestory");
        Toast.makeText(this, "onDestory!", Toast.LENGTH_SHORT).show();

        if (serviceIntent != null){
            setAlarmTimer();
        }

        if (mThread != null){
            mThread.interrupt();
            mThread = null;
        }

        lm.removeUpdates(gpsLocationListener);
        locationDB.close();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        setAlarmTimer();
    }

    protected void setAlarmTimer(){
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.add(Calendar.SECOND, 1);
        Intent intent = new Intent(this, AlarmRecever.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0,intent,0);

        AlarmManager mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), sender);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
