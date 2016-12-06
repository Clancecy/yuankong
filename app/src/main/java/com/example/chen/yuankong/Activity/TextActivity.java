package com.example.chen.yuankong.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chen.yuankong.R;

public class TextActivity extends Activity {
    final Context context = this;
    Button Change;
    AutoAjustSizeTextView text;

    private SharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);
        sp = getSharedPreferences("LOGIN", Context.MODE_PRIVATE);

        Change=(Button)findViewById(R.id.change);
        text=(AutoAjustSizeTextView)findViewById(R.id.mytext);

        String Email=sp.getString("Email","242564764753688");
        text.setText(Email.replace(Email.substring(2, 8), "******"));
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
                                            Intent intent = new Intent(TextActivity.this, EmailActivity.class);
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
