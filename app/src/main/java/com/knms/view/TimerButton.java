package com.knms.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.CountDownTimer;
import android.text.format.Time;
import android.util.AttributeSet;
import android.widget.Button;

import com.knms.android.R;
import com.knms.util.CommonUtils;

import static com.netease.nimlib.sdk.msg.constant.SystemMessageStatus.init;

/**
 * Created by Administrator on 2016/12/2.
 */

public class TimerButton extends Button {
    private String afterText = "获取验证码";
    private int ms = 60000;
    private static long tepms = 0;
    public TimerButton(Context context) {
        super(context);
        init();
    }

    public TimerButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }


    public TimerButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
       /* TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.timerbutton);


        afterText = typedArray.getString(R.styleable.timerbutton_afterText);

        ms = typedArray.getInt(R.styleable.timerbutton_ms, 10000);

        typedArray.recycle();*/
    }

    private void init() {
//        this.afterText = afterText;
//        this.ms = ms;
        if(tepms != 0){
            TimerButton.this.setEnabled(false);
            new CountDownTimer(tepms, 1000) {
                @Override
                public void onTick(long finish) {
                    TimerButton.this.setText(finish / 1000 + "秒后重发");
                    TimerButton.this.setBackgroundResource(R.drawable.sign_57);
                    tepms = finish;
                }
                @Override
                public void onFinish() {
                    tepms = 0;
                    TimerButton.this.setBackgroundResource(R.drawable.sign_59);
                    TimerButton.this.setEnabled(true);
                    TimerButton.this.setText(afterText);
                }
            }.start();
        }

    }

    public void startTimer() {

        TimerButton.this.setEnabled(false);

        new CountDownTimer(ms, 1000) {

            @Override
            public void onTick(long finish) {
                TimerButton.this.setText(finish / 1000 + "秒后重发");
                TimerButton.this.setBackgroundResource(R.drawable.sign_57);
                tepms = finish;
            }

            @Override
            public void onFinish() {
                tepms = 0;
                TimerButton.this.setBackgroundResource(R.drawable.sign_59);
                TimerButton.this.setEnabled(true);
                TimerButton.this.setText(afterText);
            }
        }.start();

    }

}
