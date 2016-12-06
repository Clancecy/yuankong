package com.example.chen.yuankong.Activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.chen.yuankong.R;

public class yidaoActivity extends Activity {

    Button login;
    Button reg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yidao);
        login= (Button)findViewById(R.id.login);
        reg=(Button)findViewById(R.id.reg);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(yidaoActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(yidaoActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
