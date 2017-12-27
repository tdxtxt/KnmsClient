package com.knms.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.knms.activity.goods.IdleFurnitureActivity;
import com.knms.adapter.IdleClassifyPagerAdapter;
import com.knms.android.R;
import com.knms.bean.IdleClassify;
import com.knms.bean.ResponseBody;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.fragment.base.BaseFragment;
import com.knms.net.RxRequestApi;
import com.knms.util.ToolsHelper;
import com.knms.util.Tst;
import com.knms.view.PagerSlidingTabStrip;
import com.knms.view.sticky.HeaderScrollHelper;

import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by tdx on 2016/9/1.
 * 闲置家具分类
 */
public class IdleClassifyFragment extends BaseFragment {
    private PagerSlidingTabStrip pager_tab_strip;
    private ViewPager vp_content;
    private IdleClassifyPagerAdapter fg_adapter;
    private int pageNum = 1;
    private String goclassifyid = "";
    Subscription subscription;

    public static IdleClassifyFragment newInstance() {
        IdleClassifyFragment fragment = new IdleClassifyFragment();
//        Bundle args = new Bundle();
//        args.putInt("decorateType", decorateType);
//        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RxBus.get().register(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.comm_fragment_viewpager, null);
        pager_tab_strip = (PagerSlidingTabStrip) view.findViewById(R.id.pager_tab_strip);
        vp_content = (ViewPager) view.findViewById(R.id.vp_content);
        init();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        reqApi();
    }

    @Override
    public void reqApi() {
        if (subscription != null) subscription.unsubscribe();
        subscription = RxRequestApi.getInstance().getApiService().getIdleClassifys()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<List<IdleClassify>>>() {
                    @Override
                    public void call(ResponseBody<List<IdleClassify>> body) {
                        if (body.isSuccess()) {
                            updateView(body.data);
                        } else {
                            Tst.showToast(body.desc);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Tst.showToast(throwable.toString());
                    }
                });
    }

    public void updateView(List<IdleClassify> data) {
        if (data != null && data.size() > 0) {
            ToolsHelper.getInstance().sort(data,"seq");//按照seq排序
            int position = vp_content.getCurrentItem();
            fg_adapter = new IdleClassifyPagerAdapter(getFragmentManager(), data);
            //关闭预加载，默认一次只加载一个Fragment
            vp_content.setOffscreenPageLimit(1);
            vp_content.setAdapter(fg_adapter);
            pager_tab_strip.setViewPager(vp_content);
            vp_content.setCurrentItem(position);
            IdleFurnitureActivity.getPulltoLayout().setCurrentScrollableContainer((HeaderScrollHelper.ScrollableContainer) fg_adapter.getItem(0));

            vp_content.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    IdleFurnitureActivity.getPulltoLayout().setCurrentScrollableContainer((HeaderScrollHelper.ScrollableContainer) fg_adapter.getItem(position));
                }
            });
        }
    }

    private void init() {
        pager_tab_strip.setAllCaps(false);
        pager_tab_strip.setIndicatorColor(getResources().getColor(R.color.no_color));
        pager_tab_strip.setShowSelectedTabBg(true);
    }

    @Override
    public void onDestroy() {
        RxBus.get().unregister(this);
        super.onDestroy();
    }

    @Subscribe(tags = {@Tag(BusAction.REFRESH_IDLE)})
    public void refresh(Boolean isRefresh) {
        List<Fragment> fragments = getFragmentManager().getFragments();
        if(fragments != null && fragments.size() > 0 && fragments.size() > vp_content.getCurrentItem()){
            for (Fragment fragment: fragments) {
                if(fragment instanceof IdleClassifyChildFragment){
                    IdleClassifyChildFragment idleClassifyFragment = (IdleClassifyChildFragment) fragment;
                    if(TextUtils.isEmpty(fg_adapter.getItemObject(vp_content.getCurrentItem()).clid)) continue;
                    if(fg_adapter.getItemObject(vp_content.getCurrentItem()).clid.equals(idleClassifyFragment.typeId)){
                        if(isRefresh)idleClassifyFragment.refreshApi();
                        else idleClassifyFragment.scrollTo();
                        return;
                    }
                }
            }
        }
    }

}
