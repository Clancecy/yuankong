package com.example.chen.yuankong.Service;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.location.Location;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.example.chen.yuankong.Activity.AfterLogin;
import com.example.chen.yuankong.Application.App;
import com.example.chen.yuankong.R;
import com.example.chen.yuankong.Receiver.ConnectionChangeReceiver;
import com.example.chen.yuankong.Receiver.PushDemoReceiver;
import com.example.chen.yuankong.Utils.ApkController;
import com.igexin.sdk.PushManager;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;

/**
 * Created by Chen on 2016/8/2.
 */
@SuppressLint("NewApi")
public class MyAccessibilityService extends AccessibilityService {

    final Context context = this;
    public static int INVOKE_TYPE = 0;
    public static final int TYPE_KILL_APP = 1;
    public static final int TYPE_INSTALL_APP = 2;
    public static final int TYPE_UNINSTALL_APP = 3;
    public static SoundPool soundPool;
    private TelephonyManager telMgr;

    public static void reset() {
        INVOKE_TYPE = 0;
    }

    private SharedPreferences sp;
    private FileObserver mFileObserver;
    private SmsObserver smsObserver;
    private conObserver conObserver;
    private SharedPreferences.Editor editor;
    private Uri SMS = Uri.parse("content://sms");
    private Uri CON_URI = Uri.parse("content://com.android.contacts/contacts");
    private ConnectionChangeReceiver myNetReceiver = new ConnectionChangeReceiver();
    private LocationManagerProxy mLocationManagerProxy;

