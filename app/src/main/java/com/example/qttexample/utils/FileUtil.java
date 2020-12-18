package com.example.qttexample.utils;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtil {
    private static final String LOG_FOLDER_FILE_NAME = "qtt_log";
    private static final String LOG_FILE_NAME = "Qtt.log";


    public static String initLogFile(Context context) {
        File folder;
        if (Build.VERSION.SDK_INT >= 29) {
            folder = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), LOG_FOLDER_FILE_NAME);
        } else {
            String path = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + File.separator +
                    context.getPackageName() + File.separator +
                    LOG_FOLDER_FILE_NAME;
            folder = new File(path);
            if (!folder.exists() ) {
                folder.mkdirs();
            }
            Log.d("initEngine", "initLogFile: "+folder.getAbsolutePath());
        }

        if (folder != null && !folder.exists() && !folder.mkdir()) {
            return "";
        } else {
            return new File(folder, LOG_FILE_NAME).getAbsolutePath();
        }
    }

    public static String initLogFile(Context context,long uid) {
        File folder;
        if (Build.VERSION.SDK_INT >= 29) {
            folder = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), LOG_FOLDER_FILE_NAME);
        } else {
            String path = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + File.separator +
                    context.getPackageName() + File.separator +
                    LOG_FOLDER_FILE_NAME;
            folder = new File(path);
            if (!folder.exists() ) {
                folder.mkdirs();
            }
            Log.d("initEngine", "initLogFile: "+folder.getAbsolutePath());
        }

        if (folder != null && !folder.exists() && !folder.mkdir()) {
            return "";
        } else {
            String path=uid+"___"+TimeUtil.getCurrentDateStr("yyyy-MM-dd HH:mm:ss")+".log";
            return new File(folder, path).getAbsolutePath();
        }
    }


    public static String copyAssetsFile(Context context, String fileName) {
        String result = context.getFilesDir() + "/" + fileName;
        try {
            File file = new File(result);
            if (file.exists()) {
                return result;
            }
            InputStream is = context.getAssets().open(fileName);
            FileOutputStream fos = new FileOutputStream(result);
            byte[] buffer = new byte[4096];
            int len = -1;
            while ((len = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            fos.flush();
            fos.close();
            is.close();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
