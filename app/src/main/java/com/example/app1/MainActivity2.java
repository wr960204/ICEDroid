package com.example.app1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;

public class MainActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);
//------------------------------------------文本---------------------------------------------------
        Intent intent = getIntent();
        String btn = intent.getStringExtra("btn");
        String s = intent.getStringExtra("s");
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView title = findViewById(R.id.title);
        TextView textView1 = findViewById(R.id.textview1);
        title.setText(btn);
        textView1.setText(s);
        //是否有风险
        boolean isdangerous = intent.getBooleanExtra("isdangerous",false);
        TextView result = findViewById(R.id.result);
        if (isdangerous){
            result.setText("设备存在风险");
            result.setTextColor(Color.parseColor("#ff0000"));
        }else {
            result.setText("设备无风险");
            result.setTextColor(Color.parseColor("#00ff00"));
        }

//------------------------------------------更新---------------------------------------------------
        Button button1 = findViewById(R.id.button1);
        button1.setOnClickListener(view -> {
            filewr fl = new filewr();
            assert s != null;
            fl.writeToAppSpecificFile(this, "check.txt",s);
        });
        Button button2 = findViewById(R.id.button2);
        button2.setOnClickListener(view -> {
            filewr fl = new filewr();
            fl.bufferSave(s,"a.txt");
        });
//------------------------------------------上传---------------------------------------------------
        Button button3 = findViewById(R.id.button3);
        button3.setOnClickListener(view -> {
            senddata sd = new senddata();
            try {
                sd.sendDataToServer(s);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
//------------------------------------------返回---------------------------------------------------
        Button button4 = findViewById(R.id.button4);
        button4.setOnClickListener(view -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}