package com.knms.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.knms.android.R;

/**
 * Created by Administrator on 2017/9/14.
 */

public class EllipsisEditText extends FrameLayout {
    private Context mContext;

    private EditText editText;
    private TextView textView;
    private ImageView imageView;

    private TextChangedListener linstener;
    private FocusChangeListener focusChangeListener;
    private boolean isFastLoad = true;

    public EllipsisEditText(Context context) {
        super(context);
        initView(context);
    }

    public EllipsisEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
        this.mContext = context;
    }

    public EllipsisEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
        this.mContext = context;
    }

    public EllipsisEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
        this.mContext = context;
    }

    public void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_ellipsis_edittext, this);
        editText = (EditText) findViewById(R.id.et_text);
        textView = (TextView) findViewById(R.id.tv_showtext);
        imageView = (ImageView) findViewById(R.id.iv_delete);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                imageView.setVisibility(s.length() == 0 || !isFastLoad ? GONE : VISIBLE);
                textView.setText(s.toString());
                isFastLoad = true;
                if (linstener != null) linstener.onTextChanged(s, start, before, count);

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        editText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(v, InputMethodManager.SHOW_FORCED);
                }
                imageView.setVisibility(hasFocus && !TextUtils.isEmpty(editText.getText()) ? VISIBLE : GONE);
                textView.setVisibility(hasFocus || TextUtils.isEmpty(editText.getText()) ? GONE : VISIBLE);
                editText.setVisibility(hasFocus || TextUtils.isEmpty(editText.getText()) ? VISIBLE : GONE);
                if (focusChangeListener != null) focusChangeListener.onFocusChanged(hasFocus);
            }
        });
        textView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getVisibility() != VISIBLE) {
                    editText.setVisibility(VISIBLE);
                    editText.setFocusable(true);
                    editText.setFocusableInTouchMode(true);
                    editText.requestFocus();
                    editText.setSelection((editText.getText() + "").length());
                }

            }
        });
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText("");
                textView.setText("");
            }
        });
    }

    public void setHint(String hint) {
        editText.setHint(hint);
    }

    public void setText(String text) {
        isFastLoad = false;
        if (TextUtils.isEmpty(text)) {
            textView.setVisibility(GONE);
            editText.setVisibility(VISIBLE);
        } else {
            editText.setVisibility(GONE);
            textView.setVisibility(VISIBLE);
        }
        editText.setText(text);
        textView.setText(text);
    }

    public String getText() {
        return editText.getText() + "";
    }

    public void setInputType(int type) {
        editText.setInputType(type);
    }

    public void setTextChangedListener(TextChangedListener linstener) {
        this.linstener = linstener;
    }

    public void setOnFocusChangeListener(FocusChangeListener listener) {
        this.focusChangeListener = listener;
    }

    public interface TextChangedListener {
        void onTextChanged(CharSequence s, int start, int before, int count);
    }

    public interface FocusChangeListener {
        void onFocusChanged(boolean hasFocus);
    }

}
