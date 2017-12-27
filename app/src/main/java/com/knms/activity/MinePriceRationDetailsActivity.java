package com.knms.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.knms.activity.base.HeadNotifyBaseActivity;
import com.knms.activity.details.ProductDetailsOrdinaryActivity;
import com.knms.activity.details.base.CannotBuyBaseDetailsActivity;
import com.knms.activity.im.ChatActivity;
import com.knms.activity.pic.ImgBrowerPagerActivity;
import com.knms.adapter.LabelAdapter;
import com.knms.adapter.MinePriceRationDetailAdapter;
import com.knms.adapter.MineReplyPricRationAdapter;
import com.knms.adapter.baserecycler.BaseQuickAdapter;
import com.knms.android.R;
import com.knms.bean.ResponseBody;
import com.knms.bean.myparity.MyParity;
import com.knms.bean.other.Pic;
import com.knms.bean.user.User;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.net.HttpConstant;
import com.knms.net.ReqApi;
import com.knms.net.RxRequestApi;
import com.knms.oncall.AsyncHttpCallBack;
import com.knms.util.DialogHelper;
import com.knms.util.ImageLoadHelper;
import com.knms.util.LocalDisplay;
import com.knms.util.ScreenUtil;
import com.knms.util.StrHelper;
import com.knms.util.Tst;
import com.knms.view.clash.FullyGridLayoutManager;
import com.knms.view.clash.FullyLinearLayoutManager;
import com.knms.view.flowlayout.TagFlowLayout;
import com.umeng.analytics.MobclickAgent;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/9/14.
 * 我的比价 详情界面
 */
public class MinePriceRationDetailsActivity extends HeadNotifyBaseActivity {

    private TextView time, edit, content, reply_amount;
    private ImageView mainPicture;
    private TagFlowLayout tagFlowLayout;
    private RecyclerView rl_img;
    private LabelAdapter labelAdapter;
    private RecyclerView mRecyclerView;
    private MinePriceRationDetailAdapter adapter;

    private MyParity myParity;
    private String coid, goid;
    private boolean isHidden;
    private PullToRefreshScrollView pullToRefreshScrollView;
    private FullyLinearLayoutManager layoutManager;
    private View view;
    private TextView tvDetails, tvRefresh;
    private int type = 2;//(2：个性比比价。5：内部商品比比货)
    private MineReplyPricRationAdapter replyAdapter;


