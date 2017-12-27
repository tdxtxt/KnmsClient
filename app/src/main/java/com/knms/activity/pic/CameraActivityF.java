package com.knms.activity.pic;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.knms.activity.base.BaseActivity;
import com.knms.activity.main.PriceRationActivity;
import com.knms.adapter.AlbumFolderAdapter;
import com.knms.adapter.BrowserPicAdapterF;
import com.knms.adapter.MediaPicAdapter;
import com.knms.adapter.baserecycler.BaseQuickAdapter;
import com.knms.android.R;
import com.knms.app.KnmsApp;
import com.knms.bean.other.AlbumFolder;
import com.knms.bean.other.Pic;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.other.LoaderMediaSubscribeF;
import com.knms.util.BitmapHelper;
import com.knms.util.CameraHelper;
import com.knms.util.CommonUtils;
import com.knms.util.LocalDisplay;
import com.knms.util.SDCardUtils;
import com.knms.util.ScreenUtil;
import com.knms.util.Tst;
import com.knms.view.VerticalDrawerLayout;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ValueAnimator;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


/**
 * Created by tdx on 2016/9/13.
 * http://www.10tiao.com/html/169/201702/2650822025/1.html
 * 比比价相片选取界面(照相&相册)
 */
public class CameraActivityF extends BaseActivity {
    /***********
     * 数据源
     **************/
    private List<Pic> initPics;
    private List<Pic> allPics;
    private List<Pic> selectPics = new ArrayList<>();
    private int selectPosition;
    private boolean isFromAlbum;//true:从相册中加载图片;false:从灵感界面传入图片中加载图片
    private int startY,endY;//动画位置
    private boolean isOpen = false;//动画状态
    private int isShowRedMsg = 1;//发布比比价是否显示小红点1:显示;0:不显示
    /***********
     * 控件
     **************/
    private RecyclerView recyclerView;
    private ImageView iv_arrow;
    private SurfaceView surfaceView;
    private ImageView ivBtn_pz;//拍照按钮
    private ImageView iv_flash_light;//闪光灯按钮
    private TextView tv_select_count;//选择图片的数量
    private ViewPager browserView;//图片浏览器
    private RelativeLayout rl_browser;//图片浏览器夫布局
    private TextView tv_browser_position;
    private TextView tvBtn_browser_all;
    private VerticalDrawerLayout verticalDrawerLayout;
    private RecyclerView recyclerAlbumFolder;//相册文件夹
    /***********
     * 照相机
     **************/
    private String path;
    private int PHOTO_SIZE = 2000;
    private CameraHelper mCameraHelper;
    private Camera.Parameters parameters = null;
    private Camera cameraInst = null;
    private int mCurrentCameraId = 0;  //1是前置 0是后置
    private Bundle bundle = null;
    private boolean mPreviewing = false;
    /***********
     * 相册
     **************/
    private BrowserPicAdapterF browserPicAdapter;
    private MediaPicAdapter mediaPicAdapter;//
    private AlbumFolderAdapter albumFolderAdapter;
    private int column = 5;
    private int MAX_SELECT_COUNT = 9;

    private SensorManager sm;
    private int jqAngle = 0;
    @Override
    protected void getParmas(Intent intent) {
        isShowRedMsg = intent.getIntExtra("isShowRedMsg",1);
        initPics = (List<Pic>) intent.getSerializableExtra("photos");//从发布界面中传过来的值
        allPics = (List<Pic>) intent.getSerializableExtra("remotePics");//从灵感比比价中传过来的值
        selectPosition = intent.getIntExtra("selectPosition", -1);//从灵感比比价中传过来的值
        if (allPics == null) {
            isFromAlbum = true;
        } else {
            isFromAlbum = false;
        }

        if (selectPosition > -1 && allPics != null && allPics.size() > selectPosition) {
            allPics.get(selectPosition).order = "1";
            allPics.get(selectPosition).isSelect = true;
            if (selectPics == null) selectPics = new ArrayList<Pic>();
            selectPics.add(allPics.get(selectPosition));
        }
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_camera_new;
    }


    SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (Sensor.TYPE_ACCELEROMETER != event.sensor.getType()) {
                return;
            }
            float[] values = event.values;
            int orientation = -1;
            float X = -values[0];
            float Y = -values[1];
            float Z = -values[2];
            float magnitude = X*X + Y*Y;
            // Don't trust the angle if the magnitude is small compared to the y value
            if (magnitude * 4 >= Z*Z) {
                float OneEightyOverPi = 57.29577957855f;
                float angle = (float)Math.atan2(-Y, X) * OneEightyOverPi;
                orientation = 90 - (int)Math.round(angle);
                // normalize to 0 - 359 range
                while (orientation >= 360) {
                    orientation -= 360;
                }
                while (orientation < 0) {
                    orientation += 360;
                }
            }
            if (orientation > 45 && orientation < 135) {//右横
                jqAngle = 180;
            } else if (orientation > 135 && orientation < 225) {//倒屏
                jqAngle = mCurrentCameraId == 0 ? 270 : 90;
            } else if (orientation > 225 && orientation < 315) {//左横
                jqAngle = 0;
            } else if ((orientation > 315 && orientation < 360)
                    || (orientation > 0 && orientation < 45)) {//竖屏
                jqAngle = mCurrentCameraId == 0 ? 90 : 270;
            } else{//水平
                jqAngle = mCurrentCameraId == 0 ? jqAngle : 270;
            }
            Log.i("angleXX",(mCurrentCameraId == 0 ? "后置:" : "前置:") + jqAngle + ";orientation:" + orientation);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    @Override
    public void initView() {
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        sm.registerListener(sensorEventListener, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        mCameraHelper = new CameraHelper(this);
        recyclerView = findView(R.id.recycler_view);
        iv_arrow = findView(R.id.iv_arrow);
        surfaceView = findView(R.id.surfaceView);
        iv_flash_light = findView(R.id.iv_flash_light);
        tv_select_count = findView(R.id.tv_select_count);
        browserView = findView(R.id.cp_browser_pic);
        rl_browser = findView(R.id.rl_browser);
        verticalDrawerLayout = findView(R.id.v_drawer_layout);
        if (!isFromAlbum) rl_browser.setVisibility(View.VISIBLE);//来源于灵感，则不需要照片
        tv_browser_position = findView(R.id.tv_browser_position);
        recyclerAlbumFolder = findView(R.id.recycler_albumFolder);
        tvBtn_browser_all = findView(R.id.tvBtn_browser_all);
        ivBtn_pz = findView(R.id.ivBtn_pz);
        /***********照相机**************/
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.setKeepScreenOn(true);
        surfaceView.setFocusable(true);
        surfaceView.setBackgroundColor(TRIM_MEMORY_BACKGROUND);
        surfaceView.getHolder().addCallback(new SurfaceCallback());//为SurfaceView的句柄添加一个回调函数

        recyclerView.setLayoutManager(new GridLayoutManager(this, column));
        recyclerAlbumFolder.setLayoutManager(new LinearLayoutManager(this));
        startY = (int) (ScreenUtil.getScreenHeight() * 0.11f);
        endY = (int) (ScreenUtil.getScreenHeight() * 0.32f);
        changCameraHeight(startY);
    }

    @Override
    public void initData() {
        setCaptrueLastIcon();//初始化视图
        CommonUtils.checkPermission(this, Manifest.permission.CAMERA);
        /*************加载相册************/
        if (mediaPicAdapter == null) {
            mediaPicAdapter = new MediaPicAdapter(this);
        }
        recyclerView.setAdapter(mediaPicAdapter);
        if (browserPicAdapter == null) {
            browserPicAdapter = new BrowserPicAdapterF(this);
        }
        browserView.setAdapter(browserPicAdapter);
        browserView.setOffscreenPageLimit(1);
        if (albumFolderAdapter == null) {
            albumFolderAdapter = new AlbumFolderAdapter(this,new ArrayList<AlbumFolder>(0));
            recyclerAlbumFolder.setAdapter(albumFolderAdapter);
        }
        loadMedia().map(new Func1<List<Pic>, List<Pic>>() {
            @Override
            public List<Pic> call(List<Pic> pics) {
                if (!(initPics != null && initPics.size() > 0)) return allPics = pics;
                int pos = 1;
                for (Pic pic : pics) {//初始化数据
                    if (initPics.toString().contains(pic.url)) {
                        pic.isSelect = true;
                        pic.order = pos + "";
                        selectPics.add(pic);
                        pos++;
                    } else {
                        pic.isSelect = false;
                        pic.order = "";
                    }
                }
                for (Pic pic : initPics) {
                    if(pics == null){
                        selectPics.add(pic);
                    }else{
                        if(!pics.toString().contains(pic.url)){
                            selectPics.add(pic);
                        }
                    }
                }
                return allPics = pics;
            }
        }).subscribe(new Action1<List<Pic>>() {
            @Override
            public void call(List<Pic> pics) {
                updateViewPics(pics);
                browserPicAdapter.setNewData(selectPics);
                updateViewBrowserPics();
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Tst.showToast("加载图片失败");
            }
        });
        initEvent();//注册点击事件
    }

    @Override
    public String setStatisticsTitle() {
        return "相片选取页(照相&相册)";
    }

    private void updateViewPics(List<Pic> pics) {
        if (selectPics != null && selectPics.size() > 0) {//若有选择的照片，显示右上角选中的数目图标
            tv_select_count.setVisibility(View.VISIBLE);
            tv_select_count.setText(selectPics.size() + "");
        }
        mediaPicAdapter.setNewData(pics);
    }

    private Observable<List<Pic>> loadMedia() {
        if (isFromAlbum) {//从相册中加载
            return loadAlubmFolder()
                    .map(new Func1<List<AlbumFolder>, List<Pic>>() {
                        @Override
                        public List<Pic> call(List<AlbumFolder> albumFolders) {
                            for (AlbumFolder albumFolder : albumFolders) {
                                if ("所有图片".equals(albumFolder.name)) {
                                    return albumFolder.pics;
                                }
                            }
                            return null;
                        }
                    });
        }
        if (allPics == null) allPics = new ArrayList<>();
        return Observable.just(allPics).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    private Observable<List<AlbumFolder>> loadAlubmFolder() {
        return Observable.create(new LoaderMediaSubscribeF()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).cache();
    }

    /** 改变高度 */
    private void changCameraHeight(int height){
        ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
        params.height = height;
        recyclerView.setLayoutParams(params);
    }
    private void openView() {
        if(isOpen) return;
        isOpen = true;
        ValueAnimator anim = ValueAnimator.ofInt(startY, endY);
        anim.setDuration(500);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int currentValue = Integer.parseInt(animation.getAnimatedValue().toString());
                changCameraHeight(currentValue);
            }
        });
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                iv_arrow.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                iv_arrow.setEnabled(true);
                iv_arrow.setImageResource(R.drawable.bg_down_arrow);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        anim.start();
    }

    private void closeView() {
        if(!isOpen) return;
        isOpen = false;
        ValueAnimator anim = ValueAnimator.ofInt(endY, startY);
        anim.setDuration(500);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int currentValue = Integer.parseInt(animation.getAnimatedValue().toString());
                changCameraHeight(currentValue);
            }
        });
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                iv_arrow.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                iv_arrow.setEnabled(true);
                iv_arrow.setImageResource(R.drawable.bg_up_arrow);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        anim.start();
    }

    private void setCaptrueAfterIcon() {
        findView(R.id.iv_confirm).setVisibility(View.VISIBLE);
        findView(R.id.iv_cancel).setVisibility(View.VISIBLE);
        ivBtn_pz.setVisibility(View.GONE);
    }

    private void setCaptrueLastIcon() {
        findView(R.id.iv_confirm).setVisibility(View.GONE);
        findView(R.id.iv_cancel).setVisibility(View.GONE);
        ivBtn_pz.setVisibility(View.VISIBLE);
    }

    @Subscribe(tags = {@Tag(BusAction.ACTION_DELETE_PIC)})
    public void deleteSelectPic(Pic pic) {
        if (selectPics == null) selectPics = new ArrayList<>();
        if (allPics == null) allPics = new ArrayList<>();
        selectPics.remove(pic);
        browserPicAdapter.remove(pic);
        orderByPic(selectPics);
        updateViewBrowserPics();

        for (Pic item : allPics) {
            if (item.equals(pic)) {
                item.order = "";
                item.isSelect = false;
                break;
            }
        }
        mediaPicAdapter.notifyDataSetChanged();
    }

    private void initEvent() {
        ivBtn_pz.setOnClickListener(onClickListener);
        iv_arrow.setOnClickListener(onClickListener);
        findView(R.id.iv_confirm).setOnClickListener(onClickListener);
        findView(R.id.iv_cancel).setOnClickListener(onClickListener);
        findView(R.id.iv_close).setOnClickListener(onClickListener);
        findView(R.id.iv_trun).setOnClickListener(onClickListener);
        findView(R.id.llBtn_next).setOnClickListener(onClickListener);
        tvBtn_browser_all.setOnClickListener(onClickListener);
        iv_flash_light.setOnClickListener(onClickListener);
        browserView.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (selectPics == null) selectPics = new ArrayList<Pic>();
                tv_browser_position.setText((selectPics.size() == 0 ? 0 : position + 1) + "/" + selectPics.size());
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        mediaPicAdapter.setOnRecyclerViewItemClickListener(new BaseQuickAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Pic item = (Pic) mediaPicAdapter.getItem(position);
                switch (item.getItemType()) {
                    case Pic.IMG_NORMAR://正常的图片
                        item.isSelect = !item.isSelect;
                        if (item.isSelect) { //选中
                            if (selectPics.size() >= MAX_SELECT_COUNT) {
                                item.isSelect = false;
                                Tst.showToast("最多可选择" + MAX_SELECT_COUNT + "张照片");
                                return;
                            }
                            selectPics.add(item);
                            browserPicAdapter.add(item);
                        } else {
                            selectPics.remove(item);
                            item.order = "";
                            browserPicAdapter.remove(item);
                        }
                        orderByPic(selectPics);//排序
                        mediaPicAdapter.notifyDataSetChanged();//更新
                        updateViewBrowserPics();
                        break;
                    case Pic.IMG_CAMERA://拍照图片,点击变为照相模式
                        rl_browser.setVisibility(View.GONE);
                        mediaPicAdapter.setAlbumMode();//设置为相册图片
                        findViewById(R.id.iv_trun).setVisibility(View.VISIBLE);
                        findView(R.id.iv_flash_light).setVisibility(View.VISIBLE);
                        tvBtn_browser_all.setVisibility(View.GONE);
                        closeView();
                        break;
                    case Pic.IMG_ALBUM://相册图片,点击变为相册模式
                        openView();
                        updateViewBrowserPics();
                        tvBtn_browser_all.setVisibility(View.VISIBLE);
                        rl_browser.setVisibility(View.VISIBLE);
                        findViewById(R.id.iv_trun).setVisibility(View.GONE);
                        findView(R.id.iv_flash_light).setVisibility(View.GONE);
                        mediaPicAdapter.setCameraMode();//设置为照相图片
                        break;
                }
            }
        });
        albumFolderAdapter.setOnRecyclerViewItemClickListener(new BaseQuickAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                AlbumFolder albumFolder = albumFolderAdapter.getItem(position);
                tvBtn_browser_all.setText(albumFolder.name);
                Observable.just(albumFolder.pics).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).cache()
                        .map(new Func1<List<Pic>, List<Pic>>() {
                            @Override
                            public List<Pic> call(List<Pic> pics) {
                                if (selectPics == null) return allPics = pics;
                                for (Pic pic : pics) {//初始化数据
                                    for (Pic item : selectPics) {
                                        if (item.url.equals(pic.url)) {
                                            pic.isSelect = true;
                                            pic.order = item.order;
                                        }
                                    }
                                }
                                return allPics = pics;
                            }
                        }).subscribe(new Action1<List<Pic>>() {
                    @Override
                    public void call(List<Pic> pics) {
                        updateViewPics(pics);
                        browserPicAdapter.setNewData(selectPics);
                        updateViewBrowserPics();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Tst.showToast("加载图片失败");
                    }
                });
                verticalDrawerLayout.closeDrawer();
            }
        });
    }

    private void updateViewBrowserPics() {
        if (selectPics == null) selectPics = new ArrayList<>();
        if (selectPics.size() > 0) {
            openView();
            findViewById(R.id.iv_trun).setVisibility(View.GONE);
            findView(R.id.iv_flash_light).setVisibility(View.GONE);
            tv_select_count.setVisibility(View.VISIBLE);
            tv_select_count.setText(selectPics.size() + "");
            if (isFromAlbum) {
                tvBtn_browser_all.setVisibility(View.VISIBLE);
                rl_browser.setVisibility(View.VISIBLE);
                mediaPicAdapter.setCameraMode();
//                openView();
            }
            browserView.setCurrentItem(browserPicAdapter.getCount());
        } else {
            if (isFromAlbum) {
                rl_browser.setVisibility(View.GONE);
                mediaPicAdapter.setAlbumMode();
                findViewById(R.id.iv_trun).setVisibility(View.VISIBLE);
                findView(R.id.iv_flash_light).setVisibility(View.VISIBLE);
            }else{
                findViewById(R.id.iv_trun).setVisibility(View.GONE);
                findView(R.id.iv_flash_light).setVisibility(View.GONE);
            }
            tvBtn_browser_all.setVisibility(View.GONE);
            tv_select_count.setVisibility(View.GONE);
            tv_select_count.setText("");
//            closeView();
        }
        tv_browser_position.setVisibility(browserPicAdapter.getCount()==0?View.GONE:View.VISIBLE);
        tv_browser_position.setText(browserPicAdapter.getCount() + "/" + browserPicAdapter.getCount());
    }

    private void orderByPic(List<Pic> data) {
        for (int i = 0; i < data.size(); i++) {
            data.get(i).order = "" + (i + 1);
        }
    }

    private long lastClickTime;
    public boolean isFastClick() {
        boolean flag = false;
        long curClickTime = System.currentTimeMillis();
        if ((curClickTime - lastClickTime) >= 1000) {
            flag = true;
        }
        lastClickTime = curClickTime;
        return flag;
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.ivBtn_pz://拍照
                    if(isFastClick()) {
                        try {
                            mPreviewing = false;
                            cameraInst.takePicture(null, null, new MyPictureCallback());
                        } catch (Throwable t) {
                            t.printStackTrace();
                            Tst.showToast("拍照失败，请重试！");
                            try {
                                cameraInst.startPreview();
                                mPreviewing = true;
                            } catch (Throwable e) {
                            }
                        }
                    }
                    break;
                case R.id.iv_confirm:
                    //保存图片
                    cameraInst.startPreview();
                    mPreviewing = true;
                    Pic pic = new Pic();
                    pic.setItemType(Pic.IMG_NORMAR);
                    pic.url = path;
                    if (selectPics.size() < MAX_SELECT_COUNT) {//还可以选
                        pic.isSelect = true;
                        selectPics.add(pic);
                        orderByPic(selectPics);
                        browserPicAdapter.add(pic);
                        updateViewBrowserPics();
                    }
                    mediaPicAdapter.add(1, pic);
                    setCaptrueLastIcon();
                    break;
                case R.id.iv_cancel:
                    //取消保存图片
                    cameraInst.startPreview();
                    mPreviewing = true;
                    setCaptrueLastIcon();
                    BitmapHelper.deleteBitmap(path);
                    // 最后通知图库更新已删除的图片
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(path))));
                    break;
                case R.id.iv_arrow://收放高度
                    if(isOpen){
                        closeView();
                    }else {
                        openView();
                    }
                    break;
                case R.id.iv_close:
                    finshActivity();
                    break;
                case R.id.iv_trun:
                    switchCamera();
                    break;
                case R.id.iv_flash_light:
                    turnLight(cameraInst);
                    break;
                case R.id.llBtn_next:
                    if (initPics != null) {//表示重新选择
                        Intent intent = new Intent();
                        intent.putExtra("photos", (Serializable) selectPics);
                        if (!isFromAlbum) intent.putExtra("allPhotos", (Serializable) allPics);
                        setResult(RESULT_OK, intent);
                        KnmsApp.getInstance().finishActivity(CameraActivityF.this);
                    } else {
                        Map<String, Object> paramst = new HashMap<String, Object>();
                        paramst.put("photos", selectPics);
                        if (!isFromAlbum) paramst.put("allPhotos", allPics);
                        paramst.put("isShowRedMsg",isShowRedMsg);
                        startActivityAnimGeneral(PriceRationActivity.class, paramst);
                        CameraActivityF.this.finshActivity();
                    }
                    break;
                case R.id.tvBtn_browser_all:
                    if (verticalDrawerLayout.isDrawerOpen()) {
                        verticalDrawerLayout.closeDrawer();
                        return;
                    }
                    loadAlubmFolder().subscribe(new Action1<List<AlbumFolder>>() {
                        @Override
                        public void call(List<AlbumFolder> albumFolders) {
                            albumFolderAdapter.setNewData(albumFolders);
                        }
                    });
                    verticalDrawerLayout.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            verticalDrawerLayout.openDrawerView();
                        }
                    }, 200);
                    break;
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
//        orientationEventListener.disable();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        orientationEventListener.enable();
    }

    /**
     * 闪光灯开关   开->关->自动
     *
     * @param mCamera
     */
    private void turnLight(Camera mCamera) {
        if (mCamera == null || mCamera.getParameters() == null
                || mCamera.getParameters().getSupportedFlashModes() == null) {
            return;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        String flashMode = mCamera.getParameters().getFlashMode();
        List<String> supportedModes = mCamera.getParameters().getSupportedFlashModes();
        if (Camera.Parameters.FLASH_MODE_OFF.equals(flashMode)
                && supportedModes.contains(Camera.Parameters.FLASH_MODE_ON)) {//关闭状态
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
            mCamera.setParameters(parameters);
            iv_flash_light.setImageResource(R.drawable.btn_camera_flash_on);
        } else if (Camera.Parameters.FLASH_MODE_ON.equals(flashMode)) {//开启状态
            if (supportedModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                iv_flash_light.setImageResource(R.drawable.btn_camera_flash_auto);
                mCamera.setParameters(parameters);
            } else if (supportedModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                iv_flash_light.setImageResource(R.drawable.btn_camera_flash_off);
                mCamera.setParameters(parameters);
            }
        } else if (Camera.Parameters.FLASH_MODE_AUTO.equals(flashMode)
                && supportedModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(parameters);
            iv_flash_light.setImageResource(R.drawable.btn_camera_flash_off);
        }
    }

    //切换前后置摄像头
    private void switchCamera() {
        mCurrentCameraId = (mCurrentCameraId + 1) % mCameraHelper.getNumberOfCameras();
        releaseCamera();
        setUpCamera(mCurrentCameraId);
    }

    private void releaseCamera() {
        if (cameraInst != null) {
            cameraInst.setPreviewCallback(null);
            cameraInst.release();
            cameraInst = null;
        }
        adapterSize = null;
        previewSize = null;
    }

    private void setUpCamera(int mCurrentCameraId2) {
        cameraInst = getCameraInstance(mCurrentCameraId2);
        if (cameraInst != null) {
            try {
                cameraInst.setPreviewDisplay(surfaceView.getHolder());//绑定绘制预览图像的surface
                initCamera();
                cameraInst.startPreview();
                mPreviewing = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Tst.showToast("切换失败，请重试！");

        }
    }

    private Camera getCameraInstance(final int id) {
        Camera c = null;
        try {
            c = mCameraHelper.openCamera(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    /*SurfaceCallback*/
    private final class SurfaceCallback implements SurfaceHolder.Callback {

        public void surfaceDestroyed(SurfaceHolder holder) {
            try {
                if (cameraInst != null) {
                    cameraInst.stopPreview();
                    mPreviewing = false;
                    cameraInst.release();
                    cameraInst = null;
                }
            } catch (Exception e) {
                //相机已经关了
            }
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (null == cameraInst) {
                try {
                    cameraInst = Camera.open();
                    cameraInst.setPreviewDisplay(holder);
                    initCamera();
                    cameraInst.startPreview();
                    mPreviewing = true;
                } catch (Throwable e) {
                    if (cameraInst != null) cameraInst.release();
                    cameraInst = null;
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if (holder.getSurface() == null){
                return;
            }
            autoFocus();
        }
    }
    Subscription subscription;
    //实现自动对焦
    private void autoFocus() {
        if(subscription != null) subscription.unsubscribe();
        subscription = Observable.interval(1, 2, TimeUnit.SECONDS)//延时1秒 ，每间隔5秒
                .compose(this.<Long>applySchedulers())
        .subscribe(new Action1<Long>() {
            @Override
            public void call(Long aLong) {
                if (cameraInst != null && mPreviewing) {
                    cameraInst.autoFocus(new Camera.AutoFocusCallback() {
                        @Override
                        public void onAutoFocus(boolean success, Camera camera) {
                            if (success) {
                                initCamera();//实现相机的参数初始化
                                camera.cancelAutoFocus();//只有加上了这一句，才会自动对焦。
                            }else {
                                camera.cancelAutoFocus();//只有加上了这一句，才会自动对焦。
                            }
                        }
                    });
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
//                Tst.showToast("对焦失败!");
                autoFocus();
            }
        });
    }
    private Camera.Size adapterSize = null;//摄像头的分辨率
    private Camera.Size previewSize = null;//预览分辨率

    private void initCamera() {
        parameters = cameraInst.getParameters();
        parameters.setPictureFormat(PixelFormat.JPEG);
        //调整方向
//        setDispaly(parameters, cameraInst);
        setCameraDisplayOrientation(mCurrentCameraId,cameraInst);
        //if (adapterSize == null) {
        setUpPicSize(parameters);
        setUpPreviewSize(parameters);
        //}
        if (adapterSize != null) {
            parameters.setPictureSize(adapterSize.width, adapterSize.height);
        }
        if (previewSize != null) {
            parameters.setPreviewSize(previewSize.width, previewSize.height);
        }
//        parameters.setPreviewFpsRange(5,7);//每秒帧
//        parameters.setJpegQuality(90); // 设置照片质量

        List<String> focusModes = parameters.getSupportedFocusModes();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);//1连续对焦
            }
        } else {
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
        }
        if (parameters.isZoomSupported()) {
            parameters.setZoom(parameters.getZoom());
        }

        try {
            cameraInst.setParameters(parameters);
            cameraInst.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mPreviewing = true;
        cameraInst.cancelAutoFocus();// 2如果要实现连续的自动对焦，这一句必须加上
    }

    /**
     * 拍照图片分辨率选择
     * @param parameters
     */
    private void setUpPicSize(Camera.Parameters parameters) {
        if (adapterSize != null) {
            return;
        } else {
            adapterSize = findBestPictureResolution(parameters);
            return;
        }
    }

    /**
     * 预览图片分辨率选择
     * @param parameters
     */
    private void setUpPreviewSize(Camera.Parameters parameters) {
        if (previewSize != null) {
            return;
        } else {
            Point p = findBestPreviewResolution(parameters);
            previewSize =  cameraInst.new Size(p.x,p.y);
        }
    }

    /**
     * 最小预览界面的分辨率
     */
    private static final int MIN_PREVIEW_PIXELS = 480 * 320;
    /**
     * 最大宽高比差
     */
    private static final double MAX_ASPECT_DISTORTION = 0.15;
    private static final String TAG = "Camera";
    /**
     * 找出最适合的预览界面分辨率
     *
     * @return
     */
    private Point findBestPreviewResolution(Camera.Parameters cameraParameters) {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        double x = dm.widthPixels;
        double y = dm.heightPixels;

        Camera.Size defaultPreviewResolution = cameraParameters.getPreviewSize();
        Log.d(TAG, "camera default resolution " + defaultPreviewResolution.width + "x" + defaultPreviewResolution.height);

        List<Camera.Size> rawSupportedSizes = cameraParameters.getSupportedPreviewSizes();
        if (rawSupportedSizes == null) {
            Log.w(TAG, "Device returned no supported preview sizes; using default");
            return new Point(defaultPreviewResolution.width, defaultPreviewResolution.height);
        }

        // 按照分辨率从大到小排序
        List<Camera.Size> supportedPreviewResolutions = new ArrayList<Camera.Size>(rawSupportedSizes);
        Collections.sort(supportedPreviewResolutions, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size a, Camera.Size b) {
                int aPixels = a.height * a.width;
                int bPixels = b.height * b.width;
                if (bPixels < aPixels) {
                    return -1;
                }
                if (bPixels > aPixels) {
                    return 1;
                }
                return 0;
            }
        });

        StringBuilder previewResolutionSb = new StringBuilder();
        for (Camera.Size supportedPreviewResolution : supportedPreviewResolutions) {
            previewResolutionSb.append(supportedPreviewResolution.width).append('x').append(supportedPreviewResolution.height)
                    .append(' ');
        }
        Log.v(TAG, "Supported preview resolutions: " + previewResolutionSb);


        // 移除不符合条件的分辨率
        double screenAspectRatio = x / y;
        Iterator<Camera.Size> it = supportedPreviewResolutions.iterator();
        while (it.hasNext()) {
            Camera.Size supportedPreviewResolution = it.next();
            int width = supportedPreviewResolution.width;
            int height = supportedPreviewResolution.height;

            // 移除低于下限的分辨率，尽可能取高分辨率
            if (width * height < MIN_PREVIEW_PIXELS) {
                it.remove();
                continue;
            }

            // 在camera分辨率与屏幕分辨率宽高比不相等的情况下，找出差距最小的一组分辨率
            // 由于camera的分辨率是width>height，我们设置的portrait模式中，width<height
            // 因此这里要先交换然preview宽高比后在比较
            boolean isCandidatePortrait = width > height;
            int maybeFlippedWidth = isCandidatePortrait ? height : width;
            int maybeFlippedHeight = isCandidatePortrait ? width : height;
            double aspectRatio = (double) maybeFlippedWidth / (double) maybeFlippedHeight;
            double distortion = Math.abs(aspectRatio - screenAspectRatio);
            if (distortion > MAX_ASPECT_DISTORTION) {
                it.remove();
                continue;
            }

            // 找到与屏幕分辨率完全匹配的预览界面分辨率直接返回
            if (maybeFlippedWidth == x && maybeFlippedHeight == y) {
                Point exactPoint = new Point(width, height);

                return exactPoint;
            }
        }

        // 如果没有找到合适的，并且还有候选的像素，则设置其中最大比例的，对于配置比较低的机器不太合适
        if (!supportedPreviewResolutions.isEmpty()) {
            Camera.Size largestPreview = supportedPreviewResolutions.get(0);
            Point largestSize = new Point(largestPreview.width, largestPreview.height);
            return largestSize;
        }

        // 没有找到合适的，就返回默认的
        Point defaultResolution = new Point(defaultPreviewResolution.width, defaultPreviewResolution.height);

        return defaultResolution;
    }

    /**
     * 拍照图片分辨率
     * @return
     */
    private Camera.Size findBestPictureResolution(Camera.Parameters cameraParameters) {
        List<Camera.Size> supportedPicResolutions = cameraParameters.getSupportedPictureSizes(); // 至少会返回一个值

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        List<Camera.Size> temp = new ArrayList<>();
        for(int i = 0;i < supportedPicResolutions.size();i ++){
            //如果又符合的分辨率，则直接返回
            if (supportedPicResolutions.get(i).width >= dm.widthPixels
                    && supportedPicResolutions.get(i).height >= dm.heightPixels){
                temp.add(supportedPicResolutions.get(i));
            }
        }
        int bestX = 0;
        int bestY = 0;
        int diff = Integer.MAX_VALUE;
        if(temp != null && temp.size() > 0){
            for (Camera.Size size : temp) {
                int newDiff = Math.abs(size.width - dm.widthPixels)
                        + Math.abs(size.height - dm.heightPixels);
                float ratio = size.height / size.width;
                if(newDiff < diff && ratio != 0.75){
                    diff = newDiff;
                    bestX = size.width;
                    bestY = size.height;
                }
            }
        }
        if(bestX > 0 && bestY > 0){
            return cameraInst.new Size(bestX,bestY);
        }
        Camera.Size defaultPictureResolution = cameraParameters.getPictureSize();
        return defaultPictureResolution;
        /*DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        double x = dm.widthPixels > dm.heightPixels ? dm.widthPixels : dm.heightPixels;
        double y = dm.widthPixels < dm.heightPixels ? dm.widthPixels : dm.heightPixels;

        Camera.Parameters cameraParameters = cameraInst.getParameters();
        List<Camera.Size> supportedPicResolutions = cameraParameters.getSupportedPictureSizes(); // 至少会返回一个值

        StringBuilder picResolutionSb = new StringBuilder();
        for (Camera.Size supportedPicResolution : supportedPicResolutions) {
            picResolutionSb.append(supportedPicResolution.width).append('x').append(supportedPicResolution.height).append(" ");
        }
        Log.d(TAG, "Supported picture resolutions: " + picResolutionSb);

        Camera.Size defaultPictureResolution = cameraParameters.getPictureSize();
        Log.d(TAG, "default picture resolution " + defaultPictureResolution.width + "x" + defaultPictureResolution.height);

        // 排序
        List<Camera.Size> sortedSupportedPicResolutions = new ArrayList<Camera.Size>(supportedPicResolutions);
        Collections.sort(sortedSupportedPicResolutions, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size a, Camera.Size b) {
                int aPixels = a.height * a.width;
                int bPixels = b.height * b.width;
                if (bPixels < aPixels) {
                    return -1;
                }
                if (bPixels > aPixels) {
                    return 1;
                }
                return 0;
            }
        });

        // 移除不符合条件的分辨率
        double screenAspectRatio = x / y;
        Iterator<Camera.Size> it = sortedSupportedPicResolutions.iterator();
        while (it.hasNext()) {
            Camera.Size supportedPreviewResolution = it.next();
            int width = supportedPreviewResolution.width;
            int height = supportedPreviewResolution.height;

            // 在camera分辨率与屏幕分辨率宽高比不相等的情况下，找出差距最小的一组分辨率
            // 由于camera的分辨率是width>height，我们设置的portrait模式中，width<height
            // 因此这里要先交换然后在比较宽高比
            boolean isCandidatePortrait = width > height;
            int maybeFlippedWidth = isCandidatePortrait ? height : width;
            int maybeFlippedHeight = isCandidatePortrait ? width : height;
            double aspectRatio = (double) maybeFlippedWidth / (double) maybeFlippedHeight;
            double distortion = Math.abs(aspectRatio - screenAspectRatio);
            if (distortion > MAX_ASPECT_DISTORTION) {
                it.remove();
                continue;
            }
        }

        // 如果没有找到合适的，并且还有候选的像素，对于照片，则取其中最大比例的，而不是选择与屏幕分辨率相同的
        if (!sortedSupportedPicResolutions.isEmpty()) {
            Camera.Size largestPreview = sortedSupportedPicResolutions.get(0);
            Point largestSize = new Point(largestPreview.width, largestPreview.height);
            Log.d(TAG, "using largest suitable picture resolution: " + largestSize);
            return largestSize;
        }

        // 没有找到合适的，就返回默认的
        Point defaultResolution = new Point(defaultPictureResolution.width, defaultPictureResolution.height);
        Log.d(TAG, "No suitable picture resolutions, using default: " + defaultResolution);

        return defaultResolution;*/
    }

    private double getFormat(int formatX, int formatY) {
        DecimalFormat format = new DecimalFormat("#.0");
        double result = Double.parseDouble(format.format((double) formatX / (double) formatY));
        return result;
    }

    //控制图像的正确显示方向
    private void setDispaly(Camera.Parameters parameters, Camera camera) {
        if (Build.VERSION.SDK_INT >= 8) {
            // MZ 180， other 90...
            if ("M9".equalsIgnoreCase(Build.MODEL) || "MX".equalsIgnoreCase(Build.MODEL)) {
                setDisplayOrientation(camera, 180);
            } else {
                setDisplayOrientation(camera, 90);
            }
        } else {
            parameters.set("orientation", "portrait");
            parameters.set("rotation", 90);
        }
    }
    public void setCameraDisplayOrientation(int cameraId, Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (360 - (info.orientation + degrees) % 360) % 360;
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        Log.i("setCameraDis",result + "");
        camera.setDisplayOrientation(result);
    }

    //实现的图像的正确显示
    private void setDisplayOrientation(Camera camera, int i) {
        Method downPolymorphic;
        try {
            downPolymorphic = camera.getClass().getMethod("setDisplayOrientation",
                    new Class[]{int.class});
            if (downPolymorphic != null) {
                downPolymorphic.invoke(camera, new Object[]{i});
            }
        } catch (Exception e) {
            Log.e("Came_e", "图像出错");
        }
    }

    private final class MyPictureCallback implements Camera.PictureCallback {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            bundle = new Bundle();
            bundle.putByteArray("bytes", data); //将图片字节数据保存在bundle当中，实现数据交换
            new SavePicTask(data).execute();
//            camera.startPreview(); // 拍完照后，重新开始预览
        }
    }

    private class SavePicTask extends AsyncTask<Void, Void, String> {
        private byte[] data;

        protected void onPreExecute() {
            showProgress();
        }

        SavePicTask(byte[] data) {
            this.data = data;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
//                return saveToSDCard(data);
                return savePicture(data);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (!TextUtils.isEmpty(result)) {
                path = result;
                hideProgress();
                //数据处理完成
                setCaptrueAfterIcon();
            } else {
                Tst.showToast("拍照失败，请稍后重试！");
            }
        }
    }

    private String savePicture(byte[] data) {
        BufferedOutputStream bos = null;
        Bitmap bm = null;
        Bitmap bmEnd = null;
        try {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            // 获得图片
            bm = BitmapFactory.decodeByteArray(data, 0, data.length, opts);
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            opts.inSampleSize = calculateInSampleSize(opts, dm.heightPixels, dm.widthPixels);
            opts.inPurgeable = true;
            opts.inInputShareable = true;
            opts.inTempStorage = new byte[64 * 1024];//为位图设置64K的缓存
            opts.inJustDecodeBounds = false;
            bm = BitmapFactory.decodeByteArray(data, 0, data.length, opts);
            bmEnd = BitmapHelper.imageCrop(bm,4,3,true);
            if (bmEnd != null) {
                bmEnd = rotate(bmEnd, jqAngle);
            }
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                // 首先保存图片
                File appDir = SDCardUtils.getDCIMDir(KnmsApp.getInstance());
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss"); // 格式化时间
                String fileName = format.format(new Date()) + ".jpg";
                File file = new File(appDir, fileName);

                if (!file.exists()) {
                    file.createNewFile();
                }
                bos = new BufferedOutputStream(new FileOutputStream(file));
//                bmEnd = BitmapHelper.imageCrop(bm,4,3,true);
                bmEnd.compress(Bitmap.CompressFormat.JPEG, 90, bos);//将图片压缩到流中

                // 通知图库更新
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                return file.getAbsolutePath();
            } else {
                Tst.showToast("没有内存");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                bos.flush();//输出
                bos.close();//关闭
                if(bm != null) bm.recycle();// 回收bitmap空间
                if(bmEnd != null) bmEnd.recycle();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public Bitmap rotate(Bitmap b, int degrees) {
        return rotateAndMirror(b, degrees, mCurrentCameraId == 1);
    }

    public Bitmap rotateAndMirror(Bitmap b, int degrees, boolean mirror) {
        if ((degrees != 0 || mirror) && b != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees/*, (float) b.getWidth() / 2, (float) b.getHeight() / 2*/);
            if (mirror) {
                m.postScale(-1, 1);
                degrees = (degrees + 360) % 360;
                if (degrees == 0 || degrees == 180) {
                    m.postTranslate((float) b.getWidth(), 0);
                } else if (degrees == 90 || degrees == 270) {
                    m.postTranslate((float) b.getHeight(), 0);
                } else {
                    throw new IllegalArgumentException("Invalid degrees=" + degrees);
                }
            }
            try {
                Bitmap b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), m, true);
                if (b != b2) {
                    b.recycle();
                    b = b2;
                }
            } catch (OutOfMemoryError ex) {
                // We have no memory to rotate. Return the original bitmap.
            }
        }
        return b;
    }

    /**
     * 将拍下来的照片存放在SD卡中
     *
     * @param data
     * @throws IOException
     */
    public String saveToSDCard(byte[] data) throws IOException {
        Bitmap croppedImage;

        //获得图片大小
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);

        PHOTO_SIZE = options.outHeight > options.outWidth ? options.outWidth : options.outHeight;
        int height = options.outHeight > options.outWidth ? options.outHeight : options.outWidth;
        options.inJustDecodeBounds = false;
        Rect r;
        if (mCurrentCameraId == 1) {
            r = new Rect(height - PHOTO_SIZE, 0, height, PHOTO_SIZE);
        } else {
            r = new Rect(0, 0, PHOTO_SIZE, PHOTO_SIZE);
        }
        try {
            croppedImage = decodeRegionCrop(data, r);
        } catch (Exception e) {
            return null;
        }
        String imagePath = BitmapHelper.saveImageToGallery(croppedImage);
        croppedImage.recycle();
        return imagePath;
    }

    private Bitmap decodeRegionCrop(byte[] data, Rect rect) {
        InputStream is = null;
        System.gc();
        Bitmap croppedImage = null;
        try {
            is = new ByteArrayInputStream(data);
            BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(is, false);

            try {
                croppedImage = decoder.decodeRegion(rect, new BitmapFactory.Options());
            } catch (IllegalArgumentException e) {
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {

            }
        }
        Matrix m = new Matrix();
        m.setRotate(90, PHOTO_SIZE / 2, PHOTO_SIZE / 2);
        if (mCurrentCameraId == 1) {
            m.postScale(1, -1);
        }
        Bitmap rotatedImage = Bitmap.createBitmap(croppedImage, 0, 0, PHOTO_SIZE, PHOTO_SIZE, m, true);
        if (rotatedImage != croppedImage)
            croppedImage.recycle();
        return rotatedImage;
    }

    @Override
    public void finshActivity() {
        if (verticalDrawerLayout.isDrawerOpen()) {
            verticalDrawerLayout.closeDrawer();
        } else {
            super.finshActivity();
        }
    }

    @Override
    protected void onDestroy() {
        sm.unregisterListener(sensorEventListener);
        releaseCamera();
        if (allPics != null) allPics.clear();
        if (selectPics != null) selectPics.clear();
        if (initPics != null) initPics.clear();
        allPics = null;
        selectPics = null;
        initPics = null;
        subscription.unsubscribe();
        System.gc();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && verticalDrawerLayout.isDrawerOpen()) {
            verticalDrawerLayout.closeDrawer();
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

}
