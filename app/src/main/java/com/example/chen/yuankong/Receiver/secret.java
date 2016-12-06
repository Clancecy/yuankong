package com.example.chen.yuankong.Receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings;

import com.example.chen.yuankong.indexActivity;


/**
 * Created by Chen on 2016/5/16.
 */
public class secret extends BroadcastReceiver {


    public static final String SECRET_CODE_ACTION = "android.provider.Telephony.SECRET_CODE";
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(SECRET_CODE_ACTION)) {
            String host = intent.getData() != null ? intent.getData().getHost() : null;
            if (host.equals("0056")) {
                PackageManager pm = context.getPackageManager();
                ComponentName name = new ComponentName(context, indexActivity.class);
                pm.setComponentEnabledSetting(name, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED);
                Intent it = new Intent(context, indexActivity.class);
                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(it);
            }
        }
    }
}
