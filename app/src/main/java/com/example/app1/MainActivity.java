package com.example.app1;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    StringBuilder s = new StringBuilder("检测开始\n");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
//------------------------------------------Root检测---------------------------------------------------
        Button button1 = findViewById(R.id.button1);
        button1.setOnClickListener(view -> {

            result rs = new result();
            try {
                s.append(rs.rootCheck());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            checkSign();
            startScheduledTask();
            setDailyAlarm();

            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            intent.putExtra("s",s.toString());
            startActivity(intent);

            s.delete(5,s.length());
        });
//------------------------------------------模拟器检测---------------------------------------------------
        Button button2 = findViewById(R.id.button2);
        button2.setOnClickListener(view -> {

            result rs = new result();
            s.append(rs.emulatorCheck(this));
            checkSign();

            startScheduledTask();
            setDailyAlarm();

            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            intent.putExtra("s",s.toString());
            startActivity(intent);

            s.delete(5,s.length());
        });
//------------------------------------------指纹检测---------------------------------------------------
        Button button3 = findViewById(R.id.button3);
        button3.setOnClickListener(view -> {

            result rs = new result();
            s.append(rs.checkFingerPrint(this));

            checkSign();
            startScheduledTask();
            setDailyAlarm();

            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            intent.putExtra("s",s.toString());
            startActivity(intent);

            s.delete(5,s.length());
        });
//------------------------------------------hook检测---------------------------------------------------
        Button button4 = findViewById(R.id.button4);
        button4.setOnClickListener(view -> {

            result rs = new result();
            s.append(rs.checkhook());

            checkSign();
            startScheduledTask();
            setDailyAlarm();

            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            intent.putExtra("s",s.toString());
            startActivity(intent);

            s.delete(5,s.length());
        });
//------------------------------------------native检测---------------------------------------------------
        Button button5 = findViewById(R.id.button5);
        button5.setOnClickListener(view -> {

            result rs = new result();
            s.append(rs.fingerprintjni()).append("\n");

            checkSign();
            startScheduledTask();
            setDailyAlarm();

            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            intent.putExtra("s",s.toString());
            startActivity(intent);

            s.delete(5,s.length());
        });
//------------------------------------------历史记录---------------------------------------------------
        Button button6 = findViewById(R.id.button6);
        button6.setOnClickListener(view -> {

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

            result rs = new result();
            s.append(rs.test(this));

            checkSign();
            startScheduledTask();
            setDailyAlarm();

            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            intent.putExtra("s",s.toString());
            startActivity(intent);

            s.delete(5,s.length());
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //------------------------------------------获取已安装应用---------------------------------------------
        Button button8 = findViewById(R.id.button8);
        button8.setOnClickListener(view -> {

            result rs = new result();
            s.append(rs.appname(this));

            checkSign();
            startScheduledTask();
            setDailyAlarm();

            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            intent.putExtra("s",s.toString());
            startActivity(intent);

            s.delete(5,s.length());
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //------------------------------------------获取系统证书---------------------------------------------
        Button button9 = findViewById(R.id.button9);
        button9.setOnClickListener(view -> {

            result rs = new result();
            s.append(rs.certinfo());

            checkSign();
            startScheduledTask();
            setDailyAlarm();

            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            intent.putExtra("s",s.toString());
            startActivity(intent);

            s.delete(5,s.length());
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });





    }


    //-----------------------------------------------签名校验------------------------------------------------------
    private void checkSign(){
        if(signCheck()) {
            //TODO 签名正常
            s.append("\n签名校验成功");
        }else{
            //TODO 签名不正确
            s.append("\n签名校验失败");
        }
    }
    private boolean signCheck(){
        //默认密钥库签名
        String sc_default = "5F:49:E9:F6:AC:16:31:F7:9A:77:7F:1A:15:06:EE:84:48:1D:4D:DF";
        //自建密钥库签名
        String sc_myappkey = "4D:DD:19:7F:A2:A2:59:77:0F:F1:3A:EB:FE:DD:26:A4:C1:8A:80:AA";

        signcheck signCheck = new signcheck(this, sc_default);
        return signCheck.check();
    }
    private void startScheduledTask() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleWithFixedDelay(this::signCheck, 0, 15, TimeUnit.SECONDS);
        Log.d("ScheduledTask", "signcheck executed");
    }

    private static int executionCount = 0;

    private void setDailyAlarm() {
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

    private static class MyReceiver extends BroadcastReceiver {
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


}

