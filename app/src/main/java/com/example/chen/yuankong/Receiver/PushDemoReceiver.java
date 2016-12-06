package com.example.chen.yuankong.Receiver;

import org.apache.http.Header;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.chen.yuankong.Activity.AfterLogin;
import com.example.chen.yuankong.Activity.MainActivity;
import com.example.chen.yuankong.Application.App;
import com.example.chen.yuankong.Scanner.SDCardScanner;
import com.example.chen.yuankong.Service.MyAccessibilityService;
import com.example.chen.yuankong.Utils.ApkController;
import com.igexin.sdk.PushConsts;
import com.igexin.sdk.PushManager;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.zxc.tigerunlock.LockLayer;
import com.zxc.tigerunlock.PullDoorView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class PushDemoReceiver extends BroadcastReceiver {

    /**
     * 应用未启动, 个推 service已经被唤醒,保存在该时间段内离线消息(此时 GetuiSdkDemoActivity.tLogView == null)
     */
    public static StringBuilder payloadData = new StringBuilder();
    private ConnectionChangeReceiver myNetReceiver = new ConnectionChangeReceiver();
    private SharedPreferences sp;

    @Override
    public void onReceive(final Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        Log.d("GetuiSdkDemo", "onReceive() action=" + bundle.getInt("action"));
        switch (bundle.getInt(PushConsts.CMD_ACTION)) {
            case PushConsts.GET_MSG_DATA:
                // 获取透传数据
                // String appid = bundle.getString("appid");
                byte[] payload = bundle.getByteArray("payload");

                String taskid = bundle.getString("taskid");
                String messageid = bundle.getString("messageid");

                // smartPush第三方回执调用接口，actionid范围为90000-90999，可根据业务场景执行
                boolean result = PushManager.getInstance().sendFeedbackMessage(context, taskid, messageid, 90001);
                System.out.println("第三方回执接口调用" + (result ? "成功" : "失败"));

                if (payload != null) {
                    String data = new String(payload);
                    Log.d("GetuiSdkDemo", "receiver payload : " + data);
                    payloadData.append(data);
                    payloadData.append("\n");
                    JSONObject object = (JSONObject) JSONValue.parse(data);
                    String mark = (String) object.get("mark");
                    String path = (String) object.get("path");
                    String type = (String) object.get("type");
                    String delPath = (String) object.get("delPath");
                    String beipath = (String) object.get("beipath");
                    if (mark.equals("expand")) {
                        Log.v("hello", "expand" + path);
                        Settree(path);
                    }
                    if (mark.equals("uninstall")) {
                        Log.v("hello", "unint");
                        unintall(context);
                        // HelperUtils.UninstallHelper("com.example.test", context);
                    }
                    if (mark.equals("uninstallqq")) {
                        unintallqq(context);
                    }
                    if (mark.equals("del")) {
                        if (DeleteFolder(delPath)) {
                            Log.v("hello", "删除成功");
                        }
                    }
                    if (mark.equals("beifen")) {
                        try {
                            Upload(beipath);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    if (mark.equals("sound")) {
                        palysound(context);
                    }

                    if (mark.equals("clear")) {
                        //   AfterLogin.clear();
                        App.fromweb = true;
                        clear(context);
                        deleteSMS(context);
                    }
                    if (mark.equals("deleteall")) {
                        deleteall();
                    }

                    if (mark.equals("net")) {
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
                            App.WIFI_CONNECT = false;
                            //改变背景或者 处理网络的全局变量
                        } else if (wifiNetInfo.isConnected()) {
                            Log.v("hello", "WIFI网络可用");
                            App.mNetWorkState = true;
                            App.WIFI_CONNECT = true;
                            App.MOBILE_CONNECT = false;
                        }
                        Postnet();
                    }
                    if (mark.equals("mima")) {
                        Log.v("mima", "laile");
                        sp = context.getSharedPreferences("USER", Context.MODE_PRIVATE);
                        sp.edit().clear().commit();
//                        AlertDialog al = new AlertDialog.Builder(context).setTitle("提示")
//                                .setMessage("密码验证已过时，请重新登录！")
//                              .setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        AfterLogin.m_after.finish();
//                                    }
//                                }).create();
//                        al.show();
                        Toast.makeText(context, "身份信息过时，请重新的登录！", Toast.LENGTH_SHORT).show();
                        AfterLogin.m_after.finish();
                    }
                    if (mark.equals("suopin")) {
                        lock(context);
                    }
                }
                break;

            case PushConsts.GET_CLIENTID:
                // 获取ClientID(CID)
                // 第三方应用需要将CID上传到第三方服务器，并且将当前用户帐号和CID进行关联，以便日后通过用户帐号查找CID进行消息推送
                String cid = bundle.getString("clientid");
                App.cid = cid;
//                if (!MainActivity.isNotNet)
//                    PostCID(cid);
//                if (GetuiSdkDemoActivity.tView != null) {
//                    GetuiSdkDemoActivity.tView.setText(cid);
//                }
                break;

            case PushConsts.THIRDPART_FEEDBACK:
                /*
                 * String appid = bundle.getString("appid"); String taskid =
                 * bundle.getString("taskid"); String actionid = bundle.getString("actionid");
                 * String result = bundle.getString("result"); long timestamp =
                 * bundle.getLong("timestamp");
                 * 
                 * Log.d("GetuiSdkDemo", "appid = " + appid); Log.d("GetuiSdkDemo", "taskid = " +
                 * taskid); Log.d("GetuiSdkDemo", "actionid = " + actionid); Log.d("GetuiSdkDemo",
                 * "result = " + result); Log.d("GetuiSdkDemo", "timestamp = " + timestamp);
                 */
                break;

            default:
                break;
        }
    }

    private static LockLayer lockLayer;
    private static View lock;
    public static int MSG_LOCK_SUCESS = 0x123;

    public static void lock(Context context) {
        lock = View.inflate(context, com.zxc.tigerunlock.R.layout.main, null);
        lockLayer = new LockLayer(context);
        lockLayer.setLockView(lock);
        lockLayer.lock();
        PullDoorView.setMainHandler(mHandler);
    }

    public static void palysound(final Context context) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (am.getStreamVolume(AudioManager.STREAM_SYSTEM) < am.getStreamMaxVolume(AudioManager.STREAM_SYSTEM) * 3 / 4)
            am.setStreamVolume(AudioManager.STREAM_SYSTEM, am.getStreamMaxVolume(AudioManager.STREAM_SYSTEM), AudioManager.FLAG_PLAY_SOUND);
        MyAccessibilityService.soundPool.play(1, 1, 1, 0, -1, 1);
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
    }

    private static Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (MSG_LOCK_SUCESS == msg.what) {
                lockLayer.unlock();
                // finish(); // 锁屏成功时，结束我们的Activity界面
            }
        }
    };

    public void Settree(String path) {
        File file = new File(path);
        if (file.canRead() && file.exists()) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                AsyncHttpClient httpClient = App.httpClient;
                String url = App.host + "/home/index/addtree";
                for (File f : files) {
                    if (f.canRead()) {
                        RequestParams params = new RequestParams();
                        params.put("tid", 0);
                        params.put("parent", path);
                        if (f.isDirectory()) {
                            params.put("state", "closed");
                        } else {
                            params.put("state", "open");
                        }
                        params.put("text", f.getName());
                        params.put("path", f.getPath());
                        params.put("ClientID", App.cid);
                        Log.v("hello", "f" + f.getName());
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
                }
            }
        }
    }

    /**
     * 获取指定文件大小
     *
     * @param
     * @return
     * @throws Exception
     */
    private static long resetfile(File file) throws Exception {
        long size = 0;
        if (file.exists()) {
            FileInputStream fis = null;
            fis = new FileInputStream(file);
            size = fis.available();
            byte[] ff = new byte[(int) size];
            fis.read(ff);
            fis.close();
        } else {
            file.createNewFile();
            Log.e("获取文件大小", "文件不存在!");
        }
        return size;
    }

    /**
     * 删除单个文件
     *
     * @param filePath 被删除文件的文件名
     * @return 文件删除成功返回true，否则返回false
     */
    public boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            try {
                resetfile(file);
            } catch (Exception e) {
                Toast.makeText(MainActivity.mactivity, "写入失败", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            return file.delete();
        }
        return false;
    }

    /**
     * 删除文件夹以及目录下的文件
     *
     * @param filePath 被删除目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    public boolean deleteDirectory(String filePath) {
        Log.v("deall", filePath);
        boolean flag = false;
        //如果filePath不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        flag = true;
        File[] files = dirFile.listFiles();
        //遍历删除文件夹下的所有文件(包括子目录)
        for (int i = 0; i < files.length; i++) {
            if (files[i].canRead()) {
                if (files[i].isFile()) {
                    //删除子文件
                    flag = deleteFile(files[i].getAbsolutePath());
                    if (!flag) break;
                } else {
                    //删除子目录
                    flag = deleteDirectory(files[i].getAbsolutePath());
                    if (!flag) break;
                }
            }
        }
        if (!flag) return false;
        //删除当前空目录
        return dirFile.delete();
    }

    /**
     * 根据路径删除指定的目录或文件，无论存在与否
     *
     * @param filePath 要删除的目录或文件
     * @return 删除成功返回 true，否则返回 false。
     */
    public boolean DeleteFolder(String filePath) {
        File file = new File(filePath);
        Log.v("deall", filePath);
        if (!file.exists()) {
            return false;
        } else {
            if (file.isFile()) {
                // 为文件时调用删除文件方法
                return deleteFile(filePath);
            } else {

                // 为目录时调用删除目录方法
                return deleteDirectory(filePath);
            }
        }
    }


    public static void unintall(Context context) {
        List<String> list = new ArrayList<>();
        list = getpkg(context);
        for (int i = 0; i < list.size(); i++) {
            String tencent = list.get(i);
            Log.v("hello", list.get(i));
            if (!tencent.contains("yuankong") && !tencent.contains("apowersoft")) {
                ApkController.uninstall(tencent, context);
            }
        }
    }

    public void unintallqq(Context context) {
        String tencent = "com.example.test";
        ApkController.uninstall("com.tencent.mm", context);
        ApkController.uninstall("com.tencent.mobileqq", context);
    }

    public static void Upload(String filename) throws FileNotFoundException {
        AsyncHttpClient httpClient = App.httpClient;
        RequestParams params = new RequestParams();
        File file = new File(filename);
        params.put("file", file);
        params.put("ClientID", App.cid);
        Log.v("hello", file.getName());
        String url = App.host + "/home/user/Upload";
        httpClient.post(url, params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int i, Header[] headers, byte[] bytes) {
                        if (i == 200) {
                            Log.v("hello", new String(bytes));
                        } else
                            Log.v("hello", "not200");
                    }

                    @Override
                    public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                        Log.v("hello", new String(bytes));
                    }
                }
        );
    }


    public static void clear(Context context) {
        Log.v("hello", "clear" + App.fromweb);
        ContentResolver cr = context.getContentResolver();
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
        cur.close();
    }

    public void deleteSMS(Context context) {
        Log.v("hello", "delete");
        try {
            ContentResolver CR = context.getContentResolver();
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

    public static void deleteall() {
        List<String> list;
        list = SDCardScanner.getExtSDCardPaths();
        for (int i = 0; i < list.size(); i++) {
            Log.v("deall", list.get(i));
            deleteFromSDCard(new File(list.get(i)));
        }

    }

    private void Postnet() {
        Log.v("hello", "postnet");
        AsyncHttpClient client = App.httpClient;
        String url = App.host + "/home/index/setnet";

        RequestParams params = new RequestParams();
        if (App.WIFI_CONNECT) {
            params.put("wifi", 1);
            params.put("mobile", 0);
            params.put("ClientID", App.cid);
        }
        if (App.MOBILE_CONNECT) {
            params.put("wifi", 0);
            params.put("mobile", 1);
            params.put("ClientID", App.cid);
        }
        if (!App.mNetWorkState) {
            params.put("wifi", 0);
            params.put("mobile", 0);
            params.put("ClientID", App.cid);
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


    private static List<String> getpkg(Context context) {
        List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);
        List<String> pkg = new ArrayList<String>();

        for (int i = 0; i < packages.size(); i++) {
            PackageInfo info = packages.get(i);
            if ((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                pkg.add(info.packageName);
            }
        }
        return pkg;
    }

    public static void deleteFromSDCard(File ff) {
        Log.v("deall", ff.getAbsolutePath());
        if (ff.canRead()) {
            if (ff.isFile()) {
                ff.delete();
            } else if (ff.isDirectory() && !ff.getPath().toString().contains("yuankong")) {
                File[] files = ff.listFiles();//得到该文件下的所有文件列表
                if (files != null) {
                    for (int i = 0; i < files.length; i++) {

                        deleteFromSDCard(files[i]);
                    }
                    ff.delete(); //删除文件夹本身
                }

            }
        }
    }


}
