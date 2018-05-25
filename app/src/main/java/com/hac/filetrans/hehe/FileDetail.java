package com.hac.filetrans.hehe;

import java.io.File;
import java.nio.file.Files;
import java.text.SimpleDateFormat;

/**
 * Created by hac on 2018/3/30.
 */

public class FileDetail {

    public static String[] fileView(String path) {

        File filePath = new File(path);

            File filePaths[] = filePath.listFiles();

            String fileNames[] = new String[filePaths.length];
            int i = 0;
            for (File ff : filePaths) {
                fileNames[i] = filePaths[i].getName();
                i++;
            }

            return fileNames;

    }

    public static String[] fileDate(String path){

        File filePath = new File(path);


            File filePaths[] = filePath.listFiles();
            String fileDate[] = new String[filePaths.length];
            int i = 0;
            for (File ff : filePaths) {

                SimpleDateFormat formatter = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
                fileDate[i] = formatter.format(filePaths[i].lastModified());

                i++;
            }

            return fileDate;

    }


}
