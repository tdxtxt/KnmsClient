package com.knms.util;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import com.knms.app.KnmsApp;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2016/9/5.
 */
public class BitmapHelper {
    private static final String TAG = "BitmapHelper";
    /**
     * @Title: compressQuality
     * @Description: 不改变图片的尺寸,质量压缩一般可用于上传大图前的处理
     * @param image
     * @return
     * @return: Bitmap
     */
    public static Bitmap compressQuality(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 75, baos);//质量压缩方法，这里80表示压缩80%，把压缩后的数据存放到baos中
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        try {
            baos.flush();
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
    /**
     * Can't compress a recycled bitmap
     * @param srcImagePath     原始图片的路径
     * @param outWidth        期望的输出图片的宽度
     * @param outHeight       期望的输出图片的高度
     * @param maxFileSize       期望的输出图片的最大占用的存储空间
     * @return
     */
    private static String compress(String srcImagePath, int outWidth, int outHeight, int maxFileSize){
        //进行大小缩放来达到压缩的目的
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(srcImagePath, options);
        //根据原始图片的宽高比和期望的输出图片的宽高比计算最终输出的图片的宽和高
        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;
        float maxWidth = outWidth;
        float maxHeight = outHeight;
        float srcRatio = srcWidth / srcHeight;
        float outRatio = maxWidth / maxHeight;
        float actualOutWidth = srcWidth;
        float actualOutHeight = srcHeight;

        if (srcWidth > maxWidth || srcHeight > maxHeight) {
            //如果输入比率小于输出比率,则最终输出的宽度以maxHeight为准()
            //比如输入比为10:20 输出比是300:10 如果要保证输出图片的宽高比和原始图片的宽高比相同,则最终输出图片的高为10
            //宽度为10/20 * 10 = 5  最终输出图片的比率为5:10 和原始输入的比率相同

            //同理如果输入比率大于输出比率,则最终输出的高度以maxHeight为准()
            //比如输入比为20:10 输出比是5:100 如果要保证输出图片的宽高比和原始图片的宽高比相同,则最终输出图片的宽为5
            //高度需要根据输入图片的比率计算获得 为5 / 20/10= 2.5  最终输出图片的比率为5:2.5 和原始输入的比率相同
            if (srcRatio < outRatio) {
                actualOutHeight = maxHeight;
                actualOutWidth = actualOutHeight * srcRatio;
            } else if (srcRatio > outRatio) {
                actualOutWidth = maxWidth;
                actualOutHeight = actualOutWidth / srcRatio;
            } else {
                actualOutWidth = maxWidth;
                actualOutHeight = maxHeight;
            }
        }
        options.inSampleSize = computSampleSize(options, actualOutWidth, actualOutHeight);
        options.inJustDecodeBounds = false;
        Bitmap scaledBitmap = null;
        try {
            scaledBitmap = BitmapFactory.decodeFile(srcImagePath, options);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        if (scaledBitmap == null) {
            return null;//压缩失败
        }
        //生成最终输出的bitmap
        Bitmap actualOutBitmap = Bitmap.createScaledBitmap(scaledBitmap, (int) actualOutWidth, (int) actualOutHeight, true);
        if(actualOutBitmap != scaledBitmap)
            scaledBitmap.recycle();

//        //处理图片旋转问题
//        ExifInterface exif = null;
//        try {
//            exif = new ExifInterface(srcImagePath);
//            int orientation = exif.getAttributeInt(
//                    ExifInterface.TAG_ORIENTATION, 0);
//            Matrix matrix = new Matrix();
//            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
//                matrix.postRotate(90);
//            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
//                matrix.postRotate(180);
//            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
//                matrix.postRotate(270);
//            }
//            actualOutBitmap = Bitmap.createBitmap(actualOutBitmap, 0, 0,
//                    actualOutBitmap.getWidth(), actualOutBitmap.getHeight(), matrix, true);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        }

        //进行有损压缩
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int options_ = 100;
        actualOutBitmap.compress(Bitmap.CompressFormat.JPEG, options_, baos);//质量压缩方法，把压缩后的数据存放到baos中 (100表示不压缩，0表示压缩到最小)

        int baosLength = baos.toByteArray().length;

        while (baosLength / 1024 > maxFileSize) {//循环判断如果压缩后图片是否大于maxMemmorrySize,大于继续压缩
            baos.reset();//重置baos即让下一次的写入覆盖之前的内容
            options_ = Math.max(0, options_ - 10);//图片质量每次减少10
            actualOutBitmap.compress(Bitmap.CompressFormat.JPEG, options_, baos);//将压缩后的图片保存到baos中
            baosLength = baos.toByteArray().length;
            if (options_ == 0)//如果图片的质量已降到最低则，不再进行压缩
                break;
        }
        actualOutBitmap.recycle();

        //将bitmap保存到指定路径
        FileOutputStream fos = null;
        try {
            File f = new File(srcImagePath);
            if (f.exists()) {
                f.delete();
            }
            fos = new FileOutputStream(srcImagePath);
            //包装缓冲流,提高写入速度
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fos);
            bufferedOutputStream.write(baos.toByteArray());
            bufferedOutputStream.flush();
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return "ok";
    }
    private static int computSampleSize(BitmapFactory.Options options, float reqWidth, float reqHeight) {
        float srcWidth = options.outWidth;//20
        float srcHeight = options.outHeight;//10
        int sampleSize = 1;
        if (srcWidth > reqWidth || srcHeight > reqHeight) {
            int withRatio = Math.round(srcWidth / reqWidth);
            int heightRatio = Math.round(srcHeight / reqHeight);
            sampleSize = Math.min(withRatio, heightRatio);
        }
        return sampleSize;
    }

    /**
     * 压缩并存储
     * @param path
     */
    public static void saveBitmap(String path){
        try {
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, opt);
            int picWidth = opt.outWidth;
            int picHeight = opt.outHeight;
            opt.inJustDecodeBounds = false;
            compress(path,picWidth,picHeight,300);//最多300k
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static Bitmap compressSize(String path) {
        Bitmap bitmap = null;
        try {
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inJustDecodeBounds = true;
            bitmap = BitmapFactory.decodeFile(path, opt);
            int picWidth = opt.outWidth;
            int picHeight = opt.outHeight;
            int width = 1024;
            opt.inSampleSize = 2;//若值为2图片的宽度和高度都变为以前的1/2
            if (picWidth > picHeight) {
                if (picWidth > width) {
                    opt.inSampleSize = picWidth / width;
                }
            } else {
                if (picHeight > width) {
                    opt.inSampleSize = picHeight / width;
                }
            }
            opt.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFile(path, opt);
            int afterwidth = bitmap.getWidth();
            int afterheight = bitmap.getHeight();
            float con = 1.0f;
            int bigger = afterheight > afterwidth ? afterheight : afterwidth;
            if (bigger > width) {
                con = (float) width / bigger;
            }
            bitmap = Bitmap.createScaledBitmap(bitmap, (int) (con * afterwidth), (int) (con * afterheight), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }
    /**
     * 将bitmap 保存成文件
     */
    public static void saveBitmap(Bitmap bp, String path) {
        File f = new File(path);
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bp.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void deleteBitmap(String path){
        File f = new File(path);
        if (f.exists()) {
            f.delete();
        }
    }
    public static String saveImageToGallery(Bitmap bmp) {
        if(bmp == null) return "";
        BufferedOutputStream bos = null;
        File file = null;
        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                // 首先保存图片
                File appDir = SDCardUtils.getDCIMDir(KnmsApp.getInstance());
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss"); // 格式化时间
                String fileName = format.format(new Date()) + ".jpg";
                file = new File(appDir, fileName);
                if (!file.exists()) {
                    file.createNewFile();
                }
                bos = new BufferedOutputStream(new FileOutputStream(file));
                bmp.compress(Bitmap.CompressFormat.JPEG, 90, bos);//将图片压缩到流中

                // 通知图库更新
                KnmsApp.getInstance().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                bos.flush();//输出
                bos.close();//关闭
                bmp.recycle();// 回收bitmap空间
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return (file != null) ? file.getAbsolutePath() : "保存失败";
    }
    /**
     * 旋转图片
     * @param bp     图片
     * @param degree 角度
     * @return
     */
    public static Bitmap rotateBitMap(Bitmap bp, int degree) {
        if (degree == 0)
            return bp;
        Matrix matrix = new Matrix();
        matrix.reset();
        matrix.postRotate(degree);
        Bitmap nowBp = Bitmap.createBitmap(bp, 0, 0, bp.getWidth(),
                bp.getHeight(), matrix, true);
        if (bp.isRecycled()) {
            bp.recycle();
        }
        return nowBp;
    }
    /**
     * 通过路径得到bitmap
     *
     * @param path
     * @return
     */
    public static Bitmap getBitmap(String path) {

        //先解析图片边框的大小
        BitmapFactory.Options ops = new BitmapFactory.Options();
        ops.inJustDecodeBounds = true;
        Bitmap bm = BitmapFactory.decodeFile(path, ops);
        ops.inSampleSize = 1;
        int oHeight = ops.outHeight;
        int oWidth = ops.outWidth;

        //控制压缩比
        int contentWidth = 1024;
        int contentHeight = 1280;
        if (oWidth > contentWidth || oHeight > contentHeight)
            if (((float) oHeight / contentHeight) < ((float) oWidth / contentWidth)) {
                ops.inSampleSize = (int) Math.ceil((float) oWidth / contentWidth);
            } else {
                ops.inSampleSize = (int) Math.ceil((float) oHeight / contentHeight);
            }
        ops.inJustDecodeBounds = false;
        bm = BitmapFactory.decodeFile(path, ops);
        return bm;
    }
    public static Bitmap decode(InputStream is) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        // RGB_565
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        /**
         * 在4.4上，如果之前is标记被移动过，会导致解码失败
         */
        try {
            if (is.markSupported()) {
                is.reset();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            return BitmapFactory.decodeStream(is, null, options);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        return null;
    }
    public static int[] decodeBound(File file) {
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            int[] bound = decodeBound(is);
            return bound;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return new int[]{0, 0};
    }
    public static int[] decodeBound(InputStream is) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, options);

        return new int[]{options.outWidth, options.outHeight};
    }
    /**
     * 按照一定的宽高比例裁剪图片
     *
     * @param bitmap
     * @param num1
     *            长边的比例
     * @param num2
     *            短边的比例
     * @return
     */
    public static Bitmap imageCrop(Bitmap bitmap, int num1, int num2,
                                   boolean isRecycled){
        if (bitmap == null)
        {
            return null;
        }
        int w = bitmap.getWidth(); // 得到图片的宽，高
        int h = bitmap.getHeight();
        int retX, retY;
        int nw, nh;
        if (w > h)
        {
            if (h > w * num2 / num1)
            {
                nw = w;
                nh = w * num2 / num1;
                retX = 0;
                retY = (h - nh) / 2;
            } else
            {
                nw = h * num1 / num2;
                nh = h;
                retX = (w - nw) / 2;
                retY = 0;
            }
        } else
        {
            if (w > h * num2 / num1)
            {
                nh = h;
                nw = h * num2 / num1;
                retY = 0;
                retX = (w - nw) / 2;
            } else
            {
                nh = w * num1 / num2;
                nw = w;
                retY = (h - nh) / 2;
                retX = 0;
            }
        }
        Bitmap bmp = Bitmap.createBitmap(bitmap, retX, retY, nw, nh, null,
                false);
        if (isRecycled && bitmap != null && !bitmap.equals(bmp)
                && !bitmap.isRecycled())
        {
            bitmap.recycle();
            bitmap = null;
        }
        return bmp;// Bitmap.createBitmap(bitmap, retX, retY, nw, nh, null,
        // false);
    }
}
