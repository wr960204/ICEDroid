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

import com.darvin.security.detectfrida;

import top.canyie.magiskkiller.killmagisk;

public class MainActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);

        Intent intent = getIntent();

//------------------------------------------aar---------------------------------------------------
        if (intent.getBooleanExtra("magiskkiller",false)){
            killmagisk km = new killmagisk(MainActivity2.this);

            km.setViews(findViewById(R.id.text));
            km.startDetection();
        } else if (intent.getBooleanExtra("detectfrida",false)) {
            detectfrida df = new detectfrida();
            df.detectf();
        }
//------------------------------------------文本---------------------------------------------------

        String btn = intent.getStringExtra("btn");
        String s = intent.getStringExtra("s");
        TextView title = findViewById(R.id.title);
        TextView textView1 = findViewById(R.id.text);
        title.setText(btn);
        textView1.setText(s);
//------------------------------------------返回---------------------------------------------------
        Button button = findViewById(R.id.button);
        button.setOnClickListener(view -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}