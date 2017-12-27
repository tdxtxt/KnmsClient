package com.knms.activity;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.knms.activity.base.BaseActivity;
import com.knms.activity.base.HeadBaseActivity;
import com.knms.activity.coupons.StoreCouponActivity;
import com.knms.core.qrcode.QRCodeView;
import com.knms.util.Tst;
import com.knms.android.R;

import java.util.HashMap;
import java.util.Map;

import static com.baidu.location.d.j.T;

/**
 * Created by tdx on 2016/9/23.
 * 二维码扫描
 */
public class ScanQRCodeActivity extends HeadBaseActivity implements QRCodeView.Delegate{
    private QRCodeView mQRCodeView;
    @Override
    protected void getParmas(Intent intent) {
        super.getParmas(intent);
    }

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("扫描二维码");
    }
    @Override
    protected int layoutResID() {
        return R.layout.activity_qrcode;
    }
    @Override
    protected void initView() {
        mQRCodeView = findView(R.id.zbarview);
        findView(R.id.information).setVisibility(View.GONE);
        findView(R.id.home_page).setVisibility(View.GONE);
    }

    @Override
    protected void initData() {
        mQRCodeView.setDelegate(this);
    }

    @Override
    public String setStatisticsTitle() {
        return "扫描二维码";
    }

    @Override
    protected void onStart() {
        super.onStart();
        mQRCodeView.startCamera();
        mQRCodeView.showScanRect();//显示扫描框
    }

    @Override
    protected void onResume() {
        super.onResume();
        mQRCodeView.startSpot();
    }

    @Override
    protected void onStop() {
        mQRCodeView.stopCamera();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mQRCodeView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onScanQRCodeSuccess(String result) {
        mQRCodeView.startSpot();
        if(TextUtils.isEmpty(result)) return;
        if(result.startsWith("http://")||result.startsWith("https://")){
            Uri uri = Uri.parse(result);
            String countNumber = null;
            String parmas = uri.getEncodedQuery();
            if(!TextUtils.isEmpty(parmas) && parmas.contains("countNumber=")){
                String[] values = parmas.split("&");
                for (String value : values) {
                    if(value.contains("countNumber=")){
                        countNumber = value.replace("countNumber=","");
                        break;
                    }
                }
            }
//            String countNumber = uri.getQueryParameter("countNumber");直接用该方法会导致无法获取带"+"符号的参数值
            Map<String,Object> map = new HashMap<>();
            if(TextUtils.isEmpty(countNumber)){
                map.put("url",result);
                startActivityAnimGeneral(CommWebViewActivity.class,map);
            }else{
                map.put("id",countNumber);
                startActivityAnimGeneral(StoreCouponActivity.class,map);
            }
        }else{
            Tst.showToast(result);
        }
    }

    @Override
    public void onScanQRCodeOpenCameraError() {
        Tst.showToast("打开相机出错");
    }
}
