package com.knms.activity.details;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.google.gson.Gson;
import com.gyf.barlibrary.ImmersionBar;
import com.knms.activity.base.HeadDetailNotifyBaseFragmentActivity;
import com.knms.activity.im.ChatActivity;
import com.knms.activity.login.FasterLoginActivity;
import com.knms.activity.pic.CameraActivityF;
import com.knms.adapter.DecorationStyleFragmentPagerAdapter;
import com.knms.android.R;
import com.knms.app.KnmsApp;
import com.knms.bean.ResponseBody;
import com.knms.bean.other.AttachDecoration;
import com.knms.bean.style.StyleDetails;
import com.knms.bean.style.StyleId;
import com.knms.core.im.msg.Product;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.fragment.DecorationStyleDetailsFragment;
import com.knms.net.RxRequestApi;
import com.knms.util.LocalDisplay;
import com.knms.util.SPUtils;
import com.knms.util.Tst;
import com.knms.view.BdScrollView;
import com.knms.view.GuideView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import cn.sharesdk.onekeyshare.OnekeyShare;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.knms.android.R.id.viewpager;


/**
 * Created by tdx on 2016/10/25.
 * 家装风格详情
 * 若果需要无限查看下一个详情，则需要传入参数shopId&type(风格类型id)&ids(风格id数组)
 * 若只需产看单个详情，只需传入id即可
 * 其中position用于定位显示位置
 */

public class DecorationStyleDetailsActivity extends HeadDetailNotifyBaseFragmentActivity {
    ViewPager viewPager;
    public Map<String,StyleDetails> details = new HashMap<>();
    private List<StyleId> styleIds;
    private DecorationStyleFragmentPagerAdapter adapter;
    private int position;
    private String type, shopId;
    private boolean isLoadMore = false;//true正在加载更多
    private boolean isReqNext = true;//是否请求下一组数据，false不请求
    public static int currentPage, maxPage;
    private View mTopLayout;
    /*******/
    private TextView mContent;//介绍内容
    private LinearLayout mCollect, mShare, mBottomLayout;
    private ImageView imgCollect;
    private TextView mContactVendor, mcollectNum;
    private LinearLayout ivBtn_bbj;
    private BdScrollView scrollView;

    private boolean isChoose;
    @Override
    protected void getParmas(Intent intent) {
        List<String> ids = (ArrayList<String>) intent.getSerializableExtra("ids");
        if (ids != null) {
            if (styleIds == null) styleIds = new ArrayList<StyleId>();
            for (String id :
                    ids) {
                StyleId styleid = new StyleId();
                styleid.inid = id;
                styleIds.add(styleid);
            }
        } else {
            String id = intent.getStringExtra("id");
            StyleId styleId = new StyleId();
            styleId.inid = id;
            styleIds = Arrays.asList(styleId);
            isReqNext = false;
        }
        position = intent.getIntExtra("position", 0);
        type = intent.getStringExtra("type");
        shopId = intent.getStringExtra("shopId");
        if(TextUtils.isEmpty(type) && TextUtils.isEmpty(shopId)) isLoadMore = true;
        if (styleIds != null) maxPage = styleIds.size();
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_decoration_style_details_f;
    }

    @Override
    protected void initBar() {
        View barView = findView(R.id.view);
        if(barView == null) return;
        mImmersionBar = ImmersionBar.with(this);
        mImmersionBar.statusBarView(barView)
//                .keyboardEnable(true)  //解决软键盘与底部输入框冲突问题
                .statusBarDarkFont(true, 0.1f)//状态栏字体是深色，不写默认为亮色
                .flymeOSStatusBarFontColor(R.color.color_white);  //修改flyme OS状态栏字体颜色;
        mImmersionBar.init();
    }

