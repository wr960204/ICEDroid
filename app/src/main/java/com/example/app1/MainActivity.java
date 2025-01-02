package com.example.app1;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    String s ="检测开始\n";
    String sc_myappkey = "4D:DD:19:7F:A2:A2:59:77:0F:F1:3A:EB:FE:DD:26:A4:C1:8A:80:AA";//自建密钥库签名
    String sc_default = "5F:49:E9:F6:AC:16:31:F7:9A:77:7F:1A:15:06:EE:84:48:1D:4D:DF";//默认密钥库签名

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
//------------------------------------------Root检测---------------------------------------------------
        Button button1 = findViewById(R.id.button1);
        button1.setOnClickListener(view -> {

            try {
                rootCheck();
                checkSign();

                startScheduledTask();
                setDailyAlarm();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            intent.putExtra("s",s);
            startActivity(intent);

            s = "检测开始";
        });
//------------------------------------------模拟器检测---------------------------------------------------
        Button button2 = findViewById(R.id.button2);
        button2.setOnClickListener(view -> {

            emulatorCheck();
            checkSign();

            startScheduledTask();
            setDailyAlarm();

            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            intent.putExtra("s",s);
            startActivity(intent);

            s = "检测开始";
        });
//------------------------------------------指纹检测---------------------------------------------------
        Button button3 = findViewById(R.id.button3);
        button3.setOnClickListener(view -> {

            try {
                checkFingerPrint();

                checkSign();
                startScheduledTask();
                setDailyAlarm();

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            intent.putExtra("s",s);
            startActivity(intent);

            s = "检测开始";
        });
//------------------------------------------hook检测---------------------------------------------------
        Button button6 = findViewById(R.id.button6);
        button6.setOnClickListener(view -> {

            try {
                String h = checkhook();
                s += h;
                checkSign();

                startScheduledTask();
                setDailyAlarm();

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            intent.putExtra("s",s);
            startActivity(intent);

            s = "检测开始";
        });
//------------------------------------------native检测---------------------------------------------------
        Button button4 = findViewById(R.id.button4);
        button4.setOnClickListener(view -> {

            String fj = fingerprintjni();
            s += fj + "\n";

            checkSign();
            startScheduledTask();
            setDailyAlarm();

            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            intent.putExtra("s",s);
            startActivity(intent);

            s = "检测开始";
        });
//------------------------------------------历史记录---------------------------------------------------
        Button button5 = findViewById(R.id.button5);
        button5.setOnClickListener(view -> {

            filewr fl = new filewr();
            String fr = fl.bufferRead("a.txt");

            checkSign();
            startScheduledTask();
            setDailyAlarm();

            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            intent.putExtra("s",fr);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //------------------------------------------测试---------------------------------------------------
        Button button7 = findViewById(R.id.button7);
        button7.setOnClickListener(view -> {

            fptest fp = new fptest();
            StringBuilder fs = new StringBuilder();
            List<String> p = null;
            try {
                p = fp.getProperties(this);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            for (String property : p){
                fs.append(property).append("\n");
            }


            String result = fs.toString();


            checkSign();
            startScheduledTask();
            setDailyAlarm();

            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            intent.putExtra("s",result);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });





    }

    //-----------------------------------------------ROOT检测------------------------------------------------------
    public void rootCheck() throws IOException {
        boolean flag = false;
        //检查SU命令
        rootcheck rc = new rootcheck();
        if (rc.checkSuCommand()){
            s += "\n检查SU命令:执行成功";
            flag = true;
        }else {
            s += "\n检查SU命令:执行失败";
        }
        //检查root文件
        if (rc.checkRootFiles()){
            s += "\n检查root文件:存在特征文件";
            flag = true;
        }else {
            s += "\n检查root文件:不存在特征文件";
        }
        //检查系统标签
        if (rc.checkSystemTags()){
            s += "\n检查系统标签:异常";
            flag = true;
        }else {
            s += "\n检查系统标签:正常";
        }
        //检查系统分区
        if (rc.checkMountInfo()){
            s += "\n检查分区读写模式:系统分区可写";
            flag = true;
        }else {
            s += "\n检查分区读写模式:系统分区只读";
        }
        //检查系统属性
        if (rc.checkSystemProperty()){
            s += "\n检查系统属性:异常";
            flag = true;
        }else {
            s += "\n检查系统属性:正常";
        }
        //检查SELinux
        if (rc.checkSELinuxStatus().equalsIgnoreCase("y")){
            s += "\n检查SELinux状态:异常";
            flag = true;
        } else if (rc.checkSELinuxStatus().equalsIgnoreCase("n")) {
            s += "\n检查SELinux状态:正常";
        } else {
            s += "\n检查SELinux状态:未知";
        }
        //检查bootloader
        if (rc.isBootloaderUnlocked()){
            s += "\n检查Bootloader状态:异常";
            flag = true;
        }else {
            s += "\n检查Bootloader状态:正常";
        }
        //检查TEE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (rc.checkTEE()){
                s += "\n检查TEE状态:正常";

            }else {
                s += "\n检查TEE状态:异常";
                flag = true;
            }
        }
        if(rc.checkFP()){
            s += "\n检查系统指纹属性：一致";
        }else {
            s += "\n检查系统指纹属性：异常";
            flag = true;
        }


        if (flag){
            s += "\n\n可能已root";
        }else {
            s += "\n\n可能未root";
        }

    }

    //-----------------------------------------------模拟器检测------------------------------------------------------
    public void emulatorCheck(){
        boolean flag = false;
        Context context = this;

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

    }


    //-----------------------------------------------签名检测------------------------------------------------------
    public void checkSign(){
        if(signCheck()) {
            //TODO 签名正常
            s += "\n签名校验成功";
        }else{
            //TODO 签名不正确
            s += "\n签名校验失败";
        }
    }

    public boolean signCheck(){
        signcheck signCheck = new signcheck(this,sc_default);
        return signCheck.check();
    }

    public void startScheduledTask() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleWithFixedDelay(this::signCheck, 0, 15, TimeUnit.SECONDS);
        Log.d("ScheduledTask", "signcheck executed");
    }


    private static int executionCount = 0;

    public void setDailyAlarm() {

        AlarmManager aManager = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, MyReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 24);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long triggerTime = calendar.getTimeInMillis();
        if (System.currentTimeMillis() > triggerTime) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            triggerTime = calendar.getTimeInMillis();
        }
        long intervalMillis = AlarmManager.INTERVAL_DAY; // 每天
        aManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerTime, intervalMillis, pendingIntent);

        filewr fl = new filewr();
        fl.bufferRead("sc.txt");
    }

    public static class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 通过上下文调用 signCheck 方法
            if (context instanceof MainActivity) {
                ((MainActivity) context).signCheck();
                System.out.println("setDailyAlarm");
                executionCount += 1;

                filewr fl = new filewr();
                fl.bufferSave(String.valueOf(executionCount),"sc.txt");

            }
        }
    }

    //-----------------------------------------------设备指纹检测------------------------------------------------------
    public void checkFingerPrint() {
        fingerprint fp = new fingerprint();
        String dev =fp.getDeviceID(getContentResolver());
        String net = fp.getLocalMacAddress();
        String sys = fp.getSystemProperties();

        fingerprintjni j = new fingerprintjni();
        String fpjni = "\n系统指纹：" + j.fingerprint() + "\n";
        String npjni = "网络地址：\n" + j.netfp() + "\n";

        s += "\njava层检测：\n"+dev + net + sys + "\nnative层检测：" + fpjni + npjni;

        compareResults(sys,fpjni);

        fp.getAccounts(this);
    }

    private void compareResults(String result1, String result2) {
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
            s += "\n指纹属性一致\n";

        s += result;
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


    //-----------------------------------------------native检测方法------------------------------------------------------
    public String fingerprintjni(){
        fingerprintjni j = new fingerprintjni();
        StringBuilder s = new StringBuilder();
        String fpjni = "\n系统指纹：" + j.fingerprint() + "\n";
        String npjni = "\n网络地址：\n" + j.netfp() + "\n";
        String chjni = j.check() + "\n";
        String msjni = j.mapscheck() + "\n";
        s.append(fpjni).append(npjni).append("\nhook检测：\n").append(chjni).append(msjni);
        return s.toString();
    }

    //-----------------------------------------------hook检测方法------------------------------------------------------
    public String checkhook(){
        String ch = checkfrida() + "\n";

        fingerprintjni j = new fingerprintjni();
        String chjni = j.check() + "\n";
        String msjni = j.mapscheck() + "\n";

        return "\njava层检测：\n" + ch + "\nnative层检测：\n" + chjni + msjni;
    }

    public String checkfrida(){
        hookcheck hc = new hookcheck();
        String h = "未检测到frida";
        if(hc.hasReadProcMaps("frida")){
            h = "检测到frida";
        }
        if(hc.mCheckFridaTcp())
            h = "检测到frida";
        return h;
    }

    //-----------------------------------------------测试方法------------------------------------------------------


}

