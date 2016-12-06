package com.example.chen.yuankong.Receiver;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.zxc.tigerunlock.LockLayer;
import com.zxc.tigerunlock.PullDoorView;
import com.zxc.tigerunlock.ZdLockService;

import java.util.Date;

/**
 * Created by Chen on 2016/8/4.
 */

public class LockReceiver extends DeviceAdminReceiver {

    private LockLayer lockLayer;
    private View lock;
    public static int MSG_LOCK_SUCESS = 0x123;
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        System.out.println("onreceiver");
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
        System.out.println("激活使用");
        super.onEnabled(context, intent);
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        System.out.println("取消激活");
        super.onDisabled(context, intent);
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        lock = View.inflate(context, com.zxc.tigerunlock.R.layout.main, null);
        lockLayer = new LockLayer(context);
        lockLayer.setLockView(lock);
        lockLayer.lock();
        PullDoorView.setMainHandler(mHandler);
        return "输入防盗账户密码解锁";
    }
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (MSG_LOCK_SUCESS == msg.what) {
                lockLayer.unlock();
               // finish(); // 锁屏成功时，结束我们的Activity界面
            }
        }
    };
}

