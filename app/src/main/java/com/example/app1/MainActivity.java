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
    StringBuilder s = new StringBuilder();
    result rs = new result();
    boolean isdangerous;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
//------------------------------------------Root检测---------------------------------------------------
        Button button1 = findViewById(R.id.button1);
        button1.setOnClickListener(view -> {

            try {
                s.append(rs.rootCheck());
                isdangerous = rs.isdangerous(this);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            keyattestion ka = new keyattestion();
            String c = ka.checkcertchain();
            System.out.println(c);

            checkSign();
            startScheduledTask();
            setDailyAlarm();

            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            String buttonText = button1.getText().toString();
            intent.putExtra("s", s.toString());
            intent.putExtra("btn", buttonText);
            intent.putExtra("isdangerous",isdangerous);
            startActivity(intent);

            s.delete(0, s.length());
        });
//------------------------------------------模拟器检测---------------------------------------------------
        Button button2 = findViewById(R.id.button2);
        button2.setOnClickListener(view -> {

            s.append(rs.emulatorCheck(this));
            try {
                isdangerous = rs.isdangerous(this);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            checkSign();
            startScheduledTask();
            setDailyAlarm();

            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            String buttonText = button2.getText().toString();
            intent.putExtra("s", s.toString());
            intent.putExtra("btn", buttonText);
            intent.putExtra("isdangerous",isdangerous);
            startActivity(intent);

            s.delete(0, s.length());
        });
//------------------------------------------指纹检测---------------------------------------------------
        Button button3 = findViewById(R.id.button3);
        button3.setOnClickListener(view -> {

            s.append(rs.checkFingerPrint(this));
            try {
                isdangerous = rs.isdangerous(this);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            checkSign();
            startScheduledTask();
            setDailyAlarm();

            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            String buttonText = button3.getText().toString();
            intent.putExtra("s", s.toString());
            intent.putExtra("btn", buttonText);
            intent.putExtra("isdangerous",isdangerous);
            startActivity(intent);

            s.delete(0, s.length());
        });
//------------------------------------------hook检测---------------------------------------------------
        Button button4 = findViewById(R.id.button4);
        button4.setOnClickListener(view -> {

            s.append(rs.checkhook());
            try {
                isdangerous = rs.isdangerous(this);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            checkSign();
            startScheduledTask();
            setDailyAlarm();

            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            String buttonText = button4.getText().toString();
            intent.putExtra("s", s.toString());
            intent.putExtra("btn", buttonText);
            intent.putExtra("isdangerous",isdangerous);
            startActivity(intent);

            s.delete(0, s.length());
        });
//------------------------------------------native检测---------------------------------------------------
        Button button5 = findViewById(R.id.button5);
        button5.setOnClickListener(view -> {

            s.append(rs.fingerprintjni()).append("\n");
            try {
                isdangerous = rs.isdangerous(this);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            checkSign();
            startScheduledTask();
            setDailyAlarm();

            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            String buttonText = button5.getText().toString();
            intent.putExtra("s", s.toString());
            intent.putExtra("btn", buttonText);
            intent.putExtra("isdangerous",isdangerous);
            startActivity(intent);

            s.delete(0, s.length());
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
//------------------------------------------历史记录---------------------------------------------------
        Button button6 = findViewById(R.id.button6);
        button6.setOnClickListener(view -> {

            filewr fl = new filewr();
            //String fr = fl.bufferRead("a.txt");
            String fr = fl.readFromAppSpecificFile(this, "check.txt");
            try {
                isdangerous = rs.isdangerous(this);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            checkSign();
            startScheduledTask();
            setDailyAlarm();

            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            String buttonText = button6.getText().toString();
            intent.putExtra("s", fr);
            intent.putExtra("btn", buttonText);
            intent.putExtra("isdangerous",isdangerous);
            startActivity(intent);

            s.delete(0, s.length());
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

//------------------------------------------测试---------------------------------------------------
        Button button7 = findViewById(R.id.button7);
        button7.setOnClickListener(view -> {

            s.append(rs.test(this));
            try {
                isdangerous = rs.isdangerous(this);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            checkSign();
            startScheduledTask();
            setDailyAlarm();

            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            String buttonText = button7.getText().toString();
            intent.putExtra("s", s.toString());
            intent.putExtra("btn", buttonText);
            intent.putExtra("isdangerous",isdangerous);
            startActivity(intent);

            s.delete(0, s.length());
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

//------------------------------------------获取已安装应用---------------------------------------------
        Button button8 = findViewById(R.id.button8);
        button8.setOnClickListener(view -> {

            s.append(rs.appname(this));
            try {
                isdangerous = rs.isdangerous(this);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            checkSign();
            startScheduledTask();
            setDailyAlarm();

            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            String buttonText = button8.getText().toString();
            intent.putExtra("s", s.toString());
            intent.putExtra("btn", buttonText);
            intent.putExtra("isdangerous",isdangerous);
            startActivity(intent);

            s.delete(0, s.length());
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

//------------------------------------------获取系统证书---------------------------------------------
        Button button9 = findViewById(R.id.button9);
        button9.setOnClickListener(view -> {

            s.append(rs.certinfo());
            try {
                isdangerous = rs.isdangerous(this);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            checkSign();
            startScheduledTask();
            setDailyAlarm();

            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            String buttonText = button9.getText().toString();
            intent.putExtra("s", s.toString());
            intent.putExtra("btn", buttonText);
            intent.putExtra("isdangerous",isdangerous);
            startActivity(intent);

            s.delete(0, s.length());
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

//------------------------------------------获取支持软硬件---------------------------------------------
        Button button10 = findViewById(R.id.button10);
        button10.setOnClickListener(view -> {

            s.append(rs.devicefeatures(this));
            try {
                isdangerous = rs.isdangerous(this);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            checkSign();
            startScheduledTask();
            setDailyAlarm();

            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            String buttonText = button10.getText().toString();
            intent.putExtra("s", s.toString());
            intent.putExtra("btn", buttonText);
            intent.putExtra("isdangerous",isdangerous);
            startActivity(intent);

            s.delete(0, s.length());
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
//------------------------------------------密钥认证---------------------------------------------
        Button button11 = findViewById(R.id.button11);
        button11.setOnClickListener(view -> {

            s.append(rs.keyattestion());
            try {
                isdangerous = rs.isdangerous(this);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            checkSign();
            startScheduledTask();
            setDailyAlarm();

            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            String buttonText = button11.getText().toString();
            intent.putExtra("s", s.toString());
            intent.putExtra("btn", buttonText);
            intent.putExtra("isdangerous",isdangerous);
            startActivity(intent);

            s.delete(0, s.length());
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
//------------------------------------------汇总---------------------------------------------
        Button button12 = findViewById(R.id.button12);
        button12.setOnClickListener(view -> {

            try {
                s.append(rs.total(this));
                isdangerous = rs.isdangerous(this);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            checkSign();
            startScheduledTask();
            setDailyAlarm();

            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            String buttonText = button12.getText().toString();
            intent.putExtra("s", s.toString());
            intent.putExtra("btn", buttonText);
            intent.putExtra("isdangerous",isdangerous);
            startActivity(intent);

            s.delete(0, s.length());
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
            s.append("\n\n签名校验成功");
        }else{
            //TODO 签名不正确
            s.append("\n\n签名校验失败");
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
    //定时校验
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
    }
    private static class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 通过上下文调用 signCheck 方法
            if (context instanceof MainActivity) {
                ((MainActivity) context).signCheck();
                System.out.println("setDailyAlarm");
                executionCount += 1;
            }
        }
    }


}

