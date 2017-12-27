package com.knms.util;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.knms.android.R;
import com.knms.app.KnmsApp;

import static android.R.attr.id;

/**
 * Created by tdx on 2016/9/8.
 * 统一dialog辅助类
 */
public class DialogHelper {
    /**
     * 自定义view的dialog
     * @param activity
     * @param resId
     * @param eventListener
     */
    public static void showAlertDialog(Activity activity, int resId, OnEventListener eventListener) {
        Dialog dialog = new Dialog(activity);
        //去掉绿色的边缘线
        int divierId = activity.getResources().getIdentifier("android:id/titleDivider", null, null);
        View divider = dialog.findViewById(divierId);
        if(divider != null)divider.setBackgroundColor(Color.TRANSPARENT);

        dialog.show();
        View view = LayoutInflater.from(KnmsApp.getInstance()).inflate(resId, null);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        if (eventListener != null) eventListener.eventListener(view, dialog);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(view);
    }
    /**
     * （自下而上进入，自上而下退出）Dialog
     * @param activity
     * @param resId
     * @param eventListener
     */
    public static void showBottomDialog(Activity activity, int resId, OnEventListener eventListener){
        Dialog dialog = new Dialog(activity);
        //去掉绿色的边缘线
        int divierId = activity.getResources().getIdentifier("android:id/titleDivider", null, null);
        View divider = dialog.findViewById(divierId);
        if(divider != null)divider.setBackgroundColor(Color.TRANSPARENT);

        dialog.show();
        View dialogView = LayoutInflater.from(KnmsApp.getInstance()).inflate(resId, null);
        if (eventListener != null) eventListener.eventListener(dialogView, dialog);
        Window w = dialog.getWindow();
        w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        w.setBackgroundDrawable(new ColorDrawable(0));
        w.setWindowAnimations(R.style.push_in_out);//由底部进入或者退出
        WindowManager.LayoutParams lp = w.getAttributes();
        lp.width = ScreenUtil.getScreenWidth();
        lp.gravity = Gravity.BOTTOM;
        dialog.onWindowAttributesChanged(lp);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(dialogView);
    }
    /**
     * title和content必须存在一个(传入谁，显示谁)，菜单文字如果传入为空，则会隐藏对应的菜单
     * @param activity
     * @param title 标题
     * @param content 提示内容
     * @param leftMenu 左菜单显示文字
     * @param centerMenu 中间菜单显示文字
     * @param rightMenu 右菜单显示文字
     * @param onMenuClick 点击监听
     */
    public static void showPromptDialog(Activity activity, String title,
                                        String content, String leftMenu, String centerMenu,
                                        String rightMenu, final OnMenuClick onMenuClick){
        if(TextUtils.isEmpty(title) && TextUtils.isEmpty(content)){
            return;
        }
        final Dialog dialog = new Dialog(activity);
        //去掉绿色的边缘线
        int divierId = activity.getResources().getIdentifier("android:id/titleDivider", null, null);
        View divider = dialog.findViewById(divierId);
        if(divider != null)divider.setBackgroundColor(Color.TRANSPARENT);

        dialog.show();
        View view = LayoutInflater.from(KnmsApp.getInstance()).inflate(R.layout.common_prompt_dialog, null);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
        int screenWidth = (int) (metrics.widthPixels * 0.80);
        dialog.getWindow().setLayout(screenWidth, ViewGroup.LayoutParams.WRAP_CONTENT); //set below the setContentview

        TextView textTitle = (TextView) view.findViewById(R.id.tv_common_prompt_title);
        TextView textContent = (TextView) view.findViewById(R.id.tv_common_prompt_content);
        //设置title
        if(TextUtils.isEmpty(title)){
            textTitle.setVisibility(View.GONE);
        }else{
            textTitle.setVisibility(View.VISIBLE);
            textTitle.setText(title);
        }
        //设置content
        if(TextUtils.isEmpty(content)){
            textContent.setVisibility(View.GONE);
        }else{
            textContent.setVisibility(View.VISIBLE);
            textContent.setText(content);
        }

        TextView btnLeft = (TextView) view.findViewById(R.id.btn_common_prompt_left);
        TextView btnCenter = (TextView) view.findViewById(R.id.btn_common_prompt_center);
        TextView btnRight = (TextView) view .findViewById(R.id.btn_common_prompt_right);

        if(TextUtils.isEmpty(leftMenu)){
            btnLeft.setVisibility(View.GONE);
        }else{
            btnLeft.setVisibility(View.VISIBLE);
            btnLeft.setText(leftMenu);
        }

        if(TextUtils.isEmpty(centerMenu)){
            btnCenter.setVisibility(View.GONE);
        }else{
            btnCenter.setVisibility(View.VISIBLE);
            btnCenter.setText(centerMenu);
        }

        if(TextUtils.isEmpty(rightMenu)){
            btnRight.setVisibility(View.GONE);
        }else{
            btnRight.setVisibility(View.VISIBLE);
            btnRight.setText(rightMenu);
        }
        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onMenuClick != null)
                    onMenuClick.onLeftMenuClick();
                dialog.dismiss();
            }
        });
        btnCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onMenuClick != null)
                    onMenuClick.onCenterMenuClick();
                dialog.dismiss();
            }
        });
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onMenuClick != null)
                    onMenuClick.onRightMenuClick();
                dialog.dismiss();
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(view);
    }
    public interface OnEventListener<T> {
        public void eventListener(View parentView, T window);
    }
    public interface OnMenuClick {
        public void onLeftMenuClick() ;
        public void onCenterMenuClick() ;
        public void onRightMenuClick() ;
    }
}
