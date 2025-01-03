package com.example.app1;

import android.content.Context;
import android.os.Build;

import java.io.IOException;

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
        String s = "模拟器检测：";
        boolean flag = false;

        emulatorcheck ec = new emulatorcheck();
        //检查设备型号和品牌
        if (ec.checkDeviceModel()){
            s += "\n检查设备型号和品牌:异常";
            flag = true;
        }else {
            s += "\n检查设备型号和品牌:正常";
        }
        //检查硬件特征
        if (ec.checkHardwareFeatures(context)){
            s += "\n检查硬件特征:异常";
            flag = true;
        }else {
            s += "\n检查硬件特征:正常";
        }
        //检查文件系统
        if (ec.checkFileSystem()){
            s += "\n检查文件系统:存在特征文件";
            flag = true;
        }else {
            s += "\n检查文件系统:不存在特征文件";
        }
        //检查运行程序
        if (ec.checkRunningApps()){
            s += "\n检查运行程序:存在运行中特征程序";
            flag = true;
        }else {
            s += "\n检查运行程序:不存在运行中特征程序";
        }
        //检查系统架构
        if (ec.checkArchitecture()){
            s += "\n检查系统架构:x86";
            flag = true;
        }else {
            s += "\n检查系统架构:arm";
        }
        //检查电池状态
        if (ec.checkBattery(context)){
            s += "\n检查电池状态:异常";
            flag = true;
        }else {
            s += "\n检查电池状态:正常";
        }

        if (flag){
            s += "\n\n可能是模拟器";
        }else {
            s += "\n\n可能是真机";
        }
        return s;

    }


}
