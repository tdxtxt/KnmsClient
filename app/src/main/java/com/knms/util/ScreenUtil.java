package com.knms.util;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.knms.app.KnmsApp;

/**
 * 屏幕工具类--获取手机屏幕信息
 * 
 * @author zihao
 * 
 */
public class ScreenUtil {

	private static int mWidth;
	private static int mHeight;

	public static int getScreenWidth() {
		if (mWidth == 0) {
			calculateScreenDimensions();
		}
		return mWidth;
	}

	public static int getScreenHeight() {
		if (mHeight == 0) {
			calculateScreenDimensions();
		}
		return mHeight;
	}

	private static void calculateScreenDimensions() {
		WindowManager wm = (WindowManager) KnmsApp.getInstance().getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			final Point point = new Point();
			display.getSize(point);
			mWidth = point.x;
			mHeight = point.y;
		} else {
			mWidth = display.getWidth();
			mHeight = display.getHeight();
		}
	}

	/**
	 * 获取屏幕中控件顶部位置的高度--即控件顶部的Y点
	 * 
	 * @return
	 */
	public static int getScreenViewTopHeight(View view) {
		return view.getTop();
	}

	/**
	 * 获取屏幕中控件底部位置的高度--即控件底部的Y点
	 * 
	 * @return
	 */
	public static int getScreenViewBottomHeight(View view) {
		return view.getBottom();
	}

	/**
	 * 获取屏幕中控件左侧的位置--即控件左侧的X点
	 * 
	 * @return
	 */
	public static int getScreenViewLeftHeight(View view) {
		return view.getLeft();
	}

	/**
	 * 获取屏幕中控件右侧的位置--即控件右侧的X点
	 * 
	 * @return
	 */
	public static int getScreenViewRightHeight(View view) {
		return view.getRight();
	}

}