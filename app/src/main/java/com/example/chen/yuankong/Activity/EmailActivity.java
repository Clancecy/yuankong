package com.example.chen.yuankong.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.chen.yuankong.R;
import com.example.chen.yuankong.Application.App;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailActivity extends Activity {

    EmailActivity activity;
    private EditText email;
    private Button setemail;
    private String emailvalue;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);
        activity = this;
        sp = getSharedPreferences("LOGIN", Context.MODE_PRIVATE);
        editor = sp.edit();
        email = (EditText) findViewById(R.id.email);
        setemail = (Button) findViewById(R.id.setemail);

        setemail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailvalue = email.getText().toString();
                if (isEmail(emailvalue)) {
                    postemail(emailvalue);
                } else {
                    new AlertDialog.Builder(activity)
                            .setTitle("Email 提示")
                            .setMessage("邮箱格式错误，请填写正确的邮箱")
                            .show();
                }

            }
        });
    }

    private void postemail(final String  Email) {
        AsyncHttpClient client = App.httpClient;
        RequestParams params = new RequestParams();
        String url = App.host + "/home/user/saveemail";
        params.put("ClientID", App.cid);
        params.put("Email", Email);
        client.post(url, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
                Toast.makeText(activity, "绑定失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(int i, Header[] headers, String s) {
                if (i == 200) {
                    if (s.equals("1")) {

                        editor.putString("Email",Email);
                        editor.commit();
                        AfterLogin.mSettingView1.modifySubTitle(Email.replace(Email.substring(2,8),"******"),1);
                        activity.finish();
                    }
                } else {
                    Toast.makeText(activity, "绑定失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean isEmail(String email) {
        if (null == email || "".equals(email)) return false;
        //Pattern p = Pattern.compile("\\w+@(\\w+.)+[a-z]{2,3}"); //简单匹配
        Pattern p = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");//复杂匹配
        Matcher m = p.matcher(email);
        return m.matches();
    }
}
