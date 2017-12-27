package com.knms.view;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.knms.android.R;

import java.util.concurrent.TimeUnit;

/**
 * 类描述：
 * 创建人：Administrator
 * 创建时间：2017/8/30 16:27
 * 传参：
 * 返回:
 */
public class CountdownView extends LinearLayout{
    public static String StartTimeType = "1";//开始倒计时类型
    public static String EndTimeType = "2";//接受倒计时类型
    public long errorValue = 0;//毫秒

    boolean hasStartTime,hasEndTime;
    long startTimeSeconds,endTimeSeconds;

    private TextView tvTimeRemark;
    private TextView tvDay,tvHour, tvMinute, tvSecond;

    CountDownTimer startTimer,endTimer;
    public CountdownView(Context context) {
        super(context);
        initView(context);
    }

    public CountdownView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public CountdownView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public CountdownView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }
    private void initView(Context context){
        LayoutInflater.from(context).inflate(R.layout.view_down_timer, this);
        tvTimeRemark = (TextView) findViewById(R.id.tv_time_remark);//距开始还有、距结束还有
        tvDay = (TextView) findViewById(R.id.tv_day);
        tvHour = (TextView) findViewById(R.id.tv_hour);
        tvMinute = (TextView) findViewById(R.id.tv_minute);
        tvSecond = (TextView) findViewById(R.id.tv_second);
    }
    public void startCountDown() {
        //有四种情况
        if (startTimeSeconds == 0 && endTimeSeconds == 0) {//1:都为0
            if(!hasStartTime && !hasEndTime){//都不显示
                setVisibility(GONE);
                tvTimeRemark.setText("距开始还有");
                listener.finishTime(StartTimeType,hasStartTime);
            }else if(hasStartTime && !hasEndTime){//只显示开始抢购
                setVisibility(GONE);
                tvTimeRemark.setText("距开始还有");
                listener.finishTime(StartTimeType,hasStartTime);
            }else if(hasStartTime && hasEndTime){//都显示
                setVisibility(VISIBLE);
                tvTimeRemark.setText("距结束还剩");
                listener.finishTime(EndTimeType,hasEndTime);
            }else if(!hasStartTime && hasEndTime){//只显示结束抢购
                setVisibility(VISIBLE);
                tvTimeRemark.setText("距结束还剩");
                listener.finishTime(EndTimeType,hasEndTime);
            }
        }else if (startTimeSeconds > 0 && endTimeSeconds == 0) {//2：只有开始抢购时间
            countDownStartSnapUp(startTimeSeconds);
        }else if (startTimeSeconds > 0 && endTimeSeconds > 0) {//3:既有开始抢购时间又有结束抢购时间
            countDownSnapup(startTimeSeconds, endTimeSeconds);
        }else if(startTimeSeconds == 0 && endTimeSeconds > 0){//4:只有结束抢购时间
            countDwonStopSnapUp(endTimeSeconds);
        }
    }
    private void countDownSnapup(final long startSeconds, final long endSeconds){
        if(startTimer != null) startTimer.cancel();
        if(endTimer != null) endTimer.cancel();
        if(hasStartTime){
            setVisibility(View.VISIBLE);
        }else{
            setVisibility(View.GONE);
        }
        tvTimeRemark.setText("距开始还有");
        if(startSeconds > 0){
            startTimer = new CountDownTimer(startSeconds * 1000 + errorValue,1000){
                /**
                 * 固定间隔被调用
                 * @param millisUntilFinished 倒计时剩余时间
                 */
                @Override
                public void onTick(long millisUntilFinished) {
                    int seconds = (int) (Math.round((double) millisUntilFinished / 1000D));
//                    long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished);
                    updateUi(seconds);
                }
                /**
                 * 倒计时完成时被调用
                 */
                @Override
                public void onFinish() {
                    updateUi(0);
                    if(listener != null) listener.finishTime(StartTimeType,hasStartTime);
                    countDwonStopSnapUp(endSeconds - startSeconds);
                }
            }.start();
            if(listener != null) listener.startTime(StartTimeType);
            updateUi(startTimeSeconds);
        }else{
            countDwonStopSnapUp(endSeconds - startSeconds);
        }
    }
    private void countDownStartSnapUp(long startSeconds){
        if(startTimer != null) startTimer.cancel();
        if(hasStartTime){
            setVisibility(View.VISIBLE);
        }else{
            setVisibility(View.GONE);
        }
        tvTimeRemark.setText("距开始还有");
        startTimer = new CountDownTimer(startSeconds * 1000 + errorValue,1000){
            /**
             * 固定间隔被调用
             * @param millisUntilFinished 倒计时剩余时间
             */
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (Math.round((double) millisUntilFinished / 1000D));
//                    long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished);
                updateUi(seconds);
            }
            /**
             * 倒计时完成时被调用
             */
            @Override
            public void onFinish() {
                updateUi(0);
                if(listener != null) listener.finishTime(StartTimeType,hasStartTime);
            }
        }.start();
        if(listener != null) listener.startTime(StartTimeType);
        updateUi(startTimeSeconds);
    }
    private void countDwonStopSnapUp(long endSeconds){
        if(hasEndTime){
            setVisibility(View.VISIBLE);
        }else{
            setVisibility(View.GONE);
        }
        tvTimeRemark.setText("距结束还剩");
        if(endTimer != null) endTimer.cancel();
        endTimer = new CountDownTimer(endSeconds * 1000 + errorValue,1000){
            /**
             * 固定间隔被调用
             * @param millisUntilFinished 倒计时剩余时间
             */
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (Math.round((double) millisUntilFinished / 1000D));
//                    long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished);
                updateUi(seconds);
            }
            /**
             * 倒计时完成时被调用
             */
            @Override
            public void onFinish() {
                updateUi(0);
                if(listener != null) listener.finishTime(EndTimeType,hasEndTime);
            }
        }.start();
        if(listener != null) listener.startTime(EndTimeType);
        updateUi(endSeconds);
    }

    private void updateUi(long seconds){
        tvDay.setText(seconds2Day(seconds));
        tvHour.setText(seconds2Hour(seconds));
        tvMinute.setText(seconds2Minutes(seconds));
        tvSecond.setText(seconds2Seconds(seconds));
    }
    public void setStartTime(boolean hasStartTime,int startTimeSeconds){
        this.hasStartTime = hasStartTime;
        this.startTimeSeconds = startTimeSeconds;
    }
    public void setEndTime(boolean hasEndTime,int endTimeSeconds){
        this.hasEndTime = hasEndTime;
        this.endTimeSeconds = endTimeSeconds;
    }
    private String seconds2Day(long seconds){
        return String.format("%02d",TimeUnit.SECONDS.toDays(seconds));
    }
    private String seconds2Hour(long seconds){
        return String.format("%02d",TimeUnit.SECONDS.toHours(seconds) - (TimeUnit.SECONDS.toDays(seconds) *24));
    }
    private String seconds2Minutes(long seconds){
        return String.format("%02d",TimeUnit.SECONDS.toMinutes(seconds) - (TimeUnit.SECONDS.toHours(seconds)* 60));
    }
    private String seconds2Seconds(long seconds){
        return String.format("%02d",TimeUnit.SECONDS.toSeconds(seconds) - (TimeUnit.SECONDS.toMinutes(seconds) *60));
    }
    public void cancel(){
        if(startTimer != null) startTimer.cancel();
        if(endTimer != null) endTimer.cancel();
    }

    private CountDownListener listener;
    public void addCountDownListenter(CountDownListener listener){
        this.listener = listener;
    }
    public interface CountDownListener{
        public void startTime(String timeType);
        public void finishTime(String timeType,boolean hasShow);
    }
}
