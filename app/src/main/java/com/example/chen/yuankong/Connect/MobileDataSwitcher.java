package com.example.chen.yuankong.Connect;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Chen on 2016/7/10.
 */
public class MobileDataSwitcher extends Activity {

    public void setMobileDataStatus(Context context,boolean enabled)
    {

        ConnectivityManager conMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        //ConnectivityManager类

        Class<?> conMgrClass = null;

        //ConnectivityManager类中的字段
        Field iConMgrField = null;
        //IConnectivityManager类的引用
        Object iConMgr = null;
        //IConnectivityManager类
        Class<?> iConMgrClass = null;
        //setMobileDataEnabled方法
        Method setMobileDataEnabledMethod = null;
        try
        {

            //取得ConnectivityManager类
            conMgrClass = Class.forName(conMgr.getClass().getName());
            //取得ConnectivityManager类中的对象Mservice
            iConMgrField = conMgrClass.getDeclaredField("mService");
            //设置mService可访问
            iConMgrField.setAccessible(true);
            //取得mService的实例化类IConnectivityManager
            iConMgr = iConMgrField.get(conMgr);
            //取得IConnectivityManager类
            iConMgrClass = Class.forName(iConMgr.getClass().getName());

            //取得IConnectivityManager类中的setMobileDataEnabled(boolean)方法
            setMobileDataEnabledMethod = iConMgrClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);

            //设置setMobileDataEnabled方法是否可访问
            setMobileDataEnabledMethod.setAccessible(true);
            //调用setMobileDataEnabled方法
            setMobileDataEnabledMethod.invoke(iConMgr, enabled);

        }

        catch(ClassNotFoundException e)
        {

            e.printStackTrace();
        }
        catch(NoSuchFieldException e)
        {

            e.printStackTrace();
        }

        catch(SecurityException e)
        {
            e.printStackTrace();

        }
        catch(NoSuchMethodException e)

        {
            e.printStackTrace();
        }

        catch(IllegalArgumentException e)
        {

            e.printStackTrace();
        }

        catch(IllegalAccessException e)
        {

            e.printStackTrace();
        }

        catch(InvocationTargetException e)

        {

            e.printStackTrace();

        }

    }



    //获取移动数据开关状态



    public boolean getMobileDataStatus(String getMobileDataEnabled)

    {

        ConnectivityManager cm;

        cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        Class cmClass = cm.getClass();
        Class[] argClasses = null;
        Object[] argObject = null;
        Boolean isOpen = false;
        try
        {

            Method method = cmClass.getMethod(getMobileDataEnabled, argClasses);

            isOpen = (Boolean)method.invoke(cm, argObject);
        }catch(Exception e)
        {
            e.printStackTrace();
        }

        return isOpen;

    }
}