    @Override
    protected void initView() {
        mTopLayout = findView(R.id.top_details_layout);
        viewPager = findView(viewpager);
        mBottomLayout = findView(R.id.layout);
        mcollectNum = findView(R.id.num_collect);
        mContent = findView(R.id.content);
        mCollect = findView(R.id.collect);
        mShare = findView(R.id.share);
        imgCollect = findView(R.id.img_collect);
        mContactVendor = findView(R.id.contact_vendor);
        ivBtn_bbj = findView(R.id.ivBtn_bbj);
        scrollView = findView(R.id.scrollview);
        scrollView.setMaxHeight(LocalDisplay.dp2px(120));
        findView(R.id.view_bg).setAlpha(0.3f);

        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){// 横屏
            mBottomLayout.setVisibility(View.GONE);
        } else if(this.getResources().getConfiguration().orientation ==Configuration.ORIENTATION_PORTRAIT) {// 竖屏
            View view = findView(R.id.ivBtn_bbj);
            showGriade(view);
            hidden("portrait");
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){// 横屏
            mBottomLayout.setVisibility(View.GONE);
            if(guideView != null) guideView.hide();
        } else if(this.getResources().getConfiguration().orientation ==Configuration.ORIENTATION_PORTRAIT) {// 竖屏
            hidden("portrait");
        }
        super.onConfigurationChanged(newConfig);
    }

    int collectNum = 0;
    public void setDetail(final StyleDetails data){
        if(data == null) return;
        collectNum = 0;
        isChoose = false;
        //设置页码+内容
        viewPager.postDelayed(new Runnable() {
            @Override
            public void run() {
                ViewPager auto_vp = ((DecorationStyleDetailsFragment) adapter.getFragment(viewPager.getCurrentItem())).auto_vp;
                if(auto_vp == null) return;
                mContent.setText(auto_vp.getCurrentItem() + 1 + "/" + data.imglist.size() + data.coremark);
                auto_vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                        if (data.imglist.size() != 0) {
                            mContent.setText(position + 1 + "/" + data.imglist.size() + " " + data.coremark);
                        }
                    }
                    @Override
                    public void onPageSelected(int position) {
                        if (DecorationStyleDetailsActivity.currentPage == 0 && position == 0) {
                            Tst.showToast("已是第一张图");
                        } else if (DecorationStyleDetailsActivity.maxPage == DecorationStyleDetailsActivity.currentPage + 1 && position == data.imglist.size() - 1) {
                            Tst.showToast("已是最后一张图");
                        }
                    }
                    @Override
                    public void onPageScrollStateChanged(int state) {
                    }
                });
            }
        },500);
        //设置收藏按钮
        try {
            collectNum = Integer.parseInt(data.collectNumber);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        mcollectNum.setText("收藏" + (collectNum == 0 ? "" : data.collectNumber));
        if (data.iscollectNumber.equals("0")) {
            imgCollect.setImageResource(R.drawable.shou_cang_on);
            isChoose = true;
        } else {
            imgCollect.setImageResource(R.drawable.shou_cang_off);
            isChoose = false;
        }
        mCollect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collect(data);
            }
        });
        //设置分享按钮
        mShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnekeyShare oks = new OnekeyShare();
                oks.show(DecorationStyleDetailsActivity.this, data.shareData);
            }
        });
        //设置比比货按钮
        ivBtn_bbj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewPager auto_vp = ((DecorationStyleDetailsFragment) adapter.getFragment(viewPager.getCurrentItem())).auto_vp;
                int position = auto_vp != null ? auto_vp.getCurrentItem() : 0;
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("remotePics", data.imglist);
                params.put("selectPosition", position);
                startActivityAnimGeneral(CameraActivityF.class, params);
            }
        });
        //设置联系商家按钮
        mContactVendor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!SPUtils.isLogin()) {
                    startActivityAnimGeneral(FasterLoginActivity.class, null);
                    return;
                }
                if (data == null) return;
                Map<String, Object> parmas = new HashMap<>();
                parmas.put("sid", data.ssmerchantid);
                parmas.put("shopId", data.shopid);
                Product product = new Product();
                product.content = data.coremark;
                product.icon = (data.imglist != null && data.imglist.size() > 0) ? data.imglist.get(0).url : "";
                product.price = "";
                product.productType = "1";
                product.productId = data.inid;
                product.attachJson = new Gson().toJson(new AttachDecoration(data.inid,shopId));
                parmas.put("prodcut", product);
                startActivityAnimGeneral(ChatActivity.class, parmas);
            }
        });
    }
    private void collect(final StyleDetails data){
        if(!SPUtils.isLogin()){
            startActivityAnimGeneral(FasterLoginActivity.class,null);
            return;
        }
        int type = isChoose ? 1 : 0;//0：收藏 1：取消收藏
        RxRequestApi.getInstance().getApiService().collect(data.inid,3,type)
                .observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
                .subscribe(new Action1<ResponseBody>() {
                    @Override
                    public void call(ResponseBody body) {
                        if(body.isSuccess()){
                            if(isChoose){//之前已收藏了
                                isChoose = false;
                                imgCollect.setImageResource(R.drawable.shou_cang_off);
                                collectNum -- ;
                                data.iscollectNumber = "1";
                            }else {//之前没收藏了
                                isChoose = true;
                                imgCollect.setImageResource(R.drawable.shou_cang_on);
                                collectNum ++ ;
                                data.iscollectNumber = "0";
                            }
                            data.collectNumber = collectNum + "";
                            mcollectNum.setText("收藏" + (collectNum == 0 ? "" : collectNum));
                            details.put(data.inid,data);
                        }else {
                            if ("已经收藏".equals(body.desc)){
                                reqApi();//刷新列表
                            }else if(!"需要登录".equals(body.desc)){
                                Tst.showToast(body.desc);
                            }
                        }
                    }
                });
    }
    GuideView guideView;
    private void showGriade(View view){
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.bg_griade_style_bbj);
        guideView = new GuideView.Builder()
                .setTargetView(view)    // 必须调用，设置需要Guide的View
                .setCustomGuideView(imageView)  // 必须调用，设置GuideView，可以使任意View的实例，比如ImageView 或者TextView
                .setDirction(GuideView.Direction.TOP)   // 设置GuideView 相对于TargetView的位置，有八种，不设置则默认在屏幕左上角,其余的可以显示在右上，右下等等
                .setShape(GuideView.MyShape.CIRCULAR)   // 设置显示形状，支持圆形，椭圆，矩形三种样式，矩形可以是圆角矩形，
                .setOnclickExit(true)   // 设置点击消失，可以传入一个Callback，执行被点击后的操作
                .setRadius(LocalDisplay.dp2px(25))
