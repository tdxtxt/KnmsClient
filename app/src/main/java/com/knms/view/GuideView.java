package com.knms.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import com.knms.android.R;
import com.knms.util.LocalDisplay;

/**
 * Created by zhouweixian on 2016/1/23.
 * http://blog.csdn.net/qiushi_1990/article/details/51558319
 */
public class GuideView extends RelativeLayout implements ViewTreeObserver.OnGlobalLayoutListener {
    private final String TAG = getClass().getSimpleName();
    private Context mContent;
    private boolean first = true;
    /**
     * targetView前缀。SHOW_GUIDE_PREFIX + targetView.getId()作为保存在SP文件的key。
     */
    private static final String SHOW_GUIDE_PREFIX = "show_guide_on_view_";
    /**
     * GuideView 偏移量
     */
    private int offsetX, offsetY;
    /**
     * targetView 的外切圆半径
     */
    private int radius;
    /**
     * 需要显示提示信息的View
     */
    private View targetView;
    /**
     * 自定义View
     */
    private View customGuideView;
    /**
     * 透明圆形画笔
     */
    private Paint mCirclePaint,imaginaryPaint;
    /**
     * 背景色画笔
     */
    private Paint mBackgroundPaint;
    /**
     * targetView是否已测量
     */
    private boolean isMeasured;
    /**
     * targetView圆心
     */
    private int[] center;
    /**
     * 绘图层叠模式
     */
    private PorterDuffXfermode porterDuffXfermode;
    /**
     * 绘制前景bitmap
     */
    private Bitmap bitmap;
    /**
     * 背景色和透明度，格式 #aarrggbb
     */
    private int backgroundColor;
    /**
     * Canvas,绘制bitmap
     */
    private Canvas temp;
    /**
     * 相对于targetView的位置.在target的那个方向
     */
    private Direction direction;

    /**
     * 形状
     */
    private MyShape myShape;
    /**
     * targetView左上角坐标
     */
    private int[] location;
    private boolean onClickExit;
    private OnClickCallback onclickListener;
    private RelativeLayout guideViewLayout;

    public void restoreState() {
        Log.v(TAG, "restoreState");
        offsetX = offsetY = 0;
        radius = 0;
        mCirclePaint = null;
        mBackgroundPaint = null;
        isMeasured = false;
        center = null;
        porterDuffXfermode = null;
        bitmap = null;
        needDraw = true;
        //        backgroundColor = Color.parseColor("#00000000");
        temp = null;
        //        direction = null;

    }

    public int[] getLocation() {
        return location;
    }

    public void setLocation(int[] location) {
        this.location = location;
    }

