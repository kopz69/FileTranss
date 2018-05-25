package com.hac.filetrans;
import com.hac.filetrans.getIP;
import android.content.Context;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Created by zz on 2017/11/22.
 */

public class TestActivity extends AppCompatActivity {

    private Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        Button sendBtn = findViewById(R.id.sendBtn);
        final EditText et = findViewById(R.id.et);
        final TextView tvIP = findViewById(R.id.tvIP);
        final EditText etIP= findViewById(R.id.etIP);
        tvIP.setText("本机IP：" + getIP.getIp(this));


        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case 1:
                        Toast.makeText(TestActivity.this,"收到：" + msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        Toast.makeText(TestActivity.this, "正在发送...", Toast.LENGTH_SHORT).show();
                        break;

                }
            }
        };


        //监听线程
        Thread listener = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("监听中。。。");

                try {
                    // 创建一个serversocket对象，并让他在Port端口监听
                    ServerSocket serversocket = new ServerSocket(9527);
                    while (true) {
                        // 调用serversocket的accept()方法，接收客户端发送的请求
                        Socket socket = serversocket.accept();
                        BufferedReader buffer = new BufferedReader(
                                new InputStreamReader(socket.getInputStream()));
                        // 读取数据
                        final String rec = buffer.readLine();
                        System.out.println("收到了：" + rec);


                        Message msg = new Message();
                        msg.what = 1;
                        msg.obj = rec;
                        handler.sendMessage(msg);


                        /*handler.post(new Runnable() {
                            @Override
                            public void run() {
                                TextView tvRec = findViewById(R.id.tvRec);
                                tvRec.setText(rec);
                            }
                        });
                        */
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        listener.start();


        //点击发送
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new Thread() {
                    @Override
                    public void run() {

                        Message msg = new Message();
                        msg.what = 2;
                        handler.sendMessage(msg);

                        String yourip = etIP.getText().toString();

                        try {
                            // 创建socket对象，指定服务器端地址和端口号
                            Socket socket = new Socket(yourip, 9527);
                            // 获取 Client 端的输出流
                            PrintWriter out = new PrintWriter(new BufferedWriter(
                                    new OutputStreamWriter(socket.getOutputStream())), true);
                            // 填充信息
                            out.println(et.getText().toString());
                            System.out.println("已经发送了");

                            // 关闭
                            socket.close();
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }.start();

            }
        });
    }

}