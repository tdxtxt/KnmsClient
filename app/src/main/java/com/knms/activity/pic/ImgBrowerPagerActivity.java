package com.knms.activity.pic;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bm.library.Info;
import com.bm.library.PhotoView;
import com.gyf.barlibrary.ImmersionBar;
import com.knms.activity.base.BaseActivity;
import com.knms.app.KnmsApp;
import com.knms.bean.other.Pic;
import com.knms.util.BitmapHelper;
import com.knms.util.DialogHelper;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.util.ImageLoadHelper;
import com.knms.util.LocalDisplay;
import com.knms.util.SDCardUtils;
import com.knms.util.Tst;
import com.knms.view.HackyViewPager;
import com.knms.android.R;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 类说明：
 * 图片浏览器，采用Intent传参(data：List<String>-图片url地址集合；pics:List<Pic>上传成功的图片对象 ;position：int-显示第几张图片;currentPath-显示这个路径下的图片)
 * showDeleteBtn:是否显示删除按钮,默认为false不显示)
 * 其中data与pics不能同时传入,data优先；position与currentPath不能同时传入，positon优先
 * @author 作者:tdx
 * @date 时间:2016年9月6日 下午4:06:51
 */
public class ImgBrowerPagerActivity extends BaseActivity {
    List<String> list;
    List<Pic> pics;
    String currentPath = "";
    int currentPage = 0;
    private boolean showDeleteBtn = false;//true显示删除按钮,false不显示删除按钮

    LinearLayout ll_content;
    private ViewPager mViewPager;
    private TextView tv_center;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.overridePendingTransition(0, 0);
    }

    @Override
    protected void getParmas(Intent intent) {
        list = intent.getStringArrayListExtra("data");
        if (list == null) {
            list = new ArrayList<String>();

            pics = (List<Pic>) intent.getSerializableExtra("pics");
            if (pics != null && pics.size() > 0) {
                for (Pic pic : pics) {
                    list.add(pic.url);
                }
            }
        }
        if (list == null) list = new ArrayList<>();
        currentPath = intent.getStringExtra("currentPath");
        if(!TextUtils.isEmpty(currentPath)){
            for (int p = 0; p < list.size(); p++){
                if(currentPath.equals(list.get(p))){
                    currentPage = p;
                    break;
                }
            }
        }else {
            currentPage = intent.getIntExtra("position", 0);
        }
        showDeleteBtn = intent.getBooleanExtra("showDeleteBtn", false);
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_img_brower;
    }

    @Override
    protected void initView() {
        ll_content = findView(R.id.ll_img_brower_content);
        tv_center = findView(R.id.tv_title_center);
        if (showDeleteBtn) {
            findView(R.id.iv_icon_right).setVisibility(View.VISIBLE);
        } else {
            findView(R.id.iv_icon_right).setVisibility(View.GONE);
        }
        View view = findView(R.id.rl_bottom);
        view.setBackgroundColor(Color.parseColor("#000000"));
        view.setAlpha(0.1f);
    }

    @Override
    protected void initData() {
        tv_center.setText(currentPage + 1 + "/" + list.size());
        mViewPager = new HackyViewPager(this);
        ll_content.addView(mViewPager);
        mViewPager.setAdapter(new ImgBrowerPagerAdapter(list));
        mViewPager.setCurrentItem(currentPage);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {
            }

            @Override
            public void onPageScrolled(int position, float arg1, int arg2) {
                currentPage = position;
                tv_center.setText(currentPage + 1 + "/" + list.size());
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
        findView(R.id.iv_icon_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPage >= list.size()) {
                    finshActivity();
                    return;
                }
                pics.get(currentPage).order = "";
                pics.get(currentPage).isSelect = false;
                //发送通知
                RxBus.get().post(BusAction.ACTION_DELETE_PIC, pics.get(currentPage));
                pics.remove(currentPage);
                list.remove(currentPage);
                mViewPager.setAdapter(new ImgBrowerPagerAdapter(list));
                if(currentPage!=0)currentPage = currentPage - 1;
                mViewPager.setCurrentItem(currentPage);
                tv_center.setText(currentPage + "/" + list.size());
                if (list.size() == 0) finshActivity();
            }
        });
    }

    @Override
    public String setStatisticsTitle() {
        return "大图浏览页";
    }

    public class ImgBrowerPagerAdapter extends PagerAdapter {
        List<String> urls;

        public ImgBrowerPagerAdapter(List<String> urls) {
            this.urls = urls;
            if (this.urls == null) this.urls = new ArrayList<String>();
        }

        @Override
        public int getCount() {
            return urls.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            final PhotoView photoView = new PhotoView(container.getContext());
            photoView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            photoView.setBackgroundColor(Color.rgb(0, 0, 0));
            photoView.enable();
            String url = urls.get(position);
            if (TextUtils.isEmpty(url)) url = "";
            ImageLoadHelper.getInstance().displayImageOrigin(ImgBrowerPagerActivity.this,url, photoView);
            container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            photoView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Bitmap temp = drawableToBitmap(photoView.getDrawable());
                    showDialog(temp);
                    return false;
                }
            });
            //单点返回
            photoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    KnmsApp.getInstance().finishActivity(ImgBrowerPagerActivity.this);
                    ImgBrowerPagerActivity.this.overridePendingTransition(0, 0);
                }
            });
            return photoView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        private void showDialog(final Bitmap bitmap) {
            DialogHelper.showBottomDialog(ImgBrowerPagerActivity.this, R.layout.dialog_decoration_style, new DialogHelper.OnEventListener<Dialog>() {
                @Override
                public void eventListener(View parentView, final Dialog window) {
                    TextView tvCancel = (TextView) parentView.findViewById(R.id.cancel_sava);
                    tvCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            window.dismiss();
                        }
                    });
                    TextView tvSava = (TextView) parentView.findViewById(R.id.sava_style_img);
                    tvSava.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String path = BitmapHelper.saveImageToGallery(bitmap);
                            Tst.showToast(TextUtils.isEmpty(path) ? "保存失败" : (path.contains("保存失败") ? path : "保存至" + path));
                            window.dismiss();
                        }
                    });
                }
            });
        }
    }

    public static void saveImageToGallery(Context context, Bitmap bmp) {
        // 首先保存图片
        File appDir = SDCardUtils.getDCIMDir(context);
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            Tst.showToast("下载失败！");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    file.getAbsolutePath(), fileName, null);
            Tst.showToast("保存成功！");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 最后通知图库更新
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
//        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(img_path))));

    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if(drawable == null) return null;
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ?
                        Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        //canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
