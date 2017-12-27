package com.knms.activity.mine;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.handmark.pulltorefresh.library.view.XScrollView;
import com.knms.activity.base.HeadNotifyBaseActivity;
import com.knms.activity.im.ChatActivity;
import com.knms.activity.pic.ImgBrowerPagerActivity;
import com.knms.adapter.MinePriceRationDetailAdapter;
import com.knms.adapter.baserecycler.BaseQuickAdapter;
import com.knms.adapter.baserecycler.BaseViewHolder;
import com.knms.android.R;
import com.knms.bean.ResponseBody;
import com.knms.bean.other.Pic;
import com.knms.bean.repair.MyRepair;
import com.knms.bean.user.User;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.net.RxRequestApi;
import com.knms.util.DialogHelper;
import com.knms.util.ImageLoadHelper;
import com.knms.util.LocalDisplay;
import com.knms.util.ScreenUtil;
import com.knms.util.StrHelper;
import com.knms.util.Tst;
import com.knms.view.CircleImageView;
import com.knms.view.clash.FullyGridLayoutManager;
import com.knms.view.clash.FullyLinearLayoutManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/11/3.
 */
public class MineRepairDetailActivity extends HeadNotifyBaseActivity {

    private TextView time, edit, content, reply_amount;
    private RecyclerView rl_img;
    private RecyclerView recyclerView;
    private MinePriceRationDetailAdapter adapter;
    private PullToRefreshScrollView pullToRefreshScrollView;
    private TextView tvType, tvLocation;

    private String repairId;
    private MyRepair myRepair;
    private boolean falg;
    private LinearLayout linearLayout;
    private XScrollView scrollView;
    private View view;
    private FullyLinearLayoutManager layoutManager;

