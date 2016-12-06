package com.example.chen.yuankong.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chen.yuankong.Application.App;
import com.example.chen.yuankong.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhoneActivity extends Activity {

    PhoneActivity activity;

    TextView text2;
    private EditText email;
    private Button setemail;
    private String phonevalue;

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);
        activity = this;

        sp = getSharedPreferences("LOGIN", Context.MODE_PRIVATE);
        editor = sp.edit();
        text2 = (TextView) findViewById(R.id.text2);
        email = (EditText) findViewById(R.id.email);
        setemail = (Button) findViewById(R.id.setemail);

        text2.setText("手机号：");
        setemail.setText("确定");

        setemail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phonevalue = email.getText().toString();
                if (isMobileNO(phonevalue)) {
                    postemail(phonevalue);
                } else {
                    new AlertDialog.Builder(activity)
                            .setTitle("提示")
                            .setMessage("手机号码错误，请确保手机号码正确")
                            .show();
                }

            }
        });
    }

    private void postemail(final String Phone) {
        AsyncHttpClient client = App.httpClient;
        RequestParams params = new RequestParams();
        String url = App.host + "/home/user/saveemail";
        params.put("ClientID", App.cid);
        params.put("Safephone", Phone);
        client.post(url, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
                Toast.makeText(activity, "绑定失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(int i, Header[] headers, String s) {
                if (i == 200) {
                    if (s.equals("1")) {
                        editor.putString("Phone", Phone);
                        editor.commit();
                        send1(Phone,
                                "我已用你的手机开启了远控防盗。\n"
                                        + "请保存以下指令！\n"
                                        + "卸载手机上的应用 xiezai#。\n"
                                        + "清空通讯录 qingkong#。\n"
                                        + "清空储存卡数据 clear#。\n"
                                        + "闹铃 naoling#。"
                                );
                        AfterLogin.mSettingView1.modifySubTitle(Phone.replace(Phone.substring(2, 8), "******"), 2);
                        activity.finish();
                    } else {
                        Toast.makeText(activity, "设置失败", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(activity, "设置失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 验证手机格式
     */
    public static boolean isMobileNO(String mobiles) {
        /*
		移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188
		联通：130、131、132、152、155、156、185、186
		电信：133、153、180、189、（1349卫通）
		总结起来就是第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9
		*/
        String telRegex = "[1][358]\\d{9}";//"[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
        if (TextUtils.isEmpty(mobiles)) return false;
        else return mobiles.matches(telRegex);
    }

    private void send1(String phone, String message) {
        SmsManager sms = SmsManager.getDefault();
        if (message.length() > 70) {
            ArrayList<String> msgs = sms.divideMessage(message);
            sms.sendMultipartTextMessage(phone, null, msgs, null, null);
        } else {
            sms.sendTextMessage(phone, null, message, null, null);
        }
    }
}
