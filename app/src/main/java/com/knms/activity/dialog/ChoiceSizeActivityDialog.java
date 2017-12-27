package com.knms.activity.dialog;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.text.TextUtils;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gyf.barlibrary.ImmersionBar;
import com.knms.activity.base.BaseActivity;
import com.knms.activity.pic.ImgBrowerPagerActivity;
import com.knms.adapter.AttrValueAdapter;
import com.knms.adapter.NestedScrollViewOverScrollDecorAdapter;
import com.knms.android.R;
import com.knms.app.KnmsApp;
import com.knms.bean.ResponseBody;
import com.knms.bean.sku.SkuBody;
import com.knms.core.sku.Sku;
import com.knms.bean.sku.base.SkuModel;
import com.knms.bean.sku.base.Spec;
import com.knms.bean.sku.base.Vaule;
import com.knms.net.RxRequestApi;
import com.knms.oncall.OnClick;
import com.knms.oncall.SkuItemClickListenter;
import com.knms.other.SkuDataUI;
import com.knms.util.ImageLoadHelper;
import com.knms.util.ToolsHelper;
import com.knms.util.Tst;
import com.knms.view.flowlayout.TagFlowLayout;
import com.knms.view.tv.CounterView;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import me.everything.android.ui.overscroll.VerticalOverScrollBounceEffectDecorator;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


/**
 * Created by tdx on 2017/7/10.
 * 商品-规格选择界面
 * 传参:其中data和goid必须传一个，其他为非必传参数
 * data - ComdiBo  库存接口请求返回对象
 * goid - String  商品id
 * img - String  商品主图
 * initCount - int   初始化选中数量
 * initSkuId - String   初始化选中规格商品id
 * 返回值:
 * resultCount - int  规格商品购买数量
 * resultSku - SkuModel sku对象
 * resultSkuId - skuId String
 */

public class ChoiceSizeActivityDialog extends BaseActivity implements OnClick<View,SkuModel> {
    LinearLayout layoutGroup;
    CounterView countView;
    ImageView ivImg;//图片
    TextView tvStock;//库存
    TextView tvPrice;//价格
    TextView tvLimit;//限购提示语（每人限购x件）

    private SkuBody.ComdiBo mComdiBo;//库存接口请求返回对象
    private String goid;//商品id
    private int initCount = 1;
    private String initSkuId;
    private boolean isShowCount = true;

    private SkuDataUI mSkuDataUI;
    @Override
    protected int layoutResID() {
        return R.layout.dialog_act_choice_size;
    }

