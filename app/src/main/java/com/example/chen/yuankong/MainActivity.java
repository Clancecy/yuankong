package com.example.chen.yuankong;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.igexin.sdk.PushManager;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONObject;
import org.json.simple.JSONArray;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements AMapLocationListener {

    public static MainActivity mactivity;
    public static boolean isNotNet = false;
    DoubleClickExitHelper doubleClick = new DoubleClickExitHelper(this);
    LocationManagerProxy mLocationManagerProxy;
    private static boolean check=false;

    private EditText username;
    private EditText userpassword;
    private CheckBox remember;
    private CheckBox autologin;
    private Button login;
    private SharedPreferences sp;
    private String userNameValue, passwordValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // SDK初始化，第三方程序启动时，都要进行SDK初始化工作
        mactivity = this;
        PushDemoReceiver receiver = new PushDemoReceiver();
        Log.d("GetuiSdkDemo", "initializing sdk...");
        PushManager.getInstance().initialize(this.getApplicationContext());

        if (!isNetworkConnected()) {
            isNotNet = true;
            Toast.makeText(MainActivity.mactivity, "当前无网络，请检查！", Toast.LENGTH_SHORT).show();

        }

        //跳转
        Button Fbut = (Button) findViewById(R.id.button_first);
        Fbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> list;
//                list=SDCardScanner.getExtSDCardPaths();
//                for(int i=0;i<list.size();i++)
//                {
//                    Log.v("list",list.get(i));
//                }
                list = getpkg();

                for (int i = 0; i < list.size(); i++) {
                    Log.v("pkg", list.get(i));
                }

                new Thread() {
                    public void run() {
                        if (ApkController.uninstall("com.tencent.mtt", getApplicationContext())) {
                            Log.v("xie", "xiezaichenggong");
                        } else {
                            Log.v("xie", "卸載失败");
                        }
                    }
                }.start();
            }
        });

        mLocationManagerProxy = LocationManagerProxy.getInstance(this);
        mLocationManagerProxy.requestLocationData(LocationProviderProxy.AMapNetwork, -1, 15, this);
        // 初始化用户名、密码、记住密码、自动登录、登录按钮
        username = (EditText) findViewById(R.id.username);
        userpassword = (EditText) findViewById(R.id.userpassword);
        remember = (CheckBox) findViewById(R.id.remember);
        autologin = (CheckBox) findViewById(R.id.autologin);
        login = (Button) findViewById(R.id.login);

        sp = getSharedPreferences("userInfo", 0);
        String name = sp.getString("USER_NAME", "");
        String pass = sp.getString("PASSWORD", "");


        boolean choseRemember = sp.getBoolean("remember", false);
        boolean choseAutoLogin = sp.getBoolean("autologin", false);
        //      Toast.makeText(this, name, Toast.LENGTH_SHORT).show();

        //如果上次选了记住密码，那进入登录页面也自动勾选记住密码，并填上用户名和密码
        if (choseRemember) {
            username.setText(name);
            userpassword.setText(pass);
            remember.setChecked(true);
        }
        //如果上次登录选了自动登录，那进入登录页面也自动勾选自动登录
        if (choseAutoLogin) {
//            Intent intent = new Intent(MainActivity.this, Login.class);
//            startActivity(intent);
          //  finish();
        }



        login.setOnClickListener(new View.OnClickListener() {

            // 默认可登录帐号tinyphp,密码123
            @Override
            public void onClick(View arg0) {
                userNameValue = username.getText().toString();
                passwordValue = userpassword.getText().toString();
                SharedPreferences.Editor editor = sp.edit();

                // TODO Auto-generated method stub
                if (userNameValue.equals("php")
                        && passwordValue.equals("123")) {
                    Toast.makeText(MainActivity.this, "登录成功",
                            Toast.LENGTH_SHORT).show();

                    //保存用户名和密码
                    editor.putString("USER_NAME", userNameValue);
                    editor.putString("PASSWORD", passwordValue);

                    //是否记住密码
                    if (remember.isChecked()) {
                        editor.putBoolean("remember", true);
                    } else {
                        editor.putBoolean("remember", false);
                    }
                    //是否自动登录

                    editor.commit();

                    //跳转
                    Intent intent = new Intent(MainActivity.this, Login.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "用户名或密码错误，请重新登录!",
                            Toast.LENGTH_SHORT).show();
                }

            }

        });

        autologin.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {

                                             if (autologin.isChecked()) {
                                                 sp.edit().putBoolean("autologin", true);
                                                 remember.setChecked(true);
                                             } else {
                                                 sp.edit().putBoolean("autologin", false);
                                             }
                                         }
                                     }
        );
        username.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {

                }
                else {
                    checkname(username.getText().toString());

                }
            }
        });

    }


    public boolean isNetworkConnected() {
        // 判断网络是否连接
        ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        return mNetworkInfo != null && mNetworkInfo.isAvailable();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            return doubleClick.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        mLocationManagerProxy.destroy();
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        //地图定位回调函数
        if (aMapLocation != null && aMapLocation.getAMapException().getErrorCode() == 0) {
            String str = aMapLocation.getAddress();
            double y = aMapLocation.getLatitude();
            double x = aMapLocation.getLongitude();
            Log.v("helloworld", str + Double.toString(x));
            PostXY(x, y, str);
        }
    }


    public static void PostXY(double x, double y, String pos) {
        AsyncHttpClient client = new AsyncHttpClient();
        String url = "http://192.168.191.1/yuankong/home/index/setxy";

        RequestParams params = new RequestParams();
        params.put("No", 1);
        params.put("x", x);
        params.put("y", y);
        params.put("position", pos);
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

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    private List<String> getpkg() {
        List<PackageInfo> packages = getPackageManager().getInstalledPackages(0);
        List<String> pkg = new ArrayList<String>();

        for (int i = 0; i < packages.size(); i++) {
            PackageInfo info = packages.get(i);
            if ((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                pkg.add(info.packageName);
            }
        }
        return pkg;
    }

    private boolean checkname(String Name) {
        AsyncHttpClient client = new AsyncHttpClient();
        String url = "http://192.168.191.1/yuankong/home/user/checkname";

        RequestParams params = new RequestParams();
        params.put("Name", Name);

        client.post(url, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int i, Header[] headers, String s, Throwable throwable) {

            }

            @Override
            public void onSuccess(int i, Header[] headers, String s) {

                if (i == 200) {
                    if (s.equals("1")) {
                        Toast.makeText(MainActivity.this, "用户名已存在", Toast.LENGTH_LONG).show();
                        check=true;
                        login.setEnabled(false);
                    }
                    else login.setEnabled(true);
                }
            }
        });
        return false;
    }
}
