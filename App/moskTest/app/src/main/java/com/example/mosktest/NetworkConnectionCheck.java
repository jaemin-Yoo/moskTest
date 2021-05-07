package com.example.mosktest;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)    //LOLLIPOP 버전 이상에서 동작
public class NetworkConnectionCheck extends ConnectivityManager.NetworkCallback {   // 네트워크 변경에 대한 알림에 사용되는 Callback Class

    private Context context;
    private NetworkRequest networkRequest;
    private ConnectivityManager connectivityManager;

    public NetworkConnectionCheck(Context context){
        this.context=context;
        networkRequest =
                new NetworkRequest.Builder()                                        // addTransportType : 주어진 전송 요구 사항을 빌더에 추가
                        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)   // TRANSPORT_CELLULAR : 이 네트워크가 셀룰러 전송을 사용함을 나타냅니다.
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)       // TRANSPORT_WIFI : 이 네트워크가 Wi-Fi 전송을 사용함을 나타냅니다.
                        .build();
        this.connectivityManager = (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE); // CONNECTIVITY_SERVICE : 네트워크 연결 관리 처리를 검색
    }

    public void register() { this.connectivityManager.registerNetworkCallback(networkRequest, this);}

    public void unregister() {
        this.connectivityManager.unregisterNetworkCallback(this);
    }

    @Override
    public void onAvailable(@NonNull Network network) {
        super.onAvailable(network);

        // 네트워크가 연결되었을 때 할 동작
        Toast.makeText(this.context, "network available", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLost(@NonNull Network network) {
        super.onLost(network);

        // 네트워크 연결이 끊겼을 때 할 동작
        Toast.makeText(this.context, "network lost", Toast.LENGTH_SHORT).show();
    }


}