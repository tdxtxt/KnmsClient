package com.knms.activity.repair;

import android.app.Dialog;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.knms.activity.base.HeadBaseActivity;
import com.knms.activity.dialog.ChooseAreaActivityDialog;
import com.knms.activity.mine.MineRepairDetailActivity;
import com.knms.activity.pic.CameraActivityF;
import com.knms.activity.pic.ImgBrowerPagerActivity;
import com.knms.adapter.RepairTypeAdapter;
import com.knms.android.R;
import com.knms.bean.ResponseBody;
import com.knms.bean.other.City;
import com.knms.bean.other.Pic;
import com.knms.bean.repair.MyRepair;
import com.knms.bean.repair.RepairType;
import com.knms.core.compress.Luban;
import com.knms.core.location.LocationHelper;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.net.RxRequestApi;
import com.knms.net.uploadfile.RxUploadApi;
import com.knms.util.DialogHelper;
import com.knms.util.SPUtils;
import com.knms.util.Tst;
import com.knms.view.AddPhotoView;
import com.knms.view.flowlayout.FlowLayout;
import com.knms.view.flowlayout.TagFlowLayout;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
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
 * Created by Administrator on 2016/10/17.
 */
public class ReleaseRepairActivity extends HeadBaseActivity implements View.OnClickListener {
    private static final int REQEST_CODE_CITY = 0x00011;
    private AddPhotoView mAddPhotoView;
    private TagFlowLayout mTagFlowLayout;
    private TextView tvRelease, tvLocation, tvNum;
    private List<Pic> picList = new ArrayList<>();
    private RepairTypeAdapter mAdapter;
    private String remark = "", area = "", repairType = "", typeName = "";
    private EditText mRemark;
    private MyRepair myRepair;
    private Subscription subscription;
    private List<Pic> imgIds=new ArrayList<>();
    private boolean isModified = false;//是否有修改
    private final int REQUST_CODE_PHOTO = 0x00016;
    private int isShowRedMsg = 1;//是否显示小红点,0表示不显示1表示显示

