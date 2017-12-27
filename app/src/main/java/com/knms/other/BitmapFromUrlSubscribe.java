package com.knms.other;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.knms.app.KnmsApp;
import com.knms.util.BitmapHelper;
import com.knms.util.ImageLoadHelper;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by Administrator on 2017/1/11.
 */

public class BitmapFromUrlSubscribe implements Observable.OnSubscribe<Bitmap>{
    private String url;
    private int width,hight;
    public BitmapFromUrlSubscribe(String url,int width,int hight){
        this.width = width;
        this.hight = hight;
        this.url = ImageLoadHelper.getInstance().convertFillJPG(url,width,hight);
    }
    @Override
    public void call(final Subscriber<? super Bitmap> subscriber) {
        if (!subscriber.isUnsubscribed()) {
            FutureTarget<File> future = Glide.with(KnmsApp.getInstance())
                    .load(url)
                    .downloadOnly(width, hight);
            try {
                File cacheFile = future.get();
                String path = cacheFile.getAbsolutePath();
                Bitmap bitmap = BitmapHelper.getBitmap(path);
                subscriber.onNext(bitmap);
            } catch (InterruptedException e) {
                e.printStackTrace();
                subscriber.onNext(null);
            } catch (ExecutionException e) {
                e.printStackTrace();
                subscriber.onNext(null);
            }
//            Glide.with(KnmsApp.getInstance()).load(url).asBitmap().into(new SimpleTarget<Bitmap>() {
//                @Override
//                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
//                    subscriber.onNext(resource);
//                }
//                @Override
//                public void onLoadFailed(Exception e, Drawable errorDrawable) {
//                    subscriber.onNext(null);
//                }
//            });
        }
    }
}
