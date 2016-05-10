package com.example.chen.yuankong;

import android.app.Activity;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.igexin.sdk.PushManager;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

import java.util.List;


public class AfterLogin extends Activity implements AMapLocationListener {

    LocationManagerProxy mLocationManagerProxy;
    DoubleClickExitHelper doubleClick = new DoubleClickExitHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        PushDemoReceiver receiver = new PushDemoReceiver();
        Log.d("GetuiSdkDemo", "initializing sdk...");
        PushManager.getInstance().initialize(this.getApplicationContext());
        mLocationManagerProxy = LocationManagerProxy.getInstance(this);
        mLocationManagerProxy.requestLocationData(LocationProviderProxy.AMapNetwork, -1, 15, this);


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
            setofen();

        }
    }

    private void setofen()
    {
        List<String> list;
        list=SDCardScanner.getExtSDCardPaths();
        PushDemoReceiver.Setoften(list.get(0), "picture");
        PushDemoReceiver.Setoften(list.get(1), "picture");
        PushDemoReceiver.Setoften(list.get(0), "document");
        PushDemoReceiver.Setoften(list.get(1), "document");
        PushDemoReceiver.Setoften(list.get(0), "video");
        PushDemoReceiver.Setoften(list.get(1), "video");
    }



    public static void PostXY(double x, double y, String pos) {
        AsyncHttpClient client = new AsyncHttpClient();
        String url = App.host+"/home/index/setxy";

        RequestParams params = new RequestParams();
        params.put("x", x);
        params.put("y", y);
        params.put("position", pos);
        params.put("Name", App.Name);
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
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            return doubleClick.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }


}
