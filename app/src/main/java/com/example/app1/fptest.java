package com.example.app1;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class fptest {
    public List<String> getProperties(Context context) {
        List<String> properties = new ArrayList<>();

        int availableCores = Runtime.getRuntime().availableProcessors();
        properties.add("可⽤处理器核⼼数：" + availableCores);
        properties.addAll(developmentSettings(context));
        checkBattery(context);

        return getStrings(properties);
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

    public void checkBattery(Context context) {
        // 获取电池状态
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        if (batteryStatus != null) {
            int batteryLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int batteryScale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            float batteryPct = batteryLevel * 100 / (float)batteryScale;
            System.out.println(batteryPct);
            boolean isCharging = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1) == BatteryManager.BATTERY_STATUS_CHARGING;
            boolean usbCharge = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) == BatteryManager.BATTERY_PLUGGED_USB;
            boolean acCharge = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) == BatteryManager.BATTERY_PLUGGED_AC;
            System.out.println(isCharging);
            System.out.println(usbCharge);
            System.out.println(acCharge);
        }

    }





}
