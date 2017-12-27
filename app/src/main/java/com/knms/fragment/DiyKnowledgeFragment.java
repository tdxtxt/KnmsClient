package com.knms.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.reflect.TypeToken;
import com.knms.adapter.DiyStylePagerAdapter;
import com.knms.android.R;
import com.knms.bean.DiyInsp;
import com.knms.bean.ResponseBody;
import com.knms.bean.other.Tab;
import com.knms.fragment.base.BaseFragment;
import com.knms.net.HttpConstant;
import com.knms.net.ReqApi;
import com.knms.net.RxRequestApi;
import com.knms.oncall.AsyncHttpCallBack;
import com.knms.util.LocalDisplay;
import com.knms.util.ToolsHelper;
import com.knms.util.Tst;
import com.knms.view.PagerSlidingTabStrip;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/8/30.
 * 家具百科
 */
public class DiyKnowledgeFragment extends BaseFragment{
    private DiyStylePagerAdapter fg_adapter;
    private int pageNum = 1;
    private int decorateType = 0;
    private PagerSlidingTabStrip pager_tab_strip;
    private ViewPager vp_content;
    private String typeId;
    public static DiyKnowledgeFragment newInstance(int decorateType,String typeId) {
        DiyKnowledgeFragment fragment = new DiyKnowledgeFragment();
        Bundle args = new Bundle();
        args.putInt("decorateType", decorateType);
        args.putString("typeId",typeId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            decorateType = getArguments().getInt("decorateType");
            typeId = getArguments().getString("typeId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.comm_fragment_viewpager,null);
        pager_tab_strip = (PagerSlidingTabStrip) view.findViewById(R.id.pager_tab_strip);
        vp_content = (ViewPager) view.findViewById(R.id.vp_content);
        init();
        reqApi();
        return view;
    }

    private void init() {
        pager_tab_strip.setAllCaps(false);
        pager_tab_strip.setIndicatorColor(getResources().getColor(R.color.no_color));
        pager_tab_strip.setShowSelectedTabBg(true);
    }
    Subscription subscription;
    @Override
    public void reqApi() {
        subscription = RxRequestApi.getInstance().getApiService().getInspirationTabs(decorateType)
                .observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
                .subscribe(new Action1<ResponseBody<List<Tab>>>() {
                    @Override
                    public void call(ResponseBody<List<Tab>> body) {
                        if(body.isSuccess()){
                            updateView(body.data);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        hideProgress();
                        Tst.showToast(throwable.toString());
                    }
                });
    }
    public void updateView(List<Tab> data){
        if(data != null && data.size() > 0){
            ToolsHelper.getInstance().sort(data,"seq");
            int position = vp_content.getCurrentItem();
            fg_adapter = new DiyStylePagerAdapter(getFragmentManager(),data,decorateType);
            //关闭预加载，默认一次只加载一个Fragment
            vp_content.setOffscreenPageLimit(1);
            vp_content.setAdapter(fg_adapter);
            pager_tab_strip.setViewPager(vp_content);
            if(!TextUtils.isEmpty(typeId)){
                for (int i = 0; i < data.size(); i++) {
                    if(data.get(i).id.equals(typeId)){
                        position = i;
                        break;
                    }
                }
            }
            vp_content.setCurrentItem(position);
        }
    }

    @Override
    public void onDestroy() {
        if(subscription != null) subscription.unsubscribe();
        super.onDestroy();
    }
}
