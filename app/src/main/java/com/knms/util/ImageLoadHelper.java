package com.knms.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.jpeng.progress.CircleProgress;
import com.jpeng.progress.enums.CircleStyle;
import com.jpeng.progress.enums.GradientType;
import com.knms.activity.details.DecorationStyleDetailsActivity;
import com.knms.android.R;
import com.knms.app.KnmsApp;
import com.knms.net.HttpConstant;
import com.knms.other.glide.ProgressModelLoader;

import static com.knms.android.R.id.imageview;

/**
 * Created by tdx on 2016/9/6.
 * jpg格式:sharpen 锐化;quality 图片质量
 * 长边缩放,缩略填充 参数 ?x-oss-process=image/resize,m_pad,w_400,h_400,limit_0/auto-orient,1/sharpen,100/quality,q_90/format,jpg
 * 短边缩放,居中裁剪 参数 ?x-oss-process=image/resize,m_fill,w_400,h_400,limit_0/auto-orient,1/sharpen,100/quality,q_90/format,jpg
 * 单边自适应按宽度，用于原图 参数 ?x-oss-process=image/resize,m_lfit,w_700,limit_0/auto-orient,1/quality,q_61/format,jpg/interlace,1
 * 单边自适应按高度，用于原图 参数 ?x-oss-process=image/resize,m_lfit,h_700,limit_0/auto-orient,1/quality,q_61/format,jpg/interlace,1
 * <p>
 * png格式
 * 长边缩放,缩略填充 参数 ?x-oss-process=image/resize,m_pad,w_400,h_400,limit_0/auto-orient,0/format,png
 * 短边缩放,居中裁剪 参数 ?x-oss-process=image/resize,m_fill,w_400,h_400,limit_0/auto-orient,0/format,png
 */
public class ImageLoadHelper {
    public ImageLoadHelper() {}
    /**
     * 单例
     */
    public static ImageLoadHelper getInstance() {
        return ImageLoadHelper.InstanceHolder.instance;
    }
    static class InstanceHolder {
        final static ImageLoadHelper instance = new ImageLoadHelper();
    }
    public void pause(Context context) {
//        Glide.with(context).pauseRequests();
    }

    public void resume(Context context) {
//        Glide.with(context).resumeRequests();
    }

