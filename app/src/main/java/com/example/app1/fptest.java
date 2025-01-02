package com.example.app1;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class fptest {
    public List<String> getProperties(Context context) {
        List<String> properties = new ArrayList<>();

        int availableCores = Runtime.getRuntime().availableProcessors();
        properties.add("可⽤处理器核⼼数：" + availableCores);
        properties.add(getAllAppNames(context));
        properties.addAll(developmentSettings(context));

        return getStrings(properties);
    }


    public String getAllAppNames(Context context){
        PackageManager pm = context.getPackageManager();
        ////获取到所有安装了的应用程序的信息，包括那些卸载了的，但没有清除数据的应用程序
        @SuppressLint("QueryPermissionsNeeded") List<PackageInfo> list2=pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);

        int j=0;

        for (PackageInfo packageInfo : list2) {
            //得到手机上已经安装的应用的名字,即在AndriodMainfest.xml中的app_name。
            //String appName=packageInfo.applicationInfo.loadLabel(context.getPackageManager()).toString();
            //得到手机上已经安装的应用的图标,即在AndriodMainfest.xml中的icon。
            //Drawable drawable = packageInfo.applicationInfo.loadIcon(context.getPackageManager());
            //得到应用所在包的名字,即在AndriodMainfest.xml中的package的值。
            //String packageName=packageInfo.packageName;
            //Log.d("应用名", "应用的名字:"+appName);
            //Log.d("应用包名", "应用的包名字:"+packageName);
            j++;
        }
        Log.d("========", "应用的总个数:"+j);
        return "应用的总个数:"+j;
    }

    public List<String> developmentSettings(Context context){
        List<String> d = new ArrayList<>();

        int developmentSettings = Settings.Secure.getInt(context.getContentResolver(), Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0);
        if (developmentSettings == 1) {
            // 开发者选项已启⽤
            d.add("开发者选项已启⽤");
        }else {
            d.add("开发者选项未启⽤");
        }
        int adbEnabled = Settings.Global.getInt(context.getContentResolver(), Settings.Global.ADB_ENABLED, 0);
        if (adbEnabled == 1) {
            // USB调试已启⽤
            d.add("USB调试已启⽤");
        }else {
            d.add("USB调试未启⽤");
        }
        return getStrings(d);
    }

    private static @NonNull List<String> getStrings(List<String> properties) {
        List<String> processedProperties = new ArrayList<>();
        for (String property : properties) {
            String[] parts = property.split(":", 2);
            if (parts.length == 2) {
                String key = parts[0];
                String value = parts[1].trim();
                // 替换空值或unknown
                if (value.isEmpty() || value.equalsIgnoreCase("unknown")) {
                    value = "无结果";
                }
                processedProperties.add(key + ":" + value);
            } else {
                // 保留没有冒号的情况
                processedProperties.add(property);
            }
        }
        return processedProperties;
    }






}
