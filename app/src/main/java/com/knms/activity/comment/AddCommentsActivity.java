package com.knms.activity.comment;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.TextView;

import com.knms.activity.base.HeadBaseActivity;
import com.knms.activity.login.FasterLoginActivity;
import com.knms.adapter.EditCommentsAdapter;
import com.knms.android.R;
import com.knms.app.KnmsApp;
import com.knms.bean.ResponseBody;
import com.knms.bean.other.Pic;
import com.knms.bean.remark.ReportComment;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.net.RxRequestApi;
import com.knms.util.DialogHelper;
import com.knms.util.LocalDisplay;
import com.knms.util.SPUtils;
import com.knms.util.ScreenUtil;
import com.knms.util.Tst;
import com.knms.view.Star;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;

import static com.knms.util.ScreenUtil.getScreenHeight;


/**
 * Created by Administrator on 2017/7/18.
 * 软键盘相关 http://blog.csdn.net/l540675759/article/details/74528641#reply
 * 线上订单所有的商品统一评价
 */

public class AddCommentsActivity extends HeadBaseActivity {
    RecyclerView recyclerView;
    Star ratingBar;
    TextView tv_grade_state;

    EditCommentsAdapter adapter;
    List<ReportComment> data;

    public String orderId = "ordid";
    @Override
    protected void getParmas(Intent intent) {
        super.getParmas(intent);
        data = (List<ReportComment>) intent.getSerializableExtra("data");
        orderId = intent.getStringExtra("orderId");
    }

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("写评价");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initSoftKeyChange(getWindow().getDecorView().getRootView());
        /**
         * SOFT_INPUT_ADJUST_NOTHING: 不调整(输入法完全直接覆盖住,未开放此参数);
         * SOFT_INPUT_ADJUST_PAN 把整个Layout顶上去露出获得焦点的EditText,不压缩多余空间
         * SOFT_INPUT_ADJUST_RESIZE: 整个Layout重新编排,重新分配多余空间;stateHidden
         * SOFT_INPUT_ADJUST_UNSPECIFIED: 系统自己根据内容自行选择上两种方式的一种执行(默认配置).
         */
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
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
    protected int layoutResID() {
        return R.layout.activity_add_comments;
    }

    @Override
    protected void initView() {
        ratingBar = findView(R.id.ratingBar);
        recyclerView = findView(R.id.recyclerView);
        tv_grade_state = findView(R.id.tv_grade_state);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(false); //关键
        layoutManager.setReverseLayout(true);
        recyclerView.setFocusable(false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setNestedScrollingEnabled(false);


    }

    @Override
    protected void initData() {
        adapter = new EditCommentsAdapter(data);
        adapter.bindToRecyclerView(recyclerView);

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

        findView(R.id.btn_release).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<ReportComment> data = adapter.getReportComments();
                checkData(data);
            }
        });
    }

    private void checkData(final List<ReportComment> data) {
        if(!(data != null && data.size() > 0)){
            Tst.showToast("无数据提交");
            return;
        }
        int notEmptyCount = 0;
        for (ReportComment item : data) {
            if(!item.isEmpty()){
                notEmptyCount ++;
            }
        }
        if(notEmptyCount == 0){//未填写内容
            Tst.showToast("请填写评价内容");
            adapter.cursorReset();
        }else if(notEmptyCount < data.size()){//填写了部分内容
            DialogHelper.showPromptDialog(this, null, "您未填写完所有评价\n确定提交么?", "继续填写", null, "确定",
                    new DialogHelper.OnMenuClick() {
                        @Override
                        public void onLeftMenuClick() {}
                        @Override
                        public void onCenterMenuClick() {}
                        @Override
                        public void onRightMenuClick() {
                            commitData(data);
                        }
                    });
        }else{//填写完所有内容
            commitData(data);
        }
    }
    private boolean isSendOk = false;
    private void commitData(List<ReportComment> data) {
        if(!SPUtils.isLogin()){
            startActivityAnimGeneral(FasterLoginActivity.class,null);
            return;
        }
        isSendOk = false;
        showProgress();
        Observable.from(data)
                .filter(new Func1<ReportComment, Boolean>() {
                    @Override
                    public Boolean call(ReportComment reportComment) {
                        if(TextUtils.isEmpty(reportComment.content) && reportComment.observablePics == null){
                            return false;//拦截
                        }else{
                            if(TextUtils.isEmpty(reportComment.content)){
                                reportComment.content = "满意!";
                            }
                            return true;
                        }
                    }
                })
                .flatMap(new Func1<ReportComment, Observable<ReportComment>>() {
                    @Override
                    public Observable<ReportComment> call(final ReportComment reportComment) {
                        if(reportComment.observablePics == null) return Observable.just(reportComment);
                        return reportComment.observablePics.flatMap(new Func1<String, Observable<ReportComment>>() {
                            @Override
                            public Observable<ReportComment> call(String s) {
                                reportComment.imgs = s;
                                return Observable.just(reportComment);
                            }
                        });
                    }
                }).flatMap(new Func1<ReportComment, Observable<ResponseBody>>() {
            @Override
            public Observable<ResponseBody> call(ReportComment data) {
                return RxRequestApi.getInstance().getApiService().commitCommentByGoods(orderId,data.gid,data.productDesc,data.content,ratingBar.getMark(),data.imgs);
            }
        }).compose(this.<ResponseBody>applySchedulers())
                .subscribe(new Action1<ResponseBody>() {
                    @Override
                    public void call(ResponseBody body) {
                        if(body.isSuccess()){
                            isSendOk = true;
                        }else{
                            Tst.showToast(body.desc);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        hideProgress();
                        Tst.showToast(throwable.toString());
                        startToCommentListAct();
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        hideProgress();
                        startToCommentListAct();
                    }
                });
    }
    private void startToCommentListAct(){
        if(isSendOk){
            Map<String,Object> params = new HashMap<>();
            params.put("orderId",orderId);
            startActivityAnimGeneral(CommentListActivity.class,params);
            RxBus.get().post(BusAction.COMIT_COMMENTS,BusAction.COMIT_COMMENTS);
            KnmsApp.getInstance().finishActivity();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        if(data.getSerializableExtra("photos") != null){
            List<Pic> pics = (List<Pic>) data.getSerializableExtra("photos");
            int position = requestCode;
            adapter.updateAddPhotoView(position,pics);
        }
    }

    @Subscribe(tags = {@Tag(BusAction.ACTION_DELETE_PIC)})
    public void deleteSelectPic(Pic pic) {
        adapter.deleteSelectPic(pic);
    }
}