    /**
     * 注意此方法若是应用于item复用的列表，则会出现图片错乱，设置的监听导致问题的产生
     */
    public void displayImage(final Context context, final String url, final ImageView view) {
        if (view == null) return;
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onGlobalLayout() {
                displayImage(context, url, view, view.getWidth(), view.getHeight());
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    public void displayImage(Context context, int resId, ImageView view) {
        Glide.with(context).load(resId).into(view);
    }

    public void displayImage(Context context, int resId, ImageView view, int width, int height) {
        Glide.with(context).load(resId).override(width, height).fitCenter().into(view);
    }

    public void displayImage(Context context, String url, ImageView view, int width, int height) {
        if (view == null) return;
        loadImage(context, view, url, R.drawable.bg_whilte, width, height);
    }

    public void displayImageShowProgress(Context context, String url, ImageView view, int width, int height) {
        if (view == null) return;
        loadImage(context, view, url, true, android.R.color.transparent, R.drawable.bg_whilte, width, height, false, null);
    }

    public void displayImage(Context context, String url, ImageView view, RequestListener<String, GlideDrawable> listener) {
        if (null == view) return;
        loadImage(context, view, url, false, android.R.color.transparent, R.drawable.bg_whilte, view.getWidth(), view.getHeight(), false, listener);
    }

    public void displayImageOrigin(Context context, String url, ImageView view) {
        if (view == null) return;
        loadImage(context, view, url, true, android.R.color.transparent, R.drawable.bg_black, ScreenUtil.getScreenWidth(), 0, false, null);
    }

    public void displayImageLocal(Context context, String url, ImageView view) {
        if (view == null) return;
        loadImage(context, view, url, R.drawable.bg_whilte, ScreenUtil.getScreenWidth(), 0);
    }

    public void displayImageBgBlack(Context context, String url, ImageView view) {
        if (view == null) return;
        int[] size = LocalDisplay.getViewSize(view);
        displayImageBgBlack(context, url, view, size[0], size[1]);
    }

    public void displayImageBgBlack(Context context, String url, ImageView view, int width, int height) {
        loadImage(context, view, url, R.drawable.bg_black, width, height);
    }

    public void displayImageBgW(Context context, String url, ImageView view, int width, int height, int def) {
        loadImage(context, view, url, false, def, R.drawable.bg_whilte, width, height, false, null);
    }

    /**
     * 圆角的
     *
     * @param url
     * @param view
     */
    public void displayImageRounde(Context context, String url, ImageView view) {
        loadImage(context, view, url, R.drawable.bg_whilte, ScreenUtil.getScreenWidth(), 0);
    }

    public void displayImageHead(final Context context, final String url, final ImageView view) {
        if (view == null) return;
        int[] size = LocalDisplay.getViewSize(view);
        displayImageHead(context, url, view, size[0], size[1]);
    }

    public void displayImageHead(Context context, String url, ImageView view, int width, int height) {
        if (view == null) return;
        url = convertFillPNG(url,width,height);
        loadImage(context, view, url, false, R.drawable.icon_avatar, R.drawable.icon_avatar, width, height, false, null);
    }

    public void displayKnowledgeImage(Context context, String url, ImageView view) {
        displayKnowledgeImage(context, url, view, true);
    }

    public void displayKnowledgeImage(Context context, String url, ImageView view, boolean isFit) {
        if (view == null || TextUtils.isEmpty(url)) return;
        loadImage(context, view, url, true, android.R.color.transparent, R.drawable.bg_whilte, ScreenUtil.getScreenWidth(), 0, isFit, null);
    }

    private void loadImage(Context context, ImageView imageView, String url) {
        loadImage(context, imageView, url, R.drawable.bg_whilte);
    }

    private void loadImage(Context context, ImageView imageView, String url, int errRes) {
        loadImage(context, imageView, url, errRes, 0, 0);
    }

    private void loadImage(Context context, ImageView imageView, String url, int errRes, int width, int height) {
        loadImage(context, imageView, url, false, android.R.color.transparent, errRes, width, height, false, null);
    }

    private void loadImage(Context context, final ImageView imageView, String url, boolean showProgress, int defRes, int errRes, int width, int height, boolean fit, RequestListener<String, GlideDrawable> listener) {
        if (null == imageView) return;
        if (TextUtils.isEmpty(url) || "null".equals(url)) {
            imageView.setImageResource(defRes);
            return;
        }
        if (null == context) context = imageView.getContext();
        //拼接url
        if (width > 0 && height > 0) {
            url = convertFillJPG(url, width, height);
        } else if (0 == width && height == 0) {
            int[] size = LocalDisplay.getViewSize(imageView);
            width = size[0];
            height = size[1];
            url = convertFillJPG(url, width, height);
        } else {
            url = convertLfitWidthJPG(url, width == 0 ? ScreenUtil.getScreenWidth() : width);
        }

        boolean isLocal = false;//本地图片
        if (!url.startsWith("http://") && !url.startsWith("https://"))
            fit = isLocal = true;
        DrawableTypeRequest<String>  mRmanager;
        if (showProgress && !isLocal) {
            int progressColor = Color.parseColor("#88333333");
            if(context instanceof DecorationStyleDetailsActivity){
                progressColor = Color.parseColor("#88ffffff");
            }
            final CircleProgress progress = new CircleProgress.Builder()
                    .setTextColor(Color.parseColor("#FFffffff"))
                    .setTextShow(false)
                    .setProgressColor(progressColor)//设置圆环已经加载进度中的颜色
                    .setStyle(CircleStyle.FAN)//变换风格,枚举CircleStyle.Fan or Ring
                    .setBottomColor(Color.parseColor("#88333333"))//设置圆环未加载背景的颜色
                    .setGradientType(GradientType.SWEEP)
                    .setCircleRadius(LocalDisplay.dp2px(25))
                    //设置你的属性...
                    .build();
            progress.inject(imageView);
//            progress.setMaxValue(100);
           /* ProgressManager.getInstance().addResponseListener(url, new ProgressListener() {
                @Override
                public void onProgress(ProgressInfo progressInfo) {
                    progress.setLevel(progressInfo.getPercent());
                    progress.setMaxValue(100);
                    Log.i("loadProgress","" + progressInfo.getPercent());
                }
                @Override
                public void onError(long id, Exception e) {
                    Log.i("loadProgress","" + e.toString());
                    e.printStackTrace();
                }
            });*/
            Handler handler;
            mRmanager = Glide.with(context)
                    .using(new ProgressModelLoader(handler = new Handler() {//为单个的请求指定一个 model
                        @Override
                        public void handleMessage(Message msg) {
                            progress.setLevel(msg.arg1);
                            progress.setMaxValue(msg.arg2);
                        }
                    })).load(url);
            if(handler != null){
                Message message = handler.obtainMessage();
                message.what = 1;
                message.arg1 = 0;
                message.arg2 = 100;
                handler.sendMessage(message);
            }
        }else {
            mRmanager = Glide.with(context).load(url);
        }
        if (isLocal && width > 0 && height > 0) {
            mRmanager.override(width, height);/*.centerCrop()*/
        } else if (fit) {
            mRmanager.fitCenter();
        }
        if (null != listener) {
            mRmanager.listener(listener);
        }
        mRmanager.placeholder(defRes).error(errRes);//设置加载中和错误图片
        if (!isLocal) {//网络图片
            mRmanager.diskCacheStrategy(DiskCacheStrategy.SOURCE);//仅仅只缓存原来的全分辨率的图像。
        }/*else {//本地图片
            mRmanager.thumbnail(0.1f);
        }*/
        mRmanager.into(new ImageViewTarget<GlideDrawable>(imageView) {
            @Override
            protected void setResource(GlideDrawable resource) {
                imageView.setImageDrawable(resource);
            }

            @Override
            public void setRequest(Request request) {
                imageView.setTag(R.string.image, request);
            }

            @Override
            public Request getRequest() {
                return (Request) imageView.getTag(R.string.image);
            }
        });
    }
    public void loadGif(Context context,String url,ImageView imageView){
        url = convertGif(url);
        Glide.with(context).load(url).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(imageView);
    }
    public String convertGif(String url){
        if (TextUtils.isEmpty(url)) return "";
        if(!url.contains(HttpConstant.SRC) && !url.startsWith("http")){
            return HttpConstant.SRC + url;
        }
        return url;
    }
    /**
     * 等比例填充
     */
    public String convertFillJPG(String url, int width, int height) {
        if (TextUtils.isEmpty(url)) return "";
        if (url.contains(SDCardUtils.getSDPath(KnmsApp.getInstance()))) {
            return "file://" + url;
        } else if (url.startsWith("drawable://")) {
        } else if (url.startsWith("https://")) {
        } else if (!url.contains("http://")) {
            if (width > 0 && height > 0) {
                if (url.contains("?")) {
                    return url.startsWith(HttpConstant.SRC) ? url : HttpConstant.SRC + url;
                }
                return (url.startsWith(HttpConstant.SRC) ? url : HttpConstant.SRC + url) + "?x-oss-process=image/resize,m_lfit,w_" + width + ",h_" + height + ",limit_0/auto-orient,1/sharpen,100/quality,q_61/format,jpg/interlace,1";
            } else {
                return url.startsWith(HttpConstant.SRC) ? url : HttpConstant.SRC + url;
            }
        }
        return url;
    }

    /**
     * 单边自适应，按宽度
     */
    private String convertLfitWidthJPG(String url, int width) {
        if (TextUtils.isEmpty(url)) return "";
        if (url.contains(SDCardUtils.getSDPath(KnmsApp.getInstance()))) {
            return "file://" + url;
        } else if (url.startsWith("drawable://")) {
        } else if (url.startsWith("https://")) {
        } else if (!url.contains("http://")) {
            width = (width >= 1080) ? 1080 : 720;
            if (width > 0) {
                if (url.contains("?")) {
                    return url.startsWith(HttpConstant.SRC) ? url : HttpConstant.SRC + url;
                }
                return (url.startsWith(HttpConstant.SRC) ? url : HttpConstant.SRC + url) + "?x-oss-process=image/resize,m_lfit,w_" + (int) (width * 1.5) + ",limit_0/auto-orient,1/quality,q_61/format,jpg/interlace,1";
            } else {
                return url.startsWith(HttpConstant.SRC) ? url : HttpConstant.SRC + url;
            }
        }
        return url;
    }

    /**
     * 单边自适应，按宽度
     */
    private String convertLfitHightJPG(String url, int height) {
        if (TextUtils.isEmpty(url)) return "";
        if (url.contains(SDCardUtils.getSDPath(KnmsApp.getInstance()))) {
            return "file://" + url;
        } else if (url.startsWith("drawable://")) {
        } else if (url.startsWith("https://")) {
        } else if (!url.contains("http://")) {
            if (height > 0) {
                if (url.contains("?")) {
                    return url.startsWith(HttpConstant.SRC) ? url : HttpConstant.SRC + url;
                }
                return (url.startsWith(HttpConstant.SRC) ? url : HttpConstant.SRC + url) + "?x-oss-process=image/resize,m_lfit,h_" + height + ",limit_0/auto-orient,1/quality,q_61/format,jpg/interlace,1";
            } else {
                return url.startsWith(HttpConstant.SRC) ? url : HttpConstant.SRC + url;
            }
        }
        return url;
    }

    /**
     * 等比例拆剪
     */
    private String convertPadJPG(String url, int width, int height) {
        if (TextUtils.isEmpty(url)) return "";
        if (url.contains(SDCardUtils.getSDPath(KnmsApp.getInstance()))) {
            return "file://" + url;
        } else if (url.startsWith("drawable://")) {
        } else if (url.startsWith("https://")) {
        } else if (!url.contains("http://")) {
            if (width > 0 && height > 0) {
                if (url.contains("?")) {
                    return url.startsWith(HttpConstant.SRC) ? url : HttpConstant.SRC + url;
                }
                return (url.startsWith(HttpConstant.SRC) ? url : HttpConstant.SRC + url) + "?x-oss-process=image/resize,m_pad,w_" + width + ",h_" + height + ",limit_0/auto-orient,1/sharpen,100/quality,q_61/format,jpg";
            } else {
                return url.startsWith(HttpConstant.SRC) ? url : HttpConstant.SRC + url;
            }
        }
        return url;
    }

    private String convertFillPNG(String url, int width, int height) {
        if (TextUtils.isEmpty(url)) return "";
        if (url.contains(SDCardUtils.getSDPath(KnmsApp.getInstance()))) {
            return "file://" + url;
        } else if (url.startsWith("drawable://")) {
        } else if (url.startsWith("https://")) {
        } else if (!url.contains("http://")) {
            if (width > 0 && height > 0) {
                if (url.contains("?")) {
                    return url.startsWith(HttpConstant.SRC) ? url : HttpConstant.SRC + url;
                }
                return (url.startsWith(HttpConstant.SRC) ? url : HttpConstant.SRC + url) + "?x-oss-process=image/resize,m_fill,w_" + width + ",h_" + height + ",limit_0/auto-orient,1/format,png";
            } else {
                return url.startsWith(HttpConstant.SRC) ? url : HttpConstant.SRC + url;
            }
        }
        return url;
    }

    private String convertPadPNG(String url, int width, int height) {
        if (TextUtils.isEmpty(url)) return "";
        if (url.contains(SDCardUtils.getSDPath(KnmsApp.getInstance()))) {
            return "file://" + url;
        } else if (url.startsWith("drawable://")) {
        } else if (url.startsWith("https://")) {
        } else if (!url.contains("http://")) {
            if (width > 0 && height > 0) {
                if (url.contains("?")) {
                    return url.startsWith(HttpConstant.SRC) ? url : HttpConstant.SRC + url;
                }
                return (url.startsWith(HttpConstant.SRC) ? url : HttpConstant.SRC + url) + "?x-oss-process=image/resize,m_pad,w_" + width + ",h_" + height + ",limit_0/auto-orient,1/format,png";
            } else {
                return url.startsWith(HttpConstant.SRC) ? url : HttpConstant.SRC + url;
            }
        }
        return url;
    }

    /**
     * 释放内存
     *
     * @param context 上下文
     */
    public void clearMemory(Context context) {
        Glide.get(context).clearMemory();
    }

    public Bitmap getBitmapFromCache(String url) {
            /*File file = ImageLoader.getInstance().getDiskCache().get(url);
            Bitmap bitmap = null;
            if (file != null && file.exists())
                bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            return bitmap;*/

        return BitmapFactory.decodeResource(KnmsApp.getInstance().getResources(), R.drawable.logo);
    }

}