    @Override
    protected void getParmas(Intent intent) {
        myParity = (MyParity) intent.getSerializableExtra("myParity");
        isHidden = intent.getBooleanExtra("isHidden", false);
        if (myParity != null) {
            coid = myParity.coid;
        }
        if (TextUtils.isEmpty(coid)) {
            coid = intent.getStringExtra("id");
            getParityDetails(coid);
        } else reqApi();

    }

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("求购/比货");
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_mine_price_ration_details;
    }

    @Override
    protected void initView() {
        tvDetails = findView(R.id.tv_look_goodsdetails);
        tvRefresh = findView(R.id.tv_refresh_myprice);
        view = findView(R.id.blank_view);
        view.setVisibility(isHidden ? View.VISIBLE : View.GONE);
        time = findView(R.id.time);
        edit = findView(R.id.edit);
        content = findView(R.id.content);
        mainPicture = findView(R.id.main_picture);
        tagFlowLayout = findView(R.id.tag_flow_layout);
        rl_img = findView(R.id.rl_img);
        mRecyclerView = findView(R.id.id_stickynavlayout_viewpager);
        reply_amount = findView(R.id.id_stickynavlayout_indicator);
        pullToRefreshScrollView = findView(R.id.pull_to_refresh_scrollview);
        pullToRefreshScrollView.setMode(PullToRefreshBase.Mode.BOTH);
        layoutManager = new FullyLinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        tvDetails.setOnClickListener(onClickListener);
        tvRefresh.setOnClickListener(onClickListener);
    }

    @Override
    protected void initData() {
        if (myParity != null) {
            goid = myParity.goid;
            type = myParity.coType;
            tvDetails.setVisibility(myParity.coType == 5 ? View.VISIBLE : View.GONE);
            tvRefresh.setVisibility(myParity.coState .equals("1") ? View.VISIBLE : View.GONE);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            if (!dateFormat.format(new Date()).equals(myParity.updatetime.split(" ")[0]) || TextUtils.equals(myParity.cocreated, myParity.updatetime)) {
                tvRefresh.setTextColor(0xffee4b62);
                tvRefresh.setText("刷新");
            } else {
                tvRefresh.setTextColor(0xff666666);
                tvRefresh.setText("已刷新");
            }
            time.setText(StrHelper.displayTime(myParity.cocreated, true, true));
            content.setText(myParity.coremark);
            ImageLoadHelper.getInstance().displayImage(this,myParity.coInspirationPic, mainPicture);
            if (!(myParity.labelList != null && myParity.labelList.size() > 0)) {
                tagFlowLayout.setVisibility(View.GONE);
            } else {
                labelAdapter = new LabelAdapter(myParity.labelList);
                tagFlowLayout.setAdapter(labelAdapter);
            }
            if (myParity.commentList != null)
                reply_amount.setText(myParity.coType==5?"为您推荐"+myParity.commentList.size() +"个同类商家":"共有" + myParity.commentList.size() + "个商家回复");
            replyAdapter=new MineReplyPricRationAdapter(listReply,type);
            mRecyclerView.setAdapter(replyAdapter);
            load();
            pullToRefreshScrollView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ScrollView>() {
                @Override
                public void onPullDownToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                    pullToRefreshScrollView.onRefreshComplete();
                }
                @Override
                public void onPullUpToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                    pullToRefreshScrollView.onRefreshComplete();
                    if (myParity.commentList == null || myParity.commentList.size() == 0) {
                        Tst.showToast("没有更多数据");
                        return;
                    }
                    load();
                }
            });
            if (isHidden) pullToRefreshScrollView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Rect rectangle = new Rect();
                    getWindow().getDecorView().getWindowVisibleDisplayFrame(rectangle);
                    view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtil.getScreenHeight() - LocalDisplay.dp2px(86) - layoutManager.getHeight() - rectangle.top));
                    pullToRefreshScrollView.getRefreshableView().scrollTo(0, findViewById(R.id.id_stickynavlayout_topview).getHeight());
                }
            }, 500);
            edit.setOnClickListener(onClickListener);

            replyAdapter.setOnRecyclerViewItemClickListener(new BaseQuickAdapter.OnRecyclerViewItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    Map<String,Object> map=new HashMap<String, Object>();
                    if (type == 5) {
                        map.put("shopId", replyAdapter.getItem(position).shopId);
                        startActivityAnimGeneral(ShopActivityF.class, map);
                        MobclickAgent.onEvent(MinePriceRationDetailsActivity.this, "quickCompareDetailAnyStoreClick");
                    } else {
                        map.put("sid", replyAdapter.getItem(position).merchantId);
                        map.put("shopId", replyAdapter.getItem(position).shopId);
                        startActivityAnimGeneral(ChatActivity.class, map);
                    }
                }
            });
        }
    }

    @Override
    public String setStatisticsTitle() {
        return "我的比比货详情";
    }

    @Override
    protected void reqApi() {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("coid", coid);
        ReqApi.getInstance().postMethod(HttpConstant.myParityDetail, params, new AsyncHttpCallBack<List<Pic>>() {
            @Override
            public void onSuccess(ResponseBody<List<Pic>> body) {
                if (body.isSuccess()) {
                    updateView(body.data);
                } else {
                    Tst.showToast(body.desc);
                }
            }

            @Override
            public void onFailure(String msg) {
                Tst.showToast(msg);
            }

            @Override
            public Type setType() {
                return new TypeToken<ResponseBody<List<Pic>>>() {
                }.getType();
            }
        });
    }

    private void updateView(final List<Pic> data) {
        if (data == null) return;
        if (data.size() == 1) {
            adapter = new MinePriceRationDetailAdapter(this,R.layout.item_picture, data);
            rl_img.setLayoutManager(new FullyGridLayoutManager(this, 1));
        } else if (data.size() == 2 || data.size() == 4) {
            adapter = new MinePriceRationDetailAdapter(this,R.layout.item_picture, data);
            rl_img.setLayoutManager(new FullyGridLayoutManager(this, 2));
        } else {
            adapter = new MinePriceRationDetailAdapter(this,R.layout.item_picture3_3, data);
            rl_img.setLayoutManager(new FullyGridLayoutManager(this, 3));
        }
        rl_img.setAdapter(adapter);
        rl_img.setLayoutFrozen(true);
        adapter.setOnRecyclerViewItemClickListener(new BaseQuickAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("pics", data);
                map.put("position", position);
                startActivityAnimGeneral(ImgBrowerPagerActivity.class, map);
            }
        });
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.tv_refresh_myprice:
                    MobclickAgent.onEvent(MinePriceRationDetailsActivity.this, "refreshOfMyCompareClick");
                    if (tvRefresh.getText().toString().equals("已刷新")) {
                        Tst.showToast("每天只能刷新一次哦~");
                        break;
                    }
                    showProgress("刷新中");
                    RxRequestApi.getInstance().getApiService().refreshMyParity(coid)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(new Action1<ResponseBody<String>>() {
                                @Override
                                public void call(ResponseBody<String> stringResponseBody) {
                                    hideProgress();
                                    if (stringResponseBody.isSuccess()) {
                                        tvRefresh.setTextColor(0xff666666);
                                        tvRefresh.setText("已刷新");
                                        Tst.showToast("刷新成功~");
                                        RxBus.get().post(BusAction.REFRESH_BBPRICE,"");
                                    }else  Tst.showToast(stringResponseBody.desc);
                                }
                            }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                    hideProgress();
                                    Tst.showToast(throwable.getMessage());
                                }
                            });
                    break;
                case R.id.tv_look_goodsdetails:
                    MobclickAgent.onEvent(MinePriceRationDetailsActivity.this, "quickCompareDetailGoDetailClick");
                    Map<String, Object> param = new HashMap<>();
                    param.put("id", goid);
                    startActivityAnimGeneral(ProductDetailsOrdinaryActivity.class, param);
                    break;
                case R.id.edit:
                    DialogHelper.showBottomDialog(MinePriceRationDetailsActivity.this, R.layout.dialog_mine_price_ration, new DialogHelper.OnEventListener<Dialog>() {
                        @Override
                        public void eventListener(View parentView, final Dialog window) {
                            TextView tvUndercarriage = (TextView) parentView.findViewById(R.id.undercarriage);
                            TextView tvDelete = (TextView) parentView.findViewById(R.id.delete);
                            tvUndercarriage.setText("已解决");
                            if ("0".equals(myParity.coState)) {
                                tvUndercarriage.setVisibility(View.GONE);
                            } else if ("1".equals(myParity.coState)) {
                                tvDelete.setVisibility(View.GONE);
                            } else {
                                tvUndercarriage.setVisibility(View.GONE);
                                tvDelete.setVisibility(View.GONE);
                            }
                            tvUndercarriage.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (myParity == null) return;
//                            if ("0".equals(myParity.coState)) {
//                                Tst.showToast("已下架");
//                            } else {
                                    DialogHelper.showPromptDialog(MinePriceRationDetailsActivity.this, "确定已解决了吗？", "如果已经解决了，商家将不再看到此消息", "取消", "", "确定", new DialogHelper.OnMenuClick() {
                                        @Override
                                        public void onLeftMenuClick() {
                                        }

                                        @Override
                                        public void onCenterMenuClick() {
                                        }

                                        @Override
                                        public void onRightMenuClick() {
                                            HashMap<String, Object> map1 = new HashMap<String, Object>();
                                            map1.put("goid", coid);//比比价id
                                            map1.put("state", 3);//3：下架，4：删除
                                            ReqApi.getInstance().postMethod(HttpConstant.myParityUpdateState, map1, new AsyncHttpCallBack() {
                                                @Override
                                                public void onSuccess(ResponseBody body) {
                                                    Tst.showToast(body.desc);
                                                    if (body.isSuccess()) {
                                                        RxBus.get().post(BusAction.REFRESH_BBPRICE,"");
                                                        finshActivity();
                                                    }
                                                }

                                                @Override
                                                public void onFailure(String msg) {
                                                    Tst.showToast(msg);
                                                }

                                                @Override
                                                public Type setType() {
                                                    return new TypeToken<ResponseBody>() {
                                                    }.getType();
                                                }
                                            });
                                        }
                                    });
//                            }
                                    window.dismiss();
                                }
                            });

                            tvDelete.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    DialogHelper.showPromptDialog(MinePriceRationDetailsActivity.this, "", "确定删除你的比价消息吗？", "取消", "", "确定", new DialogHelper.OnMenuClick() {
                                        @Override
                                        public void onLeftMenuClick() {
                                        }

                                        @Override
                                        public void onCenterMenuClick() {
                                        }

                                        @Override
                                        public void onRightMenuClick() {
                                            HashMap<String, Object> map2 = new HashMap<String, Object>();
                                            map2.put("goid", coid);
                                            map2.put("state", 4);//3：下架，4：删除
                                            ReqApi.getInstance().postMethod(HttpConstant.myParityUpdateState, map2, new AsyncHttpCallBack() {
                                                @Override
                                                public void onSuccess(ResponseBody body) {
                                                    Tst.showToast(body.desc);
                                                    if (body.isSuccess()) {
                                                        RxBus.get().post(BusAction.REFRESH_BBPRICE,"");
                                                        finshActivity();
                                                    }
                                                }

                                                @Override
                                                public void onFailure(String msg) {
                                                    Tst.showToast(msg);
                                                }

                                                @Override
                                                public Type setType() {
                                                    return new TypeToken<ResponseBody>() {
                                                    }.getType();
                                                }
                                            });
                                        }
                                    });
                                    window.dismiss();
                                }
                            });

                            parentView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    window.dismiss();
                                }
                            });
                        }
                    });
                    break;
            }
        }
    };

    private void getParityDetails(String Id) {
        showProgress();
        RxRequestApi.getInstance().getApiService().getParityDetail(Id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<MyParity>>() {
                    @Override
                    public void call(ResponseBody<MyParity> myParityResponseBody) {
                        hideProgress();
                        if (myParityResponseBody.isSuccess()) {
                            myParity = myParityResponseBody.data;
                            initData();
                            updateView(myParity.imglist);
                        } else Tst.showToast(myParityResponseBody.desc);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        hideProgress();
                        Tst.showToast(throwable.getMessage());
                    }
                });
    }


    int totalPages=1,pageIndex=1;
    private List<MyParity.Reply> listReply=new ArrayList<>();
    private void load() {
        if(myParity.commentList==null||myParity.commentList.size()==0)return;
        totalPages=myParity.commentList.size()/20;
        if(myParity.commentList.size()%20!=0)
            totalPages++;
        if (pageIndex >totalPages) {
            pullToRefreshScrollView.onRefreshComplete();
            Tst.showToast("没有更多数据");
            return;
        }
        for (int i = (pageIndex - 1) * 20; i < pageIndex * 20&&i<myParity.commentList.size(); i++) {
            listReply.add(myParity.commentList.get(i));
        }
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                replyAdapter.notifyDataSetChanged();
                pageIndex++;
            }
        });
    }


    @Subscribe(tags = {@Tag(BusAction.ACTION_LOGIN)})
    public void loginAccout(User user) {
        finshActivity();
    }
}
