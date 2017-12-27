package com.knms.util;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.text.DecimalFormat;

/**
 * Created by Administrator on 2016/8/25.
 */
public class SDCardUtils {
    public final static String CACHE = "/Android/data/knmsApp/";
    public final static String CACHE_IMG = "imageloader/";

    /**
     * 判断SDCard是否可用
     *
     * @return
     */
    public static boolean isSDCardEnable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取Sdcard卡路径
     *
     * @return SDPath
     */
    public static String getSDPath(Context context) {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();
        } else {
            sdDir = context.getCacheDir();
        }
        return sdDir.toString();
    }

    /**
     * 获取缓存路径
     *
     * @return SDPath SDCard--/Android/data/knmsApp
     */
    public static String getCacheDir(Context context) {
        String cacheDir = getSDPath(context) + CACHE;
        File filecacheDir = new File(cacheDir);
        if (!filecacheDir.exists()) {
            filecacheDir.mkdirs();
        }
        return filecacheDir.getAbsolutePath();
    }

    /**
     * @param context
     * @return SDCard--/DCIM/Camera
     */
    public static File getDCIMDir(Context context) {
        String imgDir = getSDPath(context) + "/DCIM/Camera";
        File fileImgDir = new File(imgDir);
        if (!fileImgDir.exists()) {
            fileImgDir.mkdirs();
        }
        return fileImgDir;
    }

    /**
     * 获取缓存img路径
     *
     * @return SDPath SDCard--/Android/data/knmsApp/imageloader
     */
    public static String getCacheImgDir(Context context) {
        String cacheImgDir = getSDPath(context) + CACHE + CACHE_IMG;
        File filecacheImgDir = new File(cacheImgDir);
        if (!filecacheImgDir.exists()) {
            filecacheImgDir.mkdirs();
        }
        return filecacheImgDir.getAbsolutePath();
    }

    public static File getCacheImgDirFile(Context context) {
        String path = getCacheImgDir(context);
        File fileDir = new File(path);
        return fileDir;
    }

    public static File getCacheDirFile(Context context) {
        String path = getCacheDir(context);
        File fileDir = new File(path);
        return fileDir;
    }

    /**
     * 便利删除文件
     */
    public static void RecursionDeleteFile(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }
        if (file.isDirectory() && !file.getName().contains("nim")) {
            File[] childFile = file.listFiles();
            if (childFile == null || childFile.length == 0) {
                file.delete();
                return;
            }
            for (File f : childFile) {
                RecursionDeleteFile(f);
            }
            file.delete();
        }
    }

    /**
     * 获取文件大小
     */
    public static long getFolderSize(File file) throws Exception {
        long size = 0;
        File[] fileList = file.listFiles();
        for (int i = 0; i < fileList.length; i++) {
            if (fileList[i].isDirectory()) {
                size = size + getFolderSize(fileList[i]);
            } else {
                size = size + fileList[i].length();
            }
        }

        return size;
    }

    /**
     * 文件大小转换
     */
    public static String setFileSize(long size) {
        DecimalFormat df = new DecimalFormat("###.##");
        float f = ((float) size / (float) (1024 * 1024));

        if (f < 1.0) {
            float f2 = ((float) size / (float) (1024));

            return df.format(new Float(f2).doubleValue()) + "KB";

        } else {
            return df.format(new Float(f).doubleValue()) + "M";
        }

    }
}