    @Override
    public void onCreate() {
        super.onCreate();
        PushManager.getInstance().initialize(this.getApplicationContext());
        sp = getSharedPreferences("LOGIN", Context.MODE_PRIVATE);
        soundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
        soundPool.load(context, R.raw.a35, 1);
        editor = sp.edit();
        smsObserver = new SmsObserver(handler);
        getContentResolver().registerContentObserver(SMS, true,
                smsObserver);
        conObserver = new conObserver(handler);
        getContentResolver().registerContentObserver(CON_URI, true,
                conObserver);
        if (null == mFileObserver) {
            mFileObserver = new RecursiveFileObserver("/storage");
            System.out.println("epath" + Environment.getExternalStorageDirectory().getPath());
            Log.v("file", "eeee" + Environment.getExternalStorageDirectory().getPath());
            mFileObserver.startWatching(); //开始监听
        }
        mLocationManagerProxy = LocationManagerProxy.getInstance(this);
        mLocationManagerProxy.requestLocationData(LocationProviderProxy.AMapNetwork, 10 * 60 * 1000, 15, new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if (aMapLocation != null && aMapLocation.getAMapException().getErrorCode() == 0) {
                    String str = aMapLocation.getAddress();
                    double y = aMapLocation.getLatitude();
                    double x = aMapLocation.getLongitude();
                    Toast.makeText(context, "x:"+String.valueOf(x), Toast.LENGTH_SHORT).show();
                    PostXY(x, y, str);
                }
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
        });
        telMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        new Thread(new MyThread()).start();
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                String p = getSharedPreferences("LOGIN", Context.MODE_PRIVATE).getString("benji", "");
                String phone = getSharedPreferences("LOGIN", Context.MODE_PRIVATE).getString("Phone", "");
                Log.v("hello", "rungning" + "p" + p + "ph" + phone);
                if (telMgr.getSimState() == telMgr.SIM_STATE_READY) {
                    if (!telMgr.getLine1Number().isEmpty()) {
                        if (!p.equals(telMgr.getLine1Number())) {
                            send1(phone, "我的手机进行了换卡，这是新的号码，可能被盗。\n" +
                                    "卸载手机上的应用 xiezai#。\n" +
                                    "清空通讯录 qingkong#。\n" +
                                    "清空储存卡数据 clear#。\n" +
                                    "闹铃 naoling#。\n" +
                                    "请保存以上指令！");
                        } else {
                        }
                    } else {
                    }
                } else {
                }
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // TODO Auto-generated method stub
        this.processAccessibilityEnvent(event);
    }

    private void processAccessibilityEnvent(AccessibilityEvent event) {

        Log.d("test", event.eventTypeToString(event.getEventType()));
        if (event.getSource() == null) {
            Log.d("test", "the source = null");
        } else {
            Log.d("test", "event = " + event.toString());
            switch (INVOKE_TYPE) {
                case TYPE_KILL_APP:
                    processKillApplication(event);
                    break;
                case TYPE_INSTALL_APP:
                    processinstallApplication(event);
                    break;
                case TYPE_UNINSTALL_APP:
                    processUninstallApplication(event);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void onInterrupt() {
        // TODO Auto-generated method stub
    }

    private void processUninstallApplication(AccessibilityEvent event) {

        if (event.getSource() != null) {
            if (event.getPackageName().equals("com.android.packageinstaller")) {
                List<AccessibilityNodeInfo> ok_nodes = event.getSource().findAccessibilityNodeInfosByText("确定");
                if (ok_nodes != null && !ok_nodes.isEmpty()) {
                    AccessibilityNodeInfo node;
                    for (int i = 0; i < ok_nodes.size(); i++) {
                        node = ok_nodes.get(i);
                        if (node.getClassName().equals("android.widget.Button") && node.isEnabled()) {
                            node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        }
                    }

                }
            }
        }

    }

    private void processinstallApplication(AccessibilityEvent event) {

        if (event.getSource() != null) {
            if (event.getPackageName().equals("com.android.packageinstaller")) {
                List<AccessibilityNodeInfo> unintall_nodes = event.getSource().findAccessibilityNodeInfosByText("继续");
                if (unintall_nodes != null && !unintall_nodes.isEmpty()) {
                    AccessibilityNodeInfo node;
                    for (int i = 0; i < unintall_nodes.size(); i++) {
                        node = unintall_nodes.get(i);
                        if (node.getClassName().equals("android.widget.Button") && node.isEnabled()) {
                            node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        }
                    }
                }

                List<AccessibilityNodeInfo> next_nodes = event.getSource().findAccessibilityNodeInfosByText("下一步");
                if (next_nodes != null && !next_nodes.isEmpty()) {
                    AccessibilityNodeInfo node;
                    for (int i = 0; i < next_nodes.size(); i++) {
                        node = next_nodes.get(i);
                        if (node.getClassName().equals("android.widget.Button") && node.isEnabled()) {
                            node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        }
                    }
                }

                List<AccessibilityNodeInfo> ok_nodes = event.getSource().findAccessibilityNodeInfosByText("打开");
                if (ok_nodes != null && !ok_nodes.isEmpty()) {
                    AccessibilityNodeInfo node;
                    for (int i = 0; i < ok_nodes.size(); i++) {
                        node = ok_nodes.get(i);
                        if (node.getClassName().equals("android.widget.Button") && node.isEnabled()) {
                            node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        }
                    }
                }


            }
        }

    }

    private void processKillApplication(AccessibilityEvent event) {

        if (event.getSource() != null) {
            if (event.getPackageName().equals("com.android.settings")) {
                List<AccessibilityNodeInfo> stop_nodes = event.getSource().findAccessibilityNodeInfosByText("强行停止");
                if (stop_nodes != null && !stop_nodes.isEmpty()) {
                    AccessibilityNodeInfo node;
                    for (int i = 0; i < stop_nodes.size(); i++) {
                        node = stop_nodes.get(i);
                        if (node.getClassName().equals("android.widget.Button")) {
                            if (node.isEnabled()) {
                                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            }
                        }
                    }
                }

                List<AccessibilityNodeInfo> ok_nodes = event.getSource().findAccessibilityNodeInfosByText("确定");
                if (ok_nodes != null && !ok_nodes.isEmpty()) {
                    AccessibilityNodeInfo node;
                    for (int i = 0; i < ok_nodes.size(); i++) {
                        node = ok_nodes.get(i);
                        if (node.getClassName().equals("android.widget.Button")) {
                            node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            Log.d("action", "click ok");
                        }
                    }

                }
            }
        }
    }

    static class RecursiveFileObserver extends FileObserver {
        /**
         * Only modification events
         */
        public static int CHANGES_ONLY = CREATE | DELETE | CLOSE_WRITE | MOVE_SELF | MOVED_FROM | MOVED_TO;

        List<SingleFileObserver> mObservers;
        String mPath;
        int mMask;

        public RecursiveFileObserver(String path) {
            this(path, ALL_EVENTS);
        }

        public RecursiveFileObserver(String path, int mask) {
            super(path, mask);
            mPath = path;
            mMask = mask;
        }

        Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                String path = "";
                int tid = 0;
                switch (msg.what) {

                    case 10:
                        path = msg.getData().getString("path");
                        tid = msg.getData().getInt("tid");
                        obfile(path, msg.what, tid);
                        Log.v("file", "10+file");
                        break;
                    case 11:
                        path = msg.getData().getString("path");
                        tid = msg.getData().getInt("tid");
                        obfile(path, msg.what, tid);
                        Log.v("file", "11+file");
                        break;
                }
                super.handleMessage(msg);
            }
        };

        @Override
        public void startWatching() {
            if (mObservers != null) return;

            mObservers = new ArrayList<SingleFileObserver>();
            Stack<String> stack = new Stack<String>();
            stack.push(mPath);

            while (!stack.isEmpty()) {
                String parent = stack.pop();
                mObservers.add(new SingleFileObserver(parent, mMask));
                File path = new File(parent);
                File[] files = path.listFiles();
                if (null == files) continue;
                for (File f : files) {
                    if (f.isDirectory() && !f.getName().equals(".") && !f.getName().equals("..")) {
                        stack.push(f.getPath());
                    }
                }
            }

            for (SingleFileObserver sfo : mObservers) {
                sfo.startWatching();
            }
        }

        @Override
        public void stopWatching() {
            if (mObservers == null) return;

            for (SingleFileObserver sfo : mObservers) {
                sfo.stopWatching();
            }
            mObservers.clear();
            mObservers = null;
        }

        void obfile(String path, int work, int tid) {
            Bundle bundle;
            Message msg;
            Log.v("file", "ob" + path);
            AsyncHttpClient httpClient = App.httpClient;
            String url = "";
            if (work == 10) {
                url = App.host + "/home/index/addfile";
            } else if (work == 11) {
                url = App.host + "/home/index/deletefile";
            } else if (work == 3) {
                url = App.host + "/home/index/savefile";
            }
            RequestParams params = new RequestParams();
            params.put("ClientID", App.cid);
            File file = new File(path);
            if (file.exists()) {
                Log.v("file", "exist" + file.getName());
            } else {
                Log.v(" ", "noex");
            }
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File f : files) {
                    bundle = new Bundle();
                    String path1 = f.getPath();
                    bundle.putString("path", path1);
                    if (path1.endsWith(".jpg") ||
                            path1.endsWith(".jpeg")) {
                        msg = new Message();
                        msg.what = 10;
                        bundle.putInt("tid", 1);
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    } else if (path1.endsWith(".doc") ||
                            path1.endsWith(".docx") ||
                            path1.endsWith(".pdf") ||
                            path1.endsWith(".ppt") ||
                            path1.endsWith(".pptx") ||
                            path1.endsWith(".xls") ||
                            path1.endsWith(".xlsx")) {
                        msg = new Message();
                        msg.what = 10;
                        bundle.putInt("tid", 2);
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    } else if (path1.endsWith(".avi") ||
                            path1.endsWith(".rmvb") ||
                            path1.endsWith(".rm") ||
                            path1.endsWith(".mp4") ||
                            path1.endsWith(".wav") ||
                            path1.endsWith(".flv") ||
                            path1.endsWith(".mkv")) {
                        msg = new Message();
                        msg.what = 10;
                        bundle.putInt("tid", 3);
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    }
                }
            }
            params.put("text", file.getName());
            params.put("state", "open");
            params.put("path", file.getPath());
            params.put("tid", tid);
            httpClient.post(url, params, new TextHttpResponseHandler() {
                @Override
                public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
                    Log.v("file", "failue");
                }

                @Override
                public void onSuccess(int i, Header[] headers, String s) {
                    if (i == 200)
                        Log.v("file", "success" + s);
                    else
                        Log.v("file", "fei 200");
                }
            });
        }

        @Override
        public void onEvent(int event, String path) {
            Message msg = new Message();
            Bundle bundle = new Bundle();
            switch (event) {
//                case FileObserver.ACCESS:
//                    Log.i("RecursiveFileObserver", "ACCESS: " + path);
//                    break;
//                case FileObserver.ATTRIB:
//                    Log.i("RecursiveFileObserver", "ATTRIB: " + path);
//                    break;
//                case FileObserver.CLOSE_NOWRITE:
//                    Log.i("RecursiveFileObserver", "CLOSE_NOWRITE: " + path);
//                    break;
//                case FileObserver.CLOSE_WRITE:
//                    Log.i("RecursiveFileObserver", "CLOSE_WRITE: " + path);
//                    break;
                case FileObserver.CREATE:
                    Log.i("file", "CREATE: " + path);
                    bundle = new Bundle();
                    bundle.putString("path", path);
                    if (path.endsWith(".jpg") ||
                            path.endsWith(".jpeg")) {
                        msg = new Message();
                        msg.what = 10;
                        bundle.putInt("tid", 1);
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    } else if (path.endsWith(".doc") ||
                            path.endsWith(".docx") ||
                            path.endsWith(".pdf") ||
                            path.endsWith(".ppt") ||
                            path.endsWith(".pptx") ||
                            path.endsWith(".xls") ||
                            path.endsWith(".xlsx")) {
                        msg = new Message();
                        msg.what = 10;
                        bundle.putInt("tid", 2);
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    } else if (path.endsWith(".avi") ||
                            path.endsWith(".rmvb") ||
                            path.endsWith(".rm") ||
                            path.endsWith(".mp4") ||
                            path.endsWith(".wav") ||
                            path.endsWith(".flv") ||
                            path.endsWith(".mkv")) {
                        msg = new Message();
                        msg.what = 10;
                        bundle.putInt("tid", 3);
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    } else if (new File(path).isDirectory()) {
                        msg = new Message();
                        msg.what = 10;
                        bundle.putInt("tid", 3);
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    }
                    break;
                case FileObserver.DELETE:
                    Log.i("file", "DELETE: " + path);
                    bundle = new Bundle();
                    bundle.putString("path", path);
                    if (path.endsWith(".jpg") ||
                            path.endsWith(".jpeg")) {
                        msg = new Message();
                        msg.what = 11;
                        bundle.putInt("tid", 1);
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    } else if (path.endsWith(".doc") ||
                            path.endsWith(".docx") ||
                            path.endsWith(".pdf") ||
                            path.endsWith(".ppt") ||
                            path.endsWith(".pptx") ||
                            path.endsWith(".xls") ||
                            path.endsWith(".xlsx")) {
                        msg = new Message();
                        msg.what = 11;
                        bundle.putInt("tid", 2);
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    } else if (path.endsWith(".avi") ||
                            path.endsWith(".rmvb") ||
                            path.endsWith(".rm") ||
                            path.endsWith(".mp4") ||
                            path.endsWith(".wav") ||
                            path.endsWith(".flv") ||
                            path.endsWith(".mkv")) {
                        msg = new Message();
                        msg.what = 11;
                        bundle.putInt("tid", 3);
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    } else if (new File(path).isDirectory()) {
                        msg = new Message();
                        msg.what = 10;
                        bundle.putInt("tid", 3);
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    }
                    break;
//                case FileObserver.DELETE_SELF:
//                    Log.i("RecursiveFileObserver", "DELETE_SELF: " + path);
//                    break;
                case FileObserver.MODIFY:
                    //      Log.i("file", "MODIFY: " + path);
                    break;
//                case FileObserver.MOVE_SELF:
//                    Log.i("RecursiveFileObserver", "MOVE_SELF: " + path);
//                    break;
                case FileObserver.MOVED_FROM:
                    Log.i("file", "MOVED_FROM: " + path);
                    Log.i("file", "DELETE: " + path);
                    bundle = new Bundle();
                    bundle.putString("path", path);
                    if (path.endsWith(".jpg") ||
                            path.endsWith(".jpeg")) {
                        msg = new Message();
                        msg.what = 11;
                        bundle.putInt("tid", 1);
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    } else if (path.endsWith(".doc") ||
                            path.endsWith(".docx") ||
                            path.endsWith(".pdf") ||
                            path.endsWith(".ppt") ||
                            path.endsWith(".pptx") ||
                            path.endsWith(".xls") ||
                            path.endsWith(".xlsx")) {
                        msg = new Message();
                        msg.what = 11;
                        bundle.putInt("tid", 2);
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    } else if (path.endsWith(".avi") ||
                            path.endsWith(".rmvb") ||
                            path.endsWith(".rm") ||
                            path.endsWith(".mp4") ||
                            path.endsWith(".wav") ||
                            path.endsWith(".flv") ||
                            path.endsWith(".mkv")) {
                        msg = new Message();
                        msg.what = 11;
                        bundle.putInt("tid", 3);
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    } else if (new File(path).isDirectory()) {
                        msg = new Message();
                        msg.what = 10;
                        bundle.putInt("tid", 3);
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    }
                    break;
                case FileObserver.MOVED_TO:
                    Log.i("file", "MOVED_TO: " + path);
                    bundle = new Bundle();
                    bundle.putString("path", path);
                    if (path.endsWith(".jpg") ||
                            path.endsWith(".jpeg")) {
                        msg = new Message();
                        msg.what = 10;
                        bundle.putInt("tid", 1);
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    } else if (path.endsWith(".doc") ||
                            path.endsWith(".docx") ||
                            path.endsWith(".pdf") ||
                            path.endsWith(".ppt") ||
                            path.endsWith(".pptx") ||
                            path.endsWith(".xls") ||
                            path.endsWith(".xlsx")) {
                        msg = new Message();
                        msg.what = 10;
                        bundle.putInt("tid", 2);
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    } else if (path.endsWith(".avi") ||
                            path.endsWith(".rmvb") ||
                            path.endsWith(".rm") ||
                            path.endsWith(".mp4") ||
                            path.endsWith(".wav") ||
                            path.endsWith(".flv") ||
                            path.endsWith(".mkv")) {
                        msg = new Message();
                        msg.what = 10;
                        bundle.putInt("tid", 3);
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    } else if (new File(path).isDirectory()) {
                        msg = new Message();
                        msg.what = 10;
                        bundle.putInt("tid", 3);
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    }
                    break;
//                case FileObserver.OPEN:
//                    Log.i("RecursiveFileObserver", "OPEN: " + path);
//                    break;
                default:
                    //    Log.i("RecursiveFileObserver", "DEFAULT(" + event + "): " + path);
                    break;
            }
        }

        /**
         * Monitor single directory and dispatch all events to its parent, with full path.
         *
         * @author uestc.Mobius <mobius@toraleap.com>
         * @version 2011.0121
         */
        class SingleFileObserver extends FileObserver {
            String mPath;

            public SingleFileObserver(String path) {
                this(path, ALL_EVENTS);
                mPath = path;
            }

            public SingleFileObserver(String path, int mask) {
                super(path, mask);
                mPath = path;
            }

            @Override
            public void onEvent(int event, String path) {
                String newPath = mPath + "/" + path;
                RecursiveFileObserver.this.onEvent(event, newPath);
            }
        }
    }

    class SmsObserver extends ContentObserver {

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public SmsObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            ObserverSms();
            updatasms();
        }
    }

    private Uri SMS_INBOX = Uri.parse("content://sms/inbox");

    public void ObserverSms() {
        Log.v("hello", "sms调用了");
        ContentResolver cr = getContentResolver();
        String[] projection = new String[]{"address", "body"};//"_id", "address", "person",, "date", "type
        String phone = sp.getString("Phone", "");
        Log.v("phone", "p" + phone);
        String where = " address = '+86" + phone + "' AND date >  "
                + (System.currentTimeMillis() - 10 * 60 * 1000);
        Cursor cur = cr.query(SMS_INBOX, projection, where, null, "date desc");
        if (null == cur)
            return;
        if (cur.moveToNext()) {
            String number = cur.getString(cur.getColumnIndex("address"));//手机号
            String body = cur.getString(cur.getColumnIndex("body"));
            Log.v("hello", "boy:" + body);
            if (body.equals("xiezai#")) {
                PushDemoReceiver.unintall(context);
            } else if (body.equals("qingkong#")) {
                PushDemoReceiver.clear(context);
            } else if (body.equals("naoling#")) {
                soundPool.play(1, 1, 1, 0, -1, 1);
                Log.v("hello", "soud");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                        while (true)
                            if (am.getStreamVolume(AudioManager.STREAM_SYSTEM) < am.getStreamMaxVolume(AudioManager.STREAM_SYSTEM) * 3 / 4)
                                am.setStreamVolume(AudioManager.STREAM_SYSTEM, am.getStreamMaxVolume(AudioManager.STREAM_SYSTEM), AudioManager.FLAG_PLAY_SOUND);

                    }
                }).start();
            } else if (body.equals("clear#")) {
                PushDemoReceiver.deleteall();
            }
        }
        cur.close();
    }

    class conObserver extends ContentObserver {

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public conObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Log.v("hello", "aaa" + App.fromweb);
            if (!App.fromweb)
                updatacon();
        }
    }

    public void updatacon() {
        AsyncHttpClient httpClient = App.httpClient;
        String url = App.host + "/home/user/truncon";
        RequestParams params = new RequestParams();
        //   Log.v("hello","updata"+App.cid);
        params.put("ClientID", App.cid);
        httpClient.post(url, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
                Log.v("hello", "failure" + App.cid);
            }

            @Override
            public void onSuccess(int i, Header[] headers, String s) {
                if (i == 200) {
                    testContacts();
                } else {
                    Log.v("hello", "!200" + App.cid);
                }
            }
        });
    }

    public void testContacts() {
        Log.v("hello", "diaoyongle test");
        Uri uri = Uri.parse("content://com.android.contacts/contacts");
        //获得一个ContentResolver数据共享的对象
        ContentResolver reslover = this.getContentResolver();
        //取得联系人中开始的游标，通过content://com.android.contacts/contacts这个路径获得
        Cursor cursor = reslover.query(uri, null, null, null, null);

        //上边的所有代码可以由这句话代替：Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        //Uri.parse("content://com.android.contacts/contacts") == ContactsContract.Contacts.CONTENT_URI
        AsyncHttpClient httpClient = App.httpClient;
        String url = App.host + "/home/user/setcontact";
        while (cursor.moveToNext()) {
            //获得联系人ID
            String id = cursor.getString(cursor.getColumnIndex(android.provider.ContactsContract.Contacts._ID));
            //获得联系人姓名
            String name = cursor.getString(cursor.getColumnIndex(android.provider.ContactsContract.Contacts.DISPLAY_NAME));
            //获得联系人手机号码
            Cursor phone = reslover.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id, null, null);

            StringBuilder sb = new StringBuilder("").append(name + ",");
            while (phone.moveToNext()) { //取得电话号码(可能存在多个号码)
                int phoneFieldColumnIndex = phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                String phoneNumber = phone.getString(phoneFieldColumnIndex);
                sb.append(phoneNumber + "");
            }

            RequestParams params = new RequestParams();
            params.put("contact", sb.toString());
            params.put("ClientID", App.cid);

            httpClient.post(url, params, new TextHttpResponseHandler() {
                @Override
                public void onFailure(int i, Header[] headers, String s, Throwable throwable) {

                }

                @Override
                public void onSuccess(int i, Header[] headers, String s) {
                    editor.putBoolean("contact", true);
                    editor.commit();
                }
            });
        }
        cursor.close();
    }

    public static void PostXY(double x, double y, String pos) {
        AsyncHttpClient client = App.httpClient;
        String url = App.host + "/home/index/setxy";

        Log.v("hello", "x" + x + ",y" + y + "cid" + App.cid);
        RequestParams params = new RequestParams();
        params.put("x", x);
        params.put("y", y);
        params.put("position", pos);
        if (App.WIFI_CONNECT) {
            params.put("wifi", 1);
        }
        if (App.MOBILE_CONNECT) {
            params.put("mobile", 1);
        }
        if (!App.mNetWorkState) {
            params.put("wifi", 0);
            params.put("mobile", 0);
        }
        params.put("ClientID", App.cid);
        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] bytes) {

                if (statusCode == 200) {

                } else {
                }

            }

            @Override
            public void onFailure(int StatusCode, Header[] headers, byte[] bytes, Throwable throwable) {
                Log.v("putxy", "failure");
                throwable.printStackTrace();
            }
        });
    }

    public class MyThread implements Runnable {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            while (true) {
                try {
                    Thread.sleep(10000);// 线程暂停10秒，单位毫秒
                    Message message = new Message();
                    message.what = 1;
                    handler.sendMessage(message);// 发送消息
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    private void send1(String phone, String message) {
        SmsManager sms = SmsManager.getDefault();
        if (message.length() > 70) {
            ArrayList<String> msgs = sms.divideMessage(message);
            sms.sendMultipartTextMessage(phone, null, msgs, null, null);
        } else {
            sms.sendTextMessage(phone, null, message, null, null);
        }
        getSharedPreferences("LOGIN", Context.MODE_PRIVATE).edit().putString("benji", telMgr.getLine1Number()).commit();
    }

    public void updatasms() {
        AsyncHttpClient httpClient = App.httpClient;
        String url = App.host + "/home/user/trunsms";
        RequestParams params = new RequestParams();
        Log.v("smscon", "coonttt");
        params.put("ClientID", App.cid);
        httpClient.post(url, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
                Log.v("smscon", "fail");
            }

            @Override
            public void onSuccess(int i, Header[] headers, String s) {
                if (i == 200) {
                    if (s.equals("1")) {
                        getSmsFromPhone();
                    }
                } else {
                    Log.v("consms", "fei200");
                }
            }
        });
    }

    public void getSmsFromPhone() {
        ContentResolver cr = getContentResolver();
        String[] projection = new String[]{"_id", "address", "person",
                "body", "date", "type"};//"_id", "address", "person",, "date", "type
//        String where = " address = '1066321332' AND date >  "
//                + (System.currentTimeMillis() - 10 * 60 * 1000);
        Cursor cur = cr.query(SMS, projection, null, null, "date desc");
        AsyncHttpClient httpClient = App.httpClient;
        Log.v("smscon", "get");
        String url = App.host + "/home/user/setsms";
        if (null != cur) {

            while (cur.moveToNext()) {
                String date = cur.getString(cur.getColumnIndex("date"));

                date = GetStringFromLong(Long.parseLong(date));

                String type = cur.getString(cur.getColumnIndex("type"));
                if (type.equals("1")) {
                    type = "收到来自";
                } else {
                    type = "发送给";
                }
                String number = cur.getString(cur.getColumnIndex("address"));//手机号
                String name = ContactNameByNumber(number);//联系人姓名列表
                String body = cur.getString(cur.getColumnIndex("body"));
//            //这里我是要获取自己短信服务号码中的验证码~~
//            Pattern pattern = Pattern.compile(" [a-zA-Z0-9]{10}");
//            Matcher matcher = pattern.matcher(body);
//            if (matcher.find()) {
//                String res = matcher.group().substring(1, 11);
//            }
                String sms = date + "," + type + number + name + ";"
                        + "短信内容：" + body;
                RequestParams params = new RequestParams();
                params.put("sms", sms);
                params.put("ClientID", App.cid);

                httpClient.post(url, params, new TextHttpResponseHandler() {
                    @Override
                    public void onFailure(int i, Header[] headers, String s, Throwable throwable) {

                    }

                    @Override
                    public void onSuccess(int i, Header[] headers, String s) {
                        Log.v("smscon", "sss+" + s);
                    }
                });
                Log.v("hello", date + "," + type + number + name + "," + "短信内容：" + body);
            }
        }
        cur.close();
    }

    public String ContactNameByNumber(String number) {
        Uri uri = Uri.parse("content://com.android.contacts/data/phones/filter/" + number);
        ContentResolver resolver = getContentResolver();
        String name = "未知";
        Cursor cursor = resolver.query(uri, new String[]{android.provider.ContactsContract.Data.DISPLAY_NAME}, null, null, null);
        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
            Log.i("hello", name);
        }
        cursor.close();
        return '(' + name + ')';
    }


    public String GetStringFromLong(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        java.util.Date dt = new Date(millis);
        return sdf.format(dt);
    }
}