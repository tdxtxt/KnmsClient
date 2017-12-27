package com.knms.activity.mall.address;

import android.content.Intent;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.knms.activity.base.HeadBaseActivity;
import com.knms.activity.dialog.ChooseAreaActivityDialog;
import com.knms.android.R;
import com.knms.bean.ResponseBody;
import com.knms.bean.address.CreateAddress;
import com.knms.bean.address.ShippingAddres;
import com.knms.bean.other.City;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.net.RxRequestApi;
import com.knms.util.CommonUtils;
import com.knms.util.ConstantObj;
import com.knms.util.Tst;
import com.knms.view.EllipsisEditText;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.functions.Action1;

/**
 * Created by Administrator on 2017/7/13.
 * 添加收货信息&编辑收货信息
 * addressinfo:收货信息（编辑的时候需要,添加不需要），默认为新增收货信息
 * isfristadd：是否是第一次添加收货信息，判断是否显示设置默认地址一栏
 */

public class AddAddresActivity extends HeadBaseActivity implements View.OnClickListener {
    private EllipsisEditText mAddressee, mDetailsAddress, mContactNumber;
    private TextView mChooseArea, mSaveAddress, mShowArea;
    private ToggleButton mToggleButton;
    private ShippingAddres.orderMailingAddressBos addressDetails;
    private String strTitle;
    private boolean isfristadd;
    private RelativeLayout mSetDefaultAddresLayout, mChooseAreaLayout;


    @Override
    protected void getParmas(Intent intent) {
        addressDetails = (ShippingAddres.orderMailingAddressBos) intent.getSerializableExtra("addressinfo");
        strTitle = addressDetails == null ? "新增收货信息" : "编辑收货信息";
        isfristadd = intent.getBooleanExtra("isfristadd", false);
    }

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText(strTitle);
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_add_address_layout;
    }

    @Override
    protected void initView() {
        mShowArea = findView(R.id.tv_show_area);
        mAddressee = findView(R.id.et_addressee);
        mContactNumber = findView(R.id.et_contact_number);
        mDetailsAddress = findView(R.id.et_details_address);
        mChooseArea = findView(R.id.tv_choose_area);
        mSaveAddress = findView(R.id.tv_save_address);
        mToggleButton = findView(R.id.is_default_address);
        mSetDefaultAddresLayout = findView(R.id.rl_default_layout);
        mChooseAreaLayout = findView(R.id.rl_choose_are);
        mChooseAreaLayout.setOnClickListener(this);
        mSaveAddress.setOnClickListener(this);
        mContactNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
        mDetailsAddress.setHint("请填写详细信息");
        mToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                clearFoucs();
            }
        });
    }


    @Override
    protected void initData() {
        if (addressDetails != null) {
            mAddressee.setText(addressDetails.mailingname);
            mContactNumber.setText(addressDetails.mailingphone);
            mShowArea.setText(addressDetails.mailingarea);
            mChooseArea.setText("");
            mDetailsAddress.setText(addressDetails.mailingaddress);
            mToggleButton.setChecked(addressDetails.addresstype.equals("1"));
            if (TextUtils.equals(addressDetails.addresstype, "1"))
                mSetDefaultAddresLayout.setVisibility(View.GONE);
        }
        if (isfristadd) mSetDefaultAddresLayout.setVisibility(View.GONE);
    }

    String strAddressee, strPhoneNumber, strDetailsAddres, strArea;

    private boolean verifyData() {
        strAddressee = mAddressee.getText().toString();
        strPhoneNumber = mContactNumber.getText().toString();
        strDetailsAddres = mDetailsAddress.getText().toString();
        strArea = mShowArea.getText().toString();

        if (TextUtils.isEmpty(strAddressee)) {
            Tst.showToast("收货人信息不能为空");
            return false;
        } else if (strAddressee.length() > 16) {
            Tst.showToast("收货人最多为16个字");
            return false;
        } else if (TextUtils.isEmpty(strPhoneNumber)) {
            Tst.showToast("手机号不能为空");
            return false;
        } else if (strPhoneNumber.length() > 11 || strPhoneNumber.length() < 11) {
            Tst.showToast("手机号为11位数字");
            return false;
        } else if (!CommonUtils.isMobileNO(strPhoneNumber)) {
            Tst.showToast("请输入正确的手机号");
            return false;
        } else if (TextUtils.isEmpty(strArea)) {
            Tst.showToast("请选择所在地区");
            return false;
        } else if (TextUtils.isEmpty(strDetailsAddres)) {
            Tst.showToast("详细地址不能为空");
            return false;
        } else if (strDetailsAddres.length() > 60) {
            Tst.showToast("详细地址最多为60个字");
            return false;
        } else if (strDetailsAddres.length() < 5) {
            Tst.showToast("详细地址最少为5个字");
            return false;
        }
        return true;

    }

    @Override
    public String setStatisticsTitle() {
        return strTitle;
    }

    @Override
    public void onClick(View v) {
        clearFoucs();
        if (v.getId() == R.id.rl_choose_are) {
            Intent intent = new Intent(this, ChooseAreaActivityDialog.class);
            startActivityForResult(intent, 11);
        } else if (v.getId() == R.id.tv_save_address) {
            if (!verifyData()) return;
            reqApi();
        }
    }


    @Override
    protected void reqApi() {

        Map<String, Object> addressParamMap = new HashMap<>();
        addressParamMap.put("userId", ConstantObj.TEMP_USERID);
        if (addressDetails != null) addressParamMap.put("addressId", addressDetails.addressid);
        addressParamMap.put("mailingName", strAddressee);
        addressParamMap.put("mailingPhone", strPhoneNumber);
        addressParamMap.put("mailingArea", strArea);
        addressParamMap.put("mailingAddress", strDetailsAddres);
        addressParamMap.put("addressType", mToggleButton.isChecked() ? 1 : 2);

        Observable<ResponseBody<CreateAddress>> reqApi = addressDetails == null ? RxRequestApi.getInstance().getApiService().createShippingAddress(addressParamMap) : RxRequestApi.getInstance().getApiService().updateShippingAddress(addressParamMap);
        reqApi.compose(this.<ResponseBody<CreateAddress>>applySchedulers())
                .subscribe(new Action1<ResponseBody<CreateAddress>>() {
                    @Override
                    public void call(ResponseBody<CreateAddress> shippingAddresResponseBody) {
                        Tst.showToast(shippingAddresResponseBody.desc);
                        if (shippingAddresResponseBody.isSuccess1()) {
                            RxBus.get().post(BusAction.REFRESH_SHIPPINGADDRES, "");
                            getIntent().putExtra("addresDetails", shippingAddresResponseBody.data);
                            setResult(RESULT_OK, getIntent());
                            finshActivity();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Tst.showToast(throwable.getMessage());
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        City city = (City) data.getSerializableExtra("city");
        if (city != null) {
            mShowArea.setText(city.name);
            mChooseArea.setText("");
        }
    }

    @Override
    public void finshActivity() {
        hideKeyboard();
        super.finshActivity();
    }

    private void clearFoucs() {
        hideKeyboard();
        mAddressee.clearFocus();
        mDetailsAddress.clearFocus();
        mContactNumber.clearFocus();
    }
}