//                .setCenter(300, 300)    // 设置圆心，默认是targetView的中心
                .setOffset(0, -10)     // 设置偏移，一般用于微调GuideView的位置
                .showOnce()             // 设置首次显示，设置后，显示一次后，不再显示
                .build(this)                // 必须调用，Buider模式，返回GuideView实例
                .show();                // 必须调用，显示GuideView
    }
    @Override
    protected void initData() {
        adapter = new DecorationStyleFragmentPagerAdapter(getSupportFragmentManager(), styleIds);
        viewPager.setOffscreenPageLimit(0);
        viewPager.setAdapter(adapter);
        if (position >= styleIds.size()) {
            position = 0;
        }
        viewPager.setCurrentItem(position);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            //当页面在滑动的时候会调用此方法，在滑动被停止之前一直调用,position:当前页面，及你点击滑动的页面;positionOffset:当前页面偏移的百分比;positionOffsetPixels:当前页面偏移的像素位置
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                currentPage = position;
            }
            @Override//代表哪个页面被选中
            public void onPageSelected(final int position) {
                setDetail(details.get(styleIds.get(position).inid));
                if (!isReqNext) return;
                if (position == adapter.getCount() - 1) {//已经滑动到末尾了,加载下一页的ids
                    if (isLoadMore) return;
                    isLoadMore = true;
                    RxRequestApi.getInstance().getApiService().getStyleIds(type, styleIds.get(position).inid, shopId)
                            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<ResponseBody<List<StyleId>>>() {
                                @Override
                                public void call(ResponseBody<List<StyleId>> body) {
                                    isLoadMore = false;
                                    if (body.isSuccess() && body.data != null && body.data.size() > 0) {
                                        styleIds.addAll(body.data);
                                        adapter.notifyDataSetChanged();
                                        maxPage = styleIds.size();
                                    }
                                }
                            }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                    isLoadMore = false;
                                }
                            });
                }
            }

            @Override//当用手指滑动翻页时，手指按下去的时候会触发这个方法,0（什么都没做）,1(正在滑动) , 2(动完毕了) 。
            public void onPageScrollStateChanged(int state) {
            }
        });
    }
    public int getCurrentPage(){
        return viewPager.getCurrentItem();
    }
    @Override
    public String setStatisticsTitle() {
        return "家装风格详情";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(guideView != null) guideView.onDestroy();
    }
    @Subscribe(tags = {@Tag(BusAction.HIDDED_TOP_LAYOUT)})
    public void hidden(String obj) {
        if("onclick".equals(obj)){
            boolean b = View.VISIBLE == scrollView.getVisibility() ? false : true;//false没有隐藏
            if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {//切换到横屏
                mBottomLayout.setVisibility(View.GONE);
            }else{
                mBottomLayout.setVisibility(b ? View.VISIBLE : View.GONE);
            }
            mTopLayout.setVisibility(b ? View.VISIBLE : View.GONE);
            scrollView.setVisibility(b ? View.VISIBLE : View.GONE);
        }else {
            boolean b = View.VISIBLE == scrollView.getVisibility() ? false : true;//false没有隐藏
            mBottomLayout.setVisibility(!b ? View.VISIBLE : View.GONE);
        }
    }
}
