package com.knms.view.tv;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.knms.android.R;

import static com.netease.nimlib.sdk.msg.constant.SystemMessageStatus.init;

/**
 * Created by tdx on 2016/9/29.
 * 消息提示的红点
 */

public class MsgTipText extends TextView {
    public MsgTipText(Context context) {
        super(context);
        init();
    }

    public MsgTipText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MsgTipText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MsgTipText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    private void init(){
        setBackgroundResource(R.drawable.circle_red);
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                String text = getText().toString();
                if(TextUtils.isEmpty(text)){
                    setVisibility(View.GONE);
                }else{
                    if("0".equals(text)){
                        setVisibility(View.GONE);
                    }else{
                        setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        String text = getText().toString();
        if(TextUtils.isEmpty(text)){
            setVisibility(View.GONE);
        }else{
            if("0".equals(text)){
                setVisibility(View.GONE);
            }else{
                setVisibility(View.VISIBLE);
            }
        }
    }

}
