package com.knms.view.flashview;

import android.util.Log;
import android.view.View;

import com.knms.util.ScreenUtil;

/**
 * Created by Administrator on 2016/6/23.
 */
public class ZoomCardPageTransformer implements CustomViewPager.PageTransformer {
    private CustomViewPager viewPager;
    private int windowWidth = 0;
    private int itemCount = 5;
    private float itemWidth = 0;
    private float translation;
    private int currentItem = 0;
    private boolean is3D = false;

    public ZoomCardPageTransformer(CustomViewPager viewPager, boolean is3D) {
        this.viewPager = viewPager;
        this.is3D = is3D;
        init();
    }

    private void init() {
        windowWidth = ScreenUtil.getScreenWidth();
        itemWidth = windowWidth / itemCount;
        translation = itemWidth;
    }


    /**
     * Viewpager 滑动时回调方法
     *
     * @param view     滑动页面
     * @param position 位移区间
     */
    public void transformPage(View view, float position) {
        //获取ViewPager当前显示的页面
        currentItem = viewPager.getCurrentItem();
        setPositionViewAnimation(view, position);
    }


    /**
     * 对View进行动画效果处理
     */
    private void setPositionViewAnimation(View view, float position) {
        //View所在页面
        int postion = (Integer) view.getTag();
        //是否在当前显示页的右边
        PagerLocation location = PagerAnimationUtil.getLocation(currentItem, postion);
        //缩放比例
        float scaleFactor = PagerAnimationUtil.getScaleCoefficient(currentItem, postion);
        //位移比例
        float transFactor = PagerAnimationUtil.getTranslationCoefficient(currentItem, postion);
        //位移距离
        float translationFactor = translation * PagerAnimationUtil.getTranslationSize(location, transFactor, position);
        //缩放大小
        float scale = PagerAnimationUtil.getScaleSize(scaleFactor, position);
        //对View进行动画处理
//        view.setAlpha(0.5f + (scale - scaleFactor) / (1 - scaleFactor) * (1 - 0.5f));
        view.setScaleX(scale);
        view.setScaleY(scale);
        setViewRotation(view, location);
//
//        if(translationFactor>0){
//            translationFactor-=200;
//        }else if(translationFactor<0){
//            translationFactor+=200;
//        }
        Log.i("ZoomCardPageTransformer", "translationFactor=" + translationFactor);
        view.setTranslationX(translationFactor);
    }

    /**
     * 设置view旋转角度（3D效果）
     */
    public void setViewRotation(View view, PagerLocation location) {
        if (!is3D) {
            return;
        }
        if (location == PagerLocation.LIFT) {
            view.setRotationY(28);
        } else if (location == PagerLocation.RIGHT) {
            view.setRotationY(-28);
        } else {
            view.setRotationY(0);
        }
    }

}
