package com.example.chen.yuankong;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

/**
 * Created by Chen on 2016/5/10.
 */
public class ConnectionChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifiNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (!mobNetInfo.isConnected() && !wifiNetInfo.isConnected()) {
            Log.v("hello", "网络不可用");
            //改变背景或者 处理网络的全局变量
            App.mNetWorkState = false;
        } else if (mobNetInfo.isConnected()) {
            Log.v("hello", "手机网络可用");
            App.mNetWorkState = true;
            App.MOBILE_CONNECT = true;
            //改变背景或者 处理网络的全局变量
        } else if (wifiNetInfo.isConnected()) {
            Log.v("hello", "WIFI网络可用");
            App.mNetWorkState = true;
            App.WIFI_CONNECT = true;
        }
        Postnet();
    }

    private void Postnet() {
        AsyncHttpClient client = new AsyncHttpClient();
        String url = App.host + "/home/index/setxy";

        RequestParams params = new RequestParams();
        if (App.WIFI_CONNECT) {
            params.put("wifi", 1);
            params.put("Name", App.Name);
        }
        if (App.MOBILE_CONNECT) {
            params.put("mobile", 1);
            params.put("Name", App.Name);
        }
        if (!App.mNetWorkState) {
            params.put("wifi", 0);
            params.put("mobile", 0);
            params.put("Name", App.Name);
        }
        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] bytes) {

                if (statusCode == 200) {
                    Log.v("putxy", new String(bytes));
                } else
                    Log.v("putxy", new String(bytes));
            }

            @Override
            public void onFailure(int StatusCode, Header[] headers, byte[] bytes, Throwable throwable) {
                Log.v("putxy", "failure");
                throwable.printStackTrace();
            }
        });
    }
}
