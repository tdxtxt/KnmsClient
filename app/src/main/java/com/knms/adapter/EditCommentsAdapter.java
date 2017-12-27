package com.knms.adapter;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.knms.activity.base.BaseActivity;
import com.knms.activity.comment.AddCommentActivity;
import com.knms.activity.comment.AddCommentsActivity;
import com.knms.activity.pic.CameraActivityF;
import com.knms.activity.pic.ImgBrowerPagerActivity;
import com.knms.android.R;
import com.knms.bean.other.Pic;
import com.knms.bean.product.Idle;
import com.knms.bean.remark.ReportComment;
import com.knms.util.ImageLoadHelper;
import com.knms.util.ScreenUtil;
import com.knms.util.Tst;
import com.knms.view.AddPhotoView;
import com.knms.view.editview.FJEditTextCount;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/7/18.
 */

public class EditCommentsAdapter extends BaseQuickAdapter<ReportComment, BaseViewHolder>{
    BaseActivity activity;
    int onclickPosition;
    public EditCommentsAdapter(List data) {
        super(R.layout.item_edit_comments, data);
    }
    @Override
    protected void convert(final BaseViewHolder helper, ReportComment item) {
        helper.setText(R.id.tv_product_desc,item.productDesc);
        ImageLoadHelper.getInstance().displayImage(activity,item.productImg,(ImageView) helper.getView(R.id.iv_icon));
        FJEditTextCount editTextCount = helper.getView(R.id.edit_count);
        editTextCount.setEtHint("请写下您对本次购物的评价,如:商品质量、商家服务等...")//设置提示文字
        .setLength(200)
        .show();
        final AddPhotoView addPhotoView = helper.getView(R.id.addPhotoView);
        if(activity == null) activity = (BaseActivity) addPhotoView.getContext();
        addPhotoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                activity.hideKeyboard();
                return false;
            }
        });
        addPhotoView.setAddListener(new AddPhotoView.AddListener() {
            @Override
            public void onclick() {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("photos", addPhotoView.getPhotos());
                activity.startActivityForResultAnimGeneral(CameraActivityF.class, params, helper.getAdapterPosition());
            }
            @Override
            public void otherClick(int position) {
                onclickPosition = helper.getAdapterPosition();
                Intent intent = new Intent(activity, ImgBrowerPagerActivity.class);
                intent.putExtra("position", position);
                intent.putExtra("pics", (Serializable) addPhotoView.getPhotos());
                intent.putExtra("showDeleteBtn", true);
                activity.startActivity(intent);
            }
        });
    }
    public List<ReportComment> getReportComments(){
        List<ReportComment> data = getData();
        for (int position = 0; position < data.size(); position++){
            AddPhotoView addPhotoView = (AddPhotoView) getViewByPosition(position,R.id.addPhotoView);
            data.get(position).imgs = "";
            data.get(position).observablePics = addPhotoView.getServicePhotos();
            FJEditTextCount editText = (FJEditTextCount) getViewByPosition(position,R.id.edit_count);
            data.get(position).content = editText.getText();
        }
        return data;
    }
    public void updateAddPhotoView(int position,List<Pic> data){
        AddPhotoView addPhotoView = (AddPhotoView) getViewByPosition(position,R.id.addPhotoView);
        if(addPhotoView != null) addPhotoView.setNewData(data);
    }
    public void deleteSelectPic(Pic pic){
        AddPhotoView addPhotoView = (AddPhotoView) getViewByPosition(onclickPosition,R.id.addPhotoView);
        if(addPhotoView != null) addPhotoView.removeItem(pic);
    }
    private void requestParentDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if(getRecyclerView() != null) getRecyclerView().requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    public void cursorReset() {
//        LinearLayoutManager manager = (LinearLayoutManager) getRecyclerView().getLayoutManager();
//        manager.scrollToPositionWithOffset(0, 0);//滚动到第一行

        FJEditTextCount fjEdit = (FJEditTextCount) getViewByPosition(getItemCount() - 1,R.id.edit_count);
        if(fjEdit == null) return;
        fjEdit.getEditText().setFocusable(true);
        fjEdit.getEditText().setFocusableInTouchMode(true);
        fjEdit.getEditText().requestFocus();//获取焦点 光标出现
    }
}
