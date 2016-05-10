package com.example.chen.yuankong;


import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;

import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    public static MainActivity mactivity;
    DoubleClickExitHelper doubleClick = new DoubleClickExitHelper(this);
    private ConnectionChangeReceiver myReceiver;
    private EditText username;
    private EditText userpassword;
    private CheckBox showpwd;
    private Button login;
    private String userNameValue, passwordValue;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // SDK初始化，第三方程序启动时，都要进行SDK初始化工作
        mactivity = this;
        registerReceiver();
        //跳转
        Button Fbut = (Button) findViewById(R.id.button_first);
        Fbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                List<String> list;
////                list=SDCardScanner.getExtSDCardPaths();
////                for(int i=0;i<list.size();i++)
////                {
////                    Log.v("list",list.get(i));
////                }
//                list = getpkg();
//
//                for (int i = 0; i < list.size(); i++) {
//                    Log.v("pkg", list.get(i));
//                }
//
//                new Thread() {
//                    public void run() {
//                        if (ApkController.uninstall("com.tencent.mtt", getApplicationContext())) {
//                            Log.v("xie", "xiezaichenggong");
//                        } else {
//                            Log.v("xie", "卸載失败");
//                        }
//                    }
//                }.start();
                Log.v("hello",String.valueOf(App.mNetWorkState));

            }
        });
        // 初始化用户名、密码、记住密码、自动登录、登录按钮
        username = (EditText) findViewById(R.id.username);
        userpassword = (EditText) findViewById(R.id.userpassword);
        showpwd=(CheckBox)findViewById(R.id.showpwd);
        login = (Button) findViewById(R.id.login);
       // Intent intent = new Intent(MainActivity.this, Login.class);
        //startActivity(intent);
       // finish();


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                userNameValue = username.getText().toString();
                passwordValue = userpassword.getText().toString();
                // TODO Auto-generated method stub
                PostCID(PushDemoReceiver.scid, userNameValue, passwordValue);
                //跳转
                Intent intent = new Intent(MainActivity.this, AfterLogin.class);
                startActivity(intent);
                finish();
            }
        });

        username.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {

                } else {
                    checkname(username.getText().toString());

                }
            }
        });

        showpwd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    userpassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
                else {
                    userpassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

    }


//    public boolean isNetworkConnected() {
//        // 判断网络是否连接
//        ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
//        return mNetworkInfo != null && mNetworkInfo.isAvailable();
//    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            return doubleClick.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
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
        if (!App.mNetWorkState) {
            Toast.makeText(MainActivity.mactivity, "当前无网络，请检查！", Toast.LENGTH_SHORT).show();
        }
        AsyncHttpClient client = new AsyncHttpClient();
        String url = App.host+"/home/user/checkname";

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
                        login.setEnabled(false);
                    } else login.setEnabled(true);
                }
            }
        });
        return false;
    }

    public void PostCID(String cid, String name, String passwd) {
        // Toast.makeText(MainActivity.mactivity,"来了",5000).show();
        AsyncHttpClient client = new AsyncHttpClient();
        String url = App.host+"/home/user/Add";

        RequestParams params = new RequestParams();
        params.put("ClientID", cid);
        params.put("Name", name);
        params.put("Passwd", passwd);
        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] bytes) {

                if (statusCode == 200) {
                    Toast.makeText(MainActivity.mactivity, "成功记录ClientID", Toast.LENGTH_SHORT).show();
                    // Toast.makeText(MainActivity.mactivity, new String(bytes), 5000).show();
                } else
                    Toast.makeText(MainActivity.mactivity, new String(bytes), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int StatusCode, Header[] headers, byte[] bytes, Throwable throwable) {
                Toast.makeText(MainActivity.mactivity, "记录ClientID失败", Toast.LENGTH_SHORT).show();
                throwable.printStackTrace();
            }
        });
    }
    private  void registerReceiver(){
        IntentFilter filter=new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        myReceiver=new ConnectionChangeReceiver();
        this.registerReceiver(myReceiver, filter);
    }
    private  void unregisterReceiver(){
        this.unregisterReceiver(myReceiver);
    }
}

