package com.knms.view.tv;

/**
 * Created by Administrator on 2017/7/10.
 */

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.knms.android.R;
import com.knms.util.SystemInfo;
import com.knms.util.Tst;

import static com.knms.android.R.id.countView;
import static com.knms.util.SystemInfo.NETWORN_NONE;

/**
 * 购物车，计数
 */
public class CounterView extends LinearLayout implements View.OnClickListener, TextWatcher {
    /**
     * 最小的数量
     **/
    public static final int MIN_VALUE = 1;

    private boolean isApi = false;

    private int countValue = 1;//数量

    private TextView ivAdd, ivMinu;

    private TextView etCount;

    private IChangeCoutCallback callback;

    private ToastMsgCallback toastMsgCallback;

    private int maxValue = Integer.MAX_VALUE;


    public void setCallback(IChangeCoutCallback c) {
        this.callback = c;
    }

    private Context mContext;

    public CounterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initView(context, attrs);
    }

    /**
     * 功能描述：设置最大数量
     * 作者： hg_liuzl@qq.com
     * 时间：2016/12/3 18:33
     * 参数：
     */
    public void setMaxValue(int max) {
        if(max == 0){
            maxValue = Integer.MAX_VALUE;
        }else{
            if(max < MIN_VALUE){
                this.maxValue = MIN_VALUE;
            }else{
                this.maxValue = max;
            }
            if(countValue > maxValue){
                countValue = maxValue;
                setCountValue(countValue);
            }
        }
    }


    private void initView(Context context, AttributeSet attrs) {
        LayoutInflater.from(mContext).inflate(R.layout.model_count_view, this);

        ivMinu = (TextView) findViewById(R.id.iv_count_minus);
        ivMinu.setOnClickListener(this);

        ivAdd = (TextView) findViewById(R.id.iv_count_add);
        ivAdd.setOnClickListener(this);

        etCount = (TextView) findViewById(R.id.et_count);
        etCount.addTextChangedListener(this);
    }

    public int getCountValue(){
        return this.countValue;
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_count_add:
                addAction();
                break;
            case R.id.iv_count_minus:
                minuAction();
                break;


        }
    }

    /**
     * 添加操
     */
    private void addAction() {
        if(!isApi){
            if(countValue + 1 > maxValue){
                if(toastMsgCallback != null){
                    toastMsgCallback.maxToastMsg(countValue);
                }else{
                    Toast.makeText(mContext, String.format(toastMaxmsg), Toast.LENGTH_SHORT).show();
                }
            }else{
                countValue++;
                btnChangeWord(countValue);
            }
        }else{
            if(countValue + 1 > maxValue){
                if(toastMsgCallback != null){
                    toastMsgCallback.maxToastMsg(countValue);
                }else{
                    Toast.makeText(mContext, String.format(toastMaxmsg), Toast.LENGTH_SHORT).show();
                }
            }else {
                setEnabled(false);
                btnChangeWord(countValue + 1);
            }
        }
    }

    /**
     * 删除操作
     */
    private void minuAction() {
        if(!isApi){
            if(countValue - 1 < MIN_VALUE){
                Toast.makeText(mContext, String.format(toastMinmsg), Toast.LENGTH_SHORT).show();
            }else{
                countValue--;
                btnChangeWord(countValue);
            }
        }else{
            if(countValue - 1 < MIN_VALUE){
                Toast.makeText(mContext, String.format(toastMinmsg), Toast.LENGTH_SHORT).show();
            }else{
                setEnabled(false);
                btnChangeWord(countValue - 1);
            }

        }
    }

    /**
     * 功能描述：
     * 时间：2016/12/12 10:29
     * 参数：boolean 是否需要重新赋值
     */
    private void changeWord(boolean needUpdate) {
        if (needUpdate) {
            etCount.removeTextChangedListener(this);
            if (!TextUtils.isEmpty(etCount.getText().toString().trim())) {  //不为空的时候才需要赋值
                etCount.setText(String.valueOf(countValue));
            }
            etCount.addTextChangedListener(this);
        }

        //TODO--
        // if(callback != null) callback.change(countValue);
    }
    public void setEnabled(boolean enable){
        ivMinu.setEnabled(enable);
        ivAdd.setEnabled(enable);
        if(enable){
            ivMinu.setTextColor(Color.parseColor("#333333"));
            ivAdd.setTextColor(Color.parseColor("#333333"));
        }else{
            ivMinu.setTextColor(Color.parseColor("#CCCCCC"));
            ivAdd.setTextColor(Color.parseColor("#CCCCCC"));
        }

    }
    private void btnChangeWord(int changValue) {
        if(!isApi){
//            ivMinu.setEnabled(changValue > MIN_VALUE);
//            ivAdd.setEnabled(changValue < maxValue);
            etCount.setText(String.valueOf(changValue));
        }else{
            if(SystemInfo.getNetworkState() == NETWORN_NONE){//没有网络
                Tst.showToast("网络不给力，请检查网络设置");
                setEnabled(true);
                return;
            }
        }
        if(callback != null) callback.change(changValue);
//        etCount.setSelection(etCount.getText().toString().trim().length());
    }
    public void setCountValue(int value){
        this.countValue = value;
        etCount.setText(String.valueOf(countValue));
    }
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }
    private String toastMaxmsg = "已是商品最大购买数量";
    private String toastMinmsg = "已是商品最小购买数量";
    public void setToastMaxmsg(String msg){
        if(TextUtils.isEmpty(msg)) return;
        this.toastMaxmsg = msg;
    }
    public void setToastMinmsg(String msg){
        if(TextUtils.isEmpty(msg)) return;
        this.toastMinmsg = msg;
    }
    @Override
    public void afterTextChanged(Editable s) {
        boolean needUpdate = false;
        if (!TextUtils.isEmpty(s)) {
            countValue = Integer.valueOf(s.toString());
            if (countValue <= MIN_VALUE) {
                countValue = MIN_VALUE;
                needUpdate = true;
            } else if (countValue >= maxValue) {
                countValue = maxValue;
                needUpdate = true;
            } else {
                ivMinu.setEnabled(true);
                ivAdd.setEnabled(true);
            }
        } else {  //如果编辑框被清空了，直接填1
            countValue = 1;
            ivMinu.setEnabled(false);
            ivAdd.setEnabled(true);
            needUpdate = true;
        }
        changeWord(needUpdate);
    }

    public void setApi(boolean isApi) {
        this.isApi = isApi;
    }

    public void addToastMsg(ToastMsgCallback callback){
        this.toastMsgCallback = null;
        this.toastMsgCallback = callback;
    }
    /**
     * 监听选择技能数量的变化
     */
    public interface IChangeCoutCallback {
        void change(int count);
    }
    public interface ToastMsgCallback{
        void maxToastMsg(int currentCount);
    }
}
