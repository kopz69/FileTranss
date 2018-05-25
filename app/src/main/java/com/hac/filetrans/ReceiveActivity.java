package com.hac.filetrans;
import com.hac.filetrans.QRCodeUtil;
import com.hac.filetrans.hehe.FilesActivity;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.icu.text.DecimalFormat;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;



import static android.text.TextUtils.indexOf;

import static java.sql.DriverManager.println;

/**
 * Created by zz on 2017/11/14.
 */

public class ReceiveActivity extends AppCompatActivity {

    private ServerSocket server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);

        //二维码
        ImageView iv_QR = (ImageView) findViewById(R.id.iv_QR);
        Bitmap mBitmap = QRCodeUtil.createQRCodeBitmap(getIP.getIp(this), 480, 480);
        iv_QR.setImageBitmap(mBitmap);


        //进度条
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setCancelable(true);// 设置是否可以通过点击Back键取消
        pd.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条




        //handler消息处理
        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    //开始接收文件
                    case 1:
                        pd.setTitle("接收中：");
                        pd.setMessage(msg.obj.toString());
                        pd.show();
                        break;
                    case 2:
                        Toast.makeText(ReceiveActivity.this,"接收完成，耗时" + msg.arg1 + "秒", Toast.LENGTH_LONG).show();
                        pd.dismiss();

                        System.out.println("shijian" + msg.arg1);
                        System.out.println("daxiao" + msg.arg2);

                        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(ReceiveActivity.this);
                        builder.setTitle("传输完成");
                        builder.setMessage("平均传输速度：" + msg.arg2 + "KB/S");
                        builder.setPositiveButton("确定", null);
                        builder.setNegativeButton("查看", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(ReceiveActivity.this,FilesActivity.class);
                                startActivity(intent);
                            }
                        });
                        builder.show();

                        break;
                    case 3:
                        Toast.makeText(ReceiveActivity.this,"接收失败：" + msg.obj.toString(), Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };


        //接收线程
        Thread listener = new Thread(new Runnable() {
            @Override
            public void run() {

                int port = 9999;
                while (port > 9000) {
                    try {
                        server = new ServerSocket(port);
                        break;
                    } catch (Exception e) {
                        port--;
                    }
                }
                if (server != null) {
                    while (true) {
                        System.out.println("rec执行中");
                        try {
                            // 接收文件名
                            Socket name = server.accept();
                            InputStream nameStream = name.getInputStream();
                            InputStreamReader streamReader = new InputStreamReader(nameStream);
                            BufferedReader br = new BufferedReader(streamReader);
                            final String fileName = br.readLine();
                            br.close();
                            streamReader.close();
                            nameStream.close();
                            name.close();

                            System.out.println("正在接收"+fileName);

                            //发送handler message
                            Message msg1 = new Message();
                            msg1.what = 1;
                            msg1.obj = fileName;
                            handler.sendMessage(msg1);


                            //计时开始
                            long start_time = System.currentTimeMillis();

                            // 接收文件数据
                            Socket data = server.accept();
                            InputStream dataStream = data.getInputStream();

                            // 创建文件的存储路径
                            File dir = new File("/storage/emulated/0/FileTrans");
                            if (!dir.exists()) {
                                dir.mkdirs();
                            }

                            // 定义完整的存储路径
                            String savePath = "/storage/emulated/0/FileTrans/" + getName(fileName);


                            //进度条最大值
                            pd.setMax(getSize(fileName));

                            FileOutputStream file = new FileOutputStream(savePath, false);
                            byte[] buffer = new byte[1024];
                            int size = -1;
                            while ((size = dataStream.read(buffer)) != -1) {
                                file.write(buffer, 0, size);

                                //传输1KB
                                pd.incrementProgressBy(1);
                            }
                            file.close();
                            dataStream.close();
                            data.close();

                            //计时结束
                            long end_time = System.currentTimeMillis();
                            int spend_time = (int)(end_time - start_time)/1000;

                            System.out.println( "接收完成");

                            //发送handler
                            Message msg2 = new Message();
                            msg2.what = 2;
                            msg2.arg1 = spend_time;
                            msg2.arg2 = getSize(fileName)/spend_time;
                            handler.sendMessage(msg2);



                        } catch (Exception e) {
                            System.out.println( "接收错误:\n" + e.getMessage());
                            //发送handler
                            Message msg3 = new Message();
                            msg3.what = 3;
                            msg3.obj = e.getMessage();
                            handler.sendMessage(msg3);
                        }

                    }
                }
            }
        });
        listener.start();
    }

    //截取文件名方法
    private String getName(String str) {
        int s = str.indexOf("”(大小:");
        String realName = str.substring(1,s);
        return realName;
    }

    //从文件名获取文件大小，单位KB
    int getSize (String str){
        int s = str.indexOf("”(大小:") + 5;
        int e = str.lastIndexOf("KB");
        int fileSize = Integer.parseInt(str.substring(s,e));
        return  fileSize;
    }

}

