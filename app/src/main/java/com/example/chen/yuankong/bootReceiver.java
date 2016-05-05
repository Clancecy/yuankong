package com.example.chen.yuankong;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

//开机自启动广播
public class bootReceiver extends BroadcastReceiver
{
	private Intent intent_service_boot;
	@Override
	public void onReceive(Context context, Intent intent)
	{
		// TODO Auto-generated method stub
		intent_service_boot=new Intent(context, mainControlService.class);
		context.startService(intent_service_boot);
	}

}
