package com.example.chen.yuankong.Activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.OperationApplicationException;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.chen.yuankong.Application.App;
import com.example.chen.yuankong.R;
import com.example.chen.yuankong.Utils.Tb_contacts;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class daoruActivity extends Activity {
    Button btn;
    List<Tb_contacts> contactslist;
    static daoruActivity m_ac;
    static ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daoru);
        m_ac = this;
        dialog = new ProgressDialog(this);
        dialog.setTitle("提示");
        dialog.setMessage("正在导入,请稍后...");
        dialog.setCancelable(false);
        btn = (Button) findViewById(R.id.btn_5);
        contactslist = new ArrayList<Tb_contacts>();

        getcontacts();
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("hello", "clisize" + contactslist.size());
                add();
            }
        });
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    btn.setText("导入完成");
                    dialog.dismiss();
                    Log.v("hello", "okokok");
            }
        }
    };

    void getcontacts() {
        AsyncHttpClient httpClient = App.httpClient;
        String url = App.host + "/home/user/getpcontact";
        RequestParams params = new RequestParams();
        params.put("ClientID", App.cid);
        httpClient.post(url, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int i, Header[] headers, String s, Throwable throwable) {

            }

            @Override
            public void onSuccess(int i, Header[] headers, String s) {
                if (i == 200) {
                    try {
                        org.json.JSONArray array = new org.json.JSONArray(s);
                        org.json.JSONObject ob1 = array.getJSONObject(0);
                        String str1 = ob1.getString("contact");
                        Log.v("hello", "size" + array.length());
                        Log.v("hello", "con" + str1);
                        for (int f = 0; f < array.length(); f++) {
                            org.json.JSONObject ob = array.getJSONObject(f);
                            String str = ob.getString("contact");
                            Log.v("hello", "str :" + str);
                            String[] c = str.split(",");
                           if(c.length==1) {
                               Tb_contacts tb=new Tb_contacts(c[0],"");
                               contactslist.add(tb);
                           }else{
                               Tb_contacts tb=new Tb_contacts(c[0],c[1]);
                               contactslist.add(tb);
                           }
                        }
                    } catch (JSONException e) {
                    }
                }
            }
        });
    }

    public void add() {
        dialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BatchAddContact(contactslist);
                } catch (Exception e) {
                }

                Message msg = new Message();
                msg.what = 1;
                handler.sendMessage(msg);
            }
        }).start();
    }

    public void BatchAddContact(List<Tb_contacts> list)
            throws RemoteException, OperationApplicationException {
        Log.v("hello", "list" + list.size());
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        int rawContactInsertIndex = 0;
        for (Tb_contacts contact : list) {
            rawContactInsertIndex = ops.size(); // 有了它才能给真正的实现批量添加

            ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .withYieldAllowed(true).build());
            Log.v("hello", "TB" + contact.toString());
            // 添加姓名
            ops.add(ContentProviderOperation
                    .newInsert(
                            android.provider.ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.RawContacts.Data.RAW_CONTACT_ID,
                            rawContactInsertIndex)
                    .withValue(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contact.getName())
                    .withYieldAllowed(true).build());
            // 添加号码
            ops.add(ContentProviderOperation
                    .newInsert(
                            android.provider.ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.RawContacts.Data.RAW_CONTACT_ID,
                            rawContactInsertIndex)
                    .withValue(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contact.getNumber())
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.LABEL, "").withYieldAllowed(true).build());
        }
        if (ops != null) {
            // 真正添加
            ContentProviderResult[] results = m_ac.getContentResolver()
                    .applyBatch(ContactsContract.AUTHORITY, ops);
            for (ContentProviderResult result : results) {
                Log.v("hello", "content" + result.uri.toString());
            }
        }
    }
}
