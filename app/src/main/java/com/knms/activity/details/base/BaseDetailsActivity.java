package com.knms.activity.details.base;

import android.content.Intent;
import android.support.v4.widget.NestedScrollView;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gyf.barlibrary.ImmersionBar;
import com.knms.activity.base.BaseActivity;
import com.knms.adapter.NestedScrollViewOverScrollDecorAdapter;
import com.knms.android.R;
import com.knms.core.im.IMHelper;
import com.knms.util.LocalDisplay;

import me.everything.android.ui.overscroll.VerticalOverScrollBounceEffectDecorator;
import rx.Subscription;
import rx.functions.Action1;


/**
 * Created by Administrator on 2017/7/11.
 */

public abstract class BaseDetailsActivity extends BaseActivity {
    protected ViewStub viewsbHead,viewsbTop,viewsbFooter,viewsbStivkyOne,viewsbStivkyTwo,viewsbMenu;
    protected View viewHead,viewTop,viewFooter,viewMenu,viewStivkyOne,viewStivkyTwo;
    protected NestedScrollView scrollView;
    protected TextView tvError;
    protected ImageView ivContactMerchant,mAnimImageView;

    public String goodsId = "";
    @Override
    protected void getParmas(Intent intent) {
        super.getParmas(intent);
        goodsId = intent.getStringExtra("id");
    }
    protected void initBar(View view){
        if(view == null) return;
        mImmersionBar = ImmersionBar.with(this);
        mImmersionBar.statusBarView(view)
//                .keyboardEnable(true)  //解决软键盘与底部输入框冲突问题
                .statusBarDarkFont(true, 0.1f)//状态栏字体是深色，不写默认为亮色
                .flymeOSStatusBarFontColor(R.color.status_bar_textcolor);  //修改flyme OS状态栏字体颜色;
        mImmersionBar.init();

        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) viewsbStivkyOne.getLayoutParams();
        int height = LocalDisplay.getViewSize(viewHead)[1];
        lp.setMargins(0,height,0,0);
        viewsbStivkyOne.setLayoutParams(lp);
    }
    @Override
    protected int layoutResID() {
        return R.layout.base_act_details;
    }

    @Override
    protected void initView() {
        mAnimImageView =findView(R.id.cart_anim_icon);

        ivContactMerchant=findView(R.id.iv_contact_merchant);
        scrollView = findView(R.id.scrollView);
        viewsbHead = findView(R.id.viewstub_head);
        viewsbTop = findView(R.id.viewstub_top);
        viewsbFooter = findView(R.id.viewstub_footer);
        viewsbStivkyOne = findView(R.id.viewstub_sticky_one);
        viewsbStivkyTwo = findView(R.id.viewstub_sticky_two);
        viewsbMenu = findView(R.id.viewstub_menu);

        tvError = findView(R.id.sold_out_layout);

        viewHead = loadHeadView(viewsbHead);
        viewTop = loadTopView(viewsbTop);
        View[] views = loadStivkyView(viewsbStivkyOne,viewsbStivkyTwo);
        if(views != null && views.length == 2){
            viewStivkyOne = views[0];
            viewStivkyTwo = views[1];
        }
        viewFooter = loadFooterView(viewsbFooter);
        viewMenu = loadMenuView(viewsbMenu);

        initBar(viewHead.findViewById(R.id.view));
        new VerticalOverScrollBounceEffectDecorator(new NestedScrollViewOverScrollDecorAdapter(scrollView));
    }
    public Subscription subscriptionMsgCount;
    public boolean isMsg = false;
    @Override
    protected void onResume() {
        super.onResume();
        showImMsg();
    }
    public void showImMsg(){
        if(subscriptionMsgCount != null) subscriptionMsgCount.unsubscribe();
        subscriptionMsgCount = IMHelper.getInstance().isUnreadMsg().subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean aBoolean) {
                isMsg = aBoolean;
                updateMsgTip(aBoolean);
            }
        });
    }

    @Override
    protected void onDestroy() {
        if(subscriptionMsgCount != null) subscriptionMsgCount.unsubscribe();
        super.onDestroy();
    }

    public abstract View loadHeadView(ViewStub viewStub);
    public abstract View loadTopView(ViewStub viewStub);
    public abstract View[] loadStivkyView(ViewStub viewStubOne,ViewStub viewStubTwo);
    public abstract View loadFooterView(ViewStub viewStub);
    public abstract View loadMenuView(ViewStub viewStub);
    public abstract void updateMsgTip(boolean isUnreadMsg);

}
