package com.example.chen.yuankong.Application;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.igexin.sdk.PushManager;
import com.loopj.android.http.AsyncHttpClient;
import java.util.List;

/**
 * Created by Chen on 2016/5/10.
 */
public class App extends Application {
    private static Application mApplication;
    public static boolean mNetWorkState;
    public static String host;
    public static String Name;
    public static boolean WIFI_CONNECT;
    public static boolean MOBILE_CONNECT;
    public static String cid;
    public static AsyncHttpClient httpClient;
    public static String deviceId;
    public static boolean fromweb;

    public static  List<String> treename;
    public static List<String> treestatic;

    public static synchronized Application getInstance() {
        return mApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
        initData();
    }
    public void initData() {
        mNetWorkState = false;
        host = "http://www.cugyk.top";//127.0.0.1";//172.30.3.196";
        Name = null;
        WIFI_CONNECT = false;
        MOBILE_CONNECT = false;
        cid = "";//"f199731d8785495db61e713c5ac9dece";
        httpClient=new AsyncHttpClient();
        deviceId="";
        fromweb=false;
    }
}
