package com.knms.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.knms.activity.base.BaseActivity;
import com.knms.activity.base.BaseFragmentActivity;
import com.knms.activity.comment.AddCommentsActivity;
import com.knms.activity.pic.ImgBrowerPagerActivity;
import com.knms.android.R;
import com.knms.bean.ResponseBody;
import com.knms.bean.other.Pic;
import com.knms.core.compress.Luban;
import com.knms.net.uploadfile.RxUploadApi;
import com.knms.util.ImageLoadHelper;
import com.knms.util.LocalDisplay;
import com.knms.view.clash.SquareCenterImageView;
import com.yuyh.library.imgsel.ImgSelActivity;
import com.yuyh.library.imgsel.ImgSelConfig;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by tdx on 2016/9/8.
 * 添加图片控件
 */
public class AddPhotoView extends GridView {
    public static final int REQUSTCODE_IMG = 0x1322601;
    private AddPhotoAdapter mAddPhotoAdapter;
    public final String MODE_ONE = "1";
    public final String MODE_TWO = "2";
    private String MODE = MODE_ONE;
    private AddListener addListener;
    private boolean ismodified=false;
    private Context context;

    public AddPhotoView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public AddPhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public AddPhotoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        init();
    }

    private void init() {
        setNumColumns(4);
        setColumnWidth(LocalDisplay.dp2px(60));
        setHorizontalSpacing(LocalDisplay.dp2px(5));
        setVerticalSpacing(LocalDisplay.dp2px(5));
        setGravity(Gravity.CENTER);
        setPadding(15,15,15,15);
        setSelector(new ColorDrawable(Color.TRANSPARENT));

        this.mAddPhotoAdapter = new AddPhotoAdapter();
        setAdapter(mAddPhotoAdapter);
        setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mAddPhotoAdapter.getItem(position) == null) {
                    if(MODE_ONE.equals(MODE)){
                        ImgSelConfig config = new ImgSelConfig.Builder()
                                // 是否多选
                                .multiSelect(true)
                                .uploadService(false)
                                // “确定”按钮背景色
                                .btnBgColor(Color.parseColor("#ffdc50"))
                                .btnNotSelBgColor(Color.parseColor("#e0e0e0"))
                                // “确定”按钮文字颜色
                                .btnTextColor(Color.parseColor("#333333"))
                                .btnNotSelTextColor(Color.parseColor("#999999"))
                                // 使用沉浸式状态栏
                                .statusBarColor(Color.parseColor("#FFFFFF"))
                                .titleBgColor(Color.parseColor("#FFFFFF"))                                // 返回图标ResId
                                .backResId(R.drawable.sign_63)
                                // 标题文字颜色
                                .titleColor(Color.parseColor("#333333"))
                                // 裁剪大小。needCrop为true的时候配置
//                .cropSize(1, 1, 200, 200)
                                .needCrop(false)
                                // 第一个是否显示相机
                                .needCamera(true)
                                // 最大选择图片数量
                                .maxNum(mAddPhotoAdapter.getMaxPhotoCount() - mAddPhotoAdapter.getPhotos().size())
                                .build();
                        // 跳转到图片选择器
                        ImgSelActivity.startActivity((Activity) parent.getContext(), config, REQUSTCODE_IMG);
                    }else { //用户自行定义
                        if(addListener != null) addListener.onclick();
                    }
                } else {
                    if(MODE_ONE.equals(MODE)){
                        //浏览图片
                        Map<String, Object> parmas = new HashMap<String, Object>();
                        parmas.put("position", position);
                        parmas.put("pics", mAddPhotoAdapter.getPhotos());
                        parmas.put("showDeleteBtn",true);
                        if (parent.getContext() instanceof BaseFragmentActivity) {
                            BaseFragmentActivity act = (BaseFragmentActivity) parent.getContext();
                            act.startActivityAnimGeneral(ImgBrowerPagerActivity.class, parmas);
                        } else if (parent.getContext() instanceof BaseActivity) {
                            BaseActivity act = (BaseActivity) parent.getContext();
                            act.startActivityAnimGeneral(ImgBrowerPagerActivity.class, parmas);
                        } else {
                            Intent intent = new Intent(parent.getContext(), ImgBrowerPagerActivity.class);
                            intent.putExtra("position", position);
                            intent.putExtra("pics", (Serializable)mAddPhotoAdapter.getPhotos());
                            ((Activity) parent.getContext()).startActivity(intent);
                        }
                    }else { //用户自行定义
                        if(addListener != null) addListener.otherClick(position);
                    }
                }
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUSTCODE_IMG) {
                List<Pic> pics = (List<Pic>) data.getSerializableExtra(ImgSelActivity.RESULT_REMOTE_PIC);
                if(pics == null) pics = (List<Pic>) data.getSerializableExtra("photos");
                if(pics != null && pics.size() > 0){
                    addPhotos(pics);
                    ismodified=true;
                }
            }
        }
    }

    public void setMaxPhotoCount(int maxPhotoCount) {
        mAddPhotoAdapter.setMaxPhotoCount(maxPhotoCount);
        mAddPhotoAdapter.notifyDataSetChanged();
    }

    public void addPhotos(List<Pic> photos) {
        mAddPhotoAdapter.addPhotos(photos);
        mAddPhotoAdapter.notifyDataSetChanged();
    }
    public void addPhoto(Pic photo) {
        mAddPhotoAdapter.addPhoto(photo);
        mAddPhotoAdapter.notifyDataSetChanged();
    }
    public void setNewData(List<Pic> photos){
        mAddPhotoAdapter.setNewData(photos);
        mAddPhotoAdapter.notifyDataSetChanged();
    }
    public void removeItem(Pic item){
        mAddPhotoAdapter.removeItem(item);
        mAddPhotoAdapter.notifyDataSetChanged();
    }
    public boolean isModified(){
        return ismodified;
    }

    public List<Pic> getPhotos(){
        return mAddPhotoAdapter.getPhotos();
    }
    public Observable<String> getServicePhotos(){
        List<Pic> data = mAddPhotoAdapter.getPhotos();
        if(!(data != null && data.size() > 0)) return null;
        return Observable.from(data)
                .map(new Func1<Pic, File>() {
                    @Override
                    public File call(Pic pic) {
                        File file = new File(pic.url);
                        return Luban.with(context).load(file).get();
                    }
                })
                .flatMap(new Func1<File, Observable<ResponseBody<Pic>>>() {
                    @Override
                    public Observable<ResponseBody<Pic>> call(File file) {
                        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
                        return RxUploadApi.getInstance().getApiService().uploadImage(body);
                    }
                }).buffer(mAddPhotoAdapter.getCount())

             /*   .flatMap(new Func1<File, Observable<List<ResponseBody<Pic>>>>() {
            @Override
            public Observable<List<ResponseBody<Pic>>> call(File file) {
                RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
                return RxUploadApi.getInstance().getApiService().uploadImage(body).buffer(mAddPhotoAdapter.getCount());
            }
        })*/
                .flatMap(new Func1<List<ResponseBody<Pic>>, Observable<String>>() {
                    @Override
                    public Observable<String> call(List<ResponseBody<Pic>> bodys) {
                        StringBuffer stringBuffer = new StringBuffer();
                        boolean isOk = true;
                        for (ResponseBody<Pic> body : bodys) {
                            if (body.isSuccess()) {
                                isOk = body.isSuccess() && isOk;
                                stringBuffer.append(body.data.id + ",");
                            }
                        }
                        if(isOk){
                            if (stringBuffer.toString().endsWith(",")) {
                                stringBuffer.deleteCharAt(stringBuffer.length() - 1);
                            }
                        }
                        return Observable.just(stringBuffer.toString());
                    }
                });
    }
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            return true;// 禁止Gridview进行滑动
        }
        return super.dispatchTouchEvent(ev);
    }

    public void setAddListener(AddListener listener) {
        this.MODE = MODE_TWO;
        this.addListener = listener;
    }

    private class AddPhotoAdapter extends BaseAdapter {
        private List<Pic> mPhotos;
        private int mMaxPhotoCount = 9;

        public AddPhotoAdapter() {
            this.mPhotos = new ArrayList<Pic>();
        }

        public void setMaxPhotoCount(int maxPhotoCount) {
            this.mMaxPhotoCount = maxPhotoCount;
        }

        public void addPhotos(List<Pic> photos) {
            mPhotos.addAll(photos);
        }
        public void addPhoto(Pic photo) {
            mPhotos.add(photo);
        }
        public void setNewData(List<Pic> photos){
            mPhotos.clear();
            mPhotos.addAll(photos);
        }
        public void removeItem(Pic item){
            mPhotos.remove(item);
        }
        public int getMaxPhotoCount() {
            return mMaxPhotoCount;
        }

        public List<Pic> getPhotos() {
            return mPhotos;
        }

        @Override
        public int getCount() {
            if (mPhotos == null) {
                return 1;
            } else {
                if (mPhotos.size() == mMaxPhotoCount) {
                    return mPhotos.size();
                } else {
                    return mPhotos.size() + 1;
                }
            }
        }

        @Override
        public Object getItem(int position) {
            return position < mPhotos.size() ? mPhotos.get(position) : null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = new SquareCenterImageView(parent.getContext());
            if (position < mPhotos.size()) {
                convertView.setPadding(5,5,5,5);
                ImageLoadHelper.getInstance().displayImage(getContext(),mPhotos.get(position).url, (ImageView) convertView,LocalDisplay.dp2px(80),LocalDisplay.dp2px(80));
            } else if(position == mPhotos.size()){
                convertView.setPadding(5,5,5,5);
                ImageLoadHelper.getInstance().displayImage(getContext(),R.drawable.icon_addphoto, (ImageView) convertView,LocalDisplay.dp2px(80),LocalDisplay.dp2px(80));
            }
            return convertView;
        }
    }
    public interface AddListener {
        public void onclick();
        public void otherClick(int positon);
    }
}
