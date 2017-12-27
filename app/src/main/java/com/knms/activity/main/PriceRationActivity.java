package com.knms.activity.main;

import com.knms.activity.MinePriceRationDetailsActivity;
import com.knms.activity.MoreLableActivity;
import com.knms.activity.base.HeadBaseActivity;
import com.knms.activity.comment.AddCommentActivity;
import com.knms.activity.login.FasterLoginActivity;
import com.knms.activity.pic.CameraActivityF;
import com.knms.activity.pic.ImgBrowerPagerActivity;
import com.knms.adapter.LabelAdapter;
import com.knms.adapter.LabelSelectAdapter;
import com.knms.bean.ResponseBody;
import com.knms.bean.myparity.MyParity;
import com.knms.bean.other.BBPrice;
import com.knms.bean.other.Label;
import com.knms.bean.other.Pic;
import com.knms.core.compress.Luban;
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
import com.knms.android.R;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
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
 * Created by tdx on 2016/9/14.
 * 比比价
 */
public class PriceRationActivity extends HeadBaseActivity {
    private EditText edt_desc;
    private AddPhotoView addPhotoView;
    private TagFlowLayout tag_flow_layout_hot, tag_flow_layout_select;
    private LabelAdapter hotTagAdapter;
    private LabelSelectAdapter selectTagAdapter;
    private int MAXLABEL = 10;//最多选择的标签数据
    private List<Label> selectLabels = new ArrayList<Label>();//已选标签
    private List<Label> hotLabels = new ArrayList<Label>();//从热门标签中选择的
    private List<Label> allLabels = new ArrayList<Label>();//从所有标签中选择的
    public final int REQUST_CODE_TAG = 0x00012;
    public final int REQUST_CODE_PHOTO = 0x00013;