    public GuideView(Context context) {
        super(context);
        this.mContent = context;
        init();
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void setShape(MyShape shape) {
        this.myShape = shape;
    }

    public void setCustomGuideView(View customGuideView) {
        this.customGuideView = customGuideView;
        if (!first) {
            restoreState();
        }
    }

    public void setBgColor(int background_color) {
        this.backgroundColor = background_color;
    }

    public View getTargetView() {
        return targetView;
    }

    public void setTargetView(View targetView) {
        this.targetView = targetView;
        //        restoreState();
        if (!first) {
            //            guideViewLayout.removeAllViews();
        }
    }

    private void init() {
    }

    public void showOnce() {
        if (targetView != null) {
            mContent.getSharedPreferences(TAG, Context.MODE_PRIVATE).edit().putBoolean(generateUniqId(targetView), true).commit();
        }
    }

    private boolean hasShown() {
        if (targetView == null)
            return true;
        return mContent.getSharedPreferences(TAG, Context.MODE_PRIVATE).getBoolean(generateUniqId(targetView), false);
    }

    private String generateUniqId(View v) {
        return SHOW_GUIDE_PREFIX + mContent.getClass().getSimpleName();
    }

    public int[] getCenter() {
        return center;
    }

    public void setCenter(int[] center) {
        this.center = center;
    }

    public void hide() {
        Log.v(TAG, "hide");
        if (customGuideView != null) {
            targetView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            this.removeAllViews();
            ((FrameLayout) ((Activity) mContent).getWindow().getDecorView()).removeView(this);
            restoreState();
        }
    }

    public GuideView show() {
        Log.v(TAG, "show");
        if(this.getTag() != null && this.getTag() instanceof Boolean){
           if((boolean)this.getTag()){
               if (hasShown())
                   return this;
               showOnce();
           }
        }
        if (targetView != null) {
            targetView.getViewTreeObserver().addOnGlobalLayoutListener(this);
        }

        this.setBackgroundResource(R.color.no_color);

        ((FrameLayout) ((Activity) mContent).getWindow().getDecorView()).addView(this);
        first = false;
        return this;
    }
    public void onDestroy(){
        if (customGuideView != null) {
            if(targetView != null) targetView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            this.removeAllViews();
            ((FrameLayout) ((Activity) mContent).getWindow().getDecorView()).removeView(this);
            restoreState();
        }
        targetView = null;
        customGuideView = null;
        mContent = null;
        System.gc();
    }
    /**
     * 添加提示文字，位置在targetView的下边
     * 在屏幕窗口，添加蒙层，蒙层绘制总背景和透明圆形，圆形下边绘制说明文字
     */
    private void createGuideView() {
        Log.v(TAG, "createGuideView");

        // 添加到蒙层
        //        if (guideViewLayout == null) {
        //            guideViewLayout = new RelativeLayout(mContent);
        //        }

        // Tips布局参数
        LayoutParams guideViewParams;
        guideViewParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        guideViewParams.setMargins(0, center[1] + radius + 10, 0, 0);

        if (customGuideView != null) {

            //            LayoutParams guideViewParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (direction != null) {
                int width = this.getWidth();
                int height = this.getHeight();

                int left = center[0] - radius;
                int right = center[0] + radius;
                int top = center[1] - radius;
                int bottom = center[1] + radius;
                switch (direction) {
                    case TOP:
                        this.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
                        guideViewParams.setMargins(offsetX, offsetY - height + top, -offsetX, height - top - offsetY);
                        break;
                    case LEFT:
                        this.setGravity(Gravity.RIGHT);
                        guideViewParams.setMargins(offsetX - width + left, top + offsetY, width - left - offsetX, -top - offsetY);
                        break;
                    case BOTTOM:
                        this.setGravity(Gravity.CENTER_HORIZONTAL);
                        guideViewParams.setMargins(offsetX, bottom + offsetY, -offsetX, -bottom - offsetY);
                        break;
                    case RIGHT:
                        guideViewParams.setMargins(right + offsetX, top + offsetY, -right - offsetX, -top - offsetY);
                        break;
                    case LEFT_TOP:
                        this.setGravity(Gravity.RIGHT | Gravity.BOTTOM);
                        guideViewParams.setMargins(offsetX - width + left, offsetY - height + top, width - left - offsetX, height - top - offsetY);
                        break;
                    case LEFT_BOTTOM:
                        this.setGravity(Gravity.RIGHT);
                        guideViewParams.setMargins(offsetX - width + left, bottom + offsetY, width - left - offsetX, -bottom - offsetY);
                        break;
                    case RIGHT_TOP:
                        this.setGravity(Gravity.BOTTOM);
                        guideViewParams.setMargins(right + offsetX, offsetY - height + top, -right - offsetX, height - top - offsetY);
                        break;
                    case RIGHT_BOTTOM:
                        guideViewParams.setMargins(right + offsetX, bottom + offsetY, -right - offsetX, -top - offsetY);
                        break;
                }
            } else {
                guideViewParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                guideViewParams.setMargins(offsetX, offsetY, -offsetX, -offsetY);
            }

            //            guideViewLayout.addView(customGuideView);

            this.addView(customGuideView, guideViewParams);
        }
    }

    /**
     * 获得targetView 的宽高，如果未测量，返回｛-1， -1｝
     *
     * @return
     */
    private int[] getTargetViewSize() {
        int[] location = {-1, -1};
        if (isMeasured) {
            location[0] = targetView.getWidth();
            location[1] = targetView.getHeight();
        }
        return location;
    }

