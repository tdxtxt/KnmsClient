package com.knms.activity.goods;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.knms.activity.base.BaseFragmentActivity;
import com.knms.activity.im.MessageCenterActivity;
import com.knms.activity.login.FasterLoginActivity;
import com.knms.core.im.IMHelper;
import com.knms.fragment.DiyKnowledgeFragment;
import com.knms.fragment.DiyStyleFragment;
import com.knms.android.R;
import com.knms.util.SPUtils;

import java.util.ArrayList;
import java.util.List;

import rx.Subscription;
import rx.functions.Action1;

/**
 * Created by tdx on 2016/8/30.
 * 家具灵感【家装风格&家具百科】
 */
public class DiyInspirationActivity extends BaseFragmentActivity{
    private List<Fragment> fragments;
    private DiyStyleFragment fragment_diy_style;
    private DiyKnowledgeFragment fragment_diy_knowledge;
    private View view_line_style,view_line_knowledge;
    private String typeId;
    private int decorateType;//2:家装风格; 3:家具百科
    public static int DecorateStyle = 2;
    public static int DecorateKnowledge = 3;
    private TextView tv_jzfg,tv_jjbk;
    @Override
    protected int layoutResID() {
        return R.layout.activity_diy_inspiration;
    }

    @Override
    protected void getParmas(Intent intent) {
        decorateType = intent.getIntExtra("decorateType",DecorateStyle);//表示 家装风格&家具百科
        typeId = intent.getStringExtra("typeId");//表示 家装风格中的哪一类
    }

    @Override
    protected void initView() {
        view_line_style = findView(R.id.view_line_style);
        view_line_knowledge = findView(R.id.view_line_knowledge);
        tv_jjbk=findView(R.id.tv_jjbk);
        tv_jzfg=findView(R.id.tv_jzfg);
    }

    @Override
    protected void initData() {
        fragments = new ArrayList<Fragment>();
        fragment_diy_style = DiyStyleFragment.newInstance(DecorateStyle,typeId);
        fragment_diy_knowledge = DiyKnowledgeFragment.newInstance(DecorateKnowledge,typeId);
        fragments.add(fragment_diy_style);
        fragments.add(fragment_diy_knowledge);

        if(decorateType == DecorateKnowledge){
            setDiyKnowledge();
        }else {
            setDiyStyle();
        }

        findView(R.id.rl_style).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDiyStyle();
            }
        });
        findView(R.id.rl_knowledge).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDiyKnowledge();
            }
        });
        findView(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finshActivity();
            }
        });
        findView(R.id.iv_icon_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(SPUtils.isLogin())
                    startActivityAnimGeneral(MessageCenterActivity.class,null);
                else
                    startActivityAnimGeneral(FasterLoginActivity.class,null);
            }
        });
    }

    @Override
    public String setStatisticsTitle() {
        return "家居灵感";
    }

    private void setDiyStyle(){
        this.changeFragment(fragment_diy_style);
        view_line_style.setVisibility(View.VISIBLE);
        tv_jzfg.setTextColor(0xff333333);
        tv_jjbk.setTextColor(0xff666666);
        view_line_knowledge.setVisibility(View.GONE);
    }

    private void setDiyKnowledge(){
        this.changeFragment(fragment_diy_knowledge);
        view_line_knowledge.setVisibility(View.VISIBLE);
        view_line_style.setVisibility(View.GONE);
        tv_jjbk.setTextColor(0xff333333);
        tv_jzfg.setTextColor(0xff666666);
    }

    private void changeFragment(Fragment fragment) {
        FragmentManager fm = super.getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.main_content, fragment);
        transaction.commit();
    }
    Subscription subscriptionMsgCount;
    @Override
    protected void onResume() {
        super.onResume();
        if(subscriptionMsgCount != null) subscriptionMsgCount.unsubscribe();
        subscriptionMsgCount = IMHelper.getInstance().isUnreadMsg().subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean aBoolean) {
                if(aBoolean) ((ImageView)findView(R.id.iv_icon_right)).setImageResource(R.drawable.home_03);
                else ((ImageView)findView(R.id.iv_icon_right)).setImageResource(R.drawable.home_12);
            }
        });
    }

    @Override
    protected void onDestroy() {
        if(subscriptionMsgCount != null) subscriptionMsgCount.unsubscribe();
        super.onDestroy();
    }

}
