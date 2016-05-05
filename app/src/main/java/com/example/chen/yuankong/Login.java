package com.example.chen.yuankong;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;


public class Login extends Activity {


    DoubleClickExitHelper doubleClick = new DoubleClickExitHelper(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            return doubleClick.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }
}
