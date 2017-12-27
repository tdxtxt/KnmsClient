package com.knms.activity;

import com.baidu.location.BDLocation;
import com.knms.activity.base.HeadBaseActivity;
import com.knms.activity.comment.AddCommentActivity;
import com.knms.activity.dialog.ChooseAreaActivityDialog;
import com.knms.activity.login.FasterLoginActivity;
import com.knms.activity.pic.CameraActivityF;
import com.knms.activity.pic.ImgBrowerPagerActivity;
import com.knms.activity.repair.ReleaseRepairActivity;
import com.knms.bean.ResponseBody;
import com.knms.bean.idle.ReIdleClassify;
import com.knms.bean.myidle.MyIdle;
import com.knms.bean.other.City;
import com.knms.bean.other.Pic;
import com.knms.core.compress.Luban;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.net.RxRequestApi;
import com.knms.net.uploadfile.RxUploadApi;
import com.knms.util.DialogHelper;
import com.knms.core.location.LocationHelper;
import com.knms.util.SPUtils;
import com.knms.util.Tst;
import com.knms.view.AddPhotoView;
import com.knms.view.PopDialogView;
import com.knms.android.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

/**
 * Created by tdx on 2016/9/9.
 * 发布闲置界面
 */
public class ReleaseIdleActivity extends HeadBaseActivity implements OnClickListener {

    private static final int REQUEST_CODE_CLASSIFY = 0x00010;
    private static final int REQEST_CODE_CITY = 0x00011;

    private EditText mDescribeContent;
    private TextView mWords, mRelease, mLocation;
    private AddPhotoView addPhotoView;
    private LayoutInflater mInflater;
    private PopDialogView mPopDialog;
    private View popView;
    private TextView mClassification, mPrice, mSaveDraft, mNotSaveDraft, mCancel;

    private Subscription subscription;
    Observable<ResponseBody<String>> observable;
    private MyIdle old;
    private boolean isModified = false;
    private String firstClassifId = "";
    private final int REQUST_CODE_PHOTO = 0x00016;
    private int isShowRedMsg = 1;//是否显示小红点,0表示不显示1表示显示

