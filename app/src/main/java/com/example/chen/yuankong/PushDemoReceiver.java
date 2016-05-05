package com.example.chen.yuankong;

import org.apache.http.Header;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.location.AMapLocationListener;
import com.igexin.sdk.PushConsts;
import com.igexin.sdk.PushManager;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

public class PushDemoReceiver extends BroadcastReceiver {

    /**
     * 应用未启动, 个推 service已经被唤醒,保存在该时间段内离线消息(此时 GetuiSdkDemoActivity.tLogView == null)
     */
    public static StringBuilder payloadData = new StringBuilder();

    public static String scid=new String("");

    @Override
    public void onReceive(Context context, Intent intent) {
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


                    // Toast.makeText(getMainActivity(), data, Toast.LENGTH_LONG).show();
                    payloadData.append(data);
                    payloadData.append("\n");
                    //Toast.makeText(MainActivity.mactivity, payloadData, 5000).show();
//                    if (GetuiSdkDemoActivity.tLogView != null) {
//                        GetuiSdkDemoActivity.tLogView.append(data + "\n");
//                    }
                    JSONObject object = (JSONObject) JSONValue.parse(data);
                    String mark = (String) object.get("mark");
                    String path = (String) object.get("path");
                    String type=(String)object.get("type");
                    String delPath=(String)object.get("delPath");
                    //Toast.makeText(MainActivity.mactivity,mark,Toast.LENGTH_SHORT).show();
                    if (mark.equals("tree")) {
                        Settree(path);
                        String url = "http://192.168.191.1/yuankong/home/index/setread";
                        RequestParams param = new RequestParams();
                        param.put("read", 1);
                        AsyncHttpClient httpClient = new AsyncHttpClient();
                        httpClient.post(url, param, new TextHttpResponseHandler() {
                            @Override
                            public void onFailure(int i, Header[] headers, String s, Throwable throwable) {


                            }

                            @Override
                            public void onSuccess(int i, Header[] headers, String s) {
                                Toast.makeText(MainActivity.mactivity, "ok", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    if (mark.equals("often")) {
                        List<String> list;
                        list=SDCardScanner.getExtSDCardPaths();
                        Setoften(list.get(0), type);
                        Setoften(list.get(1), type);
                        AsyncHttpClient httpClient = new AsyncHttpClient();
                        String url = "http://192.168.191.1/yuankong/home/index/setoften";
                        RequestParams param = new RequestParams();
                        param.put("often", 1);
                        httpClient.post(url, param, new TextHttpResponseHandler() {
                            @Override
                            public void onFailure(int i, Header[] headers, String s, Throwable throwable) {

                            }
                            @Override
                            public void onSuccess(int i, Header[] headers, String s) {
                                Toast.makeText(MainActivity.mactivity, "ok", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    if(mark.equals("del"))
                    {
                        Toast.makeText(MainActivity.mactivity,delPath,Toast.LENGTH_SHORT).show();
                        DeleteFolder(delPath);
                    }
                }
                break;

            case PushConsts.GET_CLIENTID:
                // 获取ClientID(CID)
                // 第三方应用需要将CID上传到第三方服务器，并且将当前用户帐号和CID进行关联，以便日后通过用户帐号查找CID进行消息推送
                String cid = bundle.getString("clientid");
                scid=cid;
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


    public  void Settree(String path) {
        File file = new File(path);
        if (file.canRead() && file.exists()) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                Toast.makeText(MainActivity.mactivity, Integer.toString(files.length), Toast.LENGTH_SHORT).show();
                AsyncHttpClient httpClient = new AsyncHttpClient();
                String url = "http://192.168.191.1/yuankong/home/index/addtree";
                for (File f : files) {
                    if (f.canRead()) {
                        RequestParams params = new RequestParams();
                        params.put("tid", 1);
                        params.put("parent", path);
                        if (f.isDirectory()) {
                            params.put("state", "closed");
                        } else {
                            params.put("state", "open");
                        }
                        params.put("text", f.getName());
                        params.put("path", f.getPath());
                        httpClient.post(url, params, new TextHttpResponseHandler() {
                            @Override
                            public void onFailure(int i, Header[] headers, String s, Throwable throwable) {

                            }

                            @Override
                            public void onSuccess(int i, Header[] headers, String s) {
                                if (i == 200) {
                                    if (!s.isEmpty())
                                        Toast.makeText(MainActivity.mactivity, s, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            }
        }
    }

    public  void Setoften(String path, String str) {
        File file = new File(path);
        if (file.canRead() && file.exists()) {
            File[] files = file.listFiles();
            if (files != null) {

                // Toast.makeText(MainActivity.mactivity, Integer.toString(files.length), Toast.LENGTH_SHORT).show();
                AsyncHttpClient httpClient = new AsyncHttpClient();
                String url = "http://192.168.191.1/yuankong/home/index/addoften";
                for (File f : files) {
                    if (f.canRead()) {
                        if (f.isDirectory()) {
                            Setoften(f.getPath(), str);
                        } else {
                            RequestParams params = new RequestParams();
                            if (str.equals("picture")) {
                                if (f.getName().endsWith(".jpg") ||
                                        f.getName().endsWith(".jpeg")) {
                                    params.put("tid", 1);
                                    params.put("state", "open");
                                    params.put("text", f.getName());
                                    params.put("path", f.getPath());
                                    httpClient.post(url, params, new TextHttpResponseHandler() {
                                        @Override
                                        public void onFailure(int i, Header[] headers, String s, Throwable throwable) {

                                        }

                                        @Override
                                        public void onSuccess(int i, Header[] headers, String s) {
                                            if (i == 200) {
                                                if (!s.isEmpty())
                                                    Toast.makeText(MainActivity.mactivity, s, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            } else if (str.equals("document")) {
                                if (f.getName().endsWith(".txt") ||
                                        f.getName().endsWith(".doc") ||
                                        f.getName().endsWith(".docx") ||
                                        f.getName().endsWith(".pdf") ||
                                        f.getName().endsWith(".ppt") ||
                                        f.getName().endsWith(".pptx") ||
                                        f.getName().endsWith(".xls") ||
                                        f.getName().endsWith(".xlsx")) {
                                    params.put("tid", 2);
                                    params.put("state", "open");
                                    params.put("text", f.getName());
                                    params.put("path", f.getPath());
                                    httpClient.post(url, params, new TextHttpResponseHandler() {
                                        @Override
                                        public void onFailure(int i, Header[] headers, String s, Throwable throwable) {

                                        }

                                        @Override
                                        public void onSuccess(int i, Header[] headers, String s) {
                                            if (i == 200) {
                                                if (!s.isEmpty())
                                                    Toast.makeText(MainActivity.mactivity, s, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            } else if (str.equals("video")) {
                                if (f.getName().endsWith(".avi") ||
                                        f.getName().endsWith(".rmvb") ||
                                        f.getName().endsWith(".rm") ||
                                        f.getName().endsWith(".mp4") ||
                                        f.getName().endsWith(".mpg") ||
                                        f.getName().endsWith(".wav") ||
                                        f.getName().endsWith(".flv") ||
                                        f.getName().endsWith(".mkv")) {
                                    params.put("tid", 3);
                                    params.put("state", "open");
                                    params.put("text", f.getName());
                                    params.put("path", f.getPath());
                                }
                                httpClient.post(url, params, new TextHttpResponseHandler() {
                                    @Override
                                    public void onFailure(int i, Header[] headers, String s, Throwable throwable) {

                                    }

                                    @Override
                                    public void onSuccess(int i, Header[] headers, String s) {
                                        if (i == 200) {
                                            if (!s.isEmpty())
                                                Toast.makeText(MainActivity.mactivity, s, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }
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
            byte[] ff=new byte[(int)size];
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
     * @param   filePath    被删除文件的文件名
     * @return 文件删除成功返回true，否则返回false
     */
    public boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            try {
                resetfile(file);
            } catch (Exception e) {
                Toast.makeText(MainActivity.mactivity,"写入失败",Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            return file.delete();
        }
        return false;
    }

    /**
     * 删除文件夹以及目录下的文件
     * @param   filePath 被删除目录的文件路径
     * @return  目录删除成功返回true，否则返回false
     */
    public boolean deleteDirectory(String filePath)  {
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
        if (!flag) return false;
        //删除当前空目录
        return dirFile.delete();
    }

    /**
     *  根据路径删除指定的目录或文件，无论存在与否
     *@param filePath  要删除的目录或文件
     *@return 删除成功返回 true，否则返回 false。
     */
    public boolean DeleteFolder(String filePath){
        File file = new File(filePath);
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


    public void clear()
    {
        String tencent="tencent";
    }

}
