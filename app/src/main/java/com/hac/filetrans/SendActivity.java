package com.hac.filetrans;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.icu.text.DecimalFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
/**
 * Created by zz on 2017/11/14.
 */



public class SendActivity extends AppCompatActivity {

    private static final int FILE_SELECT_CODE = 0;
    private static final String TAG = "VideoActivity";
    String path = "null";
    String fileName = "null";
    String myIp = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        //消息处理
        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case 1:
                        Toast.makeText(SendActivity.this,msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        Toast.makeText(SendActivity.this, "正在发送...", Toast.LENGTH_SHORT).show();
                        break;

                }
            }
        };


        //定义按钮
        Button startSendBtn = findViewById(R.id.startSendBtn);
        Button fileBtn = findViewById(R.id.fileBtn);
        Button scanBtn = findViewById(R.id.scanBtn);




        //扫描按钮
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 创建IntentIntegrator对象
                IntentIntegrator intentIntegrator = new IntentIntegrator(SendActivity.this);
                // 开始扫描
                intentIntegrator.initiateScan();

            }
        });




        //发送按钮
        startSendBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {

                //定义端口、IP
                final int port = 9999;
                //path = "/storage/emulated/0/hehe/浮躁.flac";
                fileName = path.substring(path.lastIndexOf("/") + 1, path.length());

                System.out.println("-----------------------" + fileName);
                Toast.makeText(SendActivity.this, "开始发送" + fileName, Toast.LENGTH_SHORT).show();
                File file = new File(path);


                //发送文件名
                try {
                    FileInputStream fis = new FileInputStream(file);
                    int size = fis.available();


                    //String size2 = FormetFileSize(size);

                    String size2 = size/1024 + "KB";
                    fileName = "“" + fileName + "”" + "(大小:" + size2 + ")";
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }



                //发送文件线程，调用发送文件方法
                Thread sendThread = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        //SendFile(fileName, path, ipAddress, port);
                        String sendStatus = SendFile(fileName,path,myIp, port);

                        Message msg = new Message();
                        msg.what = 1;
                        msg.obj = sendStatus;
                        handler.sendMessage(msg);

                        System.out.println("正在发送：" + path);
                        System.out.println(sendStatus);

                    }
                });
                sendThread.start();
            }
        });



        //选择文件Intent
        fileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                try {
                    startActivityForResult(Intent.createChooser(intent, "选择文件"), FILE_SELECT_CODE);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(SendActivity.this, "请安装文件管理器", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



    //发送方法
    public static String SendFile(String fileName, String path, String ipAddress, int port) {
        try {

            //发送文件名
            Socket name = new Socket(ipAddress, port);
            OutputStream outputName = name.getOutputStream();
            OutputStreamWriter outputWriter = new OutputStreamWriter(outputName);
            BufferedWriter bwName = new BufferedWriter(outputWriter);
            bwName.write(fileName);
            bwName.close();
            outputWriter.close();
            outputName.close();
            name.close();


            //发送文件数据
            Socket data = new Socket(ipAddress, port);

            //输出流是socket
            OutputStream outputData = data.getOutputStream();

            //文件输入流 是 文件路径
            FileInputStream fileInput = new FileInputStream(path);
            int size = -1;

            //新建缓冲数组
            byte[] buffer = new byte[1024];

            //新建size，size每一次缓存文件输入流的1KB数据
            while ((size = fileInput.read(buffer, 0, 1024)) != -1) {

                //将1KB的size  接入 socket 输出流
                outputData.write(buffer, 0, size);
            }
            outputData.close();
            fileInput.close();
            data.close();
            return fileName + " 发送完成";

        } catch (Exception e) {
            return "发送错误:" + e.getMessage();
        }
    }



    //回调onActivity
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_SELECT_CODE) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                path = getPath(this, uri);
                Toast.makeText(this, "您已选择" + path, Toast.LENGTH_LONG).show();
            }
        }


        //返回二维码扫描结果
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "取消扫描", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "发现设备，IP：" + result.getContents() + "请点击开始发送", Toast.LENGTH_LONG).show();
                myIp = result.getContents();

            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }




    //获取选择的文件的路径
    public static String getPath(Context context, Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else
            if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }


}