    @Override
    protected void getParmas(Intent intent) {
        isShowRedMsg = intent.getIntExtra("isShowRedMsg", 1);
        old = (MyIdle) intent.getSerializableExtra("idle");
        if (intent.getBooleanExtra("isDraft", false)) {
            if (old == null) {
                old = SPUtils.getSerializable("draftIdle", MyIdle.class);
            }
        }
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_release_idle;
    }

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("发布闲置");
    }

    @Override
    protected void initView() {
        mInflater = LayoutInflater.from(this);
        mClassification = (TextView) findViewById(R.id.classification);
        mDescribeContent = (EditText) findViewById(R.id.describe_content);
        mWords = (TextView) findViewById(R.id.words);
        mPrice = (TextView) findViewById(R.id.price);
        mLocation = (TextView) findViewById(R.id.location);
        mRelease = (TextView) findViewById(R.id.release);

        popView = mInflater.inflate(R.layout.dialog_save_draft, null);
        mPopDialog = new PopDialogView(this);
        mPopDialog.setContentView(popView);
        mSaveDraft = (TextView) popView.findViewById(R.id.save_draft);
        mNotSaveDraft = (TextView) popView.findViewById(R.id.not_save_draft);
        mCancel = (TextView) popView.findViewById(R.id.cancel);
        addPhotoView = findView(R.id.addPhotoView);
    }

    @Override
    protected void initData() {
        findViewById(R.id.rl_choose_classification).setOnClickListener(this);
        findViewById(R.id.rl_make_a_price).setOnClickListener(this);
        findViewById(R.id.rl_choose_location).setOnClickListener(this);
        mRelease.setOnClickListener(this);
        mDescribeContent.addTextChangedListener(mTextWatcher);

        mSaveDraft.setOnClickListener(this);
        mNotSaveDraft.setOnClickListener(this);
        mCancel.setOnClickListener(this);

        updataView();

        if (!(old != null && !TextUtils.isEmpty(old.goarea))) getLocation();

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
                Intent intent = new Intent(ReleaseIdleActivity.this, ImgBrowerPagerActivity.class);
                intent.putExtra("position", position);
                intent.putExtra("pics", (Serializable) addPhotoView.getPhotos());
                intent.putExtra("showDeleteBtn", true);
                startActivity(intent);
            }
        });
    }

    @Override
    public String setStatisticsTitle() {
        return "发布闲置";
    }

    Subscription subscriptionLocation;

    private void getLocation() {
        if (subscriptionLocation != null) subscriptionLocation.unsubscribe();
        subscriptionLocation = LocationHelper.getInstance().startLocation().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<City>() {
                    @Override
                    public void call(City city) {
                        if (city != null) {
                            mLocation.setText(city.name);
                            areaId = city.id;
                        } else {
                            mLocation.setText("位置获取失败");
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mLocation.setText("位置获取失败");
                    }
                });
    }

    private void updataView() {
        if (old == null) return;
        if (!TextUtils.isEmpty(old.goarea)) {
            mLocation.setText(old.goareaname);
            areaId = old.goarea.split(",").length == 2 ? old.goarea.split(",")[1] : old.goarea;
        }
        if (!TextUtils.isEmpty(old.goclassifyid)) {
            mClassification.setText(old.goclassifyname);
            classifyId = old.goclassifyid.split(",").length == 2 ? old.goclassifyid.split(",")[1] : old.goclassifyid;
            firstClassifId = old.goclassifyid.split(",").length == 2 ? old.goclassifyid.split(",")[0] : "";
        }
        if (!TextUtils.isEmpty(old.coremark)) {
            mDescribeContent.setText(old.coremark);
            coremark = old.coremark;
        }
        if (old.goprice != 0) {
            mPrice.setText("￥" + old.goprice);
//            mPrice.setText("￥" + old.goprice + "(" + ((old.orprice != 0) ? "原价￥" + old.orprice + "、" : "") + (old.isfreeshop == 0 ? "包邮" : (freeshopprice == 0 ? "运费待议" : "运费￥" + freeshopprice)) + ")");
            price = old.goprice;
            orprice = old.orprice;
            isfreeshop = old.isfreeshop;
            freeshopprice = old.freeshopprice;
        }
        if (old.imglist != null && old.imglist.size() > 0) addPhotoView.addPhotos(old.imglist);
        checkSendContent(false);
    }

    String coremark;

    private boolean checkSendContent(boolean isToast) {
        mRelease.setTextColor(getResources().getColor(R.color.color_gray_999999));
        if (TextUtils.isEmpty(classifyId)) {
            if (isToast) Tst.showToast("请选择分类");
            mRelease.setBackgroundResource(R.drawable.bg_rectangle_gray);
            return false;
        }
        coremark = mDescribeContent.getText().toString();
        if (TextUtils.isEmpty(coremark)) {
            if (isToast) Tst.showToast("请填写描述");
            mRelease.setBackgroundResource(R.drawable.bg_rectangle_gray);
            return false;
        }
        if (!(addPhotoView.getPhotos() != null && addPhotoView.getPhotos().size() > 0)) {
            if (isToast) Tst.showToast("请添加图片");
            mRelease.setBackgroundResource(R.drawable.bg_rectangle_gray);
            return false;
        }
        if (TextUtils.isEmpty(mPrice.getText().toString())) {
            if (isToast) Tst.showToast("请发布价格");
            mRelease.setBackgroundResource(R.drawable.bg_rectangle_gray);
            return false;
        }
        if (TextUtils.isEmpty(areaId)) {
            if (isToast) Tst.showToast("请选择地区");
            mRelease.setBackgroundResource(R.drawable.bg_rectangle_gray);
            return false;
        }
        mRelease.setTextColor(getResources().getColor(R.color.color_black_333333));
        mRelease.setBackgroundResource(R.drawable.bg_rectangle_btn);
        return true;
    }

    @Override
    protected void reqApi() {
//        if (!checkSendContent(true)) return;
        if (!checkSendContent(false)) return;
        showProgress();
        List<Pic> photos = addPhotoView.getPhotos();

        final StringBuffer imgIds = new StringBuffer();
        final StringBuffer imgSeqs = new StringBuffer();
        int pos = 0;
        for (Pic pic : photos) {
            if (pos == 0) {
//                imgIds.append(pic.id);
                imgSeqs.append(pos);
            } else {
                imgSeqs.append("," + pos);
//                imgIds.append("," + pic.id);null,null54fd1d811c2746a5add7270403f6ee59,2cfb56ca743e4c6399a9d2181bc213b4,
            }
            pos++;
        }
        subscription = Observable.from(addPhotoView.getPhotos())
                .filter(new Func1<Pic, Boolean>() {
                    @Override
                    public Boolean call(Pic pic) {
                        if (TextUtils.isEmpty(pic.id)) {
                            return true;
                        }
                        imgIds.append(pic.id + ",");
                        return false;
                    }
                })
                .map(new Func1<Pic, File>() {
                    @Override
                    public File call(Pic pic) {
                        File file = new File(pic.url);
                        return Luban.with(ReleaseIdleActivity.this).load(file).get();
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
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
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
                        if (TextUtils.isEmpty(imgIds.toString())) {
                            hideProgress();
                            Tst.showToast("发布失败");
                            return;
                        }
                        if (imgIds.toString().endsWith(",")) {
                            imgIds.deleteCharAt(imgIds.length() - 1);
                        }
                        if (getIntent().getSerializableExtra("idle") != null) {//编辑
                            observable = RxRequestApi.getInstance().getApiService().editIdle(old.goid, classifyId, coremark, orprice, price, freeshopprice, isfreeshop, areaId
                                    , imgIds.toString(), imgSeqs.toString());
                        } else {
                            observable = RxRequestApi.getInstance().getApiService().releaseIdle(classifyId, coremark, orprice, price, freeshopprice, isfreeshop, areaId
                                    , imgIds.toString(), imgSeqs.toString(), isShowRedMsg);
                        }
                        if (subscription != null) subscription.unsubscribe();
                        subscription = observable.observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.io())
                                .cache()
                                .subscribe(new Action1<ResponseBody<String>>() {
                                    @Override
                                    public void call(ResponseBody<String> body) {
                                        hideProgress();
                                        Tst.showToast(body.desc);
                                        if (body.isSuccess()) {
                                            RxBus.get().post(BusAction.REFRESH_IDLE, "");
                                            Map<String, Object> parmas = new HashMap<String, Object>();
                                            if (getIntent().getBooleanExtra("isDraft", false))
                                                SPUtils.clearSerializable("draftIdle", MyIdle.class);
                                            if (getIntent().getSerializableExtra("idle") != null)
                                                parmas.put("id", ((MyIdle) getIntent().getSerializableExtra("idle")).goid);
                                            else
                                                parmas.put("id", body.data);
                                            startActivityAnimGeneral(UndercarriageDetailsActivity.class, parmas);
                                            ReleaseIdleActivity.super.finshActivity();
                                        }

                                    }
                                }, new Action1<Throwable>() {
                                    @Override
                                    public void call(Throwable throwable) {
                                        hideProgress();
                                        Tst.showToast("发布失败");
                                    }
                                });

                    }
                });


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_choose_classification:
                Map<String, Object> map = new HashMap<>();
                map.put("firstClassifId", firstClassifId);
                startActivityForResultAnimGeneral(CowryClassificationActivity.class, map, REQUEST_CODE_CLASSIFY);
                break;
            case R.id.rl_make_a_price:
                DialogHelper.showBottomDialog(this, R.layout.dialog_input_idle, new DialogHelper.OnEventListener<Dialog>() {
                    @Override
                    public void eventListener(View parentView, Dialog window) {
                        setViewContent(parentView, window);
                    }
                });
                break;
            case R.id.rl_choose_location:
                Intent intent = new Intent(this, ChooseAreaActivityDialog.class);
                startActivityForResult(intent, REQEST_CODE_CITY);
                break;
            case R.id.release:
                if (SPUtils.isLogin()) {
                    reqApi();
                } else {
                    startActivityAnimGeneral(FasterLoginActivity.class, null);
                }
                break;
            case R.id.save_draft:
                mPopDialog.dismiss();
                break;
            case R.id.not_save_draft:
                mPopDialog.dismiss();
                finish();//结束当前activity
                break;
            case R.id.cancel:
                mPopDialog.dismiss();
                break;
        }
    }

    private int isfreeshop = 1;//是否包邮（0：是，1：否）
    private double freeshopprice = 0;//邮费
    private double price, orprice = -1;//价格&原始价格

    private void setViewContent(View parentView, final Dialog window) {
        TextView tvBtn_complete = (TextView) parentView.findViewById(R.id.tvBtn_complete);
        final EditText et_price = (EditText) parentView.findViewById(R.id.et_price);
        final EditText et_orprice = (EditText) parentView.findViewById(R.id.et_orprice);
        final EditText et_freight = (EditText) parentView.findViewById(R.id.et_freight);
        final CheckBox cbox_baoyou = (CheckBox) parentView.findViewById(R.id.cbox_baoyou);
        parentView.findViewById(R.id.rl_cancel).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                price = TextUtils.isEmpty(et_price.getText().toString()) ? price : Double.parseDouble(et_price.getText().toString());
                mPrice.setText("￥" + price);
                orprice = TextUtils.isEmpty(et_orprice.getText().toString()) ? orprice : Double.parseDouble(et_orprice.getText().toString());
                freeshopprice = TextUtils.isEmpty(et_freight.getText().toString()) ? freeshopprice : Double.parseDouble(et_freight.getText().toString());
                isfreeshop = cbox_baoyou.isChecked() ? 0 : 1;
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0); //强制隐藏键盘
            }
        });
        if (freeshopprice != 0) {
            et_freight.setText(freeshopprice + "");
        }
        if (price != 0) {
            et_price.setText((int) price + "");
            et_price.setSelection(et_price.getText().toString().length());
        }
        if (orprice != -1) {
            et_orprice.setText((int) orprice + "");
            et_orprice.setSelection(et_orprice.getText().toString().length());
        }
        if (isfreeshop == 0) cbox_baoyou.setChecked(true);
        et_price.postDelayed(new Runnable() {
            @Override
            public void run() {
                et_price.requestFocus();
                showKeyboard();
            }
        }, 100);
        et_orprice.addTextChangedListener(pWatcher);
        et_price.addTextChangedListener(pWatcher);
        et_freight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String price = s.toString().trim();
                if (price.length() > 3) {
                    s.delete(price.length() - 1, price.length());
                    Tst.showToast("运费最大不超过999");
                }
            }
        });
        tvBtn_complete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isModified = true;
                String txt_price = et_price.getText().toString();
                if (!TextUtils.isEmpty(txt_price)) {
                    price = Double.parseDouble(txt_price);
                } else {
                    Tst.showToast("请输入价格");
                    return;
                }

                String txt_orprice = et_orprice.getText().toString();
                if (!TextUtils.isEmpty(txt_orprice)) {
                    orprice = Double.parseDouble(txt_orprice);
                } else {
                    orprice = -1;
//                    Tst.showToast("请输入原价");
                }

                if (!cbox_baoyou.isChecked()) { //不包邮
                    isfreeshop = 1;
                    String freight = et_freight.getText().toString();
                    if (!TextUtils.isEmpty(freight)) {
                        freeshopprice = Double.parseDouble(freight);
                    } else {
                        freeshopprice = 0;
//                        Tst.showToast("请输入运费");
//                        return;
                    }
                } else {//包邮
                    freeshopprice = 0;
                }
