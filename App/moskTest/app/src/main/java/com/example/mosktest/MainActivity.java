package com.example.mosktest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private String TAG = "Log";

    //Layout
    private Button path, delete, hdelete, send, news, netstart, netstop;

    //SQLite
    SQLiteDatabase locationDB = null;
    private final String dbname = "Mosk";
    private final String tablename = "location";
    private final String tablehome = "place";

    //Socket
    private String data = "";

    //NetworkService
    NetworkService networkService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Glide
        back=findViewById(R.id.btn_back);
        Glide.with(this).load("https://i.imgur.com/iYM8Gc1.png").into(back); // 이미지 로드
        */

        //Create DB, Table
        locationDB = this.openOrCreateDatabase(dbname, MODE_PRIVATE, null);
        locationDB.execSQL("CREATE TABLE IF NOT EXISTS "+tablename
        +" (preTime datetime PRIMARY KEY, curTime datetime DEFAULT(datetime('now', 'localtime')), Latitude double NOT NULL, Longitude double NOT NULL)");

        locationDB.execSQL("CREATE TABLE IF NOT EXISTS "+tablehome
                +" (Latitude double NOT NULL, Longitude double NOT NULL, PRIMARY KEY(Latitude, Longitude))");

        // 2주 전 위치정보 삭제
        Cursor cursor = locationDB.rawQuery("SELECT * FROM "+tablename+" WHERE curTime<datetime('now','localtime','-14 days')", null);
        if (cursor.getCount() != 0){
            locationDB.execSQL("DELETE FROM "+tablename+" WHERE curTime<datetime('now','localtime','-14 days')");
            Toast.makeText(this, "2주 전 위치정보가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
        }

        path = findViewById(R.id.btn_path);
        path.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,PathActivity.class);
                startActivity(intent);
            }
        });

        news = findViewById(R.id.btn_news);
        news.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NewsActivity.class);
                startActivity(intent);
            }
        });

        delete = findViewById(R.id.btn_delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationDB.execSQL("DELETE FROM "+tablename); // 추후 삭제해야되는 코드
                Toast.makeText(getApplicationContext(), "Delete", Toast.LENGTH_LONG).show();
            }
        });

        hdelete = findViewById(R.id.btn_hdelete);
        hdelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationDB.execSQL("DELETE FROM "+tablehome); // 추후 삭제해야되는 코드
                Toast.makeText(getApplicationContext(), "Delete", Toast.LENGTH_LONG).show();
            }
        });

        send = findViewById(R.id.btn_send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MyService.serviceIntent!=null){
                    if (MyService.networKWriter!=null){
                        Cursor cursor = locationDB.rawQuery("SELECT * FROM "+tablename, null);
                        while(cursor.moveToNext()){
                            String pretime = cursor.getString(0);
                            String curtime = cursor.getString(1);
                            double Lat = cursor.getDouble(2);
                            double Long = cursor.getDouble(3);

                            if (curtime != null){
                                // 동선 저장 중인 위치는 전송 x
                                PrintWriter out = new PrintWriter(MyService.networKWriter, true);
                                data = pretime+"/"+curtime+"/"+Lat+"/"+Long;
                                out.println(data);
                                Log.d(TAG,"전송된 데이터: "+data);
                            }
                        }

                        if (data==""){
                            Toast.makeText(MainActivity.this, "전송 할 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                        } else{
                            Toast.makeText(MainActivity.this, "데이터를 전송하였습니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else{
                        Toast.makeText(MainActivity.this, "서버 상태를 확인하세요.", Toast.LENGTH_SHORT).show();
                    }
                } else{
                    Toast.makeText(MainActivity.this, "백그라운드를 실행 해 주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        netstart = findViewById(R.id.btn_net_start);
        netstart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (networkService == null){
                    Intent intent = new Intent(MainActivity.this, NetworkService.class);
                    startService(intent);
                }
            }
        });

        netstop = findViewById(R.id.btn_net_stop);
        netstop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NetworkService.class);
                stopService(intent);
            }
        });
    }

}