package com.example.app1;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class result {
    //-----------------------------------------------ROOT检测------------------------------------------------------
    public String rootCheck() throws IOException {
        rootcheck rc = new rootcheck();

        StringBuilder s = new StringBuilder("root检测:");
        boolean flag = false;
        //检查SU命令
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
        emulatorcheck ec = new emulatorcheck();

        StringBuilder s = new StringBuilder("模拟器检测：");
        boolean flag = false;
        //检查设备指纹
        if (ec.checkfingerprint()){
            s.append("\n检查设备指纹:异常");
            flag = true;
        }else {
            s.append("\n检查设备指纹:正常");
        }
        //检查设备属性
        if (ec.checkBuild()){
            s.append("\n检查设备属性:异常");
            flag = true;
        }else {
            s.append("\n检查设备属性:正常");
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
        fingerprint fp = new fingerprint();
        fingerprintjni j = new fingerprintjni();

        StringBuilder s = new StringBuilder("设备指纹检测：");
        //java层
        String dev =fp.getDeviceID(context.getContentResolver());
        String net = fp.getLocalMacAddress();
        String sys = fp.getSystemProperties();
        //native层
        String aid = j.getandroidid() + "\n";
        String fpjni = "系统指纹：" + j.fingerprint() + "\n";
        String npjni = "网络地址：\n" + j.netfp() + "\n";
        //合并结果
        s.append("\njava层检测：\n").append(dev).append(net).append(sys);
        s.append("\nnative层检测：\n").append(aid).append(fpjni).append(npjni);
        //对比结果
        s.append(compareResults(sys, fpjni));

        return s.toString();
    }

    private String compareResults(String result1, String result2) {
        StringBuilder s = new StringBuilder("\n对比结果：");
        String[] lines1 = result1.split("\n");
        String[] lines2 = result2.split("\n");
        StringBuilder result = getStringBuilder(lines1, lines2);
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

    private @NonNull StringBuilder getStringBuilder(String[] lines1, String[] lines2) {
        StringBuilder result = new StringBuilder("\n以下属性存在不同：\n");
        //找到最大的行数
        int maxLines = Math.max(lines1.length, lines2.length);
        for (int i = 0; i < maxLines; i++) {
            String line1 = i < lines1.length ? lines1[i] : "无结果";  // 如果行不存在，则显示“无结果”
            String line2 = i < lines2.length ? lines2[i] : "无结果";  // 如果行不存在，则显示“无结果”
            String[] parts1 = line1.split(":", 2);
            String[] parts2 = line2.split(":", 2);
            //默认值
            String key1 = parts1.length > 0 ? parts1[0].trim() : "无结果";
            String value1 = parts1.length > 1 ? parts1[1].trim() : "无结果";
            String value2 = parts2.length > 1 ? parts2[1].trim() : "无结果";
            //对比结果
            if(!value1.equals(value2)){
                String r = key1 + "：\n" + value1 +"\n" + value2 +"\n";
                result.append(r);
            }
        }
        return result;
    }

    private boolean areValuesIdentical(String[] data) {
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
        hookcheck hc = new hookcheck();
        fingerprintjni j = new fingerprintjni();

        StringBuilder s = new StringBuilder("hook检测：");
        //java层
        String cf = hc.checkfrida();
        //native层
        String jc = j.check();
        String jm = j.mapscheck();
        //合并结果
        s.append("\nJava层检测：\n");
        s.append(cf).append("\n");
        s.append("\nnative层检测：\n");
        s.append(jc).append("\n").append(jm).append("\n");

        return s.toString();
    }


    //-----------------------------------------------native检测方法------------------------------------------------------
    public String fingerprintjni(){
        fingerprintjni j = new fingerprintjni();

        StringBuilder s = new StringBuilder("native层检测：\n");
        String fingerprint = "系统指纹：" + j.fingerprint() + "\n";
        String netaddress = "\n网络地址：\n" + j.netfp() + "\n";
        String hookcheck = "\nhook检测：\n" + j.check() + "\n" + j.mapscheck() + "\n";
        String appnames = "\n检测已安装应用：\n" + j.getappnames() + "\n";
        String cert = "\n检测CA证书：\n" + j.getcertificate() + "\n";
        String features = "\n检测支持软硬件：\n" + j.getdevicefeatures() + "\n";
        s.append(fingerprint).append(netaddress).append(hookcheck).append(appnames).append(cert).append(features);

        return s.toString();
    }

    //------------------------------------------测试-------------------------------------------------------------------
    public String test(Context context){
        fptest fp = new fptest();

        StringBuilder fs = new StringBuilder("test：\n");
        List<String> p = fp.getProperties(context);
        for (String property : p){
            fs.append(property).append("\n");
        }

        return fs.toString();
    }

    //------------------------------------------获取已安装应用----------------------------------------------------------
    public String appname(Context context){
        appname an = new appname();
        fingerprintjni j = new fingerprintjni();

        StringBuilder name = new StringBuilder("获取已安装应用：");
        //java层
        String javaan = an.getAllAppNames(context);
        //native层
        String nativean = j.getappnames();
        //合并结果
        name.append("\njava层检测：\n").append(javaan);
        name.append("\nnative层检测：\n").append(nativean);

        return name.toString();
    }

    //------------------------------------------获取系统证书----------------------------------------------------------
    public String certinfo(){
        certificate c = new certificate();
        fingerprintjni j = new fingerprintjni();

        StringBuilder cert = new StringBuilder("获取CA证书：");
        //java层
        String jc = c.listInstalledCertificates();
        //native层
        String nc = j.getcertificate();
        //合并结果
        cert.append("\njava层检测：\n").append(jc);
        cert.append("\nnative层检测：\n").append(nc);

        return cert.toString();
    }

    //------------------------------------------获取支持软硬件----------------------------------------------------------
    public String devicefeatures(Context context){
        devicesfeatures df = new devicesfeatures();
        fingerprintjni j = new fingerprintjni();

        StringBuilder features = new StringBuilder("获取支持软硬件：");
        //java层
        String jf = df.features(context);
        //native层
        String nf = j.getdevicefeatures();
        //合并结果
        features.append("\njava层检测：\n").append(jf);
        features.append("\nnative层检测：\n").append(nf);

        return features.toString();
    }

    //------------------------------------------汇总----------------------------------------------------------
    public String total(Context context) throws IOException {
        StringBuilder t = new StringBuilder();
        String rc = rootCheck();
        String ec = emulatorCheck(context);
        String fc = checkFingerPrint(context);
        String hc = checkhook();
        String tc = test(context);
        String an = appname(context);
        String ct = certinfo();
        String df = devicefeatures(context);

        t.append(rc).append(ec).append(fc).append(hc).append(tc).append(an).append(ct).append(df);

        return t.toString();
    }


}
