package com.knms.activity.fastfind;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshNestedScrollView;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.knms.activity.base.HeadBaseActivity;
import com.knms.activity.dialog.UseDescActivity;
import com.knms.activity.main.WelcomeActivity;
import com.knms.adapter.ClassifiesLabelAdapter;
import com.knms.adapter.LabelAdapter;
import com.knms.android.R;
import com.knms.bean.ResponseBody;
import com.knms.bean.other.Classify;
import com.knms.bean.other.FastLabel;
import com.knms.bean.other.Label;
import com.knms.net.RxRequestApi;
import com.knms.other.BestBlurOnSubscribe;
import com.knms.other.RetrofitCache;
import com.knms.util.Tst;
import com.knms.view.clash.FullyLinearLayoutManager;
import com.knms.view.clash.FzLinearLayoutManager;
import com.knms.view.flowlayout.TagFlowLayout;
import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.functions.Action1;

/**
 * Created by Administrator on 2017/3/22.
 */

public class QuicklyGoodsAcitvity extends HeadBaseActivity {
    private TagFlowLayout styleLabel;
    private RecyclerView recyclerView;
    private ClassifiesLabelAdapter adapter;
    private PullToRefreshNestedScrollView pullToRefreshScrollView;
    private List<Label> selectStyleLabels;//选择的风格标签
    private List<Classify> selectClassifyLeabels;//选择的分类标签
    List<Classify> classifies;
    private RelativeLayout rl_blur;
    @Override
    protected int layoutResID() {
        return R.layout.activity_quickly_goods;
    }

    @Override
    protected void onResume() {
        super.onResume();
        rl_blur.setVisibility(View.GONE);
    }

    @Override
    protected void initView() {
        styleLabel = findView(R.id.styleLabel);
        rl_blur = findView(R.id.rl_blur);
        styleLabel.setMaxSelectCount(2,"最多可选择2个风格标签哦~");
        pullToRefreshScrollView = findView(R.id.pullToRefreshScrollView);
        recyclerView = findView(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false);
        pullToRefreshScrollView.setMode(PullToRefreshBase.Mode.BOTH);
        setRightMenuCallBack(new RightCallBack() {
            @Override
            public void setRightContent(TextView tv, ImageView icon) {
                tv.setVisibility(View.VISIBLE);
                tv.setText("找货攻略");
                tv.setTextColor(ContextCompat.getColor(tv.getContext(),R.color.yellow));
                icon.setVisibility(View.GONE);
            }
            @Override
            public void onclick() {
                Bitmap bitmap = getIerceptionScreen(QuicklyGoodsAcitvity.this);
                Observable.create(new BestBlurOnSubscribe(QuicklyGoodsAcitvity.this, bitmap, 7, 0.2f))
                .compose(QuicklyGoodsAcitvity.this.<Bitmap>applySchedulers())
                .subscribe(new Action1<Bitmap>() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void call(Bitmap bitmap) {
                        rl_blur.setVisibility(View.VISIBLE);
                        rl_blur.setBackground(new BitmapDrawable(QuicklyGoodsAcitvity.this.getResources(),bitmap));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {}
                });
                startActivity(new Intent(QuicklyGoodsAcitvity.this, UseDescActivity.class));
                overridePendingTransition(R.anim.zoomax, R.anim.zoomin);
            }
        });
    }

    @Override
    protected void initData() {
        adapter = new ClassifiesLabelAdapter(null);
        recyclerView.setAdapter(adapter);
        reqApi();
        pullToRefreshScrollView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<NestedScrollView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<NestedScrollView> refreshView) {
                reset();
                reqApi();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<NestedScrollView> refreshView) {
                pullToRefreshScrollView.onRefreshComplete();
            }
        });
        //选择的风格标签
        styleLabel.setOnSelectListener(new TagFlowLayout.OnSelectListener<Label>() {
            @Override
            public void onSelected(List<Label> selectPosSet, Label remove) {
                MobclickAgent.onEvent(QuicklyGoodsAcitvity.this, "eachStyleTagBtnClick");
                selectStyleLabels = selectPosSet;
            }
        });
        findView(R.id.btn_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(adapter==null) return;
                selectClassifyLeabels = adapter.selects;
                if(!((selectClassifyLeabels != null && selectClassifyLeabels.size() > 0)
                        || (selectStyleLabels != null && selectStyleLabels.size() > 0))){
                    Tst.showToast("请至少选择1个标签");
                    return;
                }
                MobclickAgent.onEvent(QuicklyGoodsAcitvity.this, "startFindGoodsClick");
                Map<String,Object> map=new HashMap<String, Object>();
                map.put("ClassifyLabels",selectClassifyLeabels);
                map.put("StyleLabels",selectStyleLabels);
                startActivityAnimGeneral(FastfindGoodsResultActivity.class,map);
            }
        });
        findView(R.id.btn_reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MobclickAgent.onEvent(QuicklyGoodsAcitvity.this, "restartBtnClick");
                reset();
            }
        });
    }
    private void reset(){
        if(selectClassifyLeabels != null) selectClassifyLeabels.clear();
        if(selectStyleLabels != null) selectStyleLabels.clear();
        styleLabel.onChanged();
        adapter.setNewData(classifies);

    }
    @Override
    public String setStatisticsTitle() {
        return "极速找货";
    }

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("极速找货");
    }

    @Override
    protected void reqApi() {
        RetrofitCache.load("fastgoods",RxRequestApi.getInstance().getApiService().fastfindLabels())
        .compose(this.<ResponseBody<FastLabel>>applySchedulers())
        .subscribe(new Action1<ResponseBody<FastLabel>>() {
            @Override
            public void call(ResponseBody<FastLabel> body) {
                pullToRefreshScrollView.onRefreshComplete();
                if (body.isSuccess()) {
                    updateView(body.data);
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                pullToRefreshScrollView.onRefreshComplete();
            }
        });
    }
    private void updateView(FastLabel data){
        if(data == null) return;
        if(data.styles != null){
            final LabelAdapter adapter = new LabelAdapter(data.styles);
            styleLabel.setAdapter(adapter);
        }

        if(data.classifies != null){
            classifies = data.classifies;
            adapter.setNewData(data.classifies);
        }
    }

}
