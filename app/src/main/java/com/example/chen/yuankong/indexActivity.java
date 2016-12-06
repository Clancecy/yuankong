package com.example.chen.yuankong;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.chen.yuankong.Activity.AfterLogin;
import com.example.chen.yuankong.Activity.LoginActivity;
import com.example.chen.yuankong.Activity.MainActivity;
import com.example.chen.yuankong.Activity.yidaoActivity;
import com.example.chen.yuankong.Application.App;
import com.example.chen.yuankong.Receiver.ConnectionChangeReceiver;
import com.example.chen.yuankong.Receiver.PushDemoReceiver;
import com.example.chen.yuankong.Utils.ApkController;
import com.example.chen.yuankong.Utils.SmsWriteOputil;
import com.igexin.sdk.PushManager;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class indexActivity extends Activity {

    private Handler mHandler = new Handler();
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private TelephonyManager telMgr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);  //无title
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);  //全屏
        setContentView(R.layout.activity_index);
        // SDK初始化，第三方程序启动时，都要进行SDK初始化工作
        ApkController.hasRootPerssion();
        PushManager.getInstance().initialize(this.getApplicationContext());
        getImieStatus();
        telMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        getSharedPreferences("LOGIN", Context.MODE_PRIVATE).edit().putString("benji", telMgr.getLine1Number()).commit();
      //  Toast.makeText(indexActivity.this, getSharedPreferences("LOGIN", Context.MODE_PRIVATE).getString("benji",""), Toast.LENGTH_SHORT).show();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Start();
            }
        }, 2500);
    }
    void Start() {

        sp = getSharedPreferences("LOGIN", Context.MODE_PRIVATE);
        Log.v("hello", "indexemail" + sp.getString("Email", ""));
        String cid = PushManager.getInstance().getClientid(this);
        Log.d("hello", "当前应用的cid为：" + cid);
        App.cid=cid;
        //  ApkController.hasRootPerssion();
        sp = getSharedPreferences("USER", Context.MODE_PRIVATE);
    //    Toast.makeText(indexActivity.this, sp.getString("LOGIN",""), Toast.LENGTH_SHORT).show();
        if (!sp.getString("LOGIN", "").isEmpty()) {
            Intent intent = new Intent(indexActivity.this, AfterLogin.class);
            intent.addFlags(intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            finish();

        } else {
            Intent intent = new Intent(indexActivity.this, LoginActivity.class);
            intent.addFlags(intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            finish();
        }
    }

    private void checkcid() {
        AsyncHttpClient client = App.httpClient;
        String url = App.host + "/home/user/FindCID";

        RequestParams params = new RequestParams();
        params.put("DeviceId", App.deviceId);
        Log.v("hello", "deciveid " + App.deviceId);
        client.post(url, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
                Intent intent = new Intent(indexActivity.this, MainActivity.class);
                intent.addFlags(intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
            }

            @Override
            public void onSuccess(int i, Header[] headers, String s) {
                if (i == 200) {
                    JSONObject object = (JSONObject) JSONValue.parse(s);
                    String status = (String) object.get("status");
                    String Email = (String) object.get("Email");
                    String Safephone = (String) object.get("Safephone");
                    if (status.equals("1")) {
                        //跳转
                        editor=getSharedPreferences("LOGIN", Context.MODE_PRIVATE).edit();
                        editor.putString("Email", Email);
                        editor.putString("Phone", Safephone);
                        editor.commit();
                        Intent intent = new Intent(indexActivity.this, AfterLogin.class);
                        intent.addFlags(intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);
                        finish();
                    } else {
                        Intent intent = new Intent(indexActivity.this, MainActivity.class);
                        intent.addFlags(intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        });
    }

    private void getImieStatus() {
        TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = tm.getDeviceId();
        App.deviceId = deviceId;
        Log.e("hello ", deviceId + " ");
    }
}