    /**
     * 获得targetView 的半径
     *
     * @return
     */
    private int getTargetViewRadius() {
        if (isMeasured) {
            int[] size = getTargetViewSize();
            int x = size[0];
            int y = size[1];

            return (int) (Math.sqrt(x * x + y * y) / 2);
        }
        return -1;
    }

    boolean needDraw = true;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.v(TAG, "onDraw");

        if (!isMeasured)
            return;

        if (targetView == null)
            return;

        //        if (!needDraw) return;

        drawBackground(canvas);

    }

    private void drawBackground(Canvas canvas) {
        Log.v(TAG, "drawBackground");
        needDraw = false;
        // 先绘制bitmap，再将bitmap绘制到屏幕
        bitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
        temp = new Canvas(bitmap);

        // 背景画笔
        Paint bgPaint = new Paint();
        if (backgroundColor != 0)
            bgPaint.setColor(backgroundColor);
        else{
//            bgPaint.setColor(getResources().getColor(R.color.black_cc222222));
            bgPaint.setColor(getResources().getColor(R.color.no_color));
        }

        // 绘制屏幕背景
        temp.drawRect(0, 0, temp.getWidth(), temp.getHeight(), bgPaint);

        // targetView 的透明圆形画笔
        if (mCirclePaint == null)
            mCirclePaint = new Paint();
        //这是一个非常强大的转换模式，使用它，可以使用图像合成的16条Porter-Duff规则的任意一条来控制Paint如何与已有的Canvas图像进行交互
        porterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT);// 或者CLEAR
        mCirclePaint.setXfermode(porterDuffXfermode);//该方法用来设置两张图片相交时的模式，系统已经实现的Xfermode子类有以下三个：
        mCirclePaint.setAntiAlias(true);
        imaginaryPaint = new Paint();
        imaginaryPaint.setAntiAlias(true);
        imaginaryPaint.setStyle(Paint.Style.STROKE);
        imaginaryPaint.setColor(Color.WHITE);
        imaginaryPaint.setStrokeWidth(5);
        PathEffect effects = new DashPathEffect(new float[]{5,5},1);
        imaginaryPaint.setPathEffect(effects);

        if (myShape != null) {
            RectF oval = new RectF();
            switch (myShape) {
                case CIRCULAR://圆形
                    temp.drawCircle(center[0], center[1], radius+5, imaginaryPaint);
                    temp.drawCircle(center[0], center[1], radius, mCirclePaint);//绘制圆形
                    break;
                case ELLIPSE://椭圆
                    oval.left = center[0] -  targetView.getWidth()/2+ LocalDisplay.dip2px(19); //左边
                    oval.top = center[1] -  targetView.getHeight()/2+LocalDisplay.dip2px(4);   //上边
                    oval.right = center[0] +  targetView.getWidth()/2-LocalDisplay.dip2px(19); //右边
                    oval.bottom = center[1] + targetView.getHeight()/2-LocalDisplay.dip2px(4); //下边
                    temp.drawOval(oval, imaginaryPaint);                   //绘制椭圆

                    //RectF对象
                    oval.left = center[0] -  targetView.getWidth()/2+ LocalDisplay.dip2px(20); //左边
                    oval.top = center[1] -  targetView.getHeight()/2+LocalDisplay.dip2px(5);   //上边
                    oval.right = center[0] +  targetView.getWidth()/2-LocalDisplay.dip2px(20); //右边
                    oval.bottom = center[1] + targetView.getHeight()/2-LocalDisplay.dip2px(5); //下边
                    temp.drawOval(oval, mCirclePaint);                   //绘制椭圆
                    break;
                case RECTANGULAR://圆角矩形
                    //RectF对象
                    oval.left = center[0] - 150;                              //左边
                    oval.top = center[1] - 50;                                   //上边
                    oval.right = center[0] + 150;                             //右边
                    oval.bottom = center[1] + 50;                                //下边
                    temp.drawRoundRect(oval, radius, radius, mCirclePaint);                   //绘制圆角矩形
                    break;
            }
        } else {
            temp.drawCircle(center[0], center[1], radius, mCirclePaint);//绘制圆形
        }

        // 绘制到屏幕
        canvas.drawBitmap(bitmap, 0, 0, bgPaint);
        bitmap.recycle();
    }

    public void setOnClickExit(boolean onClickExit) {
        this.onClickExit = onClickExit;
    }

    public void setOnclickListener(OnClickCallback onclickListener) {
        this.onclickListener = onclickListener;
    }

    private void setClickInfo() {
        final boolean exit = onClickExit;
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onclickListener != null) {
                    onclickListener.onClickedGuideView(v);
                }
                if (exit) {
                    hide();
                }
            }
        });
    }

    @Override
    public void onGlobalLayout() {
        if (isMeasured)
            return;
        if (targetView.getHeight() > 0 && targetView.getWidth() > 0) {
            isMeasured = true;
        }

        // 获取targetView的中心坐标
        if (center == null) {
            // 获取右上角坐标
            location = new int[2];
            targetView.getLocationInWindow(location);
            center = new int[2];
            // 获取中心坐标
            center[0] = location[0] + targetView.getWidth() / 2;
            center[1] = location[1] + targetView.getHeight() / 2;
        }
        // 获取targetView外切圆半径
        if (radius == 0) {
            radius = getTargetViewRadius();
        }
        // 添加GuideView
        createGuideView();
    }

    /**
     * 定义GuideView相对于targetView的方位，共八种。不设置则默认在targetView下方
     */
    public enum Direction {
        LEFT, TOP, RIGHT, BOTTOM,
        LEFT_TOP, LEFT_BOTTOM,
        RIGHT_TOP, RIGHT_BOTTOM
    }

    /**
     * 定义目标控件的形状，共3种。圆形，椭圆，带圆角的矩形（可以设置圆角大小），不设置则默认是圆形
     */
    public enum MyShape {
        CIRCULAR, ELLIPSE, RECTANGULAR
    }

    /**
     * GuideView点击Callback
     */
    public interface OnClickCallback {
        void onClickedGuideView(View view);
    }

    public static class Builder {
        boolean isShowOnce = false;//是否显示一次
        View target;//目标控件
        int color;
        Direction dir;
        MyShape shape;
        Point offsetPoint,centerPoint;
        int radius;
        boolean onclickExit;
        OnClickCallback onClickCallback;
        View customGuideView;//引导视图界面

        public Builder setTargetView(View target) {
            this.target = target;
            return this;
        }

        public Builder setBgColor(int color) {
            this.color = color;
            return this;
        }

        public Builder setDirction(Direction dir) {
            this.dir = dir;
            return this;
        }

        public Builder setShape(MyShape shape) {
            this.shape = shape;
            return this;
        }

        public Builder setOffset(int x, int y) {
            this.offsetPoint = new Point(x,y);
            return this;
        }

        public Builder setRadius(int radius) {
            this.radius = radius;
            return this;
        }

        public Builder setCustomGuideView(View view) {
            this.customGuideView = view;
            return this;
        }

        public Builder setCenter(int x, int y) {
            this.centerPoint = new Point(x,y);
            return this;
        }

        public Builder showOnce() {
            isShowOnce = true;
            return this;
        }
        public Builder setOnclickExit(boolean onclickExit) {
            this.onclickExit = onclickExit;
            return this;
        }

        public Builder setOnclickListener(OnClickCallback callback) {
            this.onClickCallback = callback;
            return this;
        }
        public GuideView build(Context context) {
            GuideView guiderView = new GuideView(context);
            guiderView.setTag(isShowOnce);
            if(target != null) guiderView.setTargetView(target);
            if(customGuideView != null) guiderView.setCustomGuideView(customGuideView);
            if(shape != null) guiderView.setShape(shape);
            if(dir != null) guiderView.setDirection(dir);
            if(color > 0) guiderView.setBgColor(color);

            if(centerPoint != null) guiderView.setCenter(new int[]{centerPoint.x,centerPoint.y});
            if(offsetPoint != null){
                guiderView.setOffsetX(offsetPoint.x);
                guiderView.setOffsetY(offsetPoint.y);
            }
            if(radius > 0) guiderView.setRadius(radius);
            guiderView.setOnClickExit(onclickExit);
            guiderView.setOnclickListener(onClickCallback);
            guiderView.setClickInfo();
            return guiderView;
        }
    }
}
