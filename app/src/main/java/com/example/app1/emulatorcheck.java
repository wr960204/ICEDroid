package com.example.app1;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

public class emulatorcheck {
    //模拟器检测
    //检查设备指纹
    public boolean checkfingerprint(){
        boolean flag = false;
        try {
            @SuppressLint("PrivateApi") Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);

            String[] properties = {
                    "ro.build.fingerprint",
                    "ro.odm.build.fingerprint",
                    "ro.product.build.fingerprint",
                    "ro.system_ext.build.fingerprint",
                    "ro.system.build.fingerprint",
                    "ro.vendor.build.fingerprint",
                    "ro.build.description",

                    "ro.build.date",
                    "ro.build.date.utc",
            };

            for (String property : properties) {
                String value = (String) get.invoke(c, property);
                if(Objects.equals(value, "")){
                    continue;
                }
                assert value != null;
                if (value.contains("x86")) {
                    System.out.println(value);
                    flag = true;
                }
            }
            return flag;
        } catch (Exception e) {
            Log.w("getSystemPropertiesException",  e.getMessage(), e);
        }
        return flag;
    }
    //检查设备属性
    public boolean checkBuild() {
        String board = Build.BOARD;
        String brand = Build.BRAND;
        String abi = Build.CPU_ABI;
        String device = Build.DEVICE;
        String model = Build.MODEL;
        String product = Build.PRODUCT;
        String support = Arrays.toString(Build.SUPPORTED_ABIS);
        return  board.contains("x86") ||
                brand.contains("generic") ||
                abi.contains("x86") ||
                device.contains("x86")||device.contains("generic") ||
                model.contains("Genymotion") || model.contains("x86") ||
                product.contains("sdk") || product.contains("x86") ||
                support.contains("x86") || !support.contains("arm");
    }
    //检查硬件特征
    public boolean checkHardwareFeatures(Context context) {
        PackageManager pm = context.getPackageManager();
        boolean hasTelephony = pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
        boolean hasSensor = pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER);
        return !hasTelephony || !hasSensor; // 模拟器可能缺少这些特征
    }
    //检查文件系统
    public boolean checkFileSystem() {
        String[] emulatorFiles = {
                "/dev/socket/qemud",
                "/dev/qemu_pipe",
        };
        for (String filePath : emulatorFiles) {
            if (new File(filePath).exists()) {
                System.out.println(filePath);
                return true; // 找到模拟器特有文件
            }
        }
        return false; // 没有找到模拟器特有文件
    }
    //检查运行程序
    public boolean checkRunningApps() {
        try {
            Process process = Runtime.getRuntime().exec("ps");
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
                if (line.contains("emulator")) {
                    return true; // 找到模拟器相关进程
                }
            }
        } catch (IOException e) {
            System.out.println("f");
            return false; // 读取进程信息失败
        }
        return false;
    }
    //检查电池状态
    public boolean checkBattery(Context context) {
        // 获取电池状态
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        if (batteryStatus != null) {
            int batteryLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int batteryScale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            float batteryPct = batteryLevel * 100 / (float)batteryScale;
            System.out.println(batteryPct);
            boolean isCharging = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1) == BatteryManager.BATTERY_STATUS_CHARGING;
            // 判断电池电量和充电状态，通常模拟器电池满电且不在充电
            return batteryPct == 80.0 && !isCharging; // 可能在模拟器中
        }
        return false; // 不在模拟器中
    }
}