    @Override
    protected void getParmas(Intent intent) {
        mComdiBo = (SkuBody.ComdiBo) getIntent().getSerializableExtra("data");
        goid = intent.getStringExtra("goid");
        initCount = intent.getIntExtra("initCount",1);
        initSkuId = intent.getStringExtra("initSkuId");
        isShowCount = intent.getBooleanExtra("isShowCount",true);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager m = getWindowManager();
        Display d = m.getDefaultDisplay(); // 为获取屏幕宽、高
        WindowManager.LayoutParams p = getWindow().getAttributes(); // 获取对话框当前的参数值
        p.width = (int) (d.getWidth() * 1.0); // 满屏宽度
        getWindow().setAttributes(p);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    private void loadData() {
        Map<String,Object> params = new HashMap<>();
        params.put("showId",goid);
        RxRequestApi.getInstance().getApiService().getSkuProduct(params)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<ResponseBody<SkuBody>>() {
                    @Override
                    public void call(ResponseBody<SkuBody> body) {
                        if(body.isSuccess1()){
                            mComdiBo = body.data.comdiBo;
                            updateView();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {}
                });
    }
    @Override
    public void initView(){
        layoutGroup = (LinearLayout) findViewById(R.id.layout_group);
        countView = (CounterView) findViewById(R.id.countView);
        ivImg = (ImageView) findViewById(R.id.image);
        tvPrice = (TextView) findViewById(R.id.tv_price);
        tvStock = (TextView) findViewById(R.id.tv_stock);
        tvLimit = findView(R.id.tv_limit);
        if(isShowCount){
            findView(R.id.rl_count).setVisibility(View.VISIBLE);
        }else{
            findView(R.id.rl_count).setVisibility(View.GONE);
        }
        NestedScrollView scrollView = (NestedScrollView) findViewById(R.id.scrollView);
        new VerticalOverScrollBounceEffectDecorator(new NestedScrollViewOverScrollDecorAdapter(scrollView));
    }
    @Override
    public void initData(){
        findViewById(R.id.rl_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCancelData();
            }
        });
        findViewById(R.id.ivBtn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCancelData();
            }
        });
        findViewById(R.id.ll_layout).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        findViewById(R.id.tvBtn_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendConfirmData();
            }
        });
        ivImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MobclickAgent.onEvent(ChoiceSizeActivityDialog.this,"product_openSpecimgs");
                if(mSkuDataUI != null && mSkuDataUI.imgs != null && mSkuDataUI.imgs.size() > 0){
                    String currentImg = (String) v.getTag();
                    if(TextUtils.isEmpty(currentImg)) currentImg = mComdiBo.defaultProductImg;
                    if(!TextUtils.isEmpty(currentImg)){
                        Map<String,Object> parmas = new HashMap<>();
                        List<String> data = new ArrayList<String>();
                        data.addAll(mSkuDataUI.imgs);
                        parmas.put("data",data);
                        parmas.put("currentPath",currentImg);
                        startActivityGeneral(ImgBrowerPagerActivity.class,parmas);
                    }
                }
            }
        });
        if(mComdiBo == null){
            loadData();
        }else{
            updateView();
        }
    }
    public void updateView(){
        if(isShowCount){
            countView.setToastMaxmsg(mComdiBo.toastMsg);
            //设置最大限制
            long minLimit = Math.min(mComdiBo.maxBuyNumByAccount, mComdiBo.maxBuyNumByOrder);
            minLimit = minLimit > 0 ? minLimit : Math.max(mComdiBo.maxBuyNumByAccount, mComdiBo.maxBuyNumByOrder);
            countView.setMaxValue((int)Math.min(mComdiBo.getTotalStock(), minLimit));
            countView.addToastMsg(new CounterView.ToastMsgCallback() {
                @Override
                public void maxToastMsg(int currentCount) {
                    if(currentCount == mComdiBo.maxBuyNumByAccount){
                        Tst.showToast("已是商品最大购买数量");//账号最大购买数量
                    }else if(currentCount == mComdiBo.maxBuyNumByOrder){
                        Tst.showToast("已是商品最大购买数量");//订单最大购买数量
                    }else if(currentCount == mComdiBo.getTotalStock()){
                        Tst.showToast("库存不足,无法添加");
                    }
                }
            });
            tvLimit.setText("(" + mComdiBo.toastMsg + ")");
            countView.setCountValue(initCount);
        }
        List<SkuModel> skuProducts = mComdiBo.skuProducts;
        Sku.getSkuData(skuProducts).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<SkuDataUI>() {
                    @Override
                    public void call(SkuDataUI skuDataUI) {
                        mSkuDataUI = skuDataUI;
                        if(mSkuDataUI.defaultSku != null){
                            mSkuDataUI.defaultSku.imageUrl = mComdiBo.defaultProductImg;
                        }
                        if(mSkuDataUI.imgs == null){
                            mSkuDataUI.imgs = new HashSet<>();
                        }
                        mSkuDataUI.imgs.add(mComdiBo.defaultProductImg);
                        for (Spec spec : skuDataUI.specs) {
                            View groupView = View.inflate(ChoiceSizeActivityDialog.this, R.layout.item_spec, null);
                            TextView groupNameTv = (TextView) groupView.findViewById(R.id.tv_title);
                            TagFlowLayout tagFlowLayout = (TagFlowLayout) groupView.findViewById(R.id.tag_flow_layout);
                            groupNameTv.setText(spec.name);
                            AttrValueAdapter adapter = new AttrValueAdapter(spec.vaules);
                            tagFlowLayout.setAdapter(adapter);
                            skuDataUI.attrValueAdapters.add(adapter);
                            if (adapter.currentVaule != null)
                                mSkuDataUI.selectAllVaules.add(adapter.currentVaule);
                            //设置点击监听器
                            SkuItemClickListenter listenter = new SkuItemClickListenter(skuDataUI, adapter, ChoiceSizeActivityDialog.this);
                            tagFlowLayout.setOnTagClickListener(listenter);
                            layoutGroup.addView(groupView);
                        }

                        //初始化数据-默认选中被传入的skuId
                        if (!TextUtils.isEmpty(initSkuId) && mSkuDataUI.skuProducts != null) {
                            SkuModel initMode = null;
                            for (SkuModel mode : mSkuDataUI.skuProducts) {
                                if (initSkuId.equals(mode.skuId)) {
                                    initMode = mode;
                                    break;
                                }
                            }
                            //初始化数据-传入规格商品
                            if (initMode != null && initMode.vaules != null && mSkuDataUI.specs != null) {
                                mSkuDataUI.selectAllVaules.clear();
                                mSkuDataUI.selectAllVaules.addAll(initMode.vaules);
                                for (Vaule vaule : initMode.vaules) {
                                    for (AttrValueAdapter adapter : mSkuDataUI.attrValueAdapters) {
                                        if(adapter.getData() != null && adapter.getData().contains(vaule)){
                                            for (Vaule va : adapter.getData()) {
                                                if (vaule.equals(va)) {
                                                    va.status = 1;
                                                    adapter.currentVaule = va;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                                mSkuDataUI.handleBtn();
                                clickItem(null, initMode);
                            }else {
                                mSkuDataUI.handleBtn();
                                clickItem(null, mSkuDataUI.defaultSku);
                            }
                        } else {
                            //初始化数据-默认规格商品
                            mSkuDataUI.handleBtn();
                            clickItem(null, mSkuDataUI.defaultSku);
                        }

                        //排序，若用户没有收到选中属性，点击确认按钮需要进行排序id顺序
                        if (mSkuDataUI.selectAllVaules.size() > 0)
                            ToolsHelper.getInstance().sort(mSkuDataUI.selectAllVaules, "id", true);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Tst.showToast(throwable.getMessage());
                        ChoiceSizeActivityDialog.this.finish();
                    }
                });
    }
    @Override
    public void clickItem(View view, final SkuModel mode) {
        MobclickAgent.onEvent(ChoiceSizeActivityDialog.this,"product_clickSpec");
        if(isShowCount){
            //设置最大限制
            long minLimit = Math.min(mComdiBo.maxBuyNumByAccount, mComdiBo.maxBuyNumByOrder);
            minLimit = minLimit > 0 ? minLimit : Math.max(mComdiBo.maxBuyNumByAccount, mComdiBo.maxBuyNumByOrder);
            countView.setMaxValue((int)Math.min(mode.stock, minLimit));
            countView.addToastMsg(new CounterView.ToastMsgCallback() {
                @Override
                public void maxToastMsg(int currentCount) {
                    if(currentCount == mComdiBo.maxBuyNumByAccount){
                        Tst.showToast("已是商品最大购买数量");//账号最大购买数量
                    }else if(currentCount == mComdiBo.maxBuyNumByOrder){
                        Tst.showToast("已是商品最大购买数量");//订单最大购买数量
                    }else if(currentCount == mode.stock){
                        Tst.showToast("库存不足,无法添加");
                    }else{
                        if(currentCount > mode.stock){
                            Tst.showToast("库存不足,无法添加");
                        }else{
                            Tst.showToast("已是商品最大购买数量");
                        }
                    }
                }
            });
        }

        tvStock.setText("库存:  " + mode.stock + "件");
        ivImg.setTag(mode.imageUrl);
        ImageLoadHelper.getInstance().displayImage(this,TextUtils.isEmpty(mode.imageUrl) ? mComdiBo.defaultProductImg : mode.imageUrl,ivImg);
        if(TextUtils.isEmpty(mode.skuId))tvPrice.setText(mComdiBo.defaultProce);
        else tvPrice.setText("" + mode.price);
    }
    private void sendConfirmData(){
        if(mSkuDataUI != null && mSkuDataUI.selectAllVaules != null &&
                mSkuDataUI.selectAllVaules.size() == mSkuDataUI.specs.size()) {
            String key = TextUtils.join(";", mSkuDataUI.selectAllVaules);
            SkuModel current = mSkuDataUI.localData.get(key);

            getIntent().putExtra("resultSku", current);
            getIntent().putExtra("resultSkuId",current.skuId);
            if (isShowCount) getIntent().putExtra("resultCount", countView.getCountValue());
            setResult(RESULT_OK, getIntent());
            finshActivity();
        }else{
            if (mSkuDataUI != null && mSkuDataUI.specs != null) {
                Tst.showToast("请选择" + mSkuDataUI.getToastMsg());
            } else {
                Tst.showToast("请选择属性");
            }
        }
    }
    private void sendCancelData(){
        if(mSkuDataUI != null && mSkuDataUI.selectAllVaules != null &&
                mSkuDataUI.selectAllVaules.size() == mSkuDataUI.specs.size()) {
            String key = TextUtils.join(";", mSkuDataUI.selectAllVaules);
            SkuModel current = mSkuDataUI.localData.get(key);
            getIntent().putExtra("resultSkuId",current.skuId);
            setResult(RESULT_OK, getIntent());
            finshActivity();
        }else{
            getIntent().putExtra("resultSkuId","");
            setResult(RESULT_OK, getIntent());
            finshActivity();
        }
        finshActivity();
    }
    @Override
    public void finshActivity() {
        KnmsApp.getInstance().finishActivity(this);
    }
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.push_up_out);
    }
    @Override
    public String setStatisticsTitle() {
        return null;
    }
}
