package com.yuyh.library.imgsel;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.gyf.barlibrary.ImmersionBar;
import com.knms.android.R;
import com.knms.bean.ResponseBody;
import com.knms.bean.other.AlbumFolder;
import com.knms.bean.other.Pic;
import com.knms.core.compress.Luban;
import com.knms.net.uploadfile.RxUploadApi;
import com.knms.util.Tst;
import com.knms.view.VerticalDrawerLayout;
import com.knms.view.progress.CircleProgressDialog;
import com.yuyh.library.imgsel.adapter.FolderListAdapterF;
import com.yuyh.library.imgsel.bean.Folder;
import com.yuyh.library.imgsel.common.Callback;
import com.yuyh.library.imgsel.common.Constant;
import com.yuyh.library.imgsel.utils.FileUtils;
import com.yuyh.library.imgsel.utils.StatusBarCompat;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static android.R.attr.path;


/**
 * https://github.com/smuyyh/ImageSelector
 * 返回参数
 * RESULT_LOACAL_PATH:List<String>对象,封装本地路径
 * RESULT_REMOTE_PIC:List<Pic>对象,封装上传图片对象或者是本地图片对象
 */
public class ImgSelActivity extends FragmentActivity implements View.OnClickListener, Callback {
    CircleProgressDialog circleProgressDialog;
    public static final String RESULT_LOACAL_PATH = "paths";//本地路径
    public static final String RESULT_REMOTE_PIC = "pics";//服务器url

    private static final int IMAGE_CROP_CODE = 1;
    private static final int STORAGE_REQUEST_CODE = 1;

    private ImgSelConfig config;

    private RelativeLayout rlTitleBar;
    private TextView tvTitle,tvPosition;
    private Button btnConfirm;
    private ImageView ivBack;
    private VerticalDrawerLayout verticalDrawerLayout;
    private RecyclerView folderRecyclerView;
    private FolderListAdapterF folderAdapter;
    private String cropImagePath;

    private ImgSelFragment fragment;

    private ArrayList<String> result = new ArrayList<>();
    private List<Pic> resultPic = new ArrayList<>();

    Subscription subscription;

    public void showProgress() {
        if (circleProgressDialog == null) circleProgressDialog = new CircleProgressDialog(this);
        if (circleProgressDialog.isShowing()) circleProgressDialog.dismiss();

        circleProgressDialog.showDialog();
    }

    public void hideProgress() {
        if (circleProgressDialog != null) circleProgressDialog.dismiss();
    }

    public static void startActivity(Activity activity, ImgSelConfig config, int RequestCode) {
        Intent intent = new Intent(activity, ImgSelActivity.class);
        Constant.config = config;
        activity.startActivityForResult(intent, RequestCode);
    }

    public static void startActivity(Fragment fragment, ImgSelConfig config, int RequestCode) {
        Intent intent = new Intent(fragment.getActivity(), ImgSelActivity.class);
        Constant.config = config;
        fragment.startActivityForResult(intent, RequestCode);
    }
    protected ImmersionBar mImmersionBar;
    protected void initBar(){
        View barView = findViewById(R.id.view);
        if(barView == null) return;
        mImmersionBar = ImmersionBar.with(this);
        mImmersionBar.statusBarView(barView)
//                .keyboardEnable(true)  //解决软键盘与底部输入框冲突问题
                .statusBarDarkFont(true, 0.1f)//状态栏字体是深色，不写默认为亮色
                .flymeOSStatusBarFontColor(R.color.status_bar_textcolor);  //修改flyme OS状态栏字体颜色;
        mImmersionBar.init();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_img_sel);
        config = Constant.config;
        initBar();