//                mPrice.setText("￥" + price + "(" + ((orprice != 0) ? "原价￥" + orprice + "、" : "") +
//                        (cbox_baoyou.isChecked() ? "包邮" : (freeshopprice == 0 ? "运费待议" : "运费￥" + freeshopprice)) + ")");
                mPrice.setText("￥" + price);
                hideKeyboard();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(v, InputMethodManager.SHOW_FORCED);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0); //强制隐藏键盘
                checkSendContent(false);
                window.dismiss();
            }
        });
        cbox_baoyou.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isfreeshop = 0;
                    et_freight.setText("");
                    et_freight.setEnabled(false);
                } else {
                    isfreeshop = 1;
                    et_freight.setEnabled(true);
                }
            }
        });

    }

    TextWatcher mTextWatcher = new TextWatcher() {
        private CharSequence temp;
        private int editStart;
        private int editEnd;

        @Override
        public void onTextChanged(CharSequence s, int start, int count,
                                  int after) {
            temp = s;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            checkSendContent(false);
            if (old == null)
                isModified = true;
            else if (!old.coremark.equals(s.toString()))
                isModified = true;
            editStart = mDescribeContent.getSelectionStart();
            editEnd = mDescribeContent.getSelectionEnd();
            mWords.setText(temp.length() + "/200");
        }
    };
    TextWatcher pWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            String price = s.toString().trim();
            if (price.length() > 6) {
                s.delete(price.length() - 1, price.length());
                Tst.showToast("价格最大不超过999999");
            }
        }
    };

    private String classifyId, areaId;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        addPhotoView.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        switch (requestCode) {
            case REQUEST_CODE_CLASSIFY:
                ReIdleClassify reIdleClassify = (ReIdleClassify) data.getSerializableExtra("classify");
                if (reIdleClassify != null) {
                    isModified = true;
                    mClassification.setText(reIdleClassify.name);
                    classifyId = reIdleClassify.id;
                }
                break;
            case REQEST_CODE_CITY:
                City city = (City) data.getSerializableExtra("city");
                if (city != null) {
                    isModified = true;
                    mLocation.setText(city.name);
                    areaId = city.id;
                }
                break;
            case REQUST_CODE_PHOTO:
                if ((List<Pic>) data.getSerializableExtra("photos") != null) {
                    isModified = true;
                    addPhotoView.setNewData((List<Pic>) data.getSerializableExtra("photos"));
                }
                break;
        }
        checkSendContent(false);
    }

    @Override
    public void finshActivity() {
        /*if (TextUtils.isEmpty(classifyId)
                && TextUtils.isEmpty(mDescribeContent.getText().toString())
                && !(addPhotoView.getPhotos() != null && addPhotoView.getPhotos().size() > 0)
                && TextUtils.isEmpty(mPrice.getText().toString())
                && TextUtils.isEmpty(areaId)) {
            super.finshActivity();
        }*/
        if (!isModified || TextUtils.isEmpty(classifyId)
                && TextUtils.isEmpty(mDescribeContent.getText().toString())
                && !(addPhotoView.getPhotos() != null && addPhotoView.getPhotos().size() > 0)
                && TextUtils.isEmpty(mPrice.getText().toString())
                ) {
            super.finshActivity();
        } else {
            DialogHelper.showBottomDialog(this, R.layout.dialog_save_draft, new DialogHelper.OnEventListener<Dialog>() {
                @Override
                public void eventListener(View parentView, final Dialog window) {
                    parentView.findViewById(R.id.not_save_draft).setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SPUtils.clearSerializable("draftIdle", MyIdle.class);
                            window.dismiss();
                            ReleaseIdleActivity.super.finshActivity();
                        }
                    });
                    parentView.findViewById(R.id.save_draft).setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            saveContent();
                            window.dismiss();
                            ReleaseIdleActivity.super.finshActivity();
                        }
                    });
                    parentView.findViewById(R.id.cancel).setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            window.dismiss();
                        }
                    });

                }
            });
        }
    }


    private void saveContent() {
        MyIdle reIdle = new MyIdle();
        reIdle.imglist = addPhotoView.getPhotos();
        reIdle.coremark = mDescribeContent.getText().toString();
        reIdle.goclassifyid = classifyId;
        reIdle.goclassifyname = mClassification.getText().toString();
        reIdle.goarea = areaId;
        reIdle.goareaname = mLocation.getText().toString();
        reIdle.goprice = price;
        reIdle.orprice = orprice;
        reIdle.isfreeshop = isfreeshop;//是否包邮；
        reIdle.freeshopprice = freeshopprice;//运费
        SPUtils.saveSerializable("draftIdle", reIdle);//保存
    }

    @Override
    protected void onDestroy() {
        if (subscription != null) subscription.unsubscribe();
        if (subscriptionLocation != null) subscriptionLocation.unsubscribe();
        super.onDestroy();
    }


    @Subscribe(tags = {@Tag(BusAction.ACTION_DELETE_PIC)})
    public void deleteSelectPic(Pic pic) {
        addPhotoView.removeItem(pic);
        isModified = true;
    }
}
