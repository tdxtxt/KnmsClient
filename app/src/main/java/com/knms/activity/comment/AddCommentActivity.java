package com.knms.activity.comment;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.gyf.barlibrary.ImmersionBar;
import com.knms.activity.QuickComplaintActivity;
import com.knms.activity.base.HeadBaseActivity;
import com.knms.activity.pic.CameraActivityF;
import com.knms.activity.pic.ImgBrowerPagerActivity;
import com.knms.android.R;
import com.knms.bean.ResponseBody;
import com.knms.bean.other.Pic;
import com.knms.core.compress.Luban;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.net.RxRequestApi;
import com.knms.net.uploadfile.RxUploadApi;
import com.knms.util.LocalDisplay;
import com.knms.util.ScreenUtil;
import com.knms.util.Tst;
import com.knms.view.AddPhotoView;
import com.knms.view.Star;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;

import static com.knms.util.ScreenUtil.getScreenHeight;

/**
 * Created by tdx on 2017/4/26.
 * 写评论
 */

public class AddCommentActivity extends HeadBaseActivity {
    private String orderId;//订单id
    private String content;//评价内容
    private double score;//评分（1到5颗星分别为 1.0、2.0....5.0）
    private String imgs;//图片id拼串，用逗号隔开

    private AddPhotoView addPhotoView;
    private int REQUST_CODE_PHOTO = AddPhotoView.REQUSTCODE_IMG;

    private EditText edit_comment;
    private TextView tv_calczise;
    private TextView tv_grade_state;
    private Star ratingBar;
    private Button btn_release;

    @Override
    protected void getParmas(Intent intent) {
        orderId = intent.getStringExtra("orderId");
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_add_comment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initSoftKeyChange(getWindow().getDecorView().getRootView());
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initView() {
        addPhotoView = findView(R.id.addPhotoView);
        edit_comment = findView(R.id.edit_comment);
        tv_calczise = findView(R.id.tv_calczise);
        tv_grade_state = findView(R.id.tv_grade_state);
        ratingBar = findView(R.id.ratingBar);
        btn_release = findView(R.id.btn_release);
    }
    private void initSoftKeyChange(final View rootView){
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);
                //获取然键盘的高度
                int heightDifference = getScreenHeight() - (r.bottom - r.top);
                int HeightDiffDip = LocalDisplay.px2dip(heightDifference);
                int softKeyHeight =  ScreenUtil.getScreenHeight() / 3;
                if (HeightDiffDip < 100) {
                    //键盘关闭
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            findView(R.id.btn_release).setVisibility(View.VISIBLE);
                        }
                    },200);
                }else {
                    //键盘开启
                    findView(R.id.btn_release).setVisibility(View.GONE);
                }
            }
        });
    }
    @Override
    protected void initData() {
        addPhotoView.setMaxPhotoCount(9);
        ratingBar.setStarChangeLister(new Star.OnStarChangeListener() {
            @Override
            public void onStarChange(Float mark) {
                int grade = mark.intValue();
                switch (grade){
                    case 1:
                        tv_grade_state.setText("很不满意");
                        break;
                    case 2:
                        tv_grade_state.setText("不太满意");
                        break;
                    case 3:
                        tv_grade_state.setText("满意");
                        break;
                    case 4:
                        tv_grade_state.setText("很满意");
                        break;
                    case 5:
                        tv_grade_state.setText("非常满意");
                        break;
                }
            }
        });
        edit_comment.addTextChangedListener(new TextWatcher() {
            private CharSequence temp;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                temp = s;
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                tv_calczise.setText(temp.length() + "/200");
            }
        });
        addPhotoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                hideKeyboard();
                return false;
            }
        });
        addPhotoView.setAddListener(new AddPhotoView.AddListener() {
            @Override
            public void onclick() {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("photos", addPhotoView.getPhotos());
                startActivityForResultAnimGeneral(CameraActivityF.class, params, REQUST_CODE_PHOTO);
            }
            @Override
            public void otherClick(int position) {
                Intent intent = new Intent(AddCommentActivity.this, ImgBrowerPagerActivity.class);
                intent.putExtra("position", position);
                intent.putExtra("pics", (Serializable) addPhotoView.getPhotos());
                intent.putExtra("showDeleteBtn", true);
                startActivity(intent);
            }
        });
        btn_release.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                score = ratingBar.getMark();
                content = edit_comment.getText().toString();
                if(TextUtils.isEmpty(content)){
                    Tst.showToast("请填写评价内容");
                    return;
                }
                reqApi();
            }
        });
    }

    @Override
    public String setStatisticsTitle() {
        return "写评价";
    }

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("写评价");
    }

    @Override
    protected void reqApi() {
        showProgress();
        final StringBuffer imgIds = new StringBuffer();
        Observable.from(addPhotoView.getPhotos())
                .filter(new Func1<Pic, Boolean>() {
                    @Override
                    public Boolean call(Pic pic) {
                        if (TextUtils.isEmpty(pic.id)) {
                            return true;
                        }
                        imgIds.append(pic.id + ",");
                        return false;
                    }
                }).map(new Func1<Pic, File>() {
            @Override
            public File call(Pic pic) {
                File file = new File(pic.url);
                return Luban.with(AddCommentActivity.this).load(file).get();
            }
        }).flatMap(new Func1<File, Observable<ResponseBody<Pic>>>() {
            @Override
            public Observable<ResponseBody<Pic>> call(File file) {
                RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
                return RxUploadApi.getInstance().getApiService().uploadImage(body);
            }
        }).doOnError(new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                throwable.printStackTrace();
            }
        }).onErrorResumeNext(new Func1<Throwable, Observable<? extends ResponseBody<Pic>>>() {
            @Override
            public Observable<? extends ResponseBody<Pic>> call(Throwable throwable) {
                return Observable.empty();
            }
        })
                .compose(this.<ResponseBody<Pic>>applySchedulers())
                .subscribe(new Action1<ResponseBody<Pic>>() {
                    @Override
                    public void call(ResponseBody<Pic> body) {
                        if (body.isSuccess() && body.data != null) {
                            imgIds.append(body.data.id + ",");
                        }
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
                        if (imgIds.toString().endsWith(",")) {
                            imgIds.deleteCharAt(imgIds.length() - 1);
                        }
                        RxRequestApi.getInstance().getApiService().commitComment(orderId,content,score,imgIds.toString())
                                .compose(AddCommentActivity.this.<ResponseBody>applySchedulers())
                                .subscribe(new Action1<ResponseBody>() {
                                    @Override
                                    public void call(ResponseBody body) {
                                        if(body.isSuccess()){
                                            Tst.showToast("评价发布成功");
                                            RxBus.get().post(BusAction.REFRESH_MY_ORDER,"");//刷新订单
                                            HashMap<String,Object> parmas = new HashMap<String, Object>();
                                            parmas.put("orderId",orderId);
                                            startActivityAnimGeneral(CommentInfoActivity.class,parmas);
                                            AddCommentActivity.this.finshActivity();

                                        }else{
                                            Tst.showToast(body.desc);
                                        }
                                    }
                                }, new Action1<Throwable>() {
                                    @Override
                                    public void call(Throwable throwable) {
                                        Tst.showToast("发布失败");
                                    }
                                });
                    }
                });
    }

    @Subscribe(tags = {@Tag(BusAction.ACTION_DELETE_PIC)})
    public void deleteSelectPic(Pic pic) {
        addPhotoView.removeItem(pic);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        if((List<Pic>) data.getSerializableExtra("photos")!=null){
            addPhotoView.setNewData((List<Pic>) data.getSerializableExtra("photos"));
        }
    }
}
