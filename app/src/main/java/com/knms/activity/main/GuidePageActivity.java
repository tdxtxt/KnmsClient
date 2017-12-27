package com.knms.activity.main;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.knms.activity.base.BaseActivity;
import com.knms.adapter.GuidePageAdapter;
import com.knms.android.BuildConfig;
import com.knms.android.R;
import com.knms.app.KnmsApp;
import com.knms.bean.GuidePage;
import com.knms.util.SPUtils;
import com.knms.view.banner.AutoViewPager;
import com.knms.view.banner.CirclePageIndicator;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/2/9.
 */

public class GuidePageActivity extends BaseActivity {

    private AutoViewPager mViewPager;
    private GuidePageAdapter mAdapter;
    private List<GuidePage> list;
    private TextView tvSkip;
    private ImageView ivIntoApp;
    private int lastPos=0;
    private boolean isLeft=false;

    @Override
    protected int layoutResID() {
        return R.layout.activity_guide_page_layout;
    }

    @Override
    protected void initView() {
        mViewPager= (AutoViewPager) findViewById(R.id.vp_guide_page);
        tvSkip= (TextView) findViewById(R.id.tv_skip);
        ivIntoApp= (ImageView) findViewById(R.id.tv_into_app);

        mViewPager.setBoundaryLooping(false);
        mViewPager.setAuto(false);
        ivIntoApp.setOnClickListener(onClickListener);
        tvSkip.setOnClickListener(onClickListener);

        list=new ArrayList<>();
        list.add(new GuidePage(R.drawable.guide_title_01,R.drawable.guide_content_01,R.drawable.guide_bg_01));
        list.add(new GuidePage(R.drawable.guide_title_02,R.drawable.guide_content_02,R.drawable.guide_bg_01));
        list.add(new GuidePage(R.drawable.guide_title_04,R.drawable.guide_content_04,R.drawable.guide_bg_01));
        list.add(new GuidePage(R.drawable.guide_title_03,R.drawable.guide_content_03,R.drawable.guide_bg_01));

        mAdapter=new GuidePageAdapter(list,this);
        CirclePageIndicator cpi_banner_advertisement = (CirclePageIndicator)findViewById(R.id.cpi_guide_page);
        mViewPager.setAdapter(mAdapter);
        cpi_banner_advertisement.setViewPager(mViewPager);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                isLeft=lastPos>position;
                lastPos=position;
            }
            @Override
            public void onPageSelected(int position) {
                tvSkip.setVisibility(position!=list.size()-1?View.VISIBLE:View.GONE);
                ivIntoApp.setVisibility(position==list.size()-1?View.VISIBLE:View.INVISIBLE);
                MobclickAgent.onEvent(GuidePageActivity.this,isLeft?"featuresIntroductionLeftSlide":"featuresIntroductionRightSlide");
            }
            @Override
            public void onPageScrollStateChanged(int state) { }
        });

        SPUtils.saveToApp(SPUtils.KeyConstant.current_versions, BuildConfig.SER_VERSION_CODE);

    }

    private View.OnClickListener onClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(view.getId()==R.id.tv_into_app)
                MobclickAgent.onEvent(GuidePageActivity.this,"featuresIntroductionInterAPPClick");
            else
                MobclickAgent.onEvent(GuidePageActivity.this,"featuresIntroductionJumpClick");
            startActivity(new Intent(GuidePageActivity.this,MainActivity.class));
            KnmsApp.getInstance().finishActivity(GuidePageActivity.this);
        }
    };

    @Override
    protected void initData() {
    }

    @Override
    public String setStatisticsTitle() {
        return "产品引导页";
    }

}
