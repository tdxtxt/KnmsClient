package com.knms.activity;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.TextAppearanceSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.knms.activity.base.BaseActivity;
import com.knms.activity.pic.ImgBrowerPagerActivity;
import com.knms.adapter.ComplaintDetailsAdapter;
import com.knms.adapter.baserecycler.BaseQuickAdapter;
import com.knms.android.R;
import com.knms.bean.ResponseBody;
import com.knms.bean.complaint.ComplaintDetail;
import com.knms.bean.user.User;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.net.HttpConstant;
import com.knms.net.ReqApi;
import com.knms.oncall.AsyncHttpCallBack;
import com.knms.util.CommonUtils;
import com.knms.util.Tst;
import com.knms.view.clash.FullyGridLayoutManager;
import com.umeng.analytics.MobclickAgent;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class ComplaintDetailsActivity extends BaseActivity implements
        OnClickListener, Chronometer.OnChronometerTickListener {

    private ImageView mBack;
    private Chronometer mResponseTime;
    private TextView complaintTime, contactNumber, contactName, complaintType, complaintContent;
    private RecyclerView recyclerView;
    private ComplaintDetailsAdapter adapter;
    private String complaintsId;
    private TextView title, txtAdvanceCompensate;
    private PullToRefreshScrollView pullToRefreshScrollView;
    private long timeDifference = 0;


    @Override
    protected void getParmas(Intent intent) {
        complaintsId = intent.getStringExtra("complaintsId");
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_complaint_details;
    }

    @Override
    protected void initView() {
        mBack = findView(R.id.iv_back);
        mResponseTime = findView(R.id.response_time);
        complaintTime = findView(R.id.complaint_time);
        contactNumber = findView(R.id.contact_number);
        contactName = findView(R.id.contact_name);
        complaintType = findView(R.id.complaint_type);
        complaintContent = findView(R.id.complaint_content);
        recyclerView = findView(R.id.recycler_view);
        title = (TextView) findViewById(R.id.complaint_title);
        txtAdvanceCompensate = (TextView) findViewById(R.id.txt_advance_compensate);
        pullToRefreshScrollView = (PullToRefreshScrollView) findViewById(R.id.pull_to_refresh_scrollview);
        pullToRefreshScrollView.setMode(PullToRefreshBase.Mode.BOTH);
    }

    @Override
    protected void initData() {
        mResponseTime.setOnChronometerTickListener(this);
        mBack.setOnClickListener(this);
        reqApi();
    }

    @Override
    public String setStatisticsTitle() {
        return "投诉详情";
    }

    @Override
    protected void reqApi() {
        showProgress();
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("ocid", complaintsId);
        ReqApi.getInstance().postMethod(HttpConstant.complaintsData, params, new AsyncHttpCallBack<ComplaintDetail>() {
            @Override
            public void onSuccess(ResponseBody<ComplaintDetail> body) {
                hideProgress();
                if (body.isSuccess()) {
                    updateView(body.data);
                } else {
                    Tst.showToast(body.desc);
                }
            }

            @Override
            public void onFailure(String msg) {
                hideProgress();
                Tst.showToast(msg);
            }

            @Override
            public Type setType() {
                return new TypeToken<ResponseBody<ComplaintDetail>>() {
                }.getType();
            }
        });
    }

    private void updateView(ComplaintDetail data) {
        String strD1 = "";
        if (data.ocstate == 2) {//受理
            findViewById(R.id.accept_layout).setVisibility(View.VISIBLE);
            txtAdvanceCompensate.setVisibility(View.GONE);
            title.setText("投诉响应时间");
            strD1 = data.ocservicetime;
        } else if (data.ocstate == 3) {//处理完成
            txtAdvanceCompensate.setVisibility(View.GONE);
            findView(R.id.deal_with).setVisibility(View.VISIBLE);
            title.setText("投诉响应时间");
            strD1 = data.ocservicetime;
        } else {
            String str1 = "亲，您已投诉成功！请不要着急，我们的客服会在24小时内与您联系，若超过24小时，您可申请先行赔付，请注意查看消息中心与接听电话";
            String str2 = "亲，您已投诉成功！请不要着急，我们的客服会在24小时内与您联系，请注意查看消息中心与接听电话";
            SpannableString spStr = new SpannableString(data.ispayment == 1 ? str1 : str2);
            spStr.setSpan(new TextAppearanceSpan(this, R.style.style_textview), 4, 9, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            if (data.ispayment == 1) {
                spStr.setSpan(new ClickableSpan() {
                    @Override
                    public void updateDrawState(TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setColor(0xffFB6161);
                        ds.setUnderlineText(true);
                    }

                    @Override
                    public void onClick(View widget) {
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("url", "https://h5.kebuyer.com/document/pay_in_advance.html");
                        startActivityAnimGeneral(CommWebViewActivity.class, map);
                    }
                }, 44, 48, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            txtAdvanceCompensate.setHighlightColor(Color.TRANSPARENT); //设置点击后的颜色为透明，否则会一直出现高亮
            txtAdvanceCompensate.setText(spStr);
            txtAdvanceCompensate.setMovementMethod(LinkMovementMethod.getInstance());//开始响应点击事件
            txtAdvanceCompensate.setVisibility(View.VISIBLE);
            title.setText("已投诉时间");
            strD1 = data.sysCurrentTime;
        }
        contactNumber.setText(CommonUtils.phoneNumberFormat(data.ocrelationmobile));
        contactName.setText(data.ocrelationname);
        complaintType.setText(data.occomplaintstype);
        complaintContent.setText(data.occontent);
        complaintTime.setText(data.occreated);
        DateFormat formart = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (data.imgList != null && data.imgList.size() > 0) {
            adapter = new ComplaintDetailsAdapter(ComplaintDetailsActivity.this, data.imgList);
            recyclerView.setLayoutManager(new FullyGridLayoutManager(this, 4));//类似gridview
            recyclerView.setAdapter(adapter);
            adapter.setOnRecyclerViewItemClickListener(new BaseQuickAdapter.OnRecyclerViewItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    MobclickAgent.onEvent(ComplaintDetailsActivity.this, "orderComplaintDetailsImageClick");
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("pics", adapter.getData());
                    map.put("position", position);
                    startActivityAnimGeneral(ImgBrowerPagerActivity.class, map);
                }
            });
            recyclerView.setLayoutFrozen(true);
        }

        try {
            Date d1 = formart.parse(strD1);
            Date d2 = formart.parse(data.occreated);
            timeDifference = d1.getTime() - d2.getTime();
            long days = timeDifference / (1000 * 60 * 60 * 24);
            if (days >= 1) {
                mResponseTime.setText("24:00:00");
                mResponseTime.setTextColor(getResources().getColor(R.color.common_red));
            } else {
                mResponseTime.setText(timeFormat(timeDifference));
                if (data.ocstate == 1) {
                    mResponseTime.start();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finshActivity();
                break;
        }
    }

    @Override
    public void onChronometerTick(Chronometer chronometer) {
        if (chronometer.getText().toString().equals("24:00:00")) {
            mResponseTime.setText("24:00:00");
            mResponseTime.setTextColor(getResources().getColor(R.color.common_red));
            mResponseTime.stop();
        }
        mResponseTime.setText(timeFormat(timeDifference));
    }

    private String timeFormat(long time) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        String hms = formatter.format(time);
        timeDifference += 1000;
        return hms;
    }

    @Subscribe(tags = {@Tag(BusAction.ACTION_LOGIN)})
    public void loginAccout(User user) {
        finshActivity();
    }
}
