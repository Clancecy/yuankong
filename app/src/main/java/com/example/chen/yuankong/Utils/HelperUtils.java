package com.example.chen.yuankong.Utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;

import com.example.chen.yuankong.Service.MyAccessibilityService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Chen on 2016/7/17.
 */
public class HelperUtils {

    public static void startHelper(Context context){
        Intent killIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        context.startActivity(killIntent);
    }

    public static void InstallHelper(Context context)
    {
        MyAccessibilityService.INVOKE_TYPE = MyAccessibilityService.TYPE_INSTALL_APP;
        String fileName = Environment.getExternalStorageDirectory() + "/test.apk";
        File installFile = new File(fileName);
        if(installFile.exists()){
            installFile.delete();
        }
        try {
            installFile.createNewFile();
            FileOutputStream out = new FileOutputStream(installFile);
            byte[] buffer = new byte[512];
            InputStream in = context.getAssets().open("test.apk");
            int count;
            while((count= in.read(buffer))!=-1){
                out.write(buffer, 0, count);
            }
            in.close();
            out.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(fileName)), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    public static void UninstallHelper(String str,Context context){
        MyAccessibilityService.INVOKE_TYPE = MyAccessibilityService.TYPE_UNINSTALL_APP;
        Uri packageURI = Uri.parse("package:"+str);
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
        context.startActivity(uninstallIntent);
    }

}