    private TextView tv_num, tv_select_tag;
    private List<Pic> photos;//选择的照片
    private List<Pic> allPhotos;//可供选择的图片集合,主要是来源于非相册才能传该值
    private boolean isFromOther = false;//false来源于相册,true来源于非相册
    private Subscription subscription, subscriptionUpload, subscriptionSend;
    private boolean isDraft = false;//false从草稿中发布,true正常发布
    private View view;
    private boolean isModified = false;
    private String oldContent="";
    private int isShowRedMsg = 1;//0:不显示;1:显示 发布之后的小红点

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText(isFromOther?"求购":"拍照求购");
    }

    @Override
    protected void getParmas(Intent intent) {
        allPhotos = (List<Pic>) intent.getSerializableExtra("allPhotos");
        if (allPhotos == null) {
            isFromOther = false;//来源于相册
        } else {
            isFromOther = true;//来源于非相册
        }
        photos = (List<Pic>) intent.getSerializableExtra("photos");
        if (photos == null) photos = new ArrayList<>();
        else isModified=true;
        isDraft = intent.getBooleanExtra("isDraft", false);
        isShowRedMsg = intent.getIntExtra("isShowRedMsg",1);
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_price_relations;
    }

    @Override
    protected void initView() {
        edt_desc = findView(R.id.edt_desc);
        addPhotoView = findView(R.id.addPhotoView);
        tag_flow_layout_hot = findView(R.id.tag_flow_layout_hot);
        tag_flow_layout_select = findView(R.id.tag_flow_layout_select);
        tv_select_tag = findView(R.id.tv_select_tag);
        tv_num = findView(R.id.tv_num);
        view = findView(R.id.view_line_price);
    }

    @Override
    protected void initData() {
        addPhotoView.setMaxPhotoCount(9);
        findView(R.id.tvBtn_more_label).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> parmas = new HashMap<String, Object>();
                parmas.put("selects", selectLabels);
                startActivityForResultAnimGeneral(MoreLableActivity.class, parmas, REQUST_CODE_TAG);
            }
        });
        findView(R.id.release).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isOk = checkContent(false);
                if (isOk) {
                    //首先需要发送图片到服务器端
                    if(!SPUtils.isLogin()){
                        startActivityAnimGeneral(FasterLoginActivity.class,null);
                        return;
                    }
                    uploadPics();
                }
            }
        });
        edt_desc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().equals(oldContent))
                    isModified=true;
                checkContent(false);
                tv_num.setText(edt_desc.getText().toString().length() + "/" + 200);
            }
        });
        edt_desc.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(edt_desc.getWindowToken(), 0);
                }
            }
        });
        addPhotoView.setAddListener(new AddPhotoView.AddListener() {
            @Override
            public void onclick() {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("photos", addPhotoView.getPhotos());
                params.put("remotePics", allPhotos);
                startActivityForResultAnimGeneral(CameraActivityF.class, params, REQUST_CODE_PHOTO);
            }

            @Override
            public void otherClick(int position) {
                Intent intent = new Intent(PriceRationActivity.this, ImgBrowerPagerActivity.class);
                intent.putExtra("position", position);
                intent.putExtra("pics", (Serializable) addPhotoView.getPhotos());
                intent.putExtra("showDeleteBtn", true);
                startActivity(intent);
            }
        });
        if (photos.size() > 0) {
            addPhotoView.addPhotos(photos);
        }
        //当点击某个Tag回调
        tag_flow_layout_select.setOnTagClickListener(new TagFlowLayout.OnTagClickListener() {
            @Override
            public boolean onTagClick(View view, final int position, FlowLayout parent) {
                selectTagAdapter.remove(position);
                selectLabels = selectTagAdapter.getData();
                changeTag("select");
                isShowTextView();
                edt_desc.clearFocus();
                edt_desc.setSelected(false);
                return true;
            }
        });
        //当选择某个Tag后回调
        tag_flow_layout_hot.setOnSelectListener(new TagFlowLayout.OnSelectListener<Label>() {
            @Override
            public void onSelected(List<Label> selectPosSet, Label remove) {
                if (selectLabels != null) selectLabels.remove(remove);
                if (selectLabels != null && selectLabels.size() == MAXLABEL) {
                    Tst.showToast("最多选择" + MAXLABEL + "个标签");
                    tag_flow_layout_hot.getAdapter().setSelectdList(selectLabels);
                    return;
                }
                hotLabels = selectPosSet;
                changeTag("hot");
                isShowTextView();
                edt_desc.clearFocus();
                edt_desc.setSelected(false);
            }
        });
        reqApi();
        if (isDraft) loadDraft();
        checkContent(false);
        isShowTextView();
    }

    @Override
    public String setStatisticsTitle() {
        return "发布比比价";
    }

    /**
     * 从草稿中读取
     */
    private void loadDraft() {
        BBPrice bbPrice = SPUtils.getSerializable("draft", BBPrice.class);
        if (bbPrice != null) {
            oldContent=bbPrice.desc;
            edt_desc.setText(bbPrice.desc);
            addPhotoView.setNewData(bbPrice.pics);
            selectLabels = bbPrice.labels;
            selectTagAdapter = new LabelSelectAdapter(selectLabels);
            tag_flow_layout_select.setAdapter(selectTagAdapter);
            allPhotos = bbPrice.fromAllPics;
            if (allPhotos != null) isFromOther = true;
        }
    }

    boolean isUploadOk = true;//是否全部上传成功
    StringBuffer picIds, labelIds;

    /**
     * 上传图片&发布内容
     */
    private void uploadPics() {
        showProgress();
        picIds = new StringBuffer();
        labelIds = new StringBuffer();
        subscriptionUpload = Observable.from(addPhotoView.getPhotos())
                .filter(new Func1<Pic, Boolean>() {
                    @Override
                    public Boolean call(Pic pic) {
                        if (!TextUtils.isEmpty(pic.id)) {//表示图片已上传了
                            picIds.append(pic.id + ",");
                            return false;
                        }
                        return true;
                    }
                }).map(new Func1<Pic, File>() {
                    @Override
                    public File call(Pic pic) {
                        File file = new File(pic.url);
                        return Luban.with(PriceRationActivity.this).load(file).get();
                    }
                }).flatMap(new Func1<File, Observable<ResponseBody<Pic>>>() {
                    @Override
                    public Observable<ResponseBody<Pic>> call(File file) {
                        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
                        return RxUploadApi.getInstance().getApiService().uploadImage(body);
                    }
                })
                .doOnNext(new Action1<ResponseBody<Pic>>() {
                    @Override
                    public void call(ResponseBody<Pic> body) {

                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<Pic>>() {
                    @Override
                    public void call(ResponseBody<Pic> body) {
                        if (body.isSuccess()) {
                            picIds.append(body.data.id + ",");
                        } else {
                            Tst.showToast(body.desc);
                            isUploadOk = false;
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        isUploadOk = false;
                        hideProgress();
                        Tst.showToast("发布失败");
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        hideProgress();
                        releaseBBPrice();
                    }
                });
    }

    private void releaseBBPrice() {
        if (picIds.toString().endsWith(",")) {
            picIds.deleteCharAt(picIds.length() - 1);
        }
        String desc = edt_desc.getText().toString();
        if (selectLabels != null && selectLabels.size() > 0) {
            for (int i = 0; i < selectLabels.size(); i++) {
                if (i == 0) {
                    labelIds.append(selectLabels.get(i).id);
                } else {
                    labelIds.append("," + selectLabels.get(i).id);
                }
            }
        }
        showProgress();
        subscriptionSend = RxRequestApi.getInstance().getApiService().releaseBBPrice(desc, labelIds.toString(), picIds.toString(),isShowRedMsg)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<String>>() {
                    @Override
                    public void call(ResponseBody<String> body) {
                        hideProgress();
                        Tst.showToast(body.desc);
                        if (body.isSuccess()) {
                            RxBus.get().post(BusAction.REFRESH_BBPRICE,"");
                            if (isDraft)
                                SPUtils.clearSerializable("draft", BBPrice.class);//如果是从草稿发布的，那么清空缓存
                            Map<String, Object> params = new HashMap<String, Object>();
                            MyParity myParity = new MyParity();
                            myParity.coid = body.data;
                            myParity.coState = "1";
                            myParity.coremark = edt_desc.getText().toString();
                            myParity.labelList = selectLabels;
                            myParity.coType=2;
                            myParity.updatetime="";
                            myParity.cocreated = new Date(System.currentTimeMillis()).toString();
                            params.put("myParity", myParity);
                            startActivityAnimGeneral(MinePriceRationDetailsActivity.class, params);
                            PriceRationActivity.super.finshActivity();
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

    private boolean checkContent(boolean isToast) {
        String desc = edt_desc.getText().toString();
        List<Pic> pics = addPhotoView.getPhotos();
        ((TextView)findView(R.id.release)).setTextColor(getResources().getColor(R.color.color_gray_999999));
        findView(R.id.release).setBackgroundResource(R.drawable.bg_rectangle_gray);
        if (TextUtils.isEmpty(desc) && pics.size() == 0) {
            findView(R.id.release).setBackgroundResource(R.drawable.bg_rectangle_gray);
            if (isToast) Tst.showToast("必须填写描述或者图片");
            return false;
        }
        ((TextView)findView(R.id.release)).setTextColor(getResources().getColor(R.color.color_black_333333));
        findView(R.id.release).setBackgroundResource(R.drawable.bg_rectangle_btn);
        return true;
    }

    private void changeTag(String type) {
        isModified=true;
        if ("hot".equals(type)) {//如果在热门中标签状态发生改变
            for (Label item : hotLabels) {
                if (selectLabels.toString().contains(item.toString())) {//包含了
                    continue;
                } else {//不包含
                    selectLabels.add(item);
                }
            }
            selectTagAdapter = new LabelSelectAdapter(selectLabels);
            tag_flow_layout_select.setAdapter(selectTagAdapter);
        } else if ("select".equals(type)) {//如果在已选择标签中状态发生了改变
            hotTagAdapter.setSelectdList(selectLabels);
        } else if ("all".equals(type)) {//如果在所有选择标签中状态发送改变了
            selectLabels = allLabels;
            selectTagAdapter = new LabelSelectAdapter(selectLabels);
            tag_flow_layout_select.setAdapter(selectTagAdapter);
            hotTagAdapter.setSelectdList(selectLabels);
        }
        isShowTextView();
        checkContent(false);
    }

    @Override
    protected void reqApi() {
        showProgress();
        subscription = RxRequestApi.getInstance().getApiService().getHotLabels().observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<ResponseBody<List<Label>>>() {
                    @Override
                    public void call(ResponseBody<List<Label>> body) {
                        hideProgress();
                        if (body.isSuccess()) {
                            updateView(body.data);
                        } else {
                            Tst.showToast(body.desc);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable e) {
                        Tst.showToast(e.toString());
                        hideProgress();
                    }
                });
    }

    private void updateView(List<Label> data) {
        hotTagAdapter = new LabelAdapter(data);
        tag_flow_layout_hot.setAdapter(hotTagAdapter);
        if (isDraft) hotTagAdapter.setSelectdList(selectLabels);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RESULT_OK != resultCode) return;
        switch (requestCode) {
            case REQUST_CODE_TAG:
                allLabels = (List<Label>) data.getSerializableExtra("selects");
                changeTag("all");
                checkContent(false);
                break;
            case REQUST_CODE_PHOTO:
                photos = (List<Pic>) data.getSerializableExtra("photos");
                if (photos != null) {
                    addPhotoView.setNewData(photos);
                    isModified=true;
                }
                checkContent(false);
                break;
        }
    }

    @Override
    public void finshActivity() {
        Log.e("tag",isModified+"");
       if (TextUtils.isEmpty(edt_desc.getText().toString())
                && !(addPhotoView.getPhotos() != null && addPhotoView.getPhotos().size() > 0)
                && !(selectLabels != null && selectLabels.size() > 0)||!isModified) {
            super.finshActivity();
        } else {
            DialogHelper.showBottomDialog(this, R.layout.dialog_save_draft, new DialogHelper.OnEventListener<Dialog>() {
                @Override
                public void eventListener(View parentView, final Dialog window) {
                    parentView.findViewById(R.id.not_save_draft).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SPUtils.clearSerializable("draft", BBPrice.class);
                            window.dismiss();
                            PriceRationActivity.super.finshActivity();
                        }
                    });
                    parentView.findViewById(R.id.save_draft).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            saveContent();
                            window.dismiss();
                            PriceRationActivity.super.finshActivity();
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

    private void saveContent() {
        BBPrice bbPrice = new BBPrice();
        bbPrice.desc = edt_desc.getText().toString();
        bbPrice.pics = addPhotoView.getPhotos();
        bbPrice.labels = selectLabels;
        bbPrice.fromAllPics = allPhotos;
        SPUtils.saveSerializable("draft", bbPrice);//保存
    }

    @Subscribe(tags = {@Tag(BusAction.ACTION_DELETE_PIC)})
    public void deleteSelectPic(Pic pic) {
        if(allPhotos != null){
            for (Pic item : allPhotos) {
                if(item.url.equals(pic.url)){
                    item.isSelect = false;
                    item.order = "";
                    break;
                }
            }
        }
        addPhotoView.removeItem(pic);
        checkContent(false);
        isModified=true;
    }

    @Override
    protected void onDestroy() {
        if (subscription != null) subscription.unsubscribe();
        if (subscriptionSend != null) subscriptionSend.unsubscribe();
        if (subscriptionUpload != null) subscriptionUpload.unsubscribe();
        super.onDestroy();
    }

    private void isShowTextView() {
        if (selectLabels.size() > 0) {
            tv_select_tag.setVisibility(View.VISIBLE);
            view.setVisibility(View.VISIBLE);
        } else {
            tv_select_tag.setVisibility(View.GONE);
            view.setVisibility(View.GONE);
        }
    }
}
