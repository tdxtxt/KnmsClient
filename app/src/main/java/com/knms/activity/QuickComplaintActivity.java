package com.knms.activity;

import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.knms.activity.base.HeadBaseActivity;
import com.knms.activity.pic.CameraActivityF;
import com.knms.activity.pic.ImgBrowerPagerActivity;
import com.knms.adapter.ComplaintTypeAdapter;
import com.knms.android.R;
import com.knms.bean.ResponseBody;
import com.knms.bean.order.ComplaintsType;
import com.knms.bean.other.Pic;
import com.knms.bean.user.User;
import com.knms.core.compress.Luban;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.net.RxRequestApi;
import com.knms.net.uploadfile.RxUploadApi;
import com.knms.util.Tst;
import com.knms.view.AddPhotoView;
import com.knms.view.XEditText;
import com.knms.view.flowlayout.FlowLayout;
import com.knms.view.flowlayout.TagFlowLayout;

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
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 快捷投诉
 */
public class QuickComplaintActivity extends HeadBaseActivity implements OnClickListener {

    private AddPhotoView addPhotoView;
    private EditText mComplaintContent, mContactName;
    private XEditText mContactNumber;
    private TextView mWords, mComplaint;
    private String complaintContent;
    private String contactName = "";
    private String contactNumber = "";
    private String complaintsType = "";//投诉类型
    private String orderId;
    private List<Pic> picList = new ArrayList<>();
    private TagFlowLayout mLayoutComplaintType;
    private ComplaintTypeAdapter mAdapter;
    private StringBuffer imgIds = new StringBuffer();
    private Subscription subscription;
    private final int REQUST_CODE_PHOTO = 0x00016;
    private int type=1;


