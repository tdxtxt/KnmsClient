package com.knms.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import com.knms.android.R;


/**
 * Created by tdx on 2016/9/21.
 * 可以点击右边叉叉清除内容的editor
 */
public class XEditText extends EditText {
    final int DRAWABLE_LEFT = 0;
    final int DRAWABLE_TOP = 1;
    final int DRAWABLE_RIGHT = 2;
    final int DRAWABLE_BOTTOM = 3;
    private TextChangedListener linstener;

    public XEditText(Context context) {
        super(context);
        init();
    }

    public XEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public XEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public XEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }
    private void init() {
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(getText().toString().length() > 0&&hasFocus()){
                    Drawable drawable = getResources().getDrawable(R.drawable.sign_67);
                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight()); //设置边界
                    setCompoundDrawables(null, null,drawable, null);
                }else{
                    setCompoundDrawables(null, null, null, null);
                }
                if(linstener != null) linstener.onTextChanged(s,start,before,count);
            }
            @Override
            public void afterTextChanged(Editable s) {
                if(linstener != null) linstener.onTextChangedAfter(s);
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                Drawable drawableRight = getCompoundDrawables()[DRAWABLE_RIGHT];
                if (drawableRight != null && event.getRawX() >= (getRight() - drawableRight.getBounds().width())) {
                    setText("");
                }
                break;
        }

        return super.onTouchEvent(event);
    }

    public void setTextChangedListener(TextChangedListener linstener) {
        this.linstener = linstener;
    }

    public interface TextChangedListener {
        public void onTextChanged(CharSequence s, int start, int before, int count) ;
        public void onTextChangedAfter(Editable editable);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if(getText().toString().length() > 0&&focused){
            Drawable drawable = getResources().getDrawable(R.drawable.sign_67);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight()); //设置边界
            setCompoundDrawables(null, null,drawable, null);
        }else{
            setCompoundDrawables(null, null, null, null);
        }
    }
}
