package com.example.chen.yuankong.Activity;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
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

import com.example.chen.yuankong.Address.Contact;
import com.example.chen.yuankong.R;
import com.example.chen.yuankong.Receiver.PushDemoReceiver;
import com.example.chen.yuankong.Scanner.SDCardScanner;
import com.example.chen.yuankong.Application.App;
import com.example.chen.yuankong.Utils.ApkController;
import com.example.chen.yuankong.Utils.DoubleClickExitHelper;
import com.example.chen.yuankong.Receiver.ConnectionChangeReceiver;
import com.example.chen.yuankong.Utils.HelperUtils;
import com.igexin.sdk.PushConsts;
import com.igexin.sdk.PushManager;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    public static MainActivity mactivity;
    DoubleClickExitHelper doubleClick = new DoubleClickExitHelper(this);
    private ConnectionChangeReceiver m_Receiver;
    private EditText username;
    private EditText userpassword;
    private CheckBox showpwd;
    private Button login;
    private Button reg;
    private String userNameValue, passwordValue;
    private PushDemoReceiver pushDemoReceiver;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private EditText ip;
    List<Contact> contactList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // SDK初始化，第三方程序启动时，都要进行SDK初始化工作
        pushDemoReceiver = new PushDemoReceiver();
        PushManager.getInstance().initialize(this.getApplicationContext());

        getImieStatus();
        m_Receiver = new ConnectionChangeReceiver();
        reNet();
        // ApkController.hasRootPerssion();
        mactivity = this;

        contactList = new ArrayList<Contact>();

//     //   checkcid();
        // 初始化用户名、密码、记住密码、自动登录、登录按钮
        username = (EditText) findViewById(R.id.username);
        userpassword = (EditText) findViewById(R.id.userpassword);
        showpwd = (CheckBox) findViewById(R.id.showpwd);
        login = (Button) findViewById(R.id.login);
        reg=(Button)findViewById(R.id.reg);
        login.setText("注册");
        reg.setText("我已有账户");
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                userNameValue = username.getText().toString();
                passwordValue = userpassword.getText().toString();
                // TODO Auto-generated method stub
                if (userNameValue.isEmpty() || passwordValue.isEmpty()) {
                    Toast.makeText(MainActivity.this, "用户名或密码不能为空", Toast.LENGTH_SHORT).show();
                } else
                    PostCID(App.cid, userNameValue, passwordValue);


            }
        });

        username.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {

                } else {
                    String user = username.getText().toString();
                    if (!user.isEmpty()) {
                        checkname(username.getText().toString());
                    } else {
                        Toast.makeText(MainActivity.this, "用户名不能为空！", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        showpwd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    userpassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    userpassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });


    }

    public void reNet() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(m_Receiver, filter);
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
        AsyncHttpClient client = App.httpClient;
        String url = App.host + "/home/user/checkname";

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

    /**
     * @param cid    clientid
     * @param name   用户名
     * @param passwd 密码
     */
    public void PostCID(String cid, String name, final String passwd) {
        AsyncHttpClient client = App.httpClient;
        String url = App.host + "/home/user/add";

        RequestParams params = new RequestParams();
        params.put("ClientID", cid);
        Log.v("hello", "postcid" + cid);
//        if (cid.isEmpty()) {
//            Toast.makeText(this, "获取的用户clientid为空，没有网络或者还没有适配你的机型。给您带来不便请见谅！", Toast.LENGTH_LONG).show();
//            return;
//        }
        params.put("DeviceId", App.deviceId);
        params.put("Name", name);
        params.put("Passwd", passwd);
        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] bytes) {

                if (statusCode == 200) {
                    sp = getSharedPreferences("USER", Context.MODE_PRIVATE);
                    editor = sp.edit();
                    editor.putString("LOGIN", "yes");
                    editor.commit();
                    sp = getSharedPreferences("LOGIN", Context.MODE_PRIVATE);
                    editor = sp.edit();
                    editor.putString("PWD", passwd);
                    editor.commit();
                    //跳转
                    Intent intent = new Intent(MainActivity.this, AfterLogin.class);
                    intent.addFlags(intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onFailure(int StatusCode, Header[] headers, byte[] bytes, Throwable throwable) {
                Toast.makeText(MainActivity.mactivity, "记录ClientID失败", Toast.LENGTH_SHORT).show();
                throwable.printStackTrace();
            }
        });
    }
    private void getImieStatus() {
        TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = tm.getDeviceId();
        App.deviceId = deviceId;
        Log.e("DEVICE_ID ", deviceId + " ");
    }
}

