package com.example.chen.yuankong;

import android.app.Application;

/**
 * Created by Chen on 2016/5/10.
 */
public class App extends Application{
    private static Application mApplication;
    public static boolean mNetWorkState;
    public static String host="http://cugyuank.iask.in";
    public static String Name;
    public static boolean WIFI_CONNECT;
    public static boolean MOBILE_CONNECT;
    public static synchronized Application getInstance() {
        return mApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
        initData();
    }

    public void initData(){
        mNetWorkState=false;
        host="http://cugyuank.iask.in";
        Name=null;
        WIFI_CONNECT=false;
        MOBILE_CONNECT=false;
    }
}
