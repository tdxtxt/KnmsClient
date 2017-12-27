package com.knms.other;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.integration.okhttp3.OkHttpGlideModule;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.knms.util.SDCardUtils;

/**
 * Created by Administrator on 2017/4/20.
 */

public class GlideConfigModule extends OkHttpGlideModule {
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
//        ViewTarget.setTagId(R.id.imageview);
        int cacheSize100MegaBytes = 100 * 1024 * 1024;
        String downloadDirectoryPath = SDCardUtils.getCacheImgDir(context);
        builder.setDiskCache(
                new DiskLruCacheFactory(downloadDirectoryPath, cacheSize100MegaBytes )
        );
        builder.setDecodeFormat(DecodeFormat.PREFER_RGB_565);

        int maxMemory = (int) Runtime.getRuntime().maxMemory();//获取系统分配给应用的总内存大小
        int memoryCacheSize = maxMemory / 8;//设置图片内存缓存占用八分之一
        //设置内存缓存大小
        builder.setMemoryCache(new LruResourceCache(memoryCacheSize));
        builder.setBitmapPool(new LruBitmapPool(memoryCacheSize));

        super.applyOptions(context,builder);
    }

    @Override
    public void registerComponents(Context context, Glide glide) {
//        glide.register(GlideUrl.class, InputStream.class,new OkHttpUrlLoader.Factory(ImageLoadHelper.getInstance().getOkhttpClient()));
        super.registerComponents(context,glide);
//        glide.register(String.class, InputStream.class, new ProgressModelLoader.Factory());
    }
}