    @Override
    protected void getParmas(Intent intent) {
        repairId = intent.getStringExtra("id");
        myRepair = (MyRepair) intent.getSerializableExtra("myRepair");
        falg = intent.getBooleanExtra("falg", false);
    }

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("我的维修");
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_my_repair_detail;
    }

    @Override
    protected void initView() {
        view=findView(R.id.blank_view);
        view.setVisibility(falg?View.VISIBLE:View.GONE);
        linearLayout=findView(R.id.ll_repair_detail);
        tvType = findView(R.id.type);
        tvLocation = findView(R.id.location);
        time = findView(R.id.time);
        edit = findView(R.id.edit);
        content = findView(R.id.remark);
        reply_amount = findView(R.id.id_stickynavlayout_indicator);
        rl_img = findView(R.id.rl_img);
        recyclerView = findView(R.id.id_stickynavlayout_viewpager);
        pullToRefreshScrollView = findView(R.id.pull_to_refresh);
        pullToRefreshScrollView.setMode(PullToRefreshBase.Mode.BOTH);
         layoutManager=new FullyLinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
    }

    @Override
    protected void initData() {
        if (myRepair != null) {
            repairId = myRepair.reid;
            time.setText(StrHelper.displayTime(myRepair.recreated, true, true));
            content.setText(myRepair.reremark);
            if (myRepair.commentList != null)
                reply_amount.setText("共有" + myRepair.commentList.size() + "个师傅回复");
            recyclerView.setAdapter(new BaseQuickAdapter<MyRepair.CommentList>(R.layout.item_merchant_reply, myRepair.commentList) {
                @Override
                protected void convert(BaseViewHolder helper, final MyRepair.CommentList item) {
                    helper.setText(R.id.name, item.usnickname);
                    helper.setText(R.id.reply_time, StrHelper.displayTime(item.concattime, true, true));
                    CircleImageView head_picture = helper.getView(R.id.head_picture);
                    ImageLoadHelper.getInstance().displayImageHead(MineRepairDetailActivity.this,item.usphoto, head_picture);
                    helper.setOnClickListener(R.id.merchant_one, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Map<String, Object> map = new HashMap<String, Object>();
                            map.put("sid", item.userid);
                            startActivityAnimGeneral(ChatActivity.class, map);
                        }
                    });
                }
            });
            updateView(myRepair.imglist);
            if (falg) pullToRefreshScrollView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Rect rectangle= new Rect();
                    getWindow().getDecorView().getWindowVisibleDisplayFrame(rectangle);
                    view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ScreenUtil.getScreenHeight()- LocalDisplay.dp2px(86)-layoutManager.getHeight()-rectangle.top));
                    pullToRefreshScrollView.getRefreshableView().smoothScrollTo(0, findViewById(R.id.id_stickynavlayout_topview).getHeight());
                }
            }, 200);
            if (TextUtils.isEmpty(myRepair.retype))
                tvType.setVisibility(View.GONE);
            else
                tvType.setText(myRepair.retype);
            tvLocation.setText(myRepair.rearea);
            edit.setOnClickListener(onClickListener);
        }
        if (myRepair == null) {
            reqApi();
        }
    }

    @Override
    public String setStatisticsTitle() {
        return "我的维修详情";
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            DialogHelper.showBottomDialog(MineRepairDetailActivity.this, R.layout.dialog_mine_price_ration, new DialogHelper.OnEventListener<Dialog>() {
                @Override
                public void eventListener(View parentView, final Dialog window) {
                    TextView tvUndercarriage = (TextView) parentView.findViewById(R.id.undercarriage);
                    TextView tvDelete = (TextView) parentView.findViewById(R.id.delete);
                    if (myRepair.restate == 1) {
                        tvUndercarriage.setVisibility(View.GONE);
                    } else if (myRepair.restate==0){
                        tvUndercarriage.setText("已解决");
                        tvDelete.setVisibility(View.GONE);
                    }else{
                        tvUndercarriage.setVisibility(View.GONE);
                        tvDelete.setVisibility(View.GONE);
                    }
                    tvUndercarriage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DialogHelper.showPromptDialog(MineRepairDetailActivity.this, "确定已解决了吗？", "如果选择解决了，维修师傅将不再看到此消息", "取消", "", "确定", new DialogHelper.OnMenuClick() {
                                @Override
                                public void onLeftMenuClick() {
                                }

                                @Override
                                public void onCenterMenuClick() {
                                }

                                @Override
                                public void onRightMenuClick() {
                                    updateState(repairId, 1);
                                }
                            });
//                            }
                            window.dismiss();
                        }
                    });

                    tvDelete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DialogHelper.showPromptDialog(MineRepairDetailActivity.this, "", "确定删除你的维修消息吗？", "取消", "", "确定", new DialogHelper.OnMenuClick() {
                                @Override
                                public void onLeftMenuClick() {
                                }

                                @Override
                                public void onCenterMenuClick() {
                                }

                                @Override
                                public void onRightMenuClick() {
                                    updateState(repairId, 2);
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
        }
    };

    private void updateState(String id, final int state) {
        RxRequestApi.getInstance().getApiService().updateMyRepairState(state, id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody>() {
                    @Override
                    public void call(ResponseBody responseBody) {
                        if (responseBody.isSuccess()) {
                            RxBus.get().post(BusAction.REFRESH_MAINTAIN,"");
                            finshActivity();
                        } else {
                            Tst.showToast(responseBody.desc);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Tst.showToast(throwable.getMessage());
                    }
                });
    }

    @Override
    protected void reqApi() {
        RxRequestApi.getInstance().getApiService().getRepairDetail(repairId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<MyRepair>>() {
                    @Override
                    public void call(ResponseBody<MyRepair> repairDetailResponseBody) {
                        if (repairDetailResponseBody.isSuccess()) {
                            myRepair = repairDetailResponseBody.data;
                            initData();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Tst.showToast(throwable.getMessage());
                    }
                });
    }


    private void updateView(List<String> data) {
        if (data == null) return;
        List<Pic> listpic = new ArrayList<>();
        for (String imgUrl : data) {
            Pic p = new Pic();
            p.url = imgUrl;
            listpic.add(p);
        }
        FullyGridLayoutManager layoutManager = null;
        if (data.size() == 1) {
            adapter = new MinePriceRationDetailAdapter(this,R.layout.item_picture, listpic);
            layoutManager = new FullyGridLayoutManager(this, 1);
        } else if (data.size() == 2 || data.size() == 4) {
            adapter = new MinePriceRationDetailAdapter(this,R.layout.item_picture, listpic);
            layoutManager = new FullyGridLayoutManager(this, 2);
        } else {
            adapter = new MinePriceRationDetailAdapter(this,R.layout.item_picture3_3, listpic);
            layoutManager = new FullyGridLayoutManager(this, 3);
        }
        rl_img.setLayoutManager(layoutManager);
        rl_img.setAdapter(adapter);
        rl_img.setLayoutFrozen(true);
        adapter.setOnRecyclerViewItemClickListener(new BaseQuickAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("pics", adapter.getData());
                map.put("position",position);
                startActivityAnimGeneral(ImgBrowerPagerActivity.class, map);
            }
        });
    }

    @Subscribe(tags = {@Tag(BusAction.ACTION_LOGIN)})
    public void loginAccout(User user) {
        finshActivity();
    }

}
