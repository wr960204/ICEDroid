package com.example.app1;

import android.content.Context;
import android.os.Build;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class result {
    //-----------------------------------------------ROOT检测------------------------------------------------------
    public String rootCheck() throws IOException {
        StringBuilder s = new StringBuilder("root检测:");
        boolean flag = false;
        //检查SU命令
        rootcheck rc = new rootcheck();
        if (rc.checkSuCommand()){
            s.append("\n检查SU命令:执行成功");
            flag = true;
        }else {
            s.append("\n检查SU命令:执行失败");
        }
        //检查root文件
        if (rc.checkRootFiles()){
            s.append("\n检查root文件:存在特征文件");
            flag = true;
        }else {
            s.append("\n检查root文件:不存在特征文件");
        }
        //检查系统标签
        if (rc.checkSystemTags()){
            s.append("\n检查系统标签:异常");
            flag = true;
        }else {
            s.append("\n检查系统标签:正常");
        }
        //检查系统分区
        if (rc.checkMountInfo()){
            s.append("\n检查分区读写模式:系统分区可写");
            flag = true;
        }else {
            s.append("\n检查分区读写模式:系统分区只读");
        }
        //检查系统属性
        if (rc.checkSystemProperty()){
            s.append("\n检查系统属性:异常");
            flag = true;
        }else {
            s.append("\n检查系统属性:正常");
        }
        //检查SELinux
        if (rc.checkSELinuxStatus().equalsIgnoreCase("y")){
            s.append("\n检查SELinux状态:异常");
            flag = true;
        } else if (rc.checkSELinuxStatus().equalsIgnoreCase("n")) {
            s.append("\n检查SELinux状态:正常");
        } else {
            s.append("\n检查SELinux状态:未知");
        }
        //检查bootloader
        if (rc.isBootloaderUnlocked()){
            s.append("\n检查Bootloader状态:异常");
            flag = true;
        }else {
            s.append("\n检查Bootloader状态:正常");
        }
        //检查TEE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (rc.checkTEE()){
                s.append("\n检查TEE状态:正常");

            }else {
                s.append("\n检查TEE状态:异常");
                flag = true;
            }
        }
        if(rc.checkFP()){
            s.append("\n检查系统指纹属性：一致");
        }else {
            s.append("\n检查系统指纹属性：异常");
            flag = true;
        }


        if (flag){
            s.append("\n\n可能已root");
        }else {
            s.append("\n\n可能未root");
        }
        return s.toString();

    }

    //-----------------------------------------------模拟器检测------------------------------------------------------
    public String emulatorCheck(Context context){
        StringBuilder s = new StringBuilder("模拟器检测：");
        boolean flag = false;

        emulatorcheck ec = new emulatorcheck();
        //检查设备型号和品牌
        if (ec.checkDeviceModel()){
            s.append("\n检查设备型号和品牌:异常");
            flag = true;
        }else {
            s.append("\n检查设备型号和品牌:正常");
        }
        //检查硬件特征
        if (ec.checkHardwareFeatures(context)){
            s.append("\n检查硬件特征:异常");
            flag = true;
        }else {
            s.append("\n检查硬件特征:正常");
        }
        //检查文件系统
        if (ec.checkFileSystem()){
            s.append("\n检查文件系统:存在特征文件");
            flag = true;
        }else {
            s.append("\n检查文件系统:不存在特征文件");
        }
        //检查运行程序
        if (ec.checkRunningApps()){
            s.append("\n检查运行程序:存在运行中特征程序");
            flag = true;
        }else {
            s.append("\n检查运行程序:不存在运行中特征程序");
        }
        //检查系统架构
        if (ec.checkArchitecture()){
            s.append("\n检查系统架构:x86");
            flag = true;
        }else {
            s.append("\n检查系统架构:arm");
        }
        //检查电池状态
        if (ec.checkBattery(context)){
            s.append("\n检查电池状态:异常");
            flag = true;
        }else {
            s.append("\n检查电池状态:正常");
        }

        if (flag){
            s.append("\n\n可能是模拟器");
        }else {
            s.append("\n\n可能是真机");
        }
        return s.toString();

    }

    //-----------------------------------------------设备指纹检测------------------------------------------------------
    public String checkFingerPrint(Context context) {
        StringBuilder s = new StringBuilder("设备指纹检测：");
        fingerprint fp = new fingerprint();
        String dev =fp.getDeviceID(context.getContentResolver());
        String net = fp.getLocalMacAddress();
        String sys = fp.getSystemProperties();

        fingerprintjni j = new fingerprintjni();
        String fpjni = "\n系统指纹：" + j.fingerprint() + "\n";
        String npjni = "网络地址：\n" + j.netfp() + "\n";

        s.append("\njava层检测：\n").append(dev).append(net).append(sys).append("\nnative层检测：").append(fpjni).append(npjni);
        s.append(compareResults(sys, fpjni));

        fp.getAccounts(context);
        return s.toString();
    }
    private String compareResults(String result1, String result2) {
        StringBuilder s = new StringBuilder("\n对比结果：");

        String[] lines1 = result1.split("\n");
        String[] lines2 = result2.split("\n");
        StringBuilder result = new StringBuilder("\n以下属性存在不同：\n");
        // 找到最大的行数
        int maxLines = Math.max(lines1.length, lines2.length);
        for (int i = 0; i < maxLines; i++) {
            String line1 = i < lines1.length ? lines1[i] : "无结果";  // 如果行不存在，则显示“无结果”
            String line2 = i < lines2.length ? lines2[i] : "无结果";  // 如果行不存在，则显示“无结果”
            String[] parts1 = line1.split(":", 2);
            String[] parts2 = line2.split(":", 2);
            // 默认值
            String key1 = parts1.length > 0 ? parts1[0].trim() : "无结果";
            String value1 = parts1.length > 1 ? parts1[1].trim() : "无结果";
            String value2 = parts2.length > 1 ? parts2[1].trim() : "无结果";
            // 输出对比结果
            System.out.printf("属性: %s\n", key1);
            System.out.printf("结果1: %s\n", value1);
            System.out.printf("结果2: %s\n", value2);
            System.out.println("对比结果: " + (value1.equals(value2) ? "相同" : "不同"));
            System.out.println("-------------------------------------------------");
            if(!value1.equals(value2)){
                String r = key1 + "：\n" + value1 +"\n" + value2 +"\n";
                result.append(r);
            }
        }

        String[] fingerprint = new String[14];
        System.arraycopy(lines1,2,fingerprint,0,6);
        System.arraycopy(lines2,2,fingerprint,6,6);
        System.arraycopy(lines1,lines1.length-1,fingerprint,12,1);
        System.arraycopy(lines2,lines2.length-1,fingerprint,13,1);
        System.out.println(Arrays.toString(fingerprint));
        if(areValuesIdentical(fingerprint))
            s.append("\n指纹属性一致\n");
        s.append(result);
        return s.toString();
    }
    public static boolean areValuesIdentical(String[] data) {
        Set<String> valuesSet = new HashSet<>();
        for (String entry : data) {
            // 将字符串按冒号分割，确保有两个部分
            String[] parts = entry.split(":", 2);
            // 如果分割成功，添加冒号后的值到集合中
            if (parts.length == 2) {
                String value = parts[1].trim(); // 获取冒号后的值并去除空白
                valuesSet.add(value);
            } else {
                // 如果没有冒号或格式不正确，可以选择抛出异常或添加默认值
                // 这里直接返回 false 表示数据格式不符合要求
                return false;
            }
        }
        // 如果集合的大小为 1，表示所有值相同
        return valuesSet.size() == 1;
    }

    //-----------------------------------------------hook检测方法------------------------------------------------------
    public String checkhook(){
        StringBuilder s = new StringBuilder("hook检测：\n");
        fingerprintjni j = new fingerprintjni();

        s.append("\nJava层检测：\n");
        s.append(checkfrida()).append("\n");
        s.append("\nnative层检测：\n");
        s.append(j.check()).append("\n");
        s.append(j.mapscheck()).append("\n");

        return s.toString();
    }
    public String checkfrida(){
        StringBuilder s = new StringBuilder();
        hookcheck hc = new hookcheck();

        if(hc.hasReadProcMaps("frida") | hc.mCheckFridaTcp()){
            s.append("检测到frida");
        }
        s.append("未检测到frida");
        return s.toString();
    }

    //-----------------------------------------------native检测方法------------------------------------------------------
    public String fingerprintjni(){
        StringBuilder s = new StringBuilder("native层检测：");
        fingerprintjni j = new fingerprintjni();

        String fpjni = "\n系统指纹：" + j.fingerprint() + "\n";
        String npjni = "\n网络地址：\n" + j.netfp() + "\n";
        String chjni = j.check() + "\n";
        String msjni = j.mapscheck() + "\n";
        s.append(fpjni).append(npjni).append("\nhook检测：\n").append(chjni).append(msjni);
        return s.toString();
    }

    //------------------------------------------测试---------------------------------------------------
    public String test(Context context){
        fptest fp = new fptest();
        StringBuilder fs = new StringBuilder("test：\n");
        List<String> p = fp.getProperties(context);

        for (String property : p){
            fs.append(property).append("\n");
        }

        return fs.toString();
    }




}
