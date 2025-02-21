package com.example.app1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);
//------------------------------------------文本---------------------------------------------------
        Intent intent = getIntent();
        String s = intent.getStringExtra("s");
        TextView textView1 = findViewById(R.id.textview1);
        textView1.setText(s);
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
//------------------------------------------返回---------------------------------------------------
        Button button3 = findViewById(R.id.button3);
        button3.setOnClickListener(view -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}