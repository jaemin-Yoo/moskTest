package com.example.mosktest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private String TAG = "Log";

    //Layout
    private Button start, stop, path, delete, send;

    //SQLite
    SQLiteDatabase locationDB = null;
    private final String dbname = "Mosk";
    private final String tablename = "location";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Create DB, Table
        locationDB = this.openOrCreateDatabase(dbname, MODE_PRIVATE, null);
        locationDB.execSQL("CREATE TABLE IF NOT EXISTS "+tablename
        +" (preTime datetime PRIMARY KEY, curTime datetime DEFAULT(datetime('now', 'localtime')), Latitude double NOT NULL, Longitude double NOT NULL)");

        // 2주 전 위치정보 삭제
        Cursor cursor = locationDB.rawQuery("SELECT * FROM "+tablename+" WHERE curTime<datetime('now','localtime','-14 days')", null);
        if (cursor.getCount() != 0){
            locationDB.execSQL("DELETE FROM "+tablename+" WHERE curTime<datetime('now','localtime','-14 days')");
            Toast.makeText(this, "2주 전 위치정보가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
        }

        start = findViewById(R.id.start_service);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"start!");
                Toast.makeText(MainActivity.this, "Start", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, MyService.class);
                startService(intent);
            }
        });

        stop = findViewById(R.id.stop_service);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"stop!");
                Toast.makeText(MainActivity.this, "Stop", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, MyService.class);
                stopService(intent);
            }
        });

        path = findViewById(R.id.btn_path);
        path.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,PathActivity.class);
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

        send = findViewById(R.id.btn_send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 서버통신

                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            Log.d(TAG,"TEST!");
                            JSONObject jsonObject = new JSONObject(response);
                            String preTime = jsonObject.getString("preTime");
                            String curTime = jsonObject.getString("curTime");
                            String Latitude = jsonObject.getString("Latitude");
                            String Longitude = jsonObject.getString("Longitude");
                            Log.d(TAG, "preTime: "+preTime);
                            Log.d(TAG, "curTime: "+curTime);
                            Log.d(TAG, "Latitude: "+Latitude);
                            Log.d(TAG, "Longitude: "+Longitude);

                        } catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                };

                // SQLite 데이터 가져오기
                Cursor cursor = locationDB.rawQuery("SELECT * FROM "+tablename, null);

                String[] preTime = new String[cursor.getCount()];
                String[] curTime = new String[cursor.getCount()];
                double[] Latitude = new double[cursor.getCount()];
                double[] Longitude = new double[cursor.getCount()];
                int i = 0;

                while(cursor.moveToNext()){
                    preTime[i] = cursor.getString(0);
                    curTime[i] = cursor.getString(1);
                    Latitude[i] = cursor.getDouble(2);
                    Longitude[i] = cursor.getDouble(3);
                    i++;
                }

                AddressRequest addressRequest = new AddressRequest(preTime, curTime, Latitude, Longitude, responseListener);
                RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                queue.add(addressRequest);

                Toast.makeText(getApplicationContext(), "Send", Toast.LENGTH_LONG).show();
            }
        });
    }
}