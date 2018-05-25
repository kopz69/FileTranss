package com.hac.filetrans;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.hac.filetrans.hehe.FilesActivity;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //按钮定义
        Button sendBtn =findViewById(R.id.sendBtn);
        Button receiveBtn = findViewById(R.id.receiveBtn);
        Button testBtn = findViewById(R.id.testBtn);
        Button hisBtn = findViewById(R.id.hisBtn);

        //对话框
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setTitle("提示：");
        builder.setMessage("使用前请确保两台设备处于同一局域网内～");
        builder.setPositiveButton("知道了～", null);
        builder.setCancelable(false);
        builder.show();

        //按钮监听
        testBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                Intent intent = new Intent (MainActivity.this,TestActivity.class);
                startActivity(intent);
            }
        });

        //按钮监听
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,SendActivity.class);
                startActivity(intent);
            }
        });

        //按钮监听
        receiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,ReceiveActivity.class);
                startActivity(intent);
            }
        });


        //按钮监听
        hisBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //创建文件夹
                File dir = new File("/storage/emulated/0/FileTrans");
                if (!dir.exists()) {
                    dir.mkdirs();
                }

               Intent intent = new Intent(MainActivity.this,FilesActivity.class);
                startActivity(intent);

            }
        });

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},0);

        }



    }

}

