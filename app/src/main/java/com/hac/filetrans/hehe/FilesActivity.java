package com.hac.filetrans.hehe;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.hac.filetrans.R;
import com.hac.filetrans.hehe.Animal;


import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static com.hac.filetrans.hehe.FileDetail.fileDate;
import static com.hac.filetrans.hehe.FileDetail.fileView;

/**
 * Created by hac on 2018/3/30.
 */

public class FilesActivity extends AppCompatActivity {



        //System.out.println(FileDetail.fileView("/storage/emulated/0/FileTrans")[1]);
        //System.out.println(FileDetail.fileDate("/storage/emulated/0/FileTrans")[1]);

    String fileNames[] = fileView("/storage/emulated/0/FileTrans");
    String filedate[] = fileDate("/storage/emulated/0/FileTrans");
    int len = fileNames.length;

    private List<Animal> mData = null;
    private Context mContext;
    private AnimalAdapter mAdapter = null;
    private ListView list_animal;





    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_his);
        mContext = FilesActivity.this;
        list_animal = (ListView) findViewById(R.id.lv);
        mData = new LinkedList<Animal>();
        for (int i = 0; i < len; i++) {
            mData.add(new Animal(fileNames[i], filedate[i]));
        }

        mAdapter = new AnimalAdapter((LinkedList<Animal>) mData, mContext);
        list_animal.setAdapter(mAdapter);

        list_animal.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Uri uri = null;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    uri = FileProvider.getUriForFile(mContext, "com.hac.FileTrans.mp", new File("/storage/emulated/0/FileTrans/" + fileNames[position]));
                }else{
                    //小于安卓7.0
                    uri = Uri.fromFile(new File("/storage/emulated/0/FileTrans/" + fileNames[position]));}
                System.out.println(uri);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


                //根据后缀名判断
                String end = fileNames[position].substring(fileNames[position].lastIndexOf(".") + 1, fileNames[position].length()).toLowerCase(Locale.getDefault());

                if (end.equals("m4a") || end.equals("mp3") || end.equals("wav")|| end.equals("flac")) {
                    intent.setDataAndType(uri, "audio/*");
                }else if (end.equals("3gp") || end.equals("mp4")|| end.equals("mkv")|| end.equals("avi")|| end.equals("mov")|| end.equals("wmv")) {
                    intent.setDataAndType(uri, "video/*");
                }else if (end.equals("jpg") || end.equals("gif") || end.equals("png") || end.equals("jpeg") || end.equals("bmp")) {
                    intent.setDataAndType(uri, "image/*");
                } else if (end.equals("apk")) {
                    intent.setDataAndType(uri, "application/vnd.android.package-archive");
                }else if (end.equals("html") || end.equals("htm")||end.equals("txt")) {
                    intent.setDataAndType(uri, "text/html/*");
                }else {
                    intent.setDataAndType(uri, "*/*");
                }
                startActivity(intent);

            }
        });

    }
}