    @Override
    protected void getParmas(Intent intent) {
        orderId = intent.getStringExtra("orderId");
        contactNumber = intent.getStringExtra("tel");
        contactName = intent.getStringExtra("name");
        type=intent.getIntExtra("orderType",1);
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_quick_complaint;
    }

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("快捷投诉");
    }

    @Override
    protected void initView() {
        addPhotoView = findView(R.id.addPhotoView);
        mComplaintContent = (EditText) findViewById(R.id.complaint_content);
        mContactName = (EditText) findViewById(R.id.contacts_name);
        mContactNumber = (XEditText) findViewById(R.id.contact_number);
        mWords = (TextView) findViewById(R.id.words);
        mComplaint = (TextView) findViewById(R.id.complaint);
        mLayoutComplaintType = (TagFlowLayout) findViewById(R.id.tag_flow_layout_hot);
        getComplaintType();
    }

    @Override
    protected void initData() {
        mContactNumber.setText(contactNumber);
        mContactName.setText(contactName);

        mComplaintContent.addTextChangedListener(mTextWatcher);
        complaintContent = mComplaintContent.getText().toString();
        contactName = mContactName.getText().toString();
//        contactNumber = mContactNumber.getText().toString();
        mComplaint.setOnClickListener(this);
        addPhotoView.setAddListener(new AddPhotoView.AddListener() {
            @Override
            public void onclick() {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("photos", addPhotoView.getPhotos());
                startActivityForResultAnimGeneral(CameraActivityF.class, params, REQUST_CODE_PHOTO);
            }

            @Override
            public void otherClick(int position) {
                Intent intent = new Intent(QuickComplaintActivity.this, ImgBrowerPagerActivity.class);
                intent.putExtra("position", position);
                intent.putExtra("pics", (Serializable) addPhotoView.getPhotos());
                intent.putExtra("showDeleteBtn", true);
                startActivity(intent);
            }
        });
    }

    @Override
    public String setStatisticsTitle() {
        return "快捷投诉";
    }

    @Override
    public void onClick(View v) {
        complaintContent = mComplaintContent.getText() + "";
        contactName = mContactName.getText() + "";
        contactNumber = mContactNumber.getText() + "";
        switch (v.getId()) {
            case R.id.complaint:
                if (complaintContent.equals("")) {
                    Tst.showToast("请输入您要投诉的内容");
                    break;
                }
                if (complaintsType.equals("")) {
                    Tst.showToast("请选择投诉类型");
                    break;
                }
                if (contactName.equals("")) {
                    Tst.showToast("请填写联系人");
                    break;
                }
                if (contactNumber.equals("") && contactNumber.length() < 11) {
                    Tst.showToast("请填写联系电话");
                    break;
                }
                uploadService();
                break;
        }

    }

    private void getComplaintType() {
        //1表示商场投诉类型,2表示平台订单投诉类型
        RxRequestApi.getInstance().getApiService().getComplaintTypes(type)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<List<ComplaintsType>>>() {
                    @Override
                    public void call(ResponseBody<List<ComplaintsType>> listResponseBody) {
                        if (listResponseBody.isSuccess())
                            addComplaintTypeView(listResponseBody.data);
                        else Tst.showToast(listResponseBody.desc);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Tst.showToast(throwable.getMessage());
                    }
                });
    }

    private void addComplaintTypeView(final List<ComplaintsType> data) {
        mAdapter = new ComplaintTypeAdapter(data);
        mLayoutComplaintType.setAdapter(mAdapter);
        mLayoutComplaintType.setOnTagClickListener(new TagFlowLayout.OnTagClickListener() {
            @Override
            public boolean onTagClick(View view, int position, FlowLayout parent) {
                complaintsType = mAdapter.getItem(position).typeId;
                return true;
            }
        });
    }

    @Override
    protected void reqApi() {
        if (imgIds.toString().endsWith(",")) {
            imgIds.deleteCharAt(imgIds.length() - 1);

            Map<String,Object> map=new HashMap<>();
            map.put("orderId",orderId);
            map.put("content",complaintContent);
            map.put("complaintType",complaintsType);
            map.put("relationName",contactName);
            map.put("relationMobile",contactNumber);
            map.put("pictureIds",imgIds.toString());
            map.put("octype",type);
        }
        RxRequestApi.getInstance().getApiService().commonComplaint(orderId, complaintContent, complaintsType, contactName, contactNumber, imgIds.toString(),type)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<String>>() {
                               @Override
                               public void call(ResponseBody<String> stringResponseBody) {
                                   Tst.showToast(stringResponseBody.desc);
                                   if (stringResponseBody.isSuccess()) {
                                       Map<String, Object> param = new HashMap<String, Object>();
                                       param.put("complaintsId", stringResponseBody.data);
                                       startActivityAnimGeneral(ComplaintDetailsActivity.class, param);
                                       RxBus.get().post(BusAction.REFRESH_MY_ORDER, "");
                                       finshActivity();
                                   }
                               }
                           }, new Action1<Throwable>()

                           {
                               @Override
                               public void call(Throwable throwable) {
                                   Tst.showToast(throwable.getMessage());
                               }
                           }

                );
    }


    TextWatcher mTextWatcher = new TextWatcher() {
        private CharSequence temp;

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
            mWords.setText(temp.length() + "/200");
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        addPhotoView.onActivityResult(requestCode, resultCode, data);
//        picList = addPhotoView.getPhotos();
        if (resultCode != RESULT_OK) return;
        if((List<Pic>) data.getSerializableExtra("photos")!=null){
            addPhotoView.setNewData((List<Pic>) data.getSerializableExtra("photos"));
        }
    }
    private void uploadService() {
        showProgress();
        subscription = Observable.from(addPhotoView.getPhotos())
                .map(new Func1<Pic, File>() {
                    @Override
                    public File call(Pic pic) {
                        File file = new File(pic.url);
                        return Luban.with(QuickComplaintActivity.this).load(file).get();
                    }
                })
                .flatMap(new Func1<File, Observable<ResponseBody<Pic>>>() {
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
                            imgIds.append(body.data.id + ",");
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        hideProgress();
                        Tst.showToast("投诉失败");
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        hideProgress();
                        reqApi();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Subscribe(tags = {@Tag(BusAction.ACTION_DELETE_PIC)})
    public void deleteSelectPic(Pic pic) {
        addPhotoView.removeItem(pic);
    }

    @Subscribe(tags = {@Tag(BusAction.ACTION_LOGIN)})
    public void loginAccout(User user) {
        finshActivity();
    }

}
