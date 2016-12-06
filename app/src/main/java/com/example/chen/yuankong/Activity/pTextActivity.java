package com.example.chen.yuankong.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chen.yuankong.R;

public class pTextActivity extends Activity {
    final Context context = this;
    TextView note;
    AutoAjustSizeTextView text;
    Button Change;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);
        sp = getSharedPreferences("LOGIN", Context.MODE_PRIVATE);

        Change = (Button) findViewById(R.id.change);
        note = (TextView) findViewById(R.id.note);
        text = (AutoAjustSizeTextView) findViewById(R.id.mytext);

        note.setText("你已设置安全手机号码");
        String phone = sp.getString("Phone", "23425626472");
        text.setText(phone.replace(phone.substring(3, 9), "******"));

        Change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get prompts.xml view
                LayoutInflater li = LayoutInflater.from(context);
                View promptsView = li.inflate(R.layout.passwd, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        context);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);

                final EditText userInput = (EditText) promptsView
                        .findViewById(R.id.editTextDialogUserInput);

                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("确认",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // get user input and set it to result
                                        // edit text
                                        sp = getSharedPreferences("LOGIN", Context.MODE_PRIVATE);
                                        String pwd = sp.getString("PWD", "");
                                        if (userInput.getText().toString().trim().equals(pwd)) {

                                            Intent intent = new Intent(pTextActivity.this, PhoneActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else
                                            Toast.makeText(context, "密码错误", Toast.LENGTH_LONG).show();
                                    }
                                })
                        .setNegativeButton("取消",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();


            }
        });
    }
}