        // Android 6.0 checkSelfPermission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    STORAGE_REQUEST_CODE);
        } else {
            fragment = ImgSelFragment.instance();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fmImageList, fragment, null)
                    .commit();
        }

        initView();
        if (!FileUtils.isSdCardAvailable()) {
            Toast.makeText(this, getString(R.string.sd_disable), Toast.LENGTH_SHORT).show();
        }
    }

    private void initView() {
        rlTitleBar = (RelativeLayout) findViewById(R.id.rlTitleBar);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvPosition = (TextView) findViewById(R.id.tvPosition);
        tvTitle.setOnClickListener(this);
        btnConfirm = (Button) findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(this);

        verticalDrawerLayout = (VerticalDrawerLayout) findViewById(R.id.menu_bottom_layout);
        folderRecyclerView = (RecyclerView) findViewById(R.id.recycler_albumFolder);
        folderRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        folderAdapter = new FolderListAdapterF(null);
        folderAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<Folder>() {
            @Override
            public void onItemClick(BaseQuickAdapter<Folder, ? extends BaseViewHolder> adapter, View view, int position) {
                verticalDrawerLayout.closeDrawer();
                Folder item = adapter.getItem(position);
                folderAdapter.setSelected(position);
                fragment.updateImage(position,item);
                adapter.notifyDataSetChanged();
                if(!tvTitle.getText().toString().equals(item.name)){
                    Constant.imageList.clear();
                    onImageUnselected("");
                }
                tvTitle.setText(item.name);
            }
        });
        folderRecyclerView.setAdapter(folderAdapter);

        ivBack = (ImageView) findViewById(R.id.ivBack);
        ivBack.setOnClickListener(this);

        if (config != null) {
            if (config.backResId != -1) {
                ivBack.setImageResource(config.backResId);
            }

            if (config.statusBarColor != -1) {
                StatusBarCompat.compat(this, config.statusBarColor);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                        && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                }
            }
            if(config.titleBgColor > 0) rlTitleBar.setBackgroundColor(config.titleBgColor);
            if(config.titleColor > 0) tvTitle.setTextColor(config.titleColor);
            if(!TextUtils.isEmpty(config.title)) tvTitle.setText(config.title);
            if(config.btnBgColor > 0) btnConfirm.setBackgroundColor(config.btnBgColor);
            if(config.btnTextColor > 0) btnConfirm.setTextColor(config.btnTextColor);

            if (config.multiSelect) {
                if (!config.rememberSelected) {
                    Constant.imageList.clear();
                }
                btnConfirm.setText(String.format(getString(R.string.confirm), Constant.imageList.size(), config.maxNum));
            } else {
                Constant.imageList.clear();
                btnConfirm.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnConfirm) {
            if (Constant.imageList != null && !Constant.imageList.isEmpty()) {
                exit();
            } else {
//                Toast.makeText(this, "最少选择一张图片", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.ivBack) {
            onBackPressed();
        } else if(id == R.id.tvTitle){
            if (verticalDrawerLayout.isDrawerOpen()) {
                verticalDrawerLayout.closeDrawer();
                return;
            }
            folderAdapter.setNewData(fragment.getFolders());
            folderAdapter.notifyDataSetChanged();
            verticalDrawerLayout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    verticalDrawerLayout.openDrawerView();
                }
            }, 200);
        }
    }

    @Override
    public void onSingleImageSelected(String path) {
        if (config.needCrop) {
            crop(path);
        } else {
            Constant.imageList.add(path);
            exit();
        }
    }

    @Override
    public void onImageSelected(String path) {
        btnConfirm.setText(String.format(getString(R.string.confirm), Constant.imageList.size(), config.maxNum));
        btnConfirm.setTextColor(Constant.imageList.size() > 0 ? Color.parseColor("#333333") : Color.parseColor("#999999"));
        btnConfirm.setBackgroundColor(Constant.imageList.size() > 0 ? Color.parseColor("#FFDC50") : Color.parseColor("#E0E0E0"));
    }

    @Override
    public void onImageUnselected(String path) {
        btnConfirm.setText(String.format(getString(R.string.confirm), Constant.imageList.size(), config.maxNum));
        btnConfirm.setTextColor(Constant.imageList.size() > 0 ? Color.parseColor("#333333") : Color.parseColor("#999999"));
        btnConfirm.setBackgroundColor(Constant.imageList.size() > 0 ? Color.parseColor("#FFDC50") : Color.parseColor("#E0E0E0"));
    }

    @Override
    public void onCameraShot(File imageFile) {
        if (imageFile != null) {
            if (config.needCrop) {
                crop(imageFile.getAbsolutePath());
            } else {
                Constant.imageList.add(imageFile.getAbsolutePath());
                config.multiSelect = false; // 多选点击拍照，强制更改为单选
                exit();
            }
        }
    }

    @Override
    public void onPreviewChanged(int select, int sum, boolean visible) {
        if (visible) {
            tvTitle.setVisibility(View.GONE);
            tvPosition.setVisibility(View.VISIBLE);
            tvPosition.setText(select + "/" + sum);
        } else {
            tvTitle.setVisibility(View.VISIBLE);
            tvPosition.setVisibility(View.GONE);
        }
    }

    private void crop(String imagePath) {
        File file = new File(FileUtils.createRootPath(this) + "/" + System.currentTimeMillis() + ".jpg");

        cropImagePath = file.getAbsolutePath();
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(getImageContentUri(new File(imagePath)), "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", config.aspectX);
        intent.putExtra("aspectY", config.aspectY);
        intent.putExtra("outputX", config.outputX);
        intent.putExtra("outputY", config.outputY);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true);
        startActivityForResult(intent, IMAGE_CROP_CODE);
    }

    public Uri getImageContentUri(File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMAGE_CROP_CODE && resultCode == RESULT_OK) {
            Constant.imageList.add(cropImagePath);
            config.multiSelect = false; // 多选点击拍照，强制更改为单选
            exit();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void exit() {
        if (!this.config.uploadService) {
            Intent intent = new Intent();
            result.clear();
            result.addAll(Constant.imageList);
            resultPic.clear();
            Pic pic = null;
            for (String path : Constant.imageList) {
                pic = new Pic();
                pic.url = path;
                resultPic.add(pic);
            }
            intent.putStringArrayListExtra(RESULT_LOACAL_PATH, result);
            intent.putExtra(RESULT_REMOTE_PIC, (Serializable) resultPic);
            setResult(RESULT_OK, intent);
            Constant.imageList.clear();
            finish();
        }else {
            uploadService();
        }
    }

    private void uploadService() {
        Constant.pics.clear();
        showProgress();
        Observable.from(Constant.imageList)
                .map(new Func1<String, File>() {
                    @Override
                    public File call(String path) {
                        File file = new File(path);
                        return Luban.with(ImgSelActivity.this).load(file).get();
                    }
                })
                .flatMap(new Func1<File, Observable<ResponseBody<Pic>>>() {
            @Override
            public Observable<ResponseBody<Pic>> call(File file) {
                RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
                return RxUploadApi.getInstance().getApiService().uploadImage(body);
            }
        }).doOnError(new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Tst.showToast(throwable.toString());
            }
        }).onErrorResumeNext(new Func1<Throwable, Observable<? extends ResponseBody<Pic>>>() {
            @Override
            public Observable<? extends ResponseBody<Pic>> call(Throwable throwable) {
                return Observable.empty();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<Pic>>() {
                    @Override
                    public void call(ResponseBody<Pic> body) {
                        if (body.isSuccess() && body.data != null) Constant.pics.add(body.data);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        hideProgress();
                        Tst.showToast(throwable.toString());
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        hideProgress();
                        Intent intent = getIntent();
                        result.clear();
                        result.addAll(Constant.imageList);
                        resultPic.clear();
                        resultPic.addAll(Constant.pics);
                        intent.putStringArrayListExtra(RESULT_LOACAL_PATH, result);
                        intent.putExtra(RESULT_REMOTE_PIC, (Serializable) resultPic);
                        intent.putExtra("test", "test");
                        setResult(RESULT_OK, intent);
                        Constant.pics.clear();
                        Constant.imageList.clear();
                        finish();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case STORAGE_REQUEST_CODE:
                if (grantResults.length >= 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .add(R.id.fmImageList, ImgSelFragment.instance(), null)
                            .commitAllowingStateLoss();
                } else {
                    Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (fragment == null || !fragment.hidePreview()) {
            Constant.imageList.clear();
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (subscription != null) subscription.unsubscribe();
        Constant.config = null;
        if(mImmersionBar != null) mImmersionBar.destroy();
    }
}
