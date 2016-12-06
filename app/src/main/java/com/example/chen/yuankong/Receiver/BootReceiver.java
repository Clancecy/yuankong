package com.example.chen.yuankong.Receiver;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.example.chen.yuankong.indexActivity;
import com.igexin.sdk.PushManager;
import com.igexin.sdk.PushService;
import com.igexin.sdk.PushServiceUser;

/**
 * Created by Chen on 2016/7/13.
 */
public class BootReceiver extends BroadcastReceiver {

    static final String action_boot = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(action_boot)) {
        }

    }
}