    @Override
    protected void getParmas(Intent intent) {
        isShowRedMsg = intent.getIntExtra("isShowRedMsg",1);
        if (intent.getBooleanExtra("isDraft", false)) {
            if (myRepair == null) {
                myRepair = SPUtils.getSerializable("draftRepair", MyRepair.class);
            }
        }
    }

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("家具维修");
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_release_repair_layout;
    }

    @Override
    protected void initView() {
        mAddPhotoView = findView(R.id.addPhotoView);
        mTagFlowLayout = findView(R.id.tag_flow_layout_hot);
        tvRelease = findView(R.id.release);
        mRemark = findView(R.id.remark);
        tvLocation = findView(R.id.tv_Location);
        tvNum = findView(R.id.tv_num);
        tvLocation.setOnClickListener(this);
        tvRelease.setOnClickListener(this);
        mRemark.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (myRepair == null)
                    isModified = true;
                else if (!myRepair.reremark.equals(editable.toString()))
                    isModified = true;
                tvNum.setText(editable.length() + "/200");
                checkContent(false);
            }
        });
        mAddPhotoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                hideKeyboard();
                return false;
            }
        });
        mAddPhotoView.setAddListener(new AddPhotoView.AddListener() {
            @Override
            public void onclick() {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("photos", mAddPhotoView.getPhotos());
                startActivityForResultAnimGeneral(CameraActivityF.class, params, REQUST_CODE_PHOTO);
            }

            @Override
            public void otherClick(int position) {
                Intent intent = new Intent(ReleaseRepairActivity.this, ImgBrowerPagerActivity.class);
                intent.putExtra("position", position);
                intent.putExtra("pics", (Serializable) mAddPhotoView.getPhotos());
                intent.putExtra("showDeleteBtn", true);
                startActivity(intent);
            }
        });
        reqApi();
    }

    @Override
    protected void initData() {
        if (myRepair != null) {
            picList.clear();
            picList = myRepair.pic;
            mAddPhotoView.addPhotos(picList);
            mRemark.setText(myRepair.reremark);
            tvLocation.setText(myRepair.rearea);
            area = myRepair.areaId;
            repairType = myRepair.repairTypeId;
            remark = myRepair.reremark;
            typeName = myRepair.retype;
        }else{
            getLocation();
        }
        checkContent(false);

    }

    @Override
    public String setStatisticsTitle() {
        return "发布家具维修";
    }

    @Override
    protected void reqApi() {

        RxRequestApi.getInstance().getApiService().getRepairType()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<List<RepairType>>>() {
                    @Override
                    public void call(ResponseBody<List<RepairType>> listResponseBody) {
                        if (listResponseBody.isSuccess())
                            updateRepairTypeView(listResponseBody.data);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Tst.showToast(throwable.getMessage());
                    }
                });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.release:

                if (checkContent(true))
                    uploadService();
                break;
            case R.id.tv_Location:
                Intent intent = new Intent(this, ChooseAreaActivityDialog.class);
                startActivityForResult(intent, REQEST_CODE_CITY);
                break;
        }
    }

    private void ReleaseRepair() {
        showProgress();
        String picId = "";
        for(int i=0;i<imgIds.size();i++){
            picId+=imgIds.get(i).id+",";
        }
        if(picId.length()>0){
            picId=picId.substring(0,picId.length()-1);
        }
        RxRequestApi.getInstance().getApiService().releaseRepair(remark, area, repairType, picId,isShowRedMsg)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<String>>() {
                    @Override
                    public void call(ResponseBody<String> responseBody) {
                        hideProgress();
                        Tst.showToast(responseBody.desc);
                        if (responseBody.isSuccess()) {
                            RxBus.get().post(BusAction.REFRESH_MAINTAIN,"");
                            Map<String, Object> map = new HashMap<String, Object>();
                            map.put("id", responseBody.data);
                            startActivityAnimGeneral(MineRepairDetailActivity.class, map);
                            if (getIntent().getBooleanExtra("isDraft", false)) {
                                SPUtils.clearSerializable("draftRepair", MyRepair.class);
                            }
                            finish();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        hideProgress();
                        Tst.showToast(throwable.getMessage());
                    }
                });
    }

    private boolean checkContent(boolean isShowTst) {
        remark = mRemark.getText().toString().trim();
        picList = mAddPhotoView.getPhotos();
        if (TextUtils.isEmpty(remark) && picList.size() == 0) {
            tvRelease.setBackgroundResource(R.drawable.bg_rectangle_gray);
            return false;
        } else if (TextUtils.isEmpty(area)) {
            tvRelease.setBackgroundResource(R.drawable.bg_rectangle_gray);
            if (isShowTst) Tst.showToast("请选择地理位置");
            return false;

        } else {
            tvRelease.setBackgroundResource(R.drawable.bg_rectangle_btn);
            return true;
        }

    }

    private void updateRepairTypeView(List<RepairType> listType) {
        mAdapter = new RepairTypeAdapter(listType);
        mTagFlowLayout.setAdapter(mAdapter);
        for (int i = 0; i < listType.size(); i++) {
            if (listType.get(i).rname.equals(typeName)){
//                mAdapter.setSelected(i, listType.get(i));
                if(myRepair!=null) mAdapter.setSelectdList(Arrays.asList(listType.get(i)));
            }
        }
        mTagFlowLayout.setOnTagClickListener(new TagFlowLayout.OnTagClickListener() {
            @Override
            public boolean onTagClick(View view, int position, FlowLayout parent) {
                if( mAdapter.getItem(position).id.equals(repairType)){
                    repairType = "";
                    typeName = "";
                }else{
                    repairType = mAdapter.getItem(position).id;
                    typeName = mAdapter.getItem(position).rname;
                }
                checkContent(false);
                isModified=true;
                return true;
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mAddPhotoView.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        switch (requestCode){
            case REQEST_CODE_CITY:
                City city = (City) data.getSerializableExtra("city");
                if (city != null) {
                    tvLocation.setText(city.name);
                    area = city.id;
                    isModified=true;
                }
                break;

            case REQUST_CODE_PHOTO:
               if((List<Pic>) data.getSerializableExtra("photos")!=null){
                   isModified=true;
                   mAddPhotoView.setNewData((List<Pic>) data.getSerializableExtra("photos"));
               }
                checkContent(false);
                break;
        }
        checkContent(false);
    }

    @Override
    public void finshActivity() {
        remark = mRemark.getText() + "";
        if(TextUtils.isEmpty(remark) && !(mAddPhotoView.getPhotos() != null && mAddPhotoView.getPhotos().size() > 0) && TextUtils.isEmpty(repairType)){
            super.finshActivity();
        }else if (TextUtils.isEmpty(remark) && picList.size() == 0 && TextUtils.isEmpty(area)) {
            super.finshActivity();
        } else {
            DialogHelper.showBottomDialog(this, R.layout.dialog_save_draft, new DialogHelper.OnEventListener<Dialog>() {
                @Override
                public void eventListener(View parentView, final Dialog window) {
                    parentView.findViewById(R.id.not_save_draft).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SPUtils.clearSerializable("draftRepair", MyRepair.class);
                            window.dismiss();
                            ReleaseRepairActivity.super.finshActivity();
                        }
                    });
                    parentView.findViewById(R.id.save_draft).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MyRepair repair = new MyRepair();
                            repair.reremark = remark;
                            repair.pic = picList;
                            repair.rearea = tvLocation.getText() + "";
                            repair.areaId = area;
                            repair.retype = typeName;
                            repair.repairTypeId = repairType;
                            SPUtils.saveSerializable("draftRepair", repair);//保存
                            window.dismiss();
                            ReleaseRepairActivity.super.finshActivity();
                        }
                    });
                    parentView.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            window.dismiss();
                        }
                    });

                }
            });
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (subscription != null) subscription.unsubscribe();
    }

    @Subscribe(tags = {@Tag(BusAction.ACTION_DELETE_PIC)})
    public void deleteSelectPic(Pic pic) {
        mAddPhotoView.removeItem(pic);
        checkContent(false);
    }


    private void uploadService() {
        showProgress();
        imgIds.clear();
        subscription = Observable.from(mAddPhotoView.getPhotos())
                .map(new Func1<Pic, File>() {
                    @Override
                    public File call(Pic pic) {
                        File file = new File(pic.url);
                        return Luban.with(ReleaseRepairActivity.this).load(file).get();
                    }
                }).flatMap(new Func1<File, Observable<ResponseBody<Pic>>>() {
            @Override
            public Observable<ResponseBody<Pic>> call(File file) {
                RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
                return RxUploadApi.getInstance().getApiService().uploadImage(body);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }).onErrorResumeNext(new Func1<Throwable, Observable<? extends ResponseBody<Pic>>>() {
                    @Override
                    public Observable<? extends ResponseBody<Pic>> call(Throwable throwable) {
                        return Observable.empty();
                    }
                }).subscribe(new Action1<ResponseBody<Pic>>() {
                    @Override
                    public void call(ResponseBody<Pic> body) {
                        if (body.isSuccess() && body.data != null) {
                            imgIds.add(body.data);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        hideProgress();
                        Tst.showToast("发布失败");
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        if (mAddPhotoView.getPhotos().size()>0&&imgIds.size()==0) {
                            hideProgress();
                            Tst.showToast("发布失败");
                            return;
                        }
                        hideProgress();
                        ReleaseRepair();
                    }
                });
    }


    Subscription subscriptionLocation;
    private void getLocation() {
        if(subscriptionLocation != null) subscriptionLocation.unsubscribe();
        subscriptionLocation = LocationHelper.getInstance().startLocation().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<City>() {
                    @Override
                    public void call(City city) {
                        if(city != null){
                            tvLocation.setText(city.name);
                            area = city.id;
                        }else {
                            tvLocation.setText("位置获取失败");
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        tvLocation.setText("位置获取失败");
                    }
                });
    }


}
